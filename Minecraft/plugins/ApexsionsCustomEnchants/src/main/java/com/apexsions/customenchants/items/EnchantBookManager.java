package com.apexsions.customenchants.items;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager handling Enchantment Book creation, PDC data, and drag-and-drop mechanics.
 */
public class EnchantBookManager {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final NamespacedKey keyBookEnchant;
    private final NamespacedKey keyBookLevel;
    private final NamespacedKey keyBookSuccess;
    private final NamespacedKey keyBookDestroy;

    public EnchantBookManager(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
        this.keyBookEnchant = new NamespacedKey(plugin, "book_enchant");
        this.keyBookLevel = new NamespacedKey(plugin, "book_level");
        this.keyBookSuccess = new NamespacedKey(plugin, "book_success");
        this.keyBookDestroy = new NamespacedKey(plugin, "book_destroy");
    }

    public ItemStack createBook(CustomEnchant enchant, int level, int successRate, int destroyRate) {
        if (enchant == null) return null;
        int lvl = Math.max(1, Math.min(enchant.getMaxLevel(), level));
        int success = Math.max(1, Math.min(100, successRate));
        int destroy = Math.max(0, Math.min(100, destroyRate));

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta == null) return book;

        String color = enchant.getGroup().getColor();
        meta.displayName(mm.deserialize("<color:" + color + "><bold>" + enchant.getDisplayName() + " " + CustomEnchant.toRoman(lvl) + "</bold></color>"));

        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<green>● " + success + "% Success Rate</green>"));
        lore.add(mm.deserialize("<red>● " + destroy + "% Destroy Rate</red>"));
        lore.add(Component.empty());
        lore.add(mm.deserialize("<gray>" + enchant.getDescription() + "</gray>"));
        lore.add(Component.empty());
        lore.add(mm.deserialize("<gold>Tier:</gold> " + enchant.getGroup().getDisplayName()));
        lore.add(mm.deserialize("<gold>Berlaku Pada:</gold> <yellow>" + enchant.getAppliesTo() + "</yellow>"));
        lore.add(Component.empty());
        lore.add(mm.deserialize("<dark_gray>Drag & drop buku ini ke equipment untuk menempa sihir.</dark_gray>"));
        meta.lore(lore);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(keyBookEnchant, PersistentDataType.STRING, enchant.getId());
        pdc.set(keyBookLevel, PersistentDataType.INTEGER, lvl);
        pdc.set(keyBookSuccess, PersistentDataType.INTEGER, success);
        pdc.set(keyBookDestroy, PersistentDataType.INTEGER, destroy);

        book.setItemMeta(meta);
        return book;
    }

    public boolean isEnchantBook(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(keyBookEnchant, PersistentDataType.STRING);
    }

    public CustomEnchant getBookEnchant(ItemStack item) {
        if (!isEnchantBook(item)) return null;
        String id = item.getItemMeta().getPersistentDataContainer().get(keyBookEnchant, PersistentDataType.STRING);
        return plugin.getEnchantmentRegistry().getEnchantment(id);
    }

    public int getBookLevel(ItemStack item) {
        if (!isEnchantBook(item)) return 1;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(keyBookLevel, PersistentDataType.INTEGER, 1);
    }

    public int getBookSuccess(ItemStack item) {
        if (!isEnchantBook(item)) return 50;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(keyBookSuccess, PersistentDataType.INTEGER, 50);
    }

    public int getBookDestroy(ItemStack item) {
        if (!isEnchantBook(item)) return 20;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(keyBookDestroy, PersistentDataType.INTEGER, 20);
    }

    public ItemStack updateBookSuccess(ItemStack book, int addedSuccess) {
        if (!isEnchantBook(book)) return book;
        CustomEnchant enchant = getBookEnchant(book);
        int lvl = getBookLevel(book);
        int curSuccess = getBookSuccess(book);
        int curDestroy = getBookDestroy(book);

        int newSuccess = Math.min(100, curSuccess + addedSuccess);
        return createBook(enchant, lvl, newSuccess, curDestroy);
    }

    public NamespacedKey getKeyBookEnchant() {
        return keyBookEnchant;
    }

    public NamespacedKey getKeyBookLevel() {
        return keyBookLevel;
    }

    public NamespacedKey getKeyBookSuccess() {
        return keyBookSuccess;
    }

    public NamespacedKey getKeyBookDestroy() {
        return keyBookDestroy;
    }
}
