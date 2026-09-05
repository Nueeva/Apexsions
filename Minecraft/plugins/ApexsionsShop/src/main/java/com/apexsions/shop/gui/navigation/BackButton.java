package com.apexsions.shop.gui.navigation;

import com.apexsions.shop.gui.core.ShopGui;
import com.apexsions.shop.gui.core.ShopGuiButton;
import com.apexsions.shop.gui.core.ShopItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;

public class BackButton extends ShopGuiButton {

    public BackButton(ShopGui currentGui, ShopGui parentGui) {
        super(new ShopItemBuilder(Material.ARROW)
                .name("<yellow>◀ KEMBALI</yellow>")
                .build(), event -> {
            if (currentGui != null && currentGui.getPlayer() != null) {
                currentGui.getPlayer().playSound(currentGui.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            }
            if (parentGui != null) {
                parentGui.open();
            } else if (currentGui != null) {
                currentGui.getPlayer().closeInventory();
            }
        });
    }
}
