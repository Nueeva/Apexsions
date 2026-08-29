package com.apexsions.core.gui.admin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Universal Click Listener for all Master Admin Hub dashboards and sub-GUIs.
 */
public class AdminHubListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof MasterAdminGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof PlayerManagerGUI playerManagerGUI) {
            playerManagerGUI.handleClick(event);
        } else if (holder instanceof PlayerInspectorGUI playerInspectorGUI) {
            playerInspectorGUI.handleClick(event);
        } else if (holder instanceof CoreAdminSubGUI coreGUI) {
            coreGUI.handleClick(event);
        } else if (holder instanceof ChatAdminSubGUI chatGUI) {
            chatGUI.handleClick(event);
        } else if (holder instanceof EconomyAdminSubGUI ecoGUI) {
            ecoGUI.handleClick(event);
        } else if (holder instanceof BattlePassAdminSubGUI bpGUI) {
            bpGUI.handleClick(event);
        } else if (holder instanceof ShopAdminSubGUI shopGUI) {
            shopGUI.handleClick(event);
        } else if (holder instanceof MediaAdminSubGUI mediaGUI) {
            mediaGUI.handleClick(event);
        }
    }
}
