package com.apexsions.core.vanish;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Event listener protecting vanished players from detection, damage, mob targeting,
 * physical pressure plates, item suction, and noisy container openings.
 */
public class VanishListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final VanishManager vanishManager;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public VanishListener(ApexsionsCorePlugin plugin, VanishManager vanishManager) {
        this.plugin = plugin;
        this.vanishManager = vanishManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (vanishManager.isVanished(player)) {
            event.joinMessage(null);
        }
        vanishManager.handlePlayerJoin(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (vanishManager.isVanished(player)) {
            event.quitMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        vanishManager.handleWorldChange(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        vanishManager.handleWorldChange(event.getPlayer());
    }

    /**
     * Prevent vanished players from taking any damage (invulnerable).
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && vanishManager.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevent vanished players from attacking or being attacked.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPvP(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker && vanishManager.isVanished(attacker)) {
            attacker.sendMessage(mm.deserialize("<red>⚠️ Kamu tidak dapat menyerang entitas lain saat dalam mode vanish!</red>"));
            event.setCancelled(true);
            return;
        }

        if (event.getEntity() instanceof Player victim && vanishManager.isVanished(victim)) {
            event.setCancelled(true);
        }
    }

    /**
     * Hostile mobs completely ignore vanished players.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player player && vanishManager.isVanished(player)) {
            event.setCancelled(true);
            event.setTarget(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player && vanishManager.isVanished(player)) {
            event.setCancelled(true);
            event.setTarget(null);
        }
    }

    /**
     * Prevent vanished players from accidentally picking up ground items.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && vanishManager.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevent triggering physical pressure plates/tripwires, and allow silent chest viewing.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!vanishManager.isVanished(player)) return;

        // 1. Physical trigger cancel (pressure plates, tripwires, farmland stomp)
        if (event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
            return;
        }

        // 2. Silent container viewing (open inventory directly without animation/sound)
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            if (isContainerBlock(block.getType())) {
                if (block.getState() instanceof Container container) {
                    event.setCancelled(true);
                    player.openInventory(container.getInventory());
                    player.sendMessage(mm.deserialize("<dark_gray>[<gradient:#f1c40f:#e67e22><bold>Vanish</bold></gradient>]</dark_gray> <gray>Membuka peti secara <yellow>senyap (Silent Container)</yellow>...</gray>"));
                }
            }
        }
    }

    /**
     * Keep hunger full while vanished.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodLoss(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && vanishManager.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    private boolean isContainerBlock(Material mat) {
        return mat == Material.CHEST
                || mat == Material.TRAPPED_CHEST
                || mat == Material.BARREL
                || mat == Material.SHULKER_BOX
                || mat.name().endsWith("_SHULKER_BOX");
    }
}
