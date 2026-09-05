package com.apexsions.core.kit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Event listener routing inventory interactions for Kit-related GUIs.
 */
public class KitGUIListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof KitUserGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof KitPreviewGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof KitAdminCreatorGUI gui) {
            gui.handleClick(event);
        }
    }
}
