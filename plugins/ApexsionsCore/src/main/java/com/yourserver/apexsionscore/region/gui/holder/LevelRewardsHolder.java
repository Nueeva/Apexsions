package com.yourserver.apexsionscore.region.gui.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class LevelRewardsHolder implements InventoryHolder {
    private Inventory inventory;
    private final int page;

    public LevelRewardsHolder(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
