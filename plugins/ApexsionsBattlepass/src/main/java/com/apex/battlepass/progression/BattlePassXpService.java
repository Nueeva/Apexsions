package com.apex.battlepass.progression;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.api.event.BattlePassLevelUpEvent;
import com.apex.battlepass.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BattlePassXpService {

    private final ApexsionsBattlepass plugin;

    public BattlePassXpService(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
    }

    public void addXp(Player player, int amount) {
        if (player == null || amount <= 0) return;

        // Check if Season is active or if in transition/ended
        if (!plugin.getSeasonManager().isActive()) {
            player.sendMessage(plugin.getMessage("quest-locked-transition"));
            return;
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data == null) return;

        data.addXp(amount);
        player.sendMessage(plugin.getMessage("xp-gained").replace("%amount%", String.valueOf(amount)));

        checkLevelUp(player, data);
    }

    public void checkLevelUp(Player player, PlayerData data) {
        int maxLevel = plugin.getRewardManager().getMaxLevel();
        int currentLevel = data.getLevel();
        if (currentLevel >= maxLevel) return;

        int reqXp = plugin.getRewardManager().getRequiredXp(currentLevel);
        while (data.getXp() >= reqXp && currentLevel < maxLevel) {
            data.setXp(data.getXp() - reqXp);
            currentLevel++;
            data.setLevel(currentLevel);

            // Trigger Level Up Event
            BattlePassLevelUpEvent event = new BattlePassLevelUpEvent(player, currentLevel);
            Bukkit.getPluginManager().callEvent(event);

            player.sendMessage(plugin.getMessage("level-up").replace("%level%", String.valueOf(currentLevel)));
            reqXp = plugin.getRewardManager().getRequiredXp(currentLevel);
        }
    }

    public int getRequiredXp(int level) {
        return plugin.getRewardManager().getRequiredXp(level);
    }
}
