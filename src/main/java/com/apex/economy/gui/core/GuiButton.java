package com.apex.economy.gui.core;

import org.bukkit.inventory.ItemStack;

public class GuiButton {

    private final ItemStack itemStack;
    private final GuiAction action;

    public GuiButton(ItemStack itemStack, GuiAction action) {
        this.itemStack = itemStack;
        this.action = action;
    }

    public GuiButton(ItemStack itemStack) {
        this(itemStack, null);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public GuiAction getAction() {
        return action;
    }
}
