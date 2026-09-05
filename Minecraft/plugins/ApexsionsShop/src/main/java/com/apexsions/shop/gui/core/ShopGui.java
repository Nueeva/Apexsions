package com.apexsions.shop.gui.core;

import com.apexsions.shop.ApexsionsShop;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public abstract class ShopGui {

    protected static final MiniMessage MM = MiniMessage.miniMessage();

    protected final ApexsionsShop plugin;
    protected final Player player;
    protected final String title;
    protected final int size;
    protected ShopGui parent;
    protected Inventory inventory;
    protected final Map<Integer, ShopGuiButton> buttons = new HashMap<>();

    public ShopGui(ApexsionsShop plugin, Player player, String title, int size, ShopGui parent) {
        this.plugin = plugin != null ? plugin : ApexsionsShop.getInstance();
        this.player = player;
        this.title = title != null ? title : "<dark_gray>Shop Menu</dark_gray>";
        this.size = size > 0 ? size : 54;
        this.parent = parent;
        this.inventory = Bukkit.createInventory(new ShopGuiHolder(this), this.size, MM.deserialize(this.title));
    }

    public ShopGui(ApexsionsShop plugin, Player player, String title, int size) {
        this(plugin, player, title, size, null);
    }

    public abstract void initialize();

    public void open() {
        buttons.clear();
        inventory.clear();
        initialize();
        if (plugin.getConfig().getBoolean("gui.auto-fill-empty-slots", true)) {
            fillEmpty();
        }
        for (Map.Entry<Integer, ShopGuiButton> entry : buttons.entrySet()) {
            if (entry.getKey() >= 0 && entry.getKey() < size && entry.getValue() != null) {
                inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
            }
        }
        player.openInventory(inventory);
    }

    public void setButton(int slot, ShopGuiButton button) {
        if (slot >= 0 && slot < size) {
            buttons.put(slot, button);
            if (inventory != null && button != null) {
                inventory.setItem(slot, button.getItemStack());
            }
        }
    }

    public void fillEmpty(Material fillerMaterial) {
        Material mat = fillerMaterial != null ? fillerMaterial : Material.BLACK_STAINED_GLASS_PANE;
        ItemStack filler = new ShopItemBuilder(mat).name("<dark_gray> </dark_gray>").build();
        for (int i = 0; i < size; i++) {
            if (!buttons.containsKey(i)) {
                setButton(i, new ShopGuiButton(filler, null));
            }
        }
    }

    public void fillEmpty() {
        String matName = plugin.getConfig().getString("gui.filler-material", "BLACK_STAINED_GLASS_PANE");
        Material mat = Material.matchMaterial(matName);
        fillEmpty(mat != null ? mat : Material.BLACK_STAINED_GLASS_PANE);
    }

    public void fillBorder(Material borderMaterial) {
        Material mat = borderMaterial != null ? borderMaterial : Material.BLACK_STAINED_GLASS_PANE;
        ItemStack filler = new ShopItemBuilder(mat).name("<dark_gray> </dark_gray>").build();
        int rows = size / 9;
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;
            boolean isBorder = (row == 0 || row == rows - 1 || col == 0 || col == 8);
            if (isBorder && !buttons.containsKey(i)) {
                setButton(i, new ShopGuiButton(filler, null));
            }
        }
    }

    public void fillBorder() {
        fillBorder(Material.BLACK_STAINED_GLASS_PANE);
    }

    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= size) return;

        ShopGuiButton button = buttons.get(slot);
        if (button != null && button.getAction() != null) {
            button.getAction().accept(event);
        }
    }

    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == event.getInventory()) {
            event.setCancelled(true);
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

    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        // Override if needed
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public ShopGui getParent() {
        return parent;
    }
}
