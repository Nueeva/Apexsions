package com.apexsions.battlepass.admin.gui.quest;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.quest.model.QuestCategory;
import com.apexsions.battlepass.quest.service.QuestPeriodService;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminQuestCategoryMenu extends Gui {

    public AdminQuestCategoryMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        super(plugin, player, "&8[ &4&lQUEST EDITOR &8- Pilih Kategori ]", 45, parent);
    }

    @Override
    public void initialize() {
        fillBackground();

        // 1. Header Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.WRITABLE_BOOK)
                .name("&6&lQUEST MANAGEMENT & EDITOR")
                .lore(List.of(
                        "&7Pilih kategori dan periode quest yang",
                        "&7ingin Anda buat, edit, atau sesuaikan.",
                        " ",
                        "&7Pilihan: Daily, Weekly (1-5), Monthly (1-3)"
                ))
                .build()));

        // 2. Daily Quests Button (Slot 11)
        setButton(11, new GuiButton(new ItemBuilder(Material.SUNFLOWER)
                .name("&e&lDAILY QUESTS")
                .lore(List.of(
                        "&7Kelola seluruh quest harian pemain.",
                        " ",
                        "&eKlik untuk membuka daftar Daily Quests >"
                ))
                .build(), event -> {
            new AdminQuestListMenu(plugin, player, QuestCategory.DAILY, 0, this).open();
        }));

        // 3. Weekly Quests Buttons (Slots 19, 20, 21, 22, 23)
        for (int w = 1; w <= 4; w++) {
            final int weekIdx = w;
            setButton(18 + w, new GuiButton(new ItemBuilder(Material.BOOK)
                    .name("&6&lWEEKLY QUESTS - WEEK " + w)
                    .lore(List.of(
                            "&7Kelola quest mingguan untuk Minggu ke-" + w + ".",
                            " ",
                            "&eKlik untuk membuka >"
                    ))
                    .build(), event -> {
                new AdminQuestListMenu(plugin, player, QuestCategory.WEEKLY, weekIdx, this).open();
            }));
        }

        // Special Week 5 Button (Slot 24)
        setButton(24, new GuiButton(new ItemBuilder(Material.NETHER_STAR)
                .name("&d&lSPECIAL WEEK (WEEK 5)")
                .lore(List.of(
                        "&7Quest khusus untuk bulan yang memiliki Minggu ke-5.",
                        " ",
                        "&eKlik untuk membuka >"
                    ))
                    .build(), event -> {
            new AdminQuestListMenu(plugin, player, QuestCategory.WEEKLY, QuestPeriodService.SPECIAL_WEEK_INDEX, this).open();
        }));

        // 4. Monthly Quests Buttons (Slots 29, 31, 33)
        for (int m = 1; m <= 3; m++) {
            final int monthIdx = m;
            int slot = (m == 1) ? 29 : (m == 2) ? 31 : 33;
            setButton(slot, new GuiButton(new ItemBuilder(Material.DIAMOND)
                    .name("&b&lMONTHLY QUESTS - MONTH " + m)
                    .lore(List.of(
                            "&7Kelola quest bulanan untuk Bulan ke-" + m + " (dari 3 Bulan Season).",
                            " ",
                            "&eKlik untuk membuka >"
                    ))
                    .build(), event -> {
                new AdminQuestListMenu(plugin, player, QuestCategory.MONTHLY, monthIdx, this).open();
            }));
        }

        // Navigation
        setButton(36, new BackButton(this, parent));
        setButton(44, new CloseButton());
    }
}
