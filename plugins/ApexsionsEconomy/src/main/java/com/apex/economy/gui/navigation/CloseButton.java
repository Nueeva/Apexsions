package com.apex.economy.gui.navigation;

import com.apex.economy.gui.core.GuiButton;
import com.apex.economy.gui.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CloseButton extends GuiButton {

    public CloseButton() {
        super(createDefaultItem(), event -> {
            if (event.getWhoClicked() instanceof Player player) {
                player.closeInventory();
            }
        });
    }

    private static ItemStack createDefaultItem() {
        return new ItemBuilder(Material.BARRIER)
                .name("&c&lTUTUP")
                .lore(List.of("&7Klik untuk menutup menu."))
                .build();
    }
}
