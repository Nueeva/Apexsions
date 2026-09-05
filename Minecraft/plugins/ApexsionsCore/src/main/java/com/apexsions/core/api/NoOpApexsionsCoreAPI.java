package com.apexsions.core.api;

import com.apexsions.core.admin.AdminHubManager;
import com.apexsions.core.admin.AdminModule;
import com.apexsions.core.level.LevelManager;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.player.PlayerDataService;
import com.apexsions.core.region.Region;
import com.apexsions.core.region.RegionManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Graceful No-Op Fallback implementation of ApexsionsCoreAPI.
 * Returned when ApexsionsCore is disabled or not yet loaded, preventing ClassNotFoundException and crashes.
 */
public class NoOpApexsionsCoreAPI implements ApexsionsCoreAPI {

    public static final NoOpApexsionsCoreAPI INSTANCE = new NoOpApexsionsCoreAPI();

    private NoOpApexsionsCoreAPI() {}

    @Override
    public @Nullable PlayerData getPlayerData(@NotNull UUID uuid) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Optional<PlayerData>> getPlayerDataAsync(@NotNull UUID uuid) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public @Nullable Region getRegion(@NotNull UUID uuid) {
        return null;
    }

    @Override
    public @NotNull String getPlayerRegionKey(@NotNull UUID uuid) {
        return "NONE";
    }

    @Override
    public @NotNull String getLevelTitle(@NotNull UUID uuid) {
        return "Wanderer";
    }

    @Override
    public int getLevel(@NotNull UUID uuid) {
        return 1;
    }

    @Override
    public long getXp(@NotNull UUID uuid) {
        return 0L;
    }

    @Override
    public void addXp(@NotNull UUID uuid, long amount, @NotNull XpSource source) {
        // No-Op
    }

    @Override
    public void setLevel(@NotNull UUID uuid, int level) {
        // No-Op
    }

    @Override
    public void setXp(@NotNull UUID uuid, long xp) {
        // No-Op
    }

    @Override
    public void setRegion(@NotNull UUID uuid, @NotNull Region region) {
        // No-Op
    }

    @Override
    public @NotNull Optional<Region> getKingdomAt(@NotNull Location location) {
        return Optional.empty();
    }

    @Override
    public boolean isInKingdomTerritory(@NotNull Player player, @NotNull Region region) {
        return false;
    }

    @Override
    public @NotNull LevelManager getLevelManager() {
        throw new UnsupportedOperationException("ApexsionsCore is currently unavailable.");
    }

    @Override
    public @NotNull RegionManager getRegionManager() {
        throw new UnsupportedOperationException("ApexsionsCore is currently unavailable.");
    }

    @Override
    public @NotNull PlayerDataService getPlayerDataService() {
        throw new UnsupportedOperationException("ApexsionsCore is currently unavailable.");
    }

    @Override
    public @NotNull AdminHubManager getAdminHubManager() {
        throw new UnsupportedOperationException("ApexsionsCore is currently unavailable.");
    }

    @Override
    public void registerAdminModule(@NotNull AdminModule module) {
        // No-Op
    }

    @Override
    public @NotNull PlayerChatProfile getPlayerChatProfile(@NotNull UUID uuid) {
        return new PlayerChatProfile(
                uuid,
                "Unknown",
                1,
                0L,
                1000L,
                "Wanderer",
                null,
                "Wanderer",
                "NONE",
                "Belum Memilih",
                false,
                0.0,
                0,
                20,
                20
        );
    }

    @Override
    public double getKingdomTax(@NotNull String kingdomKey) {
        return 10.0;
    }
}
