package com.apexsions.core.region.gui.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class KingdomConfirmHolder implements InventoryHolder {

    private Inventory inventory;
    private final String kingdomKey;

    public KingdomConfirmHolder(String kingdomKey) {
        this.kingdomKey = kingdomKey;
    }

    public String getKingdomKey() {
        return kingdomKey;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
