package com.apexsions.battlepass.reward.gui;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.pass.PassTier;
import com.apexsions.battlepass.player.PlayerData;
import com.apexsions.battlepass.reward.RewardItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RewardsMenu extends Gui {

    public enum RewardState {
        CLAIMABLE,
        CLAIMED,
        LOCKED_LEVEL,
        LOCKED_PASS
    }

    private static final int LEVELS_PER_PAGE = 4;
    private final int page;

    public RewardsMenu(ApexsionsBattlepass plugin, Player player, Gui parent, int page) {
        super(plugin, player, "&8[ &b&lBP REWARDS &8- Hal. " + page + " ]", 54, parent);
        this.page = Math.max(1, page);
    }

    public RewardsMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        this(plugin, player, parent, 1);
    }

    @Override
    public void initialize() {
        fillBorder();

        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data == null) return;

        int maxLevel = plugin.getRewardManager().getMaxLevel();
        int maxPages = (int) Math.ceil((double) maxLevel / LEVELS_PER_PAGE);
        int validPage = Math.max(1, Math.min(maxPages, page));

        String seasonTimeLeft = plugin.getSeasonManager().getTimeLeftFormatted();

        // 1. Column Headers (Row 0)
        setButton(1, new GuiButton(new ItemBuilder(Material.OAK_SIGN).name("&f&lLEVEL").build()));
        setButton(2, new GuiButton(new ItemBuilder(Material.CHEST).name("&f&l[FREE]").build()));
        setButton(3, new GuiButton(new ItemBuilder(Material.GOLD_BLOCK).name("&6&l[PREMIUM]").build()));
        setButton(4, new GuiButton(new ItemBuilder(Material.DIAMOND_BLOCK).name("&b&l[PREMIUM+]").build()));
        setButton(5, new GuiButton(new ItemBuilder(Material.NETHERITE_BLOCK).name("&5&l[ULTIMATE]").build()));

        // Season Countdown Banner (Slot 7)
        setButton(7, new GuiButton(new ItemBuilder(Material.CLOCK)
                .name("&6&lSISA WAKTU SEASON")
                .lore(List.of(
                        "&7Season ends in: &e" + seasonTimeLeft,
                        "&7Raih level " + maxLevel + " untuk mengklaim seluruh hadiah!"
                ))
                .build()));


        // Player Stats Card (Slot 8)
        int reqXp = plugin.getRewardManager().getRequiredXp(data.getLevel());
        setButton(8, new GuiButton(new ItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(player)
                .name("&e&l" + player.getName())
                .lore(List.of(
                        "&7Level: &e" + data.getLevel() + " &8/ &f" + maxLevel,
                        "&7XP: &a" + data.getXp() + " &8/ &f" + reqXp,
                        "&7Passes: &b" + String.join(", ", data.getPasses()).toUpperCase(),
                        "&7Coins: &e" + plugin.getCurrencyService().format(data.getCurrency())
                ))
                .build()));

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

            boolean levelReached = data.getLevel() >= level;
            Material lvlMat = levelReached ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            setButton(slotLvl, new GuiButton(new ItemBuilder(lvlMat)
                    .name("&e&lLevel " + level)
                    .lore(List.of(
                            levelReached ? "&a✔ Level Terpenuhi" : "&c🔒 Membutuhkan Level " + level,
                            "&7Required XP: &f" + plugin.getRewardManager().getRequiredXp(level)
                    ))
                    .build()));

            // 4 Pass Tier Reward Buttons
            setButton(slotFree, createRewardButton(data, level, "free"));
            setButton(slotPrem, createRewardButton(data, level, "premium"));
            setButton(slotPlus, createRewardButton(data, level, "premium-plus"));
            setButton(slotUlt,  createRewardButton(data, level, "ultimate"));
        }

        // 3. Navigation Controls (Row 5)
        setButton(45, new BackButton(this, parent));

        if (validPage > 1) {
            setButton(47, new GuiButton(new ItemBuilder(Material.ARROW).name("&e◀ Halaman " + (validPage - 1)).build(), event -> {
                new RewardsMenu(plugin, player, parent, validPage - 1).open();
            }));
        }

        setButton(49, new GuiButton(new ItemBuilder(Material.MAP).name("&7Halaman &e" + validPage + " &8/ &f" + maxPages).build()));

        if (validPage < maxPages) {
            setButton(51, new GuiButton(new ItemBuilder(Material.ARROW).name("&eHalaman " + (validPage + 1) + " ▶").build(), event -> {
                new RewardsMenu(plugin, player, parent, validPage + 1).open();
            }));
        }

        setButton(53, new CloseButton());

        // 4. Fill every remaining empty slot across all 54 slots
        fillBackground(Material.BLACK_STAINED_GLASS_PANE);
    }

    private GuiButton createRewardButton(PlayerData data, int level, String passId) {
        List<RewardItem> rewards = plugin.getRewardManager().getRewards(level, passId);
        boolean hasPass = plugin.getPassManager().canAccessRewardTier(data.getPasses(), passId);
        boolean levelReached = data.getLevel() >= level;
        boolean claimed = data.isRewardClaimed(level, passId);

        PassTier tier = plugin.getPassManager().getPass(passId);
        String passName = tier != null ? tier.getDisplayName() : passId.toUpperCase();

        if (rewards.isEmpty()) {
            ItemStack emptyItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .name("&8[ Kosong ] &7" + passName + " - Level " + level)
                    .lore(List.of("&8Tidak ada hadiah pada slot ini."))
                    .build();
            return new GuiButton(emptyItem, null);
        }

        // Determine RewardState
        RewardState state;
        if (claimed) {
            state = RewardState.CLAIMED;
        } else if (!hasPass) {
            state = RewardState.LOCKED_PASS;
        } else if (!levelReached) {
            state = RewardState.LOCKED_LEVEL;
        } else {
            state = RewardState.CLAIMABLE;
        }

        Material displayMat = getPassDisplayMaterial(passId, state);

        List<String> lore = new ArrayList<>();
        lore.add("&7Tier Pass: &f" + passName);
        lore.add("&7Syarat Level: &eLevel " + level);
        lore.add(" ");
        lore.add("&7Isi Hadiah Level Ini:");
        for (RewardItem ri : rewards) {
            lore.add(" &8● &f" + ri.getAmount() + "x " + ri.getDisplayName());
        }
        lore.add(" ");

        switch (state) {
            case CLAIMED -> {
                lore.add("&a✔ SUDAH DIKLAIM");
                ItemStack item = new ItemBuilder(displayMat)
                        .name("&a[DIKLAIM] &f" + passName + " &8- Level " + level)
                        .lore(lore)
                        .hideAttributes()
                        .build();
                return new GuiButton(item, null);
            }
            case LOCKED_PASS -> {
                lore.add("&c🔒 TERKUNCI — BUTUH " + passName + " PASS");
                lore.add("&7Beli atau miliki " + passName + " untuk membuka hadiah!");
                ItemStack item = new ItemBuilder(displayMat)
                        .name("&c[TERKUNCI] &f" + passName + " &8- Level " + level)
                        .lore(lore)
                        .hideAttributes()
                        .build();
                return new GuiButton(item, event -> {
                    player.sendMessage(plugin.getMessage("reward-pass-locked").replace("%pass%", passName));
                });
            }
            case LOCKED_LEVEL -> {
                lore.add("&c🔒 TERKUNCI — LEVEL BELUM TERCAPAI");
                lore.add("&7Raih Level " + level + " untuk membuka hadiah ini.");
                ItemStack item = new ItemBuilder(displayMat)
                        .name("&c[TERKUNCI] &f" + passName + " &8- Level " + level)
                        .lore(lore)
                        .hideAttributes()
                        .build();
                return new GuiButton(item, event -> {
                    player.sendMessage(plugin.getMessage("reward-level-not-reached").replace("%level%", String.valueOf(level)));
                });
            }
            case CLAIMABLE -> {
                lore.add("&a&l[KLIK UNTUK KLAIM HADIAH SEKARANG]");
                ItemStack item = new ItemBuilder(displayMat)
                        .name("&a&l[BISA DIKLAIM] &f" + passName + " &8- Level " + level)
                        .lore(lore)
                        .hideAttributes()
                        .build();
                return new GuiButton(item, event -> {
                    if (plugin.getRewardManager().claimReward(player, level, passId)) {
                        open(); // In-place refresh
                    }
                });
            }
        }

        return new GuiButton(new ItemStack(Material.AIR), null);
    }

    private Material getPassDisplayMaterial(String passId, RewardState state) {
        String p = passId.toLowerCase();
        switch (p) {
            case "free":
                return switch (state) {
                    case CLAIMABLE -> Material.CHEST;
                    case CLAIMED -> Material.MINECART;
                    case LOCKED_LEVEL -> Material.IRON_BARS;
                    case LOCKED_PASS -> Material.BARRIER;
                };
            case "premium":
                return switch (state) {
                    case CLAIMABLE -> Material.GOLD_BLOCK;
                    case CLAIMED -> Material.GOLD_INGOT;
                    case LOCKED_LEVEL -> Material.CHAIN;
                    case LOCKED_PASS -> Material.REDSTONE_BLOCK;
                };
            case "vip":
                return switch (state) {
                    case CLAIMABLE -> Material.EMERALD_BLOCK;
                    case CLAIMED -> Material.EMERALD;
                    case LOCKED_LEVEL -> Material.COPPER_BLOCK;
                    case LOCKED_PASS -> Material.RAW_COPPER_BLOCK;
                };
            case "elite":
                return switch (state) {
                    case CLAIMABLE -> Material.AMETHYST_BLOCK;
                    case CLAIMED -> Material.AMETHYST_SHARD;
                    case LOCKED_LEVEL -> Material.PURPLE_STAINED_GLASS_PANE;
                    case LOCKED_PASS -> Material.PURPLE_CONCRETE;
                };
            case "premium-plus":
            case "premium_plus":
            case "plus":
                return switch (state) {
                    case CLAIMABLE -> Material.DIAMOND_BLOCK;
                    case CLAIMED -> Material.DIAMOND;
                    case LOCKED_LEVEL -> Material.CYAN_STAINED_GLASS_PANE;
                    case LOCKED_PASS -> Material.LAPIS_BLOCK;
                };
            case "ultimate":
                return switch (state) {
                    case CLAIMABLE -> Material.NETHERITE_BLOCK;
                    case CLAIMED -> Material.NETHERITE_INGOT;
                    case LOCKED_LEVEL -> Material.NETHER_BRICKS;
                    case LOCKED_PASS -> Material.CRYING_OBSIDIAN;
                };
            default:
                return switch (state) {
                    case CLAIMABLE -> Material.ENDER_CHEST;
                    case CLAIMED -> Material.HOPPER_MINECART;
                    case LOCKED_LEVEL -> Material.IRON_BARS;
                    case LOCKED_PASS -> Material.BARRIER;
                };
        }
    }
}
