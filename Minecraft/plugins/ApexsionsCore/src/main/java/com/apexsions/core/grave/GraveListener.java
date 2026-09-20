package com.apexsions.core.grave;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Listens for deaths and grave interaction. Grave item storage is additional to
 * the existing death-coordinate tracking and never cancels vanilla events.
 */
public class GraveListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final GraveManager graveManager;

    public GraveListener(@NotNull ApexsionsCorePlugin plugin, @NotNull GraveManager graveManager) {
        this.plugin = plugin;
        this.graveManager = graveManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        if (!graveManager.isEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        List<ItemStack> drops = new ArrayList<>(event.getDrops());
        int droppedExp = event.getDroppedExp();

        if (drops.isEmpty() && droppedExp <= 0) {
            return;
        }

        // Store items virtually so lava, void and despawn cannot destroy them.
        event.getDrops().clear();
        event.setDroppedExp(0);

        String cause = resolveCause(event);
        graveManager.createGrave(player, player.getLocation(), drops, droppedExp, cause);
    }

    private String resolveCause(@NotNull PlayerDeathEvent event) {
        try {
            if (event.deathMessage() != null) {
                return PlainTextComponentSerializer.plainText().serialize(event.deathMessage());
            }
        } catch (Throwable ignored) {
            // fall through to damage cause
        }
        if (event.getEntity().getLastDamageCause() != null) {
            return event.getEntity().getLastDamageCause().getCause().name().replace('_', ' ');
        }
        return "Kematian Tragis";
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(@NotNull PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (!(clicked instanceof Interaction)) {
            return;
        }
        String graveId = clicked.getPersistentDataContainer().get(graveManager.getGraveKey(), PersistentDataType.STRING);
        if (graveId == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        GraveRecord grave = graveManager.getGraveById(graveId);
        if (grave != null) {
            graveManager.collect(player, grave, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(@NotNull PlayerRespawnEvent event) {
        if (!graveManager.isEnabled() || !graveManager.autoCompassOnRespawn()) {
            return;
        }
        Player player = event.getPlayer();
        if (graveManager.getActiveGrave(player.getUniqueId()) == null) {
            return;
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                graveManager.pointCompassToGrave(player);
            }
        }, 30L);
    }
}