package com.yourserver.apexsionscore.integration;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import com.yourserver.apexsionscore.player.PlayerData;
import com.yourserver.apexsionscore.region.Region;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * PlaceholderAPI expansion providing KingdomCore placeholders to external plugins (e.g. TAB, Scoreboards, Holograms).
 */
public class PlaceholderApiHook extends PlaceholderExpansion {

    private final ApexsionsCorePlugin plugin;

    public PlaceholderApiHook(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "apexsionscore";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Antigravity";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) {
            return "";
        }

        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(offlinePlayer.getUniqueId());
        if (dataOpt.isEmpty()) {
            return "";
        }

        PlayerData data = dataOpt.get();

        switch (params.toLowerCase()) {
            case "level":
                return String.valueOf(data.getLevel());

            case "xp":
                return String.valueOf(data.getXp());

            case "xp_needed":
                long needed = plugin.getLevelManager().getRequiredXpForNextLevel(data.getLevel());
                return needed == Long.MAX_VALUE ? "MAX" : String.valueOf(needed);

            case "region":
            case "kingdom":
                if (data.getRegionId() != null) {
                    Optional<Region> regionOpt = plugin.getRegionManager().getRegion(data.getRegionId());
                    if (regionOpt.isPresent()) {
                        return regionOpt.get().getKey();
                    }
                }
                return "NONE";

            case "region_name":
            case "kingdom_name":
                if (data.getRegionId() != null) {
                    Optional<Region> regionOpt = plugin.getRegionManager().getRegion(data.getRegionId());
                    if (regionOpt.isPresent()) {
                        return regionOpt.get().getDisplayName();
                    }
                }
                return plugin.getConfigManager().getDefaultRegion();

            case "level_title":
            case "title":
                return plugin.getLevelManager().getLevelTitle(data.getUuid());

            case "rank":
                if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
                    return plugin.getLuckPermsHook().getPlayerRank(offlinePlayer.getPlayer());
                }
                return "Wanderer";

            case "rank_name":
                if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
                    return plugin.getLuckPermsHook().getPlayerRankDisplayName(offlinePlayer.getPlayer());
                }
                return "Wanderer";

            case "rank_color":
                if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
                    return plugin.getLuckPermsHook().getPlayerRankColor(offlinePlayer.getPlayer());
                }
                return "#808080";

            case "rank_prefix":
            case "prefix":
                if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
                    return plugin.getLuckPermsHook().getPlayerPrefix(offlinePlayer.getPlayer());
                }
                return "";

            case "current_territory":
                if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
                    Optional<Region> reg = plugin.getRegionManager().getRegionAt(offlinePlayer.getPlayer().getLocation());
                    return reg.map(Region::getKey).orElse("WILDERNESS");
                }
                return "WILDERNESS";

            case "current_territory_name":
                if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
                    Optional<Region> reg = plugin.getRegionManager().getRegionAt(offlinePlayer.getPlayer().getLocation());
                    return reg.map(Region::getDisplayName).orElse("Wilderness");
                }
                return "Wilderness";

            default:
                return null;
        }
    }
}
