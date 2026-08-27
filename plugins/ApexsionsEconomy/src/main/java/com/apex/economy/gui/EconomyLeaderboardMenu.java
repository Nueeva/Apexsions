package com.apex.economy.gui;

import com.apex.economy.gui.core.Gui;
import com.apex.economy.gui.core.GuiButton;
import com.apex.economy.gui.navigation.BackButton;
import com.apex.economy.gui.navigation.CloseButton;
import com.apex.economy.gui.util.ItemBuilder;
import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.currency.Currency;
import com.apex.economy.leaderboard.EconomyLeaderboardEntry;
import com.apex.economy.util.NumberFormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EconomyLeaderboardMenu extends Gui {

    private static final int ENTRIES_PER_PAGE = 10;
    // 10-slot symmetrical pyramid: Row 1 (1 slot: 13), Row 2 (2 slots: 21, 23), Row 3 (3 slots: 29, 31, 33), Row 4 (4 slots: 37, 39, 41, 43)
    private static final int[] PYRAMID_SLOTS = { 13, 21, 23, 29, 31, 33, 37, 39, 41, 43 };

    private final ApexsionsEconomy plugin;
    private final String currencyId;
    private final int page;

    public EconomyLeaderboardMenu(ApexsionsEconomy plugin, Player player, String currencyId, Gui parent, int page) {
        super(null, player, "&8[ &6&lTOP " + (Math.max(1, Math.min(10, page)) * 10) + " KEKAYAAN: &e" + currencyId.toUpperCase() + " &8]", 54, parent);
        this.plugin = plugin;
        this.currencyId = currencyId.toLowerCase();
        this.page = Math.max(1, Math.min(10, page));
    }

    public EconomyLeaderboardMenu(ApexsionsEconomy plugin, Player player, String currencyId, Gui parent) {
        this(plugin, player, currencyId, parent, 1);
    }

    @Override
    public void initialize() {
        fillBorder();

        Currency curr = plugin.getCurrencyRegistry().get(currencyId);
        List<EconomyLeaderboardEntry> list = plugin.getLeaderboardService().getLeaderboard(currencyId);

        int total = Math.min(100, list.size());
        int maxPages = Math.min(10, Math.max(1, (int) Math.ceil((double) total / ENTRIES_PER_PAGE)));
        int validPage = Math.max(1, Math.min(maxPages, page));

        int myRank = plugin.getLeaderboardService().getPlayerRank(player.getUniqueId(), currencyId);
        double myBal = plugin.getCurrencyService().getBalance(player.getUniqueId(), currencyId);
        String rankStr = (myRank > 0 && myRank <= 100) ? ("#" + myRank) : "Belum Masuk Top 100";

        // 1. Leaderboard Info Banner (Slot 0)
        setButton(0, new GuiButton(new ItemBuilder(Material.NETHER_STAR)
                .name("&6&lTOP KEKAYAAN: &e" + currencyId.toUpperCase())
                .lore(List.of(
                        "&7Daftar pemain terkaya di server",
                        "&7berdasarkan saldo " + (curr != null ? curr.getDisplayName() : currencyId) + ".",
                        " ",
                        "&7Peringkat Anda: &e" + rankStr,
                        "&7Total Terdata: &b" + total + " pemain"
                ))
                .build()));

        // Currency Tabs (Slots 1, 2)
        setButton(1, new GuiButton(new ItemBuilder(Material.EMERALD)
                .name(currencyId.equals("rupiah") ? "&a&l[âœ”] LEADERBOARD RUPIAH" : "&a&lLEADERBOARD RUPIAH")
                .lore(List.of("&7Peringkat kekayaan Rupiah (Top 100).", " ", "&eKlik untuk beralih >"))
                .build(), event -> {
            new EconomyLeaderboardMenu(plugin, player, "rupiah", parent, 1).open();
        }));

        setButton(2, new GuiButton(new ItemBuilder(Material.DIAMOND)
                .name(currencyId.equals("diamond") ? "&b&l[âœ”] LEADERBOARD DIAMOND" : "&b&lLEADERBOARD DIAMOND")
                .lore(List.of("&7Peringkat kekayaan Diamond (Top 100).", " ", "&eKlik untuk beralih >"))
                .build(), event -> {
            new EconomyLeaderboardMenu(plugin, player, "diamond", parent, 1).open();
        }));

        // 2. Profil Kamu Card (Slot 4 - Player Head)
        List<String> profileLore = new ArrayList<>();
        profileLore.add("&7Nama: &f" + player.getName());
        profileLore.add("&7Saldo: &e" + NumberFormatUtil.format(myBal, curr));
        profileLore.add("&7Peringkat Anda: &e" + rankStr);
        profileLore.add("&7Mata Uang: &f" + (curr != null ? curr.getDisplayName() : currencyId));

        setButton(4, new GuiButton(new ItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(player)
                .name("&6&lProfil Kamu")
                .lore(profileLore)
                .build()));

        // 3. Render Pyramid Entries
        int startIdx = (validPage - 1) * ENTRIES_PER_PAGE;

        for (int i = 0; i < PYRAMID_SLOTS.length; i++) {
            int entryIdx = startIdx + i;
            if (entryIdx >= total) break;

            EconomyLeaderboardEntry entry = list.get(entryIdx);
            OfflinePlayer op = Bukkit.getOfflinePlayer(entry.getUuid());
            String name = op.getName() != null ? op.getName() : "Pemain_" + entry.getRank();
            String statusText = op.isOnline() ? "&aOnline" : "&7Offline";

            String rankPrefix = switch (entry.getRank()) {
                case 1 -> "&e&lðŸ¥‡ #1 ";
                case 2 -> "&f&lðŸ¥ˆ #2 ";
                case 3 -> "&6&lðŸ¥‰ #3 ";
                default -> "&7#" + entry.getRank() + " ";
            };

            ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                    .skullOwner(op)
                    .name(rankPrefix + "&f" + name + " &8[" + statusText + "&8]")
                    .lore(List.of(
                            "&7Saldo: &e&l" + NumberFormatUtil.format(entry.getBalance(), curr),
                            "&7Status: " + statusText
                    ))
                    .build();

            setButton(PYRAMID_SLOTS[i], new GuiButton(head, null));
        }

        // 4. Navigation Controls
        setButton(45, new BackButton(this, parent));

        if (validPage > 1) {
            setButton(47, new GuiButton(new ItemBuilder(Material.ARROW).name("&eâ—€ Top " + ((validPage - 1) * 10)).build(), event -> {
                new EconomyLeaderboardMenu(plugin, player, currencyId, parent, validPage - 1).open();
            }));
        }

        setButton(49, new GuiButton(new ItemBuilder(Material.MAP).name("&7Halaman &e" + validPage + " &8/ &f" + maxPages).build()));

        if (validPage < maxPages) {
            setButton(51, new GuiButton(new ItemBuilder(Material.ARROW).name("&eTop " + ((validPage + 1) * 10) + " â–¶").build(), event -> {
                new EconomyLeaderboardMenu(plugin, player, currencyId, parent, validPage + 1).open();
            }));
        }

        setButton(53, new CloseButton());
    }
}
