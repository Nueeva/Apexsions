package com.apexsions.customenchants.gui;

import com.apexsions.core.kit.KitStatType;
import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.gui.dialog.ItemEditDialogFlow;
import com.apexsions.customenchants.items.ColorUtil;
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

    // Active Creator Sessions
    private static final Map<UUID, AdminItemCreatorGUI> activeCreators = new java.util.concurrent.ConcurrentHashMap<>();

    public static AdminItemCreatorGUI getActiveCreator(UUID uuid) {
        return activeCreators.get(uuid);
    }

    public static void registerActiveCreator(UUID uuid, AdminItemCreatorGUI gui) {
        activeCreators.put(uuid, gui);
    }

    public static void unregisterActiveCreator(UUID uuid) {
        activeCreators.remove(uuid);
    }

    public Map<Integer, ItemStack> getPlacedItems() {
        return placedItems;
    }

    public boolean isSetBonusConfigured() {
        return setBonusConfigured && (!globalSet2Stats.isEmpty() || !globalSet4Stats.isEmpty());
    }


    // State flags
    private boolean saveToPreset = false;
    private boolean isNavigatingSubGUI = false;

    public AdminItemCreatorGUI(ApexsionsCustomEnchantsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gold><bold>🛠 APEXSIONS ITEM & FULLSET CREATOR 🛠</bold></gold>"));
        registerActiveCreator(player.getUniqueId(), this);
        // Defaults are empty until configured by admin
        buildGUI();
    }

    public void open() {
        registerActiveCreator(player.getUniqueId(), this);
        buildGUI();
        player.openInventory(inventory);
        Bukkit.getScheduler().runTask(plugin, () -> {
            this.isNavigatingSubGUI = false;
        });
    }

    public void setNavigatingSubGUI(boolean navigating) {
        this.isNavigatingSubGUI = navigating;
    }

    public boolean isNavigatingSubGUI() {
        return this.isNavigatingSubGUI;
    }

    public String getGlobalSetId() {
        return globalSetId;
    }

    public void setGlobalSetId(String globalSetId) {
        this.globalSetId = globalSetId;
    }

    public String getGlobalSetName() {
        return globalSetName;
    }

    public void setGlobalSetName(String globalSetName) {
        this.globalSetName = globalSetName;
    }

    public Map<KitStatType, Double> getGlobalSet2Stats() {
        return globalSet2Stats;
    }

    public Map<KitStatType, Double> getGlobalSet4Stats() {
        return globalSet4Stats;
    }

    public void setSetBonusConfigured(boolean configured) {
        this.setBonusConfigured = configured;
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
            statusLore.add(mm.deserialize("<gray>Nama Set: </gray>").append(ColorUtil.parse(globalSetName)));
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
                statusLore.add(mm.deserialize("<gray>Stat yang sama pada 4-set mengutamakan nilai 4-set.</gray>"));
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
                ? "<gold><bold>[✓ SET BONUS DIATUR]</bold></gold>"
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

        // Slot 48: Rename Set / Prefix via GUI
        List<Component> renameSetLore = new ArrayList<>();
        renameSetLore.add(mm.deserialize("<gray>Nama Set Saat Ini: </gray>").append(!globalSetName.isBlank() ? ColorUtil.parse(globalSetName) : mm.deserialize("<dark_gray>(Belum Diatur)</dark_gray>")));
        renameSetLore.add(mm.deserialize("<gray>Set ID: <yellow>" + (!globalSetId.isBlank() ? globalSetId : "(Belum Diatur)") + "</yellow></gray>"));
        renameSetLore.add(Component.empty());
        renameSetLore.add(mm.deserialize("<yellow>▶ Klik untuk masukkan nama set via GUI!</yellow>"));
        renameSetLore.add(mm.deserialize("<dark_gray>Otomatis me-rename seluruh armor & tools di slot</dark_gray>"));
        renameSetLore.add(mm.deserialize("<dark_gray>mengikuti pola: [Nama Set] [Tipe Item]</dark_gray>"));
        inventory.setItem(48, createItem(Material.NAME_TAG, "<gradient:#f1c40f:#e67e22><bold>🏷 UBAH NAMA SET VIA GUI</bold></gradient>", renameSetLore, false));

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

    public String getEffectiveSetName() {
        if (globalSetName != null && !globalSetName.isBlank()) {
            return globalSetName;
        }
        List<Integer> allSlots = new ArrayList<>(List.of(SLOT_HELMET, SLOT_CHESTPLATE, SLOT_LEGGINGS, SLOT_BOOTS));
        for (int ts : TOOL_SLOTS) allSlots.add(ts);

        for (int slot : allSlots) {
            ItemStack piece = placedItems.get(slot);
            if (piece != null && piece.hasItemMeta()) {
                ItemMeta meta = piece.getItemMeta();
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                NamespacedKey kName = new NamespacedKey("apexsions", "set_name");
                if (pdc.has(kName, PersistentDataType.STRING)) {
                    String n = pdc.get(kName, PersistentDataType.STRING);
                    if (n != null && !n.isBlank()) return n;
                }
                if (meta.hasDisplayName()) {
                    String plain = PlainTextComponentSerializer.plainText().serialize(meta.displayName()).trim();
                    for (String suffix : new String[]{" Helmet", " Chestplate", " Elytra", " Leggings", " Boots",
                            " Sword", " Axe", " Pickaxe", " Shovel", " Hoe", " Bow", " Crossbow", " Trident", " Mace", " Fishing Rod", " Shears", " Shield"}) {
                        if (plain.endsWith(suffix)) {
                            plain = plain.substring(0, plain.length() - suffix.length()).trim();
                            break;
                        }
                    }
                    if (!plain.isBlank()) return plain;
                }
            }
        }
        return "Apexsions";
    }

    public boolean isFullsetComplete() {
        return placedItems.containsKey(SLOT_HELMET) &&
                placedItems.containsKey(SLOT_CHESTPLATE) &&
                placedItems.containsKey(SLOT_LEGGINGS) &&
                placedItems.containsKey(SLOT_BOOTS);
    }

    public void checkAndApplyFullsetBonus() {
        if (!setBonusConfigured || globalSetId.isBlank() || (globalSet2Stats.isEmpty() && globalSet4Stats.isEmpty())) return;

        // Apply armor set bonus to placed armor pieces
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

    public static void cleanSetBonusLore(ItemMeta meta) {
        if (meta == null || !meta.hasLore() || meta.lore() == null) return;
        List<Component> lore = new ArrayList<>(meta.lore());
        List<Component> cleaned = new ArrayList<>();

        for (Component c : lore) {
            String plain = PlainTextComponentSerializer.plainText().serialize(c).trim().toUpperCase();
            if (plain.contains("SET BONUS") ||
                plain.contains("SYARAT") ||
                plain.contains("EFEK") ||
                plain.contains("PIECES") ||
                plain.contains("HALF SET") ||
                plain.contains("FULL SET")) {
                continue;
            }
            cleaned.add(c);
        }

        // Remove trailing empty lines that were previously inserted for spacing
        while (!cleaned.isEmpty()) {
            Component last = cleaned.get(cleaned.size() - 1);
            String plain = PlainTextComponentSerializer.plainText().serialize(last).trim();
            if (plain.isEmpty()) {
                cleaned.remove(cleaned.size() - 1);
            } else {
                break;
            }
        }
        meta.lore(cleaned);
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

            cleanSetBonusLore(meta);
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

        // Clean existing set bonus lore thoroughly using plain text matching
        cleanSetBonusLore(meta);

        // Update lore with clean non-gradient colors
        if (!globalSet2Stats.isEmpty() || !globalSet4Stats.isEmpty()) {
            List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Component.empty());
            Component setComp = (!globalSetName.isBlank()) ? ColorUtil.parse(globalSetName) : mm.deserialize("<yellow>APEXSIONS</yellow>");
            lore.add(mm.deserialize("<gold><bold>★ SET BONUS: </bold></gold>").append(setComp).append(mm.deserialize("<gold><bold> ★</bold></gold>")));
            if (!globalSet2Stats.isEmpty()) {
                lore.add(mm.deserialize("<gray>Syarat: <yellow>2 Pieces (Half Set)</yellow></gray>"));
                for (Map.Entry<KitStatType, Double> e : globalSet2Stats.entrySet()) {
                    lore.add(mm.deserialize("<gray>  ● Efek: <aqua>" + e.getKey().getDisplayName() + " " + e.getKey().formatValue(e.getValue()) + "</aqua></gray>"));
                }
            }
            if (!globalSet4Stats.isEmpty()) {
                lore.add(mm.deserialize("<gray>Syarat: <yellow>4 Pieces (Full Set)</yellow></gray>"));
                for (Map.Entry<KitStatType, Double> e : globalSet4Stats.entrySet()) {
                    lore.add(mm.deserialize("<gray>  ● Efek: <aqua>" + e.getKey().getDisplayName() + " " + e.getKey().formatValue(e.getValue()) + "</aqua></gray>"));
                }
            }
            meta.lore(lore);
        }
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
        player.sendMessage(mm.deserialize("<green><bold>✓ SUKSES!</bold> Seluruh armor dan tools diubah namanya menjadi </green>")
                .append(ColorUtil.parse(newBaseName))
                .append(mm.deserialize("<green> [Tipe]!</green>")));
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
        return ColorUtil.formatPieceName(baseName, suffix);
    }

    public static String getPlainTextSafe(String text) {
        return ColorUtil.toPlainText(text);
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

            // Slot 15: Set Bonus Settings (Native Dialog GUI)
            if (rawSlot == SLOT_SET_STATUS) {
                event.setCancelled(true);
                this.isNavigatingSubGUI = true;
                if (this.globalSetName.isBlank()) {
                    this.globalSetName = getEffectiveSetName();
                    this.globalSetId = getPlainTextSafe(this.globalSetName).toLowerCase().replaceAll("[^a-z0-9_-]", "_");
                    if (this.globalSetId.isBlank()) this.globalSetId = "apexsions";
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                player.closeInventory();
                ItemEditDialogFlow.openGlobalArmorSetBonus(plugin, player, this);
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

    public static boolean isPlainItem(ItemStack is) {
        if (is == null || !is.hasItemMeta()) return true;
        ItemMeta meta = is.getItemMeta();
        if (meta == null) return true;
        if (meta.hasDisplayName()) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return !pdc.has(new NamespacedKey("apexsions", "set_name"), PersistentDataType.STRING)
                && !pdc.has(new NamespacedKey("apexsions", "set_id"), PersistentDataType.STRING)
                && !pdc.has(new NamespacedKey("apexsions", "tool_bonus"), PersistentDataType.STRING)
                && !pdc.has(new NamespacedKey("apexsions", "set2_stats"), PersistentDataType.STRING)
                && !pdc.has(new NamespacedKey("apexsions", "set4_stats"), PersistentDataType.STRING);
    }

    public static void parseStatString(String raw, Map<KitStatType, Double> target) {
        if (raw == null || raw.isBlank()) return;
        for (String p : raw.split(";")) {
            String[] kv = p.split(":");
            if (kv.length == 2) {
                try {
                    KitStatType st = KitStatType.valueOf(kv[0].trim());
                    double val = Double.parseDouble(kv[1].trim());
                    target.put(st, val);
                } catch (Exception ignored) {}
            }
        }
    }

    private void autoReadFromItem(ItemStack item, int slot) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        boolean detectedAny = false;

        // 1. Auto-read Set Name from PDC or item display name
        NamespacedKey kName = new NamespacedKey("apexsions", "set_name");
        String detectedName = null;
        if (pdc.has(kName, PersistentDataType.STRING)) {
            detectedName = pdc.get(kName, PersistentDataType.STRING);
        }
        if ((detectedName == null || detectedName.isBlank()) && meta.hasDisplayName()) {
            String plain = PlainTextComponentSerializer.plainText().serialize(meta.displayName()).trim();
            for (String suffix : new String[]{" Helmet", " Chestplate", " Elytra", " Leggings", " Boots",
                    " Sword", " Axe", " Pickaxe", " Shovel", " Hoe", " Bow", " Crossbow", " Trident", " Mace", " Fishing Rod", " Shears", " Shield"}) {
                if (plain.endsWith(suffix)) {
                    plain = plain.substring(0, plain.length() - suffix.length()).trim();
                    if (!plain.isBlank()) {
                        detectedName = plain;
                        break;
                    }
                }
            }
        }

        if (detectedName != null && !detectedName.isBlank()) {
            if (this.globalSetName.isBlank()) {
                this.globalSetName = detectedName;
                detectedAny = true;
            }
        }

        // 2. Auto-read Set ID
        NamespacedKey kId = new NamespacedKey("apexsions", "set_id");
        String detectedId = null;
        if (pdc.has(kId, PersistentDataType.STRING)) {
            detectedId = pdc.get(kId, PersistentDataType.STRING);
        }
        if (detectedId == null || detectedId.isBlank()) {
            NamespacedKey kTool = new NamespacedKey("apexsions", "tool_bonus");
            if (pdc.has(kTool, PersistentDataType.STRING)) {
                detectedId = pdc.get(kTool, PersistentDataType.STRING);
            }
        }
        if (detectedId != null && !detectedId.isBlank()) {
            if (this.globalSetId.isBlank()) {
                this.globalSetId = detectedId;
                detectedAny = true;
            }
        } else if (!this.globalSetName.isBlank() && this.globalSetId.isBlank()) {
            this.globalSetId = ColorUtil.toPlainText(this.globalSetName).toLowerCase().replaceAll("[^a-z0-9_-]", "_");
        }

        // 3. Auto-read 2-Piece Stats
        NamespacedKey k2 = new NamespacedKey("apexsions", "set2_stats");
        if (pdc.has(k2, PersistentDataType.STRING)) {
            String raw2 = pdc.get(k2, PersistentDataType.STRING);
            if (raw2 != null && !raw2.isBlank()) {
                if (this.globalSet2Stats.isEmpty()) {
                    parseStatString(raw2, this.globalSet2Stats);
                    if (!this.globalSet2Stats.isEmpty()) {
                        detectedAny = true;
                    }
                }
            }
        }

        // 4. Auto-read 4-Piece Stats
        NamespacedKey k4 = new NamespacedKey("apexsions", "set4_stats");
        if (pdc.has(k4, PersistentDataType.STRING)) {
            String raw4 = pdc.get(k4, PersistentDataType.STRING);
            if (raw4 != null && !raw4.isBlank()) {
                if (this.globalSet4Stats.isEmpty()) {
                    parseStatString(raw4, this.globalSet4Stats);
                    if (!this.globalSet4Stats.isEmpty()) {
                        detectedAny = true;
                    }
                }
            }
        }

        // 5. Legacy set_stats fallback
        if (this.globalSet2Stats.isEmpty() && this.globalSet4Stats.isEmpty()) {
            NamespacedKey kStats = new NamespacedKey("apexsions", "set_stats");
            if (pdc.has(kStats, PersistentDataType.STRING)) {
                String rawLegacy = pdc.get(kStats, PersistentDataType.STRING);
                int req = pdc.getOrDefault(new NamespacedKey("apexsions", "set_req"), PersistentDataType.INTEGER, 4);
                if (req == 2) {
                    parseStatString(rawLegacy, this.globalSet2Stats);
                } else {
                    parseStatString(rawLegacy, this.globalSet4Stats);
                }
                if (!this.globalSet2Stats.isEmpty() || !this.globalSet4Stats.isEmpty()) {
                    detectedAny = true;
                }
            }
        }

        if (!this.globalSet2Stats.isEmpty() || !this.globalSet4Stats.isEmpty()) {
            this.setBonusConfigured = true;
        }

        if (detectedAny && !this.globalSetName.isBlank()) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.4f);
            player.sendMessage(mm.deserialize("<green>✓ Terdeteksi Set: </green>")
                    .append(ColorUtil.parse(this.globalSetName))
                    .append(mm.deserialize("<green>! Properti dan set bonus otomatis dibaca.</green>")));
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

            // 1. Auto-read existing set data from item FIRST
            autoReadFromItem(toPlace, slot);

            // 2. Only rename if item is a plain unconfigured item
            if (!globalSetName.isBlank() && isPlainItem(toPlace)) {
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
                player.closeInventory();
                ItemEditDialogFlow.openRoot(plugin, player, placed, slot, this);
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

            // 1. Auto-read existing set data from tool FIRST
            autoReadFromItem(toPlace, slot);

            // 2. Only rename if item is plain
            if (!globalSetName.isBlank() && isPlainItem(toPlace)) {
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
                player.closeInventory();
                ItemEditDialogFlow.openRoot(plugin, player, placed, slot, this);
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

        // 1. Auto-read existing set data FIRST
        autoReadFromItem(clone, slot);

        // 2. Only rename if item is plain
        if (!globalSetName.isBlank() && isPlainItem(clone)) {
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
        unregisterActiveCreator(player.getUniqueId());
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
    }

    public void handleClose(InventoryCloseEvent event) {
        if (isNavigatingSubGUI) return;

        // Check on next tick if player truly closed the inventory or just transitioned to another GUI
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                Inventory top = player.getOpenInventory().getTopInventory();
                if (top.getHolder() instanceof AdminItemCreatorGUI ||
                    top.getHolder() instanceof ArmorSetBonusPickerGUI ||
                    top.getHolder() instanceof ArmorSetBonusTierGUI ||
                    top.getHolder() instanceof StatValuePickerGUI ||
                    top.getHolder() instanceof ToolBonusPickerGUI ||
                    top.getHolder() instanceof ToolStatValuePickerGUI ||
                    top.getHolder() instanceof ItemModifierGUI ||
                    top.getHolder() instanceof CustomEnchantPickerGUI ||
                    top.getHolder() instanceof VanillaEnchantPickerGUI ||
                    top.getHolder() instanceof EnchantLevelPickerGUI ||
                    top.getHolder() instanceof VanillaLevelPickerGUI ||
                    top.getHolder() instanceof AdminPresetsGUI) {
                    return; // Still navigating our plugin's GUIs, do NOT return items!
                }
            }
            if (!isNavigatingSubGUI && !placedItems.isEmpty()) {
                returnAllItems();
                player.sendMessage(mm.deserialize("<yellow>Creator ditutup. Seluruh item telah dikembalikan secara aman ke inventarismu.</yellow>"));
            }
        });
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
        unregisterActiveCreator(player.getUniqueId());
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
