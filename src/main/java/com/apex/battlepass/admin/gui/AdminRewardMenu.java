package com.apex.battlepass.admin.gui;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.navigation.CloseButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.reward.RewardItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AdminRewardMenu extends Gui {

    private static final int LEVELS_PER_PAGE = 4;
    private final int page;

    public AdminRewardMenu(ApexsionsBattlepass plugin, Player player, Gui parent, int page) {
        super(plugin, player, plugin.getGuiConfig().getString("titles.admin-reward", "&8[ &4&lABP REWARD MANAGEMENT &8- Hal. %page% ]").replace("%page%", String.valueOf(page)), 54, parent);
        this.page = Math.max(1, page);
    }

    public AdminRewardMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        this(plugin, player, parent, 1);
    }

    @Override
    public void initialize() {
        fillBackground();

        int maxLevel = plugin.getRewardManager().getMaxLevel();
        int maxPages = (int) Math.ceil((double) maxLevel / LEVELS_PER_PAGE);
        int validPage = Math.max(1, Math.min(maxPages, page));

        // 1. Column Headers (Row 0)
        setButton(1, new GuiButton(new ItemBuilder(Material.OAK_SIGN).name("&f&lLEVEL").build()));
        setButton(2, new GuiButton(new ItemBuilder(Material.CHEST).name("&f&l[FREE]").build()));
        setButton(3, new GuiButton(new ItemBuilder(Material.GOLD_BLOCK).name("&6&l[PREMIUM]").build()));
        setButton(4, new GuiButton(new ItemBuilder(Material.BEACON).name("&e&l[PREMIUM+]").build()));
        setButton(5, new GuiButton(new ItemBuilder(Material.NETHERITE_BLOCK).name("&5&l[ULTIMATE]").build()));

        // Admin Info Banner (Slot 7)
        setButton(7, new GuiButton(new ItemBuilder(Material.COMMAND_BLOCK)
                .name("&4&lINSPEKSI REWARDS")
                .lore(List.of(
                        "&7Menampilkan daftar seluruh reward",
                        "&7yang dikonfigurasi di &erewards.yml&7.",
                        " ",
                        "&7Total Max Level: &e" + maxLevel
                ))
                .build()));

        // Reward Editor Mode (Slot 8)
        setButton(8, new GuiButton(new ItemBuilder(Material.ANVIL)
                .name("&a&l[✏] MODE EDIT REWARDS")
                .lore(List.of(
                        "&7Kelola reward: Tambah item langsung",
                        "&7dari inventory, atur currency, hapus, dll.",
                        " ",
                        "&aKlik untuk membuka editor rewards >"
                ))
                .build(), event -> {
            new com.apex.battlepass.admin.gui.reward.AdminRewardPassMenu(plugin, player, this).open();
        }));

        // 2. Render 4 Level Rows
        int startLevel = (validPage - 1) * LEVELS_PER_PAGE + 1;
        int[] rowLevelSlots  = { 10, 19, 28, 37 };
        int[] rowFreeSlots   = { 11, 20, 29, 38 };
        int[] rowPremSlots   = { 12, 21, 30, 39 };
        int[] rowPlusSlots   = { 13, 22, 31, 40 };
        int[] rowUltSlots    = { 14, 23, 32, 41 };

        for (int i = 0; i < LEVELS_PER_PAGE; i++) {
            int level = startLevel + i;
            if (level > maxLevel) break;

            int slotLvl = rowLevelSlots[i];
            int slotFree = rowFreeSlots[i];
            int slotPrem = rowPremSlots[i];
            int slotPlus = rowPlusSlots[i];
            int slotUlt  = rowUltSlots[i];

            setButton(slotLvl, new GuiButton(new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                    .name("&e&lLevel " + level)
                    .lore(List.of(
                            "&7Required XP: &f" + plugin.getRewardManager().getRequiredXp(level) + " XP"
                    ))
                    .build()));

            setButton(slotFree, createTierInspectButton(level, "free"));
            setButton(slotPrem, createTierInspectButton(level, "premium"));
            setButton(slotPlus, createTierInspectButton(level, "premium-plus"));
            setButton(slotUlt,  createTierInspectButton(level, "ultimate"));
        }

        // 3. Navigation Controls (Row 5)
        setButton(45, new BackButton(this, parent));

        if (validPage > 1) {
            setButton(48, new GuiButton(new ItemBuilder(Material.PAPER).name("&e< Halaman " + (validPage - 1)).build(), event -> {
                new AdminRewardMenu(plugin, player, parent, validPage - 1).open();
            }));
        }

        setButton(49, new GuiButton(new ItemBuilder(Material.BOOK).name("&7Halaman &e" + validPage + " &8/ &f" + maxPages).build()));

        if (validPage < maxPages) {
            setButton(50, new GuiButton(new ItemBuilder(Material.PAPER).name("&eHalaman " + (validPage + 1) + " >").build(), event -> {
                new AdminRewardMenu(plugin, player, parent, validPage + 1).open();
            }));
        }

        setButton(53, new CloseButton());
    }

    private GuiButton createTierInspectButton(int level, String passId) {
        List<RewardItem> rewards = plugin.getRewardManager().getRewards(level, passId);
        if (rewards.isEmpty()) {
            ItemStack emptyItem = new ItemBuilder(Material.GRAY_DYE)
                    .name("&7&l[KOSONG]")
                    .lore(List.of("&7Tidak ada reward " + passId.toUpperCase() + " pada level ini."))
                    .build();
            return new GuiButton(emptyItem, null);
        }

        RewardItem first = rewards.get(0);
        Material displayMat = first.getMaterial();
        String name = first.getName() != null ? first.getName() : "&f" + passId.toUpperCase() + " Lvl " + level;

        List<String> lore = new ArrayList<>();
        lore.add("&7Tier: &e" + passId.toUpperCase());
        lore.add("&7Total Items: &f" + rewards.size());
        lore.add(" ");
        for (RewardItem item : rewards) {
            lore.add(" &8- &f" + item.getType() + ": " + (item.getName() != null ? item.getName() : item.getMaterial().name()) + " x" + item.getAmount());
            if (!item.getCommands().isEmpty()) {
                lore.add("   &7Cmds: &8" + String.join(", ", item.getCommands()));
            }
        }

        ItemStack item = new ItemBuilder(displayMat)
                .name("&a" + name)
                .lore(lore)
                .hideAttributes()
                .build();
        return new GuiButton(item, null);
    }
}
