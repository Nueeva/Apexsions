package com.apexsions.battlepass.admin.gui.shop;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.shop.ShopCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminShopCategoryMenu extends Gui {

    public AdminShopCategoryMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        super(plugin, player, "&8[ &4&lSHOP EDITOR &8- Pilih Kategori ]", 36, parent);
    }

    @Override
    public void initialize() {
        fillBackground();

        // 1. Header Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.CHEST)
                .name("&6&lBATTLEPASS SHOP ITEM EDITOR")
                .lore(List.of(
                        "&7Pilih kategori shop yang ingin Anda kelola itemnya.",
                        "&7Setiap kategori memiliki katalog dan peluang rarity sendiri."
                ))
                .build()));

        // 2. Daily Shop (Slot 11)
        int dailyCount = plugin.getShopManager().getShopItems(ShopCategory.DAILY).size();
        setButton(11, new GuiButton(new ItemBuilder(Material.EMERALD_BLOCK)
                .name("&a&lDAILY SHOP KATALOG")
                .lore(List.of(
                        "&7Total Item Terdaftar: &e" + dailyCount + " items",
                        " ",
                        "&eKlik untuk mengelola item Daily Shop >"
                ))
                .build(), event -> {
            new AdminShopItemListMenu(plugin, player, ShopCategory.DAILY, this).open();
        }));

        // 3. Weekly Shop (Slot 13)
        int weeklyCount = plugin.getShopManager().getShopItems(ShopCategory.WEEKLY).size();
        setButton(13, new GuiButton(new ItemBuilder(Material.GOLD_BLOCK)
                .name("&e&lWEEKLY SHOP KATALOG")
                .lore(List.of(
                        "&7Total Item Terdaftar: &e" + weeklyCount + " items",
                        " ",
                        "&eKlik untuk mengelola item Weekly Shop >"
                ))
                .build(), event -> {
            new AdminShopItemListMenu(plugin, player, ShopCategory.WEEKLY, this).open();
        }));

        // 4. Monthly Shop (Slot 15)
        int monthlyCount = plugin.getShopManager().getShopItems(ShopCategory.MONTHLY).size();
        setButton(15, new GuiButton(new ItemBuilder(Material.DIAMOND_BLOCK)
                .name("&d&lMONTHLY SHOP KATALOG")
                .lore(List.of(
                        "&7Total Item Terdaftar: &e" + monthlyCount + " items",
                        " ",
                        "&eKlik untuk mengelola item Monthly Shop >"
                ))
                .build(), event -> {
            new AdminShopItemListMenu(plugin, player, ShopCategory.MONTHLY, this).open();
        }));

        // 5. Rarity Chances Editor (Slot 22)
        setButton(22, new GuiButton(new ItemBuilder(Material.SPYGLASS)
                .name("&b&l[📊] ATUR PELUANG RARITY CHANCES")
                .lore(List.of(
                        "&7Atur persentase (%) peluang kemunculan masing-masing",
                        "&7rarity untuk Daily, Weekly, dan Monthly (Total 100%).",
                        " ",
                        "&bKlik untuk membuka editor rarity chance >"
                ))
                .build(), event -> {
            new AdminRarityChanceEditorMenu(plugin, player, ShopCategory.DAILY, this).open();
        }));

        // Navigation
        setButton(27, new BackButton(this, parent));
        setButton(35, new CloseButton());
    }
}
