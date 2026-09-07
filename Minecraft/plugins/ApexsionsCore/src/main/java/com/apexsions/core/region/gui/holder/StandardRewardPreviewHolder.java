package com.apexsions.core.region.gui.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class StandardRewardPreviewHolder implements InventoryHolder {
    private Inventory inventory;
    private final int level;
    private final int fromPage;

    public StandardRewardPreviewHolder(int level, int fromPage) {
        this.level = level;
        this.fromPage = fromPage;
    }

    public int getLevel() {
        return level;
    }

    public int getFromPage() {
        return fromPage;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
