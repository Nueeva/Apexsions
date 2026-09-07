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
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Horizontal 4-Row BattlePass Rewards Menu:
 * - Row 1 (Slots 0-8): Top Bar / Season Info / Player Stats
 * - Row 2 (Slots 9-17): Exsio Pass (Slot 9: Label, Slots 10-17: Rewards)
 * - Row 3 (Slots 18-26): Sio Pass (Slot 18: Label, Slots 19-26: Rewards)
 * - Row 4 (Slots 27-35): Progress Level (Slot 27: Label, Slots 28-35: Level Status)
 * - Row 5 (Slots 36-44): Citizen Pass (Slot 36: Label, Slots 37-44: Rewards)
 * - Row 6 (Slots 45-53): Bottom Navigation & Claim All
 */
public class RewardsMenu extends Gui {

    public enum RewardState {
        CLAIMABLE,
        CLAIMED,
        LOCKED_LEVEL,
        LOCKED_PASS
    }

    private static final int LEVELS_PER_PAGE = 8;
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
        fillBackground(Material.BLACK_STAINED_GLASS_PANE);

        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data == null) return;

        int maxLevel = plugin.getRewardManager().getMaxLevel();
        int maxPages = Math.max(1, (int) Math.ceil((double) maxLevel / LEVELS_PER_PAGE));
        int validPage = Math.max(1, Math.min(maxPages, page));

        String seasonTimeLeft = plugin.getSeasonManager().getTimeLeftFormatted();
        int reqXp = plugin.getRewardManager().getRequiredXp(data.getLevel());

        boolean hasExsio = plugin.getPassManager().canAccessRewardTier(data.getPasses(), "exsio");
        boolean hasSio = plugin.getPassManager().canAccessRewardTier(data.getPasses(), "sio");

        // ══════════════════════════════════════════════════════════════════════
        // 1. ROW 1: Top Bar & Season Info (Slots 0 to 8)
        // ══════════════════════════════════════════════════════════════════════
        String ownedPassTitle = hasExsio ? "&d&lExsio Pass (Tertinggi)" : (hasSio ? "&6&lSio Pass (Berbayar)" : "&f&lCitizen Pass (Gratis)");
        setButton(0, new GuiButton(new ItemBuilder(hasExsio ? Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE : (hasSio ? Material.GOLD_BLOCK : Material.BOOK))
                .name("&6&lSTATUS PASS KAMU")
                .lore(List.of(
                        "&7Pass Aktif: " + ownedPassTitle,
                        "&7Citizen Pass: &aDimiliki (Gratis)",
                        "&7Sio Pass: " + (hasSio ? "&aDimiliki ✓" : "&cBelum Dimiliki ✖"),
                        "&7Exsio Pass: " + (hasExsio ? "&aDimiliki ✓" : "&cBelum Dimiliki ✖")
                ))
                .build()));

        // Season Countdown Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.CLOCK)
                .name("&e&lSEASON: &6&l" + plugin.getSeasonManager().getCurrentSeason().getName())
                .lore(List.of(
                        "&7Sisa Waktu Season: &e" + seasonTimeLeft,
                        "&7Maksimum Level: &b" + maxLevel,
                        "&7Capai level " + maxLevel + " untuk seluruh reward!"
                ))
                .build()));

        // Player Stats Card (Slot 8)
        setButton(8, new GuiButton(new ItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(player)
                .name("&e&l" + player.getName())
                .lore(List.of(
                        "&7Level: &e" + data.getLevel() + " &8/ &f" + maxLevel,
                        "&7XP: &a" + data.getXp() + " &8/ &f" + reqXp,
                        "&7Battle Coins: &e" + plugin.getCurrencyService().format(data.getCurrency())
                ))
                .build()));

        // ══════════════════════════════════════════════════════════════════════
        // 2. ROW LABELS (Column 0: Slots 9, 18, 27, 36)
        // ══════════════════════════════════════════════════════════════════════
        // Baris ke-2: Exsio Pass Label (Slot 9)
        setButton(9, new GuiButton(new ItemBuilder(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                .name("&d&l[✦] EXSIO PASS")
                .lore(List.of(
                        "&7Pass Berbayar Tertinggi",
                        "&7Akses reward prestise paling langka!",
                        " ",
                        "&7Status: " + (hasExsio ? "&a&lDIMILIKI ✓" : "&c&lBELUM DIMILIKI 🔒")
                ))
                .build()));

        // Baris ke-3: Sio Pass Label (Slot 18)
        setButton(18, new GuiButton(new ItemBuilder(Material.GOLD_BLOCK)
                .name("&6&l[★] SIO PASS")
                .lore(List.of(
                        "&7Pass Berbayar",
                        "&7Akses reward berlimpah Sio & Citizen!",
                        " ",
                        "&7Status: " + (hasSio ? "&a&lDIMILIKI ✓" : "&c&lBELUM DIMILIKI 🔒")
                ))
                .build()));

        // Baris ke-4: Progress Level Label (Slot 27)
        setButton(27, new GuiButton(new ItemBuilder(Material.COMPASS)
                .name("&b&l[◆] PROGRESS LEVEL")
                .lore(List.of(
                        "&7Level Karakter: &e" + data.getLevel() + " &8/ &f" + maxLevel,
                        "&7XP Saat Ini: &a" + data.getXp() + " &8/ &f" + reqXp,
                        " ",
                        "&7Garis tengah menunjukkan pencapaian level kamu."
                ))
                .build()));

        // Baris ke-5: Citizen Pass Label (Slot 36)
        setButton(36, new GuiButton(new ItemBuilder(Material.CHEST)
                .name("&f&l[✿] CITIZEN PASS")
                .lore(List.of(
                        "&7Pass Gratis Semua Warga",
                        "&7Dapat diklaim langsung tanpa biaya!",
                        " ",
                        "&7Status: &a&lDIMILIKI (GRATIS) ✓"
                ))
                .build()));

        // ══════════════════════════════════════════════════════════════════════
        // 3. HORIZONTAL 8-LEVEL COLUMNS (Slots 10-17, 19-26, 28-35, 37-44)
        // ══════════════════════════════════════════════════════════════════════
        int startLevel = (validPage - 1) * LEVELS_PER_PAGE + 1;

        for (int i = 0; i < LEVELS_PER_PAGE; i++) {
            int level = startLevel + i;
            if (level > maxLevel) {
                // Empty slots beyond maxLevel
                setButton(10 + i, new GuiButton(new ItemBuilder(Material.BARRIER).name("&8[ Maksimum Level ]").build()));
                setButton(19 + i, new GuiButton(new ItemBuilder(Material.BARRIER).name("&8[ Maksimum Level ]").build()));
                setButton(28 + i, new GuiButton(new ItemBuilder(Material.BARRIER).name("&8[ Maksimum Level ]").build()));
                setButton(37 + i, new GuiButton(new ItemBuilder(Material.BARRIER).name("&8[ Maksimum Level ]").build()));
                continue;
            }

            int slotExsio   = 10 + i;
            int slotSio     = 19 + i;
            int slotLevel   = 28 + i;
            int slotCitizen = 37 + i;

            // Baris ke-2: Exsio Pass Reward
            setButton(slotExsio, createRewardButton(data, level, "exsio"));

            // Baris ke-3: Sio Pass Reward
            setButton(slotSio, createRewardButton(data, level, "sio"));

            // Baris ke-4: Progress Level Indicator
            setButton(slotLevel, createLevelProgressButton(data, level));

            // Baris ke-5: Citizen Pass Reward
            setButton(slotCitizen, createRewardButton(data, level, "citizen"));
        }

        // ══════════════════════════════════════════════════════════════════════
        // 4. ROW 6: Navigation & Claim All (Slots 45 to 53)
        // ══════════════════════════════════════════════════════════════════════
        setButton(45, new BackButton(this, parent));

        if (validPage > 1) {
            setButton(48, new GuiButton(new ItemBuilder(Material.ARROW).name("&e◀ Halaman " + (validPage - 1)).build(), event -> {
                new RewardsMenu(plugin, player, parent, validPage - 1).open();
            }));
        }

        setButton(49, new GuiButton(new ItemBuilder(Material.BOOK)
                .name("&7Halaman &e" + validPage + " &8/ &f" + maxPages)
                .lore(List.of(
                        "&7Menampilkan Level &e" + startLevel + " - " + Math.min(maxLevel, startLevel + LEVELS_PER_PAGE - 1),
                        "&7Total Level: &f" + maxLevel
                ))
                .build()));

        if (validPage < maxPages) {
            setButton(50, new GuiButton(new ItemBuilder(Material.ARROW).name("&eHalaman " + (validPage + 1) + " ▶").build(), event -> {
                new RewardsMenu(plugin, player, parent, validPage + 1).open();
            }));
        }

        // Claim All Button (Slot 53)
        int unclaimedCount = countUnclaimed(data, maxLevel);
        if (unclaimedCount > 0) {
            setButton(53, new GuiButton(new ItemBuilder(Material.HOPPER)
                    .name("&a&l[✔] KLAIM SEMUA HADIAH (" + unclaimedCount + ")")
                    .lore(List.of(
                            "&7Klaim seluruh hadiah yang sudah terbuka",
                            "&7secara otomatis sekaligus.",
                            " ",
                            "&e▶ Klik untuk klaim semua sekarang!"
                    ))
                    .build(), event -> {
                int claimed = claimAll(data, maxLevel);
                if (claimed > 0) {
                    player.sendMessage("§aBerhasil mengklaim §e" + claimed + " §ahadiah BattlePass!");
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
                    open();
                }
            }));
        } else {
            setButton(53, new CloseButton());
        }
    }

    private GuiButton createLevelProgressButton(PlayerData data, int level) {
        int reqXp = plugin.getRewardManager().getRequiredXp(level);
        if (data.getLevel() >= level) {
            // Level Selesai
            return new GuiButton(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                    .name("&a&lLevel " + level + " &7(Selesai ✓)")
                    .lore(List.of(
                            "&7XP Diperlukan: &a" + reqXp + " XP",
                            " ",
                            "&a✔ Level ini telah selesai dicapai!"
                    ))
                    .build());
        } else if (data.getLevel() == level - 1) {
            // Sedang Berjalan
            int remaining = Math.max(0, reqXp - data.getXp());
            return new GuiButton(new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE)
                    .name("&e&lLevel " + level + " &7(Sedang Berjalan ⏳)")
                    .lore(List.of(
                            "&7Progres XP: &e" + data.getXp() + " &8/ &f" + reqXp + " XP",
                            "&7Kurang: &c" + remaining + " XP &7lagi untuk naik level!",
                            " ",
                            "&eSelesaikan misi untuk mendapatkan XP!"
                    ))
                    .build());
        } else {
            // Terkunci
            return new GuiButton(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .name("&7&lLevel " + level + " &c(Terkunci 🔒)")
                    .lore(List.of(
                            "&7XP Diperlukan: &f" + reqXp + " XP",
                            " ",
                            "&cCapai level sebelumnya untuk membuka level ini."
                    ))
                    .build());
        }
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

        // Check preview type: Special for milestone every 50 levels or specialPreview flag
        boolean isSpecial = (level % 50 == 0) || rewards.stream().anyMatch(RewardItem::isSpecialPreview);

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
                lore.add(" ");
                lore.add(isSpecial ? "&6👑 Klik untuk melihat Preview Istimewa!" : "&b▶ Klik untuk melihat Preview Hadiah!");
                ItemStack item = new ItemBuilder(displayMat)
                        .name("&c[TERKUNCI] &f" + passName + " &8- Level " + level)
                        .lore(lore)
                        .hideAttributes()
                        .build();
                return new GuiButton(item, event -> {
                    if (isSpecial) {
                        new SpecialRewardPreviewMenu(plugin, player, level, passId, rewards, this).open();
                    } else {
                        new RewardPreviewMenu(plugin, player, level, passId, rewards, this).open();
                    }
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                });
            }
            case LOCKED_LEVEL -> {
                lore.add("&c🔒 TERKUNCI — LEVEL BELUM TERCAPAI");
                lore.add("&7Raih Level " + level + " untuk membuka hadiah ini.");
                lore.add(" ");
                lore.add(isSpecial ? "&6👑 Klik untuk melihat Preview Istimewa!" : "&b▶ Klik untuk melihat Preview Hadiah!");
                ItemStack item = new ItemBuilder(displayMat)
                        .name("&c[TERKUNCI] &f" + passName + " &8- Level " + level)
                        .lore(lore)
                        .hideAttributes()
                        .build();
                return new GuiButton(item, event -> {
                    if (isSpecial) {
                        new SpecialRewardPreviewMenu(plugin, player, level, passId, rewards, this).open();
                    } else {
                        new RewardPreviewMenu(plugin, player, level, passId, rewards, this).open();
                    }
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                });
            }
            case CLAIMABLE -> {
                lore.add("&a&l[KLIK UNTUK KLAIM HADIAH SEKARANG]");
                ItemStack item = new ItemBuilder(displayMat)
                        .name("&a&l[BISA DIKLAIM] &f" + passName + " &8- Level " + level)
                        .lore(lore)
                        .hideAttributes()
                        .glow()
                        .build();
                return new GuiButton(item, event -> {
                    boolean success = plugin.getRewardManager().claimReward(player, level, passId);
                    if (success) {
                        open(); // Re-render this page
                    }
                });
            }
        }

        return new GuiButton(new ItemStack(Material.AIR), null);
    }

    private Material getPassDisplayMaterial(String passId, RewardState state) {
        String norm = com.apexsions.battlepass.pass.PassManager.normalizePassId(passId);
        return switch (norm) {
            case "exsio" -> switch (state) {
                case CLAIMED -> Material.MINECART;
                case CLAIMABLE -> Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE;
                case LOCKED_PASS, LOCKED_LEVEL -> Material.ANCIENT_DEBRIS;
            };
            case "sio" -> switch (state) {
                case CLAIMED -> Material.MINECART;
                case CLAIMABLE -> Material.GOLD_BLOCK;
                case LOCKED_PASS, LOCKED_LEVEL -> Material.RAW_GOLD_BLOCK;
            };
            default -> switch (state) { // citizen
                case CLAIMED -> Material.MINECART;
                case CLAIMABLE -> Material.CHEST;
                case LOCKED_PASS, LOCKED_LEVEL -> Material.BARREL;
            };
        };
    }

    private int countUnclaimed(PlayerData data, int maxLevel) {
        int count = 0;
        for (int lvl = 1; lvl <= maxLevel; lvl++) {
            if (data.getLevel() < lvl) continue;
            for (String passId : List.of("citizen", "sio", "exsio")) {
                if (plugin.getPassManager().canAccessRewardTier(data.getPasses(), passId)) {
                    if (!data.isRewardClaimed(lvl, passId) && !plugin.getRewardManager().getRewards(lvl, passId).isEmpty()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private int claimAll(PlayerData data, int maxLevel) {
        int claimed = 0;
        for (int lvl = 1; lvl <= maxLevel; lvl++) {
            if (data.getLevel() < lvl) continue;
            for (String passId : List.of("citizen", "sio", "exsio")) {
                if (plugin.getPassManager().canAccessRewardTier(data.getPasses(), passId)) {
                    if (!data.isRewardClaimed(lvl, passId)) {
                        if (plugin.getRewardManager().claimReward(player, lvl, passId)) {
                            claimed++;
                        }
                    }
                }
            }
        }
        return claimed;
    }
}
