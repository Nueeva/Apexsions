package com.apex.battlepass.gui.util;

import com.apex.battlepass.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private final ItemStack itemStack;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material != null ? material : Material.STONE);
        this.meta = itemStack.getItemMeta();
    }

    public ItemBuilder(Material material, int amount) {
        this.itemStack = new ItemStack(material != null ? material : Material.STONE, amount);
        this.meta = itemStack.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.itemStack = (item != null && item.getType() != Material.AIR) ? item.clone() : new ItemStack(Material.STONE);
        this.meta = itemStack.getItemMeta();
    }

    public ItemBuilder name(String name) {
        if (meta != null && name != null) {
            meta.setDisplayName(ColorUtil.colorize(name));
        }
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        if (meta != null && lore != null) {
            meta.setLore(ColorUtil.colorize(lore));
        }
        return this;
    }

    public ItemBuilder addLoreLine(String line) {
        if (meta != null && line != null) {
            List<String> currentLore = meta.getLore();
            if (currentLore == null) currentLore = new ArrayList<>();
            currentLore.add(ColorUtil.colorize(line));
            meta.setLore(currentLore);
        }
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        if (meta != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }

    public ItemBuilder hideAttributes() {
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        }
        return this;
    }

    public ItemBuilder skullOwner(org.bukkit.OfflinePlayer offlinePlayer) {
        if (meta instanceof org.bukkit.inventory.meta.SkullMeta skullMeta && offlinePlayer != null) {
            skullMeta.setOwningPlayer(offlinePlayer);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }
}
