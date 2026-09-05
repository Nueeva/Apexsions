package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
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
 * Category Selection GUI for /ce shop, allowing players to filter by item target first before rarity.
 */
public class ShopCategorySelectGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Integer, String> slotCategoryMap = new HashMap<>();

    public ShopCategorySelectGUI(ApexsionsCustomEnchantsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#9b59b6:#e74c3c><bold>✦ PILIH KATEGORI ITEM ✦</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotCategoryMap.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Header Guide at Slot 4
        inventory.setItem(4, createItem(Material.BOOK, "<gold><bold>📜 PANDUAN TOKO SIHIR SPESIFIK</bold></gold>", List.of(
                mm.deserialize("<gray>Pilih jenis perlengkapan yang ingin kamu enchant,</gray>"),
                mm.deserialize("<gray>lalu pilih rarity sihir yang kamu cari!</gray>")
        )));

        // Armor row
        addCategory(11, Material.NETHERITE_HELMET, "<gradient:#f1c40f:#e67e22><bold>🪖 HELMET</bold></gradient>", "HELMET");
        addCategory(12, Material.NETHERITE_CHESTPLATE, "<gradient:#f1c40f:#e67e22><bold>🛡 CHESTPLATE & ELYTRA</bold></gradient>", "CHESTPLATE");
        addCategory(13, Material.NETHERITE_LEGGINGS, "<gradient:#f1c40f:#e67e22><bold>👖 LEGGINGS</bold></gradient>", "LEGGINGS");
        addCategory(14, Material.NETHERITE_BOOTS, "<gradient:#f1c40f:#e67e22><bold>👢 BOOTS</bold></gradient>", "BOOTS");
        addCategory(15, Material.SHIELD, "<gradient:#e74c3c:#c0392b><bold>🛡 SEMUA ARMOR</bold></gradient>", "ARMOR");

        // Weapons & Tools row
        addCategory(20, Material.NETHERITE_SWORD, "<gradient:#3498db:#2980b9><bold>⚔ SWORDS (Pedang)</bold></gradient>", "SWORD");
        addCategory(21, Material.BOW, "<gradient:#2ecc71:#27ae60><bold>🏹 BOW & CROSSBOW</bold></gradient>", "BOW");
        addCategory(22, Material.NETHERITE_PICKAXE, "<gradient:#f39c12:#d35400><bold>⛏ PICKAXES (Beliung)</bold></gradient>", "PICKAXE");
        addCategory(23, Material.NETHERITE_AXE, "<gradient:#e67e22:#d35400><bold>🪓 AXES (Kapak Tempur & Tebang)</bold></gradient>", "AXE");
        addCategory(24, Material.NETHERITE_SHOVEL, "<gradient:#1abc9c:#16a085><bold>🌱 SHOVELS & HOES</bold></gradient>", "SHOVEL");

        // Misc & Global row
        addCategory(29, Material.FISHING_ROD, "<gradient:#9b59b6:#8e44ad><bold>🎣 FISHING ROD & LAINNYA</bold></gradient>", "FISHING");
        addCategory(33, Material.NETHER_STAR, "<gradient:#f1c40f:#e74c3c><bold>✦ SEMUA KATEGORI ✦</bold></gradient>", "ALL");

        // Bottom Navigation
        // Slot 45: Back to Enchanter
        inventory.setItem(45, createItem(Material.ARROW, "<gradient:#3498db:#2980b9><bold>⬅ KEMBALI KE GACHA ENCHANTER</bold></gradient>", List.of(
                mm.deserialize("<gray>Kembali ke menu gacha random book.</gray>")
        )));

        // Slot 53: Close
        inventory.setItem(53, createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>", null));
    }

    private void addCategory(int slot, Material mat, String name, String categoryKey) {
        slotCategoryMap.put(slot, categoryKey);
        inventory.setItem(slot, createItem(mat, name, List.of(
                mm.deserialize("<gray>Kategori: <gold>" + categoryKey + "</gold></gray>"),
                Component.empty(),
                mm.deserialize("<yellow>▶ Klik untuk melihat custom enchant kategori ini!</yellow>")
        )));
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            new EnchanterGUI(plugin, player).open();
            return;
        }

        if (slot == 53) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            player.closeInventory();
            return;
        }

        String cat = slotCategoryMap.get(slot);
        if (cat != null) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            new SpecificBookShopGUI(plugin, player, cat).open();
        }
    }

    private ItemStack createItem(Material mat, String name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            if (lore != null) meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
