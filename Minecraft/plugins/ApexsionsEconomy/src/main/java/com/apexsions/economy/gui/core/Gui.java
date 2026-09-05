package com.apexsions.economy.gui.core;

import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.gui.util.ItemBuilder;
import com.apexsions.economy.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public abstract class Gui {

    protected final ApexsionsEconomy plugin;
    protected final Player player;
    protected final String title;
    protected final int size;
    protected Gui parent;
    protected Inventory inventory;
    protected final Map<Integer, GuiButton> buttons = new HashMap<>();

    public Gui(ApexsionsEconomy plugin, Player player, String title, int size, Gui parent) {
        this.plugin = plugin != null ? plugin : ApexsionsEconomy.getInstance();
        this.player = player;
        this.title = ColorUtil.colorize(title != null ? title : "Menu");
        this.size = size > 0 ? size : 27;
        this.parent = parent;
        this.inventory = Bukkit.createInventory(new GuiHolder(this), this.size, this.title);
    }

    public Gui(ApexsionsEconomy plugin, Player player, String title, int size) {
        this(plugin, player, title, size, null);
    }

    public abstract void initialize();

    public void open() {
        buttons.clear();
        inventory.clear();
        initialize();
        for (Map.Entry<Integer, GuiButton> entry : buttons.entrySet()) {
            if (entry.getKey() >= 0 && entry.getKey() < size && entry.getValue() != null) {
                inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
            }
        }
        player.openInventory(inventory);
    }

    public void setButton(int slot, GuiButton button) {
        if (slot >= 0 && slot < size) {
            buttons.put(slot, button);
            if (inventory != null && button != null) {
                inventory.setItem(slot, button.getItemStack());
            }
        }
    }

    public void setButton(int slot, ItemStack item, GuiAction action) {
        setButton(slot, new GuiButton(item, action));
    }

    public void fillBackground(Material fillerMaterial) {
        Material mat = fillerMaterial != null ? fillerMaterial : Material.BLACK_STAINED_GLASS_PANE;
        ItemStack filler = new ItemBuilder(mat).name(" ").build();
        for (int i = 0; i < size; i++) {
            if (!buttons.containsKey(i)) {
                setButton(i, new GuiButton(filler, null));
            }
        }
    }

    public void fillBackground() {
        fillBackground(Material.BLACK_STAINED_GLASS_PANE);
    }

    public void fillBorder(Material fillerMaterial) {
        Material mat = fillerMaterial != null ? fillerMaterial : Material.BLACK_STAINED_GLASS_PANE;
        ItemStack filler = new ItemBuilder(mat).name(" ").build();
        int rows = size / 9;
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;
            boolean isBorder = (row == 0 || row == rows - 1 || col == 0 || col == 8);
            if (isBorder && !buttons.containsKey(i)) {
                setButton(i, new GuiButton(filler, null));
            }
        }
    }

    public void fillBorder() {
        fillBorder(Material.BLACK_STAINED_GLASS_PANE);
    }

    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == event.getInventory()) {
            handleClick(event);
        } else {
            handleBottomInventoryClick(event);
        }
    }

    public void handleBottomInventoryClick(InventoryClickEvent event) {
        // Default: do nothing
    }

    public void onInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= size) return;

        GuiButton button = buttons.get(slot);
        if (button != null && button.getAction() != null) {
            button.getAction().execute(event);
        }
    }

    public void onInventoryClose(InventoryCloseEvent event) {
        // Default: do nothing
    }

    public Gui getParent() {
        return parent;
    }

    public void setParent(Gui parent) {
        this.parent = parent;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }
}
