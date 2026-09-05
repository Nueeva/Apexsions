package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Event listener routing inventory interactions for all Custom Enchant GUIs.
 */
public class CustomEnchantsGUIListener implements Listener {

    private ApexsionsCustomEnchantsPlugin plugin;

    public CustomEnchantsGUIListener() {}

    public CustomEnchantsGUIListener(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof EnchanterGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof SpecificBookShopGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof AdminTierPricingGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof AceAdminHubGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof AceEnchantsCatalogGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof AceBookLevelsSubGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof AdminItemCreatorGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof TinkererComingSoonGUI gui) {
            gui.handleClick(event);
        }
    }
}
