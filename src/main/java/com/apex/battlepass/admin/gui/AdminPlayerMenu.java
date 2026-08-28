package com.apex.battlepass.admin.gui;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.navigation.CloseButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AdminPlayerMenu extends Gui {

    private static final int PLAYERS_PER_PAGE = 28;
    private final int page;
    private final String filter;

    public AdminPlayerMenu(ApexsionsBattlepass plugin, Player player, Gui parent, int page, String filter) {
        super(plugin, player, plugin.getGuiConfig().getString("titles.admin-player", "&8[ &4&lABP PLAYER MANAGEMENT &8- Hal. %page% ]").replace("%page%", String.valueOf(page)), 54, parent);
        this.page = Math.max(1, page);
        this.filter = filter != null ? filter.trim().toLowerCase() : "";
    }

    public AdminPlayerMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        this(plugin, player, parent, 1, "");
    }

    @Override
    public void initialize() {
        fillBackground();

        int seasonId = plugin.getSeasonManager().getCurrentSeason().getId();
        // Load all players (offline + cached in memory)
        Map<UUID, PlayerData> allPlayers = new HashMap<>();
        try {
            List<PlayerData> dbList = plugin.getRepository().loadAllPlayerData(seasonId).get();
            for (PlayerData d : dbList) allPlayers.put(d.getUuid(), d);
        } catch (Exception ignored) {}
        for (PlayerData mem : plugin.getPlayerManager().getPlayerDataCache().values()) {
            allPlayers.put(mem.getUuid(), mem);
        }

        List<PlayerData> filteredList = new ArrayList<>();
        for (PlayerData d : allPlayers.values()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(d.getUuid());
            String name = op.getName() != null ? op.getName() : "Player_" + d.getUuid().toString().substring(0, 6);
            if (filter.isEmpty() || name.toLowerCase().contains(filter)) {
                filteredList.add(d);
            }
        }

        // Sort: Online players first, then Level DESC, then name
        filteredList.sort((a, b) -> {
            boolean aOnline = Bukkit.getOfflinePlayer(a.getUuid()).isOnline();
            boolean bOnline = Bukkit.getOfflinePlayer(b.getUuid()).isOnline();
            if (aOnline != bOnline) return aOnline ? -1 : 1;
            int cmp = Integer.compare(b.getLevel(), a.getLevel());
            if (cmp != 0) return cmp;
            return a.getUuid().compareTo(b.getUuid());
        });

        int totalPlayers = filteredList.size();
        int maxPages = Math.max(1, (int) Math.ceil((double) totalPlayers / PLAYERS_PER_PAGE));
        int validPage = Math.max(1, Math.min(maxPages, page));

        // 1. Header Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.COMPASS)
                .name("&e&lDAFTAR SELURUH PLAYER (" + totalPlayers + ")")
                .lore(List.of(
                        "&7Menampilkan seluruh pemain (Online & Offline).",
                        "&7Filter saat ini: &f" + (filter.isEmpty() ? "Semua" : filter),
                        " ",
                        "&7Total Pemain Terdata: &e" + totalPlayers
                ))
                .build()));

        // Search Filter Button (Slot 8)
        setButton(8, new GuiButton(new ItemBuilder(Material.NAME_TAG)
                .name("&a&l[🔍] CARI NAMA PLAYER")
                .lore(List.of(
                        "&7Klik untuk mencari pemain berdasarkan nama.",
                        " ",
                        "&eKlik untuk memasukkan nama >"
                ))
                .build(), event -> {
            plugin.getChatInputManager().startInput(player, "Masukkan nama pemain yang ingin dicari (atau 'semua' untuk reset):", input -> {
                String f = (input.equalsIgnoreCase("semua") || input.equalsIgnoreCase("all") || input.equalsIgnoreCase("reset")) ? "" : input.trim();
                new AdminPlayerMenu(plugin, player, parent, 1, f).open();
            }, this::open);
        }));

        // 2. Render Player Heads (Slots 10..16, 19..25, 28..34, 37..43)
        int[] playerSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int startIdx = (validPage - 1) * PLAYERS_PER_PAGE;
        int slotIdx = 0;

        for (int i = startIdx; i < totalPlayers && slotIdx < playerSlots.length; i++) {
            PlayerData data = filteredList.get(i);
            OfflinePlayer op = Bukkit.getOfflinePlayer(data.getUuid());
            String name = op.getName() != null ? op.getName() : "Player_" + data.getUuid().toString().substring(0, 6);
            boolean isOnline = op.isOnline();
            int reqXp = plugin.getRewardManager().getRequiredXp(data.getLevel());

            ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                    .name((isOnline ? "&a● &e&l" : "&7○ &f") + name)
                    .lore(List.of(
                            "&7Status: " + (isOnline ? "&aOnline" : "&7Offline"),
                            "&7Level: &e" + data.getLevel() + " &8/ &f" + plugin.getRewardManager().getMaxLevel(),
                            "&7XP: &a" + data.getXp() + " &8/ &f" + reqXp,
                            "&7Battle Coins: &e" + plugin.getCurrencyService().format(data.getCurrency()),
                            "&7Passes: &b" + String.join(", ", data.getPasses()).toUpperCase(),
                            "&7Refresh Hari Ini: &f" + data.getDailyRefreshCount() + " kali",
                            " ",
                            "&eKlik untuk mengelola pemain ini >"
                    ))
                    .build();

            setButton(playerSlots[slotIdx++], new GuiButton(head, event -> {
                new AdminPlayerDetailMenu(plugin, player, data.getUuid(), name, this).open();
            }));
        }

        // 3. Navigation Controls (Row 5)
        setButton(45, new BackButton(this, parent));

        if (validPage > 1) {
            setButton(48, new GuiButton(new ItemBuilder(Material.PAPER).name("&e< Halaman " + (validPage - 1)).build(), event -> {
                new AdminPlayerMenu(plugin, player, parent, validPage - 1, filter).open();
            }));
        }

        setButton(49, new GuiButton(new ItemBuilder(Material.BOOK).name("&7Halaman &e" + validPage + " &8/ &f" + maxPages).build()));

        if (validPage < maxPages) {
            setButton(50, new GuiButton(new ItemBuilder(Material.PAPER).name("&eHalaman " + (validPage + 1) + " >").build(), event -> {
                new AdminPlayerMenu(plugin, player, parent, validPage + 1, filter).open();
            }));
        }

        setButton(53, new CloseButton());
    }
}

