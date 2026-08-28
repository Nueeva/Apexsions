package com.apexsions.battlepass.quest.gui;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.player.PlayerData;
import com.apexsions.battlepass.quest.model.Quest;
import com.apexsions.battlepass.quest.model.QuestStatus;
import com.apexsions.battlepass.quest.service.QuestPeriodService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WeeklyQuestMenu extends Gui {

    private final int weekNumber;

    public WeeklyQuestMenu(ApexsionsBattlepass plugin, Player player, Gui parent, int weekNumber) {
        super(plugin, player, weekNumber == QuestPeriodService.SPECIAL_WEEK_INDEX ? "&8[ &6&lSPECIAL WEEK QUESTS &8]" : "&8[ &e&lWEEKLY QUESTS &8- Week " + weekNumber + " ]", 36, parent);
        this.weekNumber = weekNumber;
    }

    @Override
    public void initialize() {
        fillBackground();

        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data == null) return;

        String timeLeft = plugin.getQuestManager().getPeriodService().getWeeklyTimeLeft();
        String weekTitle = weekNumber == QuestPeriodService.SPECIAL_WEEK_INDEX ? "Special Week" : "Week " + weekNumber;

        // Info Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.CLOCK)
                .name("&e&l" + weekTitle + " - WAKTU TERSISA")
                .lore(List.of(
                        "&7Waktu tersisa: &a" + timeLeft,
                        "&7Selesaikan quest sebelum periode berakhir!"
                ))
                .build()));

        Map<String, Quest> wQuests = plugin.getQuestManager().getWeeklyQuests().getOrDefault(weekNumber, Map.of());
        int[] slots = { 11, 12, 13, 14, 15, 20, 21, 22, 23, 24 };
        int idx = 0;

        for (Quest quest : wQuests.values()) {
            if (idx >= slots.length) break;
            setButton(slots[idx++], createQuestButton(data, quest));
        }

        setButton(31, new BackButton(this));
    }


    private GuiButton createQuestButton(PlayerData data, Quest quest) {
        QuestStatus status = plugin.getQuestManager().getQuestStatus(player, quest);
        int progress = data.getQuestProgress(quest.getId());
        int target = quest.getTargetAmount();

        List<String> lore = new ArrayList<>();
        lore.add("&7" + quest.getDescription());
        lore.add(" ");
        lore.add("&7Progress: &e" + Math.min(progress, target) + " &8/ &f" + target);
        lore.add("&7Reward XP: &a+" + quest.getRewardXp() + " XP");
        if (quest.getRewardCoins() > 0) {
            lore.add("&7Reward Coins: &e+" + quest.getRewardCoins() + " Coins");
        }
        lore.add(" ");

        Material icon;
        String statusStr;
        if (status == QuestStatus.COMPLETED) {
            icon = Material.WRITTEN_BOOK;
            statusStr = "&a✔ SELESAI";
        } else if (status == QuestStatus.ACTIVE) {
            icon = Material.BOOK;
            statusStr = "&e&lDALAM PROSES";
        } else if (status == QuestStatus.EXPIRED) {
            icon = Material.BARRIER;
            statusStr = "&c🔒 EXPIRED";
        } else {
            icon = Material.TRIPWIRE_HOOK;
            statusStr = "&7🔒 UPCOMING";
        }
        lore.add("Status: " + statusStr);

        ItemStack item = new ItemBuilder(icon)
                .name("&e&l" + quest.getName())
                .lore(lore)
                .build();
        return new GuiButton(item, null);
    }
}
