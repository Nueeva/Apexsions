package com.apexsions.core.security;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Security listener monitoring block reach distance and high-frequency ore mining spikes (Anti-XRay anomaly detector).
 */
public class AntiXrayListener implements Listener {

    public enum OreTier {
        ANCIENT_DEBRIS(3, "ancient debris"),
        DIAMOND_EMERALD(6, "diamond / emerald"),
        STANDARD_ORE(15, "rare ore / mineral");

        private final int threshold;
        private final String label;

        OreTier(int threshold, String label) {
            this.threshold = threshold;
            this.label = label;
        }

        public int getThreshold() { return threshold; }
        public String getLabel() { return label; }
    }

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, Map<OreTier, List<Long>>> oreHistory = new ConcurrentHashMap<>();
    private final Map<String, Long> lastStaffAlert = new ConcurrentHashMap<>();

    private static final int WINDOW_MILLIS = 60_000;
    private static final double MAX_REACH_DISTANCE = 5.2;

    public AntiXrayListener(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    private OreTier getOreTier(Material mat) {
        if (mat == Material.ANCIENT_DEBRIS) {
            return OreTier.ANCIENT_DEBRIS;
        }
        if (mat == Material.DIAMOND_ORE || mat == Material.DEEPSLATE_DIAMOND_ORE ||
            mat == Material.EMERALD_ORE || mat == Material.DEEPSLATE_EMERALD_ORE) {
            return OreTier.DIAMOND_EMERALD;
        }
        if (mat == Material.GOLD_ORE || mat == Material.DEEPSLATE_GOLD_ORE ||
            mat == Material.NETHER_GOLD_ORE || mat == Material.RAW_IRON_BLOCK ||
            mat == Material.RAW_COPPER_BLOCK || mat == Material.RAW_GOLD_BLOCK) {
            return OreTier.STANDARD_ORE;
        }
        return null;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // 1. Raytrace Reach Check
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            double distance = player.getEyeLocation().distance(block.getLocation().add(0.5, 0.5, 0.5));
            if (distance > MAX_REACH_DISTANCE && !player.isOp()) {
                event.setCancelled(true);
                player.sendActionBar(mm.deserialize("<red>✖ Interaksi blok dibatalkan (Jarak jangkau tidak wajar: " + String.format("%.2f", distance) + "m)!</red>"));
                return;
            }
        }

        // 2. Anti-XRay Ore Mining Spike Tracker (Segregated by Tier)
        Material mat = block.getType();
        OreTier tier = getOreTier(mat);
        if (tier != null) {
            UUID playerId = player.getUniqueId();
            long now = System.currentTimeMillis();

            Map<OreTier, List<Long>> playerHistory = oreHistory.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
            List<Long> timestamps = playerHistory.computeIfAbsent(tier, k -> Collections.synchronizedList(new ArrayList<>()));
            timestamps.add(now);

            // Clean old entries
            timestamps.removeIf(ts -> now - ts > WINDOW_MILLIS);

            int count = timestamps.size();
            if (count >= tier.getThreshold()) {
                String alertKey = playerId + ":" + tier.name();
                Long lastAlertTime = lastStaffAlert.get(alertKey);
                if (lastAlertTime == null || now - lastAlertTime > 45_000) {
                    lastStaffAlert.put(alertKey, now);
                    String oreName = mat.name().replace("_", " ").toLowerCase();
                    int x = block.getX();
                    int y = block.getY();
                    int z = block.getZ();

                    String alertMsg = "<dark_red><bold>[Anti-XRay Alert]</bold></dark_red> " +
                            "<gold>" + player.getName() + "</gold> <gray>menambang </gray>" +
                            "<aqua><bold>" + count + "x " + oreName + "</bold></aqua> " +
                            "<gray>dalam 60 detik di </gray><yellow>[" + x + ", " + y + ", " + z + "]</yellow><gray>!</gray>";

                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (online.hasPermission("apexsions.staff") || online.hasPermission("apexsions.staff.alerts") || online.isOp()) {
                            online.sendMessage(mm.deserialize(alertMsg));
                            online.playSound(online.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.7f, 1.8f);
                        }
                    }

                    plugin.getLogger().warning("[Anti-XRay Alert] Player " + player.getName() + " mined " + count + " " + oreName + " within 60s at [" + x + "," + y + "," + z + "].");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        oreHistory.remove(playerId);
        lastStaffAlert.keySet().removeIf(k -> k.startsWith(playerId.toString()));
    }
}
