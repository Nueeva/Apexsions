package com.apexsions.battlepass.admin.gui;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.quest.model.Quest;
import com.apexsions.battlepass.quest.service.QuestPeriodService;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminQuestMenu extends Gui {

    public AdminQuestMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        super(plugin, player, plugin.getGuiConfig().getString("titles.admin-quest", "&8[ &4&lABP QUEST MANAGEMENT &8]"), 45, parent);
    }

    @Override
    public void initialize() {
        fillBackground();

        int currentWeek = plugin.getQuestManager().getPeriodService().getCurrentWeekNumber();
        int currentMonth = plugin.getQuestManager().getPeriodService().getCurrentMonthNumber();
        boolean hasSpecialWeek = plugin.getQuestManager().getPeriodService().monthHasSpecialWeek();

        // 1. Daily Quests Summary (Slot 11)
        Map<String, Quest> daily = plugin.getQuestManager().getDailyQuests();
        List<String> dailyLore = new ArrayList<>();
        dailyLore.add("&7Total Quests Terdaftar: &e" + daily.size());
        dailyLore.add("&7Waktu Reset: &a" + plugin.getQuestManager().getPeriodService().getDailyResetTimeLeft());
        dailyLore.add(" ");
        dailyLore.add("&6Daftar Quests:");
        for (Quest q : daily.values()) {
            dailyLore.add(" &8- &f" + q.getName() + " &8(&e+" + q.getRewardXp() + " XP&8, &6+" + q.getRewardCoins() + " C&8)");
        }

        setButton(11, new GuiButton(new ItemBuilder(Material.SUNFLOWER)
                .name("&e&lDAILY QUESTS (" + daily.size() + ")")
                .lore(dailyLore)
                .build()));

        // 2. Weekly Quests Summary (Week 1-4) (Slot 13)
        Map<Integer, Map<String, Quest>> weekly = plugin.getQuestManager().getWeeklyQuests();
        List<String> weeklyLore = new ArrayList<>();
        weeklyLore.add("&7Week Berjalan: &fWeek " + (currentWeek == QuestPeriodService.SPECIAL_WEEK_INDEX ? "5 (Special)" : currentWeek));
        weeklyLore.add("&7Sisa Waktu Weekly: &a" + plugin.getQuestManager().getPeriodService().getWeeklyTimeLeft());
        weeklyLore.add(" ");
        for (int w = 1; w <= 4; w++) {
            Map<String, Quest> wQuests = weekly.getOrDefault(w, Map.of());
            weeklyLore.add("&eWeek " + w + ": &f" + wQuests.size() + " Quests " + (w == currentWeek ? "&a[AKTIF]" : "&7[TERKUNCI/EXPIRED]"));
        }

        setButton(13, new GuiButton(new ItemBuilder(Material.BOOK)
                .name("&6&lWEEKLY QUESTS (WEEKS 1-4)")
                .lore(weeklyLore)
                .build()));

        // 3. Special Week (Week 5) Summary (Slot 15)
        Map<String, Quest> week5Quests = weekly.getOrDefault(QuestPeriodService.SPECIAL_WEEK_INDEX, Map.of());
        List<String> spLore = new ArrayList<>();
        spLore.add("&7Bulan Memiliki Week 5: " + (hasSpecialWeek ? "&aYa" : "&cTidak"));
        spLore.add("&7Durasi Hari Sisa: &e" + plugin.getQuestManager().getPeriodService().getSpecialWeekDayCount() + " Hari");
        spLore.add("&7Status Saat Ini: " + (currentWeek == QuestPeriodService.SPECIAL_WEEK_INDEX ? "&a&lSEDANG BERJALAN" : "&7Belum Aktif"));
        spLore.add("&7Total Quests Week 5: &f" + week5Quests.size());
        spLore.add(" ");
        spLore.add("&6Daftar Special Quests:");
        for (Quest q : week5Quests.values()) {
            spLore.add(" &8- &d" + q.getName() + " &8(&e+" + q.getRewardXp() + " XP&8)");
        }

        setButton(15, new GuiButton(new ItemBuilder(Material.NETHER_STAR)
                .name("&d&lSPECIAL WEEK (WEEK 5)")
                .lore(spLore)
                .build()));

        // 4. Monthly Quests Summary (Slot 22)
        Map<Integer, Map<String, Quest>> monthly = plugin.getQuestManager().getMonthlyQuests();
        List<String> monthlyLore = new ArrayList<>();
        monthlyLore.add("&7Month Berjalan: &fMonth " + currentMonth + " &8(dari 3 Bulan)");
        monthlyLore.add("&7Sisa Waktu Monthly: &a" + plugin.getQuestManager().getPeriodService().getMonthlyTimeLeft());
        monthlyLore.add(" ");
        for (int m = 1; m <= 3; m++) {
            Map<String, Quest> mQuests = monthly.getOrDefault(m, Map.of());
            monthlyLore.add("&dMonth " + m + ": &f" + mQuests.size() + " Quests " + (m == currentMonth ? "&a[AKTIF]" : "&7[TERKUNCI]"));
        }

        setButton(22, new GuiButton(new ItemBuilder(Material.DIAMOND)
                .name("&b&lMONTHLY QUESTS (MONTHS 1-3)")
                .lore(monthlyLore)
                .build()));

        // 0. Edit Quests Mode (Slot 8)
        setButton(8, new GuiButton(new ItemBuilder(Material.ANVIL)
                .name("&a&l[✏] MODE EDIT QUESTS")
                .lore(List.of(
                        "&7Tambah quest baru, ubah tipe objektif,",
                        "&7target entity/block/item, & rewards.",
                        " ",
                        "&aKlik untuk membuka editor quests >"
                ))
                .build(), event -> {
            new com.apexsions.battlepass.admin.gui.quest.AdminQuestCategoryMenu(plugin, player, this).open();
        }));

        // Navigation
        setButton(36, new BackButton(this, parent));
        setButton(44, new CloseButton());
    }
}
