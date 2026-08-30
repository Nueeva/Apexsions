package com.apexsions.core.integration;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Comprehensive PlaceholderAPI expansion providing Apexsions placeholders
 * to external plugins (e.g. TAB v6.1, Scoreboards, DecentHolograms, ajLeaderboards, Skript).
 */
public class PlaceholderApiHook extends PlaceholderExpansion {

    private final ApexsionsCorePlugin plugin;
    private final String identifier;

    public PlaceholderApiHook(ApexsionsCorePlugin plugin) {
        this(plugin, "apexsions");
    }

    public PlaceholderApiHook(ApexsionsCorePlugin plugin, String identifier) {
        this.plugin = plugin;
        this.identifier = identifier;
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
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

            case "req_xp":
            case "xp_needed":
            case "next_xp":
                long needed = plugin.getLevelFormula().getRequiredXpForNextLevel(data.getLevel());
                return needed == Long.MAX_VALUE ? "MAX" : String.valueOf(needed);

            case "xp_progressbar":
            case "progressbar":
                long nextXp = plugin.getLevelFormula().getRequiredXpForNextLevel(data.getLevel());
                int percent = (int) Math.min(100, Math.max(0, (data.getXp() * 100) / Math.max(1, nextXp)));
                int totalBars = 10;
                int filled = (percent * totalBars) / 100;
                StringBuilder sb = new StringBuilder("&e");
                for (int i = 0; i < filled; i++) sb.append("█");
                sb.append("&8");
                for (int i = filled; i < totalBars; i++) sb.append("░");
                return sb.toString();

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
            case "kingdom_formatted":
                if (data.getRegionId() != null) {
                    Optional<Region> regionOpt = plugin.getRegionManager().getRegion(data.getRegionId());
                    if (regionOpt.isPresent()) {
                        return regionOpt.get().getDisplayName();
                    }
                }
                return "Belum Memilih";

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

            case "rank_animated":
            case "animated_rank":
            case "prefix":
                if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null && plugin.getRankAnimationManager() != null) {
                    return plugin.getRankAnimationManager().getAnimatedRankPrefix(offlinePlayer.getPlayer());
                }
                return "<gradient:#dfe6e9:#74b9ff><bold>[Wanderer]</bold></gradient> ";

            case "active_title":
                return data.getActiveTitle() != null ? data.getActiveTitle() : "";

            case "balance_rupiah":
                if (plugin.getVaultHook().hasEconomy() && offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
                    return String.format("%.0f", plugin.getVaultHook().getBalance(offlinePlayer.getPlayer()));
                }
                return "0";

            case "balance_rupiah_formatted":
                if (plugin.getVaultHook().hasEconomy() && offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
                    double bal = plugin.getVaultHook().getBalance(offlinePlayer.getPlayer());
                    return "Rp " + String.format("%,.0f", bal);
                }
                return "Rp 0";

            case "balance_diamond":
                return "0";

            case "balance_diamond_formatted":
                return "0";

            case "rank_badge":
            case "badge":
                if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
                    String rank = plugin.getLuckPermsHook().getPlayerRankKey(offlinePlayer.getPlayer());
                    return switch (rank.toLowerCase()) {
                        case "ancestor", "owner" -> "<gradient:#8B0000:#FF0000><bold>👑 ANCESTOR</bold></gradient>";
                        case "warden", "admin" -> "<gradient:#1e3c72:#2a5298><bold>🛡 WARDEN</bold></gradient>";
                        case "herald", "mod" -> "<gradient:#f857a6:#ff5858><bold>📜 HERALD</bold></gradient>";
                        case "sions" -> "<gradient:#00FFFF:#FFD700><bold>✦ SIONS ✦</bold></gradient>";
                        case "emperor" -> "<gradient:#e52d27:#b31217><bold>⚔ EMPEROR</bold></gradient>";
                        case "sovereign" -> "<gradient:#f39c12:#f1c40f><bold>⚜ SOVEREIGN</bold></gradient>";
                        case "archon" -> "<gradient:#00c6ff:#0072ff><bold>💎 ARCHON</bold></gradient>";
                        case "ascendant" -> "<gradient:#11998e:#38ef7d><bold>☘ ASCENDANT</bold></gradient>";
                        default -> "<gradient:#bdc3c7:#7f8c8d>Wanderer</gradient>";
                    };
                }
                return "<gradient:#bdc3c7:#7f8c8d>Wanderer</gradient>";

            case "level_badge":
                return "<gradient:#f1c40f:#e67e22><bold>[Lv." + data.getLevel() + "]</bold></gradient>";

            case "kingdom_badge":
                if (data.getRegionId() != null) {
                    Optional<Region> regOpt = plugin.getRegionManager().getRegion(data.getRegionId());
                    if (regOpt.isPresent()) {
                        String k = regOpt.get().getKey().toUpperCase();
                        return switch (k) {
                            case "ZENITHAR" -> "<gradient:#ffd700:#ffa502><bold>[👑 ZENITHAR]</bold></gradient>";
                            case "SOLTERRA" -> "<gradient:#ff4757:#ff6b81><bold>[🔥 SOLTERRA]</bold></gradient>";
                            case "SYLVAMOOR" -> "<gradient:#2ed573:#1e90ff><bold>[🌿 SYLVAMOOR]</bold></gradient>";
                            default -> "<gradient:#70a1ff:#1e90ff><bold>[" + k + "]</bold></gradient>";
                        };
                    }
                }
                return "<dark_gray>[No Kingdom]</dark_gray>";

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

            case "in_own_territory":
                if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null && data.getRegionId() != null) {
                    Optional<Region> reg = plugin.getRegionManager().getRegion(data.getRegionId());
                    return reg.map(r -> String.valueOf(r.containsLocation(offlinePlayer.getPlayer().getLocation()))).orElse("false");
                }
                return "false";

            case "combat_tagged":
                if (plugin.getCombatTagService() != null) {
                    return String.valueOf(plugin.getCombatTagService().isCombatTagged(offlinePlayer.getUniqueId()));
                }
                return "false";

            case "combat_timer":
                if (plugin.getCombatTagService() != null) {
                    return String.valueOf(plugin.getCombatTagService().getRemainingSeconds(offlinePlayer.getUniqueId()));
                }
                return "0";

            case "war_status":
                if (plugin.getWarManager() != null && plugin.getWarManager().isWarActive()) {
                    return "WAR";
                }
                return "PEACE";

            case "war_timer":
                if (plugin.getWarManager() != null) {
                    return String.valueOf(plugin.getWarManager().getRemainingSeconds());
                }
                return "0";

            case "online_zenithar":
                return String.valueOf(countKingdomOnline("ZENITHAR"));

            case "online_solterra":
                return String.valueOf(countKingdomOnline("SOLTERRA"));

            case "online_sylvamoor":
                return String.valueOf(countKingdomOnline("SYLVAMOOR"));

            case "online_kingdom_members":
                if (data.getRegionId() != null) {
                    Optional<Region> rOpt = plugin.getRegionManager().getRegion(data.getRegionId());
                    if (rOpt.isPresent()) {
                        return String.valueOf(countKingdomOnline(rOpt.get().getKey()));
                    }
                }
                return "0";

            case "cosmetic_aura":
                return data.getActiveAura() != null ? data.getActiveAura() : "None";

            case "cosmetic_trail":
                return data.getActiveTrail() != null ? data.getActiveTrail() : "None";

            case "cosmetic_kill":
                return data.getActiveKillEffect() != null ? data.getActiveKillEffect() : "None";

            default:
                return null;
        }
    }

    private long countKingdomOnline(String kingdomKey) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> plugin.getApi().getPlayerRegionKey(p.getUniqueId()).equalsIgnoreCase(kingdomKey))
                .count();
    }
}
