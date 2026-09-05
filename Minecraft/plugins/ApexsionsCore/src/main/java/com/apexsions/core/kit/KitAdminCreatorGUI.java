package com.apexsions.core.kit;

import com.apexsions.core.ApexsionsCorePlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interactive Admin Drag-and-Drop GUI for creating/editing server kits.
 * Layout identical to AdminItemCreatorGUI:
 * - 4 Top-Center slots for 1 Fullset Armor (Helmet, Chestplate, Leggings, Boots).
 * - Slot 15: Fullset detection indicator.
 * - 7 Bottom-Center slots for extra items to give with the kit.
 * - Clicking stackable extra items allows changing stack quantity via chat.
 * - Auto-return placed items on close (anti-loss).
 */
public class KitAdminCreatorGUI implements InventoryHolder {

    public record AmountChatSession(Player player, KitAdminCreatorGUI gui, int slot, ItemStack item) {}
    public static final Map<UUID, AmountChatSession> activeAmountSessions = new ConcurrentHashMap<>();

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // Slot definitions (identical to Item Creator)
    public static final int SLOT_HELMET = 10;
    public static final int SLOT_CHESTPLATE = 11;
    public static final int SLOT_LEGGINGS = 12;
    public static final int SLOT_BOOTS = 13;
    public static final int SLOT_SET_STATUS = 15;

    public static final int[] EXTRA_SLOTS = {28, 29, 30, 31, 32, 33, 34};

    // Stored items in slots
    private final Map<Integer, ItemStack> placedItems = new HashMap<>();

    // Kit Metadata
    private String kitId;
    private String displayName;
    private String requiredRank = "wanderer";
    private long cooldownSeconds = 86400; // 24h default
    private Material displayIcon = Material.NETHERITE_CHESTPLATE;

    // Set Bonus Configurations
    private boolean setBonusEnabled = true;
    private KitStatType statType = KitStatType.DAMAGE_REDUCTION;
    private int requiredPieces = 4;
    private double statValue = 15.0;

    // Safety flags
    private boolean isNavigatingSubGUI = false;

    private static final List<String> RANKS = List.of(
            "wanderer", "ascendant", "archon", "sovereign", "emperor", "sions", "herald", "warden", "ancestor"
    );

    private static final List<Long> COOLDOWNS = List.of(
            3600L, 7200L, 21600L, 43200L, 86400L, 172800L, 604800L
    );

    public KitAdminCreatorGUI(ApexsionsCorePlugin plugin, Player player, Kit existingKit) {
        this.plugin = plugin;
        this.player = player;
        this.kitId = existingKit != null ? existingKit.getId() : "kit_" + (System.currentTimeMillis() % 10000);
        this.displayName = existingKit != null ? existingKit.getDisplayName() : "<gradient:#f1c40f:#e67e22><bold>📦 " + kitId.toUpperCase() + "</bold></gradient>";

        if (existingKit != null) {
            this.requiredRank = existingKit.getRequiredRank();
            this.cooldownSeconds = existingKit.getCooldownSeconds();
            this.displayIcon = existingKit.getDisplayIcon();
            if (existingKit.getSetBonus() != null) {
                this.setBonusEnabled = true;
                this.statType = existingKit.getSetBonus().getStatType();
                this.requiredPieces = existingKit.getSetBonus().getRequiredPieces();
                this.statValue = existingKit.getSetBonus().getValue();
            } else {
                this.setBonusEnabled = false;
            }

            if (existingKit.getHelmet() != null) placedItems.put(SLOT_HELMET, existingKit.getHelmet().clone());
            if (existingKit.getChestplate() != null) placedItems.put(SLOT_CHESTPLATE, existingKit.getChestplate().clone());
            if (existingKit.getLeggings() != null) placedItems.put(SLOT_LEGGINGS, existingKit.getLeggings().clone());
            if (existingKit.getBoots() != null) placedItems.put(SLOT_BOOTS, existingKit.getBoots().clone());

            List<ItemStack> extra = existingKit.getExtraItems();
            for (int i = 0; i < extra.size() && i < EXTRA_SLOTS.length; i++) {
                if (extra.get(i) != null) {
                    placedItems.put(EXTRA_SLOTS[i], extra.get(i).clone());
                }
            }
        }

        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>⚙ ADMIN KIT BUILDER: " + kitId.toUpperCase() + "</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        this.isNavigatingSubGUI = false;
        buildGUI();
        player.openInventory(inventory);
    }

    public void setNavigatingSubGUI(boolean navigating) {
        this.isNavigatingSubGUI = navigating;
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack border = createControlItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, border);
        }

        // Header slot 0: Info Guide
        inventory.setItem(0, createControlItem(Material.BOOK, "<gold><bold>📜 PANDUAN KIT BUILDER</bold></gold>", List.of(
                "<gray>Aturan Penempatan Item:</gray>",
                "<yellow>1. Slot Armor Atas:</yellow> <white>Maksimal 1 full set (Helm, Baju, Celana, Sepatu).</white>",
                "<yellow>2. Slot Bawah:</yellow> <aqua>Item ekstra untuk diberikan kepada player.</aqua>",
                "<yellow>3. Ubah Jumlah Item:</yellow> <green>Klik item ekstra stackable untuk ubah via chat!</green>"
        )));

        // Header slot 4: Kit ID & Name
        inventory.setItem(4, createControlItem(displayIcon, displayName, List.of(
                "<gray>Kit ID:</gray> <yellow>" + kitId + "</yellow>",
                "<yellow>Item ini juga berfungsi sebagai ikon display di /kits.</yellow>"
        )));

        // Header slot 8: Required Rank Selector
        inventory.setItem(8, createControlItem(Material.PLAYER_HEAD, "<gradient:#3498db:#2ecc71><bold>👑 RANK MINIMAL</bold></gradient>", List.of(
                "<gray>Pangkat yang berhak klaim:</gray>",
                "<gold><bold>" + requiredRank.toUpperCase() + "</bold></gold>",
                "",
                "<yellow>▶ Klik untuk ganti rank!</yellow>"
        )));

        // Render Armor Slots (10..13)
        renderArmorSlot(SLOT_HELMET, Material.IRON_HELMET, "🪖 HELMET KOSONG", "Letakkan Helmet di slot ini");
        renderArmorSlot(SLOT_CHESTPLATE, Material.IRON_CHESTPLATE, "🛡 CHESTPLATE KOSONG", "Letakkan Chestplate/Elytra di slot ini");
        renderArmorSlot(SLOT_LEGGINGS, Material.IRON_LEGGINGS, "👖 LEGGINGS KOSONG", "Letakkan Leggings di slot ini");
        renderArmorSlot(SLOT_BOOTS, Material.IRON_BOOTS, "👢 BOOTS KOSONG", "Letakkan Boots di slot ini");

        // Fullset Status Indicator (Slot 15)
        boolean fullset = isFullsetComplete();
        List<String> statusLore = new ArrayList<>();
        statusLore.add("<gray>Status Set Bonus:</gray> " + (setBonusEnabled ? "<green><bold>AKTIF</bold></green>" : "<red><bold>NON-AKTIF</bold></red>"));
        statusLore.add("<gray>Tipe Efek:</gray> <yellow>" + statType.getDisplayName() + "</yellow>");
        statusLore.add("<gray>Nilai Bonus:</gray> <gold>" + statType.formatValue(statValue) + "</gold>");
        statusLore.add("<gray>Syarat:</gray> <aqua>" + requiredPieces + " Pieces</aqua>");
        statusLore.add("");
        if (fullset) {
            statusLore.add("<green><bold>✓ FULLSET ARMOR TERDETEKSI</bold></green>");
        } else {
            statusLore.add("<red><bold>⚠ BELUM FULLSET (Kurang armor)</bold></red>");
        }

        ItemStack statusIcon = createControlItem(fullset ? Material.EMERALD : Material.REDSTONE,
                fullset ? "<green><bold>[✓ FULLSET TERDETEKSI]</bold></green>" : "<red><bold>[⚠ BELUM FULLSET]</bold></red>",
                statusLore);
        inventory.setItem(SLOT_SET_STATUS, statusIcon);

        // Render Extra Items (Slots 28..34)
        for (int i = 0; i < EXTRA_SLOTS.length; i++) {
            int slot = EXTRA_SLOTS[i];
            renderExtraSlot(slot, i + 1);
        }

        // Bottom Controls
        // Slot 45: Cooldown Selector
        long h = cooldownSeconds / 3600;
        inventory.setItem(45, createControlItem(Material.CLOCK, "<gradient:#f39c12:#f1c40f><bold>⏳ COOLDOWN KIT</bold></gradient>", List.of(
                "<gray>Waktu tunggu klaim:</gray>",
                "<yellow><bold>" + (h >= 24 ? (h / 24) + " Hari" : h + " Jam") + "</bold></yellow>",
                "",
                "<yellow>▶ Klik untuk ubah durasi cooldown!</yellow>"
        )));

        // Slot 47: Set Bonus Toggle / Stat
        inventory.setItem(47, createControlItem(Material.ENCHANTED_BOOK, "<gradient:#9b59b6:#e74c3c><bold>✦ SET BONUS STAT</bold></gradient>", List.of(
                "<gray>Status:</gray> " + (setBonusEnabled ? "<green>AKTIF</green>" : "<red>NON-AKTIF</red>"),
                "<gray>Tipe:</gray> <yellow>" + statType.getDisplayName() + "</yellow>",
                "<gray>Nilai:</gray> <gold>" + statType.formatValue(statValue) + "</gold>",
                "",
                "<yellow>▶ Klik Kiri: Ganti Tipe Stat</yellow>",
                "<aqua>▶ Shift + Klik Kiri: Ubah Nilai (+5%)</aqua>",
                "<red>▶ Klik Kanan: Toggle Aktif/Non-aktif</red>"
        )));

        // Slot 49: Batal / Kembalikan Semua
        inventory.setItem(49, createControlItem(Material.BARRIER, "<red><bold>✖ BATAL & KEMBALIKAN ITEM</bold></red>", List.of(
                "<gray>Tutup menu dan kembalikan seluruh item ke tasmu.</gray>"
        )));

        // Slot 53: Simpan Kit
        inventory.setItem(53, createControlItem(Material.EMERALD_BLOCK, "<gradient:#2ecc71:#27ae60><bold>✔ SIMPAN & DAFTARKAN KIT</bold></gradient>", List.of(
                "<gray>Validasi kelengkapan armor & item,</gray>",
                "<gray>lalu simpan langsung ke sistem kits.</gray>",
                "",
                "<green>▶ Klik untuk simpan kit!</green>"
        )));
    }

    private void renderArmorSlot(int slot, Material previewMat, String title, String guide) {
        if (placedItems.containsKey(slot)) {
            ItemStack is = placedItems.get(slot);
            inventory.setItem(slot, is);
        } else {
            inventory.setItem(slot, createControlItem(previewMat, "<gold><bold>" + title + "</bold></gold>", List.of(
                    "<yellow>▶ " + guide + "</yellow>",
                    "<gray>Hanya menerima 1 potong armor yang sesuai (Maks 1 Set).</gray>"
            )));
        }
    }

    private void renderExtraSlot(int slot, int index) {
        if (placedItems.containsKey(slot)) {
            ItemStack is = placedItems.get(slot);
            inventory.setItem(slot, is);
        } else {
            inventory.setItem(slot, createControlItem(Material.CHEST, "<aqua><bold>📦 ITEM EKSTRA #" + index + "</bold></aqua>", List.of(
                    "<yellow>▶ Seret item/alat/senjata tambahan ke sini.</yellow>",
                    "<gray>Klik item yang ditaruh untuk mengubah jumlahnya via chat!</gray>"
            )));
        }
    }

    public boolean isFullsetComplete() {
        return placedItems.containsKey(SLOT_HELMET) &&
                placedItems.containsKey(SLOT_CHESTPLATE) &&
                placedItems.containsKey(SLOT_LEGGINGS) &&
                placedItems.containsKey(SLOT_BOOTS);
    }

    public void handleClick(InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        ItemStack cursor = event.getCursor();

        if (rawSlot >= 0 && rawSlot < 54) {
            // Controls
            if (rawSlot == 49) { // Cancel & return
                event.setCancelled(true);
                returnAllItems();
                this.isNavigatingSubGUI = true;
                player.closeInventory();
                return;
            }

            if (rawSlot == 53) { // Save kit
                event.setCancelled(true);
                saveKit();
                return;
            }

            if (rawSlot == 8) { // Cycle rank
                event.setCancelled(true);
                int idx = RANKS.indexOf(requiredRank.toLowerCase());
                idx = (idx + 1) % RANKS.size();
                requiredRank = RANKS.get(idx);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                buildGUI();
                return;
            }

            if (rawSlot == 45) { // Cycle cooldown
                event.setCancelled(true);
                int idx = COOLDOWNS.indexOf(cooldownSeconds);
                idx = (idx + 1) % COOLDOWNS.size();
                cooldownSeconds = COOLDOWNS.get(idx);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.1f);
                buildGUI();
                return;
            }

            if (rawSlot == 47) { // Set Bonus settings
                event.setCancelled(true);
                if (event.getClick() == ClickType.RIGHT) {
                    setBonusEnabled = !setBonusEnabled;
                } else if (event.isShiftClick()) {
                    statValue += 5.0;
                    if (statValue > 50.0) statValue = 5.0;
                } else {
                    KitStatType[] types = KitStatType.values();
                    int nextIdx = (statType.ordinal() + 1) % types.length;
                    statType = types[nextIdx];
                    statValue = statType.getDefaultValue();
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                buildGUI();
                return;
            }

            // Armor slots (10..13)
            if (rawSlot >= SLOT_HELMET && rawSlot <= SLOT_BOOTS) {
                event.setCancelled(true);
                handleArmorSlotPlacement(rawSlot, cursor, event.getClick());
                return;
            }

            // Extra item slots (28..34)
            for (int eSlot : EXTRA_SLOTS) {
                if (rawSlot == eSlot) {
                    event.setCancelled(true);
                    handleExtraSlotPlacement(rawSlot, cursor, event.getClick());
                    return;
                }
            }

            event.setCancelled(true);
        } else {
            // Shift click from player inventory
            if (event.isShiftClick()) {
                ItemStack clicked = event.getCurrentItem();
                if (clicked != null && !clicked.getType().isAir()) {
                    event.setCancelled(true);
                    handleShiftClickPlacement(clicked);
                }
            }
        }
    }

    private void handleArmorSlotPlacement(int slot, ItemStack cursor, ClickType click) {
        boolean hasPlaced = placedItems.containsKey(slot);

        if (cursor != null && !cursor.getType().isAir()) {
            if (!isValidArmorForSlot(slot, cursor)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<red>Item tersebut bukan merupakan armor yang valid untuk slot ini!</red>"));
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

            placedItems.put(slot, toPlace);
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1.0f, 1.2f);
            buildGUI();
            return;
        }

        if (hasPlaced) {
            ItemStack placed = placedItems.get(slot);
            if (click == ClickType.SHIFT_RIGHT || click == ClickType.SHIFT_LEFT) {
                placedItems.remove(slot);
                player.getInventory().addItem(placed);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                buildGUI();
            }
        }
    }

    private void handleExtraSlotPlacement(int slot, ItemStack cursor, ClickType click) {
        boolean hasPlaced = placedItems.containsKey(slot);

        if (cursor != null && !cursor.getType().isAir()) {
            if (isArmor(cursor)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<red>DILARANG menaruh armor di slot item ekstra! Armor hanya boleh di slot atas (Maks 1 Set).</red>"));
                return;
            }

            ItemStack toPlace = cursor.clone();
            player.setItemOnCursor(new ItemStack(Material.AIR));

            if (hasPlaced) {
                ItemStack old = placedItems.get(slot);
                player.getInventory().addItem(old);
            }

            placedItems.put(slot, toPlace);
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1.0f, 1.2f);
            buildGUI();
            return;
        }

        if (hasPlaced) {
            ItemStack placed = placedItems.get(slot);
            if (click == ClickType.SHIFT_RIGHT || click == ClickType.SHIFT_LEFT) {
                placedItems.remove(slot);
                player.getInventory().addItem(placed);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                buildGUI();
                return;
            }

            // Stackable check -> change amount via chat
            if (placed.getMaxStackSize() > 1) {
                this.isNavigatingSubGUI = true;
                activeAmountSessions.put(player.getUniqueId(), new AmountChatSession(player, this, slot, placed));
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                player.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
                player.sendMessage(mm.deserialize("<gold><bold>🔢 UBAH JUMLAH ITEM VIA CHAT</bold></gold>"));
                player.sendMessage(mm.deserialize("<gray>Item:</gray> <yellow>" + placed.getType().name() + "</yellow> (Maks Stack: " + placed.getMaxStackSize() + ")"));
                player.sendMessage(mm.deserialize("<yellow>Ketik angka jumlah (1 s/d " + placed.getMaxStackSize() + ") di chat:</yellow>"));
                player.sendMessage(mm.deserialize("<gray>Ketik <red>cancel</red> untuk membatalkan.</gray>"));
                player.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
            } else {
                player.sendMessage(mm.deserialize("<yellow>Item ini tidak dapat di-stack (Maks 1).</yellow>"));
            }
        }
    }

    private void handleShiftClickPlacement(ItemStack item) {
        if (isHelmet(item) && !placedItems.containsKey(SLOT_HELMET)) {
            placeFromInventory(SLOT_HELMET, item, 1);
        } else if (isChestplate(item) && !placedItems.containsKey(SLOT_CHESTPLATE)) {
            placeFromInventory(SLOT_CHESTPLATE, item, 1);
        } else if (isLeggings(item) && !placedItems.containsKey(SLOT_LEGGINGS)) {
            placeFromInventory(SLOT_LEGGINGS, item, 1);
        } else if (isBoots(item) && !placedItems.containsKey(SLOT_BOOTS)) {
            placeFromInventory(SLOT_BOOTS, item, 1);
        } else if (!isArmor(item)) {
            for (int eSlot : EXTRA_SLOTS) {
                if (!placedItems.containsKey(eSlot)) {
                    placeFromInventory(eSlot, item, item.getAmount());
                    return;
                }
            }
            player.sendMessage(mm.deserialize("<red>Semua slot item ekstra sudah penuh!</red>"));
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red>Slot armor tersebut sudah terisi! Maksimal 1 set armor saja.</red>"));
        }
    }

    private void placeFromInventory(int slot, ItemStack item, int amount) {
        ItemStack clone = item.clone();
        clone.setAmount(amount);
        item.setAmount(item.getAmount() - amount);
        placedItems.put(slot, clone);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1.0f, 1.2f);
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
    }

    public void handleClose(InventoryCloseEvent event) {
        if (!isNavigatingSubGUI && !placedItems.isEmpty()) {
            returnAllItems();
            player.sendMessage(mm.deserialize("<yellow>Kit Creator ditutup. Seluruh item telah dikembalikan secara aman ke tasmu.</yellow>"));
        }
    }

    public void saveKit() {
        ItemStack helm = placedItems.get(SLOT_HELMET);
        ItemStack chest = placedItems.get(SLOT_CHESTPLATE);
        ItemStack legs = placedItems.get(SLOT_LEGGINGS);
        ItemStack boots = placedItems.get(SLOT_BOOTS);

        List<ItemStack> extra = new ArrayList<>();
        for (int eSlot : EXTRA_SLOTS) {
            if (placedItems.containsKey(eSlot)) {
                extra.add(placedItems.get(eSlot).clone());
            }
        }

        KitArmorSetBonus bonus = setBonusEnabled ? new KitArmorSetBonus(kitId, kitId, statType, statValue, requiredPieces) : null;
        Kit kit = new Kit(kitId, displayName, requiredRank, cooldownSeconds, displayIcon);
        kit.setHelmet(helm);
        kit.setChestplate(chest);
        kit.setLeggings(legs);
        kit.setBoots(boots);
        kit.setExtraItems(extra);
        kit.setSetBonus(bonus);

        plugin.getKitManager().saveKit(kit);
        placedItems.clear();
        this.isNavigatingSubGUI = true;
        player.closeInventory();
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        player.sendMessage(mm.deserialize("<green><bold>✓ SUKSES!</bold> Kit <gold>" + kitId + "</gold> berhasil disimpan & didaftarkan ke server!</green>"));
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

    private boolean isValidArmorForSlot(int slot, ItemStack is) {
        return switch (slot) {
            case SLOT_HELMET -> isHelmet(is);
            case SLOT_CHESTPLATE -> isChestplate(is);
            case SLOT_LEGGINGS -> isLeggings(is);
            case SLOT_BOOTS -> isBoots(is);
            default -> false;
        };
    }

    private ItemStack createControlItem(Material mat, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat != null ? mat : Material.STONE);
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

    public ApexsionsCorePlugin getPlugin() {
        return plugin;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
