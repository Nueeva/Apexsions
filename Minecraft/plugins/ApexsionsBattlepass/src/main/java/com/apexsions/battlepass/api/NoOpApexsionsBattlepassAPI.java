package com.apexsions.battlepass.api;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Graceful No-Op Fallback implementation of ApexsionsBattlepassAPI.
 */
public class NoOpApexsionsBattlepassAPI implements ApexsionsBattlepassAPI {

    public static final NoOpApexsionsBattlepassAPI INSTANCE = new NoOpApexsionsBattlepassAPI();

    private NoOpApexsionsBattlepassAPI() {}

    @Override
    public int getCurrentSeasonId() {
        return 1;
    }

    @Override
    public int getPlayerTier(@NotNull UUID uuid) {
        return 0;
    }

    @Override
    public int getPlayerXp(@NotNull UUID uuid) {
        return 0;
    }

    @Override
    public void addPlayerXp(@NotNull UUID uuid, int xp) {
        // No-Op
    }

    @Override
    public boolean hasPremiumPass(@NotNull UUID uuid) {
        return false;
    }

    @Override
    public boolean hasPass(@NotNull UUID uuid, @NotNull String passId) {
        return false;
    }

    @Override
    public int getPlayerPoints(@NotNull UUID uuid) {
        return 0;
    }

    @Override
    public void addPlayerPoints(@NotNull UUID uuid, int points) {
        // No-Op
    }

    @Override
    public boolean removePlayerPoints(@NotNull UUID uuid, int points) {
        return false;
    }
}
