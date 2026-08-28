package com.apex.economy.trade.gui;

import com.apex.economy.gui.core.Gui;
import com.apex.economy.gui.core.GuiButton;
import com.apex.economy.gui.util.ItemBuilder;
import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.trade.TradeOffer;
import com.apex.economy.trade.TradeSession;
import com.apex.economy.util.NumberFormatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.List;

public class TradeCurrencyEditMenu extends Gui {

    private final ApexsionsEconomy plugin;
    private final TradeSession session;
    private boolean navigatingAway = false;

    public TradeCurrencyEditMenu(ApexsionsEconomy plugin, Player player, TradeSession session) {
        super(null, player, "&8[ &2&lPENGATURAN TAWARAN SALDO &8]", 27, null);
        this.plugin = plugin;
        this.session = session;
    }

    @Override
    public void initialize() {
        fillBackground();

        TradeOffer myOffer = session.getOffer(player);
        String currentFormatted = (myOffer != null && myOffer.getCurrency() != null)
                ? NumberFormatUtil.format(myOffer.getMoneyAmount(), myOffer.getCurrency())
                : "Tidak ada";
        String currName = (myOffer != null && myOffer.getCurrency() != null)
                ? myOffer.getCurrency().getDisplayName()
                : "None";

        // 1. Current Offer Info Card (Slot 13)
        setButton(13, new GuiButton(new ItemBuilder(Material.GOLD_INGOT)
                .name("&6&lTawaran Saldo Saat Ini")
                .lore(List.of(
                        "&7Mata Uang: &f" + currName,
                        "&7Nominal: &e" + currentFormatted,
                        " ",
                        "&7Pilih opsi di bawah untuk mengubah",
                        "&7atau menghapus tawaran saldo ini."
                ))
                .build(), null));

        // 2. Change / Re-select Currency & Amount (Slot 11)
        setButton(11, new GuiButton(new ItemBuilder(Material.ANVIL)
                .name("&e&l[✏] UBAH JUMLAH / MATA UANG")
                .lore(List.of(
                        "&7Pilih kembali mata uang dan masukkan",
                        "&7nominal baru untuk tawaran ini.",
                        " ",
                        "&eKlik untuk mengubah >"
                ))
                .build(), event -> {
            navigatingAway = true;
            session.setTemporarilyClosing(true);
            new TradeCurrencySelectMenu(plugin, player, session).open();
        }));

        // 3. Remove Money Offer (Slot 15)
        setButton(15, new GuiButton(new ItemBuilder(Material.LAVA_BUCKET)
                .name("&c&l[✖] HAPUS TAWARAN SALDO")
                .lore(List.of(
                        "&7Batalkan penawaran saldo/diamond ini.",
                        "&7Status konfirmasi akan direset.",
                        " ",
                        "&cKlik untuk menghapus tawaran >"
                ))
                .build(), event -> {
            navigatingAway = true;
            session.clearMoneyOffer(player);
            player.sendMessage("§e[!] Tawaran saldo telah dihapus.");
            session.setTemporarilyClosing(true);
            new TradeMenu(plugin, player, session).open();
        }));

        // 4. Back Button (Slot 22)
        setButton(22, new GuiButton(new ItemBuilder(Material.ARROW)
                .name("&7&lKEMBALI KE TRADE")
                .lore(List.of("&7Kembali tanpa mengubah tawaran."))
                .build(), event -> {
            navigatingAway = true;
            session.setTemporarilyClosing(true);
            new TradeMenu(plugin, player, session).open();
        }));
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        if (navigatingAway) return;
        if (session.getState() == TradeSession.TradeState.ACTIVE) {
            session.cancelTrade(player, "Menu pengaturan saldo ditutup");
        }
    }
}
