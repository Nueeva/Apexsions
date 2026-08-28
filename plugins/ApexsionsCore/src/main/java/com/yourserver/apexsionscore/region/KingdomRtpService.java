package com.yourserver.apexsionscore.region;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import com.yourserver.apexsionscore.player.PlayerData;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles safe random teleportation (RTP) strictly bounded within the player's kingdom territory.
 * Fully asynchronous with Paper chunk loading, pass-through safety verification,
 * and war/combat-state teleportation locks.
 */
public class KingdomRtpService {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    private static final Set<Material> UNSAFE_BLOCKS = EnumSet.of(
            Material.LAVA,
            Material.WATER,
            Material.FIRE,
            Material.SOUL_FIRE,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.CACTUS,
            Material.SWEET_BERRY_BUSH,
            Material.WITHER_ROSE,
            Material.MAGMA_BLOCK,
            Material.POWDER_SNOW,
            Material.VOID_AIR,
            Material.CAVE_AIR,
            Material.POINTED_DRIPSTONE
    );

    public KingdomRtpService(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes /rtp for the given player within their chosen kingdom bounds.
     */
    public void executeRtp(Player player) {
        if (!player.hasPermission("apexsionscore.command.rtp") && !player.hasPermission("kingdomcore.command.rtp")) {
            player.sendMessage(miniMessage.deserialize("<red>Kamu tidak memiliki izin untuk menggunakan perintah /rtp.</red>"));
            return;
        }

        // 1. Check Combat Tag
        if (plugin.getCombatTagService() != null && plugin.getCombatTagService().isCombatTagged(player.getUniqueId())) {
            long remaining = plugin.getCombatTagService().getRemainingSeconds(player.getUniqueId());
            player.sendMessage(miniMessage.deserialize("<red>⚔ Kamu sedang dalam mode tempur (Combat Tag: <yellow>" + remaining + "s</yellow>)! RTP dinonaktifkan.</red>"));
            return;
        }

        // 2. Check player kingdom
        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
        if (dataOpt.isEmpty() || !dataOpt.get().hasRegion()) {
            player.sendMessage(miniMessage.deserialize("<yellow>⚔ Kamu belum memilih kerajaan! Pilih kerajaan terlebih dahulu untuk menggunakan RTP.</yellow>"));
            plugin.getRegionSelectionGUI().open(player);
            return;
        }

        UUID regionId = dataOpt.get().getRegionId();
        Optional<Region> regionOpt = plugin.getRegionManager().getRegion(regionId);
        if (regionOpt.isEmpty()) {
            player.sendMessage(miniMessage.deserialize("<red>Data kerajaan kamu tidak ditemukan.</red>"));
            return;
        }

        Region region = regionOpt.get();

        // 3. Strict Check: Player MUST currently be physically inside their own kingdom territory
        if (!region.containsLocation(player.getLocation())) {
            player.sendMessage(miniMessage.deserialize("<red>✖ Kamu hanya dapat menggunakan <yellow>/rtp</yellow> ketika sedang berada di dalam wilayah teritorial kerajaanmu (<gold>" + region.getDisplayName() + "</gold>)!</red>"));
            return;
        }

        // 4. Check War State in Territory
        if (plugin.getWarManager() != null && plugin.getWarManager().isWarActiveInTerritory(region)) {
            player.sendMessage(miniMessage.deserialize("<dark_red>⚔ Wilayah kerajaan sedang dalam keadaan PERANG (WAR)! Seluruh fitur teleportasi dinonaktifkan sementara.</dark_red>"));
            return;
        }

        // 5. Check Cooldown
        long now = System.currentTimeMillis();
        long cooldownSeconds = plugin.getConfigManager().getMainConfig().getLong("rtp.cooldown-seconds", 60L);
        if (!player.hasPermission("apexsionscore.rtp.bypass")) {
            Long expireTime = cooldowns.get(player.getUniqueId());
            if (expireTime != null && expireTime > now) {
                long remaining = (expireTime - now + 999) / 1000;
                player.sendMessage(miniMessage.deserialize("<red>Tunggu <yellow>" + remaining + " detik</yellow> lagi sebelum dapat menggunakan /rtp kembali.</red>"));
                return;
            }
        }

        // 6. Start Search
        player.sendMessage(miniMessage.deserialize("<gold>🔍 Mencari lokasi acak yang aman di wilayah kerajaan <yellow>" + region.getDisplayName() + "</yellow>...</gold>"));
        findAndTeleport(player, region, 0, 30, cooldownSeconds);
    }

    private void findAndTeleport(Player player, Region region, int attempt, int maxAttempts, long cooldownSeconds) {
        if (!player.isOnline()) return;

        if (attempt >= maxAttempts) {
            player.sendMessage(miniMessage.deserialize("<red>Gagal menemukan lokasi aman di wilayah " + region.getDisplayName() + " setelah beberapa percobaan. Silakan coba sesaat lagi.</red>"));
            return;
        }

        World world = Bukkit.getWorld(region.getWorldName());
        if (world == null) {
            world = player.getWorld();
        }

        TerritoryPolygon polygon = region.getPolygon();
        double targetX;
        double targetZ;

        if (polygon != null && !polygon.getVertices().isEmpty()) {
            double margin = 16.0; // Stay at least 16 blocks away from outer borders
            double minX = polygon.getMinX() + margin;
            double maxX = polygon.getMaxX() - margin;
            double minZ = polygon.getMinZ() + margin;
            double maxZ = polygon.getMaxZ() - margin;

            if (minX >= maxX || minZ >= maxZ) {
                minX = polygon.getMinX();
                maxX = polygon.getMaxX();
                minZ = polygon.getMinZ();
                maxZ = polygon.getMaxZ();
            }

            // Pick candidate inside bounding box
            targetX = ThreadLocalRandom.current().nextDouble(minX, maxX);
            targetZ = ThreadLocalRandom.current().nextDouble(minZ, maxZ);

            double testY = (polygon.getMinY() + polygon.getMaxY()) / 2.0;
            if (!polygon.contains(targetX, testY, targetZ)) {
                // Point is outside polygon shape, retry next iteration
                Bukkit.getScheduler().runTask(plugin, () -> findAndTeleport(player, region, attempt + 1, maxAttempts, cooldownSeconds));
                return;
            }
        } else {
            // Fallback radius around spawn if polygon is missing
            double spawnX = region.getSpawnX() != null ? region.getSpawnX() : 0.0;
            double spawnZ = region.getSpawnZ() != null ? region.getSpawnZ() : 0.0;
            double radius = 1000.0;
            double angle = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);
            double dist = ThreadLocalRandom.current().nextDouble(100, radius);
            targetX = spawnX + (dist * Math.cos(angle));
            targetZ = spawnZ + (dist * Math.sin(angle));
        }

        final int blockX = (int) Math.floor(targetX);
        final int blockZ = (int) Math.floor(targetZ);
        final World finalWorld = world;

        // Paper Asynchronous Chunk Loading
        finalWorld.getChunkAtAsync(blockX >> 4, blockZ >> 4).thenAccept(chunk -> {
            if (!player.isOnline()) return;

            Bukkit.getScheduler().runTask(plugin, () -> {
                int highestY = finalWorld.getHighestBlockYAt(blockX, blockZ, HeightMap.MOTION_BLOCKING_NO_LEAVES);

                // Ensure Y is within world and safe boundaries
                if (highestY < finalWorld.getMinHeight() + 5 || highestY > finalWorld.getMaxHeight() - 5) {
                    findAndTeleport(player, region, attempt + 1, maxAttempts, cooldownSeconds);
                    return;
                }

                Block ground = finalWorld.getBlockAt(blockX, highestY, blockZ);
                Block feet = finalWorld.getBlockAt(blockX, highestY + 1, blockZ);
                Block head = finalWorld.getBlockAt(blockX, highestY + 2, blockZ);

                // Safety Validation
                if (!isSafeGround(ground) || !isSafePassThrough(feet) || !isSafePassThrough(head)) {
                    findAndTeleport(player, region, attempt + 1, maxAttempts, cooldownSeconds);
                    return;
                }

                Location targetLoc = new Location(finalWorld, blockX + 0.5, highestY + 1.0, blockZ + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch());

                // Strict polygon verification
                if (polygon != null && !polygon.contains(targetLoc.getX(), targetLoc.getY(), targetLoc.getZ())) {
                    findAndTeleport(player, region, attempt + 1, maxAttempts, cooldownSeconds);
                    return;
                }

                // Teleport Player Asynchronously
                player.teleportAsync(targetLoc).thenAccept(success -> {
                    if (success) {
                        // Set cooldown
                        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (cooldownSeconds * 1000L));

                        // Play effects
                        player.playSound(targetLoc, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f);
                        player.playSound(targetLoc, Sound.ENTITY_PLAYER_TELEPORT, 0.8f, 1.2f);
                        finalWorld.spawnParticle(Particle.PORTAL, targetLoc.clone().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.1);

                        // Send Messages & Titles
                        player.sendMessage(miniMessage.deserialize("<green>✨ Berhasil teleportasi ke wilayah <yellow>" + region.getDisplayName() + "</yellow> di koordinat <aqua>[" + blockX + ", " + (highestY + 1) + ", " + blockZ + "]</aqua>!</green>"));

                        Title title = Title.title(
                                miniMessage.deserialize("<gold><bold>" + region.getDisplayName() + "</bold></gold>"),
                                miniMessage.deserialize("<gray>Wilayah Acak Kerajaan</gray>"),
                                Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(1500), Duration.ofMillis(400))
                        );
                        player.showTitle(title);
                    } else {
                        player.sendMessage(miniMessage.deserialize("<red>Gagal melakukan teleportasi. Silakan coba lagi.</red>"));
                    }
                });
            });
        });
    }

    private boolean isSafeGround(Block block) {
        if (block == null || block.isEmpty()) return false;
        Material mat = block.getType();
        if (UNSAFE_BLOCKS.contains(mat)) return false;
        if (mat.name().contains("LEAVES") || mat.name().contains("WATER") || mat.name().contains("LAVA")) return false;
        return block.getType().isSolid();
    }

    private boolean isSafePassThrough(Block block) {
        if (block == null || block.isEmpty()) return true;
        Material mat = block.getType();
        if (UNSAFE_BLOCKS.contains(mat)) return false;
        return block.isPassable();
    }

    public void clearCooldown(UUID uuid) {
        cooldowns.remove(uuid);
    }
}
