package com.apexsions.core.region.gui.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class MilestoneRewardPreviewHolder implements InventoryHolder {
    private Inventory inventory;
    private final int milestoneLevel;
    private final int fromPage;

    public MilestoneRewardPreviewHolder(int milestoneLevel, int fromPage) {
        this.milestoneLevel = milestoneLevel;
        this.fromPage = fromPage;
    }

    public int getMilestoneLevel() {
        return milestoneLevel;
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
