package com.apexsions.battlepass.quest.gui;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class QuestMainMenu extends Gui {

    public QuestMainMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        super(plugin, player, "&8[ &a&lQUESTS MENU &8]", 36, parent);
    }

    @Override
    public void initialize() {
        fillBackground();

        // 1. Daily Quests Button (Slot 11)
        setButton(11, new GuiButton(new ItemBuilder(Material.CLOCK)
                .name("&a&lDAILY QUESTS")
                .lore(List.of(
                        "&7Misi harian yang diperbarui setiap hari.",
                        "&7Reset dalam: &e" + plugin.getQuestManager().getPeriodService().getDailyResetTimeLeft(),
                        " ",
                        "&eKlik untuk membuka Daily Quests >"
                ))
                .build(), event -> {
            new DailyQuestMenu(plugin, player, this).open();
        }));

        // 2. Weekly Quests Button (Slot 13)
        int currentWeek = plugin.getQuestManager().getPeriodService().getCurrentWeekNumber();
        setButton(13, new GuiButton(new ItemBuilder(Material.BOOK)
                .name("&e&lWEEKLY QUESTS")
                .lore(List.of(
                        "&7Misi mingguan berdasarkan periode week season.",
                        "&7Week Aktif: &fWeek " + currentWeek,
                        " ",
                        "&eKlik untuk memilih Periode Week >"
                ))
                .build(), event -> {
            new WeeklyPeriodMenu(plugin, player, this).open();
        }));

        // 3. Monthly Quests Button (Slot 15)
        int currentMonth = plugin.getQuestManager().getPeriodService().getCurrentMonthNumber();
        setButton(15, new GuiButton(new ItemBuilder(Material.WRITABLE_BOOK)
                .name("&d&lMONTHLY QUESTS")
                .lore(List.of(
                        "&7Misi bulanan dengan hadiah besar.",
                        "&7Month Aktif: &fMonth " + currentMonth,
                        " ",
                        "&eKlik untuk memilih Periode Month >"
                ))
                .build(), event -> {
            new MonthlyPeriodMenu(plugin, player, this).open();
        }));

        // Back Button (Slot 31)
        setButton(31, new BackButton(this));
    }
}
