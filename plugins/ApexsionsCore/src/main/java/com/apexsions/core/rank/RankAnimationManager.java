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
 * Features shifting multi-phase RGB wave gradients, multi-scoreboard synchronization,
 * Tablist (TAB) live updates, and zero-overhead delta frame caching.
 */
public class RankAnimationManager {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private BukkitTask animationTask;
    private int currentFrame = 0;

    // Cache to prevent duplicate scoreboard team updates when prefix hasn't visually changed
    private final Map<UUID, String> lastPrefixCache = new ConcurrentHashMap<>();

    // ════════════════ MULTI-PHASE GRADIENT WAVE FRAMES (8 - 12 Frames Each) ════════════════

    // 1. Monarch: Majestic Crown Gold & Amber Blaze
    private static final List<String[]> MONARCH_GRADIENT_FRAMES = List.of(
            new String[]{"#f1c40f", "#e67e22", "#f39c12"},
            new String[]{"#f39c12", "#f1c40f", "#e67e22"},
            new String[]{"#e67e22", "#f39c12", "#f1c40f"},
            new String[]{"#f1c40f", "#ffd32a", "#ff9f1a"},
            new String[]{"#ffd32a", "#ff9f1a", "#e74c3c"},
            new String[]{"#ff9f1a", "#e74c3c", "#f1c40f"},
            new String[]{"#e74c3c", "#f1c40f", "#ffd32a"},
            new String[]{"#f1c40f", "#f39c12", "#e67e22"}
    );

    // 2. Owner / Admin: Cyberpunk Scarlet & Crimson Spark
    private static final List<String[]> ADMIN_GRADIENT_FRAMES = List.of(
            new String[]{"#ff3838", "#c56cf0", "#ff4d4d"},
            new String[]{"#ff4d4d", "#ff3838", "#c56cf0"},
            new String[]{"#c56cf0", "#ff4d4d", "#ff3838"},
            new String[]{"#ff3838", "#ff4d4d", "#ff9f1a"},
            new String[]{"#ff4d4d", "#ff9f1a", "#c56cf0"},
            new String[]{"#ff9f1a", "#c56cf0", "#ff3838"}
    );

    // 3. Developer: Neon Cyan & Emerald Matrix
    private static final List<String[]> DEV_GRADIENT_FRAMES = List.of(
            new String[]{"#17c0eb", "#7158e2", "#18dcff"},
            new String[]{"#18dcff", "#17c0eb", "#7158e2"},
            new String[]{"#7158e2", "#18dcff", "#17c0eb"},
            new String[]{"#3ae374", "#17c0eb", "#18dcff"},
            new String[]{"#18dcff", "#3ae374", "#17c0eb"},
            new String[]{"#17c0eb", "#18dcff", "#3ae374"}
    );

    // 4. Mod / Staff: Royal Azure & Ice Blue
    private static final List<String[]> MOD_GRADIENT_FRAMES = List.of(
            new String[]{"#00d2d3", "#54a0ff", "#2e86de"},
            new String[]{"#54a0ff", "#2e86de", "#00d2d3"},
            new String[]{"#2e86de", "#00d2d3", "#54a0ff"},
            new String[]{"#00d2d3", "#0abde3", "#54a0ff"},
            new String[]{"#0abde3", "#54a0ff", "#00d2d3"},
            new String[]{"#54a0ff", "#00d2d3", "#0abde3"}
    );

    // 5. VIP / MVP: Radiant Violet & Orchid Crystal
    private static final List<String[]> VIP_GRADIENT_FRAMES = List.of(
            new String[]{"#9b59b6", "#8e44ad", "#e056fd"},
            new String[]{"#e056fd", "#9b59b6", "#8e44ad"},
            new String[]{"#8e44ad", "#e056fd", "#9b59b6"},
            new String[]{"#be2edd", "#e056fd", "#9b59b6"},
            new String[]{"#e056fd", "#be2edd", "#8e44ad"},
            new String[]{"#9b59b6", "#be2edd", "#e056fd"}
    );

    // 6. Zenithar: Mountain Citrine & Radiant Gold
    private static final List<String[]> ZENITHAR_GRADIENT_FRAMES = List.of(
            new String[]{"#ffe900", "#f39c12", "#ffe900"},
            new String[]{"#fff275", "#ffe900", "#f39c12"},
            new String[]{"#ffe900", "#fff275", "#ffe900"},
            new String[]{"#f39c12", "#ffe900", "#fff275"},
            new String[]{"#f1c40f", "#e67e22", "#fff275"},
            new String[]{"#fff275", "#f1c40f", "#e67e22"}
    );

    // 7. Solterra: Desert Solar Flare & Crimson Ember
    private static final List<String[]> SOLTERRA_GRADIENT_FRAMES = List.of(
            new String[]{"#ff4d4d", "#c0392b", "#e67e22"},
            new String[]{"#ff6b6b", "#ff4d4d", "#c0392b"},
            new String[]{"#c0392b", "#e67e22", "#ff4d4d"},
            new String[]{"#e67e22", "#ff6b6b", "#c0392b"},
            new String[]{"#ff7675", "#d63031", "#e17055"},
            new String[]{"#d63031", "#e17055", "#ff7675"}
    );

    // 8. Sylvamoor: Emerald Canopy & Forest Azure
    private static final List<String[]> SYLVAMOOR_GRADIENT_FRAMES = List.of(
            new String[]{"#87ceeb", "#3498db", "#2ecc71"},
            new String[]{"#00d2d3", "#87ceeb", "#3498db"},
            new String[]{"#3498db", "#2ecc71", "#00d2d3"},
            new String[]{"#2ecc71", "#00d2d3", "#87ceeb"},
            new String[]{"#55efc4", "#00b894", "#0984e3"},
            new String[]{"#00b894", "#0984e3", "#55efc4"}
    );

    // 9. Wanderer / Member / Default: Silver Pearl & Diamond Mist
    private static final List<String[]> WANDERER_GRADIENT_FRAMES = List.of(
            new String[]{"#dfe6e9", "#b2bec3", "#74b9ff"},
            new String[]{"#74b9ff", "#dfe6e9", "#b2bec3"},
            new String[]{"#b2bec3", "#74b9ff", "#dfe6e9"},
            new String[]{"#81ecec", "#74b9ff", "#dfe6e9"},
            new String[]{"#dfe6e9", "#81ecec", "#74b9ff"},
            new String[]{"#74b9ff", "#dfe6e9", "#81ecec"}
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

        // 1. Monarch Prestige (Highest priority)
        String kingName = plugin.getConfigManager().getKingdomKing(kingdomKey);
        if (kingName != null && player.getName().equalsIgnoreCase(kingName)) {
            String[] c = MONARCH_GRADIENT_FRAMES.get(currentFrame % MONARCH_GRADIENT_FRAMES.size());
            return "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>👑 RAJA " + kingdomKey.toUpperCase() + "</bold></gradient> ";
        }

        // 2. Staff & Permission Hierarchy
        if (player.isOp() || player.hasPermission("apexsions.rank.owner")) {
            String[] c = ADMIN_GRADIENT_FRAMES.get(currentFrame % ADMIN_GRADIENT_FRAMES.size());
            return "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>👑 OWNER</bold></gradient> ";
        }

        if (player.hasPermission("apexsions.rank.admin")) {
            String[] c = ADMIN_GRADIENT_FRAMES.get(currentFrame % ADMIN_GRADIENT_FRAMES.size());
            return "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>⚡ ADMIN</bold></gradient> ";
        }

        if (player.hasPermission("apexsions.rank.dev")) {
            String[] c = DEV_GRADIENT_FRAMES.get(currentFrame % DEV_GRADIENT_FRAMES.size());
            return "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>🔧 DEV</bold></gradient> ";
        }

        if (player.hasPermission("apexsions.rank.mod")) {
            String[] c = MOD_GRADIENT_FRAMES.get(currentFrame % MOD_GRADIENT_FRAMES.size());
            return "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>🛡️ MOD</bold></gradient> ";
        }

        if (player.hasPermission("apexsions.rank.mvp")) {
            String[] c = VIP_GRADIENT_FRAMES.get(currentFrame % VIP_GRADIENT_FRAMES.size());
            return "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>★ MVP</bold></gradient> ";
        }

        if (player.hasPermission("apexsions.rank.vip")) {
            String[] c = VIP_GRADIENT_FRAMES.get(currentFrame % VIP_GRADIENT_FRAMES.size());
            return "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>✦ VIP</bold></gradient> ";
        }

        // 3. Kingdom Affiliation
        switch (kingdomKey.toUpperCase()) {
            case "ZENITHAR": {
                String[] c = ZENITHAR_GRADIENT_FRAMES.get(currentFrame % ZENITHAR_GRADIENT_FRAMES.size());
                return "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>⚜ ZENITHAR</bold></gradient> ";
            }
            case "SOLTERRA": {
                String[] c = SOLTERRA_GRADIENT_FRAMES.get(currentFrame % SOLTERRA_GRADIENT_FRAMES.size());
                return "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>⚜ SOLTERRA</bold></gradient> ";
            }
            case "SYLVAMOOR": {
                String[] c = SYLVAMOOR_GRADIENT_FRAMES.get(currentFrame % SYLVAMOOR_GRADIENT_FRAMES.size());
                return "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>⚜ SYLVAMOOR</bold></gradient> ";
            }
            default: {
                String[] c = WANDERER_GRADIENT_FRAMES.get(currentFrame % WANDERER_GRADIENT_FRAMES.size());
                return "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>✦ WANDERER</bold></gradient> ";
            }
        }
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

        // 1. Update Tablist Name
        player.playerListName(fullTabName);

        // 2. Update Display Name
        player.displayName(fullTabName);

        // 3. Update Scoreboard Team for Nameplate above head
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
