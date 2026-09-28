package com.apexsions.core.container;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Sneak + punch a container to quick-deposit matching items,
 * or Sneak + empty-hand right click to instant-sort the container.
 * Also tracks active container sessions for Bedrock chat commands.
 */
public class ContainerListener implements Listener {

    private final ContainerSortManager manager;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ContainerListener(@NotNull ContainerSortManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSneakPunch(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (!manager.sneakInteractEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isSneaking() || !player.hasPermission("apexsions.container.use")) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        Inventory container = resolve(block.getState());
        if (container == null) {
            return;
        }
        int moved = manager.quickDeposit(player, container);
        if (moved > 0) {
            // Only suppress the default action when we actually deposited, so
            // sneak-breaking a chest still works when there is nothing to move.
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
            player.sendMessage(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>SETOR CEPAT!</bold></gradient> <gray>" + moved + " tumpuk dipindahkan.</gray>"));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onSneakSort(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!manager.sneakSortEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isSneaking() || !player.hasPermission("apexsions.container.use")) {
            return;
        }

        // Only trigger when main hand is empty so players can still sneak-place blocks against chests
        ItemStack item = event.getItem();
        if (item != null && !item.getType().isAir()) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        Inventory container = resolve(block.getState());
        if (container == null) {
            return;
        }

        event.setCancelled(true);
        int count = manager.sort(container);
        if (count < 0) {
            player.sendMessage(mm.deserialize("<gray>Peti sudah kosong.</gray>"));
            return;
        }
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
        player.sendMessage(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>PETI DIRAPIKAN!</bold></gradient> <gray>" + count + " jenis item disusun & digabung.</gray>"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player) {
            manager.recordContainerOpen(player, event.getInventory());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        manager.removeContainerSession(event.getPlayer().getUniqueId());
    }

    @Nullable
    private Inventory resolve(@NotNull BlockState state) {
        return manager.getContainerAt(state);
    }
}