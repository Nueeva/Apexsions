package com.apexsions.media.raytrace;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.banner.MediaBanner;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MediaRaytraceService {

    private final ApexsionsMediaPlugin plugin;
    private final Map<UUID, String> hoveredBannerMap = new ConcurrentHashMap<>();
    private BukkitTask task;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MediaRaytraceService(ApexsionsMediaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        long interval = plugin.getConfig().getLong("raytrace.interval-ticks", 3L);
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, interval, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        hoveredBannerMap.clear();
    }

    private void tick() {
        if (!plugin.getConfig().getBoolean("raytrace.enabled", true)) return;

        double maxDist = plugin.getConfig().getDouble("raytrace.max-distance", 7.0);
        boolean showActionbar = plugin.getConfig().getBoolean("raytrace.actionbar-hint", true);
        String actionbarText = plugin.getConfig().getString("raytrace.actionbar-message",
                "<gradient:#f39c12:#f1c40f><bold>✨ KLIK KANAN</bold></gradient> <gray>untuk membuka link informasi!</gray>");
        boolean spawnParticles = plugin.getConfig().getBoolean("raytrace.particles.enabled", true);

        for (Player player : Bukkit.getOnlinePlayers()) {
            Location eye = player.getEyeLocation();
            Vector dir = eye.getDirection();
            World world = player.getWorld();

            MediaBanner hitBanner = null;

            for (MediaBanner banner : plugin.getBannerManager().getBanners()) {
                if (!banner.getWorldName().equalsIgnoreCase(world.getName())) continue;

                BoundingBox box = banner.getBoundingBox();
                RayTraceResult result = box.rayTrace(eye.toVector(), dir, maxDist);
                if (result != null) {
                    hitBanner = banner;
                    break;
                }
            }

            if (hitBanner != null) {
                hoveredBannerMap.put(player.getUniqueId(), hitBanner.getId());

                // Actionbar tooltip
                if (showActionbar && hitBanner.getLinkUrl() != null && !hitBanner.getLinkUrl().isBlank()) {
                    player.sendActionBar(miniMessage.deserialize(actionbarText));
                }

                // Border glowing particles
                if (spawnParticles) {
                    spawnBorderParticles(player, hitBanner);
                }
            } else {
                hoveredBannerMap.remove(player.getUniqueId());
            }
        }
    }

    private void spawnBorderParticles(Player viewer, MediaBanner banner) {
        World world = banner.getWorld();
        if (world == null) return;

        BoundingBox box = banner.getBoundingBox();
        Particle pType = Particle.GLOW;
        try {
            String pName = plugin.getConfig().getString("raytrace.particles.type", "GLOW");
            pType = Particle.valueOf(pName.toUpperCase());
        } catch (Exception ignored) {}

        // Spawn a few glowing points around the bounding box perimeter
        double minX = box.getMinX();
        double maxX = box.getMaxX();
        double minY = box.getMinY();
        double maxY = box.getMaxY();
        double minZ = box.getMinZ();
        double maxZ = box.getMaxZ();

        double midX = (minX + maxX) / 2.0;
        double midY = (minY + maxY) / 2.0;
        double midZ = (minZ + maxZ) / 2.0;

        viewer.spawnParticle(pType, minX, maxY, minZ, 1, 0.05, 0.05, 0.05, 0.0);
        viewer.spawnParticle(pType, maxX, maxY, maxZ, 1, 0.05, 0.05, 0.05, 0.0);
        viewer.spawnParticle(pType, minX, minY, minZ, 1, 0.05, 0.05, 0.05, 0.0);
        viewer.spawnParticle(pType, maxX, minY, maxZ, 1, 0.05, 0.05, 0.05, 0.0);
        viewer.spawnParticle(pType, midX, midY, midZ, 1, 0.1, 0.1, 0.1, 0.0);
    }

    public MediaBanner getHoveredBanner(Player player) {
        String id = hoveredBannerMap.get(player.getUniqueId());
        return id != null ? plugin.getBannerManager().getBanner(id) : null;
    }
}
