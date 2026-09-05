package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
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
import java.util.function.Consumer;

/**
 * Visual Level Selection GUI for Vanilla Enchantments.
 */
public class VanillaLevelPickerGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final ItemStack item;
    private final Enchantment enchant;
    private final InventoryHolder returnGUI;
    private final Consumer<ItemStack> onUpdate;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Integer, Integer> slotLevelMap = new HashMap<>();

    public VanillaLevelPickerGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, Enchantment enchant, InventoryHolder returnGUI, Consumer<ItemStack> onUpdate) {
        this.plugin = plugin;
        this.player = player;
        this.item = item;
        this.enchant = enchant;
        this.returnGUI = returnGUI;
        this.onUpdate = onUpdate;
        String eName = formatEnchantName(enchant.getKey().getKey());
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#f1c40f:#e67e22><bold>PILIH LEVEL: " + eName + " (I - XX)</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotLevelMap.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, border);
        }

        String eName = formatEnchantName(enchant.getKey().getKey());
        int currentLvl = item.getEnchantmentLevel(enchant);

        // Discrete Level buttons 1..20
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33
        };

        for (int lvl = 1; lvl <= 20 && lvl - 1 < slots.length; lvl++) {
            int slot = slots[lvl - 1];
            slotLevelMap.put(slot, lvl);

            boolean isCurrent = (lvl == currentLvl);
            ItemStack lvlBtn = new ItemStack(Material.EXPERIENCE_BOTTLE, lvl);
            ItemMeta meta = lvlBtn.getItemMeta();
            if (meta != null) {
                String roman = CustomEnchant.toRoman(lvl);
                meta.displayName(mm.deserialize("<gold><bold>" + eName + " " + roman + "</bold></gold>" + (isCurrent ? " <green><bold>[AKTIF]</bold></green>" : "")));
                List<Component> lore = new ArrayList<>();
                lore.add(mm.deserialize("<gray>Sihir Vanilla: <aqua>" + eName + "</aqua></gray>"));
                lore.add(mm.deserialize("<gray>Tingkat: <yellow>" + roman + " (" + lvl + ")</yellow></gray>"));
                lore.add(Component.empty());
                if (isCurrent) {
                    lore.add(mm.deserialize("<green>● Tingkat ini sedang terpasang pada item.</green>"));
                } else {
                    lore.add(mm.deserialize("<yellow>▶ Klik untuk menerapkan Level " + roman + " ke item!</yellow>"));
                }
                meta.lore(lore);
                lvlBtn.setItemMeta(meta);
            }
            inventory.setItem(slot, lvlBtn);
        }

        // Slot 49: Remove enchant button
        if (currentLvl > 0) {
            inventory.setItem(49, createItem(Material.BARRIER,
                    "<red><bold>✖ HAPUS ENCHANT VANILLA INI</bold></red>",
                    List.of(
                            mm.deserialize("<gray>Lepas enchant <yellow>" + eName + "</yellow> dari item.</gray>"),
                            Component.empty(),
                            mm.deserialize("<red>▶ Klik untuk menghapus enchant ini</red>")
                    )));
        }

        // Slot 45: Back button
        inventory.setItem(45, createItem(Material.ARROW, "<gradient:#3498db:#2980b9><bold>⬅ KEMBALI</bold></gradient>", List.of(
                mm.deserialize("<gray>Kembali ke menu sebelumnya.</gray>")
        )));
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

        if (slot == 49) {
            item.removeEnchantment(enchant);
            ItemStack updated = plugin.getEnchantmentRegistry().updateLoreAndGlint(item);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
            player.sendMessage(mm.deserialize("<yellow>Enchant <gold>" + formatEnchantName(enchant.getKey().getKey()) + "</gold> berhasil dihapus dari item!</yellow>"));
            if (onUpdate != null) onUpdate.accept(updated);
            if (returnGUI != null) {
                player.openInventory(returnGUI.getInventory());
            } else {
                player.closeInventory();
            }
            return;
        }

        if (slotLevelMap.containsKey(slot)) {
            int selectedLvl = slotLevelMap.get(slot);
            item.addUnsafeEnchantment(enchant, selectedLvl);
            ItemStack updated = plugin.getEnchantmentRegistry().updateLoreAndGlint(item);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.4f);
            player.sendMessage(mm.deserialize("<green>Berhasil menerapkan enchant vanilla <gold>" + formatEnchantName(enchant.getKey().getKey()) + " " + CustomEnchant.toRoman(selectedLvl) + "</gold>!</green>"));
            if (onUpdate != null) onUpdate.accept(updated);
            if (returnGUI != null) {
                player.openInventory(returnGUI.getInventory());
            } else {
                player.closeInventory();
            }
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
