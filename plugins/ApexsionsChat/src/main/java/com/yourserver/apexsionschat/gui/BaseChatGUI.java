package com.yourserver.apexsionschat.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class BaseChatGUI implements InventoryHolder {

    protected Inventory inventory;

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
