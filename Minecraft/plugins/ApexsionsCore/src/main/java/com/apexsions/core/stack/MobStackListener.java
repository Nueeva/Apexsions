package com.apexsions.core.stack;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Event listener that bridges mob spawn, death, transformation, shearing,
 * dyeing, and naming interactions with the smart mob stacking engine.
 */
public class MobStackListener implements Listener {

    private final MobStackManager stackManager;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public MobStackListener(@NotNull MobStackManager stackManager) {
        this.stackManager = stackManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(@NotNull CreatureSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }
        stackManager.tryMergeOnSpawn(event.getEntity(), event.getSpawnReason());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!stackManager.isEnabled()) {
            return;
        }
        int count = stackManager.getStackCount(entity);
        if (count <= 1) {
            return;
        }

        // Void death protection: instant kill without infinite replacement cascade
        if (stackManager.isInstantKillOnVoid() && stackManager.isVoidDeath(entity)) {
            return;
        }

        // Fall damage protection: when a mob grinder drop kills a stacked mob,
        // kill the remaining stack and drop loot/XP so the farm doesn't stall.
        EntityDamageEvent lastDamage = entity.getLastDamageCause();
        if (stackManager.isKillStackOnFall() && lastDamage != null && lastDamage.getCause() == EntityDamageEvent.DamageCause.FALL) {
            stackManager.dropRemainingLootAndExp(entity, count - 1, event);
            return;
        }

        // Sneak kill all: crouching players slay the entire remaining stack at once
        Player killer = entity.getKiller();
        if (stackManager.isKillAllOnSneak() && killer != null && killer.isSneaking()) {
            stackManager.dropRemainingLootAndExp(entity, count - 1, event);
            killer.sendActionBar(mm.deserialize(
                    "<gold>⚡ Slayed entire stack of <yellow>[x" + count + "]</yellow> <white>" + stackManager.formatTypeName(entity.getType()) + "</white>!</gold>"));
            return;
        }

        // Standard single-kill: decrement count and spawn replacement with preserved state
        stackManager.handleDeath(entity);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityTransform(@NotNull EntityTransformEvent event) {
        Entity original = event.getEntity();
        Entity transformed = event.getTransformedEntity();
        if (!stackManager.isEnabled() || !(transformed instanceof LivingEntity livingTransformed)) {
            return;
        }
        int count = stackManager.getStackCount(original);
        if (count > 1) {
            stackManager.setStackCount(livingTransformed, count);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntity(@NotNull PlayerInteractEntityEvent event) {
        if (!stackManager.isEnabled() || !stackManager.respectCustomNames()) {
            return;
        }
        if (!(event.getRightClicked() instanceof Mob mob)) {
            return;
        }
        int count = stackManager.getStackCount(mob);
        if (count <= 1) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());
        if (item.getType() != Material.NAME_TAG) {
            return;
        }
        var meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        // Player wants to name ONE mob from the stack.
        // Unstack 1 mob, apply the name, and decrement the stack count.
        event.setCancelled(true);
        if (player.getGameMode() != GameMode.CREATIVE) {
            item.subtract(1);
        }
        stackManager.setStackCount(mob, count - 1);

        Location loc = mob.getLocation();
        World world = loc.getWorld();
        if (world != null) {
            Class<? extends Entity> entityClass = mob.getType().getEntityClass();
            if (entityClass != null && Mob.class.isAssignableFrom(entityClass)) {
                @SuppressWarnings("unchecked")
                Class<? extends Mob> mobClass = (Class<? extends Mob>) entityClass;
                world.spawn(loc, mobClass, CreatureSpawnEvent.SpawnReason.CUSTOM, namedMob -> {
                    stackManager.copyEntityState(mob, namedMob);
                    stackManager.setStackCount(namedMob, 1);
                    namedMob.customName(meta.displayName());
                    namedMob.setCustomNameVisible(true);
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerShearEntity(@NotNull PlayerShearEntityEvent event) {
        if (!stackManager.isEnabled() || !(event.getEntity() instanceof Sheep sheep)) {
            return;
        }
        int count = stackManager.getStackCount(sheep);
        if (count <= 1) {
            return;
        }
        // Current sheep gets sheared and becomes a single sheep
        stackManager.setStackCount(sheep, 1);

        // Spawn the remaining unsheared sheep as a stack
        Location loc = sheep.getLocation();
        World world = loc.getWorld();
        if (world != null) {
            world.spawn(loc, Sheep.class, CreatureSpawnEvent.SpawnReason.CUSTOM, unsheared -> {
                stackManager.copyEntityState(sheep, unsheared);
                unsheared.setSheared(false);
                stackManager.setStackCount(unsheared, count - 1);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSheepDyeWool(@NotNull SheepDyeWoolEvent event) {
        if (!stackManager.isEnabled()) {
            return;
        }
        Sheep sheep = event.getEntity();
        int count = stackManager.getStackCount(sheep);
        if (count <= 1) {
            return;
        }
        DyeColor oldColor = sheep.getColor();
        // Current sheep gets dyed and becomes a single sheep
        stackManager.setStackCount(sheep, 1);

        // Spawn the remaining original colored sheep as a stack
        Location loc = sheep.getLocation();
        World world = loc.getWorld();
        if (world != null) {
            world.spawn(loc, Sheep.class, CreatureSpawnEvent.SpawnReason.CUSTOM, rep -> {
                stackManager.copyEntityState(sheep, rep);
                rep.setColor(oldColor);
                stackManager.setStackCount(rep, count - 1);
            });
        }
    }
}