package com.apexsions.fishing.gui;

import com.apexsions.economy.api.ApexsionsEconomyAPI;
import com.apexsions.economy.api.ApexsionsEconomyProvider;
import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.model.PlayerFishingStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * Virtual Bait Quota Shop GUI (/fish bait).
 * Zero Inventory Clutter: Packages directly add to player's virtual bait balance.
 */
public class BaitShopGUI implements InventoryHolder {

    private final ApexsionsFishing plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final Map<Integer, BaitPackageData> slotToPackage = new HashMap<>();

    public record BaitPackageData(String key, String displayName, int amount, long priceRupiah, int priceDiamond, Material material, List<String> desc) {}

    public BaitShopGUI(@NotNull ApexsionsFishing plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 36, mm.deserialize("<gradient:#00c6ff:#0072ff><bold>Toko Kuota Umpan Virtual</bold></gradient>"));
        render();
    }

    public void open() {
        render();
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void render() {
        inventory.clear();
        slotToPackage.clear();

        PlayerFishingStats stats = plugin.getVaultStorage().getStats(player.getUniqueId());

        // Background Glass
        ItemStack borderGlass = createGuiItem(Material.CYAN_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < 36; i++) {
            if (i < 9 || i >= 27 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderGlass);
            }
        }

        // Slot 4: Player Virtual Bait Info
        inventory.setItem(4, createGuiItem(Material.NETHER_STAR,
                "<gradient:#00c6ff:#0072ff><bold>⚡ SALDO KUOTA UMPAN VIRTUAL ⚡</bold></gradient>",
                List.of(
                        "<gray>Pemilik: <white>" + player.getName() + "</white></gray>",
                        "<gray>Saldo Kuota Saat Ini: <gold><bold>" + String.format("%,d", stats.getVirtualBait()) + " Poin Kuota</bold></gold></gray>",
                        "",
                        "<dark_gray>● 1 Kuota otomatis terpotong saat pancingan Auto-Catch menyambar.</dark_gray>",
                        "<dark_gray>● Tanpa item fisik di tas, inventori Anda tetap 100% lega.</dark_gray>",
                        "<dark_gray>● Pancingan biasa (vanilla) bebas digunakan tanpa kuota umpan.</dark_gray>"
                )));

        // Load Packages from baits.yml
        File baitsFile = new File(plugin.getDataFolder(), "baits.yml");
        if (!baitsFile.exists()) {
            plugin.saveResource("baits.yml", false);
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(baitsFile);
        ConfigurationSection sec = cfg.getConfigurationSection("packages");

        int[] packageSlots = {19, 21, 23, 25};
        int idx = 0;

        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                if (idx >= packageSlots.length) break;

                String name = sec.getString(key + ".display-name", "<yellow>" + key + "</yellow>");
                int amount = sec.getInt(key + ".amount", 50);
                long priceRupiah = sec.getLong(key + ".price-rupiah", 50000);
                int priceDiamond = sec.getInt(key + ".price-diamond", 10);
                String matStr = sec.getString(key + ".material", "GLOW_BERRIES");
                Material mat = Material.matchMaterial(matStr);
                if (mat == null) mat = Material.GLOW_BERRIES;
                List<String> desc = sec.getStringList(key + ".description");

                BaitPackageData pkg = new BaitPackageData(key, name, amount, priceRupiah, priceDiamond, mat, desc);
                int slot = packageSlots[idx++];
                slotToPackage.put(slot, pkg);

                List<String> lore = new ArrayList<>();
                for (String d : desc) {
                    lore.add(d);
                }
                lore.add("");
                lore.add("<yellow>✦ Jumlah Kuota:</yellow> <gold><bold>+" + String.format("%,d", amount) + " Kuota</bold></gold>");
                lore.add("<yellow>✦ Harga Rupiah:</yellow> <green>Rp " + String.format("%,d", priceRupiah) + "</green>");
                lore.add("<yellow>✦ Harga Diamond:</yellow> <aqua>" + String.format("%,d", priceDiamond) + " Diamond</aqua>");
                lore.add("");
                lore.add("<green>● Klik Kiri:</green> <white>Beli dengan Saldo Rupiah</white>");
                lore.add("<aqua>● Klik Kanan:</aqua> <white>Beli dengan Diamond</white>");

                inventory.setItem(slot, createGuiItem(mat, name, lore));
            }
        }

        // Slot 31: Close Button
        inventory.setItem(31, createGuiItem(Material.BARRIER,
                "<red><bold>✖ Tutup Toko</bold></red>",
                List.of("<gray>Klik untuk menutup menu.</gray>")));
    }

    public void handleClick(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 31) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }

        BaitPackageData pkg = slotToPackage.get(slot);
        if (pkg == null) return;

        boolean isRightClick = event.getClick().isRightClick();

        if (!ApexsionsEconomyProvider.isAvailable()) {
            player.sendMessage(mm.deserialize("<red>Sistem ekonomi sedang tidak tersedia.</red>"));
            return;
        }
        ApexsionsEconomyAPI eco = ApexsionsEconomyProvider.get();

        if (isRightClick) {
            // Purchase with Diamond
            if (!eco.has(player.getUniqueId(), "diamond", pkg.priceDiamond)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<red>Diamond Anda tidak mencukupi! Diperlukan <aqua>" + pkg.priceDiamond + " Diamond</aqua>.</red>"));
                return;
            }

            eco.withdraw(player.getUniqueId(), "diamond", pkg.priceDiamond);
            completePurchase(pkg, "Diamond", String.valueOf(pkg.priceDiamond));
        } else {
            // Purchase with Rupiah
            if (!eco.has(player.getUniqueId(), "rupiah", pkg.priceRupiah)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<red>Saldo Rupiah Anda tidak mencukupi! Diperlukan <yellow>Rp " + String.format("%,d", pkg.priceRupiah) + "</yellow>.</red>"));
                return;
            }

            eco.withdraw(player.getUniqueId(), "rupiah", pkg.priceRupiah);
            completePurchase(pkg, "Rupiah", "Rp " + String.format("%,d", pkg.priceRupiah));
        }
    }

    private void completePurchase(BaitPackageData pkg, String currencyType, String formattedPrice) {
        PlayerFishingStats stats = plugin.getVaultStorage().getStats(player.getUniqueId());
        stats.addVirtualBait(pkg.amount);
        plugin.getVaultStorage().savePlayerData(player.getUniqueId());

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.4f);
        player.sendMessage(mm.deserialize("<green><bold>PEMBELIAN BERHASIL!</bold> Anda membeli </green>")
                .append(mm.deserialize(pkg.displayName))
                .append(mm.deserialize("<green> seharga <yellow>" + formattedPrice + " (" + currencyType + ")</yellow>!</green>")));
        player.sendMessage(mm.deserialize("<gray>Saldo kuota umpan virtual Anda sekarang: <gold><bold>" + String.format("%,d", stats.getVirtualBait()) + " Poin</bold></gold>.</gray>"));

        render();
    }

    private ItemStack createGuiItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            List<Component> compLore = new ArrayList<>();
            for (String l : lore) {
                compLore.add(mm.deserialize(l));
            }
            meta.lore(compLore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
