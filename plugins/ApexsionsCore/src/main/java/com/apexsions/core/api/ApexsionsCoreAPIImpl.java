package com.apexsions.core.api;

import com.apexsions.core.ApexsionsCorePlugin;
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
 * Implementation of the public ApexsionsCoreAPI interface.
 */
public class ApexsionsCoreAPIImpl implements ApexsionsCoreAPI {

    private final ApexsionsCorePlugin plugin;

    public ApexsionsCoreAPIImpl(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable PlayerData getPlayerData(@NotNull UUID uuid) {
        return plugin.getPlayerDataService().getCached(uuid).orElse(null);
    }

    @Override
    public @NotNull CompletableFuture<Optional<PlayerData>> getPlayerDataAsync(@NotNull UUID uuid) {
        Optional<PlayerData> cached = plugin.getPlayerDataService().getCached(uuid);
        if (cached.isPresent()) {
            return CompletableFuture.completedFuture(cached);
        }
        return plugin.getPlayerRepository().findByUuid(uuid);
    }

    @Override
    public @Nullable Region getRegion(@NotNull UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        if (data != null && data.getRegionId() != null) {
            return plugin.getRegionManager().getRegion(data.getRegionId()).orElse(null);
        }
        return null;
    }

    @Override
    public @NotNull String getPlayerRegionKey(@NotNull UUID uuid) {
        Region reg = getRegion(uuid);
        return reg != null ? reg.getKey() : "NONE";
    }

    @Override
    public @NotNull String getLevelTitle(@NotNull UUID uuid) {
        return plugin.getLevelManager().getLevelTitle(uuid);
    }

    @Override
    public int getLevel(@NotNull UUID uuid) {
        return plugin.getLevelManager().getLevel(uuid);
    }

    @Override
    public long getXp(@NotNull UUID uuid) {
        return plugin.getLevelManager().getXp(uuid);
    }

    @Override
    public void addXp(@NotNull UUID uuid, long amount, @NotNull XpSource source) {
        plugin.getLevelManager().addXp(uuid, amount, source);
    }

    @Override
    public void setLevel(@NotNull UUID uuid, int level) {
        plugin.getLevelManager().setLevel(uuid, level);
    }

    @Override
    public void setXp(@NotNull UUID uuid, long xp) {
        plugin.getLevelManager().setXp(uuid, xp);
    }

    @Override
    public void setRegion(@NotNull UUID uuid, @NotNull Region region) {
        plugin.getPlayerDataService().updateRegion(uuid, region.getId());
    }

    @Override
    public @NotNull Optional<Region> getKingdomAt(@NotNull org.bukkit.Location location) {
        return plugin.getRegionManager().getRegionAt(location);
    }

    @Override
    public boolean isInKingdomTerritory(@NotNull org.bukkit.entity.Player player, @NotNull Region region) {
        return region.containsLocation(player.getLocation());
    }

    @Override
    public @NotNull LevelManager getLevelManager() {
        return plugin.getLevelManager();
    }

    @Override
    public @NotNull RegionManager getRegionManager() {
        return plugin.getRegionManager();
    }

    @Override
    public @NotNull PlayerDataService getPlayerDataService() {
        return plugin.getPlayerDataService();
    }
}
