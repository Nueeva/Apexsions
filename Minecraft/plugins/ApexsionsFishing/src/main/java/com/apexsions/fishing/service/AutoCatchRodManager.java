package com.apexsions.fishing.service;

import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.model.FishingRodData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AutoCatchRodManager {

    private final ApexsionsFishing plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public final NamespacedKey keyAutoCatch;
    public final NamespacedKey keyLuckBonus;
    public final NamespacedKey keyWeightBonus;
    public final NamespacedKey keyCatchSpeed;
    public final NamespacedKey keyOrigin;
    public final NamespacedKey keyMinLevel;
    public final NamespacedKey keyRodId;

    private final Map<String, FishingRodData> rodCatalog = new LinkedHashMap<>();

    public AutoCatchRodManager(ApexsionsFishing plugin) {
        this.plugin = plugin;
        this.keyAutoCatch = new NamespacedKey(plugin, "auto_catch");
        this.keyLuckBonus = new NamespacedKey(plugin, "luck_bonus");
        this.keyWeightBonus = new NamespacedKey(plugin, "weight_bonus");
        this.keyCatchSpeed = new NamespacedKey(plugin, "catch_speed");
        this.keyOrigin = new NamespacedKey(plugin, "origin");
        this.keyRodId = new NamespacedKey(plugin, "rod_id");
        // Use global apexsions:min_level key so it matches ApexsionsCore & CustomEnchants
        this.keyMinLevel = new NamespacedKey("apexsions", "min_level");
        reload();
    }

    public void reload() {
        rodCatalog.clear();
        java.io.File file = new java.io.File(plugin.getDataFolder(), "rods.yml");
        if (!file.exists()) {
            plugin.saveResource("rods.yml", false);
        }
        org.bukkit.configuration.file.FileConfiguration cfg = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        org.bukkit.configuration.ConfigurationSection sec = cfg.getConfigurationSection("rods");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                String name = sec.getString(key + ".display-name", "<aqua>" + key + "</aqua>");
                boolean auto = sec.getBoolean(key + ".auto-catch", true);
                double luck = sec.getDouble(key + ".luck-bonus", 0.0);
                double weight = sec.getDouble(key + ".weight-bonus", 0.0);
                int speed = sec.getInt(key + ".catch-speed-seconds", 20);
                int minLvl = sec.getInt(key + ".min-level", 0);
                double priceRupiah = sec.getDouble(key + ".price-rupiah", 0.0);
                double priceDiamond = sec.getDouble(key + ".price-diamond", 0.0);
                boolean unbreak = sec.getBoolean(key + ".unbreakable", false);
                int cmd = sec.getInt(key + ".custom-model-data", 0);
                List<String> desc = sec.getStringList(key + ".description");

                FishingRodData data = new FishingRodData(key, name, auto, luck, weight, speed, minLvl, priceRupiah, priceDiamond, unbreak, cmd, desc);
                rodCatalog.put(key.toLowerCase(), data);
            }
        }
    }

    public @Nullable FishingRodData getRodData(@Nullable String id) {
        if (id == null) return null;
        return rodCatalog.get(id.toLowerCase());
    }

    public Collection<FishingRodData> getAllRods() {
        return Collections.unmodifiableCollection(rodCatalog.values());
    }

    public @Nullable ItemStack createRodById(@Nullable String id) {
        FishingRodData data = getRodData(id);
        if (data == null) return null;
        return createRod(data);
    }

    public void saveRodData(@NotNull FishingRodData data) {
        rodCatalog.put(data.getId().toLowerCase(), data);
        java.io.File file = new java.io.File(plugin.getDataFolder(), "rods.yml");
        org.bukkit.configuration.file.FileConfiguration cfg = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        String path = "rods." + data.getId() + ".";
        cfg.set(path + "display-name", data.getDisplayName());
        cfg.set(path + "auto-catch", data.isAutoCatch());
        cfg.set(path + "luck-bonus", data.getLuckBonus());
        cfg.set(path + "weight-bonus", data.getWeightBonus());
        cfg.set(path + "catch-speed-seconds", data.getCatchSpeedSeconds());
        cfg.set(path + "min-level", data.getMinLevel());
        cfg.set(path + "price-rupiah", data.getPriceRupiah());
        cfg.set(path + "price-diamond", data.getPriceDiamond());
        cfg.set(path + "unbreakable", data.isUnbreakable());
        cfg.set(path + "custom-model-data", data.getCustomModelData());
        cfg.set(path + "description", data.getDescription());

        try {
            cfg.save(file);
        } catch (java.io.IOException e) {
            plugin.getLogger().severe("Gagal menyimpan rod ke rods.yml: " + e.getMessage());
        }
    }


    public boolean isAutoCatchRod(@Nullable ItemStack item) {
        if (item == null || item.getType() != Material.FISHING_ROD || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(keyAutoCatch, PersistentDataType.BYTE) && pdc.getOrDefault(keyAutoCatch, PersistentDataType.BYTE, (byte) 0) == 1;
    }

    public double getLuckBonus(@Nullable ItemStack item) {
        if (!isAutoCatchRod(item)) return 0.0;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(keyLuckBonus, PersistentDataType.DOUBLE, 0.0);
    }

    public double getWeightBonus(@Nullable ItemStack item) {
        if (!isAutoCatchRod(item)) return 0.0;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(keyWeightBonus, PersistentDataType.DOUBLE, 0.0);
    }

    public int getCatchSpeed(@Nullable ItemStack item) {
        if (!isAutoCatchRod(item)) return 20;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(keyCatchSpeed, PersistentDataType.INTEGER, 20);
    }

    public int getMinLevel(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        return meta.getPersistentDataContainer().getOrDefault(keyMinLevel, PersistentDataType.INTEGER, 0);
    }

    public @NotNull ItemStack createRod(@NotNull FishingRodData data) {
        ItemStack item = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(mm.deserialize(data.getDisplayName()));

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (data.isAutoCatch()) {
            pdc.set(keyAutoCatch, PersistentDataType.BYTE, (byte) 1);
        }
        pdc.set(keyLuckBonus, PersistentDataType.DOUBLE, data.getLuckBonus());
        pdc.set(keyWeightBonus, PersistentDataType.DOUBLE, data.getWeightBonus());
        pdc.set(keyCatchSpeed, PersistentDataType.INTEGER, data.getCatchSpeedSeconds());
        pdc.set(keyOrigin, PersistentDataType.STRING, "official_shop");
        pdc.set(keyRodId, PersistentDataType.STRING, data.getId());

        if (data.getMinLevel() > 0) {
            pdc.set(keyMinLevel, PersistentDataType.INTEGER, data.getMinLevel());
        }

        if (data.getCustomModelData() > 0) {
            meta.setCustomModelData(data.getCustomModelData());
        }

        meta.setUnbreakable(data.isUnbreakable());

        // Build standardized lore:
        // 1. Enchants (if any)
        // 2. [Empty line]
        // 3. Syarat Level (if any)
        // 4. [Empty line]
        // 5. Spesifikasi pancingan
        rebuildRodLore(meta, data);

        item.setItemMeta(meta);
        return item;
    }

    public void rebuildRodLore(@NotNull ItemMeta meta, @NotNull FishingRodData data) {
        List<Component> lore = new ArrayList<>();

        // 1. Enchants (Vanilla display handled natively or custom if applied)
        // 2. Syarat Level
        if (data.getMinLevel() > 0) {
            lore.add(mm.deserialize("<gradient:#f39c12:#e67e22><bold>🎖 SYARAT PENGGUNAAN: </bold></gradient><yellow>Level </yellow><gold><bold>" + data.getMinLevel() + "+</bold></gold>"));
            lore.add(Component.empty());
        }

        // 3. Spesifikasi Pancingan
        lore.add(mm.deserialize("<gradient:#00c6ff:#0072ff><bold>⚙ SPESIFIKASI ALAT PANCING:</bold></gradient>"));
        if (data.isAutoCatch()) {
            lore.add(mm.deserialize("<gray>  ● Mode: <green><bold>AUTO-CATCH AKTIF</bold></green></gray>"));
        } else {
            lore.add(mm.deserialize("<gray>  ● Mode: <red>Manual</red></gray>"));
        }

        if (data.getLuckBonus() > 0) {
            int luckPct = (int) Math.round(data.getLuckBonus() * 100);
            lore.add(mm.deserialize("<gray>  ● Keberuntungan: <yellow><bold>+" + luckPct + "% Luck Multiplier</bold></yellow></gray>"));
        }

        if (data.getWeightBonus() > 0) {
            int wPct = (int) Math.round(data.getWeightBonus() * 100);
            lore.add(mm.deserialize("<gray>  ● Pengali Bobot: <gold><bold>+" + wPct + "% Bobot Tangkapan</bold></gold></gray>"));
        }

        lore.add(mm.deserialize("<gray>  ● Kecepatan Strike: <aqua>" + data.getCatchSpeedSeconds() + " Detik</aqua></gray>"));

        if (!data.getDescription().isEmpty()) {
            lore.add(Component.empty());
            for (String desc : data.getDescription()) {
                lore.add(mm.deserialize(desc));
            }
        }

        meta.lore(lore);
    }
}
