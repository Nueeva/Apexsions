package com.apex.battlepass.admin.gui;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.navigation.CloseButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.shop.ItemRarity;
import com.apex.battlepass.shop.ShopCategory;
import com.apex.battlepass.shop.refresh.ShopRefreshService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

public class AdminShopMenu extends Gui {

    public AdminShopMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        super(plugin, player, plugin.getGuiConfig().getString("titles.admin-shop", "&8[ &4&lABP SHOP MANAGEMENT &8]"), 54, parent);
    }

    @Override
    public void initialize() {
        fillBackground();

        ShopRefreshService refreshService = plugin.getShopRefreshService();
        int dailyCount = plugin.getShopManager().getShopItems(ShopCategory.DAILY).size();
        int weeklyCount = plugin.getShopManager().getShopItems(ShopCategory.WEEKLY).size();
        int monthlyCount = plugin.getShopManager().getShopItems(ShopCategory.MONTHLY).size();

        // 1. Shop Catalog Summary Card (Slot 13)
        setButton(13, new GuiButton(new ItemBuilder(Material.CHEST)
                .name("&6&lKATALOG BATTLEPASS SHOP")
                .lore(List.of(
                        "&7Daily Shop Items: &e" + dailyCount + " items",
                        "&7Weekly Shop Items: &e" + weeklyCount + " items",
                        "&7Monthly Shop Items: &e" + monthlyCount + " items",
                        " ",
                        "&7Kategori item mendukung: &fCosmetics, Titles,",
                        "&fEffects, Special Rewards, Materials, & Consumables.",
                        "&7Tingkat Kelangkaan: &fCommon -> Mythic"
                ))
                .build()));

        // 2. Base Refresh Cost Adjuster (Slot 20)
        setButton(20, new GuiButton(new ItemBuilder(Material.GOLD_INGOT)
                .name("&e&lBASE REFRESH COST: &f" + refreshService.getBaseCost() + " Coins")
                .lore(List.of(
                        "&7Biaya dasar untuk setiap kali refresh shop.",
                        " ",
                        "&a[Klik Kiri] &7+10 Coins",
                        "&c[Klik Kanan] &7-10 Coins"
                ))
                .build(), event -> {
            if (event.getClick() == ClickType.RIGHT) {
                refreshService.setBaseCost(Math.max(10, refreshService.getBaseCost() - 10));
            } else {
                refreshService.setBaseCost(refreshService.getBaseCost() + 10);
            }
            open();
        }));

        // 3. Minimum Refresh Cost Adjuster (Slot 21)
        setButton(21, new GuiButton(new ItemBuilder(Material.COPPER_INGOT)
                .name("&6&lMINIMUM REFRESH COST: &f" + refreshService.getMinCost() + " Coins")
                .lore(List.of(
                        "&7Batas bawah harga refresh shop.",
                        " ",
                        "&a[Klik Kiri] &7+5 Coins",
                        "&c[Klik Kanan] &7-5 Coins"
                ))
                .build(), event -> {
            if (event.getClick() == ClickType.RIGHT) {
                refreshService.setMinCost(Math.max(5, refreshService.getMinCost() - 5));
            } else {
                refreshService.setMinCost(refreshService.getMinCost() + 5);
            }
            open();
        }));

        // 4. Maximum Refresh Cost Adjuster (Slot 22)
        setButton(22, new GuiButton(new ItemBuilder(Material.NETHERITE_INGOT)
                .name("&c&lMAXIMUM REFRESH COST: &f" + refreshService.getMaxCost() + " Coins")
                .lore(List.of(
                        "&7Batas atas harga refresh shop.",
                        " ",
                        "&a[Klik Kiri] &7+50 Coins",
                        "&c[Klik Kanan] &7-50 Coins"
                ))
                .build(), event -> {
            if (event.getClick() == ClickType.RIGHT) {
                refreshService.setMaxCost(Math.max(refreshService.getMinCost(), refreshService.getMaxCost() - 50));
            } else {
                refreshService.setMaxCost(refreshService.getMaxCost() + 50);
            }
            open();
        }));

        // 5. Cooldown Seconds Adjuster (Slot 23)
        setButton(23, new GuiButton(new ItemBuilder(Material.CLOCK)
                .name("&b&lCOOLDOWN REFRESH: &f" + refreshService.getCooldownSeconds() + " Detik")
                .lore(List.of(
                        "&7Proteksi anti-spam cooldown antara refresh.",
                        " ",
                        "&a[Klik Kiri] &7+1 Detik",
                        "&c[Klik Kanan] &7-1 Detik"
                ))
                .build(), event -> {
            if (event.getClick() == ClickType.RIGHT) {
                refreshService.setCooldownSeconds(Math.max(1, refreshService.getCooldownSeconds() - 1));
            } else {
                refreshService.setCooldownSeconds(refreshService.getCooldownSeconds() + 1);
            }
            open();
        }));

        // 6. Rarity Multipliers Overview Card (Slot 24)
        List<String> rarityLore = new ArrayList<>();
        rarityLore.add("&7Multiplier bobot kelangkaan item:");
        rarityLore.add(" ");
        for (ItemRarity r : ItemRarity.values()) {
            rarityLore.add(" &8- " + r.getColor() + r.getDisplayName() + "&7: &e" + refreshService.getRarityMultiplier(r) + "x");
        }
        rarityLore.add(" ");
        rarityLore.add("&7Scaling per refresh hari ini: &a+" + (int)(refreshService.getScalingPerRefresh() * 100) + "%");

        setButton(24, new GuiButton(new ItemBuilder(Material.AMETHYST_SHARD)
                .name("&d&lRARITY & SCALING MULTIPLIERS")
                .lore(rarityLore)
                .build()));

        // 7. Action: Reset All Players Daily Refresh Counter (Slot 30)
        setButton(30, new GuiButton(new ItemBuilder(Material.TNT)
                .name("&4&lRESET COUNTER REFRESH SEMUA PLAYER")
                .lore(List.of(
                        "&7Reset counter refresh harian seluruh player",
                        "&7kembali ke 0 (menormalkan scaling refresh).",
                        " ",
                        "&cKlik untuk membuka konfirmasi reset >"
                ))
                .build(), event -> {
            new AdminConfirmMenu(plugin, player, "&8[ &4&lRESET ALL REFRESH COUNTERS &8]",
                    List.of(
                            "&7Apakah Anda yakin ingin mereset counter",
                            "&7refresh harian untuk seluruh player online & offline?"
                    ),
                    this,
                    () -> {
                        refreshService.resetAllDailyRefreshCounts();
                        player.sendMessage(plugin.getMessage("shop-refresh-reset-all"));
                        open();
                    },
                    this::open
            ).open();
        }));

        // 8. Action: Reload Shop Configurations (Slot 32)
        setButton(32, new GuiButton(new ItemBuilder(Material.EMERALD_BLOCK)
                .name("&a&lRELOAD KATALOG SHOP")
                .lore(List.of(
                        "&7Muat ulang daily.yml, weekly.yml, monthly.yml,",
                        "&7serta parameter refresh dari config.yml.",
                        " ",
                        "&eKlik untuk reload shop >"
                ))
                .build(), event -> {
            plugin.getShopManager().loadShop();
            refreshService.loadConfiguration();
            player.sendMessage("§a[Apexsions BP] Berhasil memuat ulang seluruh katalog shop!");
            open();
        }));

        // 0. Edit Shop Items & Chances Mode (Slot 8)
        setButton(8, new GuiButton(new ItemBuilder(Material.ANVIL)
                .name("&a&l[✏] MODE EDIT SHOP & CHANCES")
                .lore(List.of(
                        "&7Kelola katalog item (tambah/edit/hapus)",
                        "&7dan atur persentase peluang Rarity Chances.",
                        " ",
                        "&aKlik untuk membuka editor shop >"
                ))
                .build(), event -> {
            new com.apex.battlepass.admin.gui.shop.AdminShopCategoryMenu(plugin, player, this).open();
        }));

        // Navigation
        setButton(45, new BackButton(this, parent));
        setButton(53, new CloseButton());
    }
}
