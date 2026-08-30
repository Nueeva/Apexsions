package com.apexsions.battlepass.api;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ApexsionsBattlepassAPIImpl implements ApexsionsBattlepassAPI {

    private final ApexsionsBattlepass plugin;

    public ApexsionsBattlepassAPIImpl(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
    }

    @Override
    public int getCurrentSeasonId() {
        return plugin.getSeasonManager().getCurrentSeason().getId();
    }

    @Override
    public int getPlayerTier(@NotNull UUID uuid) {
        PlayerData p = plugin.getPlayerManager().getPlayerData(uuid);
        return p != null ? p.getLevel() : 1;
    }

    @Override
    public int getPlayerXp(@NotNull UUID uuid) {
        PlayerData p = plugin.getPlayerManager().getPlayerData(uuid);
        return p != null ? p.getXp() : 0;
    }

    @Override
    public void addPlayerXp(@NotNull UUID uuid, int xp) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            plugin.getXpService().addXp(player, xp);
        } else {
            PlayerData p = plugin.getPlayerManager().getPlayerData(uuid);
            if (p != null) {
                p.addXp(xp);
            }
        }
    }

    @Override
    public boolean hasPremiumPass(@NotNull UUID uuid) {
        PlayerData p = plugin.getPlayerManager().getPlayerData(uuid);
        return p != null && p.hasPass("premium");
    }

    @Override
    public boolean hasPass(@NotNull UUID uuid, @NotNull String passId) {
        PlayerData p = plugin.getPlayerManager().getPlayerData(uuid);
        return p != null && p.hasPass(passId);
    }

    @Override
    public int getPlayerPoints(@NotNull UUID uuid) {
        return (int) plugin.getCurrencyService().getBalance(uuid);
    }

    @Override
    public void addPlayerPoints(@NotNull UUID uuid, int points) {
        plugin.getCurrencyService().addCurrency(uuid, points);
    }

    @Override
    public boolean removePlayerPoints(@NotNull UUID uuid, int points) {
        return plugin.getCurrencyService().removeCurrency(uuid, points);
    }
}
