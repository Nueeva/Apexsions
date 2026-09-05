package com.apexsions.battlepass.api;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Public service interface for ApexsionsBattlepass.
 */
public interface ApexsionsBattlepassAPI {

    int getCurrentSeasonId();

    int getPlayerTier(@NotNull UUID uuid);

    int getPlayerXp(@NotNull UUID uuid);

    void addPlayerXp(@NotNull UUID uuid, int xp);

    boolean hasPremiumPass(@NotNull UUID uuid);

    boolean hasPass(@NotNull UUID uuid, @NotNull String passId);

    int getPlayerPoints(@NotNull UUID uuid);

    void addPlayerPoints(@NotNull UUID uuid, int points);

    boolean removePlayerPoints(@NotNull UUID uuid, int points);
}
