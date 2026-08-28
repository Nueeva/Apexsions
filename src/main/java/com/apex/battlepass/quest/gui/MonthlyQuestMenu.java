package com.apex.battlepass.quest.gui;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.player.PlayerData;
import com.apex.battlepass.quest.model.Quest;
import com.apex.battlepass.quest.model.QuestStatus;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MonthlyQuestMenu extends Gui {

    private final int monthNumber;

    public MonthlyQuestMenu(ApexsionsBattlepass plugin, Player player, Gui parent, int monthNumber) {
        super(plugin, player, "&8[ &d&lMONTHLY QUESTS &8- Month " + monthNumber + " ]", 36, parent);
        this.monthNumber = monthNumber;
    }

    @Override
    public void initialize() {
        fillBackground();

        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data == null) return;

        String timeLeft = plugin.getQuestManager().getPeriodService().getMonthlyTimeLeft();

        // Info Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.CLOCK)
                .name("&d&lMONTH " + monthNumber + " - WAKTU TERSISA")
                .lore(List.of(
                        "&7Waktu tersisa: &a" + timeLeft,
                        "&7Selesaikan quest bulanan sebelum bulan berganti!"
                ))
                .build()));

        Map<String, Quest> mQuests = plugin.getQuestManager().getMonthlyQuests().getOrDefault(monthNumber, Map.of());
        int[] slots = { 11, 12, 13, 14, 15, 20, 21, 22, 23, 24 };
        int idx = 0;

        for (Quest quest : mQuests.values()) {
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
                .name("&d&l" + quest.getName())
                .lore(lore)
                .build();
        return new GuiButton(item, null);
    }
}
