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
 * High-performance Animated Rank Prefix & Nameplate Engine.
 * Features shifting RGB gradients, smart delta frame detection, and zero redundant team packet spam.
 */
public class RankAnimationManager {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private BukkitTask animationTask;
    private int currentFrame = 0;

    // Cache to prevent duplicate scoreboard team updates when prefix hasn't visually changed
    private final Map<UUID, String> lastPrefixCache = new ConcurrentHashMap<>();

    // Gradient wave color presets for shifting animation (Hex values)
    private static final List<String[]> MONARCH_GRADIENT_FRAMES = List.of(
            new String[]{"#f1c40f", "#e67e22", "#f39c12"},
            new String[]{"#e67e22", "#f39c12", "#f1c40f"},
            new String[]{"#f39c12", "#f1c40f", "#e67e22"},
            new String[]{"#f1c40f", "#f39c12", "#e74c3c"},
            new String[]{"#f39c12", "#e74c3c", "#f1c40f"},
            new String[]{"#e74c3c", "#f1c40f", "#f39c12"}
    );

    private static final List<String[]> ZENITHAR_GRADIENT_FRAMES = List.of(
            new String[]{"#ffe900", "#f39c12", "#ffe900"},
            new String[]{"#fff275", "#ffe900", "#f39c12"},
            new String[]{"#ffe900", "#fff275", "#ffe900"},
            new String[]{"#f39c12", "#ffe900", "#fff275"}
    );

    private static final List<String[]> SOLTERRA_GRADIENT_FRAMES = List.of(
            new String[]{"#ff4d4d", "#c0392b", "#e67e22"},
            new String[]{"#ff6b6b", "#ff4d4d", "#c0392b"},
            new String[]{"#c0392b", "#e67e22", "#ff4d4d"},
            new String[]{"#e67e22", "#ff6b6b", "#c0392b"}
    );

    private static final List<String[]> SYLVAMOOR_GRADIENT_FRAMES = List.of(
            new String[]{"#87ceeb", "#3498db", "#2ecc71"},
            new String[]{"#00d2d3", "#87ceeb", "#3498db"},
            new String[]{"#3498db", "#2ecc71", "#00d2d3"},
            new String[]{"#2ecc71", "#00d2d3", "#87ceeb"}
    );

    private static final List<String[]> VIP_GRADIENT_FRAMES = List.of(
            new String[]{"#9b59b6", "#8e44ad", "#e056fd"},
            new String[]{"#be2edd", "#9b59b6", "#8e44ad"},
            new String[]{"#e056fd", "#be2edd", "#9b59b6"},
            new String[]{"#8e44ad", "#e056fd", "#be2edd"}
    );

    public RankAnimationManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        // Update nameplates only every 5 ticks (250ms) using smart delta caching
        animationTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            currentFrame++;
            for (Player player : Bukkit.getOnlinePlayers()) {
                updatePlayerNameplate(player);
            }
        }, 10L, 5L);
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
        if (player == null) return "<gray>[Player]</gray>";

        PlayerData data = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
        String kingdomKey = "NONE";
        if (data != null && data.getRegionId() != null) {
            kingdomKey = plugin.getRegionManager().getRegion(data.getRegionId())
                    .map(Region::getKey)
                    .orElse("NONE");
        }

        // 1. Check Monarch Status
        String kingName = plugin.getConfigManager().getKingdomKing(kingdomKey);
        if (kingName != null && player.getName().equalsIgnoreCase(kingName)) {
            String[] colors = MONARCH_GRADIENT_FRAMES.get(currentFrame % MONARCH_GRADIENT_FRAMES.size());
            return "<gradient:" + colors[0] + ":" + colors[1] + ":" + colors[2] + "><bold>👑 RAJA</bold></gradient> ";
        }

        // 2. Check VIP / Staff Permissions
        if (player.isOp() || player.hasPermission("apexsions.rank.admin")) {
            String[] colors = MONARCH_GRADIENT_FRAMES.get(currentFrame % MONARCH_GRADIENT_FRAMES.size());
            return "<gradient:" + colors[0] + ":" + colors[1] + ":" + colors[2] + "><bold>⚡ ADMIN</bold></gradient> ";
        }

        if (player.hasPermission("apexsions.rank.vip")) {
            String[] colors = VIP_GRADIENT_FRAMES.get(currentFrame % VIP_GRADIENT_FRAMES.size());
            return "<gradient:" + colors[0] + ":" + colors[1] + ":" + colors[2] + "><bold>✦ VIP</bold></gradient> ";
        }

        // 3. Check Kingdom-specific Animated Prefix
        return switch (kingdomKey.toUpperCase()) {
            case "ZENITHAR" -> {
                String[] c = ZENITHAR_GRADIENT_FRAMES.get(currentFrame % ZENITHAR_GRADIENT_FRAMES.size());
                yield "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>⚜ ZENITHAR</bold></gradient> ";
            }
            case "SOLTERRA" -> {
                String[] c = SOLTERRA_GRADIENT_FRAMES.get(currentFrame % SOLTERRA_GRADIENT_FRAMES.size());
                yield "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>⚜ SOLTERRA</bold></gradient> ";
            }
            case "SYLVAMOOR" -> {
                String[] c = SYLVAMOOR_GRADIENT_FRAMES.get(currentFrame % SYLVAMOOR_GRADIENT_FRAMES.size());
                yield "<gradient:" + c[0] + ":" + c[1] + ":" + c[2] + "><bold>⚜ SYLVAMOOR</bold></gradient> ";
            }
            default -> "<gray>[Wanderer]</gray> ";
        };
    }

    /**
     * Updates the player's nametag scoreboard team using Delta Caching to avoid TPS overhead.
     */
    public void updatePlayerNameplate(Player player) {
        if (!player.isOnline()) {
            lastPrefixCache.remove(player.getUniqueId());
            return;
        }

        String prefixMm = getAnimatedRankPrefix(player);
        PlayerData data = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
        if (data != null && data.getActiveTitle() != null && !data.getActiveTitle().isEmpty()) {
            prefixMm = prefixMm + data.getActiveTitle() + " ";
        }

        // Smart Delta Check: If visual prefix hasn't changed, skip scoreboard write!
        String cached = lastPrefixCache.get(player.getUniqueId());
        if (prefixMm.equals(cached)) {
            return;
        }
        lastPrefixCache.put(player.getUniqueId(), prefixMm);

        Scoreboard board = player.getScoreboard();
        String teamName = "apx_" + player.getName();
        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
        }

        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
        }

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }

        Component prefixComp = mm.deserialize(prefixMm);
        team.prefix(prefixComp);
    }
}
