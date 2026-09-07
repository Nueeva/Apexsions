package com.apexsions.core.sions;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Protects all containers in Kerajaan Sions with an ancient seal.
 * Containers can only be unlocked if the player possesses an official Sions Ancient Key.
 */
public class SionsContainerLockListener implements Listener {

    private final SionsTemporalService service;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public SionsContainerLockListener(SionsTemporalService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onContainerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        if (!service.isEnabled() || !service.isContainerLockEnabled()) return;
        if (!service.isInSions(block.getLocation())) return;

        if (!(block.getState() instanceof Container)) {
            return;
        }

        Player player = event.getPlayer();

        // 1. Check admin bypass
        if (service.isBypassing(player) || (player.hasPermission("apexsions.admin") && player.isSneaking())) {
            return;
        }

        // 2. Validate Sions Ancient Key
        if (service.hasSionsKey(player)) {
            boolean consumed = service.consumeSionsKey(player);
            player.playSound(block.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.6f);
            player.playSound(block.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            player.spawnParticle(Particle.PORTAL, block.getLocation().clone().add(0.5, 0.8, 0.5), 10, 0.2, 0.2, 0.2, 0.05);

            if (consumed && service.isConsumeKeyOnUse()) {
                player.sendMessage(miniMessage.deserialize("<green>🔓 Segel kuno terbuka! 1 <gradient:#8e44ad:#d4af37><bold>Sions Ancient Key</bold></gradient> telah dikonsumsi.</green>"));
            } else {
                player.sendMessage(miniMessage.deserialize("<green>🔓 Segel kuno terbuka dengan <gradient:#8e44ad:#d4af37><bold>Sions Ancient Key</bold></gradient> Anda!</green>"));
            }
            return;
        }

        // 3. No Key: Cancel & feedback
        event.setCancelled(true);
        player.playSound(block.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1.0f, 0.8f);
        player.spawnParticle(Particle.ENCHANT, block.getLocation().clone().add(0.5, 0.8, 0.5), 15, 0.3, 0.3, 0.3, 0.1);
        player.sendMessage(miniMessage.deserialize(
                "<red>🔒 <gradient:#8e44ad:#9b59b6><bold>Segel Kuno Sions:</bold></gradient> <gray>Peti ini terkunci oleh sihir peradaban purba! Dibutuhkan </gray><gradient:#8e44ad:#d4af37><bold>Sions Ancient Key</bold></gradient> <gray>untuk membukanya.</gray></red>"
        ));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onContainerBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!service.isEnabled() || !service.isContainerLockEnabled()) return;
        if (!service.isInSions(block.getLocation())) return;

        if (!(block.getState() instanceof Container)) {
            return;
        }

        Player player = event.getPlayer();
        if (service.isBypassing(player)) {
            return;
        }

        event.setCancelled(true);
        player.playSound(block.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1.0f, 0.6f);
        player.sendMessage(miniMessage.deserialize("<red>🔒 Anda tidak dapat menghancurkan peti relik Kerajaan Sions!</red>"));
    }
}
