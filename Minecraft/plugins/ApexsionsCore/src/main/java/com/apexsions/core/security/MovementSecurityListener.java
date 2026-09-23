package com.apexsions.core.security;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance movement security listener preventing Fly Hack, Speed, Jesus, NoFall, and Step exploits,
 * with complete immunity to false positives from water climbing/swimming, bubble columns, ladders,
 * and Geyser/Floodgate Bedrock client physics.
 */
public class MovementSecurityListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // Player state tracking
    private final Map<UUID, Location> lastSafeGround = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> airborneTicks = new ConcurrentHashMap<>();
    private final Map<UUID, Double> serverFallDistance = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastKnockbackTime = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastVelocityTime = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastLiquidTime = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastClimbableTime = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> violationCount = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastAlertTime = new ConcurrentHashMap<>();

    private static final long ALERT_COOLDOWN_MS = 5000L;
    private static final long LIQUID_EXIT_GRACE_MS = 4000L;
    private static final long CLIMBABLE_EXIT_GRACE_MS = 3000L;
    private static final long VELOCITY_GRACE_MS = 2500L;

    public MovementSecurityListener(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            lastKnockbackTime.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        lastVelocityTime.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        lastSafeGround.put(uuid, event.getTo());
        airborneTicks.put(uuid, 0);
        serverFallDistance.put(uuid, 0.0);
        lastLiquidTime.put(uuid, 0L);
        lastClimbableTime.put(uuid, 0L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastSafeGround.remove(uuid);
        airborneTicks.remove(uuid);
        serverFallDistance.remove(uuid);
        lastKnockbackTime.remove(uuid);
        lastVelocityTime.remove(uuid);
        lastLiquidTime.remove(uuid);
        lastClimbableTime.remove(uuid);
        violationCount.remove(uuid);
        lastAlertTime.remove(uuid);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // 0. Configuration Master Toggles
        if (!plugin.getConfig().getBoolean("security.enabled", true) ||
            !plugin.getConfig().getBoolean("security.movement.enabled", true)) {
            return;
        }

        // 1. Exemptions: Creative, Spectator, Flying permitted, Elytra gliding, Vehicle riding, Trident Riptiding, Staff bypass
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        if (player.getAllowFlight() || player.isGliding() || player.isInsideVehicle() || player.isRiptiding()) {
            return;
        }
        if (player.hasPermission("apexsions.bypass.movement")) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        // If no coordinate displacement (only camera look / pitch / yaw), skip check
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            return;
        }

        UUID uuid = player.getUniqueId();
        double deltaY = to.getY() - from.getY();
        double deltaX = to.getX() - from.getX();
        double deltaZ = to.getZ() - from.getZ();
        double horizontalDistSq = (deltaX * deltaX) + (deltaZ * deltaZ);

        long now = System.currentTimeMillis();
        boolean isBedrock = player.getName().startsWith(".");

        // Comprehensive environment checks
        boolean inLiquid = isPhysicallyInOrNearLiquid(player, from, to);
        boolean nearClimbable = isNearClimbable(player.getLocation()) || isNearClimbable(to);
        boolean hasLevitation = player.hasPotionEffect(PotionEffectType.LEVITATION);
        boolean hasSlowFalling = player.hasPotionEffect(PotionEffectType.SLOW_FALLING);
        boolean hasJumpBoost = player.hasPotionEffect(PotionEffectType.JUMP_BOOST);
        boolean hasDolphinsGrace = player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE);
        boolean hasConduitPower = player.hasPotionEffect(PotionEffectType.CONDUIT_POWER);
        boolean isGrounded = isPhysicallyOnGround(player);

        // ---------------------------------------------------------------------
        // 2. Liquid & Water State Management (Complete immunity for water ascending)
        // ---------------------------------------------------------------------
        if (inLiquid) {
            lastLiquidTime.put(uuid, now);
            airborneTicks.put(uuid, 0);
            serverFallDistance.put(uuid, 0.0);
            lastSafeGround.put(uuid, to.clone());
            return; // In water/lava/bubble-column: player is 100% legitimately moving in a fluid medium
        }

        if (nearClimbable) {
            lastClimbableTime.put(uuid, now);
            airborneTicks.put(uuid, 0);
            serverFallDistance.put(uuid, 0.0);
            lastSafeGround.put(uuid, to.clone());
            return; // On ladders/vines/scaffolding: vertical climbing is completely legitimate
        }

        // Grace period checks (exiting liquid, exiting climbables, recent knockback, or velocity boost)
        boolean recentLiquid = (now - lastLiquidTime.getOrDefault(uuid, 0L)) < LIQUID_EXIT_GRACE_MS;
        boolean recentClimbable = (now - lastClimbableTime.getOrDefault(uuid, 0L)) < CLIMBABLE_EXIT_GRACE_MS;
        boolean recentKnockback = (now - lastKnockbackTime.getOrDefault(uuid, 0L)) < 2000L;
        boolean recentVelocity = (now - lastVelocityTime.getOrDefault(uuid, 0L)) < VELOCITY_GRACE_MS;

        if (recentLiquid || recentClimbable) {
            // Player just surfaced or hopped out of water/ladder (dolphin leap, waterfall breach, bubble elevator)
            airborneTicks.put(uuid, 0);
            serverFallDistance.put(uuid, 0.0);
            lastSafeGround.put(uuid, to.clone());
            return;
        }

        // ---------------------------------------------------------------------
        // 3. Ground State & True Server-Side NoFall
        // ---------------------------------------------------------------------
        if (isGrounded) {
            lastSafeGround.put(uuid, to.clone());
            airborneTicks.put(uuid, 0);

            // Server-Side NoFall verification:
            boolean nofallEnabled = plugin.getConfig().getBoolean("security.movement.nofall-verification", true);
            if (nofallEnabled) {
                Double trackedFall = serverFallDistance.getOrDefault(uuid, 0.0);
                if (trackedFall > 3.6 && !hasSlowFalling && !recentLiquid && !isDamageAbsorbing(to.getBlock())) {
                    double expectedDamage = Math.max(1.0, trackedFall - 3.0);
                    if (player.getFallDistance() < 0.5f) {
                        player.damage(expectedDamage);
                    }
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

            // ---------------------------------------------------------------------
            // 4. Fly Hack & AirWalk / Hovering Check
            // ---------------------------------------------------------------------
            boolean flyHackEnabled = plugin.getConfig().getBoolean("security.movement.fly-hack-detection", true);
            if (flyHackEnabled) {
                // Bedrock Geyser translation requires higher tolerance due to batching & jitter
                int requiredAirTicks = isBedrock ? 35 : 20;

                if (airTicks > requiredAirTicks && !hasLevitation && !hasSlowFalling &&
                    !hasJumpBoost && !recentKnockback && !recentVelocity) {

                    // Flag if ascending persistently in open air or hovering stationary without gravity
                    if (deltaY > 0.08 || (deltaY >= -0.01 && airTicks > (requiredAirTicks + 15))) {
                        handleMovementViolation(player, "Fly Hack / AirWalk", event);
                        return;
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 5. Jesus / WaterWalk Check
        // ---------------------------------------------------------------------
        boolean jesusEnabled = plugin.getConfig().getBoolean("security.movement.jesus-waterwalk", true);
        if (jesusEnabled && !isGrounded && player.isOnGround()) {
            Block blockBelow = to.clone().subtract(0, 0.5, 0).getBlock();
            if (isLiquidMaterial(blockBelow.getType()) && !player.isSwimming() && !isNearbySolid(to, 1.2)) {
                // Client claims onGround while standing on open deep water without solid blocks
                handleMovementViolation(player, "Jesus / WaterWalk", event);
                return;
            }
        }

        // ---------------------------------------------------------------------
        // 6. Horizontal Speed Hack Check
        // ---------------------------------------------------------------------
        boolean speedEnabled = plugin.getConfig().getBoolean("security.movement.speed-detection", true);
        if (speedEnabled) {
            double maxSpeedSq = isBedrock ? 1.20 : 0.75; // Bedrock Geyser translation tolerance
            if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                maxSpeedSq += 0.50;
            }
            if (hasDolphinsGrace || hasConduitPower) {
                maxSpeedSq += 0.60;
            }

            if (horizontalDistSq > maxSpeedSq && !recentKnockback && !recentVelocity && !player.isGliding() && !recentLiquid) {
                handleMovementViolation(player, "Speed Hack", event);
            }
        }
    }

    private void handleMovementViolation(Player player, String cheatType, PlayerMoveEvent event) {
        UUID uuid = player.getUniqueId();
        int currentViolations = violationCount.getOrDefault(uuid, 0) + 1;
        violationCount.put(uuid, currentViolations);

        // Reset server fall distance to prevent lethal damage upon correction
        serverFallDistance.put(uuid, 0.0);
        player.setFallDistance(0f);

        // Safe rubberband: prefer lastSafeGround, fallback to event.getFrom()
        Location safeLoc = lastSafeGround.get(uuid);
        if (safeLoc != null && safeLoc.getWorld().equals(player.getWorld()) &&
            safeLoc.distanceSquared(event.getFrom()) < 225.0) { // within 15 blocks
            event.setTo(safeLoc.clone());
        } else {
            event.setTo(event.getFrom());
        }

        // Actionbar warning
        player.sendActionBar(mm.deserialize("<red>⚠ Gerakan tidak wajar (" + cheatType + ") terdeteksi! Posisi disesuaikan kembali.</red>"));

        // Alert online staff if violations accumulate
        int maxViolations = plugin.getConfig().getInt("security.movement.max-violations-before-staff-alert", 8);
        if (currentViolations >= maxViolations) {
            long now = System.currentTimeMillis();
            Long lastAlert = lastAlertTime.get(uuid);
            if (lastAlert == null || (now - lastAlert) > ALERT_COOLDOWN_MS) {
                lastAlertTime.put(uuid, now);

                String staffAlert = "<gold>[<red>Apexsions AntiCheat</red>]</gold> <yellow>" + player.getName() +
                        "</yellow> <gray>terdeteksi</gray> <red>" + cheatType +
                        "</red> <gray>(VL: " + currentViolations + ", Ping: " + player.getPing() + "ms)</gray>";

                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("apexsions.staff") || p.hasPermission("apexsions.admin"))
                        .forEach(staff -> staff.sendMessage(mm.deserialize(staffAlert)));

                plugin.getLogger().warning("[AntiCheat] " + player.getName() + " flagged for " + cheatType + " (" + currentViolations + " VL).");
            }
        }
    }

    /**
     * Checks if the player is submerged in or contacting any liquid, bubble column, or waterlogged block.
     * Includes exhaustive 3x3 horizontal and vertical neighborhood scanning.
     */
    private boolean isPhysicallyInOrNearLiquid(Player player, Location from, Location to) {
        if (player.isInWater() || player.isInLava() || player.isInBubbleColumn() || player.isSwimming()) {
            return true;
        }

        // Fast neighborhood checks on 'from' and 'to'
        return isNearLiquidBlock(from) || isNearLiquidBlock(to);
    }

    private boolean isNearLiquidBlock(Location loc) {
        if (loc.getWorld() == null) return false;
        Block center = loc.getBlock();
        if (isLiquidMaterial(center.getType()) || isWaterloggedBlock(center)) {
            return true;
        }
        int bx = loc.getBlockX();
        int by = loc.getBlockY();
        int bz = loc.getBlockZ();
        org.bukkit.World w = loc.getWorld();

        // Check horizontal 3x3 radius and vertical range -1 (beneath feet) to +2 (above head)
        for (int y = -1; y <= 2; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block b = w.getBlockAt(bx + x, by + y, bz + z);
                    if (isLiquidMaterial(b.getType()) || isWaterloggedBlock(b)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isLiquidMaterial(Material mat) {
        return mat == Material.WATER || mat == Material.LAVA || mat == Material.BUBBLE_COLUMN ||
               mat == Material.KELP || mat == Material.KELP_PLANT ||
               mat == Material.SEAGRASS || mat == Material.TALL_SEAGRASS;
    }

    private boolean isWaterloggedBlock(Block block) {
        return block.getBlockData() instanceof Waterlogged wl && wl.isWaterlogged();
    }

    private boolean isPhysicallyOnGround(Player player) {
        if (player.isOnGround()) return true;

        Location loc = player.getLocation();
        Block feet = loc.getBlock();
        Block below = loc.clone().subtract(0, 0.45, 0).getBlock();

        if (feet.getType().isSolid() || below.getType().isSolid()) return true;

        // Check 4 horizontal corners around feet
        double offset = 0.3;
        return loc.clone().add(offset, -0.2, 0).getBlock().getType().isSolid() ||
               loc.clone().add(-offset, -0.2, 0).getBlock().getType().isSolid() ||
               loc.clone().add(0, -0.2, offset).getBlock().getType().isSolid() ||
               loc.clone().add(0, -0.2, -offset).getBlock().getType().isSolid();
    }

    private boolean isNearClimbable(Location loc) {
        Block feet = loc.getBlock();
        Block below = loc.clone().subtract(0, 0.8, 0).getBlock();
        Block head = loc.clone().add(0, 1.2, 0).getBlock();

        return isClimbableMaterial(feet.getType()) ||
               isClimbableMaterial(below.getType()) ||
               isClimbableMaterial(head.getType());
    }

    private boolean isClimbableMaterial(Material mat) {
        return mat == Material.LADDER || mat == Material.VINE || mat == Material.SCAFFOLDING ||
               mat == Material.WEEPING_VINES || mat == Material.WEEPING_VINES_PLANT ||
               mat == Material.TWISTING_VINES || mat == Material.TWISTING_VINES_PLANT ||
               mat == Material.COBWEB || mat == Material.CHAIN;
    }

    private boolean isNearbySolid(Location loc, double radius) {
        int r = (int) Math.ceil(radius);
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    if (loc.clone().add(x, y, z).getBlock().getType().isSolid()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isDamageAbsorbing(Block block) {
        Material mat = block.getType();
        return mat == Material.HAY_BLOCK || mat == Material.SLIME_BLOCK || mat == Material.HONEY_BLOCK ||
               mat == Material.WATER || mat == Material.COBWEB;
    }
}
