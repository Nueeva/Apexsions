package com.apexsions.crates.hologram.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.CratesPlugin;
import com.apexsions.crates.hologram.HologramManager;
import su.nightexpress.nightcore.manager.AbstractListener;

public class HologramListener extends AbstractListener<CratesPlugin> {

    private final HologramManager manager;

    public HologramListener(@NotNull CratesPlugin plugin, @NotNull HologramManager manager) {
        super(plugin);
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        this.manager.removeForViewer(event.getPlayer());
    }
}
