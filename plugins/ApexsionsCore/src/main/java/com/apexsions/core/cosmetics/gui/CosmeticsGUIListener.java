package com.apexsions.core.cosmetics.gui;

import com.apexsions.core.title.gui.TitleVaultGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Click listener for TitleVaultGUI and CosmeticsMainGUI.
 */
public class CosmeticsGUIListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof CosmeticsMainGUI cosmeticsGUI) {
            cosmeticsGUI.handleClick(event);
        } else if (holder instanceof TitleVaultGUI titleGUI) {
            titleGUI.handleClick(event);
        }
    }
}
