package com.apexsions.fishing.integration;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * WorldEdit integration hook for ApexsionsFishing.
 * Safely accesses WorldEdit selection sessions to define cuboid fishing zones.
 */
public class WorldEditHook {

    public record SelectionBounds(String worldName, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public int getVolume() {
            return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        }
    }

    /**
     * Checks if WorldEdit is active and loaded on the server.
     */
    public static boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("WorldEdit");
    }

    /**
     * Retrieves the current WorldEdit cuboid selection of a player.
     *
     * @param player Player to inspect
     * @return SelectionBounds bounding coordinates or null if WorldEdit is unavailable/empty
     * @throws IncompleteRegionException If the player has not finished defining pos1 and pos2
     */
    @Nullable
    public static SelectionBounds getPlayerSelection(Player player) throws IncompleteRegionException {
        if (!isAvailable() || player == null) {
            return null;
        }

        try {
            var actor = BukkitAdapter.adapt(player);
            var session = WorldEdit.getInstance().getSessionManager().get(actor);
            if (session == null) {
                return null;
            }

            var world = BukkitAdapter.adapt(player.getWorld());
            Region region = session.getSelection(world);
            if (region == null) {
                return null;
            }

            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();

            int minX = Math.min(min.getBlockX(), max.getBlockX());
            int minY = Math.min(min.getBlockY(), max.getBlockY());
            int minZ = Math.min(min.getBlockZ(), max.getBlockZ());
            int maxX = Math.max(min.getBlockX(), max.getBlockX());
            int maxY = Math.max(min.getBlockY(), max.getBlockY());
            int maxZ = Math.max(min.getBlockZ(), max.getBlockZ());

            return new SelectionBounds(player.getWorld().getName(), minX, minY, minZ, maxX, maxY, maxZ);
        } catch (IncompleteRegionException e) {
            throw e;
        } catch (Throwable t) {
            Bukkit.getLogger().warning("[ApexsionsFishing] Gagal membaca seleksi WorldEdit: " + t.getMessage());
            return null;
        }
    }
}
