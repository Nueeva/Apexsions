package com.apexsions.core.gui.warp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class WarpGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof WarpGUI gui) {
            gui.handleClick(e);
        } else if (e.getInventory().getHolder() instanceof WarpAdminGUI adminGui) {
            adminGui.handleClick(e);
        } else if (e.getInventory().getHolder() instanceof WarpEditorGUI editorGui) {
            editorGui.handleClick(e);
        }
    }
}
