package com.apexsions.core.pose;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PoseListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final PoseManager poseManager;
    private final MiniMessage mm;

    public PoseListener(ApexsionsCorePlugin plugin, PoseManager poseManager) {
        this.plugin = plugin;
        this.poseManager = poseManager;
        this.mm = MiniMessage.miniMessage();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        if (player.isSneaking()) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        // Only allow if clicking Stairs or Slabs
        if (!(block.getBlockData() instanceof Stairs) && !(block.getBlockData() instanceof Slab)) {
            return;
        }

        // Check if holding an interactive block or item
        Material inHand = player.getInventory().getItemInMainHand().getType();
        if (inHand.isBlock() || inHand.isEdible() || inHand == Material.WATER_BUCKET || inHand == Material.LAVA_BUCKET) {
            return;
        }

        // Check distance limit
        double distSq = player.getLocation().distanceSquared(block.getLocation().add(0.5, 0.5, 0.5));
        double maxDist = poseManager.getChairDistanceLimit();
        if (distSq > maxDist * maxDist) {
            return;
        }

        if (poseManager.sitOnChair(player, block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDismount(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (poseManager.isSeatEntity(event.getDismounted())) {
            poseManager.standUp(player, true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) {
            return;
        }

        Player player = event.getPlayer();
        if (poseManager.hasActivePose(player) && !poseManager.isSitting(player)) {
            // Check overhead clearance if player is crawling/laying
            Block headBlock = player.getLocation().add(0, 1.5, 0).getBlock();
            if (headBlock.getType().isSolid()) {
                player.sendActionBar(mm.deserialize("<red>❌ Ruang atas terlalu sempit untuk berdiri tegak!</red>"));
                return;
            }
            poseManager.standUp(player, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        // Stand up any player sitting on this block
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (poseManager.isSitting(online)) {
                if (online.getLocation().getBlock().equals(block) || online.getLocation().add(0, -0.5, 0).getBlock().equals(block)) {
                    poseManager.standUp(online, true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        poseManager.standUp(event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        poseManager.standUp(event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        poseManager.standUp(event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (poseManager.hasActivePose(player)) {
                poseManager.standUp(player, false);
            }
        }
    }
}
