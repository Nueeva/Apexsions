package com.apexsions.jobs.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface JobsGuiHolder extends InventoryHolder {
    void handleClick(InventoryClickEvent event);
}
