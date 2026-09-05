package com.apexsions.shop.gui.core;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class ShopGuiButton {

    private final ItemStack itemStack;
    private final Consumer<InventoryClickEvent> action;

    public ShopGuiButton(ItemStack itemStack, Consumer<InventoryClickEvent> action) {
        this.itemStack = itemStack;
        this.action = action;
    }

    public ShopGuiButton(ItemStack itemStack) {
        this(itemStack, null);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Consumer<InventoryClickEvent> getAction() {
        return action;
    }
}
