package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
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
        } else if (holder instanceof ArmorSetBonusTierGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof StatValuePickerGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof ToolBonusPickerGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof ToolStatValuePickerGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof RemoveEnchantsGUI gui) {
            gui.handleClick(event);
        }
    }

    public static boolean isCreatorRelatedHolder(InventoryHolder holder) {
        return holder instanceof AdminItemCreatorGUI
                || holder instanceof ArmorSetBonusPickerGUI
                || holder instanceof ArmorSetBonusTierGUI
                || holder instanceof StatValuePickerGUI
                || holder instanceof ToolBonusPickerGUI
                || holder instanceof ToolStatValuePickerGUI
                || holder instanceof ItemModifierGUI
                || holder instanceof CustomEnchantPickerGUI
                || holder instanceof VanillaEnchantPickerGUI
                || holder instanceof EnchantLevelPickerGUI
                || holder instanceof VanillaLevelPickerGUI
                || holder instanceof RemoveEnchantsGUI
                || holder instanceof AdminPresetsGUI;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof org.bukkit.entity.Player player)) return;
        InventoryHolder holder = event.getInventory().getHolder();

        // If closing the main creator directly
        if (holder instanceof AdminItemCreatorGUI gui) {
            gui.handleClose(event);
            return;
        }

        // If closing any sub-GUI or external window while having an active creator session
        AdminItemCreatorGUI creator = AdminItemCreatorGUI.getActiveCreator(player.getUniqueId());
        if (creator != null) {
            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    creator.returnAllItems();
                    AdminItemCreatorGUI.unregisterActiveCreator(player.getUniqueId());
                    return;
                }
                Inventory top = player.getOpenInventory().getTopInventory();
                if (isCreatorRelatedHolder(top.getHolder())) {
                    // Still navigating between creator GUIs, do NOT return items yet!
                    return;
                }
                // The player closed the GUI or opened something unrelated!
                if (!creator.getPlacedItems().isEmpty()) {
                    creator.returnAllItems();
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(
                            "<yellow>Item Creator ditutup. Seluruh item telah dikembalikan secara aman ke inventarismu.</yellow>"
                    ));
                }
                AdminItemCreatorGUI.unregisterActiveCreator(player.getUniqueId());
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        AdminItemCreatorGUI creator = AdminItemCreatorGUI.getActiveCreator(player.getUniqueId());
        if (creator != null) {
            creator.returnAllItems();
            AdminItemCreatorGUI.unregisterActiveCreator(player.getUniqueId());
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
                || holder instanceof ArmorSetBonusPickerGUI
                || holder instanceof ArmorSetBonusTierGUI
                || holder instanceof StatValuePickerGUI
                || holder instanceof ToolBonusPickerGUI
                || holder instanceof ToolStatValuePickerGUI
                || holder instanceof RemoveEnchantsGUI) {
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot < event.getInventory().getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
