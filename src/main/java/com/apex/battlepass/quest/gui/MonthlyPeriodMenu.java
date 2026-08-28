package com.apex.battlepass.quest.gui;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.quest.model.QuestStatus;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class MonthlyPeriodMenu extends Gui {

    public MonthlyPeriodMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        super(plugin, player, "&8[ &d&lMONTHLY QUEST PERIODS &8]", 36, parent);
    }

    @Override
    public void initialize() {
        fillBackground();

        int currentMonth = plugin.getQuestManager().getPeriodService().getCurrentMonthNumber();
        String timeLeft = plugin.getQuestManager().getPeriodService().getMonthlyTimeLeft();

        // 1. Info Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.CLOCK)
                .name("&d&lPERIODE BULANAN SEASON")
                .lore(List.of(
                        "&7Bulan Berjalan: &fMonth " + currentMonth + " &8(dari 3 Bulan Season)",
                        "&7Sisa Waktu Bulan Ini: &a" + timeLeft,
                        "&7Quest bulanan dirancang untuk progres jangka panjang!"
                ))
                .build()));

        // 2. Month 1, Month 2, Month 3 Buttons (Slots 11, 13, 15)
        int[] slots = { 11, 13, 15 };
        String[] monthNames = { "Month 1 (Bulan Ke-1)", "Month 2 (Bulan Ke-2)", "Month 3 (Bulan Ke-3)" };

        for (int m = 1; m <= 3; m++) {
            final int monthNum = m;
            QuestStatus status = plugin.getQuestManager().getPeriodService().getMonthlyPeriodStatus(monthNum);

            Material mat;
            String statusTag;
            List<String> lore;

            if (status == QuestStatus.ACTIVE) {
                mat = Material.LIME_DYE;
                statusTag = "&a[✔ AKTIF]";
                lore = List.of(
                        "&7Periode bulan saat ini sedang aktif dalam Season.",
                        "&7Sisa Waktu: &e" + timeLeft,
                        " ",
                        "&e&lKLIK UNTUK MEMBUKA DAFTAR QUEST >"
                );
            } else if (status == QuestStatus.EXPIRED) {
                mat = Material.GRAY_DYE;
                statusTag = "&c[🔒 EXPIRED]";
                lore = List.of(
                        "&7Periode bulan ini telah berlalu.",
                        "&cQuest pada bulan ini tidak dapat lagi dikerjakan."
                );
            } else {
                mat = Material.RED_DYE;
                statusTag = "&7[🔒 UPCOMING]";
                lore = List.of(
                        "&7Periode bulan ini belum dimulai.",
                        "&7Tunggu hingga bulan ini aktif dalam Season."
                );
            }

            setButton(slots[m - 1], new GuiButton(new ItemBuilder(mat)
                    .name("&d&l" + monthNames[m - 1] + " " + statusTag)
                    .lore(lore)
                    .build(), event -> {
                if (status == QuestStatus.ACTIVE) {
                    new MonthlyQuestMenu(plugin, player, this, monthNum).open();
                } else if (status == QuestStatus.EXPIRED) {
                    player.sendMessage(plugin.getMessage("quest-expired"));
                } else {
                    player.sendMessage(plugin.getMessage("quest-upcoming"));
                }
            }));
        }

        setButton(31, new BackButton(this));
    }
}
