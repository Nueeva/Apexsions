package com.apexsions.core.gui.core;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

/**
 * Kontrak GUI yang dapat di-host oleh {@link GuiHolder} dan dilayani oleh
 * {@link GuiClickListener}. Plugin mengimplementasikan interface ini pada
 * class GUI masing-masing (mis. {@code Gui}) agar framework GUI generik di
 * Core dapat dipakai ulang tanpa duplikasi.
 */
public interface GuiHandler {

    void onInventoryClick(InventoryClickEvent event);

    void onInventoryDrag(InventoryDragEvent event);

    void onInventoryClose(InventoryCloseEvent event);

    Inventory getInventory();
}
