package com.apexsions.core.stack;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Event listener that bridges item spawn, drop, merge, and pickup events
 * with the uncapped item stacking and hologram engine.
 */
public class ItemStackListener implements Listener {

    private final ItemStackManager stackManager;

    public ItemStackListener(@NotNull ItemStackManager stackManager) {
        this.stackManager = stackManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(@NotNull ItemSpawnEvent event) {
        if (!stackManager.isEnabled()) {
            return;
        }
        Item item = event.getEntity();
        stackManager.initItem(item);

        if (stackManager.isMergeOnSpawn() && stackManager.isStackable(item)) {
            Bukkit.getScheduler().runTask(stackManager.getPlugin(), () -> {
                if (item.isValid() && !item.isDead()) {
                    stackManager.tryMergeNearby(item);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(@NotNull PlayerDropItemEvent event) {
        if (!stackManager.isEnabled()) {
            return;
        }
        Item item = event.getItemDrop();
        stackManager.initItem(item);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemMerge(@NotNull ItemMergeEvent event) {
        if (!stackManager.isEnabled()) {
            return;
        }
        Item target = event.getTarget();
        Item entity = event.getEntity();

        int countTarget = stackManager.getStackCount(target);
        int countEntity = stackManager.getStackCount(entity);
        int total = countTarget + countEntity;

        if (total <= stackManager.getMaxStackSize()) {
            stackManager.setStackCount(target, total);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPickupItem(@NotNull EntityPickupItemEvent event) {
        if (!stackManager.isEnabled()) {
            return;
        }
        Item item = event.getItem();
        int totalCount = stackManager.getStackCount(item);

        LivingEntity picker = event.getEntity();

        if (picker instanceof Player player) {
            ItemStack template = item.getItemStack().clone();
            int maxStack = template.getMaxStackSize();
            int remainingToGive = totalCount;
            int given = 0;

            PlayerInventory inv = player.getInventory();

            while (remainingToGive > 0) {
                int batch = Math.min(remainingToGive, maxStack);
                template.setAmount(batch);
                Map<Integer, ItemStack> leftover = inv.addItem(template);
                if (leftover.isEmpty()) {
                    given += batch;
                    remainingToGive -= batch;
                } else {
                    int notAdded = leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
                    int added = batch - notAdded;
                    given += added;
                    remainingToGive -= added;
                    break; // Inventory is full
                }
            }

            if (given > 0) {
                int remainingOnGround = totalCount - given;
                if (remainingOnGround <= 0) {
                    try {
                        player.playPickupItemAnimation(item);
                    } catch (Throwable ignored) {
                    }
                    item.remove();
                } else {
                    stackManager.setStackCount(item, remainingOnGround);
                }
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.25f, 1.8f);
            }

            // Always cancel vanilla pickup so vanilla does not delete the entity or duplicate items
            event.setCancelled(true);
        } else {
            // Non-player mob pickup (Fox, Piglin, Zombie)
            if (totalCount > 1) {
                ItemStack single = item.getItemStack().clone();
                single.setAmount(1);
                if (picker.getEquipment() != null) {
                    picker.getEquipment().setItemInMainHand(single);
                }
                int remaining = totalCount - 1;
                stackManager.setStackCount(item, remaining);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryPickupItem(@NotNull InventoryPickupItemEvent event) {
        if (!stackManager.isEnabled()) {
            return;
        }
        Item item = event.getItem();
        int totalCount = stackManager.getStackCount(item);
        if (totalCount <= 1) {
            return;
        }

        Inventory inv = event.getInventory();
        ItemStack sample = item.getItemStack().clone();
        sample.setAmount(1);

        Map<Integer, ItemStack> leftover = inv.addItem(sample);
        if (leftover.isEmpty()) {
            int remaining = totalCount - 1;
            if (remaining <= 0) {
                item.remove();
            } else {
                stackManager.setStackCount(item, remaining);
            }
        }
        event.setCancelled(true);
    }
}
