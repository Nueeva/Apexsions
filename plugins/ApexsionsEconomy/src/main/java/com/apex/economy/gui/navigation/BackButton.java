package com.apex.economy.gui.navigation;

import com.apex.economy.gui.core.Gui;
import com.apex.economy.gui.core.GuiButton;
import com.apex.economy.gui.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BackButton extends GuiButton {

    public BackButton(Gui currentGui) {
        this(currentGui, currentGui != null ? currentGui.getParent() : null);
    }

    public BackButton(Gui currentGui, Gui explicitParent) {
        super(createDefaultItem(), event -> {
            if (explicitParent != null) {
                explicitParent.open();
            } else if (currentGui != null && currentGui.getParent() != null) {
                currentGui.getParent().open();
            } else if (currentGui != null) {
                currentGui.getPlayer().closeInventory();
            }
        });
    }

    public BackButton(Runnable onBackAction) {
        super(createDefaultItem(), event -> {
            if (onBackAction != null) {
                onBackAction.run();
            }
        });
    }

    private static ItemStack createDefaultItem() {
        return new ItemBuilder(Material.ARROW)
                .name("&c&l< KEMBALI")
                .lore(List.of("&7Klik untuk kembali ke menu sebelumnya."))
                .build();
    }
}

