package com.yourserver.apexsionschat.gui;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class ItemShowcaseGUI extends BaseChatGUI {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final ItemStack showcasedItem;

    public ItemShowcaseGUI(ApexsionsChatPlugin plugin, ItemStack showcasedItem) {
        this.plugin = plugin;
        this.showcasedItem = showcasedItem != null ? showcasedItem.clone() : new ItemStack(Material.BARRIER);
        this.inventory = Bukkit.createInventory(this, 27, miniMessage.deserialize("<dark_gray>🔍 Item Showcase</dark_gray>"));
        build();
    }

    private void build() {
        ItemStack border = createBorderItem();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, border);
        }

        // Showcase item in center Slot 13
        inventory.setItem(13, showcasedItem);

        // Close button at Slot 22
        ItemStack closeBtn = new ItemStack(Material.BARRIER);
        ItemMeta meta = closeBtn.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize("<red><bold>Close Inspection</bold></red>"));
            meta.lore(Collections.singletonList(miniMessage.deserialize("<gray>Click to close this window.</gray>")));
            closeBtn.setItemMeta(meta);
        }
        inventory.setItem(22, closeBtn);
    }

    private ItemStack createBorderItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}
