package com.apexsions.core.gui.admin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Handles clicks for MasterAdminGUI, CoreAdminSubGUI, and universal back navigation.
 */
public class AdminHubListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof MasterAdminGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof CoreAdminSubGUI subGUI) {
            subGUI.handleClick(event);
        }
    }
}
