package com.apex.battlepass.admin.gui.quest;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.navigation.CloseButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.quest.model.QuestObjectiveType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;

public class AdminQuestTypePickerMenu extends Gui {

    private final Consumer<QuestObjectiveType> onSelect;

    public AdminQuestTypePickerMenu(ApexsionsBattlepass plugin, Player player, Gui parent, Consumer<QuestObjectiveType> onSelect) {
        super(plugin, player, "&8[ &4&lPILIH TIPE OBJEKTIF QUEST &8]", 54, parent);
        this.onSelect = onSelect;
    }

    @Override
    public void initialize() {
        fillBorder();

        // Header Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.COMPASS)
                .name("&6&lPILIH TIPE OBJEKTIF QUEST")
                .lore(List.of(
                        "&7Klik pada salah satu tipe objektif di bawah.",
                        "&7Setelah dipilih, daftar target akan disesuaikan otomatis."
                ))
                .build()));

        // Grid of Objective Types
        addTypeButton(10, QuestObjectiveType.MINE_BLOCK, Material.DIAMOND_PICKAXE, "&b&lMINE BLOCK (MENAMBANG)",
                "&7Menambang ore atau blok batu tertentu.");

        addTypeButton(11, QuestObjectiveType.BREAK_BLOCK, Material.IRON_PICKAXE, "&e&lBREAK BLOCK (MENGHANCURKAN)",
                "&7Menghancurkan blok apa saja di dunia.");

        addTypeButton(12, QuestObjectiveType.PLACE_BLOCK, Material.BRICKS, "&a&lPLACE BLOCK (MEMASANG)",
                "&7Memasang blok tertentu saat membangun.");

        addTypeButton(13, QuestObjectiveType.KILL_ENTITY, Material.DIAMOND_SWORD, "&c&lKILL ENTITY (BUNUH MONSTER/MOB)",
                "&7Membunuh jenis monster atau hewan tertentu.");

        addTypeButton(14, QuestObjectiveType.KILL_PLAYER, Material.NETHERITE_SWORD, "&4&lKILL PLAYER (PVP)",
                "&7Membunuh player lain dalam pertarungan.");

        addTypeButton(15, QuestObjectiveType.CRAFT_ITEM, Material.CRAFTING_TABLE, "&6&lCRAFT ITEM (MEMBUAT BARANG)",
                "&7Membuat item menggunakan Crafting Table.");

        addTypeButton(16, QuestObjectiveType.SMELT_ITEM, Material.BLAST_FURNACE, "&e&lSMELT ITEM (MELEBUR / MASAK)",
                "&7Melebur ore atau memasak makanan di Furnace.");

        addTypeButton(19, QuestObjectiveType.FISH, Material.FISHING_ROD, "&9&lFISH (MEMANCING)",
                "&7Memancing ikan atau harta karun dari air.");

        addTypeButton(20, QuestObjectiveType.BREED_ANIMALS, Material.WHEAT, "&a&lBREED ANIMALS (BIAKKAN HEWAN)",
                "&7Mengawinkan hewan ternak (sapi, domba, dll).");

        addTypeButton(21, QuestObjectiveType.HARVEST_CROPS, Material.HAY_BLOCK, "&e&lHARVEST CROPS (PANEN TANAMAN)",
                "&7Memanen hasil kebun yang sudah matang.");

        addTypeButton(22, QuestObjectiveType.PLANT_CROPS, Material.WHEAT_SEEDS, "&2&lPLANT CROPS (MENANAM)",
                "&7Menanam benih tanaman di lahan pertanian.");

        addTypeButton(23, QuestObjectiveType.ENCHANT_ITEM, Material.ENCHANTING_TABLE, "&d&lENCHANT ITEM (ENCHANT)",
                "&7Melakukan enchant pada senjata atau armor.");

        addTypeButton(24, QuestObjectiveType.VILLAGER_TRADE, Material.EMERALD, "&a&lVILLAGER TRADE (BERDAGANG)",
                "&7Melakukan transaksi dengan penduduk desa.");

        addTypeButton(25, QuestObjectiveType.CONSUME_FOOD, Material.GOLDEN_APPLE, "&6&lCONSUME FOOD (MAKAN)",
                "&7Memakan makanan atau potion tertentu.");

        addTypeButton(28, QuestObjectiveType.EXP_GAIN, Material.EXPERIENCE_BOTTLE, "&a&lEXP GAIN (KUMPULKAN EXP)",
                "&7Mendapatkan poin experience (XP).");

        addTypeButton(29, QuestObjectiveType.TRAVEL_DISTANCE, Material.ELYTRA, "&b&lTRAVEL (MENEMPUH JARAK)",
                "&7Berjalan, berlari, atau terbang sejauh X blok.");

        addTypeButton(30, QuestObjectiveType.CUSTOM, Material.COMMAND_BLOCK, "&5&lCUSTOM EVENT (KUSTOM)",
                "&7Objektif khusus / event dari server.");

        // Navigation (Row 5)
        setButton(45, new BackButton(this, parent));
        setButton(53, new CloseButton());
    }

    private void addTypeButton(int slot, QuestObjectiveType type, Material icon, String name, String desc) {
        setButton(slot, new GuiButton(new ItemBuilder(icon)
                .name(name)
                .lore(List.of(
                        desc,
                        " ",
                        "&aKlik untuk memilih tipe ini >"
                ))
                .build(), event -> {
            if (onSelect != null) {
                onSelect.accept(type);
            }
            if (parent != null) {
                parent.open();
            }
        }));
    }
}
