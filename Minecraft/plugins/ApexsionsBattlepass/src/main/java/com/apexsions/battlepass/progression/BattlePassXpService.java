package com.apexsions.battlepass.progression;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.api.event.BattlePassLevelUpEvent;
import com.apexsions.battlepass.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
        plugin.getRepository().savePlayerData(data);
        syncPlayerIfCorePresent(player);
    }

    public void checkLevelUp(Player player, PlayerData data) {
        if (data == null) return;
        int maxLevel = plugin.getRewardManager().getMaxLevel();
        int currentLevel = data.getLevel();

        if (currentLevel < maxLevel) {
            int reqXp = plugin.getRewardManager().getRequiredXp(currentLevel);
            while (data.getXp() >= reqXp && currentLevel < maxLevel) {
                data.setXp(data.getXp() - reqXp);
                currentLevel++;
                data.setLevel(currentLevel);

                // Trigger Level Up Event
                if (player != null && player.isOnline()) {
                    BattlePassLevelUpEvent event = new BattlePassLevelUpEvent(player, currentLevel);
                    Bukkit.getPluginManager().callEvent(event);

                    player.sendMessage(plugin.getMessage("level-up").replace("%level%", String.valueOf(currentLevel)));
                }
                reqXp = plugin.getRewardManager().getRequiredXp(currentLevel);
            }
        }

        // If player is at max level, convert excess XP to Battle Coins (10 XP per coin)
        if (data.getLevel() >= maxLevel) {
            checkAndConvertMaxLevelOverflow(player, data);
        }
    }

    public void checkAndConvertMaxLevelOverflow(Player player, PlayerData data) {
        if (data == null) return;
        int maxLevel = plugin.getRewardManager().getMaxLevel();
        if (data.getLevel() < maxLevel) return;

        boolean overflowEnabled = plugin.getConfig().getBoolean("battlepass.max-level-overflow.enabled", true);
        if (!overflowEnabled) return;

        int xpPerCoin = plugin.getConfig().getInt("battlepass.max-level-overflow.xp-per-coin", 10);
        if (xpPerCoin <= 0) xpPerCoin = 10;

        int currentXp = data.getXp();
        if (currentXp >= xpPerCoin) {
            int coinsToAdd = currentXp / xpPerCoin;
            int remainingXp = currentXp % xpPerCoin;

            data.setXp(remainingXp);
            plugin.getCurrencyService().addCurrency(data.getUuid(), coinsToAdd);

            if (player != null && player.isOnline()) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.4f);
                String currencyName = plugin.getCurrencyService().getCurrencyName();
                String msg = plugin.getMessage("max-level-overflow-converted");
                if (msg != null && !msg.isBlank()) {
                    player.sendMessage(msg.replace("%coins%", String.valueOf(coinsToAdd))
                            .replace("%currency%", currencyName)
                            .replace("%xp%", String.valueOf(coinsToAdd * xpPerCoin)));
                }
            }
        }
    }

    public int getRequiredXp(int level) {
        return plugin.getRewardManager().getRequiredXp(level);
    }

    public static void syncPlayerIfCorePresent(Player player) {
        if (player == null) return;
        try {
            org.bukkit.plugin.Plugin core = org.bukkit.Bukkit.getPluginManager().getPlugin("ApexsionsCore");
            if (core != null && core.isEnabled()) {
                Object bridge = core.getClass().getMethod("getWebBridgeService").invoke(core);
                if (bridge != null) {
                    bridge.getClass().getMethod("syncPlayerAsync", Player.class).invoke(bridge, player);
                }
            }
        } catch (Throwable ignored) {}
    }
}
