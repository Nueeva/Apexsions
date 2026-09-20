package com.apexsions.core.stack;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Smart Mob Stacking Engine.
 *
 * Reduces raw entity counts from spawners, farms and natural spawns by merging
 * identical passive/hostile mobs into a single representative entity carrying a
 * stack counter in its PersistentDataContainer. Fewer rendered entities keeps
 * server TPS stable and helps low-end Bedrock clients hold their FPS.
 *
 * Merge is purely cosmetic for gameplay: killing a stacked mob removes one unit
 * from the stack and spawns the remaining units, so loot rates are unchanged.
 */
public class MobStackManager {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final NamespacedKey countKey;

    private BukkitTask scanTask;

    /** Cached per pass to avoid re-evaluating the world list for every entity. */
    private final Map<EntityType, Boolean> ignoredTypeCache = new ConcurrentHashMap<>();

    public MobStackManager(@NotNull ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        this.countKey = new NamespacedKey(plugin, "stack_count");
    }

    // --- Configuration ---

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("mob-stacking.enabled", true);
    }

    public double getMergeRadius() {
        return Math.max(1.0, plugin.getConfig().getDouble("mob-stacking.merge-radius", 6.0));
    }

    public int getMaxStackSize() {
        return Math.max(2, plugin.getConfig().getInt("mob-stacking.max-stack-size", 50));
    }

    public boolean mergeOnSpawn() {
        return plugin.getConfig().getBoolean("mob-stacking.merge-on-spawn", true);
    }

    public boolean respectCustomNames() {
        return plugin.getConfig().getBoolean("mob-stacking.respect-custom-names", true);
    }

    public long getScanIntervalTicks() {
        return Math.max(20L, plugin.getConfig().getLong("mob-stacking.scan-interval-ticks", 100L));
    }

    public int getScanEntitiesPerPass() {
        return Math.max(100, plugin.getConfig().getInt("mob-stacking.scan-entities-per-pass", 4000));
    }

    private List<String> getWorlds() {
        List<String> worlds = plugin.getConfig().getStringList("mob-stacking.worlds");
        return worlds != null ? worlds : new ArrayList<>();
    }

    private boolean isWorldEnabled(@NotNull World world) {
        List<String> worlds = getWorlds();
        if (worlds.isEmpty()) {
            return true;
        }
        for (String w : worlds) {
            if (w != null && w.equalsIgnoreCase(world.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isIgnoredType(@NotNull EntityType type) {
        return ignoredTypeCache.computeIfAbsent(type, t -> {
            List<String> ignored = plugin.getConfig().getStringList("mob-stacking.ignored-types");
            if (ignored != null) {
                for (String ig : ignored) {
                    if (ig != null && ig.equalsIgnoreCase(t.name())) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private boolean isIgnoredSpawnReason(@NotNull CreatureSpawnEvent.SpawnReason reason) {
        List<String> ignored = plugin.getConfig().getStringList("mob-stacking.ignored-spawn-reasons");
        if (ignored != null) {
            for (String ig : ignored) {
                if (ig != null && ig.equalsIgnoreCase(reason.name())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean mergeableReason(@NotNull CreatureSpawnEvent.SpawnReason reason) {
        return !isIgnoredSpawnReason(reason);
    }

    // --- Lifecycle ---

    public void start() {
        if (!isEnabled()) {
            return;
        }
        startScanTask();
    }

    public void stop() {
        if (scanTask != null) {
            scanTask.cancel();
            scanTask = null;
        }
    }

    private void startScanTask() {
        if (scanTask != null) {
            scanTask.cancel();
        }
        scanTask = Bukkit.getScheduler().runTaskTimer(plugin, this::runSweep, getScanIntervalTicks(), getScanIntervalTicks());
    }

    // --- Stack state helpers ---

    public int getStackCount(@NotNull Entity entity) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        Integer count = pdc.get(countKey, PersistentDataType.INTEGER);
        return count != null && count > 0 ? count : 1;
    }

    public void setStackCount(@NotNull Entity entity, int count) {
        if (count <= 1) {
            entity.getPersistentDataContainer().remove(countKey);
            entity.customName(null);
            if (entity instanceof LivingEntity living) {
                living.setCustomNameVisible(false);
            }
        } else {
            entity.getPersistentDataContainer().set(countKey, PersistentDataType.INTEGER, count);
            if (entity instanceof LivingEntity living) {
                living.customName(buildName(entity.getType(), count));
                living.setCustomNameVisible(true);
            }
        }
    }

    private Component buildName(@NotNull EntityType type, int count) {
        String raw = plugin.getConfig().getString("mob-stacking.display.name-format",
                "<gray>%type% <yellow>[x%count%]</yellow>");
        if (raw == null) {
            raw = "<gray>%type% <yellow>[x%count%]</yellow>";
        }
        String pretty = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        if (!pretty.isEmpty()) {
            pretty = Character.toUpperCase(pretty.charAt(0)) + pretty.substring(1);
        }
        String text = raw.replace("%type%", pretty).replace("%count%", String.valueOf(count));
        return mm.deserialize(text);
    }

    public boolean isStackable(@NotNull Entity entity) {
        if (!(entity instanceof Mob mob) || entity instanceof Boss) {
            return false;
        }
        if (entity instanceof Player || entity instanceof Tameable tameable && tameable.isTamed()) {
            return false;
        }
        if (!mob.getPassengers().isEmpty()) {
            return false;
        }
        if (mob.getEquipment() != null) {
            for (var item : mob.getEquipment().getArmorContents()) {
                if (item != null && !item.getType().isAir()) {
                    return false;
                }
            }
            if (mob.getEquipment().getItemInMainHand() != null && !mob.getEquipment().getItemInMainHand().getType().isAir()) {
                return false;
            }
        }
        if (mob.isLeashed()) {
            return false;
        }
        if (respectCustomNames() && mob.customName() != null && getStackCount(mob) <= 1) {
            return false;
        }
        return isWorldEnabled(mob.getWorld()) && !isIgnoredType(mob.getType());
    }

    private boolean sameStackGroup(@NotNull Entity a, @NotNull Entity b) {
        if (a.getType() != b.getType()) {
            return false;
        }
        if (a instanceof Ageable aa && b instanceof Ageable bb && aa.isAdult() != bb.isAdult()) {
            return false;
        }
        return isStackable(b);
    }

    // --- Merge operations ---

    /**
     * Called after a mob spawns. Attempts to merge it into a nearby identical stack.
     */
    public void tryMergeOnSpawn(@NotNull LivingEntity spawned, @NotNull CreatureSpawnEvent.SpawnReason reason) {
        if (!isEnabled() || !mergeOnSpawn()) {
            return;
        }
        if (!mergeableReason(reason) || !(spawned instanceof Mob)) {
            return;
        }
        if (getStackCount(spawned) > 1) {
            return; // already authoring a stack (replacement spawn)
        }
        if (!isStackable(spawned)) {
            return;
        }

        double radius = getMergeRadius();
        for (Entity nearby : spawned.getNearbyEntities(radius, radius, radius)) {
            if (nearby.getUniqueId().equals(spawned.getUniqueId())) {
                continue;
            }
            if (!sameStackGroup(spawned, nearby)) {
                continue;
            }
            if (!(nearby instanceof LivingEntity living)) {
                continue;
            }
            int total = getStackCount(living) + getStackCount(spawned);
            if (total > getMaxStackSize()) {
                continue;
            }
            // Drop the newly spawned entity instantly and absorb its count.
            spawned.remove();
            setStackCount(living, total);
            return;
        }
    }

    /**
     * Called from EntityDeathEvent. When a stacked mob dies, removes one unit and
     * re-spawns the remainder so loot/XP rates stay vanilla-equivalent.
     */
    public void handleDeath(@NotNull LivingEntity dead) {
        if (!isEnabled()) {
            return;
        }
        int count = getStackCount(dead);
        if (count <= 1) {
            return;
        }
        int remaining = count - 1;
        Location loc = dead.getLocation().clone();
        World world = loc.getWorld();
        if (world == null) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!world.isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                return;
            }
            try {
                Entity replacement = world.spawnEntity(loc, dead.getType());
                if (replacement instanceof LivingEntity) {
                    setStackCount(replacement, remaining);
                }
            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "Failed spawning mob stack replacement for " + dead.getType(), t);
                // Do not lose the remainder silently: drop experience to offset the lost units.
                world.spawn(loc, org.bukkit.entity.ExperienceOrb.class, orb -> orb.setExperience(remaining));
            }
        });
    }

    // --- Periodic sweep ---

    /**
     * Batched, capped sweep that merges already-existing mob crowds (large farms
     * that predate the feature). Groups entities per chunk to stay near O(n).
     */
    private void runSweep() {
        if (!isEnabled()) {
            return;
        }
        int budget = getScanEntitiesPerPass();
        int processed = 0;

        for (World world : Bukkit.getWorlds()) {
            if (!isWorldEnabled(world)) {
                continue;
            }
            List<LivingEntity> candidates = new ArrayList<>();
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof LivingEntity living) || entity instanceof Player) {
                    continue;
                }
                candidates.add(living);
                // Budget is checked while scanning so we never materialize a huge
                // candidate list on entity-dense worlds.
                processed++;
                if (processed >= budget) {
                    break;
                }
            }

            Map<Long, List<LivingEntity>> byChunk = new HashMap<>();
            for (LivingEntity living : candidates) {
                if (!isStackable(living)) {
                    continue;
                }
                Location l = living.getLocation();
                long key = (((long) (l.getBlockX() >> 4)) << 32) ^ (l.getBlockZ() >> 4);
                byChunk.computeIfAbsent(key, k -> new ArrayList<>()).add(living);
            }

            for (List<LivingEntity> group : byChunk.values()) {
                mergeGroup(group);
            }

            if (processed >= budget) {
                return;
            }
        }
    }

    private void mergeGroup(@NotNull List<LivingEntity> group) {
        if (group.size() < 2) {
            return;
        }
        Map<EntityType, List<LivingEntity>> byType = new HashMap<>();
        for (LivingEntity living : group) {
            byType.computeIfAbsent(living.getType(), k -> new ArrayList<>()).add(living);
        }

        for (List<LivingEntity> sameType : byType.values()) {
            if (sameType.size() < 2) {
                continue;
            }
            // Split adults vs babies so ages never mix.
            List<LivingEntity> adults = new ArrayList<>();
            List<LivingEntity> babies = new ArrayList<>();
            for (LivingEntity living : sameType) {
                if (living instanceof Ageable ageable && !ageable.isAdult()) {
                    babies.add(living);
                } else {
                    adults.add(living);
                }
            }
            mergeList(adults);
            mergeList(babies);
        }
    }

    private void mergeList(@NotNull List<LivingEntity> list) {
        if (list.size() < 2) {
            return;
        }
        LivingEntity base = list.get(0);
        int count = getStackCount(base);
        for (int i = 1; i < list.size(); i++) {
            LivingEntity other = list.get(i);
            if (!sameStackGroup(base, other)) {
                continue;
            }
            if (count >= getMaxStackSize()) {
                break;
            }
            int add = getStackCount(other);
            if (count + add > getMaxStackSize()) {
                continue;
            }
            count += add;
            other.remove();
        }
        setStackCount(base, count);
    }

}
