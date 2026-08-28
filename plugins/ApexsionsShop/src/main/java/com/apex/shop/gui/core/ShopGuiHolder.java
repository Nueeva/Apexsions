package com.apex.shop.gui.core;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ShopGuiHolder implements InventoryHolder {

    private final ShopGui gui;

    public ShopGuiHolder(ShopGui gui) {
        this.gui = gui;
    }

    public ShopGui getGui() {
        return gui;
    }

    @Override
    public Inventory getInventory() {
        return gui.getInventory();
    }
}
