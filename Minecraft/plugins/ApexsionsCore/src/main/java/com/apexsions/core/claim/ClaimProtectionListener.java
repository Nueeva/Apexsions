package com.apexsions.core.claim;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enforces anti-griefing protections on claimed lands and sovereign kingdom territory.
 */
public class ClaimProtectionListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final ClaimManager claimManager;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, Long> lastAlert = new ConcurrentHashMap<>();

    public ClaimProtectionListener(ApexsionsCorePlugin plugin, ClaimManager claimManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
    }

    private void sendThrottledAlert(Player player, Location loc) {
        long now = System.currentTimeMillis();
        Long prev = lastAlert.get(player.getUniqueId());
        if (prev != null && now - prev < 1500) {
            return;
        }
        lastAlert.put(player.getUniqueId(), now);

        Optional<ClaimChunk> claimOpt = claimManager.getClaimAt(loc);
        if (claimOpt.isPresent()) {
            player.sendActionBar(mm.deserialize("<red>✖ Wilayah ini diklaim oleh <gold>" + claimOpt.get().getOwnerName() + "</gold>!</red>"));
        } else {
            player.sendActionBar(mm.deserialize("<red>✖ Wilayah ini dilindungi oleh kedaulatan Kerajaan!</red>"));
        }
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 0.5f, 1.2f);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!claimManager.isProtectBlocks()) return;
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!claimManager.canBuild(player, block.getLocation())) {
            event.setCancelled(true);
            sendThrottledAlert(player, block.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!claimManager.isProtectBlocks()) return;
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!claimManager.canBuild(player, block.getLocation())) {
            event.setCancelled(true);
            sendThrottledAlert(player, block.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (!claimManager.canInteract(player, block.getLocation(), block.getType())) {
            event.setCancelled(true);
            sendThrottledAlert(player, block.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!claimManager.isPreventFluidPlacing()) return;
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!claimManager.canBuild(player, block.getLocation())) {
            event.setCancelled(true);
            sendThrottledAlert(player, block.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!claimManager.canBuild(player, block.getLocation())) {
            event.setCancelled(true);
            sendThrottledAlert(player, block.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!claimManager.isProtectPassiveEntities()) return;
        Entity victim = event.getEntity();

        // Check if victim is passive animal, villager, or armor stand
        if (victim instanceof Animals || victim instanceof Villager || victim instanceof ArmorStand || victim instanceof ItemFrame) {
            Player attacker = resolvePlayer(event.getDamager());
            if (attacker != null && !claimManager.canBuild(attacker, victim.getLocation())) {
                event.setCancelled(true);
                sendThrottledAlert(attacker, victim.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player player) {
            if (!claimManager.canBuild(player, event.getEntity().getLocation())) {
                event.setCancelled(true);
                sendThrottledAlert(player, event.getEntity().getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity instanceof ArmorStand || entity instanceof ItemFrame) {
            Player player = event.getPlayer();
            if (!claimManager.canBuild(player, entity.getLocation())) {
                event.setCancelled(true);
                sendThrottledAlert(player, entity.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!claimManager.isPreventExplosions()) return;
        event.blockList().removeIf(b -> claimManager.getClaimAt(b.getLocation()).isPresent());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!claimManager.isPreventExplosions()) return;
        event.blockList().removeIf(b -> claimManager.getClaimAt(b.getLocation()).isPresent());
    }

    private Player resolvePlayer(Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) return p;
        return null;
    }
}
