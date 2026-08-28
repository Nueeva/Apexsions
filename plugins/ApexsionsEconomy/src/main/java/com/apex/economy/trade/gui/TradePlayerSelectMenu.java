package com.apex.economy.trade.gui;

import com.apex.economy.gui.core.Gui;
import com.apex.economy.gui.core.GuiButton;
import com.apex.economy.gui.navigation.BackButton;
import com.apex.economy.gui.navigation.CloseButton;
import com.apex.economy.gui.util.ItemBuilder;
import com.apex.economy.ApexsionsEconomy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TradePlayerSelectMenu extends Gui {

    private final ApexsionsEconomy plugin;

    public TradePlayerSelectMenu(ApexsionsEconomy plugin, Player player, Gui parent) {
        super(null, player, "&8[ &2&lPILIH PEMAIN UNTUK TRADE &8]", 54, parent);
        this.plugin = plugin;
    }

    public TradePlayerSelectMenu(ApexsionsEconomy plugin, Player player) {
        this(plugin, player, null);
    }

    @Override
    public void initialize() {
        fillBorder();

        // 1. Info Card (Slot 2)
        setButton(2, new GuiButton(new ItemBuilder(Material.COMPASS)
                .name("&6&lSistem Barter & Trade")
                .lore(List.of(
                        "&7Pilih pemain online untuk mengirim",
                        "&7permintaan barter item & uang.",
                        " ",
                        "&aAman, Real-time & Anti-Scam!"
                ))
                .build(), null));

        // 2. Trade Toggle Button (Slot 4)
        boolean myTradeEnabled = plugin.getTradeManager().isTradeEnabled(player.getUniqueId());
        setButton(4, new GuiButton(new ItemBuilder(myTradeEnabled ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(myTradeEnabled ? "&a&l[✔] STATUS TRADE ANDA: AKTIF" : "&c&l[✖] STATUS TRADE ANDA: NONAKTIF")
                .lore(List.of(
                        myTradeEnabled ? "&7Pemain lain dapat mengirim permintaan trade ke Anda." : "&7Pemain lain TIDAK DAPAT mengirim permintaan trade ke Anda.",
                        " ",
                        myTradeEnabled ? "&eKlik untuk menonaktifkan trade >" : "&aKlik untuk mengaktifkan kembali trade >"
                ))
                .build(), event -> {
            plugin.getTradeManager().toggleTrade(player);
            open();
        }));

        // 3. Custom Player Search Button (Slot 6)
        setButton(6, new GuiButton(new ItemBuilder(Material.NAME_TAG)
                .name("&e&l[🔍] KETIK NAMA PEMAIN")
                .lore(List.of(
                        "&7Kirim permintaan trade dengan",
                        "&7mengetik nama pemain di chat.",
                        " ",
                        "&eKlik untuk memasukkan nama >"
                ))
                .build(), event -> {
            plugin.getChatInputManager().startInput(player, "Masukkan nama pemain yang ingin diajak trade:", targetName -> {
                Player target = Bukkit.getPlayer(targetName);
                if (target == null || !target.isOnline()) {
                    player.sendMessage("§cPemain " + targetName + " tidak ditemukan atau sedang offline!");
                    open();
                    return;
                }
                plugin.getTradeManager().sendRequest(player, target);
            }, this::open);
        }));

        // 4. Online Players Grid
        List<Player> onlineList = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getUniqueId().equals(player.getUniqueId())) {
                onlineList.add(p);
            }
        }

        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        if (onlineList.isEmpty()) {
            setButton(22, new GuiButton(new ItemBuilder(Material.BARRIER)
                    .name("&c&lTIDAK ADA PLAYER LAIN ONLINE")
                    .lore(List.of(
                            "&7Saat ini tidak ada pemain lain yang sedang online.",
                            "&7Anda dapat menunggu pemain lain bergabung."
                    ))
                    .build(), null));
        } else {
            int idx = 0;
            for (Player target : onlineList) {
                if (idx >= slots.length) break;

                boolean inTrade = plugin.getTradeManager().isInTrade(target);
                boolean targetTradeEnabled = plugin.getTradeManager().isTradeEnabled(target.getUniqueId());

                String statusTag;
                String statusLore;
                String actionLore;

                if (!targetTradeEnabled) {
                    statusTag = "&c[Trade Nonaktif]";
                    statusLore = "&7Status: &cMenonaktifkan permintaan trade";
                    actionLore = "&cTidak dapat diajak trade saat ini.";
                } else if (inTrade) {
                    statusTag = "&c[Sedang Trade]";
                    statusLore = "&7Status: &cSedang berada dalam sesi trade";
                    actionLore = "&cTidak dapat diajak trade saat ini.";
                } else {
                    statusTag = "&a[Tersedia]";
                    statusLore = "&7Status: &aSiap untuk diajak trade";
                    actionLore = "&aKlik untuk mengirim permintaan trade >";
                }

                ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                        .skullOwner(target)
                        .name("&e&l" + target.getName() + " " + statusTag)
                        .lore(List.of(
                                statusLore,
                                " ",
                                actionLore
                        ))
                        .build();

                setButton(slots[idx++], new GuiButton(head, event -> {
                    if (!targetTradeEnabled) {
                        player.sendMessage("§cPemain §e" + target.getName() + " §csedang menonaktifkan permintaan trade!");
                        return;
                    }
                    if (inTrade) {
                        player.sendMessage("§cPemain §e" + target.getName() + " §csedang berada dalam sesi trade lain!");
                        return;
                    }
                    player.closeInventory();
                    plugin.getTradeManager().sendRequest(player, target);
                }));
            }
        }


        // Navigation
        if (parent != null) {
            setButton(45, new BackButton(this, parent));
        }
        setButton(53, new CloseButton());
    }
}
