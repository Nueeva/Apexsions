package com.apexsions.core.pose;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing player poses, emotes, and chair sitting on stairs/slabs.
 */
public class PoseManager {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm;

    private final Map<UUID, ArmorStand> activeSeats = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerPoseType> activePoses = new ConcurrentHashMap<>();
    private final Map<Location, UUID> occupiedChairs = new ConcurrentHashMap<>();

    public PoseManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        this.mm = MiniMessage.miniMessage();
    }

    /**
     * Sits a player on the ground at their current location or on a chair.
     */
    public boolean sit(@NotNull Player player, @Nullable Location targetLoc, boolean isChair) {
        if (!canUsePose(player, true)) {
            return false;
        }

        Location loc = targetLoc != null ? targetLoc.clone() : player.getLocation().clone();
        if (!isChair) {
            loc.setY(loc.getY() - 1.7);
        }

        // If player already has a pose or seat, stand up first
        standUp(player, false);

        ArmorStand seat = loc.getWorld().spawn(loc, ArmorStand.class, as -> {
            as.setVisible(false);
            as.setMarker(true);
            as.setSmall(true);
            as.setGravity(false);
            as.setInvulnerable(true);
            as.setPersistent(false);
            as.addScoreboardTag("apexsions:seat");
        });

        seat.addPassenger(player);
        activeSeats.put(player.getUniqueId(), seat);
        activePoses.put(player.getUniqueId(), PlayerPoseType.SITTING);

        if (isChair && targetLoc != null) {
            occupiedChairs.put(targetLoc.getBlock().getLocation(), player.getUniqueId());
        }

        sendActionBar(player, "<gray>Anda sedang duduk. Tekan <gold><bold>SHIFT</bold></gold> untuk berdiri.</gray>");
        return true;
    }

    /**
     * Sits a player on a clicked chair block (Stairs or Slab).
     */
    public boolean sitOnChair(@NotNull Player player, @NotNull Block block) {
        if (!isChairsEnabled()) {
            return false;
        }

        if (!canUsePose(player, true)) {
            return false;
        }

        Location blockLoc = block.getLocation();
        if (occupiedChairs.containsKey(blockLoc)) {
            Player occupant = plugin.getServer().getPlayer(occupiedChairs.get(blockLoc));
            if (occupant != null && occupant.isOnline() && activeSeats.containsKey(occupant.getUniqueId())) {
                player.sendMessage(mm.deserialize("<red>❌ Kursi ini sedang diduduki oleh <yellow>" + occupant.getName() + "</yellow>!</red>"));
                return false;
            } else {
                occupiedChairs.remove(blockLoc);
            }
        }

        // Validate overhead block clearance (must be passable)
        Block above = block.getRelative(BlockFace.UP);
        if (above.getType().isSolid()) {
            player.sendMessage(mm.deserialize("<red>❌ Tidak bisa duduk di sini karena ruang atas terhalang!</red>"));
            return false;
        }

        Location seatLoc = blockLoc.clone().add(0.5, 0.25, 0.5);

        if (block.getBlockData() instanceof Stairs stairs) {
            if (stairs.getHalf() != Bisected.Half.BOTTOM) {
                return false; // Upside-down stairs cannot be sat on
            }
            BlockFace facing = stairs.getFacing().getOppositeFace();
            seatLoc.setYaw(faceToYaw(facing));
            seatLoc.setY(blockLoc.getY() - 0.7);
        } else if (block.getBlockData() instanceof Slab slab) {
            if (slab.getType() != Slab.Type.BOTTOM) {
                return false; // Only bottom half slabs
            }
            seatLoc.setYaw(player.getLocation().getYaw());
            seatLoc.setY(blockLoc.getY() - 0.7);
        } else {
            return false;
        }

        return sit(player, seatLoc, true);
    }

    /**
     * Puts a player into a laying / sleeping pose.
     */
    public boolean lay(@NotNull Player player) {
        if (!canUsePose(player, true)) {
            return false;
        }

        standUp(player, false);

        player.setPose(Pose.SLEEPING, true);
        activePoses.put(player.getUniqueId(), PlayerPoseType.LAYING);
        sendActionBar(player, "<gray>Anda sedang tiduran. Tekan <gold><bold>SHIFT</bold></gold> untuk bangun.</gray>");
        return true;
    }

    /**
     * Puts a player into a crawling pose (1-block height hitbox).
     */
    public boolean crawl(@NotNull Player player) {
        if (!canUsePose(player, false)) {
            return false;
        }

        if (activePoses.get(player.getUniqueId()) == PlayerPoseType.CRAWLING) {
            standUp(player, true);
            return true;
        }

        standUp(player, false);

        player.setPose(Pose.SWIMMING, true);
        activePoses.put(player.getUniqueId(), PlayerPoseType.CRAWLING);
        sendActionBar(player, "<gray>Anda sedang merangkak. Tekan <gold><bold>SHIFT</bold></gold> untuk berdiri.</gray>");
        return true;
    }

    /**
     * Puts a player into a belly-flop pose.
     */
    public boolean bellyflop(@NotNull Player player) {
        if (!canUsePose(player, true)) {
            return false;
        }

        standUp(player, false);

        player.setPose(Pose.FALL_FLYING, true);
        activePoses.put(player.getUniqueId(), PlayerPoseType.BELLYFLOP);
        sendActionBar(player, "<gray>Anda sedang tengkurap. Tekan <gold><bold>SHIFT</bold></gold> untuk berdiri.</gray>");
        return true;
    }

    /**
     * Puts a player into a spinning riptide pose.
     */
    public boolean spin(@NotNull Player player) {
        if (!canUsePose(player, true)) {
            return false;
        }

        standUp(player, false);

        player.setPose(Pose.SPIN_ATTACK, true);
        activePoses.put(player.getUniqueId(), PlayerPoseType.SPINNING);
        sendActionBar(player, "<gray>Anda sedang berputar! Tekan <gold><bold>SHIFT</bold></gold> untuk berhenti.</gray>");
        return true;
    }

    /**
     * Stands up from any current pose or chair, cleaning up entities safely.
     */
    public void standUp(@NotNull Player player, boolean safeElevate) {
        UUID uuid = player.getUniqueId();
        PlayerPoseType current = activePoses.remove(uuid);

        ArmorStand seat = activeSeats.remove(uuid);
        if (seat != null) {
            Location seatLoc = seat.getLocation();
            occupiedChairs.values().removeIf(id -> id.equals(uuid));
            if (seat.isValid()) {
                seat.removePassenger(player);
                seat.remove();
            }
            if (safeElevate && player.isOnline()) {
                Location safe = player.getLocation().clone().add(0, 0.6, 0);
                player.teleport(safe);
            }
        }

        if (player.isOnline()) {
            if (player.isSleeping()) {
                player.wakeup(false);
            }
            player.setPose(Pose.STANDING, false);
            if (current != null && current != PlayerPoseType.NONE) {
                sendActionBar(player, "<gray>Anda kembali berdiri.</gray>");
            }
        }
    }

    /**
     * Checks if a player can currently perform a pose/sit action.
     */
    public boolean canUsePose(@NotNull Player player, boolean checkGround) {
        if (plugin.getCombatTagService() != null && plugin.getCombatTagService().isCombatTagged(player.getUniqueId())) {
            player.sendMessage(mm.deserialize("<red>❌ Anda tidak dapat mengubah pose saat dalam pertempuran (Combat Tagged)!</red>"));
            return false;
        }

        if (player.isInsideVehicle() && !activeSeats.containsKey(player.getUniqueId())) {
            player.sendMessage(mm.deserialize("<red>❌ Anda sedang berada di dalam kendaraan lain!</red>"));
            return false;
        }

        if (player.isDead()) {
            return false;
        }

        if (checkGround && player.getLocation().getBlock().getType() == Material.WATER) {
            player.sendMessage(mm.deserialize("<red>❌ Tidak dapat melakukan pose ini di dalam air.</red>"));
            return false;
        }

        return true;
    }

    public boolean isSitting(@NotNull Player player) {
        return activePoses.get(player.getUniqueId()) == PlayerPoseType.SITTING;
    }

    public boolean hasActivePose(@NotNull Player player) {
        PlayerPoseType type = activePoses.get(player.getUniqueId());
        return type != null && type != PlayerPoseType.NONE;
    }

    @NotNull
    public PlayerPoseType getActivePose(@NotNull Player player) {
        return activePoses.getOrDefault(player.getUniqueId(), PlayerPoseType.NONE);
    }

    public boolean isSeatEntity(@NotNull org.bukkit.entity.Entity entity) {
        return entity instanceof ArmorStand && entity.getScoreboardTags().contains("apexsions:seat");
    }

    /**
     * Cleanly removes all active seats and resets all poses on server stop/reload.
     */
    public void cleanupAll() {
        for (Map.Entry<UUID, ArmorStand> entry : activeSeats.entrySet()) {
            ArmorStand seat = entry.getValue();
            if (seat != null && seat.isValid()) {
                seat.eject();
                seat.remove();
            }
            Player p = plugin.getServer().getPlayer(entry.getKey());
            if (p != null && p.isOnline()) {
                p.setPose(Pose.STANDING, false);
            }
        }
        activeSeats.clear();
        occupiedChairs.clear();

        for (UUID uuid : activePoses.keySet()) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null && p.isOnline()) {
                if (p.isSleeping()) {
                    p.wakeup(false);
                }
                p.setPose(Pose.STANDING, false);
            }
        }
        activePoses.clear();
    }

    private void sendActionBar(Player player, String message) {
        if (plugin.getConfig().getBoolean("pose.actionbar-notification", true)) {
            player.sendActionBar(mm.deserialize(message));
        }
    }

    public boolean isChairsEnabled() {
        return plugin.getConfig().getBoolean("pose.chairs.enabled", true);
    }

    public double getChairDistanceLimit() {
        return plugin.getConfig().getDouble("pose.chairs.distance-limit", 3.5);
    }

    private float faceToYaw(BlockFace face) {
        return switch (face) {
            case NORTH -> 180f;
            case EAST -> 270f;
            case SOUTH -> 0f;
            case WEST -> 90f;
            default -> 0f;
        };
    }
}
