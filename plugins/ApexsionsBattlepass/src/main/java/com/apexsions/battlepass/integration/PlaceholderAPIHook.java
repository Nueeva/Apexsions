package com.apexsions.battlepass.integration;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.player.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final ApexsionsBattlepass plugin;

    public PlaceholderAPIHook(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "apexsionsbattlepass";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ApexTeam";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        switch (params.toLowerCase()) {
            case "level" -> {
                return data != null ? String.valueOf(data.getLevel()) : "1";
            }
            case "xp" -> {
                return data != null ? String.valueOf(data.getXp()) : "0";
            }
            case "required_xp" -> {
                int lvl = data != null ? data.getLevel() : 1;
                return String.valueOf(plugin.getRewardManager().getRequiredXp(lvl));
            }
            case "currency" -> {
                return data != null ? String.valueOf(data.getCurrency()) : "0";
            }
            case "pass" -> {
                return data != null ? String.join(", ", data.getPasses()).toUpperCase() : "FREE";
            }
            case "season" -> {
                return plugin.getSeasonManager().getCurrentSeason().getName();
            }
            case "season_time_left" -> {
                return plugin.getSeasonManager().getTimeLeftFormatted();
            }
        }

        return null;
    }
}
