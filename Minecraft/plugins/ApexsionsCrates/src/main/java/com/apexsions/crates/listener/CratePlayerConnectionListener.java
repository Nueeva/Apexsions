package com.apexsions.crates.listener;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.animation.OpeningSession;
import com.apexsions.crates.crate.CrateLocation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class CratePlayerConnectionListener implements Listener {

    private final ApexsionsCratesPlugin plugin;

    public CratePlayerConnectionListener(ApexsionsCratesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        OpeningSession session = plugin.getSession(event.getPlayer().getUniqueId());
        if (session != null && !session.isCompleted()) {
            session.skip();
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (CrateLocation cl : plugin.getCrateManager().getLocations()) {
            if (cl.getWorld().equals(event.getWorld().getName())) {
                int chunkX = cl.getX() >> 4;
                int chunkZ = cl.getZ() >> 4;
                if (chunkX == event.getChunk().getX() && chunkZ == event.getChunk().getZ()) {
                    plugin.getHologramManager().spawnHologram(cl);
                }
            }
        }
    }

    @EventHandler
    public void onChunkUnload(org.bukkit.event.world.ChunkUnloadEvent event) {
        for (CrateLocation cl : plugin.getCrateManager().getLocations()) {
            if (cl.getWorld().equals(event.getWorld().getName())) {
                int chunkX = cl.getX() >> 4;
                int chunkZ = cl.getZ() >> 4;
                if (chunkX == event.getChunk().getX() && chunkZ == event.getChunk().getZ()) {
                    plugin.getHologramManager().removeHologram(cl);
                }
            }
        }
    }
}
