package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import com.apexsions.customenchants.group.EnchantmentGroup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

/**
 * 54-Slot GUI allowing selective 1-click removal of individual custom enchantments
 * or vanilla enchantments attached to an item in Item Creator, without needing to reset all enchants.
 */
public class RemoveEnchantsGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private ItemStack item;
    private final InventoryHolder returnGUI;
    private final Consumer<ItemStack> onUpdate;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private int page = 1;
    private final Map<Integer, CustomEnchant> slotCustomEnchantMap = new HashMap<>();
    private final Map<Integer, Enchantment> slotVanillaEnchantMap = new HashMap<>();

    public RemoveEnchantsGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, InventoryHolder returnGUI, Consumer<ItemStack> onUpdate) {
        this.plugin = plugin;
        this.player = player;
        this.item = item;
        this.returnGUI = returnGUI;
        this.onUpdate = onUpdate;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#c0392b><bold>✖ HAPUS ENCHANT TERTENTU ✖</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotCustomEnchantMap.clear();
        slotVanillaEnchantMap.clear();

        // 1. Decorative border
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
        }
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, border);
        }
        int[] sideSlots = {9, 17, 18, 26, 27, 35, 36, 44};
        for (int s : sideSlots) {
            inventory.setItem(s, border);
        }

        // Slot 4: Item Preview
        inventory.setItem(4, item);

        // 2. Collect active custom enchants and vanilla enchants
        Map<CustomEnchant, Integer> activeCustom = plugin.getEnchantmentRegistry().getEnchantsOnItem(item);
        Map<Enchantment, Integer> activeVanilla = new LinkedHashMap<>(item.getEnchantments());

        List<Object> enchantList = new ArrayList<>();
        enchantList.addAll(activeCustom.keySet());
        enchantList.addAll(activeVanilla.keySet());

        int centerSlotsCount = 28; // 4 rows of 7 slots: 10..16, 19..25, 28..34, 37..43
        int totalPages = Math.max(1, (int) Math.ceil((double) enchantList.size() / centerSlotsCount));
        page = Math.max(1, Math.min(totalPages, page));

        if (enchantList.isEmpty()) {
            inventory.setItem(22, createItem(Material.BARRIER,
                    "<gray><bold>TIDAK ADA ENCHANT AKTIF</bold></gray>",
                    List.of(
                            mm.deserialize("<gray>Item ini tidak memiliki sihir custom ataupun enchant vanilla.</gray>"),
                            Component.empty(),
                            mm.deserialize("<yellow>Pasang sihir terlebih dahulu di menu enchant!</yellow>")
                    )));
        } else {
            int startIndex = (page - 1) * centerSlotsCount;
            int endIndex = Math.min(startIndex + centerSlotsCount, enchantList.size());

            int[] displaySlots = {
                    10, 11, 12, 13, 14, 15, 16,
                    19, 20, 21, 22, 23, 24, 25,
                    28, 29, 30, 31, 32, 33, 34,
                    37, 38, 39, 40, 41, 42, 43
            };

            int slotIdx = 0;
            for (int i = startIndex; i < endIndex; i++) {
                int slot = displaySlots[slotIdx++];
                Object entry = enchantList.get(i);

                if (entry instanceof CustomEnchant ce) {
                    slotCustomEnchantMap.put(slot, ce);
                    int lvl = activeCustom.getOrDefault(ce, 1);
                    EnchantmentGroup grp = ce.getGroup();

                    ItemStack star = new ItemStack(Material.FIREWORK_STAR);
                    FireworkEffectMeta meta = (FireworkEffectMeta) star.getItemMeta();
                    if (meta != null) {
                        FireworkEffect effect = FireworkEffect.builder()
                                .withColor(grp.getBukkitColor())
                                .build();
                        meta.setEffect(effect);

                        String title = "<color:" + grp.getColor() + "><bold>✦ " + ce.getDisplayName() + " " + CustomEnchant.toRoman(lvl) + "</bold></color>";
                        meta.displayName(mm.deserialize(title));

                        List<Component> lore = new ArrayList<>();
                        lore.add(mm.deserialize("<gray>Tipe: <gold>Custom Enchantment</gold></gray>"));
                        lore.add(mm.deserialize("<gray>Tier: " + grp.getDisplayName() + "</gray>"));
                        lore.add(mm.deserialize("<gray>Tingkat: <gold>" + CustomEnchant.toRoman(lvl) + " (" + lvl + ")</gold></gray>"));
                        lore.add(mm.deserialize("<gray>Target: <aqua>" + ce.getAppliesTo() + "</aqua></gray>"));
                        lore.add(Component.empty());
                        lore.add(mm.deserialize("<yellow>" + ce.getDescription() + "</yellow>"));
                        lore.add(Component.empty());
                        lore.add(mm.deserialize("<red><bold>✖ KLIK UNTUK MELEPAS SIHIR INI</bold></red>"));
                        lore.add(mm.deserialize("<gray>Hanya sihir ini yang akan dihapus dari item.</gray>"));

                        meta.lore(lore);
                        star.setItemMeta(meta);
                    }
                    inventory.setItem(slot, star);

                } else if (entry instanceof Enchantment ve) {
                    slotVanillaEnchantMap.put(slot, ve);
                    int lvl = activeVanilla.getOrDefault(ve, 1);
                    String eName = formatEnchantName(ve.getKey().getKey());

                    ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                    ItemMeta meta = book.getItemMeta();
                    if (meta != null) {
                        meta.displayName(mm.deserialize("<gold><bold>📜 " + eName + " " + CustomEnchant.toRoman(lvl) + "</bold></gold>"));

                        List<Component> lore = new ArrayList<>();
                        lore.add(mm.deserialize("<gray>Tipe: <aqua>Vanilla Enchantment</aqua></gray>"));
                        lore.add(mm.deserialize("<gray>Key: <yellow>" + ve.getKey().getKey() + "</yellow></gray>"));
                        lore.add(mm.deserialize("<gray>Tingkat: <yellow>" + CustomEnchant.toRoman(lvl) + " (" + lvl + ")</yellow></gray>"));
                        lore.add(Component.empty());
                        lore.add(mm.deserialize("<red><bold>✖ KLIK UNTUK MELEPAS ENCHANT INI</bold></red>"));
                        lore.add(mm.deserialize("<gray>Hanya enchant vanilla ini yang akan dihapus dari item.</gray>"));

                        meta.lore(lore);
                        book.setItemMeta(meta);
                    }
                    inventory.setItem(slot, book);
                }
            }

            // Fill leftover display slots with gray glass
            while (slotIdx < displaySlots.length) {
                inventory.setItem(displaySlots[slotIdx++], createItem(Material.GRAY_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null));
            }
        }

        // 3. Bottom Controls
        // Slot 45: Back
        inventory.setItem(45, createItem(Material.ARROW, "<gradient:#3498db:#2980b9><bold>⬅ SELESAI & KEMBALI</bold></gradient>", List.of(
                mm.deserialize("<gray>Kembali ke menu edit item.</gray>")
        )));

        // Slot 48: Prev Page
        if (page > 1) {
            inventory.setItem(48, createItem(Material.SPECTRAL_ARROW, "<yellow><bold>⬅ Halaman Sebelumnya (" + (page - 1) + ")</bold></yellow>", null));
        }

        // Slot 49: Info & Counter
        inventory.setItem(49, createItem(Material.COMPASS, "<gold><bold>Daftar Enchant Aktif</bold></gold>", List.of(
                mm.deserialize("<gray>Custom Enchants: <gold>" + activeCustom.size() + " Sihir</gold></gray>"),
                mm.deserialize("<gray>Vanilla Enchants: <aqua>" + activeVanilla.size() + " Sihir</aqua></gray>"),
                mm.deserialize("<gray>Total Terpasang: <yellow>" + enchantList.size() + " Sihir</yellow></gray>"),
                Component.empty(),
                mm.deserialize("<gray>Halaman: <yellow>" + page + " / " + totalPages + "</yellow></gray>")
        )));

        // Slot 50: Next Page
        if (page < totalPages) {
            inventory.setItem(50, createItem(Material.SPECTRAL_ARROW, "<yellow><bold>Halaman Berikutnya (" + (page + 1) + ") ➡</bold></yellow>", null));
        }

        // Slot 53: Reset All Shortcut
        if (!enchantList.isEmpty()) {
            inventory.setItem(53, createItem(Material.CAULDRON,
                    "<red><bold>🗑 HAPUS SEMUA SEKALIGUS</bold></red>",
                    List.of(
                            mm.deserialize("<gray>Hapus seluruh sihir sekaligus jika diperlukan.</gray>"),
                            Component.empty(),
                            mm.deserialize("<red>▶ Klik untuk reset semua enchant</red>")
                    )));
        }
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        // 1. Back button
        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            if (returnGUI != null) {
                player.openInventory(returnGUI.getInventory());
            } else {
                player.closeInventory();
            }
            return;
        }

        // 2. Pagination
        if (slot == 48 && page > 1) {
            page--;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            buildGUI();
            return;
        }

        if (slot == 50) {
            Map<CustomEnchant, Integer> activeCustom = plugin.getEnchantmentRegistry().getEnchantsOnItem(item);
            Map<Enchantment, Integer> activeVanilla = item.getEnchantments();
            int totalSize = activeCustom.size() + activeVanilla.size();
            int totalPages = Math.max(1, (int) Math.ceil((double) totalSize / 28));
            if (page < totalPages) {
                page++;
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                buildGUI();
                return;
            }
        }

        // 3. Reset All Shortcut
        if (slot == 53) {
            for (CustomEnchant ce : plugin.getEnchantmentRegistry().getAllEnchantments()) {
                item = plugin.getEnchantmentRegistry().removeEnchant(item, ce);
            }
            for (Enchantment ve : new ArrayList<>(item.getEnchantments().keySet())) {
                item.removeEnchantment(ve);
            }
            item = plugin.getEnchantmentRegistry().updateLoreAndGlint(item);
            player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<yellow>Seluruh enchantment berhasil dihapus dari item!</yellow>"));
            if (onUpdate != null) onUpdate.accept(item);
            buildGUI();
            return;
        }

        // 4. Remove specific Custom Enchant
        if (slotCustomEnchantMap.containsKey(slot)) {
            CustomEnchant ce = slotCustomEnchantMap.get(slot);
            item = plugin.getEnchantmentRegistry().removeEnchant(item, ce);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
            player.sendMessage(mm.deserialize("<yellow>Sihir <gold>" + ce.getDisplayName() + "</gold> berhasil dilepas dari item!</yellow>"));
            if (onUpdate != null) onUpdate.accept(item);
            buildGUI();
            return;
        }

        // 5. Remove specific Vanilla Enchant
        if (slotVanillaEnchantMap.containsKey(slot)) {
            Enchantment ve = slotVanillaEnchantMap.get(slot);
            item.removeEnchantment(ve);
            item = plugin.getEnchantmentRegistry().updateLoreAndGlint(item);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
            player.sendMessage(mm.deserialize("<yellow>Enchant vanilla <gold>" + formatEnchantName(ve.getKey().getKey()) + "</gold> berhasil dilepas dari item!</yellow>"));
            if (onUpdate != null) onUpdate.accept(item);
            buildGUI();
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
