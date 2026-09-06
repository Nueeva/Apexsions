package com.apexsions.crates.hologram;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.crate.Crate;
import com.apexsions.crates.crate.CrateLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CrateHologramManager {

    private final ApexsionsCratesPlugin plugin;
    private final NamespacedKey holoKey;
    private final Map<CrateLocation, UUID> activeHolograms = new ConcurrentHashMap<>();

    public CrateHologramManager(ApexsionsCratesPlugin plugin) {
        this.plugin = plugin;
        this.holoKey = new NamespacedKey(plugin, "crate_hologram");
    }

    public void spawnAllHolograms() {
        if (!plugin.getConfig().getBoolean("settings.holograms-enabled", true)) return;

        for (CrateLocation cl : plugin.getCrateManager().getLocations()) {
            spawnHologram(cl);
        }
    }

    public void removeAllHolograms() {
        for (Map.Entry<CrateLocation, UUID> entry : activeHolograms.entrySet()) {
            Location loc = entry.getKey().toBukkitLocation();
            if (loc != null && loc.getWorld() != null) {
                Entity entity = loc.getWorld().getEntity(entry.getValue());
                if (entity != null && entity.isValid()) {
                    entity.remove();
                }
            }
        }
        activeHolograms.clear();
    }

    public void spawnHologram(CrateLocation cl) {
        if (!plugin.getConfig().getBoolean("settings.holograms-enabled", true)) return;
        if (activeHolograms.containsKey(cl)) {
            removeHologram(cl);
        }

        Location loc = cl.toBukkitLocation();
        if (loc == null || loc.getWorld() == null) return;
        if (!loc.isChunkLoaded()) return;

        Crate crate = plugin.getCrateManager().getCrate(cl.getCrateId());
        if (crate == null || !crate.isHologramEnabled()) return;

        double offset = plugin.getConfig().getDouble("settings.hologram-height-offset", 1.25);
        Location spawnLoc = loc.clone().add(0.5, offset, 0.5);

        // Build composite multiline component
        Component composite = Component.empty();
        List<String> lines = crate.getHologramLines();
        if (lines.isEmpty()) {
            lines = List.of(
                    crate.getName(),
                    "<gray>Klik-Kiri: <yellow>Preview Hadiah</yellow></gray>",
                    "<gray>Klik-Kanan: <green>Buka Peti</green></gray>"
            );
        }

        for (int i = 0; i < lines.size(); i++) {
            composite = composite.append(MiniMessage.miniMessage().deserialize(lines.get(i)));
            if (i < lines.size() - 1) {
                composite = composite.append(Component.newline());
            }
        }

        final Component finalComponent = composite;
        try {
            TextDisplay display = loc.getWorld().spawn(spawnLoc, TextDisplay.class, entity -> {
                entity.text(finalComponent);
                entity.setBillboard(Display.Billboard.CENTER);
                entity.setDefaultBackground(false);
                entity.setBackgroundColor(Color.fromARGB(80, 0, 0, 0));
                entity.setShadowed(true);
                entity.getPersistentDataContainer().set(holoKey, PersistentDataType.BYTE, (byte) 1);
            });

            activeHolograms.put(cl, display.getUniqueId());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn hologram at " + cl.getWorld() + "," + cl.getX() + ": " + e.getMessage());
        }
    }

    public void removeHologram(CrateLocation cl) {
        UUID id = activeHolograms.remove(cl);
        if (id == null) return;

        Location loc = cl.toBukkitLocation();
        if (loc != null && loc.getWorld() != null) {
            Entity entity = loc.getWorld().getEntity(id);
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }
    }

    public void cleanupOrphanHologramsInWorld(World world) {
        if (world == null) return;
        for (Entity entity : world.getEntitiesByClass(TextDisplay.class)) {
            if (entity.getPersistentDataContainer().has(holoKey, PersistentDataType.BYTE)) {
                entity.remove();
            }
        }
    }
}
