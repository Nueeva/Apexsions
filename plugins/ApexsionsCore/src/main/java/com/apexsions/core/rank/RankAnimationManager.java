package com.apexsions.core.rank;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ultra-Smooth Animated Rank Prefix & Nameplate Engine.
 * Features shifting multi-phase RGB wave gradients for all 9 official server ranks,
 * Royal Monarch titles, 3 Kingdom factions, multi-scoreboard synchronization,
 * and live Tablist updates.
 */
public class RankAnimationManager {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private BukkitTask animationTask;
    private int currentFrame = 0;

    // Cache to prevent duplicate scoreboard team updates when prefix hasn't visually changed
    private final Map<UUID, String> lastPrefixCache = new ConcurrentHashMap<>();

    // ════════════════ 9 OFFICIAL RANK GRADIENT WAVE FRAMES (8 Frames Each) ════════════════

    // 1. ANCESTOR (Weight 100 - Owner Tier): Blood Ruby & Dark Crimson Flame
    private static final List<String[]> ANCESTOR_FRAMES = List.of(
            new String[]{"#8B0000", "#FF0000", "#ff4757"},
            new String[]{"#FF0000", "#ff4757", "#8B0000"},
            new String[]{"#ff4757", "#8B0000", "#FF0000"},
            new String[]{"#8B0000", "#c0392b", "#e74c3c"},
            new String[]{"#c0392b", "#e74c3c", "#8B0000"},
            new String[]{"#e74c3c", "#8B0000", "#c0392b"},
            new String[]{"#8B0000", "#ff6b81", "#FF0000"},
            new String[]{"#ff6b81", "#FF0000", "#8B0000"}
    );

    // 2. WARDEN (Weight 90 - Head Staff / Admin): Deep Sapphire & Royal Navy
    private static final List<String[]> WARDEN_FRAMES = List.of(
            new String[]{"#1e3c72", "#2a5298", "#4a69bd"},
            new String[]{"#2a5298", "#4a69bd", "#1e3c72"},
            new String[]{"#4a69bd", "#1e3c72", "#2a5298"},
            new String[]{"#1e3c72", "#0c2461", "#4a69bd"},
            new String[]{"#0c2461", "#4a69bd", "#1e3c72"},
            new String[]{"#4a69bd", "#1e3c72", "#0c2461"},
            new String[]{"#1e3c72", "#1e90ff", "#2a5298"},
            new String[]{"#1e90ff", "#2a5298", "#1e3c72"}
    );

    // 3. HERALD (Weight 80 - Staff / Moderator): Hot Pink & Flaming Coral
    private static final List<String[]> HERALD_FRAMES = List.of(
            new String[]{"#f857a6", "#ff5858", "#ff6b81"},
            new String[]{"#ff5858", "#ff6b81", "#f857a6"},
            new String[]{"#ff6b81", "#f857a6", "#ff5858"},
            new String[]{"#f857a6", "#e84393", "#ff5858"},
            new String[]{"#e84393", "#ff5858", "#f857a6"},
            new String[]{"#ff5858", "#f857a6", "#e84393"},
            new String[]{"#f857a6", "#fd79a8", "#ff5858"},
            new String[]{"#fd79a8", "#ff5858", "#f857a6"}
    );

    // 4. SIONS (Weight 70 - Apex Donator Tier): Neon Cyan & Radiant Sun Gold
    private static final List<String[]> SIONS_FRAMES = List.of(
            new String[]{"#00FFFF", "#FFD700", "#00d2d3"},
            new String[]{"#FFD700", "#00d2d3", "#00FFFF"},
            new String[]{"#00d2d3", "#00FFFF", "#FFD700"},
            new String[]{"#00FFFF", "#fff200", "#00cec9"},
            new String[]{"#fff200", "#00cec9", "#00FFFF"},
            new String[]{"#00cec9", "#00FFFF", "#fff200"},
            new String[]{"#00FFFF", "#fffa65", "#00d2d3"},
            new String[]{"#fffa65", "#00d2d3", "#00FFFF"}
    );

    // 5. EMPEROR (Weight 60 - Donator Tier 4): Imperial Ruby & Fiery Crimson
    private static final List<String[]> EMPEROR_FRAMES = List.of(
            new String[]{"#e52d27", "#b31217", "#ff4757"},
            new String[]{"#b31217", "#ff4757", "#e52d27"},
            new String[]{"#ff4757", "#e52d27", "#b31217"},
            new String[]{"#e52d27", "#c0392b", "#b31217"},
            new String[]{"#c0392b", "#b31217", "#e52d27"},
            new String[]{"#b31217", "#e52d27", "#c0392b"},
            new String[]{"#e52d27", "#ff6b81", "#b31217"},
            new String[]{"#ff6b81", "#b31217", "#e52d27"}
    );

    // 6. SOVEREIGN (Weight 50 - Donator Tier 3): Royal Amber & Citrine Gold
    private static final List<String[]> SOVEREIGN_FRAMES = List.of(
            new String[]{"#f39c12", "#f1c40f", "#ffd32a"},
            new String[]{"#f1c40f", "#ffd32a", "#f39c12"},
            new String[]{"#ffd32a", "#f39c12", "#f1c40f"},
            new String[]{"#f39c12", "#e67e22", "#f1c40f"},
            new String[]{"#e67e22", "#f1c40f", "#f39c12"},
            new String[]{"#f1c40f", "#f39c12", "#e67e22"},
            new String[]{"#f39c12", "#ff9f1a", "#ffd32a"},
            new String[]{"#ff9f1a", "#ffd32a", "#f39c12"}
    );

    // 7. ARCHON (Weight 40 - Donator Tier 2): Electric Azure & Sky Diamond
    private static final List<String[]> ARCHON_FRAMES = List.of(
            new String[]{"#00c6ff", "#0072ff", "#70a1ff"},
            new String[]{"#0072ff", "#70a1ff", "#00c6ff"},
            new String[]{"#70a1ff", "#00c6ff", "#0072ff"},
            new String[]{"#00c6ff", "#1e90ff", "#0072ff"},
            new String[]{"#1e90ff", "#0072ff", "#00c6ff"},
            new String[]{"#0072ff", "#00c6ff", "#1e90ff"},
            new String[]{"#00c6ff", "#54a0ff", "#70a1ff"},
            new String[]{"#54a0ff", "#70a1ff", "#00c6ff"}
    );

    // 8. ASCENDANT (Weight 30 - Donator Tier 1): Mint Emerald & Spring Green
    private static final List<String[]> ASCENDANT_FRAMES = List.of(
            new String[]{"#11998e", "#38ef7d", "#2ecc71"},
            new String[]{"#38ef7d", "#2ecc71", "#11998e"},
            new String[]{"#2ecc71", "#11998e", "#38ef7d"},
            new String[]{"#11998e", "#00b894", "#38ef7d"},
            new String[]{"#00b894", "#38ef7d", "#11998e"},
            new String[]{"#38ef7d", "#11998e", "#00b894"},
            new String[]{"#11998e", "#55efc4", "#2ecc71"},
            new String[]{"#55efc4", "#2ecc71", "#11998e"}
    );

    // 9. WANDERER (Weight 10 - Default Rank): Silver Mist & Diamond Aqua
    private static final List<String[]> WANDERER_FRAMES = List.of(
            new String[]{"#dfe6e9", "#b2bec3", "#74b9ff"},
            new String[]{"#74b9ff", "#dfe6e9", "#b2bec3"},
            new String[]{"#b2bec3", "#74b9ff", "#dfe6e9"},
            new String[]{"#81ecec", "#74b9ff", "#dfe6e9"},
            new String[]{"#dfe6e9", "#81ecec", "#74b9ff"},
            new String[]{"#74b9ff", "#dfe6e9", "#81ecec"},
            new String[]{"#b2bec3", "#dfe6e9", "#81ecec"},
            new String[]{"#dfe6e9", "#b2bec3", "#74b9ff"}
    );

    // Monarch Royal Wave
    private static final List<String[]> MONARCH_FRAMES = List.of(
            new String[]{"#f1c40f", "#e67e22", "#f39c12"},
            new String[]{"#f39c12", "#f1c40f", "#e67e22"},
            new String[]{"#e67e22", "#f39c12", "#f1c40f"},
            new String[]{"#f1c40f", "#ffd32a", "#ff9f1a"},
            new String[]{"#ffd32a", "#ff9f1a", "#e74c3c"},
            new String[]{"#ff9f1a", "#e74c3c", "#f1c40f"},
            new String[]{"#e74c3c", "#f1c40f", "#ffd32a"},
            new String[]{"#f1c40f", "#f39c12", "#e67e22"}
    );

    public RankAnimationManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        // Update nameplates every 4 ticks (200ms) with smart delta frame caching
        animationTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            currentFrame++;
            for (Player player : Bukkit.getOnlinePlayers()) {
                updatePlayerNameplate(player);
            }
        }, 10L, 4L);
    }

    public void stop() {
        if (animationTask != null && !animationTask.isCancelled()) {
            animationTask.cancel();
            animationTask = null;
        }
        lastPrefixCache.clear();
    }

    /**
     * Returns an animated MiniMessage serialized prefix string for the player.
     */
    public String getAnimatedRankPrefix(Player player) {
        if (player == null) return "<gray>[Player]</gray> ";

        PlayerData data = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
        String kingdomKey = "NONE";
        if (data != null && data.getRegionId() != null) {
            kingdomKey = plugin.getRegionManager().getRegion(data.getRegionId())
                    .map(Region::getKey)
                    .orElse("NONE");
        }

        // 1. Monarch Prestige (Highest Priority)
        String kingName = plugin.getConfigManager().getKingdomKing(kingdomKey);
        if (kingName != null && player.getName().equalsIgnoreCase(kingName)) {
            String[] c = MONARCH_FRAMES.get(currentFrame % MONARCH_FRAMES.size());
            return "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>👑 RAJA " + kingdomKey.toUpperCase() + "</bold></gradient> ";
        }

        // 2. LuckPerms Rank Resolution
        String rankKey = (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isAvailable())
                ? plugin.getLuckPermsHook().getPlayerRankKey(player)
                : (player.isOp() ? "ancestor" : "wanderer");

        return switch (rankKey.toLowerCase().trim()) {
            case "ancestor", "owner" -> {
                String[] c = ANCESTOR_FRAMES.get(currentFrame % ANCESTOR_FRAMES.size());
                yield "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>[👑 ANCESTOR]</bold></gradient> ";
            }
            case "warden", "admin", "headadmin" -> {
                String[] c = WARDEN_FRAMES.get(currentFrame % WARDEN_FRAMES.size());
                yield "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>[🛡 WARDEN]</bold></gradient> ";
            }
            case "herald", "mod", "moderator" -> {
                String[] c = HERALD_FRAMES.get(currentFrame % HERALD_FRAMES.size());
                yield "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>[📜 HERALD]</bold></gradient> ";
            }
            case "sions" -> {
                String[] c = SIONS_FRAMES.get(currentFrame % SIONS_FRAMES.size());
                yield "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>[✦ SIONS]</bold></gradient> ";
            }
            case "emperor" -> {
                String[] c = EMPEROR_FRAMES.get(currentFrame % EMPEROR_FRAMES.size());
                yield "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>[⚔ EMPEROR]</bold></gradient> ";
            }
            case "sovereign" -> {
                String[] c = SOVEREIGN_FRAMES.get(currentFrame % SOVEREIGN_FRAMES.size());
                yield "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>[⚜ SOVEREIGN]</bold></gradient> ";
            }
            case "archon" -> {
                String[] c = ARCHON_FRAMES.get(currentFrame % ARCHON_FRAMES.size());
                yield "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>[💎 ARCHON]</bold></gradient> ";
            }
            case "ascendant" -> {
                String[] c = ASCENDANT_FRAMES.get(currentFrame % ASCENDANT_FRAMES.size());
                yield "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>[☘ ASCENDANT]</bold></gradient> ";
            }
            default -> {
                // If in a kingdom, show animated kingdom tag
                if (!kingdomKey.equalsIgnoreCase("NONE")) {
                    String[] c = switch (kingdomKey.toUpperCase()) {
                        case "ZENITHAR" -> SOVEREIGN_FRAMES.get(currentFrame % SOVEREIGN_FRAMES.size());
                        case "SOLTERRA" -> EMPEROR_FRAMES.get(currentFrame % EMPEROR_FRAMES.size());
                        case "SYLVAMOOR" -> ASCENDANT_FRAMES.get(currentFrame % ASCENDANT_FRAMES.size());
                        default -> WANDERER_FRAMES.get(currentFrame % WANDERER_FRAMES.size());
                    };
                    yield "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>[⚜ " + kingdomKey.toUpperCase() + "]</bold></gradient> ";
                }
                String[] c = WANDERER_FRAMES.get(currentFrame % WANDERER_FRAMES.size());
                yield "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>[Wanderer]</bold></gradient> ";
            }
        };
    }

    /**
     * Updates the player's nametag scoreboard team, Tablist, and display name using Delta Caching.
     */
    public void updatePlayerNameplate(Player player) {
        if (!player.isOnline()) {
            lastPrefixCache.remove(player.getUniqueId());
            return;
        }

        String prefixMm = getAnimatedRankPrefix(player);
        PlayerData data = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
        if (data != null && data.getActiveTitle() != null && !data.getActiveTitle().trim().isEmpty()) {
            prefixMm = prefixMm + "<yellow>[" + data.getActiveTitle().trim() + "]</yellow> ";
        }

        // Smart Delta Check: If visual prefix hasn't changed, skip update
        String cached = lastPrefixCache.get(player.getUniqueId());
        if (prefixMm.equals(cached)) {
            return;
        }
        lastPrefixCache.put(player.getUniqueId(), prefixMm);

        Component prefixComp = mm.deserialize(prefixMm);
        Component fullTabName = prefixComp.append(Component.text(player.getName()));

        // 1. Update Tablist Name (Live animated in TAB list)
        player.playerListName(fullTabName);

        // 2. Update Display Name
        player.displayName(fullTabName);

        // 3. Update Scoreboard Team for Nameplate above head across all active scoreboards
        String teamName = "apx_" + player.getName();
        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
        }

        // Synchronize on Main Server Scoreboard
        Scoreboard mainBoard = Bukkit.getScoreboardManager().getMainScoreboard();
        applyTeam(mainBoard, teamName, player.getName(), prefixComp);

        // Synchronize on all active viewers' Scoreboards
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            Scoreboard viewerBoard = viewer.getScoreboard();
            if (viewerBoard != mainBoard) {
                applyTeam(viewerBoard, teamName, player.getName(), prefixComp);
            }
        }
    }

    /**
     * Registers all current player teams on a newly joined player's scoreboard.
     */
    public void setupScoreboardForNewPlayer(Player newPlayer) {
        Scoreboard board = newPlayer.getScoreboard();
        for (Player online : Bukkit.getOnlinePlayers()) {
            String prefixMm = lastPrefixCache.getOrDefault(online.getUniqueId(), getAnimatedRankPrefix(online));
            Component prefixComp = mm.deserialize(prefixMm);
            String teamName = "apx_" + online.getName();
            if (teamName.length() > 16) {
                teamName = teamName.substring(0, 16);
            }
            applyTeam(board, teamName, online.getName(), prefixComp);
        }
    }

    private void applyTeam(Scoreboard board, String teamName, String entryName, Component prefix) {
        if (board == null) return;
        try {
            Team team = board.getTeam(teamName);
            if (team == null) {
                team = board.registerNewTeam(teamName);
            }
            if (!team.hasEntry(entryName)) {
                team.addEntry(entryName);
            }
            team.prefix(prefix);
        } catch (Exception ignored) {}
    }
}
