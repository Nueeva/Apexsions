package com.apexsions.core.api;

import com.apexsions.core.level.LevelManager;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.player.PlayerDataService;
import com.apexsions.core.region.Region;
import com.apexsions.core.region.RegionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Public internal API interface for KingdomCore.
 * External plugins (mini-games, quests, GUI plugins) must interact through this interface.
 */
public interface ApexsionsCoreAPI {

    /**
     * Retrieves cached PlayerData for an online or recently active player.
     */
    @Nullable
    PlayerData getPlayerData(@NotNull UUID uuid);

    /**
     * Loads PlayerData asynchronously from database if not already cached.
     */
    @NotNull
    CompletableFuture<Optional<PlayerData>> getPlayerDataAsync(@NotNull UUID uuid);

    /**
     * Gets the assigned Region for a player.
     */
    @Nullable
    Region getRegion(@NotNull UUID uuid);

    /**
     * Gets the assigned Region key for a player, or 'NONE' if not pledged.
     */
    @NotNull
    String getPlayerRegionKey(@NotNull UUID uuid);

    /**
     * Resolves the level title for a player based on their region and current level.
     */
    @NotNull
    String getLevelTitle(@NotNull UUID uuid);

    /**
     * Gets the current level of a player.
     */
    int getLevel(@NotNull UUID uuid);

    /**
     * Gets the current XP of a player.
     */
    long getXp(@NotNull UUID uuid);

    /**
     * Adds XP to a player from a given XP source.
     */
    void addXp(@NotNull UUID uuid, long amount, @NotNull XpSource source);

    /**
     * Sets the level of a player.
     */
    void setLevel(@NotNull UUID uuid, int level);

    /**
     * Sets the XP of a player.
     */
    void setXp(@NotNull UUID uuid, long xp);

    /**
     * Sets the region for a player.
     */
    void setRegion(@NotNull UUID uuid, @NotNull Region region);

    /**
     * Finds the Kingdom territory containing the given spatial location (via BlueMap polygon bounds).
     */
    @NotNull
    Optional<Region> getKingdomAt(@NotNull org.bukkit.Location location);

    /**
     * Checks if a player is currently standing inside the given kingdom's territory polygon.
     */
    boolean isInKingdomTerritory(@NotNull org.bukkit.entity.Player player, @NotNull Region region);

    /**
     * Gets the LevelManager service.
     */
    @NotNull
    LevelManager getLevelManager();

    /**
     * Gets the RegionManager service.
     */
    @NotNull
    RegionManager getRegionManager();

    /**
     * Gets the PlayerDataService.
     */
    @NotNull
    PlayerDataService getPlayerDataService();

    /**
     * Gets the Central Admin Hub Manager.
     */
    @NotNull
    com.apexsions.core.admin.AdminHubManager getAdminHubManager();

    /**
     * Registers a custom module card into the Central Master Admin Hub (/admingui).
     */
    void registerAdminModule(@NotNull com.apexsions.core.admin.AdminModule module);

    /**
     * Retrieves clean decoupled PlayerChatProfile DTO for chat formatting, hover tooltips, and tablists.
     */
    @NotNull
    PlayerChatProfile getPlayerChatProfile(@NotNull UUID uuid);

    /**
     * Gets the configured tax percentage for a kingdom (e.g. Zenithar 25.0, Solterra 20.0, Sylvamoor 15.0).
     */
    double getKingdomTax(@NotNull String kingdomKey);
}
