package com.apex.economy.trade.gui;

import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.gui.core.Gui;
import com.apex.economy.gui.core.GuiButton;
import com.apex.economy.gui.navigation.BackButton;
import com.apex.economy.gui.navigation.CloseButton;
import com.apex.economy.gui.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TradePlayerSelectMenu extends Gui {

    private final ApexsionsEconomy plugin;
    private boolean globalFilter = false; // Default: Kingdom Only

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

        String myKingdom = plugin.getApexsionsCoreHook().getPlayerKingdom(player.getUniqueId());
        String myKingdomDisplay = myKingdom.equalsIgnoreCase("NONE") ? "Tanpa Kerajaan" : myKingdom;
        double transportFee = plugin.getConfig().getDouble("trade.cross-kingdom-transport-fee", 5000.0);

        // 1. Info Card (Slot 2)
        setButton(2, new GuiButton(new ItemBuilder(Material.COMPASS)
                .name("&6&lSistem Barter & Trade")
                .lore(List.of(
                        "&7Pilih pemain online untuk barter",
                        "&7item & uang secara aman dan anti-scam.",
                        " ",
                        "&7Kerajaan Anda: &6" + myKingdomDisplay,
                        "&7Biaya Lintas-Kerajaan: &eRp " + String.format("%,.0f", transportFee)
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

        // 4. Kingdom Filter Toggle Button (Slot 8)
        if (!globalFilter) {
            setButton(8, new GuiButton(new ItemBuilder(Material.GOLDEN_HELMET)
                    .name("&6&l[👑] FILTER: SESAMA KERAJAAN")
                    .lore(List.of(
                            "&7Menampilkan pemain dari &6" + myKingdomDisplay + "&7.",
                            "&aBiaya Transportasi: &2GRATIS (Rp 0)",
                            " ",
                            "&eKlik untuk beralih ke Mode Global (Semua Kerajaan) >"
                    ))
                    .build(), event -> {
                this.globalFilter = true;
                open();
            }));
        } else {
            setButton(8, new GuiButton(new ItemBuilder(Material.ENDER_EYE)
                    .name("&b&l[🌐] FILTER: SEMUA KERAJAAN (GLOBAL)")
                    .lore(List.of(
                            "&7Menampilkan pemain dari seluruh kerajaan.",
                            "&eBiaya Transportasi Lintas-Kerajaan: &6Rp " + String.format("%,.0f", transportFee),
                            " ",
                            "&eKlik untuk beralih ke Mode Sesama Kerajaan >"
                    ))
                    .build(), event -> {
                this.globalFilter = false;
                open();
            }));
        }

        // 5. Online Players Grid
        List<Player> filteredList = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getUniqueId().equals(player.getUniqueId())) {
                if (globalFilter) {
                    filteredList.add(p);
                } else {
                    // Filter to same kingdom only
                    if (myKingdom.equalsIgnoreCase("NONE") || plugin.getApexsionsCoreHook().isSameKingdom(player.getUniqueId(), p.getUniqueId())) {
                        filteredList.add(p);
                    }
                }
            }
        }

        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        if (filteredList.isEmpty()) {
            String emptyMessage = globalFilter
                    ? "&7Saat ini tidak ada pemain lain yang online."
                    : "&7Tidak ada anggota kerajaan &e" + myKingdomDisplay + " &7yang online.\n&eGunakan tombol filter di atas untuk melihat pemain global.";

            setButton(22, new GuiButton(new ItemBuilder(Material.BARRIER)
                    .name("&c&lTIDAK ADA PEMAIN DITEMUKAN")
                    .lore(List.of(emptyMessage.split("\n")))
                    .build(), null));
        } else {
            int idx = 0;
            for (Player target : filteredList) {
                if (idx >= slots.length) break;

                boolean inTrade = plugin.getTradeManager().isInTrade(target);
                boolean targetTradeEnabled = plugin.getTradeManager().isTradeEnabled(target.getUniqueId());
                String targetKingdom = plugin.getApexsionsCoreHook().getPlayerKingdom(target.getUniqueId());
                String targetKingdomDisplay = targetKingdom.equalsIgnoreCase("NONE") ? "Tanpa Kerajaan" : targetKingdom;
                boolean sameKingdom = plugin.getApexsionsCoreHook().isSameKingdom(player.getUniqueId(), target.getUniqueId());

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

                String feeLore = sameKingdom
                        ? "&aBiaya Transportasi: &2GRATIS (Sesama Kerajaan)"
                        : "&eBiaya Transportasi: &6Rp " + String.format("%,.0f", transportFee) + " &7(Dibayar saat konfirmasi)";

                ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                        .skullOwner(target)
                        .name("&e&l" + target.getName() + " " + statusTag)
                        .lore(List.of(
                                "&7Kerajaan: &6" + targetKingdomDisplay,
                                feeLore,
                                " ",
                                statusLore,
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
