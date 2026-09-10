package com.apexsions.customenchants.listener;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.gui.AdminItemCreatorGUI;
import com.apexsions.customenchants.items.ItemLevelRequirement;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Runtime enforcement listener that restricts players from equipping or using items
 * unless they meet the required ApexsionsCore level requirement.
 */
public class ItemLevelRestrictionListener implements Listener {

    private final ApexsionsCustomEnchantsPlugin plugin;

    public ItemLevelRestrictionListener(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    // =========================================================================
    // 1. COMBAT & WEAPON USAGE
    // =========================================================================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon.getType().isAir()) return;

        int req = ItemLevelRequirement.getRequiredLevel(weapon);
        if (req > 0 && !ItemLevelRequirement.canUseItem(player, weapon)) {
            event.setCancelled(true);
            ItemLevelRequirement.sendDenyFeedback(player, req, ItemLevelRequirement.getItemNoun(weapon));
        }
    }

    // =========================================================================
    // 2. MINING & TOOL USAGE
    // =========================================================================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType().isAir()) return;

        int req = ItemLevelRequirement.getRequiredLevel(tool);
        if (req > 0 && !ItemLevelRequirement.canUseItem(player, tool)) {
            event.setCancelled(true);
            ItemLevelRequirement.sendDenyFeedback(player, req, ItemLevelRequirement.getItemNoun(tool));
        }
    }

    // =========================================================================
    // 3. RANGED WEAPONS & FISHING
    // =========================================================================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack bow = event.getBow();
        if (bow == null || bow.getType().isAir()) return;

        int req = ItemLevelRequirement.getRequiredLevel(bow);
        if (req > 0 && !ItemLevelRequirement.canUseItem(player, bow)) {
            event.setCancelled(true);
            ItemLevelRequirement.sendDenyFeedback(player, req, ItemLevelRequirement.getItemNoun(bow));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack rod = player.getInventory().getItemInMainHand();
        if (rod.getType() != Material.FISHING_ROD) {
            rod = player.getInventory().getItemInOffHand();
        }
        if (rod.getType().isAir()) return;

        int req = ItemLevelRequirement.getRequiredLevel(rod);
        if (req > 0 && !ItemLevelRequirement.canUseItem(player, rod)) {
            event.setCancelled(true);
            ItemLevelRequirement.sendDenyFeedback(player, req, "pancingan ini");
        }
    }

    // =========================================================================
    // 4. INTERACT (Right-Click Equip Armor, Tool Block Interactions, etc.)
    // =========================================================================

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) return;

        int req = ItemLevelRequirement.getRequiredLevel(item);
        if (req > 0 && !ItemLevelRequirement.canUseItem(player, item)) {
            // Cancel right-click equipping armor or right-click tool/weapon abilities
            event.setUseItemInHand(Event.Result.DENY);
            if (AdminItemCreatorGUI.isArmor(item)) {
                event.setCancelled(true);
                ItemLevelRequirement.sendDenyFeedback(player, req, ItemLevelRequirement.getItemNoun(item));
            } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                ItemLevelRequirement.sendDenyFeedback(player, req, ItemLevelRequirement.getItemNoun(item));
            }
        }
    }

    // =========================================================================
    // 5. INVENTORY ARMOR EQUIPPING CHECKS
    // =========================================================================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // 1. Direct placement into armor slot
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && !cursor.getType().isAir()) {
                int req = ItemLevelRequirement.getRequiredLevel(cursor);
                if (req > 0 && !ItemLevelRequirement.canUseItem(player, cursor)) {
                    event.setCancelled(true);
                    ItemLevelRequirement.sendDenyFeedback(player, req, ItemLevelRequirement.getItemNoun(cursor));
                    return;
                }
            }

            // Number key swap into armor slot
            if (event.getClick() == ClickType.NUMBER_KEY) {
                int hotbarSlot = event.getHotbarButton();
                if (hotbarSlot >= 0 && hotbarSlot < 9) {
                    ItemStack hotbarItem = player.getInventory().getItem(hotbarSlot);
                    if (hotbarItem != null && !hotbarItem.getType().isAir()) {
                        int req = ItemLevelRequirement.getRequiredLevel(hotbarItem);
                        if (req > 0 && !ItemLevelRequirement.canUseItem(player, hotbarItem)) {
                            event.setCancelled(true);
                            ItemLevelRequirement.sendDenyFeedback(player, req, ItemLevelRequirement.getItemNoun(hotbarItem));
                            return;
                        }
                    }
                }
            }
        }

        // 2. Shift-click equipping armor from player inventory
        if (event.isShiftClick()) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && !clicked.getType().isAir() && AdminItemCreatorGUI.isArmor(clicked)) {
                int req = ItemLevelRequirement.getRequiredLevel(clicked);
                if (req > 0 && !ItemLevelRequirement.canUseItem(player, clicked)) {
                    // Prevent auto-equipping into armor slots
                    event.setCancelled(true);
                    ItemLevelRequirement.sendDenyFeedback(player, req, ItemLevelRequirement.getItemNoun(clicked));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack dragged = event.getOldCursor();
        if (dragged.getType().isAir()) return;

        int req = ItemLevelRequirement.getRequiredLevel(dragged);
        if (req > 0 && !ItemLevelRequirement.canUseItem(player, dragged)) {
            // Check if any target slot is an armor slot (raw slots 5, 6, 7, 8 in player craft view)
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot >= 5 && rawSlot <= 8) {
                    event.setCancelled(true);
                    ItemLevelRequirement.sendDenyFeedback(player, req, ItemLevelRequirement.getItemNoun(dragged));
                    return;
                }
            }
        }
    }

    // =========================================================================
    // 6. SANITY CHECKS (Join, Respawn, and Combat Damage with already-worn items)
    // =========================================================================

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        sanitizeEquippedArmor(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> sanitizeEquippedArmor(event.getPlayer()), 5L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArmorDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        int req = ItemLevelRequirement.getRequiredLevel(item);
        if (req > 0 && !ItemLevelRequirement.canUseItem(player, item)) {
            event.setCancelled(true);
            sanitizeEquippedArmor(player);
        }
    }

    /**
     * Unequips any armor piece the player is not high enough level to wear.
     */
    private void sanitizeEquippedArmor(Player player) {
        if (player == null || !player.isOnline()) return;
        if (player.isOp() || player.hasPermission("apexsions.itemlevel.bypass")) return;

        ItemStack[] armor = player.getInventory().getArmorContents();
        boolean changed = false;

        for (int i = 0; i < armor.length; i++) {
            ItemStack piece = armor[i];
            if (piece == null || piece.getType().isAir()) continue;

            int req = ItemLevelRequirement.getRequiredLevel(piece);
            if (req > 0 && !ItemLevelRequirement.canUseItem(player, piece)) {
                armor[i] = null;
                changed = true;
                ItemLevelRequirement.sendDenyFeedback(player, req, ItemLevelRequirement.getItemNoun(piece));

                // Return to inventory or drop naturally if inventory is full
                Map<Integer, ItemStack> overflow = player.getInventory().addItem(piece);
                for (ItemStack drop : overflow.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
        }

        if (changed) {
            player.getInventory().setArmorContents(armor);
            player.updateInventory();
        }
    }
}
