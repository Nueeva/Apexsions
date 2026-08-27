package com.apex.battlepass.admin.gui.quest;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.navigation.CloseButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.quest.model.QuestObjectiveType;
import com.apex.battlepass.util.ItemSerializer;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AdminQuestTargetPickerMenu extends Gui {

    public interface TargetCallback {
        void onSelect(EntityType entity, Material block, Material item);
    }

    private final QuestObjectiveType objectiveType;
    private final TargetCallback callback;

    private static final int[] CENTER_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public AdminQuestTargetPickerMenu(ApexsionsBattlepass plugin, Player player, QuestObjectiveType objectiveType, Gui parent, TargetCallback callback) {
        super(plugin, player, "&8[ &4&lTARGET: &e" + objectiveType.name() + " &8]", 54, parent);
        this.objectiveType = objectiveType;
        this.callback = callback;
    }

    @Override
    public void initialize() {
        fillBorder();

        // 1. Header Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.TARGET)
                .name("&6&lPILIH TARGET UNTUK: &e" + objectiveType.name())
                .lore(List.of(
                        "&7Daftar target yang relevan dengan tipe objektif ini.",
                        "&7Klik salah satu target di bawah untuk memilih."
                ))
                .build()));

        // Slot 10 is ALWAYS "ANY / SEMUA (Bebas)"
        setButton(10, new GuiButton(new ItemBuilder(Material.NETHER_STAR)
                .name("&e&l[★] SEMUA / APA SAJA (BEBAS)")
                .lore(List.of(
                        "&7Pemain dapat menyelesaikan quest menggunakan",
                        "&7target apa pun yang sesuai tipe objektif.",
                        " ",
                        "&aKlik untuk memilih target bebas >"
                ))
                .build(), event -> {
            if (callback != null) callback.onSelect(null, null, null);
            if (parent != null) parent.open();
        }));

        int slotIdx = 1; // start after slot 10

        // Populate targets based on objectiveType
        if (isEntityObjective(objectiveType)) {
            // Entity List
            EntityType[] entities = {
                    EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER,
                    EntityType.ENDERMAN, EntityType.BLAZE, EntityType.WITHER_SKELETON, EntityType.DROWNED,
                    EntityType.HUSK, EntityType.STRAY, EntityType.WITCH, EntityType.SLIME,
                    EntityType.MAGMA_CUBE, EntityType.GHAST, EntityType.PHANTOM, EntityType.RAVAGER,
                    EntityType.PILLAGER, EntityType.EVOKER, EntityType.WITHER, EntityType.ENDER_DRAGON,
                    EntityType.COW, EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN,
                    EntityType.WOLF, EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.VILLAGER
            };

            for (EntityType et : entities) {
                if (slotIdx >= CENTER_SLOTS.length) break;

                Material icon = getEntityIcon(et);
                int targetSlot = CENTER_SLOTS[slotIdx++];

                setButton(targetSlot, new GuiButton(new ItemBuilder(icon)
                        .name("&e&l" + formatEnumName(et.name()))
                        .lore(List.of(
                                "&7Tipe: &fEntity / Mob",
                                "&7ID: &8" + et.name(),
                                " ",
                                "&aKlik untuk memilih target mob ini >"
                        ))
                        .build(), event -> {
                    if (callback != null) callback.onSelect(et, null, null);
                    if (parent != null) parent.open();
                }));
            }
        } else if (isBlockObjective(objectiveType)) {
            // Block List
            Material[] blocks = {
                    Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.ANCIENT_DEBRIS,
                    Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE, Material.GOLD_ORE,
                    Material.COAL_ORE, Material.COPPER_ORE, Material.EMERALD_ORE,
                    Material.LAPIS_ORE, Material.REDSTONE_ORE, Material.NETHER_QUARTZ_ORE,
                    Material.STONE, Material.COBBLESTONE, Material.DEEPSLATE,
                    Material.OBSIDIAN, Material.OAK_LOG, Material.BIRCH_LOG,
                    Material.SPRUCE_LOG, Material.DARK_OAK_LOG, Material.DIRT,
                    Material.GRASS_BLOCK, Material.NETHERRACK, Material.END_STONE,
                    Material.AMETHYST_BLOCK, Material.RAW_IRON_BLOCK, Material.BOOKSHELF
            };

            for (Material mat : blocks) {
                if (slotIdx >= CENTER_SLOTS.length) break;

                int targetSlot = CENTER_SLOTS[slotIdx++];
                setButton(targetSlot, new GuiButton(new ItemBuilder(mat)
                        .name("&e&l" + ItemSerializer.formatMaterialName(mat))
                        .lore(List.of(
                                "&7Tipe: &fBlok",
                                "&7ID: &8" + mat.name(),
                                " ",
                                "&aKlik untuk memilih target blok ini >"
                        ))
                        .build(), event -> {
                    if (callback != null) callback.onSelect(null, mat, null);
                    if (parent != null) parent.open();
                }));
            }
        } else {
            // Item List (Craft, Smelt, Enchant, Fish, Crops, Food, etc.)
            Material[] items = {
                    Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND, Material.NETHERITE_INGOT,
                    Material.COPPER_INGOT, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
                    Material.BOW, Material.CROSSBOW, Material.SHIELD, Material.DIAMOND_PICKAXE,
                    Material.NETHERITE_PICKAXE, Material.DIAMOND_AXE, Material.CHEST, Material.HOPPER,
                    Material.ANVIL, Material.PISTON, Material.BEACON, Material.GOLDEN_APPLE,
                    Material.ENCHANTED_GOLDEN_APPLE, Material.BREAD, Material.COOKED_BEEF, Material.COOKED_PORKCHOP,
                    Material.POTION, Material.COD, Material.SALMON, Material.WHEAT
            };

            for (Material mat : items) {
                if (slotIdx >= CENTER_SLOTS.length) break;

                int targetSlot = CENTER_SLOTS[slotIdx++];
                setButton(targetSlot, new GuiButton(new ItemBuilder(mat)
                        .name("&e&l" + ItemSerializer.formatMaterialName(mat))
                        .lore(List.of(
                                "&7Tipe: &fItem",
                                "&7ID: &8" + mat.name(),
                                " ",
                                "&aKlik untuk memilih target item ini >"
                        ))
                        .build(), event -> {
                    if (callback != null) callback.onSelect(null, null, mat);
                    if (parent != null) parent.open();
                }));
            }
        }

        // Navigation (Row 5)
        setButton(45, new BackButton(this, parent));
        setButton(53, new CloseButton());
    }

    private static boolean isEntityObjective(QuestObjectiveType type) {
        return type == QuestObjectiveType.KILL_ENTITY || type == QuestObjectiveType.BREED_ANIMALS;
    }

    private static boolean isBlockObjective(QuestObjectiveType type) {
        return type == QuestObjectiveType.MINE_BLOCK || type == QuestObjectiveType.BREAK_BLOCK || type == QuestObjectiveType.PLACE_BLOCK;
    }

    private static Material getEntityIcon(EntityType type) {
        return switch (type) {
            case ZOMBIE -> Material.ZOMBIE_HEAD;
            case SKELETON -> Material.SKELETON_SKULL;
            case CREEPER -> Material.CREEPER_HEAD;
            case SPIDER -> Material.SPIDER_EYE;
            case ENDERMAN -> Material.ENDER_PEARL;
            case BLAZE -> Material.BLAZE_ROD;
            case WITHER_SKELETON -> Material.WITHER_SKELETON_SKULL;
            case DROWNED -> Material.NAUTILUS_SHELL;
            case GHAST -> Material.GHAST_TEAR;
            case PHANTOM -> Material.PHANTOM_MEMBRANE;
            case WITHER -> Material.NETHER_STAR;
            case ENDER_DRAGON -> Material.DRAGON_HEAD;
            case COW -> Material.LEATHER;
            case SHEEP -> Material.WHITE_WOOL;
            case PIG -> Material.PORKCHOP;
            case CHICKEN -> Material.FEATHER;
            case WOLF -> Material.BONE;
            case HORSE -> Material.SADDLE;
            case IRON_GOLEM -> Material.IRON_BLOCK;
            default -> Material.PIG_SPAWN_EGG;
        };
    }

    private static String formatEnumName(String name) {
        if (name == null) return "Unknown";
        String[] parts = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
