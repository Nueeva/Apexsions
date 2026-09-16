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
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enforces anti-griefing protections, flag states, and boundary titles on claimed lands.
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        claimManager.handlePlayerMove(event.getPlayer(), event.getFrom(), event.getTo());
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
        Entity victim = event.getEntity();
        Player attacker = resolvePlayer(event.getDamager());

        // 1. PvP Flag Enforcement
        if (victim instanceof Player && attacker != null) {
            if (!claimManager.isPvpAllowed(victim.getLocation())) {
                event.setCancelled(true);
                attacker.sendActionBar(mm.deserialize("<red>✖ Wilayah ini menonaktifkan pertempuran PvP!</red>"));
                return;
            }
        }

        // 2. Passive Entity Protection
        if (!claimManager.isProtectPassiveEntities()) return;
        if (victim instanceof Animals || victim instanceof Villager || victim instanceof ArmorStand || victim instanceof ItemFrame) {
            if (attacker != null && !claimManager.canBuild(attacker, victim.getLocation())) {
                event.setCancelled(true);
                sendThrottledAlert(attacker, victim.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Monster) {
            if (!claimManager.isMobSpawnAllowed(event.getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (!claimManager.isFireSpreadAllowed(event.getBlock().getLocation())) {
            if (event.getCause() == BlockIgniteEvent.IgniteCause.SPREAD || event.getCause() == BlockIgniteEvent.IgniteCause.LAVA) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (!claimManager.isFireSpreadAllowed(event.getBlock().getLocation())) {
            event.setCancelled(true);
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
        event.blockList().removeIf(b -> !claimManager.isExplosionsAllowed(b.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(b -> !claimManager.isExplosionsAllowed(b.getLocation()));
    }

    private Player resolvePlayer(Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) return p;
        return null;
    }
}
