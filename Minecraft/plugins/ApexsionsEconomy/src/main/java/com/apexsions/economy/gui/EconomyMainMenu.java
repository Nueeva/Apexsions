package com.apexsions.economy.gui;

import com.apexsions.economy.gui.core.Gui;
import com.apexsions.economy.gui.core.GuiButton;
import com.apexsions.economy.gui.navigation.CloseButton;
import com.apexsions.economy.gui.util.ItemBuilder;
import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.currency.Currency;
import com.apexsions.economy.util.NumberFormatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EconomyMainMenu extends Gui {

    private final ApexsionsEconomy plugin;

    public EconomyMainMenu(ApexsionsEconomy plugin, Player player, Gui parent) {
        super(null, player, "&8[ &2&lAPEXSIONS ECONOMY &8]", 45, parent);
        this.plugin = plugin;
    }

    public EconomyMainMenu(ApexsionsEconomy plugin, Player player) {
        this(plugin, player, null);
    }

    @Override
    public void initialize() {
        fillBackground();

        // 1. Player Profile & Balances Summary (Slot 13)
        Currency rupiah = plugin.getCurrencyRegistry().get("rupiah");
        Currency diamond = plugin.getCurrencyRegistry().get("diamond");
        double rupiahBal = plugin.getCurrencyService().getBalance(player.getUniqueId(), "rupiah");
        double diamondBal = plugin.getCurrencyService().getBalance(player.getUniqueId(), "diamond");
        int rupiahRank = plugin.getLeaderboardService().getPlayerRank(player.getUniqueId(), "rupiah");
        int diamondRank = plugin.getLeaderboardService().getPlayerRank(player.getUniqueId(), "diamond");

        List<String> profileLore = new ArrayList<>();
        profileLore.add("&7Nama: &f" + player.getName());
        profileLore.add("&7Saldo Rupiah: &e" + (rupiah != null ? NumberFormatUtil.format(rupiahBal, rupiah) : ("Rp" + String.format("%,.0f", rupiahBal))));
        profileLore.add("&7Saldo Diamond: &e" + (diamond != null ? NumberFormatUtil.format(diamondBal, diamond) : (String.format("%,.0f", diamondBal) + " Diamond")));
        profileLore.add("&7Format Lengkap: &f" + (rupiah != null ? NumberFormatUtil.formatFull(rupiahBal, rupiah) : ("Rp" + String.format("%,.0f", rupiahBal))));
        profileLore.add("&7Peringkat Rupiah: &e" + (rupiahRank > 0 && rupiahRank <= 100 ? "#" + rupiahRank : "Belum Masuk Top 100"));
        profileLore.add("&7Peringkat Diamond: &e" + (diamondRank > 0 && diamondRank <= 100 ? "#" + diamondRank : "Belum Masuk Top 100"));
        profileLore.add(" ");
        profileLore.add("&7Gunakan mata uang untuk transaksi, lelang,");
        profileLore.add("&7maupun berbelanja di BattlePass Shop.");

        setButton(13, new GuiButton(new ItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(player)
                .name("&6&lProfil Kamu")
                .lore(profileLore)
                .build()));

        // 2. Pay Button (Slot 19)
        setButton(19, new GuiButton(new ItemBuilder(Material.GOLD_INGOT)
                .name("&e&l[💸] KIRIM UANG (PAY)")
                .lore(List.of(
                        "&7Transfer saldo Rupiah atau Diamond",
                        "&7secara instan ke sesama pemain.",
                        " ",
                        "&eKlik untuk membuka menu transfer >"
                ))
                .build(), event -> {
            new PayMenu(plugin, player, this).open();
        }));

        // 3. Trade Button (Slot 21)
        setButton(21, new GuiButton(new ItemBuilder(Material.EMERALD)
                .name("&a&l[🤝] SISTEM TRADE & BARTER")
                .lore(List.of(
                        "&7Tukar item dan saldo secara aman",
                        "&7dan real-time dengan pemain lain.",
                        " ",
                        "&aKlik untuk memilih pemain trade >"
                ))
                .build(), event -> {
            new com.apexsions.economy.trade.gui.TradePlayerSelectMenu(plugin, player, this).open();
        }));

        // 4. Leaderboard Button (Slot 23)
        setButton(23, new GuiButton(new ItemBuilder(Material.NETHER_STAR)
                .name("&6&l[🏆] LEADERBOARD KEKAYAAN")
                .lore(List.of(
                        "&7Lihat daftar pemain terkaya di server",
                        "&7untuk Rupiah dan Diamond.",
                        " ",
                        "&6Klik untuk membuka peringkat >"
                ))
                .build(), event -> {
            new EconomyLeaderboardMenu(plugin, player, "rupiah", this).open();
        }));

        // 5. Auction House Button (Slot 25)
        setButton(25, new GuiButton(new ItemBuilder(Material.GOLDEN_HORSE_ARMOR)
                .name("&b&l[🏛] AUCTION HOUSE (LELANG)")
                .lore(List.of(
                        "&7Pasar lelang aman antar pemain.",
                        "&7Jual dan beli barang langka menggunakan",
                        "&7Rupiah atau Diamond dengan sistem escrow.",
                        " ",
                        "&bKlik untuk membuka pasar lelang >"
                ))
                .build(), event -> {
            new AuctionBrowseMenu(plugin, player, this).open();
        }));

        // 6. Bank & Deposito Button (Slot 31)
        setButton(31, new GuiButton(new ItemBuilder(Material.GOLD_BLOCK)
                .name("&6&l[🏛] BRANKAS & DEPOSITO BERJANGKA")
                .lore(List.of(
                        "&7Simpan uang Rupiah Anda di brankas kerajaan",
                        "&7dan peroleh bunga imbal hasil terukur (1.5% - 12.5%).",
                        " ",
                        "&6Klik untuk membuka menu deposito >"
                ))
                .build(), event -> {
            new BankDepositMenu(plugin, player, this).open();
        }));

        // Close Button
        setButton(40, new CloseButton());
    }
}
