package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

/**
 * 54-Slot Paginated Vanilla Enchantment Browser for selecting vanilla enchantments via GUI.
 */
public class VanillaEnchantPickerGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final ItemStack item;
    private final InventoryHolder returnGUI;
    private final Consumer<ItemStack> onUpdate;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private int page = 1;
    private final List<Enchantment> allEnchants = new ArrayList<>();
    private final Map<Integer, Enchantment> slotEnchantMap = new HashMap<>();

    public VanillaEnchantPickerGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, InventoryHolder returnGUI, Consumer<ItemStack> onUpdate) {
        this.plugin = plugin;
        this.player = player;
        this.item = item;
        this.returnGUI = returnGUI;
        this.onUpdate = onUpdate;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#f1c40f:#e67e22><bold>📜 PILIH VANILLA ENCHANT 📜</bold></gradient>"));

        for (Enchantment e : Registry.ENCHANTMENT) {
            allEnchants.add(e);
        }
        allEnchants.sort(Comparator.comparing(e -> e.getKey().getKey()));

        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotEnchantMap.clear();

        // Top Border
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
        }

        int pageSize = 36;
        int totalPages = Math.max(1, (int) Math.ceil((double) allEnchants.size() / pageSize));
        page = Math.max(1, Math.min(totalPages, page));

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allEnchants.size());

        int currentSlot = 9;
        for (int i = startIndex; i < endIndex; i++) {
            Enchantment enchant = allEnchants.get(i);
            slotEnchantMap.put(currentSlot, enchant);

            String eName = formatEnchantName(enchant.getKey().getKey());
            int currentLvl = item.getEnchantmentLevel(enchant);
            boolean isAttached = currentLvl > 0;

            ItemStack book = new ItemStack(isAttached ? Material.ENCHANTED_BOOK : Material.BOOK);
            ItemMeta meta = book.getItemMeta();
            if (meta != null) {
                meta.displayName(mm.deserialize("<gold><bold>" + eName + "</bold></gold>"));
                List<Component> lore = new ArrayList<>();
                lore.add(mm.deserialize("<gray>Key: <yellow>" + enchant.getKey().getKey() + "</yellow></gray>"));
                lore.add(mm.deserialize("<gray>Level Maks Vanilla: <aqua>" + enchant.getMaxLevel() + "</aqua></gray>"));
                lore.add(Component.empty());
                if (isAttached) {
                    lore.add(mm.deserialize("<green><bold>● Terpasang: Level " + CustomEnchant.toRoman(currentLvl) + "</bold></green>"));
                } else {
                    lore.add(mm.deserialize("<dark_gray>● Belum terpasang pada item.</dark_gray>"));
                }
                lore.add(mm.deserialize("<yellow>▶ Klik untuk memilih tingkat enchant via GUI!</yellow>"));
                meta.lore(lore);
                book.setItemMeta(meta);
            }
            inventory.setItem(currentSlot++, book);
        }

        while (currentSlot < 45) {
            inventory.setItem(currentSlot++, createItem(Material.GRAY_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null));
        }

        // Bottom Navigation Bar
        for (int s = 45; s < 54; s++) {
            inventory.setItem(s, border);
        }

        // Slot 45: Back
        inventory.setItem(45, createItem(Material.ARROW, "<gradient:#3498db:#2980b9><bold>⬅ KEMBALI</bold></gradient>", List.of(
                mm.deserialize("<gray>Kembali ke menu edit item.</gray>")
        )));

        // Slot 48: Prev
        if (page > 1) {
            inventory.setItem(48, createItem(Material.SPECTRAL_ARROW, "<yellow><bold>⬅ Halaman Sebelumnya (" + (page - 1) + ")</bold></yellow>", null));
        }

        // Slot 49: Page indicator
        inventory.setItem(49, createItem(Material.COMPASS, "<gold><bold>Halaman " + page + " / " + totalPages + "</bold></gold>", List.of(
                mm.deserialize("<gray>Total Enchant Vanilla: <yellow>" + allEnchants.size() + "</yellow></gray>")
        )));

        // Slot 50: Next
        if (page < totalPages) {
            inventory.setItem(50, createItem(Material.SPECTRAL_ARROW, "<yellow><bold>Halaman Berikutnya (" + (page + 1) + ") ➡</bold></yellow>", null));
        }
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            if (returnGUI != null) {
                player.openInventory(returnGUI.getInventory());
            } else {
                player.closeInventory();
            }
            return;
        }

        if (slot == 48 && page > 1) {
            page--;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            buildGUI();
            return;
        }

        if (slot == 50) {
            int totalPages = Math.max(1, (int) Math.ceil((double) allEnchants.size() / 36));
            if (page < totalPages) {
                page++;
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                buildGUI();
                return;
            }
        }

        if (slotEnchantMap.containsKey(slot)) {
            Enchantment enchant = slotEnchantMap.get(slot);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.4f);
            new VanillaLevelPickerGUI(plugin, player, item, enchant, this, onUpdate).open();
        }
    }

    private String formatEnchantName(String key) {
        String[] parts = key.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1).toLowerCase()).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private ItemStack createItem(Material mat, String name, List<Component> lore) {
        ItemStack is = new ItemStack(mat);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            if (name != null) meta.displayName(mm.deserialize(name));
            if (lore != null) meta.lore(lore);
            is.setItemMeta(meta);
        }
        return is;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
