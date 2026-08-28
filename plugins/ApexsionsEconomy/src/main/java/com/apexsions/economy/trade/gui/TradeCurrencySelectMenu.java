package com.apexsions.economy.trade.gui;

import com.apexsions.economy.gui.core.Gui;
import com.apexsions.economy.gui.core.GuiButton;
import com.apexsions.economy.gui.util.ItemBuilder;
import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.currency.Currency;
import com.apexsions.economy.trade.TradeSession;
import com.apexsions.economy.util.NumberFormatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.List;

public class TradeCurrencySelectMenu extends Gui {

    private final ApexsionsEconomy plugin;
    private final TradeSession session;
    private boolean navigatingAway = false;

    public TradeCurrencySelectMenu(ApexsionsEconomy plugin, Player player, TradeSession session) {
        super(null, player, "&8[ &2&lPILIH MATA UANG TRADE &8]", 27, null);
        this.plugin = plugin;
        this.session = session;
    }

    @Override
    public void initialize() {
        fillBackground();

        // 1. Rupiah Option (Slot 11)
        Currency rupiah = plugin.getCurrencyRegistry().get("rupiah");
        if (rupiah != null) {
            double bal = plugin.getCurrencyService().getBalance(player.getUniqueId(), "rupiah");
            setButton(11, new GuiButton(new ItemBuilder(Material.EMERALD)
                    .name("&a&lTawarkan Rupiah")
                    .lore(List.of(
                            "&7Saldo Anda: &e" + NumberFormatUtil.format(bal, rupiah),
                            " ",
                            "&eKlik untuk memasukkan jumlah Rupiah >"
                    ))
                    .build(), event -> {
                promptAmount(rupiah);
            }));
        }

        // 2. Diamond Option (Slot 15)
        Currency diamond = plugin.getCurrencyRegistry().get("diamond");
        if (diamond != null) {
            double bal = plugin.getCurrencyService().getBalance(player.getUniqueId(), "diamond");
            setButton(15, new GuiButton(new ItemBuilder(Material.DIAMOND)
                    .name("&b&lTawarkan Diamond")
                    .lore(List.of(
                            "&7Saldo Anda: &e" + NumberFormatUtil.format(bal, diamond),
                            " ",
                            "&eKlik untuk memasukkan jumlah Diamond >"
                    ))
                    .build(), event -> {
                promptAmount(diamond);
            }));
        }

        // 3. Back Button (Slot 22)
        setButton(22, new GuiButton(new ItemBuilder(Material.ARROW)
                .name("&c&lKEMBALI KE TRADE")
                .lore(List.of("&7Kembali ke menu penawaran barter."))
                .build(), event -> {
            navigatingAway = true;
            session.setTemporarilyClosing(true);
            new TradeMenu(plugin, player, session).open();
        }));
    }

    private void promptAmount(Currency currency) {
        navigatingAway = true;
        session.setTemporarilyClosing(true);

        plugin.getChatInputManager().startInput(player, "Masukkan nominal " + currency.getDisplayName() + " yang ingin Anda tawarkan (contoh: 5000, 50k, 1.5jt, 2m):", amountStr -> {
            try {
                double amount = NumberFormatUtil.parse(amountStr);
                if (amount <= 0) {
                    player.sendMessage("§cNominal transfer harus lebih besar dari 0!");
                } else if (!plugin.getCurrencyService().has(player.getUniqueId(), currency.getId(), amount)) {
                    player.sendMessage("§cSaldo " + currency.getDisplayName() + " Anda tidak mencukupi!");
                } else {
                    session.setMoneyOffer(player, currency, amount);
                    player.sendMessage("§a[✔] Berhasil menambahkan tawaran §e" + NumberFormatUtil.format(amount, currency) + "§a!");
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cFormat nominal tidak valid!");
            }
            session.setTemporarilyClosing(true);
            new TradeMenu(plugin, player, session).open();
        }, () -> {
            session.setTemporarilyClosing(true);
            new TradeMenu(plugin, player, session).open();
        });
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        if (navigatingAway) return;
        if (session.getState() == TradeSession.TradeState.ACTIVE) {
            session.cancelTrade(player, "Menu pemilihan mata uang ditutup");
        }
    }
}
