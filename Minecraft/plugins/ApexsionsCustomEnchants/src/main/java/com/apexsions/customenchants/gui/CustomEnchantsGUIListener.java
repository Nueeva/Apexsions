package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
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
        } else if (holder instanceof ShopCategorySelectGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof AdminPresetsGUI gui) {
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
        } else if (holder instanceof ItemModifierGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof CustomEnchantPickerGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof VanillaEnchantPickerGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof EnchantLevelPickerGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof VanillaLevelPickerGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof ArmorSetBonusPickerGUI gui) {
            gui.handleClick(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof AdminItemCreatorGUI gui) {
            gui.handleClose(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof AdminItemCreatorGUI gui) {
            gui.handleDrag(event);
        } else if (holder instanceof EnchanterGUI
                || holder instanceof SpecificBookShopGUI
                || holder instanceof ShopCategorySelectGUI
                || holder instanceof AdminPresetsGUI
                || holder instanceof AdminTierPricingGUI
                || holder instanceof AceAdminHubGUI
                || holder instanceof AceEnchantsCatalogGUI
                || holder instanceof AceBookLevelsSubGUI
                || holder instanceof TinkererComingSoonGUI
                || holder instanceof ItemModifierGUI
                || holder instanceof CustomEnchantPickerGUI
                || holder instanceof VanillaEnchantPickerGUI
                || holder instanceof EnchantLevelPickerGUI
                || holder instanceof VanillaLevelPickerGUI
                || holder instanceof ArmorSetBonusPickerGUI) {
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot < event.getInventory().getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
