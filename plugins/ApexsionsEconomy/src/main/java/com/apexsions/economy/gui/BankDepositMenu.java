package com.apexsions.economy.gui;

import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.bank.BankDeposit;
import com.apexsions.economy.currency.Currency;
import com.apexsions.economy.gui.core.Gui;
import com.apexsions.economy.gui.core.GuiButton;
import com.apexsions.economy.gui.navigation.BackButton;
import com.apexsions.economy.gui.navigation.CloseButton;
import com.apexsions.economy.gui.util.ItemBuilder;
import com.apexsions.economy.util.NumberFormatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BankDepositMenu extends Gui {

    private final ApexsionsEconomy plugin;

    public BankDepositMenu(ApexsionsEconomy plugin, Player player, Gui parent) {
        super(null, player, "&8[ &6&lBANK &8- &e&lDEPOSITO BERJANGKA &8]", 45, parent);
        this.plugin = plugin;
    }

    public BankDepositMenu(ApexsionsEconomy plugin, Player player) {
        this(plugin, player, null);
    }

    @Override
    public void initialize() {
        fillBorder();

        Currency rupiah = plugin.getCurrencyRegistry().get("rupiah");
        double userBal = plugin.getCurrencyService().getBalance(player.getUniqueId(), "rupiah");

        // 1. Top Header Info (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.GOLD_BLOCK)
                .name("&6&l✦ BRANKAS DEPOSITO BERJANGKA ✦")
                .lore(List.of(
                        "&7Simpan Rupiah Anda dalam brankas deposito berjangka",
                        "&7untuk memperoleh imbal hasil (bunga) aman dan terukur.",
                        " ",
                        "&7Saldo Rupiah Anda: &a" + NumberFormatUtil.format(userBal, rupiah),
                        "&7Status: &fAnti-Inflasi &8• &aAset Terlindungi"
                ))
                .build()));

        // 2. Package 1: 1 Hari (Slot 10) - 1.5% Yield
        setButton(10, new GuiButton(new ItemBuilder(Material.COPPER_INGOT)
                .name("&e&l[📦] PAKET HARIAN &8(1 HARI)")
                .lore(List.of(
                        "&7Jangka Waktu: &f24 Jam",
                        "&7Imbal Hasil (Bunga): &a+1.5%",
                        "&7Cocok untuk investasi jangka pendek harian.",
                        " ",
                        "&eKlik untuk memilih paket deposito 1 Hari >"
                ))
                .build(), event -> promptDepositAmount(1, 0.015)));

        // 3. Package 2: 3 Hari (Slot 12) - 5.0% Yield
        setButton(12, new GuiButton(new ItemBuilder(Material.GOLD_INGOT)
                .name("&6&l[📦] PAKET FLEKSIBEL &8(3 HARI)")
                .lore(List.of(
                        "&7Jangka Waktu: &f3 Hari (72 Jam)",
                        "&7Imbal Hasil (Bunga): &a+5.0%",
                        "&7Pilihan terpopuler dengan hasil optimal.",
                        " ",
                        "&eKlik untuk memilih paket deposito 3 Hari >"
                ))
                .build(), event -> promptDepositAmount(3, 0.05)));

        // 4. Package 3: 7 Hari (Slot 14) - 12.5% Yield
        setButton(14, new GuiButton(new ItemBuilder(Material.NETHERITE_INGOT)
                .name("&b&l[📦] PAKET MAKSIMAL &8(7 HARI)")
                .lore(List.of(
                        "&7Jangka Waktu: &f7 Hari (168 Jam)",
                        "&7Imbal Hasil (Bunga): &a+12.5%",
                        "&7Imbal hasil tertinggi bagi saudagar berpengalaman.",
                        " ",
                        "&eKlik untuk memilih paket deposito 7 Hari >"
                ))
                .build(), event -> promptDepositAmount(7, 0.125)));

        // 5. Active Deposits List in Bottom Center (Slots 28..34)
        plugin.getBankDepositService().getActiveDeposits(player.getUniqueId()).thenAccept(deposits -> {
            int[] depositSlots = {28, 29, 30, 31, 32, 33, 34};
            int idx = 0;

            for (BankDeposit dep : deposits) {
                if (idx >= depositSlots.length) break;

                boolean ready = dep.isMatured();
                Material mat = ready ? Material.EMERALD : Material.CLOCK;
                String status = ready ? "&a&l[SIAP DICAIRKAN]" : "&eSisa: " + dep.getTimeRemainingFormatted();

                ItemStack item = new ItemBuilder(mat)
                        .name("&eDeposito #" + dep.getId() + " " + status)
                        .lore(List.of(
                                "&7Pokok: &e" + NumberFormatUtil.format(dep.getAmount(), rupiah),
                                "&7Hasil Akhir: &a&l" + NumberFormatUtil.format(dep.getExpectedReturn(), rupiah) + " &7(+" + String.format("%.1f", dep.getInterestRate() * 100) + "%)",
                                "&7Status: " + status,
                                " ",
                                ready ? "&a▶ Klik untuk mencairkan saldo sekarang!" : "&cBelum jatuh tempo."
                        ))
                        .build();

                setButton(depositSlots[idx++], new GuiButton(item, ev -> {
                    if (ready) {
                        plugin.getBankDepositService().claimDeposit(player, dep).thenAccept(success -> {
                            if (success) {
                                new BankDepositMenu(plugin, player, parent).open();
                            }
                        });
                    }
                }));
            }

            if (deposits.isEmpty()) {
                setButton(31, new GuiButton(new ItemBuilder(Material.BARRIER)
                        .name("&7Belum Ada Deposito Aktif")
                        .lore(List.of(
                                "&7Pilih salah satu paket di atas untuk",
                                "&7mulai menabung di brankas kerajaan."
                        ))
                        .build()));
            }
        });

        // 6. Navigation
        setButton(36, new BackButton(this, parent));
        setButton(44, new CloseButton());
    }

    private void promptDepositAmount(int days, double rate) {
        plugin.getChatInputManager().startInput(player, "Masukkan nominal Rupiah yang ingin didepositokan (contoh: 100k, 500000, max):", input -> {
            double amount;
            double balance = plugin.getCurrencyService().getBalance(player.getUniqueId(), "rupiah");

            if (input.equalsIgnoreCase("max") || input.equalsIgnoreCase("all") || input.equalsIgnoreCase("semua")) {
                amount = balance;
            } else {
                amount = parseAmount(input);
            }

            if (amount <= 0) {
                player.sendMessage("§cNominal tidak valid.");
                this.open();
                return;
            }

            plugin.getBankDepositService().createDeposit(player, "rupiah", amount, days, rate);
            this.open();
        }, this::open);
    }

    private double parseAmount(String input) {
        if (input == null) return 0;
        String clean = input.trim().toLowerCase().replace(",", "").replace(".", "");
        double mult = 1.0;
        if (clean.endsWith("k")) {
            mult = 1_000;
            clean = clean.substring(0, clean.length() - 1);
        } else if (clean.endsWith("m") || clean.endsWith("jt")) {
            mult = 1_000_000;
            clean = clean.substring(0, clean.length() - (clean.endsWith("jt") ? 2 : 1));
        } else if (clean.endsWith("b") || clean.endsWith("mly")) {
            mult = 1_000_000_000;
            clean = clean.substring(0, clean.length() - (clean.endsWith("mly") ? 3 : 1));
        }
        try {
            return Double.parseDouble(clean) * mult;
        } catch (Exception e) {
            return 0;
        }
    }
}
