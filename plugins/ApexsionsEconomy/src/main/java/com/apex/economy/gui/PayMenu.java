package com.apex.economy.gui;

import com.apex.economy.gui.core.Gui;
import com.apex.economy.gui.core.GuiButton;
import com.apex.economy.gui.navigation.BackButton;
import com.apex.economy.gui.navigation.CloseButton;
import com.apex.economy.gui.util.ItemBuilder;
import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.currency.Currency;
import com.apex.economy.util.NumberFormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PayMenu extends Gui {

    private final ApexsionsEconomy plugin;
    private Currency selectedCurrency;

    public PayMenu(ApexsionsEconomy plugin, Player player, Gui parent) {
        super(null, player, "&8[ &2&lTRANSFER SALDO (PAY) &8]", 54, parent);
        this.plugin = plugin;
        this.selectedCurrency = plugin.getCurrencyRegistry().getDefault();
    }

    @Override
    public void initialize() {
        fillBorder();

        // 1. Currency Selector Tabs (Slots 2, 3)
        Currency rupiah = plugin.getCurrencyRegistry().get("rupiah");
        Currency diamond = plugin.getCurrencyRegistry().get("diamond");

        if (rupiah != null) {
            setButton(2, new GuiButton(new ItemBuilder(Material.EMERALD)
                    .name((selectedCurrency == rupiah ? "&a&l[âœ”] " : "&7") + "Transfer Rupiah")
                    .lore(List.of(
                            "&7Saldo Anda: &e" + NumberFormatUtil.format(plugin.getCurrencyService().getBalance(player.getUniqueId(), "rupiah"), rupiah),
                            " ",
                            "&eKlik untuk memilih mata uang ini >"
                    ))
                    .build(), event -> {
                this.selectedCurrency = rupiah;
                open();
            }));
        }

        if (diamond != null) {
            setButton(3, new GuiButton(new ItemBuilder(Material.DIAMOND)
                    .name((selectedCurrency == diamond ? "&b&l[âœ”] " : "&7") + "Transfer Diamond")
                    .lore(List.of(
                            "&7Saldo Anda: &e" + NumberFormatUtil.format(plugin.getCurrencyService().getBalance(player.getUniqueId(), "diamond"), diamond),
                            " ",
                            "&eKlik untuk memilih mata uang ini >"
                    ))
                    .build(), event -> {
                this.selectedCurrency = diamond;
                open();
            }));
        }

        // 2. Custom Player Search Button (Slot 6)
        setButton(6, new GuiButton(new ItemBuilder(Material.NAME_TAG)
                .name("&e&l[ðŸ”] KETIK NAMA PLAYER")
                .lore(List.of(
                        "&7Kirim ke pemain online dengan mengetik nama.",
                        " ",
                        "&eKlik untuk memasukkan nama >"
                ))
                .build(), event -> {
            plugin.getChatInputManager().startInput(player, "Masukkan nama penerima transfer:", targetName -> {
                Player target = Bukkit.getPlayer(targetName);
                if (target == null || !target.isOnline()) {
                    player.sendMessage("Â§cPemain " + targetName + " tidak ditemukan atau sedang offline!");
                    open();
                    return;
                }
                if (target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage("Â§cAnda tidak dapat mentransfer saldo ke diri sendiri!");
                    open();
                    return;
                }
                promptAmountAndPay(target);
            }, this::open);
        }));

        // 3. Online Players Grid (Slots 10..16, 19..25, 28..34, 37..43)
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
                            "&7Saat ini tidak ada player lain yang sedang online.",
                            "&7Anda dapat menggunakan tombol cari nama di atas",
                            "&7atau menunggu player lain bergabung."
                    ))
                    .build()));
        } else {
            int idx = 0;
            for (Player target : onlineList) {
                if (idx >= slots.length) break;

                ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                        .skullOwner(target)
                        .name("&e&l" + target.getName() + " &a[Online]")
                        .lore(List.of(
                                "&7Mata uang yang dikirim: &f" + selectedCurrency.getDisplayName(),
                                "&7Saldo Anda: &e" + NumberFormatUtil.format(plugin.getCurrencyService().getBalance(player.getUniqueId(), selectedCurrency.getId()), selectedCurrency),
                                " ",
                                "&aKlik untuk mengirim saldo ke pemain ini >"
                        ))
                        .build();

                setButton(slots[idx++], new GuiButton(head, event -> {
                    promptAmountAndPay(target);
                }));
            }
        }

        // Navigation
        setButton(45, new BackButton(this, parent));
        setButton(53, new CloseButton());
    }

    private void promptAmountAndPay(Player target) {
        plugin.getChatInputManager().startInput(player, "Masukkan jumlah saldo yang ingin dikirim ke " + target.getName() + " (contoh: 1000, 10k, 1.5jt, 2m):", amountStr -> {
            try {
                double amount = NumberFormatUtil.parse(amountStr);
                plugin.getPayService().transfer(player, target.getUniqueId(), target.getName(), selectedCurrency, amount);
                if (parent != null) parent.open();
            } catch (NumberFormatException e) {
                player.sendMessage("Â§cFormat jumlah saldo tidak valid!");
                open();
            }
        }, this::open);
    }
}
