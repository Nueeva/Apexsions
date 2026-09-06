package com.apexsions.crates.hologram;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.crate.Crate;
import com.apexsions.crates.crate.CrateLocation;
import com.apexsions.crates.key.CrateKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CrateHologramManager {

    private final ApexsionsCratesPlugin plugin;
    private final NamespacedKey holoKey;
    private final Map<CrateLocation, List<UUID>> activeHolograms = new ConcurrentHashMap<>();

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
        for (Map.Entry<CrateLocation, List<UUID>> entry : activeHolograms.entrySet()) {
            Location loc = entry.getKey().toBukkitLocation();
            if (loc != null && loc.getWorld() != null) {
                for (UUID id : entry.getValue()) {
                    Entity entity = loc.getWorld().getEntity(id);
                    if (entity != null && entity.isValid()) {
                        entity.remove();
                    }
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

        List<UUID> spawnedIds = new ArrayList<>();
        double textOffset = plugin.getConfig().getDouble("settings.hologram-height-offset", 1.45);
        Location textLoc = loc.clone().add(0.5, textOffset, 0.5);

        // 1. TextDisplay for Hologram Lines
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
            TextDisplay textDisplay = loc.getWorld().spawn(textLoc, TextDisplay.class, entity -> {
                entity.text(finalComponent);
                entity.setBillboard(Display.Billboard.CENTER);
                entity.setDefaultBackground(false);
                entity.setBackgroundColor(Color.fromARGB(90, 0, 0, 0));
                entity.setShadowed(true);
                entity.getPersistentDataContainer().set(holoKey, PersistentDataType.BYTE, (byte) 1);
            });
            spawnedIds.add(textDisplay.getUniqueId());

            // 2. Floating 3D ItemDisplay above crate block
            if (plugin.getConfig().getBoolean("settings.hologram-item-enabled", true)) {
                Location itemLoc = loc.clone().add(0.5, 0.65, 0.5);
                CrateKey key = plugin.getKeyManager().getKey(crate.getRequiredKeyId());
                ItemStack displayStack = (key != null) ? key.createItem(plugin, 1) : new ItemStack(crate.getBlockMaterial());

                ItemDisplay itemDisplay = loc.getWorld().spawn(itemLoc, ItemDisplay.class, entity -> {
                    entity.setItemStack(displayStack);
                    entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
                    entity.setBillboard(Display.Billboard.CENTER);
                    entity.getPersistentDataContainer().set(holoKey, PersistentDataType.BYTE, (byte) 1);
                });
                spawnedIds.add(itemDisplay.getUniqueId());
            }

            activeHolograms.put(cl, spawnedIds);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn hologram at " + cl.getWorld() + "," + cl.getX() + ": " + e.getMessage());
        }
    }

    public void removeHologram(CrateLocation cl) {
        List<UUID> ids = activeHolograms.remove(cl);
        if (ids == null) return;

        Location loc = cl.toBukkitLocation();
        if (loc != null && loc.getWorld() != null) {
            for (UUID id : ids) {
                Entity entity = loc.getWorld().getEntity(id);
                if (entity != null && entity.isValid()) {
                    entity.remove();
                }
            }
        }
    }

    public void cleanupOrphanHologramsInWorld(World world) {
        if (world == null) return;
        for (Entity entity : world.getEntitiesByClasses(TextDisplay.class, ItemDisplay.class)) {
            if (entity.getPersistentDataContainer().has(holoKey, PersistentDataType.BYTE)) {
                entity.remove();
            }
        }
    }
}
