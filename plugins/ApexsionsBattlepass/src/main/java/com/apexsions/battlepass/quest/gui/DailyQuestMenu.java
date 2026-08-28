package com.apexsions.battlepass.quest.gui;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.player.PlayerData;
import com.apexsions.battlepass.quest.model.Quest;
import com.apexsions.battlepass.quest.model.QuestStatus;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DailyQuestMenu extends Gui {

    public DailyQuestMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        super(plugin, player, plugin.getGuiConfig().getString("titles.daily-quest", "&8[ &a&lDAILY QUESTS &8]"), 36, parent);
    }

    @Override
    public void initialize() {
        fillBackground();

        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data == null) return;

        String resetTime = plugin.getQuestManager().getPeriodService().getDailyResetTimeLeft();

        // Reset info banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.CLOCK)
                .name("&e&lWAKTU RESET HARIAN")
                .lore(List.of(
                        "&7Reset otomatis setiap jam 00:00 (" + plugin.getConfig().getString("timezone", "Asia/Jakarta") + ")",
                        "&7Sisa waktu: &aResets in: " + resetTime
                ))
                .build()));

        Map<String, Quest> dailyQuests = plugin.getQuestManager().getActiveDailyQuests();
        int[] slots = { 11, 12, 13, 14, 15, 20, 21, 22, 23, 24 };
        int idx = 0;

        for (Quest quest : dailyQuests.values()) {
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
        } else {
            icon = Material.BARRIER;
            statusStr = "&c🔒 DIKUNCI";
        }
        lore.add("Status: " + statusStr);

        ItemStack item = new ItemBuilder(icon)
                .name("&e&l" + quest.getName())
                .lore(lore)
                .build();
        return new GuiButton(item, null);
    }
}
