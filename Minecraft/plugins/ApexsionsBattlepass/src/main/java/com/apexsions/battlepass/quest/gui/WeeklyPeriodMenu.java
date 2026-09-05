package com.apexsions.battlepass.quest.gui;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.quest.model.QuestStatus;
import com.apexsions.battlepass.quest.service.QuestPeriodService;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class WeeklyPeriodMenu extends Gui {

    public WeeklyPeriodMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        super(plugin, player, "&8[ &e&lWEEKLY QUEST PERIODS &8]", 36, parent);
    }

    @Override
    public void initialize() {
        fillBackground();

        int currentWeek = plugin.getQuestManager().getPeriodService().getCurrentWeekNumber();
        String timeLeft = plugin.getQuestManager().getPeriodService().getWeeklyTimeLeft();

        // 1. Info Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.CLOCK)
                .name("&e&lPERIODE MINGGUAN SAAT INI")
                .lore(List.of(
                        "&7Status saat ini: &f" + (currentWeek == QuestPeriodService.SPECIAL_WEEK_INDEX ? "&6Special Week" : "Week " + currentWeek),
                        "&7Waktu tersisa: &a" + timeLeft,
                        "&7Reset otomatis ke Week 1 setiap awal bulan baru!"
                ))
                .build()));

        // 2. Normal Weeks 1 to 4 (Slots 10, 12, 14, 16)
        int[] normalSlots = { 10, 12, 14, 16 };
        for (int w = 1; w <= 4; w++) {
            final int weekNum = w;
            QuestStatus status = plugin.getQuestManager().getPeriodService().getWeeklyPeriodStatus(weekNum);

            Material mat;
            String statusTag;
            List<String> lore;

            if (status == QuestStatus.ACTIVE) {
                mat = Material.LIME_DYE;
                statusTag = "&a[✔ AKTIF]";
                lore = List.of(
                        "&7Periode: &fHari ke-" + ((w - 1) * 7 + 1) + " - " + (w * 7),
                        "&7Sisa Waktu: &e" + timeLeft,
                        " ",
                        "&e&lKLIK UNTUK MEMBUKA DAFTAR QUEST >"
                );
            } else if (status == QuestStatus.EXPIRED) {
                mat = Material.GRAY_DYE;
                statusTag = "&c[🔒 EXPIRED]";
                lore = List.of(
                        "&7Periode minggu ini telah berlalu.",
                        "&cQuest pada minggu ini tidak dapat lagi dikerjakan."
                );
            } else {
                mat = Material.RED_DYE;
                statusTag = "&7[🔒 UPCOMING]";
                lore = List.of(
                        "&7Periode minggu ini belum dimulai.",
                        "&7Tunggu hingga minggu ini aktif."
                );
            }

            setButton(normalSlots[w - 1], new GuiButton(new ItemBuilder(mat)
                    .name("&e&lWEEK " + weekNum + " " + statusTag)
                    .lore(lore)
                    .build(), event -> {
                if (status == QuestStatus.ACTIVE) {
                    new WeeklyQuestMenu(plugin, player, this, weekNum).open();
                } else if (status == QuestStatus.EXPIRED) {
                    player.sendMessage(plugin.getMessage("quest-expired"));
                } else {
                    player.sendMessage(plugin.getMessage("quest-upcoming"));
                }
            }));
        }

        // 3. Special Week Button (Slot 22) if month has extra days
        if (plugin.getQuestManager().getPeriodService().monthHasSpecialWeek()) {
            final int specialWeekNum = QuestPeriodService.SPECIAL_WEEK_INDEX;
            QuestStatus status = plugin.getQuestManager().getPeriodService().getWeeklyPeriodStatus(specialWeekNum);
            int extraDays = plugin.getQuestManager().getPeriodService().getSpecialWeekDayCount();

            Material mat;
            String statusTag;
            List<String> lore;

            if (status == QuestStatus.ACTIVE) {
                mat = Material.NETHER_STAR;
                statusTag = "&6[★ SPECIAL WEEK AKTIF]";
                lore = List.of(
                        "&7Periode ekstra: &f" + extraDays + " hari sebelum akhir bulan!",
                        "&7Sisa Waktu: &e" + timeLeft,
                        " ",
                        "&e&lKLIK UNTUK MEMBUKA SPECIAL QUESTS >"
                );
            } else if (status == QuestStatus.EXPIRED) {
                mat = Material.GRAY_DYE;
                statusTag = "&c[🔒 SPECIAL WEEK BERAKHIR]";
                lore = List.of("&7Periode Special Week bulan ini sudah berlalu.");
            } else {
                mat = Material.BARRIER;
                statusTag = "&7[🔒 SPECIAL WEEK UPCOMING]";
                lore = List.of(
                        "&7Aktif otomatis setelah Week 4 berakhir.",
                        "&7Durasi: &f" + extraDays + " hari menuju awal bulan baru."
                );
            }

            setButton(22, new GuiButton(new ItemBuilder(mat)
                    .name("&6&lSPECIAL WEEK " + statusTag)
                    .lore(lore)
                    .build(), event -> {
                if (status == QuestStatus.ACTIVE) {
                    new WeeklyQuestMenu(plugin, player, this, specialWeekNum).open();
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
