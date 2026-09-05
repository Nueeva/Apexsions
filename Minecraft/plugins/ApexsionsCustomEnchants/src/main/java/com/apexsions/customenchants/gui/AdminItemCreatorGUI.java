package com.apexsions.customenchants.gui;

import com.apexsions.core.kit.KitStatType;
import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
 * - Auto-detection of Fullset Armor and automatic Set Bonus & Tool Set Bonus propagation.
 * - 7 dedicated Bottom-Center slots for Weapons and Tools.
 * - Auto-return items on close (anti-loss).
 * - Auto-naming entire set via chat.
 * - Presets integration (toggle save to preset, open preset GUI).
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

    // Global Set Bonus State for Fullset Armor & Tools
    private boolean setBonusConfigured = false;
    private String globalSetId = "";
    private String globalSetName = "";
    private final Map<KitStatType, Double> globalSet2Stats = new LinkedHashMap<>();
    private final Map<KitStatType, Double> globalSet4Stats = new LinkedHashMap<>();

    // State flags
    private boolean saveToPreset = false;
    private boolean isNavigatingSubGUI = false;

    public AdminItemCreatorGUI(ApexsionsCustomEnchantsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>🛠 APEXSIONS ITEM & FULLSET CREATOR 🛠</bold></gradient>"));
        // Defaults are empty until configured by admin
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

    public String getGlobalSetId() {
        return globalSetId;
    }

    public String getGlobalSetName() {
        return globalSetName;
    }

    public void updateItem(int slot, ItemStack newItem) {
        if (newItem == null || newItem.getType().isAir()) {
            placedItems.remove(slot);
        } else {
            ItemStack processed = plugin.getEnchantmentRegistry().updateLoreAndGlint(newItem);
            placedItems.put(slot, processed);
        }
        // Only update fullset bonus if explicitly configured
        if (setBonusConfigured && !globalSetId.isBlank() && (!globalSet2Stats.isEmpty() || !globalSet4Stats.isEmpty())) {
            checkAndApplyFullsetBonus();
        }
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
                        mm.deserialize("<gray>Seret armor atau senjata/alat ke slot kosong di bawah.</gray>"),
                        mm.deserialize("<gray>Sistem otomatis memvalidasi jenis item yang dimasukkan.</gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>● Klik item di slot untuk membuka GUI Edit Enchants & Stats!</yellow>"),
                        mm.deserialize("<yellow>● Shift + Klik Kanan item untuk mengambilnya kembali ke tas.</yellow>")
                ), true));

        // 2. Armor Section (Top Center: Slots 10, 11, 12, 13)
        renderArmorSlot(SLOT_HELMET, Material.IRON_HELMET, "🪖 HELMET KOSONG", "Letakkan Helmet di slot ini");
        renderArmorSlot(SLOT_CHESTPLATE, Material.IRON_CHESTPLATE, "🛡 CHESTPLATE KOSONG", "Letakkan Chestplate/Elytra di slot ini");
        renderArmorSlot(SLOT_LEGGINGS, Material.IRON_LEGGINGS, "👖 LEGGINGS KOSONG", "Letakkan Leggings di slot ini");
        renderArmorSlot(SLOT_BOOTS, Material.IRON_BOOTS, "👢 BOOTS KOSONG", "Letakkan Boots di slot ini");

        // 3. Set Bonus Status Slot (Slot 15)
        boolean fullset = isFullsetComplete();
        List<Component> statusLore = new ArrayList<>();
        if (!globalSetName.isBlank()) {
            statusLore.add(mm.deserialize("<gray>Nama Set: <gold>" + globalSetName + "</gold></gray>"));
        } else {
            statusLore.add(mm.deserialize("<gray>Nama Set: <dark_gray>(Belum Diatur)</dark_gray></gray>"));
        }
        if (!globalSetId.isBlank()) {
            statusLore.add(mm.deserialize("<gray>Set ID: <yellow>" + globalSetId + "</yellow></gray>"));
        } else {
            statusLore.add(mm.deserialize("<gray>Set ID: <dark_gray>(Belum Diatur)</dark_gray></gray>"));
        }
        statusLore.add(Component.empty());
        statusLore.add(mm.deserialize("<gray>Stat Bonus Aktif:</gray>"));
        if (globalSet2Stats.isEmpty() && globalSet4Stats.isEmpty()) {
            statusLore.add(mm.deserialize("<dark_gray>● Belum ada stat bonus yang dipilih</dark_gray>"));
        } else {
            if (!globalSet2Stats.isEmpty()) {
                statusLore.add(mm.deserialize("<green>● 2-Piece Bonus (" + globalSet2Stats.size() + " Efek):</green>"));
                for (Map.Entry<KitStatType, Double> e : globalSet2Stats.entrySet()) {
                    statusLore.add(mm.deserialize("<aqua>  - " + e.getKey().getDisplayName() + ": <gold>" + e.getKey().formatValue(e.getValue()) + "</gold></aqua>"));
                }
            }
            if (!globalSet4Stats.isEmpty()) {
                statusLore.add(mm.deserialize("<green>● 4-Piece Bonus (" + globalSet4Stats.size() + " Efek):</green>"));
                for (Map.Entry<KitStatType, Double> e : globalSet4Stats.entrySet()) {
                    statusLore.add(mm.deserialize("<aqua>  - " + e.getKey().getDisplayName() + ": <gold>" + e.getKey().formatValue(e.getValue()) + "</gold></aqua>"));
                }
            }
        }
        statusLore.add(Component.empty());
        if (fullset) {
            statusLore.add(mm.deserialize("<green><bold>✓ FULLSET ARMOR LENGKAP TERPASANG (4/4)</bold></green>"));
            if (setBonusConfigured && (!globalSet2Stats.isEmpty() || !globalSet4Stats.isEmpty())) {
                statusLore.add(mm.deserialize("<gray>Bonus 2-set dan 4-set aktif diterapkan bersamaan.</gray>"));
            } else {
                statusLore.add(mm.deserialize("<yellow>Atur set bonus di bawah untuk mengaktifkan efek.</yellow>"));
            }
        } else {
            int count = (placedItems.containsKey(SLOT_HELMET) ? 1 : 0)
                    + (placedItems.containsKey(SLOT_CHESTPLATE) ? 1 : 0)
                    + (placedItems.containsKey(SLOT_LEGGINGS) ? 1 : 0)
                    + (placedItems.containsKey(SLOT_BOOTS) ? 1 : 0);
            statusLore.add(mm.deserialize("<yellow><bold>⚠ STATUS ARMOR: " + count + "/4 PIECES</bold></yellow>"));
            statusLore.add(mm.deserialize("<gray>Lengkapi 4 potong armor untuk fullset bonus.</gray>"));
        }
        statusLore.add(Component.empty());
        statusLore.add(mm.deserialize("<yellow>▶ Klik untuk buka GUI Pengaturan Armor Set Bonus!</yellow>"));

        Material iconMat = fullset ? Material.NETHER_STAR : Material.SHIELD;
        String iconTitle = setBonusConfigured && (!globalSet2Stats.isEmpty() || !globalSet4Stats.isEmpty())
                ? "<green><bold>[✓ SET BONUS DIATUR]</bold></green>"
                : "<yellow><bold>[SET BONUS: BELUM DIATUR]</bold></yellow>";
        ItemStack statusIcon = createItem(iconMat, iconTitle, statusLore, fullset);
        inventory.setItem(SLOT_SET_STATUS, statusIcon);

        // 4. Tools & Weapons Section (Bottom Center: Slots 28..34)
        for (int i = 0; i < TOOL_SLOTS.length; i++) {
            int slot = TOOL_SLOTS[i];
            renderToolSlot(slot, i + 1);
        }

        // 5. Controls Row (Slots 45..53)
        // Slot 45: Return All
        inventory.setItem(45, createItem(Material.RED_CONCRETE, "<red><bold>⬅ KEMBALIKAN SEMUA ITEM</bold></red>", List.of(
                mm.deserialize("<gray>Ambil kembali seluruh item di slot creator ke tasmu.</gray>")
        ), false));

        // Slot 48: Rename Set / Prefix via Chat
        String nameDisplay = globalSetName.isBlank() ? "<dark_gray>(Belum Diatur)</dark_gray>" : "<gold>" + globalSetName + "</gold>";
        String idDisplay = globalSetId.isBlank() ? "<dark_gray>(Belum Diatur)</dark_gray>" : "<yellow>" + globalSetId + "</yellow>";
        inventory.setItem(48, createItem(Material.NAME_TAG, "<gradient:#f1c40f:#e67e22><bold>🏷 UBAH NAMA SET DI CHAT</bold></gradient>", List.of(
                mm.deserialize("<gray>Nama Set Saat Ini: " + nameDisplay + "</gray>"),
                mm.deserialize("<gray>Set ID: " + idDisplay + "</gray>"),
                Component.empty(),
                mm.deserialize("<yellow>▶ Klik untuk ketik nama set di chat!</yellow>"),
                mm.deserialize("<dark_gray>Otomatis me-rename seluruh armor & tools di slot</dark_gray>"),
                mm.deserialize("<dark_gray>mengikuti pola: [Nama Set] [Tipe Item]</dark_gray>")
        ), false));

        // Slot 49: Finish & Claim All
        inventory.setItem(49, createItem(Material.EMERALD_BLOCK, "<gradient:#2ecc71:#27ae60><bold>✔ SELESAIKAN & AMBIL SEMUA SET</bold></gradient>", List.of(
                mm.deserialize("<gray>Selesaikan proses enchant & pembuatan set,</gray>"),
                mm.deserialize("<gray>lalu masukkan seluruh item ke inventaris.</gray>"),
                Component.empty(),
                mm.deserialize(saveToPreset ? "<green>● Set ini juga akan disimpan ke PRESET.</green>" : "<gray>● Tidak disimpan ke preset.</gray>"),
                Component.empty(),
                mm.deserialize("<green>▶ Klik untuk klaim semua item!</green>")
        ), true));

        // Slot 50: Toggle Save to Preset & Preset GUI
        inventory.setItem(50, createItem(saveToPreset ? Material.ENDER_CHEST : Material.CHEST,
                "<gradient:#9b59b6:#e74c3c><bold>📦 PRESET: " + (saveToPreset ? "<green>[SIMPAN AKTIF]</green>" : "<red>[SIMPAN NON-AKTIF]</red>") + "</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Status: " + (saveToPreset ? "<green>Disimpan ke Preset saat Selesai</green>" : "<red>Tidak Disimpan ke Preset</red>") + "</gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik Kiri: Toggle Simpan ke Preset</yellow>"),
                        mm.deserialize("<aqua>▶ Klik Kanan: Buka Menu Daftar Preset</aqua>")
                ), saveToPreset));

        // Slot 53: Exit & Return All (Safety)
        inventory.setItem(53, createItem(Material.BARRIER, "<red><bold>✖ KELUAR & SIMPAN TAS</bold></red>", List.of(
                mm.deserialize("<gray>Tutup creator dan kembalikan seluruh item ke tas.</gray>")
        ), false));
    }

    private void renderArmorSlot(int slot, Material previewMat, String title, String guide) {
        if (placedItems.containsKey(slot)) {
            ItemStack is = placedItems.get(slot);
            inventory.setItem(slot, is);
        } else {
            inventory.setItem(slot, createItem(previewMat, "<gold><bold>" + title + "</bold></gold>", List.of(
                    mm.deserialize("<yellow>▶ " + guide + "</yellow>"),
                    mm.deserialize("<gray>Hanya menerima perlengkapan armor yang sesuai.</gray>")
            ), false));
        }
    }

    private void renderToolSlot(int slot, int index) {
        if (placedItems.containsKey(slot)) {
            ItemStack is = placedItems.get(slot);
            inventory.setItem(slot, is);
        } else {
            inventory.setItem(slot, createItem(Material.IRON_SWORD, "<aqua><bold>⚔ SLOT TOOL #" + index + " KOSONG</bold></aqua>", List.of(
                    mm.deserialize("<yellow>▶ Seret Pedang, Kapak, Beliung, Panah, dll ke sini.</yellow>"),
                    mm.deserialize("<gray>Hanya menerima Senjata dan Peralatan (Tools & Weapons).</gray>")
            ), false));
        }
    }

    public boolean isFullsetComplete() {
        return placedItems.containsKey(SLOT_HELMET) &&
                placedItems.containsKey(SLOT_CHESTPLATE) &&
                placedItems.containsKey(SLOT_LEGGINGS) &&
                placedItems.containsKey(SLOT_BOOTS);
    }

    public void checkAndApplyFullsetBonus() {
        if (!setBonusConfigured || globalSetId.isBlank() || (globalSet2Stats.isEmpty() && globalSet4Stats.isEmpty())) return;

        // Apply armor set bonus if fullset complete
        if (isFullsetComplete()) {
            int[] armorSlots = {SLOT_HELMET, SLOT_CHESTPLATE, SLOT_LEGGINGS, SLOT_BOOTS};
            for (int s : armorSlots) {
                ItemStack piece = placedItems.get(s);
                if (piece != null) {
                    applySetBonusToPiece(piece);
                }
            }

            // Also link Set ID to placed tools/weapons so their bonuses activate!
            for (int tSlot : TOOL_SLOTS) {
                ItemStack tool = placedItems.get(tSlot);
                if (tool != null) {
                    applyToolBonusToPiece(tool);
                }
            }
        }
    }

    public void removeFullsetBonusFromAll() {
        for (ItemStack is : placedItems.values()) {
            if (is == null) continue;
            ItemMeta meta = is.getItemMeta();
            if (meta == null) continue;
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.remove(new NamespacedKey("apexsions", "set_id"));
            pdc.remove(new NamespacedKey("apexsions", "set_name"));
            pdc.remove(new NamespacedKey("apexsions", "set_req"));
            pdc.remove(new NamespacedKey("apexsions", "set_stats"));
            pdc.remove(new NamespacedKey("apexsions", "set2_stats"));
            pdc.remove(new NamespacedKey("apexsions", "set4_stats"));
            pdc.remove(new NamespacedKey("apexsions", "set_type"));
            pdc.remove(new NamespacedKey("apexsions", "set_val"));

            List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.removeIf(c -> {
                String plain = mm.serialize(c);
                return plain.contains("SET BONUS") || plain.contains("Syarat:") || plain.contains("Efek:");
            });
            meta.lore(lore);
            is.setItemMeta(meta);
        }
    }

    private void applySetBonusToPiece(ItemStack is) {
        ItemMeta meta = is.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey("apexsions", "set_id"), PersistentDataType.STRING, globalSetId);
        pdc.set(new NamespacedKey("apexsions", "set_name"), PersistentDataType.STRING, globalSetName);

        // Serialize set2_stats
        StringBuilder sb2 = new StringBuilder();
        for (Map.Entry<KitStatType, Double> e : globalSet2Stats.entrySet()) {
            if (!sb2.isEmpty()) sb2.append(";");
            sb2.append(e.getKey().name()).append(":").append(e.getValue());
        }
        pdc.set(new NamespacedKey("apexsions", "set2_stats"), PersistentDataType.STRING, sb2.toString());

        // Serialize set4_stats
        StringBuilder sb4 = new StringBuilder();
        for (Map.Entry<KitStatType, Double> e : globalSet4Stats.entrySet()) {
            if (!sb4.isEmpty()) sb4.append(";");
            sb4.append(e.getKey().name()).append(":").append(e.getValue());
        }
        pdc.set(new NamespacedKey("apexsions", "set4_stats"), PersistentDataType.STRING, sb4.toString());

        // Backwards compatibility legacy set_stats
        Map<KitStatType, Double> legacyMap = !globalSet4Stats.isEmpty() ? globalSet4Stats : globalSet2Stats;
        StringBuilder sbLegacy = new StringBuilder();
        for (Map.Entry<KitStatType, Double> e : legacyMap.entrySet()) {
            if (!sbLegacy.isEmpty()) sbLegacy.append(";");
            sbLegacy.append(e.getKey().name()).append(":").append(e.getValue());
        }
        pdc.set(new NamespacedKey("apexsions", "set_stats"), PersistentDataType.STRING, sbLegacy.toString());
        pdc.set(new NamespacedKey("apexsions", "set_req"), PersistentDataType.INTEGER, !globalSet4Stats.isEmpty() ? 4 : 2);

        // Update lore
        List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.removeIf(c -> {
            String plain = mm.serialize(c);
            return plain.contains("SET BONUS") || plain.contains("Syarat:") || plain.contains("Efek:");
        });

        lore.add(Component.empty());
        lore.add(mm.deserialize("<gradient:#e74c3c:#f39c12><bold>★ SET BONUS: " + PlainTextComponentSerializer.plainText().serialize(mm.deserialize(globalSetName)).toUpperCase() + " ★</bold></gradient>"));
        if (!globalSet2Stats.isEmpty()) {
            lore.add(mm.deserialize("<gray>Syarat: <gold>2 Pieces (Half Set)</gold></gray>"));
            for (Map.Entry<KitStatType, Double> e : globalSet2Stats.entrySet()) {
                lore.add(mm.deserialize("<gray>  ● Efek: <aqua>" + e.getKey().getDisplayName() + " " + e.getKey().formatValue(e.getValue()) + "</aqua></gray>"));
            }
        }
        if (!globalSet4Stats.isEmpty()) {
            lore.add(mm.deserialize("<gray>Syarat: <gold>4 Pieces (Full Set)</gold></gray>"));
            for (Map.Entry<KitStatType, Double> e : globalSet4Stats.entrySet()) {
                lore.add(mm.deserialize("<gray>  ● Efek: <aqua>" + e.getKey().getDisplayName() + " " + e.getKey().formatValue(e.getValue()) + "</aqua></gray>"));
            }
        }

        meta.lore(lore);
        is.setItemMeta(meta);
    }

    private void applyToolBonusToPiece(ItemStack is) {
        ItemMeta meta = is.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey("apexsions", "set_id"), PersistentDataType.STRING, globalSetId);
        if (!globalSetName.isBlank()) {
            pdc.set(new NamespacedKey("apexsions", "set_name"), PersistentDataType.STRING, globalSetName);
        }
        is.setItemMeta(meta);
    }

    public void renameAllItems(String newBaseName) {
        if (newBaseName == null || newBaseName.isBlank()) return;
        this.globalSetName = newBaseName;
        String plain = getPlainTextSafe(newBaseName);
        this.globalSetId = plain.toLowerCase().replaceAll("[^a-z0-9_-]", "_");

        // Rename helmet
        if (placedItems.containsKey(SLOT_HELMET)) {
            renamePiece(placedItems.get(SLOT_HELMET), newBaseName, "Helmet");
        }
        // Rename chestplate / elytra
        if (placedItems.containsKey(SLOT_CHESTPLATE)) {
            ItemStack cp = placedItems.get(SLOT_CHESTPLATE);
            String label = cp.getType() == Material.ELYTRA ? "Elytra" : "Chestplate";
            renamePiece(cp, newBaseName, label);
        }
        // Rename leggings
        if (placedItems.containsKey(SLOT_LEGGINGS)) {
            renamePiece(placedItems.get(SLOT_LEGGINGS), newBaseName, "Leggings");
        }
        // Rename boots
        if (placedItems.containsKey(SLOT_BOOTS)) {
            renamePiece(placedItems.get(SLOT_BOOTS), newBaseName, "Boots");
        }

        // Rename tools
        for (int tSlot : TOOL_SLOTS) {
            ItemStack is = placedItems.get(tSlot);
            if (is != null) {
                String toolName = getToolDisplaySuffix(is);
                renamePiece(is, newBaseName, toolName);
            }
        }

        // Only update fullset bonus if bonus was configured
        if (setBonusConfigured && (!globalSet2Stats.isEmpty() || !globalSet4Stats.isEmpty())) {
            checkAndApplyFullsetBonus();
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.sendMessage(mm.deserialize("<green><bold>✓ SUKSES!</bold> Seluruh armor dan tools diubah namanya menjadi <gold>" + newBaseName + " [Tipe]</gold>!</green>"));

        // Crucial fix: Automatically reopen Item Creator GUI after rename finishes!
        this.open();
    }

    private void renamePiece(ItemStack is, String newBaseName, String suffix) {
        if (is == null) return;
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.displayName(formatPieceName(newBaseName, suffix));
            is.setItemMeta(meta);
        }
    }

    private Component formatPieceName(String baseName, String suffix) {
        if (baseName == null || baseName.isBlank()) return Component.text(suffix);
        try {
            if (baseName.contains("<") && baseName.contains(">")) {
                String clean = baseName.trim();
                int lastCloseTag = clean.lastIndexOf("</");
                if (lastCloseTag != -1) {
                    String prefix = clean.substring(0, lastCloseTag);
                    String closingTags = clean.substring(lastCloseTag);
                    return mm.deserialize(prefix + " " + suffix + closingTags);
                }
                return mm.deserialize(baseName + " " + suffix);
            } else if (baseName.contains("&")) {
                return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(baseName + " " + suffix);
            } else {
                return mm.deserialize("<gold><bold>" + baseName + " " + suffix + "</bold></gold>");
            }
        } catch (Exception e) {
            return Component.text(baseName + " " + suffix);
        }
    }

    public static String getPlainTextSafe(String text) {
        if (text == null || text.isBlank()) return "";
        try {
            if (text.contains("&")) {
                return PlainTextComponentSerializer.plainText().serialize(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(text)).trim();
            }
            return PlainTextComponentSerializer.plainText().serialize(MiniMessage.miniMessage().deserialize(text)).trim();
        } catch (Exception e) {
            return text.replaceAll("<[^>]*>", "").replaceAll("&[0-9a-fk-orA-FK-OR]", "").trim();
        }
    }

    private String getToolDisplaySuffix(ItemStack is) {
        if (is == null) return "Tool";
        String n = is.getType().name();
        if (n.endsWith("_SWORD")) return "Sword";
        if (n.endsWith("_AXE")) return "Axe";
        if (n.endsWith("_PICKAXE")) return "Pickaxe";
        if (n.endsWith("_SHOVEL")) return "Shovel";
        if (n.endsWith("_HOE")) return "Hoe";
        if (is.getType() == Material.BOW) return "Bow";
        if (is.getType() == Material.CROSSBOW) return "Crossbow";
        if (is.getType() == Material.TRIDENT) return "Trident";
        if (is.getType() == Material.MACE) return "Mace";
        if (is.getType() == Material.FISHING_ROD) return "Fishing Rod";
        if (is.getType() == Material.SHEARS) return "Shears";
        return "Weapon";
    }

    public void handleClick(InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        ItemStack cursor = event.getCursor();

        // Creator slots (0..53)
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

            // Slot 48: Rename Set in Chat
            if (rawSlot == 48) {
                event.setCancelled(true);
                this.isNavigatingSubGUI = true;
                plugin.getItemRenameManager().startSession(
                        player,
                        "Masukkan nama dasar / prefix untuk seluruh set (contoh: <gradient:#e74c3c:#f39c12><bold>Apexsions</bold></gradient> atau &6&lApexsions):",
                        newName -> {
                            renameAllItems(newName);
                            this.open();
                        },
                        this::open
                );
                return;
            }

            // Slot 49: Finish & Claim All
            if (rawSlot == 49) {
                event.setCancelled(true);
                finishAndClaimAll();
                return;
            }

            // Slot 50: Toggle Preset / Open Preset GUI
            if (rawSlot == 50) {
                event.setCancelled(true);
                if (event.getClick() == ClickType.RIGHT) {
                    // Open Presets GUI
                    this.isNavigatingSubGUI = true;
                    new AdminPresetsGUI(plugin, player, this).open();
                } else {
                    // Toggle saveToPreset
                    this.saveToPreset = !this.saveToPreset;
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                    buildGUI();
                }
                return;
            }

            // Slot 15: Set Bonus Settings
            if (rawSlot == SLOT_SET_STATUS) {
                event.setCancelled(true);
                this.isNavigatingSubGUI = true;
                ItemStack ref = placedItems.get(SLOT_HELMET);
                if (ref == null) {
                    ref = placedItems.values().stream().findFirst().orElse(null);
                }
                new ArmorSetBonusPickerGUI(plugin, player, globalSetName, globalSet2Stats, globalSet4Stats, ref, this,
                        (savedName, s2, s4) -> {
                            this.globalSetName = savedName;
                            this.globalSetId = getPlainTextSafe(savedName).toLowerCase().replaceAll("[^a-z0-9_-]", "_");
                            this.globalSet2Stats.clear();
                            this.globalSet2Stats.putAll(s2);
                            this.globalSet4Stats.clear();
                            this.globalSet4Stats.putAll(s4);
                            this.setBonusConfigured = (!globalSet2Stats.isEmpty() || !globalSet4Stats.isEmpty());

                            if (this.setBonusConfigured) {
                                renameAllItems(savedName);
                                checkAndApplyFullsetBonus();
                            } else {
                                removeFullsetBonusFromAll();
                            }
                            this.open();
                        }).open();
                return;
            }

            // 2. Armor Slots (10..13)
            if (rawSlot >= SLOT_HELMET && rawSlot <= SLOT_BOOTS) {
                event.setCancelled(true);
                handleArmorSlotPlacementOrEdit(rawSlot, cursor, event.getClick());
                return;
            }

            // 3. Tool Slots (28..34)
            for (int tSlot : TOOL_SLOTS) {
                if (rawSlot == tSlot) {
                    event.setCancelled(true);
                    handleToolSlotPlacementOrEdit(rawSlot, cursor, event.getClick());
                    return;
                }
            }

            // Cancel any other border click
            event.setCancelled(true);
        } else {
            // Player inventory click (shift click placement)
            if (event.isShiftClick()) {
                ItemStack clicked = event.getCurrentItem();
                if (clicked != null && !clicked.getType().isAir()) {
                    event.setCancelled(true);
                    handleShiftClickPlacement(clicked);
                }
            }
        }
    }

    private void handleArmorSlotPlacementOrEdit(int slot, ItemStack cursor, ClickType click) {
        boolean hasPlaced = placedItems.containsKey(slot);

        if (cursor != null && !cursor.getType().isAir()) {
            if (!isValidArmorForSlot(slot, cursor)) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<red>Item tersebut bukan merupakan <gold>" + getExpectedArmorName(slot) + "</gold> yang valid!</red>"));
                return;
            }

            ItemStack toPlace = cursor.clone();
            toPlace.setAmount(1);
            if (!globalSetName.isBlank()) {
                String suffix = switch (slot) {
                    case SLOT_HELMET -> "Helmet";
                    case SLOT_CHESTPLATE -> toPlace.getType() == Material.ELYTRA ? "Elytra" : "Chestplate";
                    case SLOT_LEGGINGS -> "Leggings";
                    case SLOT_BOOTS -> "Boots";
                    default -> "Armor";
                };
                renamePiece(toPlace, globalSetName, suffix);
            }

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
            player.sendMessage(mm.deserialize("<green>Berhasil meletakkan <gold>" + toPlace.getType().name() + "</gold>!</green>"));
            buildGUI();
            return;
        }

        if (hasPlaced) {
            ItemStack placed = placedItems.get(slot);
            if (click == ClickType.SHIFT_RIGHT || click == ClickType.SHIFT_LEFT) {
                placedItems.remove(slot);
                player.getInventory().addItem(placed);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<yellow>Item dikembalikan ke inventaris.</yellow>"));
                buildGUI();
            } else {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                this.isNavigatingSubGUI = true;
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
            if (!globalSetName.isBlank()) {
                renamePiece(toPlace, globalSetName, getToolDisplaySuffix(toPlace));
            }

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
            player.sendMessage(mm.deserialize("<green>Berhasil meletakkan <gold>" + toPlace.getType().name() + "</gold>!</green>"));
            buildGUI();
            return;
        }

        if (hasPlaced) {
            ItemStack placed = placedItems.get(slot);
            if (click == ClickType.SHIFT_RIGHT || click == ClickType.SHIFT_LEFT) {
                placedItems.remove(slot);
                player.getInventory().addItem(placed);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<yellow>Item dikembalikan ke inventaris.</yellow>"));
                buildGUI();
            } else {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                this.isNavigatingSubGUI = true;
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
        if (!globalSetName.isBlank()) {
            String suffix = switch (slot) {
                case SLOT_HELMET -> "Helmet";
                case SLOT_CHESTPLATE -> clone.getType() == Material.ELYTRA ? "Elytra" : "Chestplate";
                case SLOT_LEGGINGS -> "Leggings";
                case SLOT_BOOTS -> "Boots";
                default -> getToolDisplaySuffix(clone);
            };
            renamePiece(clone, globalSetName, suffix);
        }
        updateItem(slot, clone);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1.0f, 1.2f);
        buildGUI();
    }

    public void returnAllItems() {
        for (ItemStack is : placedItems.values()) {
            if (is != null && !is.getType().isAir()) {
                ItemStack processed = plugin.getEnchantmentRegistry().updateLoreAndGlint(is);
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(processed);
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
        // If not navigating into a sub-GUI, return all items safely to player!
        if (!isNavigatingSubGUI && !placedItems.isEmpty()) {
            returnAllItems();
            player.sendMessage(mm.deserialize("<yellow>Creator ditutup. Seluruh item telah dikembalikan secara aman ke inventarismu.</yellow>"));
        }
    }

    public void finishAndClaimAll() {
        if (placedItems.isEmpty()) {
            player.sendMessage(mm.deserialize("<red>Tidak ada item yang diletakkan di dalam creator!</red>"));
            return;
        }

        checkAndApplyFullsetBonus();

        // If preset toggle active, save to preset!
        if (saveToPreset) {
            List<ItemStack> armorList = new ArrayList<>();
            int[] aSlots = {SLOT_HELMET, SLOT_CHESTPLATE, SLOT_LEGGINGS, SLOT_BOOTS};
            for (int s : aSlots) {
                if (placedItems.containsKey(s)) {
                    armorList.add(plugin.getEnchantmentRegistry().updateLoreAndGlint(placedItems.get(s).clone()));
                }
            }

            List<ItemStack> toolList = new ArrayList<>();
            for (int tSlot : TOOL_SLOTS) {
                if (placedItems.containsKey(tSlot)) {
                    toolList.add(plugin.getEnchantmentRegistry().updateLoreAndGlint(placedItems.get(tSlot).clone()));
                }
            }

            String idToSave = globalSetId.isBlank() ? "preset_" + System.currentTimeMillis() : globalSetId;
            String nameToSave = globalSetName.isBlank() ? "Custom Set" : globalSetName;
            plugin.getPresetManager().savePreset(idToSave, nameToSave, armorList, toolList);
            player.sendMessage(mm.deserialize("<green>✓ Set <gold>" + nameToSave + "</gold> berhasil disimpan ke daftar Preset!</green>"));
        }

        int count = placedItems.size();
        for (ItemStack is : placedItems.values()) {
            if (is != null && !is.getType().isAir()) {
                ItemStack processed = plugin.getEnchantmentRegistry().updateLoreAndGlint(is);
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(processed);
                if (!overflow.isEmpty()) {
                    for (ItemStack drop : overflow.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    }
                }
            }
        }
        placedItems.clear();
        this.isNavigatingSubGUI = true; // Prevent handleClose from running again
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

    public static boolean isWeapon(ItemStack is) {
        if (is == null) return false;
        String n = is.getType().name();
        return n.endsWith("_SWORD") || n.endsWith("_AXE") || is.getType() == Material.BOW
                || is.getType() == Material.CROSSBOW || is.getType() == Material.TRIDENT || is.getType() == Material.MACE;
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
}
