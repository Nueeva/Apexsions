package com.apexsions.core.gui.core;

import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface GuiAction {
    void execute(InventoryClickEvent event);
}
