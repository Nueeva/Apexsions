package com.apexsions.fishing.gui;

import com.apexsions.fishing.gui.admin.AdminRodCreatorGUI;
import com.apexsions.fishing.gui.admin.FishingAdminHubGUI;
import com.apexsions.fishing.gui.profile.FishingJournalGUI;
import com.apexsions.fishing.gui.profile.FishingLeaderboardGUI;
import com.apexsions.fishing.gui.profile.FishingProfileGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Event listener routing inventory interactions for all ApexsionsFishing GUIs.
 */
public class FishingGUIListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof FishingVaultGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof VaultShopGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof RodShopGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof FishSellGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof FishingProfileGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof FishingLeaderboardGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof FishingJournalGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof FishingAdminHubGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof AdminRodCreatorGUI gui) {
            gui.handleClick(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof FishingVaultGUI gui) {
            gui.handleDrag(event);
        } else if (holder instanceof FishSellGUI gui) {
            gui.handleDrag(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof FishingVaultGUI gui) {
            gui.handleClose(event);
        } else if (holder instanceof FishSellGUI gui) {
            gui.handleClose(event);
        }
    }
}
