package com.apex.battlepass.expshop.gui;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.expshop.model.ExpPackage;
import com.apex.battlepass.expshop.model.ExpPurchaseContext;
import com.apex.battlepass.expshop.provider.ExpShopCurrencyProvider;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class ExpCurrencyMenu extends Gui {

    private final ExpPackage expPackage;

    public ExpCurrencyMenu(ApexsionsBattlepass plugin, Player player, Gui parent, ExpPackage expPackage) {
        super(plugin, player, "&8[ &6&lPILIH METODE PEMBAYARAN &8]", 36, parent);
        this.expPackage = expPackage;
    }

    @Override
    public void initialize() {
        fillBackground();

        // Banner displaying selected package info (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name("&e&lPaket Terpilih: " + expPackage.getDisplayName())
                .lore(List.of("&7EXP yang didapatkan: &a+" + expPackage.getExpAmount() + " BP XP"))
                .build()));

        Map<String, ExpShopCurrencyProvider> providers = plugin.getExpShopService().getCurrencyRegistry().getProviders();

        // 1. Rupiah Option (Left - Slot 12)
        if (expPackage.getPrices().containsKey("rupiah")) {
            double rupiahPrice = expPackage.getPrices().get("rupiah");
            ExpShopCurrencyProvider rupiahProvider = providers.get("rupiah");
            String formattedRupiah = rupiahProvider != null ? rupiahProvider.format(rupiahPrice) : ("Rp." + String.format("%,.0f", rupiahPrice));

            setButton(12, new GuiButton(new ItemBuilder(Material.EMERALD)
                    .name("&a&lBAYAR DENGAN RUPIAH")
                    .lore(List.of(
                            "&7Total Harga: &e" + formattedRupiah,
                            "&7Metode Pembayaran: &aRupiah (Rp.)",
                            " ",
                            "&aKlik untuk lanjut ke konfirmasi >"
                    ))
                    .build(), event -> {
                ExpPurchaseContext context = new ExpPurchaseContext(player, expPackage, "rupiah", rupiahPrice);
                new ExpConfirmMenu(plugin, player, this, context).open();
            }));
        }

        // 2. Diamond Option (Right - Slot 14)
        if (expPackage.getPrices().containsKey("diamond")) {
            double diamondPrice = expPackage.getPrices().get("diamond");
            ExpShopCurrencyProvider diamondProvider = providers.get("diamond");
            String formattedDiamond = diamondProvider != null ? diamondProvider.format(diamondPrice) : (String.format("%,.0f", diamondPrice) + " Diamond");

            setButton(14, new GuiButton(new ItemBuilder(Material.DIAMOND)
                    .name("&b&lBAYAR DENGAN DIAMOND")
                    .lore(List.of(
                            "&7Total Harga: &e" + formattedDiamond,
                            "&7Metode Pembayaran: &bDiamond",
                            " ",
                            "&bKlik untuk lanjut ke konfirmasi >"
                    ))
                    .build(), event -> {
                ExpPurchaseContext context = new ExpPurchaseContext(player, expPackage, "diamond", diamondPrice);
                new ExpConfirmMenu(plugin, player, this, context).open();
            }));
        }

        // Navigation
        setButton(31, new BackButton(this));
    }
}
