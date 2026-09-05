package com.apexsions.core.region.gui.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class XpCategoryDetailHolder implements InventoryHolder {
    private Inventory inventory;
    private final String categoryId;
    private final int page;

    public XpCategoryDetailHolder(String categoryId, int page) {
        this.categoryId = categoryId;
        this.page = page;
    }

    public XpCategoryDetailHolder(String categoryId) {
        this(categoryId, 1);
    }

    public String getCategoryId() {
        return categoryId;
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
