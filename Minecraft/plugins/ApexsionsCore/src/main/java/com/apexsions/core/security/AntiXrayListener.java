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

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, List<Long>> oreHistory = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastStaffAlert = new ConcurrentHashMap<>();

    private final Set<Material> monitoredOres = Set.of(
            Material.DIAMOND_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.ANCIENT_DEBRIS,
            Material.EMERALD_ORE,
            Material.DEEPSLATE_EMERALD_ORE
    );

    private static final int WINDOW_MILLIS = 60_000;
    private static final int SPIKE_THRESHOLD = 8;
    private static final double MAX_REACH_DISTANCE = 5.8;

    public AntiXrayListener(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
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

        // 2. Anti-XRay Ore Mining Spike Tracker
        Material mat = block.getType();
        if (monitoredOres.contains(mat)) {
            UUID playerId = player.getUniqueId();
            long now = System.currentTimeMillis();

            List<Long> timestamps = oreHistory.computeIfAbsent(playerId, k -> Collections.synchronizedList(new ArrayList<>()));
            timestamps.add(now);

            // Clean old entries
            timestamps.removeIf(ts -> now - ts > WINDOW_MILLIS);

            if (timestamps.size() >= SPIKE_THRESHOLD) {
                Long lastAlertTime = lastStaffAlert.get(playerId);
                if (lastAlertTime == null || now - lastAlertTime > 45_000) {
                    lastStaffAlert.put(playerId, now);
                    int count = timestamps.size();
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
}
