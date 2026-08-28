package com.apexsions.battlepass.shop;

import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.util.ItemSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ShopItem {

    private final String id;
    private final String displayName;
    private final Material material;
    private final int amount;
    private final String currencyType; // "VAULT", "BATTLE_COINS", or custom currency ID from ApexsionsEconomy
    private final double price;
    private final int purchaseLimit;
    private final List<String> commands;
    private final List<String> lore;
    private final ItemRarity rarity;
    private final String categoryTag;
    private final int weight;
    private final String itemData; // Base64 serialized ItemStack

    public ShopItem(String id, String displayName, Material material, int amount, String currencyType, double price, int purchaseLimit, List<String> commands, List<String> lore, ItemRarity rarity, String categoryTag, int weight, String itemData) {
        this.id = id;
        this.displayName = displayName != null ? displayName : id;
        this.material = material != null ? material : Material.CHEST;
        this.amount = amount > 0 ? amount : 1;
        this.currencyType = currencyType != null ? currencyType : "BATTLE_COINS";
        this.price = Math.max(0, price);
        this.purchaseLimit = purchaseLimit;
        this.commands = commands != null ? commands : List.of();
        this.lore = lore != null ? lore : List.of();
        this.rarity = rarity != null ? rarity : ItemRarity.COMMON;
        this.categoryTag = categoryTag != null ? categoryTag : "General";
        this.weight = Math.max(1, weight);
        this.itemData = itemData;
    }

    public ShopItem(String id, String displayName, Material material, int amount, String currencyType, double price, int purchaseLimit, List<String> commands, List<String> lore, ItemRarity rarity, String categoryTag, int weight) {
        this(id, displayName, material, amount, currencyType, price, purchaseLimit, commands, lore, rarity, categoryTag, weight, null);
    }

    public static ShopItem fromItemStack(String id, ItemStack item, String currencyType, double price, ItemRarity rarity, String categoryTag, int purchaseLimit) {
        if (item == null || item.getType() == Material.AIR) return null;
        String base64 = ItemSerializer.toBase64(item);
        String name = ItemSerializer.getItemDisplayName(item);
        return new ShopItem(id, name, item.getType(), item.getAmount(), currencyType, price, purchaseLimit, List.of(), List.of(), rarity, categoryTag, 10, base64);
    }

    public ItemStack toItemStack() {
        if (itemData != null && !itemData.isBlank()) {
            ItemStack is = ItemSerializer.fromBase64(itemData);
            if (is != null) {
                is.setAmount(amount);
                return is;
            }
        }
        return new ItemBuilder(material, amount).name(displayName).lore(lore).build();
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public String getCurrencyType() { return currencyType; }
    public boolean isVaultCurrency() { return "VAULT".equalsIgnoreCase(currencyType); }
    public double getPrice() { return price; }
    public int getPurchaseLimit() { return purchaseLimit; }
    public List<String> getCommands() { return commands; }
    public List<String> getLore() { return lore; }
    public ItemRarity getRarity() { return rarity; }
    public String getCategoryTag() { return categoryTag; }
    public int getWeight() { return weight; }
    public String getItemData() { return itemData; }
}
