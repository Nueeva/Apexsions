package com.apexsions.shop.gui.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopItemBuilder {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final ItemStack item;
    private final ItemMeta meta;

    public ShopItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material != null ? material : Material.STONE, Math.max(1, amount));
        this.meta = item.getItemMeta();
    }

    public ShopItemBuilder(Material material) {
        this(material, 1);
    }

    public ShopItemBuilder(ItemStack itemStack) {
        this.item = itemStack != null ? itemStack.clone() : new ItemStack(Material.STONE);
        this.meta = this.item.getItemMeta();
    }

    public ShopItemBuilder name(String miniMessageName) {
        if (meta != null && miniMessageName != null) {
            meta.displayName(MM.deserialize(miniMessageName));
        }
        return this;
    }

    public ShopItemBuilder lore(List<String> miniMessageLore) {
        if (meta != null && miniMessageLore != null) {
            List<Component> compLore = new ArrayList<>();
            for (String line : miniMessageLore) {
                compLore.add(MM.deserialize(line));
            }
            meta.lore(compLore);
        }
        return this;
    }

    public ShopItemBuilder addLoreLine(String line) {
        if (meta != null && line != null) {
            List<Component> current = meta.lore();
            if (current == null) current = new ArrayList<>();
            else current = new ArrayList<>(current);
            current.add(MM.deserialize(line));
            meta.lore(current);
        }
        return this;
    }

    public ShopItemBuilder glow() {
        if (meta != null) {
            meta.setEnchantmentGlintOverride(true);
        }
        return this;
    }

    public ShopItemBuilder hideAttributes() {
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    public ShopItemBuilder skullOwner(Player player) {
        if (meta instanceof SkullMeta skullMeta && player != null) {
            skullMeta.setOwningPlayer(player);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
}
