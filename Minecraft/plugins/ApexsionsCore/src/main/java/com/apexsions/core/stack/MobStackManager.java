package com.apexsions.core.stack;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Mob;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
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
 * Preserves vanilla drops, XP rates, mob traits, variants, baby statuses, and
 * prevents cross-elevation or chunk sweep elevation anomalies.
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

    public ApexsionsCorePlugin getPlugin() {
        return plugin;
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

    public boolean isKillAllOnSneak() {
        return plugin.getConfig().getBoolean("mob-stacking.kill-all-on-sneak", true);
    }

    public boolean isKillStackOnFall() {
        return plugin.getConfig().getBoolean("mob-stacking.kill-stack-on-fall", true);
    }

    public boolean isInstantKillOnVoid() {
        return plugin.getConfig().getBoolean("mob-stacking.instant-kill-on-void", true);
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

    public String formatTypeName(@NotNull EntityType type) {
        String pretty = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        if (!pretty.isEmpty()) {
            return Character.toUpperCase(pretty.charAt(0)) + pretty.substring(1);
        }
        return pretty;
    }

    private Component buildName(@NotNull EntityType type, int count) {
        String raw = plugin.getConfig().getString("mob-stacking.display.name-format",
                "<gray>%type% <yellow>[x%count%]</yellow>");
        if (raw == null) {
            raw = "<gray>%type% <yellow>[x%count%]</yellow>";
        }
        String pretty = formatTypeName(type);
        String text = raw.replace("%type%", pretty).replace("%count%", String.valueOf(count));
        return mm.deserialize(text);
    }

    public boolean isStackable(@NotNull Entity entity) {
        if (!entity.isValid() || entity.isDead()) {
            return false;
        }
        if (!(entity instanceof Mob mob) || entity instanceof Boss) {
            return false;
        }
        if (entity instanceof Player || (entity instanceof Tameable tameable && tameable.isTamed())) {
            return false;
        }
        if (!mob.getPassengers().isEmpty() || mob.isInsideVehicle()) {
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
            if (mob.getEquipment().getItemInOffHand() != null && !mob.getEquipment().getItemInOffHand().getType().isAir()) {
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

    public boolean sameStackGroup(@NotNull Entity a, @NotNull Entity b) {
        if (!a.isValid() || a.isDead() || !b.isValid() || b.isDead()) {
            return false;
        }
        if (a.getType() != b.getType()) {
            return false;
        }
        if (!isStackable(a) || !isStackable(b)) {
            return false;
        }
        if (a instanceof Ageable aa && b instanceof Ageable bb) {
            if (aa.isAdult() != bb.isAdult()) {
                return false;
            }
        }
        if (a instanceof Zombie za && b instanceof Zombie zb) {
            if (za.isBaby() != zb.isBaby()) {
                return false;
            }
        }
        if (a instanceof Piglin pa && b instanceof Piglin pb) {
            if (pa.isBaby() != pb.isBaby()) {
                return false;
            }
        }
        if (a instanceof Slime sa && b instanceof Slime sb) {
            if (sa.getSize() != sb.getSize()) {
                return false;
            }
        }
        if (a instanceof Sheep sa && b instanceof Sheep sb) {
            if (sa.getColor() != sb.getColor() || sa.isSheared() != sb.isSheared()) {
                return false;
            }
        }
        if (a instanceof MushroomCow ma && b instanceof MushroomCow mb) {
            if (ma.getVariant() != mb.getVariant()) {
                return false;
            }
        }
        if (a instanceof Creeper ca && b instanceof Creeper cb) {
            if (ca.isPowered() != cb.isPowered()) {
                return false;
            }
        }
        if (a instanceof Villager va && b instanceof Villager vb) {
            if (va.getVillagerType() != vb.getVillagerType() || va.getProfession() != vb.getProfession() || va.getVillagerLevel() != vb.getVillagerLevel()) {
                return false;
            }
        }
        if (a instanceof ZombieVillager zva && b instanceof ZombieVillager zvb) {
            if (zva.getVillagerType() != zvb.getVillagerType() || zva.getVillagerProfession() != zvb.getVillagerProfession()) {
                return false;
            }
        }
        if (a instanceof Parrot pa && b instanceof Parrot pb) {
            if (pa.getVariant() != pb.getVariant()) {
                return false;
            }
        }
        if (a instanceof Cat ca && b instanceof Cat cb) {
            if (ca.getCatType() != cb.getCatType() || ca.getCollarColor() != cb.getCollarColor()) {
                return false;
            }
        }
        if (a instanceof Wolf wa && b instanceof Wolf wb) {
            if (wa.getCollarColor() != wb.getCollarColor() || wa.getVariant() != wb.getVariant()) {
                return false;
            }
        }
        if (a instanceof Horse ha && b instanceof Horse hb) {
            if (ha.getColor() != hb.getColor() || ha.getStyle() != hb.getStyle()) {
                return false;
            }
        }
        if (a instanceof Llama la && b instanceof Llama lb) {
            if (la.getColor() != lb.getColor()) {
                return false;
            }
        }
        if (a instanceof Fox fa && b instanceof Fox fb) {
            if (fa.getFoxType() != fb.getFoxType()) {
                return false;
            }
        }
        if (a instanceof Frog fa && b instanceof Frog fb) {
            if (fa.getVariant() != fb.getVariant()) {
                return false;
            }
        }
        if (a instanceof Axolotl aa && b instanceof Axolotl ab) {
            if (aa.getVariant() != ab.getVariant()) {
                return false;
            }
        }
        if (a instanceof Rabbit ra && b instanceof Rabbit rb) {
            if (ra.getRabbitType() != rb.getRabbitType()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Replicates all attributes, color, baby status, and size from a deceased or split mob
     * onto its newly spawned replacement entity.
     */
    public void copyEntityState(@NotNull LivingEntity source, @NotNull LivingEntity target) {
        if (source instanceof Ageable sa && target instanceof Ageable ta) {
            if (sa.isAdult()) {
                ta.setAdult();
            } else {
                ta.setBaby();
            }
            ta.setAgeLock(sa.getAgeLock());
        }
        if (source instanceof Zombie sz && target instanceof Zombie tz) {
            tz.setBaby(sz.isBaby());
        }
        if (source instanceof Piglin sp && target instanceof Piglin tp) {
            tp.setBaby(sp.isBaby());
        }
        if (source instanceof Slime ss && target instanceof Slime ts) {
            ts.setSize(ss.getSize());
        }
        if (source instanceof Sheep ss && target instanceof Sheep ts) {
            ts.setColor(ss.getColor());
            ts.setSheared(ss.isSheared());
        }
        if (source instanceof MushroomCow sm && target instanceof MushroomCow tm) {
            tm.setVariant(sm.getVariant());
        }
        if (source instanceof Creeper sc && target instanceof Creeper tc) {
            tc.setPowered(sc.isPowered());
        }
        if (source instanceof Villager sv && target instanceof Villager tv) {
            tv.setVillagerType(sv.getVillagerType());
            tv.setProfession(sv.getProfession());
            tv.setVillagerLevel(sv.getVillagerLevel());
        }
        if (source instanceof ZombieVillager szv && target instanceof ZombieVillager tzv) {
            tzv.setVillagerType(szv.getVillagerType());
            tzv.setVillagerProfession(szv.getVillagerProfession());
        }
        if (source instanceof Parrot sp && target instanceof Parrot tp) {
            tp.setVariant(sp.getVariant());
        }
        if (source instanceof Cat sc && target instanceof Cat tc) {
            tc.setCatType(sc.getCatType());
            tc.setCollarColor(sc.getCollarColor());
        }
        if (source instanceof Wolf sw && target instanceof Wolf tw) {
            tw.setCollarColor(sw.getCollarColor());
            tw.setVariant(sw.getVariant());
        }
        if (source instanceof Horse sh && target instanceof Horse th) {
            th.setColor(sh.getColor());
            th.setStyle(sh.getStyle());
        }
        if (source instanceof Llama sl && target instanceof Llama tl) {
            tl.setColor(sl.getColor());
        }
        if (source instanceof Fox sf && target instanceof Fox tf) {
            tf.setFoxType(sf.getFoxType());
        }
        if (source instanceof Frog sf && target instanceof Frog tf) {
            tf.setVariant(sf.getVariant());
        }
        if (source instanceof Axolotl sa && target instanceof Axolotl ta) {
            ta.setVariant(sa.getVariant());
        }
        if (source instanceof Rabbit sr && target instanceof Rabbit tr) {
            tr.setRabbitType(sr.getRabbitType());
        }
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
        if (!spawned.isValid() || spawned.isDead()) {
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

    public boolean isVoidDeath(@NotNull LivingEntity entity) {
        Location loc = entity.getLocation();
        World world = loc.getWorld();
        if (world != null && loc.getY() < world.getMinHeight()) {
            return true;
        }
        EntityDamageEvent lastDamage = entity.getLastDamageCause();
        return lastDamage != null && lastDamage.getCause() == EntityDamageEvent.DamageCause.VOID;
    }

    /**
     * Scales loot drops and experience for the entire remaining stack when bulk-slain.
     */
    public void dropRemainingLootAndExp(@NotNull LivingEntity dead, int remainingUnits, @NotNull EntityDeathEvent event) {
        if (remainingUnits <= 0) {
            return;
        }
        // Scale dropped XP
        int baseExp = event.getDroppedExp();
        if (baseExp > 0) {
            event.setDroppedExp(baseExp * (remainingUnits + 1));
        }

        // Scale drops
        List<ItemStack> drops = event.getDrops();
        if (!drops.isEmpty()) {
            List<ItemStack> extraDrops = new ArrayList<>();
            for (ItemStack drop : drops) {
                if (drop == null || drop.getType().isAir()) {
                    continue;
                }
                int totalToAdd = drop.getAmount() * remainingUnits;
                int maxStack = drop.getMaxStackSize();
                while (totalToAdd > 0) {
                    int batch = Math.min(totalToAdd, maxStack);
                    ItemStack clone = drop.clone();
                    clone.setAmount(batch);
                    extraDrops.add(clone);
                    totalToAdd -= batch;
                }
            }
            drops.addAll(extraDrops);
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
        if (isInstantKillOnVoid() && isVoidDeath(dead)) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!world.isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                world.getChunkAt(loc);
            }
            try {
                Class<? extends Entity> entityClass = dead.getType().getEntityClass();
                if (entityClass != null && Mob.class.isAssignableFrom(entityClass)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Mob> mobClass = (Class<? extends Mob>) entityClass;
                    // Consumer executes BEFORE CreatureSpawnEvent, ensuring PDC count and state are set.
                    world.spawn(loc, mobClass, CreatureSpawnEvent.SpawnReason.CUSTOM, rep -> {
                        setStackCount(rep, remaining);
                        copyEntityState(dead, rep);
                    });
                }
            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "Failed spawning mob stack replacement for " + dead.getType(), t);
                world.spawn(loc, ExperienceOrb.class, orb -> orb.setExperience(remaining));
            }
        });
    }

    // --- Periodic sweep ---

    /**
     * Batched, capped sweep that merges already-existing mob crowds (large farms
     * that predate the feature). Groups entities per chunk with strict radius checks.
     */
    private void runSweep() {
        if (!isEnabled()) {
            return;
        }
        int budget = getScanEntitiesPerPass();
        int processed = 0;
        double radius = getMergeRadius();

        for (World world : Bukkit.getWorlds()) {
            if (!isWorldEnabled(world)) {
                continue;
            }
            List<LivingEntity> candidates = new ArrayList<>();
            for (LivingEntity living : world.getLivingEntities()) {
                if (living instanceof Player || !living.isValid() || living.isDead()) {
                    continue;
                }
                candidates.add(living);
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
                mergeGroup(group, radius);
            }

            if (processed >= budget) {
                return;
            }
        }
    }

    private void mergeGroup(@NotNull List<LivingEntity> group, double radius) {
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
                } else if (living instanceof Zombie zombie && zombie.isBaby()) {
                    babies.add(living);
                } else if (living instanceof Piglin piglin && piglin.isBaby()) {
                    babies.add(living);
                } else {
                    adults.add(living);
                }
            }
            mergeList(adults, radius);
            mergeList(babies, radius);
        }
    }

    private void mergeList(@NotNull List<LivingEntity> list, double radius) {
        if (list.size() < 2) {
            return;
        }
        double radiusSq = radius * radius;
        int maxStack = getMaxStackSize();

        for (int i = 0; i < list.size(); i++) {
            LivingEntity base = list.get(i);
            if (!base.isValid() || base.isDead()) {
                continue;
            }
            int count = getStackCount(base);
            if (count >= maxStack) {
                continue;
            }

            for (int j = i + 1; j < list.size(); j++) {
                LivingEntity other = list.get(j);
                if (!other.isValid() || other.isDead()) {
                    continue;
                }
                // Strict Euclidean distance check prevents cross-elevation or cross-room merging
                if (base.getLocation().distanceSquared(other.getLocation()) > radiusSq) {
                    continue;
                }
                if (!sameStackGroup(base, other)) {
                    continue;
                }
                int otherCount = getStackCount(other);
                int space = maxStack - count;
                if (space <= 0) {
                    break;
                }

                if (otherCount <= space) {
                    count += otherCount;
                    other.remove();
                } else {
                    count += space;
                    setStackCount(other, otherCount - space);
                    break;
                }
            }
            setStackCount(base, count);
        }
    }

}
