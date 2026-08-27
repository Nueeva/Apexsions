package com.apex.battlepass.admin.gui;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.navigation.CloseButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.quest.service.QuestPeriodService;
import com.apex.battlepass.season.Season;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminSeasonMenu extends Gui {

    public AdminSeasonMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        super(plugin, player, plugin.getGuiConfig().getString("titles.admin-season", "&8[ &4&lABP BATTLEPASS MANAGEMENT &8]"), 45, parent);
    }

    @Override
    public void initialize() {
        fillBackground();

        Season season = plugin.getSeasonManager().getCurrentSeason();
        int currentWeek = plugin.getQuestManager().getPeriodService().getCurrentWeekNumber();
        int currentMonth = plugin.getQuestManager().getPeriodService().getCurrentMonthNumber();
        boolean hasSpecialWeek = plugin.getQuestManager().getPeriodService().monthHasSpecialWeek();
        int specialWeekDays = plugin.getQuestManager().getPeriodService().getSpecialWeekDayCount();
        String weekLabel = currentWeek == QuestPeriodService.SPECIAL_WEEK_INDEX ? "Special Week" : "Week " + currentWeek;

        // 1. Season Details Card (Slot 11)
        setButton(11, new GuiButton(new ItemBuilder(Material.NETHER_STAR)
                .name("&6&lINFORMASI SEASON AKTIF")
                .lore(List.of(
                        "&7ID Season: &e#" + season.getId(),
                        "&7Nama: &f" + season.getName(),
                        "&7Status: &a" + plugin.getSeasonManager().getSeasonState(),
                        "&7Periode: &f" + plugin.getSeasonManager().getSeasonDateRangeFormatted(),
                        "&7Sisa Waktu: &e" + plugin.getSeasonManager().getTimeLeftFormatted(),
                        "&7Zona Waktu: &f" + plugin.getSeasonManager().getZoneId().getId()
                ))
                .build()));

        // 2. Periodic Progression Card (Slot 13)
        setButton(13, new GuiButton(new ItemBuilder(Material.CLOCK)
                .name("&b&lPROGRESI PERIODE & WEEKS")
                .lore(List.of(
                        "&7Month Berjalan: &fMonth " + currentMonth + " &8(dari 3 Bulan)",
                        "&7Week Berjalan: &f" + weekLabel,
                        "&7Special Week Tersedia Bulan Ini: " + (hasSpecialWeek ? "&aYa (" + specialWeekDays + " Hari)" : "&cTidak (28 Hari)"),
                        "&7Sisa Waktu Daily: &e" + plugin.getQuestManager().getPeriodService().getDailyResetTimeLeft(),
                        "&7Sisa Waktu Weekly: &e" + plugin.getQuestManager().getPeriodService().getWeeklyTimeLeft(),
                        "&7Sisa Waktu Monthly: &e" + plugin.getQuestManager().getPeriodService().getMonthlyTimeLeft()
                ))
                .build()));

        // 3. Level & XP Configuration Card (Slot 15)
        int maxLvl = plugin.getRewardManager().getMaxLevel();
        int defaultXp = plugin.getConfig().getInt("battlepass.default-required-xp", 1000);
        String xpMode = plugin.getConfig().getString("battlepass.xp-mode", "CUSTOM");

        setButton(15, new GuiButton(new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name("&a&lKONFIGURASI LEVEL & XP")
                .lore(List.of(
                        "&7Max Level: &e" + maxLvl,
                        "&7XP Mode: &f" + xpMode,
                        "&7Default Req XP: &a" + defaultXp + " XP",
                        "&7Tiers Pass Terdaftar: &b" + plugin.getPassManager().getPasses().size()
                ))
                .build()));

        // 4. Pass Tiers Overview Card (Slot 22)
        setButton(22, new GuiButton(new ItemBuilder(Material.GOLDEN_HELMET)
                .name("&e&lDAFTAR PASS TIERS")
                .lore(List.of(
                        "&7Tiers Aktif: &f" + String.join(", ", plugin.getPassManager().getPasses().keySet()).toUpperCase(),
                        " ",
                        "&7Gunakan &e/abp givepass <player> <tier>",
                        "&7untuk memberikan pass langsung ke pemain."
                ))
                .build()));

        // Navigation
        setButton(36, new BackButton(this, parent));
        setButton(44, new CloseButton());
    }
}
