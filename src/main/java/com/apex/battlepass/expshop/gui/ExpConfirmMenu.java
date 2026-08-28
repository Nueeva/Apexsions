package com.apex.battlepass.expshop.gui;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.expshop.model.ExpPurchaseContext;
import com.apex.battlepass.expshop.provider.ExpShopCurrencyProvider;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class ExpConfirmMenu extends Gui {

    private final ExpPurchaseContext context;

    public ExpConfirmMenu(ApexsionsBattlepass plugin, Player player, Gui parent, ExpPurchaseContext context) {
        super(plugin, player, "&8[ &6&lKONFIRMASI PEMBELIAN &8]", 36, parent);
        this.context = context;
    }

    @Override
    public void initialize() {
        fillBackground();

        ExpShopCurrencyProvider provider = plugin.getExpShopService().getCurrencyRegistry().getProvider(context.getCurrencyId());
        String currencyName = provider != null ? provider.getDisplayName() : context.getCurrencyId().toUpperCase();
        String formattedPrice = provider != null ? provider.format(context.getPrice()) : (context.getPrice() + " " + currencyName);

        Material summaryIcon = "rupiah".equalsIgnoreCase(context.getCurrencyId()) ? Material.EMERALD : Material.DIAMOND;
        String methodDisplayName = "rupiah".equalsIgnoreCase(context.getCurrencyId()) ? "Rupiah (Rp.)" : "Diamond";

        // 1. Transaction Summary Card (Slot 13)
        setButton(13, new GuiButton(new ItemBuilder(summaryIcon)
                .name("&6&lDETAIL TRANSAKSI")
                .lore(List.of(
                        "&7EXP: &a+" + context.getExpPackage().getExpAmount() + " Battle Pass XP",
                        "&7Metode Pembayaran: &f" + methodDisplayName,
                        "&7Total Harga: &e" + formattedPrice
                ))
                .build()));

        // 2. Confirm Button (Slot 11)
        setButton(11, new GuiButton(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                .name("&a&l[✔ KONFIRMASI BELI]")
                .lore(List.of(
                        "&7Klik untuk menyelesaikan pembelian.",
                        "&7Saldo Anda akan dipotong &e" + formattedPrice
                ))
                .build(), event -> {
            player.closeInventory();
            plugin.getExpShopService().processPurchase(context);
        }));

        // 3. Cancel Button (Slot 15)
        setButton(15, new GuiButton(new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .name("&c&l[✖ BATALKAN]")
                .lore(List.of("&7Klik untuk membatalkan dan kembali."))
                .build(), event -> {
            if (parent != null) {
                parent.open();
            } else {
                player.closeInventory();
            }
        }));

        // 4. Back Button (Slot 31)
        setButton(31, new BackButton(this));
    }
}
