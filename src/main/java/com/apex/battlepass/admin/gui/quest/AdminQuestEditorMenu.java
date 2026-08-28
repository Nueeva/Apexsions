package com.apex.battlepass.admin.gui.quest;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.navigation.CloseButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.quest.model.Quest;
import com.apex.battlepass.quest.model.QuestCategory;
import com.apex.battlepass.quest.model.QuestObjectiveType;
import com.apex.battlepass.util.ItemSerializer;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminQuestEditorMenu extends Gui {

    private final QuestCategory category;
    private final int periodIndex;
    private final Quest originalQuest;

    private String questId;
    private String questName;
    private String questDesc;
    private QuestObjectiveType objectiveType;
    private EntityType targetEntity;
    private Material targetBlock;
    private Material targetItem;
    private int targetAmount;
    private int rewardXp;
    private int rewardCoins;

    public AdminQuestEditorMenu(ApexsionsBattlepass plugin, Player player, QuestCategory category, int periodIndex, Quest originalQuest, Gui parent) {
        super(plugin, player, "&8[ &4&lEDITOR QUEST: &e" + category.name() + " &8]", 54, parent);
        this.category = category;
        this.periodIndex = periodIndex;
        this.originalQuest = originalQuest;

        if (originalQuest != null) {
            this.questId = originalQuest.getId();
            this.questName = originalQuest.getName();
            this.questDesc = originalQuest.getDescription();
            this.objectiveType = originalQuest.getType();
            this.targetEntity = originalQuest.getTargetEntity();
            this.targetBlock = originalQuest.getTargetBlock();
            this.targetItem = originalQuest.getTargetItem();
            this.targetAmount = originalQuest.getTargetAmount();
            this.rewardXp = originalQuest.getRewardXp();
            this.rewardCoins = originalQuest.getRewardCoins();
        } else {
            this.questId = "quest_" + System.currentTimeMillis() % 100000;
            this.questName = "Quest Baru";
            this.questDesc = "Deskripsi quest baru.";
            this.objectiveType = QuestObjectiveType.MINE_BLOCK;
            this.targetEntity = null;
            this.targetBlock = Material.DIAMOND_ORE;
            this.targetItem = null;
            this.targetAmount = 10;
            this.rewardXp = 100;
            this.rewardCoins = 15;
        }
    }

    @Override
    public void initialize() {
        fillBorder();

        // 1. Overview Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.WRITABLE_BOOK)
                .name("&6&lPENGATURAN QUEST: &e" + questName)
                .lore(List.of(
                        "&7ID: &8" + questId,
                        "&7Kategori: &f" + category.name() + (periodIndex > 0 ? " #" + periodIndex : ""),
                        "&7Tipe Objektif: &e" + objectiveType.name(),
                        "&7Target: &a" + getTargetString() + " &8(x" + targetAmount + ")",
                        "&7Hadiah: &b+" + rewardXp + " XP &7| &e+" + rewardCoins + " Coins",
                        " ",
                        "&7Deskripsi: &f" + questDesc
                ))
                .build()));

        // 2. Edit Name & Desc (Slot 19)
        setButton(19, new GuiButton(new ItemBuilder(Material.NAME_TAG)
                .name("&e&l[✏] UBAH NAMA & DESKRIPSI")
                .lore(List.of(
                        "&7Nama Saat Ini: &f" + questName,
                        "&7Deskripsi: &7" + questDesc,
                        " ",
                        "&eKlik untuk mengubah nama & deskripsi via chat >"
                ))
                .build(), event -> {
            plugin.getChatInputManager().startInput(player, "Masukkan nama quest baru:", name -> {
                this.questName = name;
                plugin.getChatInputManager().startInput(player, "Masukkan deskripsi quest baru:", desc -> {
                    this.questDesc = desc;
                    open();
                }, this::open);
            }, this::open);
        }));

        // 3. Select Objective Type (Slot 21 - Opens Dedicated GUI Picker)
        setButton(21, new GuiButton(new ItemBuilder(Material.COMPASS)
                .name("&6&l[🧭] PILIH TIPE OBJEKTIF: &e" + objectiveType.name())
                .lore(List.of(
                        "&7Buka menu pilihan tipe objektif quest",
                        "&7(Mine, Kill, Craft, Fish, Smelt, Breed, dll).",
                        " ",
                        "&6Klik untuk membuka GUI pilihan tipe >"
                ))
                .build(), event -> {
            new AdminQuestTypePickerMenu(plugin, player, this, chosenType -> {
                this.objectiveType = chosenType;
                // Reset targets to match new type
                this.targetEntity = null;
                this.targetBlock = null;
                this.targetItem = null;
                open();
            }).open();
        }));

        // 4. Select Target (Slot 23 - Opens Dynamic Target GUI Picker)
        setButton(23, new GuiButton(new ItemBuilder(Material.TARGET)
                .name("&b&l[🎯] PILIH TARGET: &f" + getTargetString())
                .lore(List.of(
                        "&7Buka menu daftar target yang sesuai dengan",
                        "&7tipe objektif &e" + objectiveType.name() + "&7.",
                        " ",
                        "&bKlik untuk membuka GUI pilihan target >"
                ))
                .build(), event -> {
            new AdminQuestTargetPickerMenu(plugin, player, objectiveType, this, (entity, block, item) -> {
                this.targetEntity = entity;
                this.targetBlock = block;
                this.targetItem = item;
                open();
            }).open();
        }));

        // 5. Change Target Amount (Slot 25)
        setButton(25, new GuiButton(new ItemBuilder(Material.REDSTONE)
                .name("&c&l[🔢] TARGET AMOUNT: &f" + targetAmount + "x")
                .lore(List.of(
                        "&7Jumlah target yang harus diselesaikan pemain.",
                        " ",
                        "&eKlik untuk mengubah jumlah via chat >"
                ))
                .build(), event -> {
            plugin.getChatInputManager().startNumericInput(player, "Masukkan jumlah target yang dibutuhkan (1 - 10000):", amt -> {
                this.targetAmount = amt;
                open();
            }, this::open, 1, 10000);
        }));

        // 6. Change Reward XP (Slot 29)
        setButton(29, new GuiButton(new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name("&a&l[📜] REWARD XP: &f+" + rewardXp + " XP")
                .lore(List.of(
                        "&7Hadiah XP BattlePass saat quest selesai.",
                        " ",
                        "&eKlik untuk mengubah reward XP via chat >"
                ))
                .build(), event -> {
            plugin.getChatInputManager().startNumericInput(player, "Masukkan jumlah reward XP (1 - 100000):", xp -> {
                this.rewardXp = xp;
                open();
            }, this::open, 1, 100000);
        }));

        // 7. Change Reward Coins (Slot 31)
        setButton(31, new GuiButton(new ItemBuilder(Material.EMERALD)
                .name("&6&l[🪙] REWARD COINS: &f+" + rewardCoins + " Coins")
                .lore(List.of(
                        "&7Hadiah Battle Coins saat quest selesai.",
                        " ",
                        "&eKlik untuk mengubah reward Coins via chat >"
                ))
                .build(), event -> {
            plugin.getChatInputManager().startNumericInput(player, "Masukkan jumlah reward Coins (0 - 100000):", coins -> {
                this.rewardCoins = coins;
                open();
            }, this::open, 0, 100000);
        }));

        // 8. Delete Quest Button (Slot 33)
        if (originalQuest != null) {
            setButton(33, new GuiButton(new ItemBuilder(Material.TNT)
                    .name("&4&l[✖] HAPUS QUEST INI")
                    .lore(List.of(
                            "&7Hapus quest ini dari daftar " + category.name() + ".",
                            " ",
                            "&cKlik untuk menghapus >"
                    ))
                    .build(), event -> {
                plugin.getQuestManager().deleteQuest(category, periodIndex, questId);
                player.sendMessage("§cQuest §e" + questName + " §cberhasil dihapus!");
                if (parent != null) parent.open();
            }));
        }

        // 9. Bottom Navigation (Row 5)
        setButton(45, new BackButton(this, parent));

        setButton(49, new GuiButton(new ItemBuilder(Material.LIME_CONCRETE)
                .name("&a&l[✔] SIMPAN PERUBAHAN QUEST")
                .lore(List.of(
                        "&7Simpan quest ini ke daftar &e" + category.name() + "&7.",
                        " ",
                        "&aKlik untuk menyimpan >"
                ))
                .build(), event -> {
            Quest q = new Quest(questId, questName, questDesc, category, objectiveType, periodIndex, targetEntity, targetBlock, targetItem, targetAmount, rewardXp, rewardCoins);
            plugin.getQuestManager().addOrUpdateQuest(q);
            player.sendMessage("§aBerhasil menyimpan quest §e" + questName + "§a!");
            if (parent != null) parent.open();
        }));

        setButton(53, new CloseButton());
    }

    private String getTargetString() {
        if (targetEntity != null) return targetEntity.name();
        if (targetBlock != null) return ItemSerializer.formatMaterialName(targetBlock);
        if (targetItem != null) return ItemSerializer.formatMaterialName(targetItem);
        return "SEMUA / APA SAJA (BEBAS)";
    }
}

