package com.apexsions.core.gui.core;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GuiHolder implements InventoryHolder {

    private final GuiHandler gui;

    public GuiHolder(GuiHandler gui) {
        this.gui = gui;
    }

    public GuiHandler getGui() {
        return gui;
    }

    @Override
    public Inventory getInventory() {
        return gui != null ? gui.getInventory() : null;
    }
}
