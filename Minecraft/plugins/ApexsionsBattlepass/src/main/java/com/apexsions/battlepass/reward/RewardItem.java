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
    private final boolean specialPreview;

    public RewardItem(RewardType type, Material material, int amount, String name, List<String> commands, String permission, String itemData, String currencyId, boolean specialPreview) {
        this.type = type != null ? type : RewardType.ITEM;
        this.material = material != null ? material : Material.CHEST;
        this.amount = amount > 0 ? amount : 1;
        this.name = name;
        this.commands = commands != null ? commands : List.of();
        this.permission = permission;
        this.itemData = itemData;
        this.currencyId = currencyId != null ? currencyId : "battle_coins";
        this.specialPreview = specialPreview;
    }

    public RewardItem(RewardType type, Material material, int amount, String name, List<String> commands, String permission, String itemData, String currencyId) {
        this(type, material, amount, name, commands, permission, itemData, currencyId, false);
    }

    public RewardItem(RewardType type, Material material, int amount, String name, List<String> commands, String permission) {
        this(type, material, amount, name, commands, permission, null, "battle_coins", false);
    }

    public static RewardItem fromItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        String base64 = ItemSerializer.toBase64(item);
        String name = ItemSerializer.getItemDisplayName(item);
        return new RewardItem(RewardType.ITEM, item.getType(), item.getAmount(), name, List.of(), null, base64, null, false);
    }

    public ItemStack toItemStack() {
        if (isCurrency()) {
            Material icon;
            if ("rupiah".equalsIgnoreCase(currencyId) || type == RewardType.MONEY) {
                icon = (material != null && material != Material.CHEST && material != Material.AIR) ? material : Material.GOLD_INGOT;
            } else if ("diamond".equalsIgnoreCase(currencyId)) {
                icon = Material.DIAMOND;
            } else {
                icon = (material != null && material != Material.CHEST && material != Material.AIR) ? material : Material.SUNFLOWER;
            }
            return new ItemBuilder(icon, 1).name(getDisplayName()).build();
        }

        if (itemData != null && !itemData.isBlank()) {
            ItemStack is = ItemSerializer.fromBase64(itemData);
            if (is != null) {
                is.setAmount(Math.max(1, Math.min(64, amount)));
                return is;
            }
        }
        int clampedAmount = Math.max(1, Math.min(64, amount));
        return new ItemBuilder(material != null ? material : Material.CHEST, clampedAmount).name(getDisplayName()).build();
    }

    public String getDisplayName() {
        if (name != null && !name.isBlank()) return name;
        if (isCurrency()) {
            if ("rupiah".equalsIgnoreCase(currencyId) || type == RewardType.MONEY) {
                return "&a&lRp." + String.format("%,d", (long) amount).replace(',', '.');
            } else if ("diamond".equalsIgnoreCase(currencyId)) {
                return "&b&l" + amount + " Diamond 💎";
            } else {
                return "&e&l" + amount + " Battle Coins";
            }
        }
        if (material != null) return ItemSerializer.formatMaterialName(material);
        return type.name();
    }

    public boolean isCurrency() {
        return type == RewardType.CURRENCY || type == RewardType.MONEY;
    }

    public RewardType getType() { return type; }
    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public String getName() { return name; }
    public List<String> getCommands() { return commands; }
    public String getPermission() { return permission; }
    public String getItemData() { return itemData; }
    public String getCurrencyId() { return currencyId; }
    public boolean isSpecialPreview() { return specialPreview; }
    public boolean isPreviewable() { return true; }

    public RewardItem withSpecialPreview(boolean specialPreview) {
        return new RewardItem(type, material, amount, name, commands, permission, itemData, currencyId, specialPreview);
    }
}
