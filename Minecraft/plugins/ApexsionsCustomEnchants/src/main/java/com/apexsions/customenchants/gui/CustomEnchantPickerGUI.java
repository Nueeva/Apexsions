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
 * 54-Slot Paginated Custom Enchantment Browser displaying enchantments as colored FIREWORK_STAR items according to their tier/rarity.
 */
public class CustomEnchantPickerGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final ItemStack item;
    private final InventoryHolder returnGUI;
    private final Consumer<ItemStack> onUpdate;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private int page = 1;
    private String groupFilter = "ALL";
    private final Map<Integer, CustomEnchant> slotEnchantMap = new HashMap<>();

    public CustomEnchantPickerGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, InventoryHolder returnGUI, Consumer<ItemStack> onUpdate) {
        this.plugin = plugin;
        this.player = player;
        this.item = item;
        this.returnGUI = returnGUI;
        this.onUpdate = onUpdate;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#9b59b6:#e74c3c><bold>✦ PILIH CUSTOM ENCHANT ✦</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotEnchantMap.clear();

        // 1. Top Filter Row (Slots 1..7)
        String[] groups = {"ALL", "SIMPLE", "UNIQUE", "ELITE", "ULTIMATE", "LEGENDARY", "FABLED"};
        int[] filterSlots = {1, 2, 3, 4, 5, 6, 7};
        for (int i = 0; i < groups.length; i++) {
            String g = groups[i];
            boolean isSel = groupFilter.equalsIgnoreCase(g);
            Material mat = isSel ? Material.GLOWSTONE : Material.REDSTONE_LAMP;
            String name = isSel ? "<yellow><bold>[" + g + "]</bold></yellow>" : "<gray>[" + g + "]</gray>";
            inventory.setItem(filterSlots[i], createItem(mat, name, List.of(
                    mm.deserialize("<gray>Klik untuk memfilter sihir berdasarkan grup " + g + ".</gray>")
            )));
        }

        // Fill remaining top slots with borders
        inventory.setItem(0, createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null));
        inventory.setItem(8, createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null));

        // 2. Gather filtered enchantments (auto-filtered to item compatibility)
        List<CustomEnchant> list = new ArrayList<>();
        for (CustomEnchant e : plugin.getEnchantmentRegistry().getAllEnchantments()) {
            if (item != null && !item.getType().isAir() && !e.canApplyTo(item)) {
                continue;
            }
            if (!groupFilter.equalsIgnoreCase("ALL") && !e.getGroup().getId().equalsIgnoreCase(groupFilter)) {
                continue;
            }
            list.add(e);
        }

        int pageSize = 36;
        int totalPages = Math.max(1, (int) Math.ceil((double) list.size() / pageSize));
        page = Math.max(1, Math.min(totalPages, page));

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, list.size());

        int currentSlot = 9;
        for (int i = startIndex; i < endIndex; i++) {
            CustomEnchant enchant = list.get(i);
            slotEnchantMap.put(currentSlot, enchant);

            EnchantmentGroup grp = enchant.getGroup();
            int currentLvl = plugin.getEnchantmentRegistry().getEnchantLevel(item, enchant);
            boolean isAttached = currentLvl > 0;

            // Use FIREWORK_STAR dyed with group color
            ItemStack star = new ItemStack(Material.FIREWORK_STAR);
            FireworkEffectMeta meta = (FireworkEffectMeta) star.getItemMeta();
            if (meta != null) {
                FireworkEffect effect = FireworkEffect.builder()
                        .withColor(grp.getBukkitColor())
                        .build();
                meta.setEffect(effect);

                String title = "<color:" + grp.getColor() + "><bold>✦ " + enchant.getDisplayName() + "</bold></color>";
                meta.displayName(mm.deserialize(title));

                List<Component> lore = new ArrayList<>();
                lore.add(mm.deserialize("<gray>Tier: " + grp.getDisplayName() + "</gray>"));
                lore.add(mm.deserialize("<gray>Level Maks: <gold>" + CustomEnchant.toRoman(enchant.getMaxLevel()) + " (" + enchant.getMaxLevel() + ")</gold></gray>"));
                lore.add(mm.deserialize("<gray>Target: <aqua>" + enchant.getAppliesTo() + "</aqua></gray>"));
                lore.add(Component.empty());
                lore.add(mm.deserialize("<yellow>" + enchant.getDescription() + "</yellow>"));
                lore.add(Component.empty());
                if (isAttached) {
                    lore.add(mm.deserialize("<green><bold>● Terpasang: Level " + CustomEnchant.toRoman(currentLvl) + "</bold></green>"));
                } else {
                    lore.add(mm.deserialize("<dark_gray>● Belum terpasang pada item.</dark_gray>"));
                }
                lore.add(mm.deserialize("<yellow>▶ Klik untuk memilih tingkat sihir via GUI!</yellow>"));

                meta.lore(lore);
                star.setItemMeta(meta);
            }
            inventory.setItem(currentSlot++, star);
        }

        // Fill remaining center slots with gray glass
        while (currentSlot < 45) {
            inventory.setItem(currentSlot++, createItem(Material.GRAY_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null));
        }

        // 3. Bottom Navigation Bar (Row 5)
        for (int s = 45; s < 54; s++) {
            inventory.setItem(s, createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null));
        }

        // Slot 45: Back
        inventory.setItem(45, createItem(Material.ARROW, "<gradient:#3498db:#2980b9><bold>⬅ KEMBALI</bold></gradient>", List.of(
                mm.deserialize("<gray>Kembali ke menu edit item.</gray>")
        )));

        // Slot 48: Prev Page
        if (page > 1) {
            inventory.setItem(48, createItem(Material.SPECTRAL_ARROW, "<yellow><bold>⬅ Halaman Sebelumnya (" + (page - 1) + ")</bold></yellow>", null));
        }

        // Slot 49: Page Indicator
        inventory.setItem(49, createItem(Material.COMPASS, "<gold><bold>Halaman " + page + " / " + totalPages + "</bold></gold>", List.of(
                mm.deserialize("<gray>Total Sihir: <yellow>" + list.size() + "</yellow></gray>")
        )));

        // Slot 50: Next Page
        if (page < totalPages) {
            inventory.setItem(50, createItem(Material.SPECTRAL_ARROW, "<yellow><bold>Halaman Berikutnya (" + (page + 1) + ") ➡</bold></yellow>", null));
        }
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        // 1. Check Filters (Slots 1..7)
        int[] filterSlots = {1, 2, 3, 4, 5, 6, 7};
        String[] groups = {"ALL", "SIMPLE", "UNIQUE", "ELITE", "ULTIMATE", "LEGENDARY", "FABLED"};
        for (int i = 0; i < filterSlots.length; i++) {
            if (slot == filterSlots[i]) {
                groupFilter = groups[i];
                page = 1;
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                buildGUI();
                return;
            }
        }

        // 2. Navigation
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
            List<CustomEnchant> list = new ArrayList<>(plugin.getEnchantmentRegistry().getAllEnchantments());
            if (!groupFilter.equalsIgnoreCase("ALL")) {
                list.removeIf(e -> !e.getGroup().getId().equalsIgnoreCase(groupFilter));
            }
            int totalPages = Math.max(1, (int) Math.ceil((double) list.size() / 36));
            if (page < totalPages) {
                page++;
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                buildGUI();
                return;
            }
        }

        // 3. Enchantment Selection
        if (slotEnchantMap.containsKey(slot)) {
            CustomEnchant enchant = slotEnchantMap.get(slot);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.4f);
            new EnchantLevelPickerGUI(plugin, player, item, enchant, this, onUpdate).open();
        }
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
