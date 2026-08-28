package com.apexsions.battlepass.gui.core;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GuiHolder implements InventoryHolder {

    private final Gui gui;

    public GuiHolder(Gui gui) {
        this.gui = gui;
    }

    public Gui getGui() {
        return gui;
    }

    @Override
    public Inventory getInventory() {
        return gui != null ? gui.getInventory() : null;
    }
}
