package com.apexsions.quests.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface QuestsGuiHolder extends InventoryHolder {
    void handleClick(InventoryClickEvent event);
}
