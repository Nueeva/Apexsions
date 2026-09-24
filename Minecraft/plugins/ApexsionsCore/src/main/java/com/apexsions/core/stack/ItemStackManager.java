package com.apexsions.core.stack;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dropped Item Stacking & Hologram Engine.
 *
 * Merges dropped items on the ground into a single entity even when exceeding vanilla's
 * max stack size (e.g. 64 + 64 cobblestone becomes a single dropped item with count 128).
 * Displays a clean, native floating hologram showing block/item name and quantity.
 */
public class ItemStackManager {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final NamespacedKey countKey;

    private BukkitTask sweepTask;
    private final Set<Material> ignoredMaterials = ConcurrentHashMap.newKeySet();

    public ItemStackManager(@NotNull ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        this.countKey = new NamespacedKey(plugin, "item_stack_count");
        loadIgnoredMaterials();
    }

    public ApexsionsCorePlugin getPlugin() {
        return plugin;
    }

    public NamespacedKey getCountKey() {
        return countKey;
    }

    // --- Configuration ---

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("item-stacking.enabled", true);
    }

    public double getMergeRadius() {
        return Math.max(0.5, plugin.getConfig().getDouble("item-stacking.merge-radius", 3.5));
    }

    public int getMaxStackSize() {
        return Math.max(1, plugin.getConfig().getInt("item-stacking.max-stack-size", 10000));
    }

    public boolean isMergeOnSpawn() {
        return plugin.getConfig().getBoolean("item-stacking.merge-on-spawn", true);
    }

    public boolean isShowSingle() {
        return plugin.getConfig().getBoolean("item-stacking.show-single", true);
    }

    public boolean isShowCountOnSingle() {
        return plugin.getConfig().getBoolean("item-stacking.display.show-count-on-single", true);
    }

    public boolean isStackOnlyStackables() {
        return plugin.getConfig().getBoolean("item-stacking.stack-only-stackables", true);
    }

    public long getScanIntervalTicks() {
        return Math.max(10L, plugin.getConfig().getLong("item-stacking.scan-interval-ticks", 40L));
    }

    public int getScanEntitiesPerPass() {
        return Math.max(100, plugin.getConfig().getInt("item-stacking.scan-entities-per-pass", 2000));
    }

    public List<String> getWorlds() {
        List<String> worlds = plugin.getConfig().getStringList("item-stacking.worlds");
        return worlds != null ? worlds : Collections.emptyList();
    }

    public boolean isWorldEnabled(@NotNull World world) {
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

    public void loadIgnoredMaterials() {
        ignoredMaterials.clear();
        List<String> list = plugin.getConfig().getStringList("item-stacking.ignored-materials");
        if (list != null) {
            for (String raw : list) {
                if (raw == null) continue;
                Material mat = Material.matchMaterial(raw.trim().toUpperCase(Locale.ROOT));
                if (mat != null) {
                    ignoredMaterials.add(mat);
                }
            }
        }
    }

    public boolean isIgnoredMaterial(@NotNull Material material) {
        return ignoredMaterials.contains(material);
    }

    // --- Lifecycle ---

    public void start() {
        if (!isEnabled()) {
            return;
        }
        loadIgnoredMaterials();
        startSweepTask();
    }

    public void stop() {
        if (sweepTask != null) {
            sweepTask.cancel();
            sweepTask = null;
        }
    }

    public void reload() {
        stop();
        start();
    }

    private void startSweepTask() {
        if (sweepTask != null) {
            sweepTask.cancel();
        }
        sweepTask = Bukkit.getScheduler().runTaskTimer(plugin, this::runSweep, getScanIntervalTicks(), getScanIntervalTicks());
    }

    // --- State & Hologram Management ---

    /**
     * Retrieves the true virtual stack count of an Item entity.
     */
    public int getStackCount(@NotNull Item item) {
        PersistentDataContainer pdc = item.getPersistentDataContainer();
        Integer count = pdc.get(countKey, PersistentDataType.INTEGER);
        if (count != null && count > 0) {
            return count;
        }
        return Math.max(1, item.getItemStack().getAmount());
    }

    /**
     * Initializes an item on spawn: records its current count and displays the hologram.
     */
    public void initItem(@NotNull Item item) {
        if (!item.isValid() || item.isDead()) {
            return;
        }
        PersistentDataContainer pdc = item.getPersistentDataContainer();
        if (!pdc.has(countKey, PersistentDataType.INTEGER)) {
            int initialAmount = Math.max(1, item.getItemStack().getAmount());
            pdc.set(countKey, PersistentDataType.INTEGER, initialAmount);
        }
        updateHologram(item);
    }

    /**
     * Sets the true virtual stack count of an Item entity and synchronizes the display.
     */
    public void setStackCount(@NotNull Item item, int count) {
        if (count <= 0) {
            item.remove();
            return;
        }
        item.getPersistentDataContainer().set(countKey, PersistentDataType.INTEGER, count);

        // Keep internal ItemStack amount within vanilla bounds for safe client-side mesh rendering
        ItemStack stack = item.getItemStack();
        int visualAmount = Math.min(count, stack.getMaxStackSize());
        if (stack.getAmount() != visualAmount) {
            stack.setAmount(visualAmount);
            item.setItemStack(stack);
        }

        updateHologram(item);
    }

    /**
     * Updates the native floating nametag/hologram above the item.
     */
    public void updateHologram(@NotNull Item item) {
        if (!item.isValid() || item.isDead()) {
            return;
        }
        if (!isEnabled()) {
            item.setCustomNameVisible(false);
            return;
        }

        int count = getStackCount(item);
        if (count <= 1 && !isShowSingle()) {
            item.customName(null);
            item.setCustomNameVisible(false);
            return;
        }

        ItemStack stack = item.getItemStack();
        String nameStr;
        ItemMeta meta = stack.getItemMeta();
        if (meta != null && meta.hasDisplayName() && meta.displayName() != null) {
            nameStr = mm.serialize(meta.displayName());
        } else {
            nameStr = formatMaterialName(stack.getType());
        }

        String template;
        if (count <= 1 && !isShowCountOnSingle()) {
            template = plugin.getConfig().getString("item-stacking.display.single-format", "<gold>%name%</gold>");
        } else {
            template = plugin.getConfig().getString("item-stacking.display.name-format", "<gold>%name%</gold> <yellow>x%count%</yellow>");
        }
        if (template == null) {
            template = "<gold>%name%</gold> <yellow>x%count%</yellow>";
        }

        String formatted = template.replace("%name%", nameStr).replace("%count%", String.valueOf(count));
        Component customName = mm.deserialize(formatted);

        item.customName(customName);
        item.setCustomNameVisible(true);
    }

    /**
     * Formats a Bukkit Material enum into title case (e.g. DEEPSLATE_IRON_ORE -> Deepslate Iron Ore).
     */
    public static String formatMaterialName(@NotNull Material material) {
        String raw = material.name().toLowerCase(Locale.ROOT);
        String[] parts = raw.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
            if (i < parts.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    // --- Stacking / Merging Logic ---

    public boolean isStackable(@NotNull Item item) {
        if (!item.isValid() || item.isDead()) {
            return false;
        }
        if (!isWorldEnabled(item.getWorld())) {
            return false;
        }
        Material mat = item.getItemStack().getType();
        if (mat.isAir() || isIgnoredMaterial(mat)) {
            return false;
        }
        if (isStackOnlyStackables() && item.getItemStack().getMaxStackSize() <= 1) {
            return false;
        }
        return true;
    }

    public boolean canMerge(@NotNull Item a, @NotNull Item b) {
        if (!isStackable(a) || !isStackable(b)) {
            return false;
        }
        if (!a.getWorld().equals(b.getWorld())) {
            return false;
        }
        return a.getItemStack().isSimilar(b.getItemStack());
    }

    /**
     * Merges source item into target item, combining their counts uncapped.
     */
    public void mergeItems(@NotNull Item target, @NotNull Item source) {
        if (!target.isValid() || !source.isValid() || target.isDead() || source.isDead()) {
            return;
        }
        if (target.equals(source)) {
            return;
        }

        int targetCount = getStackCount(target);
        int sourceCount = getStackCount(source);
        int maxStack = getMaxStackSize();

        if (targetCount >= maxStack) {
            return;
        }

        int total = targetCount + sourceCount;
        if (total > maxStack) {
            int absorbed = maxStack - targetCount;
            setStackCount(target, maxStack);
            setStackCount(source, sourceCount - absorbed);
            return;
        }

        // Fully absorb source into target
        setStackCount(target, total);
        target.setPickupDelay(Math.max(target.getPickupDelay(), source.getPickupDelay()));
        target.setTicksLived(Math.max(1, Math.min(target.getTicksLived(), source.getTicksLived())));
        source.remove();
    }

    /**
     * Attempts to merge a spawned item into nearby compatible dropped items.
     */
    public void tryMergeNearby(@NotNull Item item) {
        if (!item.isValid() || item.isDead() || !isStackable(item)) {
            return;
        }
        double radius = getMergeRadius();
        for (Entity nearby : item.getNearbyEntities(radius, radius, radius)) {
            if (!(nearby instanceof Item other)) {
                continue;
            }
            if (other.equals(item) || !other.isValid() || other.isDead()) {
                continue;
            }
            if (!canMerge(item, other)) {
                continue;
            }
            mergeItems(item, other);
            if (getStackCount(item) >= getMaxStackSize()) {
                break;
            }
        }
    }

    /**
     * Periodic sweep to group and merge dropped items within active chunks.
     */
    public void runSweep() {
        if (!isEnabled()) {
            return;
        }
        int budget = getScanEntitiesPerPass();
        int processed = 0;
        double radius = getMergeRadius();
        double radiusSq = radius * radius;

        for (World world : Bukkit.getWorlds()) {
            if (!isWorldEnabled(world)) {
                continue;
            }
            Collection<Item> items = world.getEntitiesByClass(Item.class);
            if (items.size() < 2) {
                // Ensure single items still have up-to-date holograms if needed
                for (Item item : items) {
                    if (item.isValid() && !item.isDead()) {
                        updateHologram(item);
                    }
                }
                continue;
            }

            Map<Long, List<Item>> byChunk = new HashMap<>();
            for (Item item : items) {
                if (!item.isValid() || item.isDead()) {
                    continue;
                }
                updateHologram(item);
                if (!isStackable(item)) {
                    continue;
                }
                processed++;
                long chunkKey = (((long) item.getLocation().getBlockX() >> 4) << 32)
                        ^ ((long) item.getLocation().getBlockZ() >> 4);
                byChunk.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(item);
                if (processed >= budget) {
                    break;
                }
            }

            for (List<Item> chunkItems : byChunk.values()) {
                if (chunkItems.size() < 2) {
                    continue;
                }
                for (int i = 0; i < chunkItems.size(); i++) {
                    Item base = chunkItems.get(i);
                    if (!base.isValid() || base.isDead()) {
                        continue;
                    }
                    for (int j = i + 1; j < chunkItems.size(); j++) {
                        Item other = chunkItems.get(j);
                        if (!other.isValid() || other.isDead()) {
                            continue;
                        }
                        if (base.getLocation().distanceSquared(other.getLocation()) <= radiusSq) {
                            if (canMerge(base, other)) {
                                mergeItems(base, other);
                                if (getStackCount(base) >= getMaxStackSize()) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (processed >= budget) {
                break;
            }
        }
    }
}
