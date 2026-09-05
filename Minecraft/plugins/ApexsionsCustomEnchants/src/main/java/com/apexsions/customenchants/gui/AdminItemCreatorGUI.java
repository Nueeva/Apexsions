package com.apexsions.customenchants.gui;

import com.apexsions.core.kit.KitStatType;
import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Interactive Admin Item & Fullset Armor Creator GUI (/ace create).
 * Features:
 * - 4 dedicated Top-Center slots for Fullset Armor (Helmet, Chestplate, Leggings, Boots).
 * - Auto-detection of Fullset Armor and automatic Set Bonus propagation.
 * - 7 dedicated Bottom-Center slots for Weapons and Tools.
 * - Strict item-type validation (rejects invalid items).
 * - Clicking placed items opens dedicated ItemModifierGUI for visual enchant & stat editing.
 */
public class AdminItemCreatorGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // Slot definitions
    public static final int SLOT_HELMET = 10;
    public static final int SLOT_CHESTPLATE = 11;
    public static final int SLOT_LEGGINGS = 12;
    public static final int SLOT_BOOTS = 13;
    public static final int SLOT_SET_STATUS = 15;

    public static final int[] TOOL_SLOTS = {28, 29, 30, 31, 32, 33, 34};

    // Stored items in slots
    private final Map<Integer, ItemStack> placedItems = new HashMap<>();

    // Global Set Bonus State for Fullset Armor
    private boolean setBonusActive = false;
    private String globalSetName = "Warlord";
    private KitStatType globalStatType = KitStatType.DAMAGE_REDUCTION;
    private int globalRequiredPieces = 4;
    private double globalBonusValue = 20.0;

    public AdminItemCreatorGUI(ApexsionsCustomEnchantsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>🛠 APEXSIONS ITEM & FULLSET CREATOR 🛠</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void updateItem(int slot, ItemStack newItem) {
        if (newItem == null || newItem.getType().isAir()) {
            placedItems.remove(slot);
        } else {
            placedItems.put(slot, newItem);
        }
        checkAndApplyFullsetBonus();
    }

    public void buildGUI() {
        inventory.clear();

        // 1. Decorative Borders
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null, false);
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, border);
        }

        // Slot 4: Info Beacon
        inventory.setItem(4, createItem(Material.BEACON,
                "<gradient:#f1c40f:#e67e22><bold>✦ PUSAT PEMBUAT ITEM & FULLSET ARMOR ✦</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Seret (drag & drop) armor atau senjata/alat ke slot kosong di bawah.</gray>"),
                        mm.deserialize("<gray>Sistem akan otomatis memvalidasi jenis item yang dimasukkan.</gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>● Klik item yang sudah ditaruh untuk membuka GUI Edit Enchants & Stats!</yellow>"),
                        mm.deserialize("<yellow>● Shift + Klik Kanan item untuk mengambilnya kembali ke inventaris.</yellow>")
                ), true));

        // Separator Row (Row 2: 18..26)
        ItemStack sep = createItem(Material.GRAY_STAINED_GLASS_PANE, "<dark_gray>⛓</dark_gray>", null, false);
        for (int i = 18; i < 27; i++) {
            inventory.setItem(i, sep);
        }
        inventory.setItem(22, createItem(Material.ANVIL,
                "<gold><bold>⚔ AREA SENJATA & PERALATAN (TOOLS) ⚔</bold></gold>",
                List.of(
                        mm.deserialize("<gray>Letakkan Pedang, Kapak, Pickaxe, Busur, dll pada slot di bawah.</gray>")
                ), false));

        // 2. Render 4 Armor Slots (Slots 10..13)
        renderArmorSlot(SLOT_HELMET, Material.CHAINMAIL_HELMET, "<yellow>🪖 Slot Helmet (Kosong)</yellow>", "<gray>Seret Helmet ke slot ini.</gray>");
        renderArmorSlot(SLOT_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, "<yellow>🦺 Slot Chestplate (Kosong)</yellow>", "<gray>Seret Chestplate ke slot ini.</gray>");
        renderArmorSlot(SLOT_LEGGINGS, Material.CHAINMAIL_LEGGINGS, "<yellow>👖 Slot Leggings (Kosong)</yellow>", "<gray>Seret Leggings ke slot ini.</gray>");
        renderArmorSlot(SLOT_BOOTS, Material.CHAINMAIL_BOOTS, "<yellow>🥾 Slot Boots (Kosong)</yellow>", "<gray>Seret Boots ke slot ini.</gray>");

        // 3. Render Armor Set Status Indicator (Slot 15)
        boolean isFullset = isFullsetComplete();
        List<Component> statusLore = new ArrayList<>();
        statusLore.add(isFullset
                ? mm.deserialize("<green><bold>✓ FULLSET ARMOR LENGKAP TERDETEKSI!</bold></green>")
                : mm.deserialize("<yellow><bold>⚠ BELUM FULLSET</bold> <gray>(Lengkapi 4 slot armor)</gray></yellow>"));
        statusLore.add(Component.empty());
        statusLore.add(mm.deserialize("<gray>Status Set Bonus: " + (setBonusActive ? "<green><bold>AKTIF</bold></green>" : "<red><bold>NONAKTIF</bold></red>") + "</gray>"));
        if (setBonusActive) {
            statusLore.add(mm.deserialize("<gray>Nama Set: <gold>" + globalSetName + "</gold></gray>"));
            statusLore.add(mm.deserialize("<gray>Tipe Stat: <aqua>" + globalStatType.getDisplayName() + "</aqua></gray>"));
            statusLore.add(mm.deserialize("<gray>Besaran: <green>+" + (int) globalBonusValue + "%</green></gray>"));
            statusLore.add(mm.deserialize("<gray>Syarat: <yellow>" + globalRequiredPieces + " Pieces</yellow></gray>"));
        }
        statusLore.add(Component.empty());
        statusLore.add(mm.deserialize("<yellow>▶ Klik untuk mengatur/mengaktifkan Armor Set Bonus Fullset via GUI!</yellow>"));

        inventory.setItem(SLOT_SET_STATUS, createItem(
                isFullset ? Material.NETHER_STAR : Material.SHIELD,
                "<gradient:#e74c3c:#f39c12><bold>🛡 PENGATURAN FULLSET ARMOR BONUS 🛡</bold></gradient>",
                statusLore,
                setBonusActive
        ));

        // 4. Render 7 Tool/Weapon Slots (Slots 28..34)
        for (int tSlot : TOOL_SLOTS) {
            if (placedItems.containsKey(tSlot)) {
                inventory.setItem(tSlot, placedItems.get(tSlot));
            } else {
                inventory.setItem(tSlot, createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                        "<gray>⚔ Slot Tool/Weapon (Kosong)</gray>",
                        List.of(
                                mm.deserialize("<dark_gray>Seret Pedang, Kapak, Pickaxe, atau Busur ke slot ini.</dark_gray>")
                        ), false));
            }
        }

        // 5. Bottom Navigation & Action Bar (Row 5)
        // Slot 45: Return all items
        inventory.setItem(45, createItem(Material.HOPPER,
                "<red><bold>❌ KEMBALIKAN SEMUA ITEM</bold></red>",
                List.of(
                        mm.deserialize("<gray>Kembalikan seluruh armor & tools di atas ke inventarismu.</gray>"),
                        Component.empty(),
                        mm.deserialize("<red>▶ Klik untuk mengambil kembali semua item</red>")
                ), false));

        // Slot 49: Finish and claim all modified items
        int totalPlaced = placedItems.size();
        inventory.setItem(49, createItem(Material.EMERALD_BLOCK,
                "<green><bold>✔ SELESAI & AMBIL SEMUA ITEM (" + totalPlaced + ")</bold></green>",
                List.of(
                        mm.deserialize("<gray>Terapkan seluruh enchant dan bonus,</gray>"),
                        mm.deserialize("<gray>lalu ambil semua item ke inventaris pemainmu!</gray>"),
                        Component.empty(),
                        mm.deserialize("<green>▶ Klik untuk menyelesaikan pembuatan item</green>")
                ), true));

        // Slot 53: Exit / Close
        inventory.setItem(53, createItem(Material.BARRIER,
                "<red><bold>✖ KELUAR & TUTUP</bold></red>",
                List.of(
                        mm.deserialize("<gray>Tutup menu creator (item tersisa aman dikembalikan).</gray>")
                ), false));
    }

    private void renderArmorSlot(int slot, Material placeholderMat, String title, String desc) {
        if (placedItems.containsKey(slot)) {
            inventory.setItem(slot, placedItems.get(slot));
        } else {
            inventory.setItem(slot, createItem(placeholderMat, title, List.of(
                    mm.deserialize(desc)
            ), false));
        }
    }

    public boolean isFullsetComplete() {
        return placedItems.containsKey(SLOT_HELMET) &&
               placedItems.containsKey(SLOT_CHESTPLATE) &&
               placedItems.containsKey(SLOT_LEGGINGS) &&
               placedItems.containsKey(SLOT_BOOTS);
    }

    private void checkAndApplyFullsetBonus() {
        if (!setBonusActive) return;
        if (!isFullsetComplete()) return;

        int[] armorSlots = {SLOT_HELMET, SLOT_CHESTPLATE, SLOT_LEGGINGS, SLOT_BOOTS};
        for (int s : armorSlots) {
            ItemStack piece = placedItems.get(s);
            if (piece != null) {
                applySetBonusToPiece(piece);
            }
        }
    }

    private void applySetBonusToPiece(ItemStack is) {
        ItemMeta meta = is.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey("apexsions", "set_id"), PersistentDataType.STRING, globalSetName.toLowerCase());
        pdc.set(new NamespacedKey("apexsions", "set_name"), PersistentDataType.STRING, globalSetName);
        pdc.set(new NamespacedKey("apexsions", "set_type"), PersistentDataType.STRING, globalStatType.name());
        pdc.set(new NamespacedKey("apexsions", "set_val"), PersistentDataType.DOUBLE, globalBonusValue);
        pdc.set(new NamespacedKey("apexsions", "set_req"), PersistentDataType.INTEGER, globalRequiredPieces);

        // Update lore
        List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.removeIf(c -> {
            String plain = mm.serialize(c);
            return plain.contains("SET BONUS") || plain.contains("Set Bonus:") || plain.contains("Pieces:");
        });

        lore.add(Component.empty());
        lore.add(mm.deserialize("<gradient:#e74c3c:#f39c12><bold>★ SET BONUS: " + globalSetName.toUpperCase() + " ★</bold></gradient>"));
        lore.add(mm.deserialize("<gray>Syarat: <gold>" + globalRequiredPieces + " Pieces</gold></gray>"));
        lore.add(mm.deserialize("<gray>Efek: <aqua>" + globalStatType.getDisplayName() + " +" + (int) globalBonusValue + "%</aqua></gray>"));

        meta.lore(lore);
        is.setItemMeta(meta);
    }

    public void handleClick(InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        ItemStack cursor = event.getCursor();

        // Check clicking on creator slots (top inventory 0..53)
        if (rawSlot >= 0 && rawSlot < 54) {
            // 1. Controls
            if (rawSlot == 53) {
                event.setCancelled(true);
                returnAllItems();
                player.closeInventory();
                return;
            }

            if (rawSlot == 45) {
                event.setCancelled(true);
                returnAllItems();
                buildGUI();
                return;
            }

            if (rawSlot == 49) {
                event.setCancelled(true);
                finishAndClaimAll();
                return;
            }

            // 2. Set Bonus Status Button (Slot 15)
            if (rawSlot == SLOT_SET_STATUS) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                // Open visual set bonus picker
                new ArmorSetBonusPickerGUI(plugin, player, null, this, updatedItem -> {}).open();
                return;
            }

            // 3. Armor Slots Interaction (Slots 10..13)
            if (rawSlot == SLOT_HELMET || rawSlot == SLOT_CHESTPLATE || rawSlot == SLOT_LEGGINGS || rawSlot == SLOT_BOOTS) {
                event.setCancelled(true);
                handleSlotPlacementOrEdit(rawSlot, cursor, event.getClick());
                return;
            }

            // 4. Tool Slots Interaction (Slots 28..34)
            for (int tSlot : TOOL_SLOTS) {
                if (rawSlot == tSlot) {
                    event.setCancelled(true);
                    handleToolSlotPlacementOrEdit(rawSlot, cursor, event.getClick());
                    return;
                }
            }

            // Other border/header clicks
            event.setCancelled(true);
            return;
        }

        // Bottom inventory (Player's own inventory)
        if (event.isShiftClick()) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && !clicked.getType().isAir()) {
                handleShiftClickPlacement(clicked);
            }
        }
    }

    private void handleSlotPlacementOrEdit(int slot, ItemStack cursor, ClickType click) {
        boolean hasPlaced = placedItems.containsKey(slot);

        // Case A: Cursor has an item -> attempt to place it
        if (cursor != null && !cursor.getType().isAir()) {
            if (!isValidArmorForSlot(slot, cursor)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<red>Item yang dimasukkan harus berupa " + getExpectedArmorName(slot) + "!</red>"));
                return;
            }

            ItemStack toPlace = cursor.clone();
            toPlace.setAmount(1);

            if (cursor.getAmount() > 1) {
                cursor.setAmount(cursor.getAmount() - 1);
            } else {
                player.setItemOnCursor(new ItemStack(Material.AIR));
            }

            if (hasPlaced) {
                ItemStack old = placedItems.get(slot);
                player.getInventory().addItem(old);
            }

            updateItem(slot, toPlace);
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1.0f, 1.2f);
            player.sendMessage(mm.deserialize("<green>Berhasil meletakkan <gold>" + toPlace.getType().name() + "</gold> ke slot armor!</green>"));
            buildGUI();
            return;
        }

        // Case B: Cursor is empty and slot has an item
        if (hasPlaced) {
            ItemStack placed = placedItems.get(slot);
            if (click == ClickType.SHIFT_RIGHT || click == ClickType.SHIFT_LEFT) {
                // Shift click takes item back
                placedItems.remove(slot);
                player.getInventory().addItem(placed);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
                player.sendMessage(mm.deserialize("<yellow>Item dikembalikan ke inventaris.</yellow>"));
                buildGUI();
            } else {
                // Regular click opens visual Edit GUI!
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                new ItemModifierGUI(plugin, player, placed, slot, this).open();
            }
        }
    }

    private void handleToolSlotPlacementOrEdit(int slot, ItemStack cursor, ClickType click) {
        boolean hasPlaced = placedItems.containsKey(slot);

        if (cursor != null && !cursor.getType().isAir()) {
            if (!isToolOrWeapon(cursor)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<red>Slot ini hanya menerima Senjata atau Peralatan (Tools & Weapons)!</red>"));
                return;
            }

            ItemStack toPlace = cursor.clone();
            toPlace.setAmount(1);

            if (cursor.getAmount() > 1) {
                cursor.setAmount(cursor.getAmount() - 1);
            } else {
                player.setItemOnCursor(new ItemStack(Material.AIR));
            }

            if (hasPlaced) {
                ItemStack old = placedItems.get(slot);
                player.getInventory().addItem(old);
            }

            updateItem(slot, toPlace);
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1.0f, 1.2f);
            player.sendMessage(mm.deserialize("<green>Berhasil meletakkan <gold>" + toPlace.getType().name() + "</gold> ke slot tools!</green>"));
            buildGUI();
            return;
        }

        if (hasPlaced) {
            ItemStack placed = placedItems.get(slot);
            if (click == ClickType.SHIFT_RIGHT || click == ClickType.SHIFT_LEFT) {
                placedItems.remove(slot);
                player.getInventory().addItem(placed);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
                player.sendMessage(mm.deserialize("<yellow>Item dikembalikan ke inventaris.</yellow>"));
                buildGUI();
            } else {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                new ItemModifierGUI(plugin, player, placed, slot, this).open();
            }
        }
    }

    private void handleShiftClickPlacement(ItemStack item) {
        if (isHelmet(item) && !placedItems.containsKey(SLOT_HELMET)) {
            placeFromInventory(SLOT_HELMET, item);
        } else if (isChestplate(item) && !placedItems.containsKey(SLOT_CHESTPLATE)) {
            placeFromInventory(SLOT_CHESTPLATE, item);
        } else if (isLeggings(item) && !placedItems.containsKey(SLOT_LEGGINGS)) {
            placeFromInventory(SLOT_LEGGINGS, item);
        } else if (isBoots(item) && !placedItems.containsKey(SLOT_BOOTS)) {
            placeFromInventory(SLOT_BOOTS, item);
        } else if (isToolOrWeapon(item)) {
            for (int tSlot : TOOL_SLOTS) {
                if (!placedItems.containsKey(tSlot)) {
                    placeFromInventory(tSlot, item);
                    return;
                }
            }
            player.sendMessage(mm.deserialize("<red>Semua slot tools sudah terisi penuh!</red>"));
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red>Hanya item Armor dan Senjata/Alat yang diizinkan!</red>"));
        }
    }

    private void placeFromInventory(int slot, ItemStack item) {
        ItemStack clone = item.clone();
        clone.setAmount(1);
        item.setAmount(item.getAmount() - 1);
        updateItem(slot, clone);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1.0f, 1.2f);
        buildGUI();
    }

    public void returnAllItems() {
        for (ItemStack is : placedItems.values()) {
            if (is != null && !is.getType().isAir()) {
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(is);
                if (!overflow.isEmpty()) {
                    for (ItemStack drop : overflow.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    }
                }
            }
        }
        placedItems.clear();
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
        player.sendMessage(mm.deserialize("<yellow>Seluruh item telah dikembalikan ke inventarismu.</yellow>"));
    }

    public void finishAndClaimAll() {
        if (placedItems.isEmpty()) {
            player.sendMessage(mm.deserialize("<red>Tidak ada item yang diletakkan di dalam creator!</red>"));
            return;
        }

        checkAndApplyFullsetBonus();

        int count = placedItems.size();
        for (ItemStack is : placedItems.values()) {
            if (is != null && !is.getType().isAir()) {
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(is);
                if (!overflow.isEmpty()) {
                    for (ItemStack drop : overflow.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    }
                }
            }
        }
        placedItems.clear();
        player.closeInventory();
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        player.sendMessage(mm.deserialize("<green><bold>✓ SUKSES!</bold> <yellow>" + count + " Item</yellow> berkekuatan sihir berhasil diselesaikan dan masuk ke inventarismu!</green>"));
    }

    public void handleDrag(InventoryDragEvent event) {
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < 54) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // Validation helpers
    public static boolean isHelmet(ItemStack is) {
        if (is == null) return false;
        String n = is.getType().name();
        return n.endsWith("_HELMET") || is.getType() == Material.TURTLE_HELMET;
    }

    public static boolean isChestplate(ItemStack is) {
        if (is == null) return false;
        String n = is.getType().name();
        return n.endsWith("_CHESTPLATE") || is.getType() == Material.ELYTRA;
    }

    public static boolean isLeggings(ItemStack is) {
        if (is == null) return false;
        return is.getType().name().endsWith("_LEGGINGS");
    }

    public static boolean isBoots(ItemStack is) {
        if (is == null) return false;
        return is.getType().name().endsWith("_BOOTS");
    }

    public static boolean isArmor(ItemStack is) {
        return isHelmet(is) || isChestplate(is) || isLeggings(is) || isBoots(is);
    }

    public static boolean isToolOrWeapon(ItemStack is) {
        if (is == null) return false;
        String n = is.getType().name();
        return n.endsWith("_SWORD") || n.endsWith("_AXE") || n.endsWith("_PICKAXE") || n.endsWith("_SHOVEL") || n.endsWith("_HOE")
                || is.getType() == Material.BOW || is.getType() == Material.CROSSBOW || is.getType() == Material.TRIDENT
                || is.getType() == Material.MACE || is.getType() == Material.FISHING_ROD || is.getType() == Material.SHEARS;
    }

    private boolean isValidArmorForSlot(int slot, ItemStack is) {
        return switch (slot) {
            case SLOT_HELMET -> isHelmet(is);
            case SLOT_CHESTPLATE -> isChestplate(is);
            case SLOT_LEGGINGS -> isLeggings(is);
            case SLOT_BOOTS -> isBoots(is);
            default -> false;
        };
    }

    private String getExpectedArmorName(int slot) {
        return switch (slot) {
            case SLOT_HELMET -> "Helmet";
            case SLOT_CHESTPLATE -> "Chestplate/Elytra";
            case SLOT_LEGGINGS -> "Leggings";
            case SLOT_BOOTS -> "Boots";
            default -> "Armor";
        };
    }

    private ItemStack createItem(Material mat, String name, List<Component> lore, boolean glow) {
        ItemStack is = new ItemStack(mat);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            if (name != null) meta.displayName(mm.deserialize(name));
            if (lore != null) meta.lore(lore);
            if (glow) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            is.setItemMeta(meta);
        }
        return is;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setGlobalSetBonus(String name, KitStatType stat, int pieces, double value) {
        this.setBonusActive = true;
        this.globalSetName = name;
        this.globalStatType = stat;
        this.globalRequiredPieces = pieces;
        this.globalBonusValue = value;
        checkAndApplyFullsetBonus();
        buildGUI();
    }
}
