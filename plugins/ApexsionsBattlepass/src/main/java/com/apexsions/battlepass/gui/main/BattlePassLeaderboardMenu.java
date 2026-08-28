package com.apexsions.battlepass.gui.main;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.leaderboard.LeaderboardEntry;
import com.apexsions.battlepass.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BattlePassLeaderboardMenu extends Gui {

    private static final int ENTRIES_PER_PAGE = 10;
    // 10-slot symmetrical pyramid: Row 1 (1 slot: 13), Row 2 (2 slots: 21, 23), Row 3 (3 slots: 29, 31, 33), Row 4 (4 slots: 37, 39, 41, 43)
    private static final int[] PYRAMID_SLOTS = { 13, 21, 23, 29, 31, 33, 37, 39, 41, 43 };

    private final int page;

    public BattlePassLeaderboardMenu(ApexsionsBattlepass plugin, Player player, Gui parent, int page) {
        super(plugin, player, "&8[ &6&lTOP " + (Math.max(1, Math.min(10, page)) * 10) + " BP &8]", 54, parent);
        this.page = Math.max(1, Math.min(10, page));
    }

    public BattlePassLeaderboardMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        this(plugin, player, parent, 1);
    }

    @Override
    public void initialize() {
        fillBorder();

        List<LeaderboardEntry> list = plugin.getLeaderboardService().getLeaderboard();
        int totalEntries = Math.min(100, list.size());
        int maxPages = Math.min(10, Math.max(1, (int) Math.ceil((double) totalEntries / ENTRIES_PER_PAGE)));
        int validPage = Math.max(1, Math.min(maxPages, page));

        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        int myRank = plugin.getLeaderboardService().getPlayerRank(player.getUniqueId());
        String rankStr = (myRank > 0 && myRank <= 100) ? ("#" + myRank) : "Belum Masuk Top 100";

        int maxLevel = plugin.getRewardManager().getMaxLevel();
        int currentLevel = data != null ? data.getLevel() : 1;
        int currentXp = data != null ? data.getXp() : 0;
        int reqXp = plugin.getRewardManager().getRequiredXp(currentLevel);
        String xpBar = buildProgressBar(currentXp, reqXp);
        String seasonTimeLeft = plugin.getSeasonManager().getTimeLeftFormatted();
        Set<String> passes = data != null ? plugin.getPassManager().getEffectivePasses(data.getPasses()) : Set.of("FREE");

        // 1. Leaderboard Info Banner (Slot 0)
        setButton(0, new GuiButton(new ItemBuilder(Material.NETHER_STAR)
                .name("&6&lTOP BATTLEPASS RANKINGS")
                .lore(List.of(
                        "&7Peringkat seluruh pemain berdasarkan",
                        "&7Level & XP BattlePass pada Season ini.",
                        " ",
                        "&7Peringkat Anda: &e" + rankStr,
                        "&7Total Peserta: &b" + totalEntries + " pemain",
                        "&7Season: &f" + (plugin.getSeasonManager().getCurrentSeason() != null ? plugin.getSeasonManager().getCurrentSeason().getName() : "Season Aktif")
                ))
                .build()));

        double myRupiah = 0.0;
        try {
            myRupiah = com.apexsions.economy.api.ApexsionsEconomyAPI.getBalance(player.getUniqueId(), "rupiah");
        } catch (Throwable t) {
            myRupiah = (data != null ? data.getCurrency() : 0);
        }

        // 2. Info BP Kamu (Slot 4 - Player Head)
        List<String> bpInfoLore = new ArrayList<>();
        bpInfoLore.add("&7Nama: &f" + player.getName());
        bpInfoLore.add("&7Level BattlePass: &e" + currentLevel + " &8/ &f" + maxLevel);
        bpInfoLore.add("&7Saldo: &eRp." + String.format("%,.0f", myRupiah));
        bpInfoLore.add("&7Peringkat Anda: &e" + rankStr);

        setButton(4, new GuiButton(new ItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(player)
                .name("&6&lInfo BP Kamu")
                .lore(bpInfoLore)
                .build()));

        // Season Clock Banner (Slot 8)
        setButton(8, new GuiButton(new ItemBuilder(Material.CLOCK)
                .name("&6&lSEASON COUNTDOWN")
                .lore(List.of(
                        "&7Sisa Waktu Season: &e" + seasonTimeLeft,
                        "&7Peringkat Anda: &a" + rankStr
                ))
                .build()));

        // 3. Render Pyramid Entries (Center 10 Slots)
        int startIdx = (validPage - 1) * ENTRIES_PER_PAGE;

        for (int i = 0; i < PYRAMID_SLOTS.length; i++) {
            int entryIdx = startIdx + i;
            if (entryIdx >= totalEntries) break;

            LeaderboardEntry entry = list.get(entryIdx);
            OfflinePlayer op = Bukkit.getOfflinePlayer(entry.getUuid());
            String statusText = entry.isOnline() ? "&aOnline" : "&7Offline";
            String rankPrefix = switch (entry.getRank()) {
                case 1 -> "&e&l🥇 #1 ";
                case 2 -> "&f&l🥈 #2 ";
                case 3 -> "&6&l🥉 #3 ";
                default -> "&7#" + entry.getRank() + " ";
            };

            double entryRupiah = 0.0;
            try {
                entryRupiah = com.apexsions.economy.api.ApexsionsEconomyAPI.getBalance(entry.getUuid(), "rupiah");
            } catch (Throwable t) {
                entryRupiah = entry.getCurrency();
            }

            ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                    .skullOwner(op)
                    .name(rankPrefix + "&f" + entry.getPlayerName() + " &8[" + statusText + "&8]")
                    .lore(List.of(
                            "&7Level BattlePass: &e" + entry.getLevel() + " &8/ &f" + plugin.getRewardManager().getMaxLevel(),
                            "&7Saldo: &eRp." + String.format("%,.0f", entryRupiah),
                            "&7Status: " + statusText
                    ))
                    .build();

            setButton(PYRAMID_SLOTS[i], new GuiButton(head, null));
        }

        // 4. Navigation Controls (Row 5)
        setButton(45, new BackButton(this, parent));

        if (validPage > 1) {
            setButton(47, new GuiButton(new ItemBuilder(Material.ARROW).name("&e◀ Top " + ((validPage - 1) * 10)).build(), event -> {
                new BattlePassLeaderboardMenu(plugin, player, parent, validPage - 1).open();
            }));
        }

        setButton(49, new GuiButton(new ItemBuilder(Material.MAP).name("&7Halaman &e" + validPage + " &8/ &f" + maxPages).build()));

        if (validPage < maxPages) {
            setButton(51, new GuiButton(new ItemBuilder(Material.ARROW).name("&eTop " + ((validPage + 1) * 10) + " ▶").build(), event -> {
                new BattlePassLeaderboardMenu(plugin, player, parent, validPage + 1).open();
            }));
        }

        setButton(53, new CloseButton());
    }

    private static String buildProgressBar(int current, int max) {
        int totalBars = 10;
        int progress = max > 0 ? (int) (((double) current / max) * totalBars) : 0;
        progress = Math.min(totalBars, Math.max(0, progress));
        int percent = max > 0 ? (int) (((double) current / max) * 100) : 0;
        percent = Math.min(100, Math.max(0, percent));

        StringBuilder sb = new StringBuilder("&a");
        for (int i = 0; i < progress; i++) {
            sb.append("\u2588");
        }
        sb.append("&7");
        for (int i = progress; i < totalBars; i++) {
            sb.append("\u2588");
        }
        sb.append(" &f(").append(percent).append("%)");
        return sb.toString();
    }
}
