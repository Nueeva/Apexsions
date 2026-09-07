package com.apexsions.crates.shop;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.key.CrateKey;
import com.apexsions.economy.api.ApexsionsEconomyAPI;
import com.apexsions.economy.api.ApexsionsEconomyProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the Crate Key Shop configuration, pricing, currency selection, and purchases.
 */
public class CrateKeyShopManager {

    private final ApexsionsCratesPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<String, KeyShopEntry> entries = new ConcurrentHashMap<>();
    private final File configFile;

    public CrateKeyShopManager(@NotNull ApexsionsCratesPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "shop_keys.yml");
    }

    public void load() {
        entries.clear();
        if (!configFile.exists()) {
            setupDefaults();
            save();
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection keysSection = config.getConfigurationSection("keys");
        if (keysSection != null) {
            for (String keyId : keysSection.getKeys(false)) {
                ConfigurationSection section = keysSection.getConfigurationSection(keyId);
                if (section != null) {
                    Map<String, Object> map = new HashMap<>();
                    for (String key : section.getKeys(false)) {
                        map.put(key, section.get(key));
                    }
                    KeyShopEntry entry = KeyShopEntry.deserialize(keyId, map);
                    entries.put(keyId.toLowerCase(Locale.ROOT), entry);
                }
            }
        }

        // Synchronize with any keys registered in KeyManager that aren't yet in config
        syncWithRegisteredKeys();
        save();
        plugin.getLogger().info("Loaded " + entries.size() + " crate key entries in Crate Key Shop.");
    }

    public void syncWithRegisteredKeys() {
        if (plugin.getKeyManager() == null) return;
        boolean added = false;
        for (CrateKey key : plugin.getKeyManager().getKeys()) {
            String keyId = key.getId().toLowerCase(Locale.ROOT);
            if (!entries.containsKey(keyId)) {
                double defaultPrice = key.isVirtual() ? 50000.0 : 25000.0;
                String defaultCurrency = "rupiah";
                entries.put(keyId, new KeyShopEntry(keyId, true, defaultPrice, defaultCurrency));
                added = true;
            }
        }
        if (added) {
            save();
        }
    }

    private void setupDefaults() {
        if (plugin.getKeyManager() != null && !plugin.getKeyManager().getKeys().isEmpty()) {
            syncWithRegisteredKeys();
            return;
        }

        // Sensible initial defaults if keys haven't finished loading yet
        entries.put("vote", new KeyShopEntry("vote", true, 10000.0, "rupiah"));
        entries.put("common", new KeyShopEntry("common", true, 25000.0, "rupiah"));
        entries.put("rare", new KeyShopEntry("rare", true, 75000.0, "rupiah"));
        entries.put("epic", new KeyShopEntry("epic", true, 150000.0, "rupiah"));
        entries.put("legendary", new KeyShopEntry("legendary", true, 50.0, "diamond"));
        entries.put("mythic", new KeyShopEntry("mythic", true, 100.0, "diamond"));
    }

    public synchronized void save() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("settings.header-title", "<gradient:#f39c12:#e74c3c><bold>✦ TOKO CRATE KEYS ✦</bold></gradient>");

        for (Map.Entry<String, KeyShopEntry> entry : entries.entrySet()) {
            String path = "keys." + entry.getKey();
            config.set(path + ".enabled", entry.getValue().isEnabled());
            config.set(path + ".price", entry.getValue().getPrice());
            config.set(path + ".currency", entry.getValue().getCurrency());
        }

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save shop_keys.yml: " + e.getMessage());
        }
    }

    @NotNull
    public Collection<KeyShopEntry> getAllEntries() {
        syncWithRegisteredKeys();
        return Collections.unmodifiableCollection(entries.values());
    }

    @NotNull
    public List<KeyShopEntry> getEnabledEntries() {
        syncWithRegisteredKeys();
        List<KeyShopEntry> list = new ArrayList<>();
        for (KeyShopEntry entry : entries.values()) {
            if (entry.isEnabled()) {
                list.add(entry);
            }
        }
        list.sort(Comparator.comparingDouble(KeyShopEntry::getPrice));
        return list;
    }

    @Nullable
    public KeyShopEntry getEntry(@NotNull String keyId) {
        return entries.get(keyId.toLowerCase(Locale.ROOT));
    }

    @NotNull
    public KeyShopEntry getOrCreateEntry(@NotNull String keyId) {
        return entries.computeIfAbsent(keyId.toLowerCase(Locale.ROOT), k -> {
            KeyShopEntry entry = new KeyShopEntry(k, true, 25000.0, "rupiah");
            save();
            return entry;
        });
    }

    @NotNull
    public String formatPrice(double price, @NotNull String currency) {
        if (ApexsionsEconomyProvider.isAvailable()) {
            try {
                return ApexsionsEconomyProvider.get().format(price, currency);
            } catch (Exception ignored) {}
        }
        if ("diamond".equalsIgnoreCase(currency)) {
            return (long) price + " 💎";
        }
        return "Rp " + String.format("%,d", (long) price).replace(',', '.');
    }

    public double getPlayerBalance(@NotNull Player player, @NotNull String currency) {
        if (ApexsionsEconomyProvider.isAvailable()) {
            try {
                return ApexsionsEconomyProvider.get().getBalance(player.getUniqueId(), currency);
            } catch (Exception ignored) {}
        }
        return 0.0;
    }

    /**
     * Executes key purchase transaction.
     */
    public boolean buyKey(@NotNull Player player, @NotNull String keyId, int amount) {
        if (amount <= 0) return false;

        CrateKey key = plugin.getKeyManager().getKeyById(keyId);
        if (key == null) {
            player.sendMessage(mm.deserialize("<red>Kunci crate '<yellow>" + keyId + "</yellow>' tidak ditemukan!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        KeyShopEntry entry = getEntry(keyId);
        if (entry == null || !entry.isEnabled()) {
            player.sendMessage(mm.deserialize("<red>Kunci ini saat ini tidak dijual di toko!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        double unitPrice = entry.getPrice();
        double totalPrice = unitPrice * amount;
        String currency = entry.getCurrency();
        String formattedPrice = formatPrice(totalPrice, currency);

        // Economy check
        if (ApexsionsEconomyProvider.isAvailable()) {
            ApexsionsEconomyAPI eco = ApexsionsEconomyProvider.get();
            if (!eco.has(player.getUniqueId(), currency, totalPrice)) {
                player.sendMessage(mm.deserialize("<red>Saldo <gold>" + currency.toUpperCase() + "</gold> kamu tidak mencukupi! Butuh <gold>" + formattedPrice + "</gold>.</red>"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return false;
            }

            boolean withdrawn = eco.withdraw(player.getUniqueId(), currency, totalPrice);
            if (!withdrawn) {
                player.sendMessage(mm.deserialize("<red>Gagal memproses transaksi ekonomi!</red>"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return false;
            }
        }

        // Give the keys (virtual or physical)
        plugin.getKeyManager().giveKey(player, key, amount);

        // Feedback
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.sendMessage(mm.deserialize("<gradient:#2ecc71:#f1c40f><bold>✔ PEMBELIAN BERHASIL!</bold></gradient> <green>Kamu membeli <yellow>" + amount + "x " + key.getName() + "</yellow> seharga <gold>" + formattedPrice + "</gold>!</green>"));
        player.sendActionBar(mm.deserialize("<gradient:#2ecc71:#f1c40f><bold>+" + amount + "x " + key.getName() + "</bold></gradient>"));
        return true;
    }
}
