package com.apexsions.core.security;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Combat security listener protecting against KillAura (angle & wall-hit), Reach hacks, and Auto-Clicker exploits.
 */
public class CombatSecurityListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final Map<UUID, Queue<Long>> attackHistory = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> combatViolations = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastCombatAlert = new ConcurrentHashMap<>();

    private static final double MAX_SURVIVAL_REACH = 4.2; // blocks (accounting for ping & bounding box)
    private static final double MAX_KILLAURA_ANGLE = 95.0; // degrees
    private static final int MAX_CPS_LIMIT = 20; // clicks/attacks per second
    private static final long ALERT_COOLDOWN_MS = 5000L;

    public CombatSecurityListener(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        attackHistory.remove(uuid);
        combatViolations.remove(uuid);
        lastCombatAlert.remove(uuid);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        // 0. Configuration Master Toggles
        if (!plugin.getConfig().getBoolean("security.enabled", true) ||
            !plugin.getConfig().getBoolean("security.combat.enabled", true)) {
            return;
        }

        // Exempt Creative/Spectator or staff bypass
        if (attacker.getGameMode() == GameMode.CREATIVE || attacker.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        if (attacker.hasPermission("apexsions.bypass.combat")) {
            return;
        }

        Entity target = event.getEntity();
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        Location eyeLoc = attacker.getEyeLocation();
        Location targetCenter = livingTarget.getLocation().add(0, livingTarget.getHeight() / 2.0, 0);
        double distance = eyeLoc.distance(targetCenter);

        // 1. Combat Reach Check
        double maxReach = plugin.getConfig().getDouble("security.combat.max-reach", MAX_SURVIVAL_REACH);
        if (distance > maxReach) {
            event.setCancelled(true);
            handleViolation(attacker, "Reach Hack (" + String.format("%.2f", distance) + "m)");
            return;
        }

        // 2. KillAura Angle Check
        Vector toTarget = targetCenter.toVector().subtract(eyeLoc.toVector()).normalize();
        boolean killauraCheck = plugin.getConfig().getBoolean("security.combat.killaura-check", true);
        if (killauraCheck) {
            Vector eyeDir = eyeLoc.getDirection().normalize();
            double angle = Math.toDegrees(eyeDir.angle(toTarget));
            double maxAngle = plugin.getConfig().getDouble("security.combat.max-killaura-angle", MAX_KILLAURA_ANGLE);

            if (angle > maxAngle) {
                event.setCancelled(true);
                handleViolation(attacker, "KillAura Angle (" + String.format("%.1f", angle) + "°)");
                return;
            }
        }

        // 3. Wall-Hit (Line of Sight Raycast) Check
        // Ensure no solid block is between attacker eyes and target center
        if (distance > 0.8) {
            RayTraceResult blockHit = eyeLoc.getWorld().rayTraceBlocks(
                    eyeLoc,
                    toTarget,
                    distance - 0.3,
                    FluidCollisionMode.NEVER,
                    true
            );

            if (blockHit != null && blockHit.getHitBlock() != null && blockHit.getHitBlock().getType().isOccluding()) {
                event.setCancelled(true);
                handleViolation(attacker, "Wall-Hit / Phase Strike");
                return;
            }
        }

        // 4. CPS Rate Limiter (Auto-Clicker check)
        UUID uuid = attacker.getUniqueId();
        long now = System.currentTimeMillis();
        Queue<Long> history = attackHistory.computeIfAbsent(uuid, k -> new LinkedList<>());

        synchronized (history) {
            history.add(now);
            while (!history.isEmpty() && (now - history.peek()) > 1000L) {
                history.poll();
            }

            if (history.size() > MAX_CPS_LIMIT) {
                event.setCancelled(true);
                attacker.sendActionBar(mm.deserialize("<red>⚠ Frekuensi serangan terlalu tinggi (Auto-Clicker dibatasi).</red>"));
            }
        }
    }

    private void handleViolation(Player attacker, String detail) {
        UUID uuid = attacker.getUniqueId();
        int vl = combatViolations.getOrDefault(uuid, 0) + 1;
        combatViolations.put(uuid, vl);

        attacker.sendActionBar(mm.deserialize("<red>⚠ Serangan dibatalkan: Anomali tempur terdeteksi (" + detail + ").</red>"));

        if (vl >= 5) {
            long now = System.currentTimeMillis();
            Long lastAlert = lastCombatAlert.get(uuid);
            if (lastAlert == null || (now - lastAlert) > ALERT_COOLDOWN_MS) {
                lastCombatAlert.put(uuid, now);

                String staffMsg = "<gold>[<red>Apexsions Combat Guard</red>]</gold> <yellow>" + attacker.getName() +
                        "</yellow> <gray>terdeteksi</gray> <red>" + detail +
                        "</red> <gray>(VL: " + vl + ", Ping: " + attacker.getPing() + "ms)</gray>";

                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("apexsions.staff") || p.hasPermission("apexsions.admin"))
                        .forEach(staff -> staff.sendMessage(mm.deserialize(staffMsg)));

                plugin.getLogger().warning("[CombatGuard] " + attacker.getName() + " flagged for " + detail + " (" + vl + " VL).");
            }
        }
    }
}
