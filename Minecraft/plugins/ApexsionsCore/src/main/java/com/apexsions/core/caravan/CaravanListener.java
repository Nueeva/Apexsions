package com.apexsions.core.caravan;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Opens the caravan menu when the merchant is clicked and processes purchases.
 */
public class CaravanListener implements Listener {

    private final CaravanManager manager;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public CaravanListener(@NotNull CaravanManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(@NotNull PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (!manager.isCaravanNpc(clicked)) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (!player.hasPermission("apexsions.caravan.use")) {
            return;
        }
        new CaravanGUI(manager, player).open(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof CaravanGUI gui)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        CaravanOffer offer = gui.getOfferAt(event.getRawSlot());
        if (offer == null) {
            return;
        }
        String result = manager.purchase(player, offer);
        player.sendMessage(mm.deserialize(result));
        if (result.contains("BERHASIL")) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            new CaravanGUI(manager, player).open(player); // refresh balance display
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }
}