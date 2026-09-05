package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import com.apexsions.customenchants.group.EnchantmentGroup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Exact replica of AdvancedEnchantments /ae admin catalog GUI, accessible via /ace enchants [page/search].
 */
public class AceEnchantsCatalogGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final int page;
    private final String filter;
    private final List<CustomEnchant> filteredList;
    private final Map<Integer, CustomEnchant> slotMap = new HashMap<>();

    public AceEnchantsCatalogGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, int page, String filter) {
        this.plugin = plugin;
        this.player = player;
        this.page = Math.max(1, page);
        this.filter = filter != null ? filter.toLowerCase().trim() : null;

        List<CustomEnchant> list = new ArrayList<>(plugin.getEnchantmentRegistry().getAllEnchantments());
        if (this.filter != null && !this.filter.isEmpty()) {
            list.removeIf(e -> !e.getId().contains(this.filter) && !e.getDisplayName().toLowerCase().contains(this.filter));
        }
        list.sort(Comparator.comparing(CustomEnchant::getId));
        this.filteredList = list;

        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gold><bold>AE</bold></gold> <green>Admin (" + this.page + ")</green>"));
        buildGUI();
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotMap.clear();

        int maxPerPage = 45;
        int startIndex = (page - 1) * maxPerPage;
        int endIndex = Math.min(startIndex + maxPerPage, filteredList.size());

        for (int i = startIndex; i < endIndex; i++) {
            CustomEnchant enchant = filteredList.get(i);
            int slot = i - startIndex;
            slotMap.put(slot, enchant);

            EnchantmentGroup grp = enchant.getGroup();
            ItemStack item = new ItemStack(Material.FIREWORK_STAR);
            FireworkEffectMeta meta = (FireworkEffectMeta) item.getItemMeta();
            if (meta != null) {
                FireworkEffect effect = FireworkEffect.builder()
                        .withColor(grp.getBukkitColor())
                        .build();
                meta.setEffect(effect);
                meta.displayName(mm.deserialize("<gray>Enchantment</gray> <gold>" + enchant.getDisplayName() + "</gold>"));
                List<Component> lore = new ArrayList<>();
                lore.add(mm.deserialize("<yellow><bold>x</bold></yellow> <gray>" + enchant.getDescription() + "</gray>"));
                lore.add(Component.empty());
                lore.add(mm.deserialize("<gold><bold>x</bold></gold> <gray>Enchant Type:</gray> <yellow>" + enchant.getAppliesTo() + "</yellow>"));
                lore.add(mm.deserialize("<gold><bold>x</bold></gold> <gray>Applies to:</gray> <yellow>" + enchant.getAppliesTo() + "</yellow>"));
                lore.add(mm.deserialize("<gold><bold>x</bold></gold> <gray>Levels:</gray> <yellow>I - " + CustomEnchant.toRoman(enchant.getMaxLevel()) + " (" + enchant.getMaxLevel() + ")</yellow>"));
                lore.add(mm.deserialize("<gold><bold>x</bold></gold> <gray>Tier Group:</gray> " + grp.getDisplayName()));
                lore.add(Component.empty());
                lore.add(mm.deserialize("<dark_gray>>> </dark_gray><gold>Click</gold> <gray>to</gray> <yellow>Access Books</yellow>"));
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot, item);
        }

        if (slotMap.isEmpty()) {
            inventory.setItem(22, createItem(Material.BARRIER, "<red>No enchants found!</red>", null));
        }

        // Bottom Navigation Row (Matching AE exact layout)
        // Slot 48: Previous Page
        if (page > 1) {
            inventory.setItem(48, createItem(Material.BOOK, "<dark_gray>«</dark_gray> <gold>Previous Page</gold>", null));
        }

        // Slot 49: Close
        inventory.setItem(49, createItem(Material.ANVIL, "<gold>Close the page.</gold>", null));

        // Slot 50: Next Page
        if (endIndex < filteredList.size()) {
            inventory.setItem(50, createItem(Material.BOOK, "<dark_gray>>></dark_gray> <gold>Next Page</gold>", null));
        }

        // Slot 52: Search for Enchantment
        inventory.setItem(52, createItem(Material.NAME_TAG, "<gold>Search for Enchantment</gold>", List.of(
                "<gray>Filter saat ini: " + (filter != null ? "<yellow>" + filter + "</yellow>" : "<italic>Semua</italic>") + "</gray>",
                "<yellow>▶ Klik untuk reset / ketik pencarian!</yellow>"
        )));
    }

    private ItemStack createItem(Material mat, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            if (loreLines != null) {
                List<Component> cList = new ArrayList<>();
                for (String l : loreLines) {
                    cList.add(mm.deserialize(l));
                }
                meta.lore(cList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 48 && page > 1) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new AceEnchantsCatalogGUI(plugin, player, page - 1, filter).open();
            return;
        }

        if (slot == 49) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (slot == 50 && ((page * 45) < filteredList.size())) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new AceEnchantsCatalogGUI(plugin, player, page + 1, filter).open();
            return;
        }

        if (slot == 52) {
            if (filter != null) {
                // Reset filter
                new AceEnchantsCatalogGUI(plugin, player, 1, null).open();
            } else {
                player.closeInventory();
                player.sendMessage(mm.deserialize("<gold>Ketik di chat sihir yang ingin kamu cari (atau jalankan <yellow>/ace enchants <nama></yellow>):</gold>"));
            }
            return;
        }

        CustomEnchant enchant = slotMap.get(slot);
        if (enchant != null) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            new AceBookLevelsSubGUI(plugin, player, enchant, page, filter).open();
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
