package com.apexsions.core.container;

import com.apexsions.core.ApexsionsCorePlugin;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Bedrock-friendly container management: instant chest sorting and quick deposit.
 *
 * Bedrock clients have no middle-click, so both actions are exposed as simple
 * chat commands (/sort, /deposit) in addition to the optional sneak-interact
 * shortcut. The caller supplies the already-resolved container inventory.
 */
public class ContainerSortManager {

    private final ApexsionsCorePlugin plugin;

    /** Classification used to group items when sorting. */
    public enum Category {
        WEAPONS, TOOLS, ARMOR, POTIONS, FOOD, REDSTONE, BLOCKS, MISC
    }

    private static final Set<Material> WEAPONS = EnumSet.of(
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
            Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
            Material.TRIDENT, Material.BOW, Material.CROSSBOW, Material.MACE,
            Material.ARROW, Material.SPECTRAL_ARROW, Material.TIPPED_ARROW, Material.SHIELD);

    private static final Set<Material> TOOLS = EnumSet.of(
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
            Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
            Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE,
            Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE,
            Material.SHEARS, Material.FLINT_AND_STEEL, Material.FISHING_ROD,
            Material.BRUSH, Material.COMPASS, Material.RECOVERY_COMPASS, Material.CLOCK,
            Material.SPYGLASS, Material.LEAD, Material.NAME_TAG, Material.SADDLE);

    private static final Set<Material> ARMOR = EnumSet.of(
            Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
            Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
            Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,
            Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS,
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
            Material.TURTLE_HELMET, Material.ELYTRA,
            Material.LEATHER_HORSE_ARMOR, Material.IRON_HORSE_ARMOR,
            Material.GOLDEN_HORSE_ARMOR, Material.DIAMOND_HORSE_ARMOR,
            Material.WOLF_ARMOR);

    private static final Set<Material> POTIONS = EnumSet.of(
            Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION,
            Material.EXPERIENCE_BOTTLE, Material.HONEY_BOTTLE, Material.DRAGON_BREATH,
            Material.GLASS_BOTTLE, Material.BREWING_STAND);

    private static final Set<Material> REDSTONE = EnumSet.of(
            Material.REDSTONE, Material.REDSTONE_TORCH, Material.REDSTONE_BLOCK,
            Material.REDSTONE_LAMP, Material.REDSTONE_WIRE, Material.REPEATER,
            Material.COMPARATOR, Material.OBSERVER, Material.PISTON, Material.STICKY_PISTON,
            Material.DISPENSER, Material.DROPPER, Material.HOPPER, Material.LEVER,
            Material.TRIPWIRE_HOOK, Material.DAYLIGHT_DETECTOR, Material.TARGET,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.NOTE_BLOCK, Material.SCULK_SENSOR, Material.CALIBRATED_SCULK_SENSOR);

    public ContainerSortManager(@NotNull ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    // --- Configuration ---

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("container.enabled", true);
    }

    public boolean isSortEnabled() {
        return isEnabled() && plugin.getConfig().getBoolean("container.sort.enabled", true);
    }

    public boolean mergeStacks() {
        return plugin.getConfig().getBoolean("container.sort.merge-stacks", true);
    }

    public boolean isDepositEnabled() {
        return isEnabled() && plugin.getConfig().getBoolean("container.quick-deposit.enabled", true);
    }

    public boolean skipHeldSlot() {
        return plugin.getConfig().getBoolean("container.quick-deposit.skip-held-slot", true);
    }

    public boolean onlyExistingTypes() {
        return plugin.getConfig().getBoolean("container.quick-deposit.only-existing-types", true);
    }

    public boolean sneakInteractEnabled() {
        return isDepositEnabled() && plugin.getConfig().getBoolean("container.quick-deposit.sneak-interact", true);
    }

    // --- Container resolution ---

    /**
     * Resolves the container inventory the player currently has open, or null
     * when no sortable container is open.
     */
    @Nullable
    public Inventory getOpenContainer(@NotNull Player player) {
        Inventory top = player.getOpenInventory().getTopInventory();
        Object holder = top.getHolder();
        if (holder instanceof Container || holder instanceof DoubleChest) {
            return top;
        }
        return null;
    }

    @Nullable
    public Inventory getContainerAt(@NotNull BlockState state) {
        if (state instanceof Container container) {
            return container.getInventory();
        }
        return null;
    }

    // --- Sorting ---

    /**
     * Sorts and optionally merges the container contents. Returns moved/sorted
     * stack count, or -1 when nothing changed.
     */
    public int sort(@NotNull Inventory container) {
        int size = container.getSize();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ItemStack stack = container.getItem(i);
            if (stack != null && !stack.getType().isAir()) {
                items.add(stack);
            }
        }
        if (items.isEmpty()) {
            return -1;
        }

        if (mergeStacks()) {
            items = merge(items);
        }

        items.sort(Comparator
                .comparingInt((ItemStack s) -> categoryOf(s.getType()).ordinal())
                .thenComparing(s -> s.getType().name())
                .thenComparing(Comparator.comparingInt(ItemStack::getAmount).reversed()));

        ItemStack[] out = new ItemStack[size];
        for (int i = 0; i < items.size() && i < size; i++) {
            out[i] = items.get(i);
        }
        container.setContents(out);
        return items.size();
    }

    private List<ItemStack> merge(@NotNull List<ItemStack> input) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack stack : input) {
            int remaining = stack.getAmount();
            int max = stack.getMaxStackSize();
            for (ItemStack existing : merged) {
                if (remaining <= 0) {
                    break;
                }
                if (!existing.isSimilar(stack) || existing.getAmount() >= max) {
                    continue;
                }
                int space = max - existing.getAmount();
                int move = Math.min(space, remaining);
                existing.setAmount(existing.getAmount() + move);
                remaining -= move;
            }
            while (remaining > 0) {
                ItemStack copy = stack.clone();
                int chunk = Math.min(max, remaining);
                copy.setAmount(chunk);
                merged.add(copy);
                remaining -= chunk;
            }
        }
        return merged;
    }

    @NotNull
    public Category categoryOf(@NotNull Material material) {
        if (WEAPONS.contains(material)) return Category.WEAPONS;
        if (TOOLS.contains(material)) return Category.TOOLS;
        if (ARMOR.contains(material)) return Category.ARMOR;
        if (POTIONS.contains(material)) return Category.POTIONS;
        if (material.isEdible()) return Category.FOOD;
        if (REDSTONE.contains(material)) return Category.REDSTONE;
        if (material.isBlock()) return Category.BLOCKS;
        return Category.MISC;
    }

    // --- Quick deposit ---

    /**
     * Deposits matching items from the player's storage into the container.
     * When only-existing-types is on, items whose type is absent from the
     * container are left untouched. Returns the number of moved stacks.
     */
    public int quickDeposit(@NotNull Player player, @NotNull Inventory container) {
        Set<Material> present = EnumSet.noneOf(Material.class);
        for (ItemStack stack : container.getContents()) {
            if (stack != null && !stack.getType().isAir()) {
                present.add(stack.getType());
            }
        }

        PlayerInventory inv = player.getInventory();
        int heldSlot = inv.getHeldItemSlot();
        int moved = 0;

        ItemStack[] storage = inv.getStorageContents();
        for (int slot = 0; slot < storage.length; slot++) {
            ItemStack stack = storage[slot];
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            if (skipHeldSlot() && slot == heldSlot) {
                continue;
            }
            if (onlyExistingTypes() && !present.contains(stack.getType())) {
                continue;
            }

            Map<Integer, ItemStack> leftover = container.addItem(stack.clone());
            if (leftover.isEmpty()) {
                inv.setItem(slot, null);
                moved++;
            } else {
                ItemStack rest = leftover.values().iterator().next();
                if (rest == null || rest.getType().isAir()) {
                    inv.setItem(slot, null);
                    moved++;
                } else if (rest.getAmount() < stack.getAmount()) {
                    inv.setItem(slot, rest);
                    moved++;
                }
            }
        }
        return moved;
    }

    /** Human-readable category label for feedback messages. */
    @NotNull
    public String categoryLabel(@NotNull Category category) {
        String name = category.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}