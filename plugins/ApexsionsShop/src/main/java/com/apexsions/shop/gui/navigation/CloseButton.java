package com.apexsions.shop.gui.navigation;

import com.apexsions.shop.gui.core.ShopGuiButton;
import com.apexsions.shop.gui.core.ShopItemBuilder;
import org.bukkit.Material;

public class CloseButton extends ShopGuiButton {

    public CloseButton() {
        super(new ShopItemBuilder(Material.BARRIER)
                .name("<red><bold>TUTUP MENU</bold></red>")
                .build(), event -> {
            if (event.getWhoClicked() != null) {
                event.getWhoClicked().closeInventory();
            }
        });
    }
}
