package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Submenu displaying book tiers for an enchantment, with Left-Click obtain & Right-Click direct enchant.
 */
public class AceBookLevelsSubGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final CustomEnchant enchant;
    private final int returnPage;
    private final String returnFilter;
    private final String returnRarity;
    private final String returnCategory;
    private final String returnSearch;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Integer, Integer> slotLevelMap = new HashMap<>();

    public AceBookLevelsSubGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, CustomEnchant enchant, int returnPage, String returnRarity, String returnCategory, String returnSearch) {
        this.plugin = plugin;
        this.player = player;
        this.enchant = enchant;
        this.returnPage = returnPage;
        this.returnRarity = returnRarity != null ? returnRarity : "ALL";
        this.returnCategory = returnCategory != null ? returnCategory : "ALL";
        this.returnSearch = returnSearch;
        this.returnFilter = returnSearch;

        int size = Math.max(9, ((enchant.getMaxLevel() + 1 + 8) / 9) * 9);
        if (size > 54) size = 54;
        this.inventory = Bukkit.createInventory(this, size, mm.deserialize("<gold><bold>AE</bold></gold> <green>Book (" + enchant.getId() + ")</green>"));
        buildGUI();
    }

    public AceBookLevelsSubGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, CustomEnchant enchant, int returnPage, String returnFilter) {
        this(plugin, player, enchant, returnPage, "ALL", "ALL", returnFilter);
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotLevelMap.clear();

        for (int lvl = 1; lvl <= enchant.getMaxLevel(); lvl++) {
            int slot = lvl - 1;
            slotLevelMap.put(slot, lvl);

            ItemStack star = new ItemStack(Material.FIREWORK_STAR, Math.min(64, lvl));
            org.bukkit.inventory.meta.FireworkEffectMeta meta = (org.bukkit.inventory.meta.FireworkEffectMeta) star.getItemMeta();
            if (meta != null) {
                meta.setEffect(org.bukkit.FireworkEffect.builder()
                        .withColor(enchant.getGroup().getBukkitColor())
                        .build());
                meta.displayName(mm.deserialize("<color:" + enchant.getGroup().getColor() + "><bold>" + enchant.getDisplayName() + " " + CustomEnchant.toRoman(lvl) + "</bold></color>"));
                List<Component> lore = new ArrayList<>();
                lore.add(mm.deserialize("<gray>" + enchant.getDescription() + "</gray>"));
                lore.add(Component.empty());
                lore.add(mm.deserialize("<gold>Tier:</gold> " + enchant.getGroup().getDisplayName()));
                lore.add(mm.deserialize("<gold>Applies to:</gold> <yellow>" + enchant.getAppliesTo() + "</yellow>"));
                lore.add(mm.deserialize("<gold>Tingkat Level:</gold> <aqua>" + CustomEnchant.toRoman(lvl) + " / " + CustomEnchant.toRoman(enchant.getMaxLevel()) + " (" + lvl + "/" + enchant.getMaxLevel() + ")</aqua>"));
                lore.add(Component.empty());
                lore.add(mm.deserialize("<yellow>(!) <italic>Left-Click</italic> untuk mengambil buku (100% Success)</yellow>"));
                lore.add(mm.deserialize("<yellow>(!) <italic>Right-Click</italic> untuk pasang ke item di tangan</yellow>"));
                meta.lore(lore);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
                star.setItemMeta(meta);
            }
            inventory.setItem(slot, star);
        }

        // Back button on last slot
        int backSlot = inventory.getSize() - 1;
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bMeta = back.getItemMeta();
        if (bMeta != null) {
            bMeta.displayName(mm.deserialize("<dark_gray><</dark_gray> <gold>Go Back</gold>"));
            back.setItemMeta(bMeta);
        }
        inventory.setItem(backSlot, back);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == inventory.getSize() - 1) { // Go back
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new AceEnchantsCatalogGUI(plugin, player, returnPage, returnRarity, returnCategory, returnSearch).open();
            return;
        }

        Integer level = slotLevelMap.get(slot);
        if (level != null) {
            if (event.getClick() == ClickType.LEFT) {
                // Obtain book
                ItemStack book = plugin.getEnchantBookManager().createBook(enchant, level, 100, 0);
                player.getInventory().addItem(book);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
                player.sendMessage(mm.deserialize("<green>✓ Berhasil mengambil buku sihir <gold>" + enchant.getDisplayName() + " " + CustomEnchant.toRoman(level) + "</gold>!</green>"));
            } else if (event.getClick() == ClickType.RIGHT) {
                // Enchant held item
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType().isAir()) {
                    player.sendMessage(mm.deserialize("<red>Kamu harus memegang item di tangan utama untuk menerapkan sihir ini!</red>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }

                if (!enchant.canApplyTo(hand)) {
                    player.sendMessage(mm.deserialize("<red>Sihir " + enchant.getDisplayName() + " tidak dapat diterapkan ke jenis item ini (" + enchant.getAppliesTo() + ")!</red>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }

                ItemStack enchanted = plugin.getEnchantmentRegistry().applyEnchant(hand, enchant, level);
                player.getInventory().setItemInMainHand(enchanted);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                player.sendMessage(mm.deserialize("<green><bold>✓ ENCHANT SUKSES!</bold> Item di tanganmu telah diberikan sihir <gold>" + enchant.getDisplayName() + " " + CustomEnchant.toRoman(level) + "</gold>!</green>"));
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
