package com.apex.battlepass.admin.gui.quest;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.navigation.CloseButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.quest.model.Quest;
import com.apex.battlepass.quest.model.QuestCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminQuestListMenu extends Gui {

    private final QuestCategory category;
    private final int periodIndex;

    public AdminQuestListMenu(ApexsionsBattlepass plugin, Player player, QuestCategory category, int periodIndex, Gui parent) {
        super(plugin, player, "&8[ &4&lQUESTS: &e" + category.name() + (periodIndex > 0 ? " #" + periodIndex : "") + " &8]", 45, parent);
        this.category = category;
        this.periodIndex = periodIndex;
    }

    @Override
    public void initialize() {
        fillBackground();

        Map<String, Quest> quests;
        if (category == QuestCategory.DAILY) {
            quests = plugin.getQuestManager().getDailyQuests();
        } else if (category == QuestCategory.WEEKLY) {
            quests = plugin.getQuestManager().getWeeklyQuests().getOrDefault(periodIndex, Map.of());
        } else {
            quests = plugin.getQuestManager().getMonthlyQuests().getOrDefault(periodIndex, Map.of());
        }

        // 1. Header Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.WRITABLE_BOOK)
                .name("&6&lDAFTAR QUEST - &e" + category.name() + (periodIndex > 0 ? " #" + periodIndex : ""))
                .lore(List.of(
                        "&7Total Quest Terdaftar: &e" + quests.size(),
                        " ",
                        "&7Pilih quest untuk mengubah objektif, target, atau reward.",
                        "&7Atau klik tombol Tambah Quest untuk membuat quest baru."
                ))
                .build()));

        // 2. Add New Quest Button (Slot 8)
        setButton(8, new GuiButton(new ItemBuilder(Material.EMERALD_BLOCK)
                .name("&a&l[➕] BUAT QUEST BARU")
                .lore(List.of(
                        "&7Tambahkan quest baru ke dalam",
                        "&7kategori &e" + category.name() + (periodIndex > 0 ? " #" + periodIndex : "") + "&7.",
                        " ",
                        "&aKlik untuk membuat quest baru >"
                ))
                .build(), event -> {
            new AdminQuestEditorMenu(plugin, player, category, periodIndex, null, this).open();
        }));

        // 3. Render Quests (Slots 19..34)
        int[] slots = { 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34 };
        int idx = 0;

        for (Quest q : quests.values()) {
            if (idx >= slots.length) break;

            List<String> lore = new ArrayList<>();
            lore.add("&7ID: &8" + q.getId());
            lore.add("&7Objektif: &e" + q.getType().name());
            if (q.getTargetEntity() != null) lore.add("&7Target Entity: &f" + q.getTargetEntity().name());
            if (q.getTargetBlock() != null) lore.add("&7Target Block: &f" + q.getTargetBlock().name());
            if (q.getTargetItem() != null) lore.add("&7Target Item: &f" + q.getTargetItem().name());
            lore.add("&7Jumlah Target: &a" + q.getTargetAmount());
            lore.add("&7Reward XP: &b+" + q.getRewardXp() + " XP");
            lore.add("&7Reward Coins: &6+" + q.getRewardCoins() + " Coins");
            lore.add(" ");
            lore.add("&eKlik untuk mengedit quest ini >");

            ItemStack item = new ItemBuilder(Material.PAPER)
                    .name("&e&l" + q.getName())
                    .lore(lore)
                    .build();

            setButton(slots[idx++], new GuiButton(item, event -> {
                new AdminQuestEditorMenu(plugin, player, category, periodIndex, q, this).open();
            }));
        }

        // Navigation
        setButton(36, new BackButton(this, parent));
        setButton(44, new CloseButton());
    }
}
