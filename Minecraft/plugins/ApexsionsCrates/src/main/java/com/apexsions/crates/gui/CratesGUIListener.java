package com.apexsions.crates.gui;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.animation.RouletteOpening;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class CratesGUIListener implements Listener {

    private final ApexsionsCratesPlugin plugin;

    public CratesGUIListener(ApexsionsCratesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof RouletteOpening roulette) {
            event.setCancelled(true);
            if (event.getRawSlot() == 22) { // Skip button
                roulette.skip();
            }
        } else if (holder instanceof com.apexsions.crates.animation.SelectableOpening selectable) {
            selectable.handleClick(event);
        } else if (holder instanceof CratesCatalogueGUI catalogue) {
            catalogue.handleClick(event);
        } else if (holder instanceof CratePreviewGUI preview) {
            preview.handleClick(event);
        } else if (holder instanceof CrateVirtualKeysGUI keysGUI) {
            keysGUI.handleClick(event);
        } else if (holder instanceof CrateAdminHubGUI adminGUI) {
            adminGUI.handleClick(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof RouletteOpening ||
            holder instanceof com.apexsions.crates.animation.SelectableOpening ||
            holder instanceof CratesCatalogueGUI ||
            holder instanceof CratePreviewGUI ||
            holder instanceof CrateVirtualKeysGUI ||
            holder instanceof CrateAdminHubGUI) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof RouletteOpening roulette) {
            if (!roulette.isCompleted()) {
                roulette.skip();
            }
        }
    }
}
