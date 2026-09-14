package com.apexsions.fishing.gui;

import com.apexsions.economy.api.ApexsionsEconomyAPI;
import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.model.PlayerVaultData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dual-Currency Vault Expansion Shop GUI.
 * Pages 2-5: Buyable with Rupiah OR Diamond.
 * Pages 6-30: Restricted strictly to Diamond currency.
 */
public class VaultShopGUI implements InventoryHolder {

    private final ApexsionsFishing plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // Map slot index to page number (pages 2 to 30)
    private final Map<Integer, Integer> slotToPage = new HashMap<>();

    public VaultShopGUI(@NotNull ApexsionsFishing plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gold><bold>Toko Halaman Brankas Mancing</bold></gold>"));
        render();
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void render() {
        inventory.clear();
        slotToPage.clear();

        PlayerVaultData vaultData = plugin.getVaultStorageManager().getVaultData(player.getUniqueId());
        int unlocked = vaultData.getUnlockedPages();

        File pricesFile = new File(plugin.getDataFolder(), "vault-prices.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(pricesFile);

        // Fill background border
        ItemStack grayGlass = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, grayGlass);
            }
        }

        // Available content slots: rows 1, 2, 3, 4 (cols 1 to 7) -> 7 * 4 = 28 slots, plus col 0 on row 5
        // Let's map pages 2 to 30 (29 pages):
        int[] contentSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43,
                44
        };

        for (int i = 0; i < 29; i++) {
            int pageNum = i + 2; // page 2 to 30
            int slot = contentSlots[i];
            slotToPage.put(slot, pageNum);

            long priceRupiah = cfg.getLong("pages." + pageNum + ".price-rupiah", 0);
            int priceDiamond = cfg.getInt("pages." + pageNum + ".price-diamond", 0);

            if (pageNum <= unlocked) {
                // Already unlocked
                inventory.setItem(slot, createGuiItem(Material.LIME_STAINED_GLASS_PANE,
                        "<green><bold>Halaman " + pageNum + " [TERBUKA]</bold></green>",
                        List.of(
                                "<gray>Kapasitas: <yellow>45 Slot</yellow></gray>",
                                "<green>✔ Sudah dibeli & siap digunakan.</green>"
                        )));
            } else if (pageNum == unlocked + 1) {
                // Next page available for purchase!
                boolean canUseRupiah = (pageNum <= 5 && priceRupiah > 0);
                List<String> lore = new ArrayList<>();
                lore.add("<gray>Beli untuk menambah kapasitas brankas!</gray>");
                lore.add("");

                if (canUseRupiah) {
                    lore.add("<yellow>✦ Harga Rupiah:</yellow> <gold><bold>Rp " + String.format("%,d", priceRupiah) + "</bold></gold>");
                    lore.add("<aqua>✦ Harga Diamond:</aqua> <blue><bold>" + String.format("%,d", priceDiamond) + " Diamond</bold></blue>");
                    lore.add("");
                    lore.add("<yellow>[Klik Kiri]</yellow> <white>Beli dengan Rupiah</white>");
                    lore.add("<aqua>[Klik Kanan]</aqua> <white>Beli dengan Diamond</white>");
                } else {
                    lore.add("<red>● Pembelian Rupiah Maksimal Halaman 5!</red>");
                    lore.add("<aqua>✦ Harga Diamond:</aqua> <blue><bold>" + String.format("%,d", priceDiamond) + " Diamond</bold></blue>");
                    lore.add("");
                    lore.add("<aqua>[Klik]</aqua> <white>Beli dengan Diamond</white>");
                }

                Material mat = canUseRupiah ? Material.GOLD_BLOCK : Material.DIAMOND_BLOCK;
                inventory.setItem(slot, createGuiItem(mat,
                        "<gradient:#ffaa00:#ffd700><bold>Buka Halaman " + pageNum + "</bold></gradient>",
                        lore));
            } else {
                // Locked future page
                inventory.setItem(slot, createGuiItem(Material.BARRIER,
                        "<red><bold>Halaman " + pageNum + " [TERKUNCI]</bold></red>",
                        List.of(
                                "<dark_gray>Buka Halaman " + (pageNum - 1) + " terlebih dahulu!</dark_gray>"
                        )));
            }
        }

        // Header info at slot 4
        inventory.setItem(4, createGuiItem(Material.CHEST,
                "<gradient:#f39c12:#e67e22><bold>Brankas Pemancing Apexsions</bold></gradient>",
                List.of(
                        "<gray>Total Terbuka: <gold><bold>" + unlocked + " / 30 Halaman</bold></gold></gray>",
                        "<gray>Mata Uang Tersedia:</gray>",
                        "<yellow>● Halaman 2-5:</yellow> <gray>Bisa Rupiah / Diamond</gray>",
                        "<aqua>● Halaman 6-30:</aqua> <blue>Eksklusif Diamond</blue>"
                )));

        // Back button at slot 49
        inventory.setItem(49, createGuiItem(Material.ARROW,
                "<yellow><bold>◀ Kembali ke Brankas Mancing</bold></yellow>",
                List.of("<gray>Klik untuk kembali ke penyimpanan.</gray>")));
    }

    public void handleClick(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            new FishingVaultGUI(plugin, player).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }

        if (!slotToPage.containsKey(slot)) {
            return;
        }

        int targetPage = slotToPage.get(slot);
        PlayerVaultData vaultData = plugin.getVaultStorageManager().getVaultData(player.getUniqueId());
        int unlocked = vaultData.getUnlockedPages();

        if (targetPage <= unlocked) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<yellow>Halaman " + targetPage + " sudah Anda miliki!</yellow>"));
            return;
        }

        if (targetPage > unlocked + 1) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red>Anda harus membuka Halaman " + (unlocked + 1) + " terlebih dahulu!</red>"));
            return;
        }

        File pricesFile = new File(plugin.getDataFolder(), "vault-prices.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(pricesFile);
        long priceRupiah = cfg.getLong("pages." + targetPage + ".price-rupiah", 0);
        int priceDiamond = cfg.getInt("pages." + targetPage + ".price-diamond", 0);

        boolean buyWithDiamond = event.getClick().isRightClick() || targetPage > 5;

        // Check restriction on Rupiah for pages > 5
        if (!buyWithDiamond && targetPage > 5) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red>⚠ Pembelian menggunakan Rupiah dibatasi hanya sampai Halaman 5! Gunakan Diamond untuk halaman ini.</red>"));
            return;
        }

        if (!com.apexsions.economy.api.ApexsionsEconomyProvider.isAvailable()) {
            player.sendMessage(mm.deserialize("<red>Sistem ekonomi sedang tidak tersedia.</red>"));
            return;
        }
        ApexsionsEconomyAPI eco = com.apexsions.economy.api.ApexsionsEconomyProvider.get();

        if (buyWithDiamond) {
            // Purchase with Diamond
            if (!eco.has(player.getUniqueId(), "diamond", priceDiamond)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<red>Diamond Anda tidak mencukupi! Diperlukan <aqua>" + priceDiamond + " Diamond</aqua>.</red>"));
                return;
            }

            eco.withdraw(player.getUniqueId(), "diamond", priceDiamond);
            completePurchase(targetPage, "Diamond", String.valueOf(priceDiamond));
        } else {
            // Purchase with Rupiah (Pages 2-5)
            if (!eco.has(player.getUniqueId(), "rupiah", priceRupiah)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<red>Rupiah Anda tidak mencukupi! Diperlukan <yellow>Rp " + String.format("%,d", priceRupiah) + "</yellow>.</red>"));
                return;
            }

            eco.withdraw(player.getUniqueId(), "rupiah", priceRupiah);
            completePurchase(targetPage, "Rupiah", "Rp " + String.format("%,d", priceRupiah));
        }
    }

    private void completePurchase(int page, String currencyName, String formattedCost) {
        PlayerVaultData vaultData = plugin.getVaultStorageManager().getVaultData(player.getUniqueId());
        vaultData.setUnlockedPages(page);
        plugin.getVaultStorageManager().savePlayerData(player.getUniqueId());

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        player.sendMessage(mm.deserialize("<green><bold>SELAMAT!</bold> Berhasil membuka <gold>Halaman Penyimpanan Mancing ke-" + page + "</gold> seharga <yellow>" + formattedCost + " " + currencyName + "</yellow>!</green>"));

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
