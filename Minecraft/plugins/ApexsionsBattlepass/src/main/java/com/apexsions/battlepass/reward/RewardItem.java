package com.apexsions.battlepass.reward;

import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.util.ItemSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RewardItem {

    private final RewardType type;
    private final Material material;
    private final int amount;
    private final String name;
    private final List<String> commands;
    private final String permission;
    private final String itemData; // Base64 serialized ItemStack
    private final String currencyId; // ApexsionsEconomy Currency ID

    public RewardItem(RewardType type, Material material, int amount, String name, List<String> commands, String permission, String itemData, String currencyId) {
        this.type = type != null ? type : RewardType.ITEM;
        this.material = material != null ? material : Material.CHEST;
        this.amount = amount > 0 ? amount : 1;
        this.name = name;
        this.commands = commands != null ? commands : List.of();
        this.permission = permission;
        this.itemData = itemData;
        this.currencyId = currencyId != null ? currencyId : "battle_coins";
    }

    public RewardItem(RewardType type, Material material, int amount, String name, List<String> commands, String permission) {
        this(type, material, amount, name, commands, permission, null, "battle_coins");
    }

    public static RewardItem fromItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        String base64 = ItemSerializer.toBase64(item);
        String name = ItemSerializer.getItemDisplayName(item);
        return new RewardItem(RewardType.ITEM, item.getType(), item.getAmount(), name, List.of(), null, base64, null);
    }

    public ItemStack toItemStack() {
        if (itemData != null && !itemData.isBlank()) {
            ItemStack is = ItemSerializer.fromBase64(itemData);
            if (is != null) {
                is.setAmount(amount);
                return is;
            }
        }
        return new ItemBuilder(material, amount).name(name).build();
    }

    public String getDisplayName() {
        if (name != null && !name.isBlank()) return name;
        if (material != null) return ItemSerializer.formatMaterialName(material);
        return type.name();
    }

    public RewardType getType() { return type; }
    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public String getName() { return name; }
    public List<String> getCommands() { return commands; }
    public String getPermission() { return permission; }
    public String getItemData() { return itemData; }
    public String getCurrencyId() { return currencyId; }
}
