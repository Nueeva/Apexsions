package com.apexsions.core.region.gui.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * InventoryHolder tag for KingdomTopGUI to guarantee strict click and drag cancellation.
 */
public class KingdomTopHolder implements InventoryHolder {

    private Inventory inventory;
    private final UUID regionId;
    private final String regionKey;

    public KingdomTopHolder(UUID regionId, String regionKey) {
        this.regionId = regionId;
        this.regionKey = regionKey;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public UUID getRegionId() {
        return regionId;
    }

    public String getRegionKey() {
        return regionKey;
    }
}
