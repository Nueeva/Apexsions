package com.apexsions.battlepass.shop.gui;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.shop.ItemRarity;
import com.apexsions.battlepass.shop.ShopCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class RarityChancesMenu extends Gui {

    private final ShopCategory category;

    public RarityChancesMenu(ApexsionsBattlepass plugin, Player player, ShopCategory category, Gui parent) {
        super(plugin, player, "&8[ &6&lPELUANG RARITY &8- " + category.name() + " ]", 36, parent);
        this.category = category;
    }

    @Override
    public void initialize() {
        fillBackground();

        // 1. Category Switcher Tabs (Slots 1, 2, 3)
        setButton(1, new GuiButton(new ItemBuilder(Material.EMERALD)
                .name(category == ShopCategory.DAILY ? "&a&l[✔] DAILY SHOP" : "&a&lDAILY SHOP")
                .lore(List.of("&7Lihat peluang rarity Daily Shop.", " ", "&eKlik untuk melihat >"))
                .build(), event -> {
            new RarityChancesMenu(plugin, player, ShopCategory.DAILY, parent).open();
        }));

        setButton(2, new GuiButton(new ItemBuilder(Material.GOLD_INGOT)
                .name(category == ShopCategory.WEEKLY ? "&e&l[✔] WEEKLY SHOP" : "&e&lWEEKLY SHOP")
                .lore(List.of("&7Lihat peluang rarity Weekly Shop.", " ", "&eKlik untuk melihat >"))
                .build(), event -> {
            new RarityChancesMenu(plugin, player, ShopCategory.WEEKLY, parent).open();
        }));

        setButton(3, new GuiButton(new ItemBuilder(Material.DIAMOND)
                .name(category == ShopCategory.MONTHLY ? "&d&l[✔] MONTHLY SHOP" : "&d&lMONTHLY SHOP")
                .lore(List.of("&7Lihat peluang rarity Monthly Shop.", " ", "&eKlik untuk melihat >"))
                .build(), event -> {
            new RarityChancesMenu(plugin, player, ShopCategory.MONTHLY, parent).open();
        }));

        // 2. Info Banner (Slot 7)
        setButton(7, new GuiButton(new ItemBuilder(Material.BOOK)
                .name("&e&lINFORMASI PELUANG ROTASI")
                .lore(List.of(
                        "&7Setiap kali shop melakukan rotasi otomatis",
                        "&7atau pemain melakukan refresh, peluang kemunculan",
                        "&7kelangkaan item dihitung berdasarkan persentase ini."
                ))
                .build()));

        // 3. Render 6 Rarities (Slots 19, 20, 21, 22, 23, 24)
        Map<ItemRarity, Double> chances = plugin.getRarityChanceService().getChances(category);
        ItemRarity[] rarities = ItemRarity.values();
        int[] slots = { 19, 20, 21, 22, 23, 24 };

        Material[] icons = {
                Material.GRAY_DYE,
                Material.LIME_DYE,
                Material.LIGHT_BLUE_DYE,
                Material.PURPLE_DYE,
                Material.GOLD_INGOT,
                Material.NETHER_STAR
        };

        for (int i = 0; i < rarities.length && i < slots.length; i++) {
            ItemRarity r = rarities[i];
            double pct = chances.getOrDefault(r, 0.0);

            setButton(slots[i], new GuiButton(new ItemBuilder(icons[i])
                    .name(r.getColor() + "&l" + r.getDisplayName())
                    .lore(List.of(
                            "&7Kategori: &f" + category.name(),
                            "&7Peluang Muncul: &a&l" + String.format("%.1f%%", pct),
                            " ",
                            "&7Multiplier Refresh: &e" + plugin.getShopRefreshService().getRarityMultiplier(r) + "x"
                    ))
                    .build()));
        }

        // Navigation
        setButton(27, new BackButton(this, parent));
        setButton(35, new CloseButton());
    }
}
