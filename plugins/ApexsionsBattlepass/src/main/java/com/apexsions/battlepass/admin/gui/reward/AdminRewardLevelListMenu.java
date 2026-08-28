package com.apexsions.battlepass.admin.gui.reward;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.reward.RewardItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AdminRewardLevelListMenu extends Gui {

    private static final int LEVELS_PER_PAGE = 28;
    private final String passId;
    private final int page;

    public AdminRewardLevelListMenu(ApexsionsBattlepass plugin, Player player, String passId, Gui parent, int page) {
        super(plugin, player, "&8[ &4&lLEVELS: &e" + passId.toUpperCase() + " &8- Hal. " + page + " ]", 54, parent);
        this.passId = passId.toLowerCase();
        this.page = Math.max(1, page);
    }

    public AdminRewardLevelListMenu(ApexsionsBattlepass plugin, Player player, String passId, Gui parent) {
        this(plugin, player, passId, parent, 1);
    }

    @Override
    public void initialize() {
        fillBackground();

        int maxLevel = plugin.getRewardManager().getMaxLevel();
        int maxPages = Math.max(1, (int) Math.ceil((double) maxLevel / LEVELS_PER_PAGE));
        int validPage = Math.max(1, Math.min(maxPages, page));

        // 1. Header Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name("&6&lPILIH LEVEL &8[&e" + passId.toUpperCase() + "&8]")
                .lore(List.of(
                        "&7Pilih level untuk melihat, menambah, atau",
                        "&7mengubah hadiah pada level tersebut.",
                        " ",
                        "&7Total Max Level: &e" + maxLevel
                ))
                .build()));

        // 2. Render Levels (Slots 10..16, 19..25, 28..34, 37..43)
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int startLevel = (validPage - 1) * LEVELS_PER_PAGE + 1;
        int slotIdx = 0;

        for (int i = 0; i < LEVELS_PER_PAGE && slotIdx < slots.length; i++) {
            int level = startLevel + i;
            if (level > maxLevel) break;

            List<RewardItem> rewards = plugin.getRewardManager().getRewards(level, passId);
            int reqXp = plugin.getRewardManager().getRequiredXp(level);

            List<String> lore = new ArrayList<>();
            lore.add("&7Required XP: &a" + reqXp + " XP");
            lore.add("&7Total Hadiah: &e" + rewards.size() + " reward");
            lore.add(" ");
            if (rewards.isEmpty()) {
                lore.add("&c[Belum ada reward terpasang]");
            } else {
                lore.add("&6Daftar Hadiah:");
                for (RewardItem ri : rewards) {
                    lore.add(" &8- &f" + ri.getType() + ": " + (ri.getName() != null ? ri.getName() : ri.getMaterial().name()) + " x" + ri.getAmount());
                }
            }
            lore.add(" ");
            lore.add("&eKlik untuk mengelola level ini >");

            Material iconMat = rewards.isEmpty() ? Material.MINECART : Material.CHEST_MINECART;

            ItemStack item = new ItemBuilder(iconMat)
                    .name("&e&lLevel " + level)
                    .lore(lore)
                    .build();

            setButton(slots[slotIdx++], new GuiButton(item, event -> {
                new AdminRewardLevelEditorMenu(plugin, player, passId, level, this).open();
            }));
        }

        // 3. Navigation Controls (Row 5)
        setButton(45, new BackButton(this, parent));

        if (validPage > 1) {
            setButton(48, new GuiButton(new ItemBuilder(Material.PAPER).name("&e< Halaman " + (validPage - 1)).build(), event -> {
                new AdminRewardLevelListMenu(plugin, player, passId, parent, validPage - 1).open();
            }));
        }

        setButton(49, new GuiButton(new ItemBuilder(Material.BOOK).name("&7Halaman &e" + validPage + " &8/ &f" + maxPages).build()));

        if (validPage < maxPages) {
            setButton(50, new GuiButton(new ItemBuilder(Material.PAPER).name("&eHalaman " + (validPage + 1) + " >").build(), event -> {
                new AdminRewardLevelListMenu(plugin, player, passId, parent, validPage + 1).open();
            }));
        }

        setButton(53, new CloseButton());
    }
}
