package com.apexsions.battlepass.expshop.gui;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.expshop.model.ExpPackage;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class ExpPackageMenu extends Gui {

    public ExpPackageMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        super(plugin, player, "&8[ &6&lEXP SHOP &8- PILIH PAKET ]", 36, parent);
    }

    @Override
    public void initialize() {
        fillBackground();

        Map<String, ExpPackage> packages = plugin.getExpShopService().getPackages();
        int[] slots = { 10, 12, 14, 16, 19, 21, 23, 25 };
        int idx = 0;

        for (ExpPackage pkg : packages.values()) {
            if (idx >= slots.length) break;

            setButton(slots[idx++], new GuiButton(new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                    .name(pkg.getDisplayName())
                    .lore(List.of(
                            "&7Dapatkan &e+" + pkg.getExpAmount() + " Battle Pass XP",
                            " ",
                            "&eKlik untuk memilih metode pembayaran >"
                    ))
                    .build(), event -> {
                // Navigate to Step 2: Currency Type Selection
                new ExpCurrencyMenu(plugin, player, this, pkg).open();
            }));
        }

        setButton(31, new BackButton(this));
    }
}
