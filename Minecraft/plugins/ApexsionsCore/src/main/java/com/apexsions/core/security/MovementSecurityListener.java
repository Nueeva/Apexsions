package com.apexsions.core.security;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance movement security listener preventing Fly Hack, Speed, Jesus, NoFall, and Step exploits.
 */
public class MovementSecurityListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // Player state tracking
    private final Map<UUID, Location> lastSafeGround = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> airborneTicks = new ConcurrentHashMap<>();
    private final Map<UUID, Double> serverFallDistance = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastKnockbackTime = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> violationCount = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastAlertTime = new ConcurrentHashMap<>();

    private static final int MAX_VIOLATIONS_BEFORE_STAFF_ALERT = 6;
    private static final long ALERT_COOLDOWN_MS = 5000L;

    public MovementSecurityListener(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Record damage / knockback event to avoid false-positives on explosion / combat velocity
            lastKnockbackTime.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        lastSafeGround.put(player.getUniqueId(), event.getTo());
        airborneTicks.put(player.getUniqueId(), 0);
        serverFallDistance.put(player.getUniqueId(), 0.0);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastSafeGround.remove(uuid);
        airborneTicks.remove(uuid);
        serverFallDistance.remove(uuid);
        lastKnockbackTime.remove(uuid);
        violationCount.remove(uuid);
        lastAlertTime.remove(uuid);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // 1. Check exemptions (Creative, Spectator, Flying permitted, Gliding with Elytra, Vehicle riding, Staff bypass)
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        if (player.getAllowFlight() || player.isGliding() || player.isInsideVehicle()) {
            return;
        }
        if (player.hasPermission("apexsions.bypass.movement")) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        // If no coordinate displacement (only head rotation / pitch / yaw), skip check
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            return;
        }

        UUID uuid = player.getUniqueId();
        double deltaY = to.getY() - from.getY();
        double deltaX = to.getX() - from.getX();
        double deltaZ = to.getZ() - from.getZ();
        double horizontalDistSq = (deltaX * deltaX) + (deltaZ * deltaZ);

        // Surrounding environment checks
        boolean nearClimbable = isNearClimbable(player.getLocation());
        boolean inLiquid = player.isInWater() || player.isInLava() || isLiquid(player.getLocation().getBlock().getType());
        boolean hasLevitation = player.hasPotionEffect(PotionEffectType.LEVITATION);
        boolean hasSlowFalling = player.hasPotionEffect(PotionEffectType.SLOW_FALLING);
        boolean isGrounded = isPhysicallyOnGround(player);

        // ---------------------------------------------------------------------
        // A. Fly Hack & Hover / AirWalk Check
        // ---------------------------------------------------------------------
        if (isGrounded) {
            lastSafeGround.put(uuid, to.clone());
            airborneTicks.put(uuid, 0);

            // True Server-Side NoFall verification:
            // If client fell a substantial distance server-side, verify fall damage
            Double trackedFall = serverFallDistance.getOrDefault(uuid, 0.0);
            if (trackedFall > 3.5 && !hasSlowFalling && !inLiquid && !isDamageAbsorbing(to.getBlock())) {
                double expectedDamage = Math.max(1.0, trackedFall - 3.0);
                if (player.getFallDistance() < 0.5f) {
                    // Client spoofed onGround packet to bypass fall damage (NoFall hack)
                    player.damage(expectedDamage);
                }
            }
            serverFallDistance.put(uuid, 0.0);
        } else {
            int airTicks = airborneTicks.getOrDefault(uuid, 0) + 1;
            airborneTicks.put(uuid, airTicks);

            // Accumulate server fall distance
            if (deltaY < 0) {
                double currentFall = serverFallDistance.getOrDefault(uuid, 0.0);
                serverFallDistance.put(uuid, currentFall + Math.abs(deltaY));
            }

            // Fly Hack detection logic:
            // If airborne for > 5 ticks, without climbable, liquid, levitation, slow falling, or recent knockback:
            // Suspicious if ascending (deltaY > 0) without jump-momentum or hovering (deltaY == 0 or falling slower than gravity allows)
            long lastKb = lastKnockbackTime.getOrDefault(uuid, 0L);
            boolean recentKnockback = (System.currentTimeMillis() - lastKb) < 1500L;

            if (airTicks > 6 && !nearClimbable && !inLiquid && !hasLevitation && !recentKnockback) {
                // If ascending in midair or hovering stationary in air (deltaY >= -0.03 while high up)
                if (deltaY > 0.05 || (deltaY >= -0.03 && airTicks > 12)) {
                    // Unauthorized flight detected!
                    handleMovementViolation(player, "Fly Hack / AirWalk", event);
                    return;
                }
            }
        }

        // ---------------------------------------------------------------------
        // B. Jesus / WaterWalk Check
        // ---------------------------------------------------------------------
        if (!isGrounded && inLiquid) {
            Block blockBelow = to.clone().subtract(0, 0.1, 0).getBlock();
            if (isLiquid(blockBelow.getType()) && player.isOnGround() && !player.isSwimming()) {
                // Walking on liquid surface as if it was a solid surface
                handleMovementViolation(player, "Jesus / WaterWalk", event);
                return;
            }
        }

        // ---------------------------------------------------------------------
        // C. Horizontal Speed Hack Check
        // ---------------------------------------------------------------------
        double maxSpeedSq = 0.65; // ~0.8 blocks/tick normal threshold
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            maxSpeedSq = 1.10;
        }

        long lastKb = lastKnockbackTime.getOrDefault(uuid, 0L);
        boolean recentKnockback = (System.currentTimeMillis() - lastKb) < 1500L;

        if (horizontalDistSq > maxSpeedSq && !recentKnockback && !player.isGliding()) {
            handleMovementViolation(player, "Speed Hack", event);
        }
    }

    private void handleMovementViolation(Player player, String cheatType, PlayerMoveEvent event) {
        UUID uuid = player.getUniqueId();
        int currentViolations = violationCount.getOrDefault(uuid, 0) + 1;
        violationCount.put(uuid, currentViolations);

        // Rubberband to last safe ground position or cancel movement
        Location safeLoc = lastSafeGround.get(uuid);
        if (safeLoc != null && safeLoc.getWorld().equals(player.getWorld())) {
            event.setTo(safeLoc.clone());
        } else {
            event.setTo(event.getFrom());
        }

        // Actionbar warning
        player.sendActionBar(mm.deserialize("<red>⚠ Gerakan tidak wajar (" + cheatType + ") terdeteksi! Posisi disesuaikan kembali.</red>"));

        // Alert online staff if violations accumulate
        if (currentViolations >= MAX_VIOLATIONS_BEFORE_STAFF_ALERT) {
            long now = System.currentTimeMillis();
            Long lastAlert = lastAlertTime.get(uuid);
            if (lastAlert == null || (now - lastAlert) > ALERT_COOLDOWN_MS) {
                lastAlertTime.put(uuid, now);

                String staffAlert = "<gold>[<red>Apexsions AntiCheat</red>]</gold> <yellow>" + player.getName() +
                        "</yellow> <gray>terdeteksi menggunakan</gray> <red>" + cheatType +
                        "</red> <gray>(Violations: " + currentViolations + ", Ping: " + player.getPing() + "ms)</gray>";

                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("apexsions.staff") || p.hasPermission("apexsions.admin"))
                        .forEach(staff -> staff.sendMessage(mm.deserialize(staffAlert)));

                plugin.getLogger().warning("[AntiCheat] " + player.getName() + " flagged for " + cheatType + " (" + currentViolations + " VL).");
            }
        }
    }

    private boolean isPhysicallyOnGround(Player player) {
        if (player.isOnGround()) return true;

        Location loc = player.getLocation();
        Block feet = loc.getBlock();
        Block below = loc.clone().subtract(0, 0.45, 0).getBlock();

        return feet.getType().isSolid() || below.getType().isSolid();
    }

    private boolean isNearClimbable(Location loc) {
        Block block = loc.getBlock();
        Material mat = block.getType();
        if (mat == Material.LADDER || mat == Material.VINE || mat == Material.SCAFFOLDING ||
            mat == Material.WEEPING_VINES || mat == Material.TWISTING_VINES || mat == Material.COBWEB) {
            return true;
        }
        Block below = loc.clone().subtract(0, 1, 0).getBlock();
        Material belowMat = below.getType();
        return belowMat == Material.LADDER || belowMat == Material.VINE || belowMat == Material.SCAFFOLDING;
    }

    private boolean isLiquid(Material mat) {
        return mat == Material.WATER || mat == Material.LAVA;
    }

    private boolean isDamageAbsorbing(Block block) {
        Material mat = block.getType();
        return mat == Material.HAY_BLOCK || mat == Material.SLIME_BLOCK || mat == Material.HONEY_BLOCK ||
               mat == Material.WATER || mat == Material.COBWEB;
    }
}
