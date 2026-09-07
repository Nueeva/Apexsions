package com.apexsions.core.sions;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

        // 2. Check if already unlocked in this temporal cycle
        if (service.isChestUnlockedThisCycle(block)) {
            return; // Container is already open during this hour!
        }

        // 3. Resolve container tier
        SionsKeyTier tier = service.getContainerTier(block);

        // 4. Validate Sions Ancient Key of matching tier
        if (service.hasSionsKey(player, tier)) {
            boolean consumed = service.consumeSionsKey(player, tier);
            service.markChestUnlockedThisCycle(block);

            player.playSound(block.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.6f);
            player.playSound(block.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            player.spawnParticle(Particle.PORTAL, block.getLocation().clone().add(0.5, 0.8, 0.5), 15, 0.2, 0.2, 0.2, 0.05);

            String keyFormattedName = tier.getDefaultFormattedName();
            if (consumed && service.isConsumeKeyOnUse()) {
                player.sendMessage(miniMessage.deserialize("<green>🔓 Segel kuno terbuka! 1 " + keyFormattedName + " <green>telah dikonsumsi.</green>"));
            } else {
                player.sendMessage(miniMessage.deserialize("<green>🔓 Segel kuno terbuka dengan " + keyFormattedName + " <green>Anda!</green>"));
            }

            // Roll custom enchants loot reward
            grantContainerCustomEnchantLoot(player, tier, block.getLocation());
            return;
        }

        // 5. No Key: Cancel & feedback with specific required key
        event.setCancelled(true);
        player.playSound(block.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1.0f, 0.8f);
        player.spawnParticle(Particle.ENCHANT, block.getLocation().clone().add(0.5, 0.8, 0.5), 15, 0.3, 0.3, 0.3, 0.1);
        player.sendMessage(miniMessage.deserialize(
                "<red>🔒 <gradient:#8e44ad:#9b59b6><bold>Segel Kuno Sions [" + tier.getDisplayName() + "]:</bold></gradient> <gray>Peti ini terkunci oleh sihir peradaban purba! Dibutuhkan </gray>" +
                tier.getDefaultFormattedName() + " <gray>untuk membukanya.</gray></red>"
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

    private void grantContainerCustomEnchantLoot(Player player, SionsKeyTier tier, Location loc) {
        if (Bukkit.getPluginManager().getPlugin("ApexsionsCustomEnchants") == null) {
            return;
        }

        double roll = Math.random();
        String command = null;
        String rewardName = null;

        if (tier == SionsKeyTier.BOSS) {
            // Guaranteed high-tier rewards from Boss Chest
            if (roll < 0.35) {
                command = "ace givebook " + player.getName() + " thunderlord 3 100 0";
                rewardName = "<gradient:#f1c40f:#d35400><bold>Buku Sihir: Thunderlord III (100% Success)</bold></gradient>";
            } else if (roll < 0.70) {
                command = "ace givebook " + player.getName() + " unholy 5 100 0";
                rewardName = "<gradient:#9b59b6:#e74c3c><bold>Buku Sihir: Unholy V (100% Success)</bold></gradient>";
            } else {
                command = "ace givescroll " + player.getName() + " white";
                rewardName = "<white><bold>White Scroll (Perlindungan Item)</bold></white>";
            }
        } else if (tier == SionsKeyTier.ELITE) {
            // 50% chance for Elite loot
            if (roll < 0.20) {
                command = "ace givebook " + player.getName() + " critical 3 85 15";
                rewardName = "<yellow><bold>Buku Sihir: Critical III</bold></yellow>";
            } else if (roll < 0.40) {
                command = "ace givebook " + player.getName() + " ward 3 85 15";
                rewardName = "<blue><bold>Buku Sihir: Ward III</bold></blue>";
            } else if (roll < 0.55) {
                command = "ace givedust " + player.getName() + " magic 25";
                rewardName = "<light_purple><bold>Magic Dust (+25% Success Rate)</bold></light_purple>";
            }
        } else if (tier == SionsKeyTier.COMMON) {
            // 25% chance for Common loot
            if (roll < 0.15) {
                command = "ace givebook " + player.getName() + " strike 2 75 25";
                rewardName = "<gray><bold>Buku Sihir: Strike II</bold></gray>";
            } else if (roll < 0.30) {
                command = "ace givedust " + player.getName() + " mystery";
                rewardName = "<dark_purple><bold>Mystery Dust</bold></dark_purple>";
            }
        }

        if (command != null) {
            final String finalCmd = command;
            final String finalReward = rewardName;
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("ApexsionsCore"), () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                player.sendMessage(miniMessage.deserialize("<gold>✨ Khazanah Kuno: </gold><gray>Anda menemukan </gray>" + finalReward + "<gray> dari dalam peti!</gray>"));
                player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.4f);
            });
        }
    }
}
