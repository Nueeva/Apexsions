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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

/**
 * Main Central Hub for Armor Set Bonus Configuration.
 * Allows configuring 2-Piece Bonus (Half Set), 4-Piece Bonus (Full Set), or BOTH simultaneously.
 */
public class ArmorSetBonusPickerGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final ItemStack item;
    private final InventoryHolder returnGUI;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // State
    private String setName = "";
    private final Map<KitStatType, Double> set2Stats = new LinkedHashMap<>();
    private final Map<KitStatType, Double> set4Stats = new LinkedHashMap<>();

    private static final List<String> PRESET_NAMES = List.of("Apexsions", "Warlord", "Gladiator", "Titan", "Phantom", "Sovereign", "Shadow", "Immortal");
    private int nameIndex = 0;

    @FunctionalInterface
    public interface SetBonusSaveCallback {
        void onSave(String setName, Map<KitStatType, Double> set2Stats, Map<KitStatType, Double> set4Stats);
    }
    private final SetBonusSaveCallback onConfigSave;

    // Single item modifier constructor
    public ArmorSetBonusPickerGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item,
                                  InventoryHolder returnGUI, Consumer<ItemStack> onUpdate) {
        this(plugin, player, "", null, null, item, returnGUI, (savedName, s2, s4) -> {
            if (onUpdate != null && item != null) {
                onUpdate.accept(item);
            }
        });
    }

    // Global creator constructor
    public ArmorSetBonusPickerGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, String initialSetName,
                                  Map<KitStatType, Double> initialSet2Stats, Map<KitStatType, Double> initialSet4Stats,
                                  ItemStack item, InventoryHolder returnGUI, SetBonusSaveCallback onConfigSave) {
        this.plugin = plugin;
        this.player = player;
        this.item = item;
        this.returnGUI = returnGUI;
        this.onConfigSave = onConfigSave;
        this.setName = (initialSetName != null && !initialSetName.isBlank()) ? initialSetName : "";

        if (initialSet2Stats != null && !initialSet2Stats.isEmpty()) {
            this.set2Stats.putAll(initialSet2Stats);
        }
        if (initialSet4Stats != null && !initialSet4Stats.isEmpty()) {
            this.set4Stats.putAll(initialSet4Stats);
        }

        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>🛡 PUSAT ARMOR SET BONUS 🛡</bold></gradient>"));

        if (this.set2Stats.isEmpty() && this.set4Stats.isEmpty() && item != null) {
            readExistingBonus();
        }

        buildGUI();
    }

    private void readExistingBonus() {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        NamespacedKey kName = new NamespacedKey("apexsions", "set_name");
        if (pdc.has(kName, PersistentDataType.STRING)) {
            this.setName = pdc.get(kName, PersistentDataType.STRING);
        }

        // Read set2_stats
        NamespacedKey k2 = new NamespacedKey("apexsions", "set2_stats");
        if (pdc.has(k2, PersistentDataType.STRING)) {
            parseStatString(pdc.get(k2, PersistentDataType.STRING), set2Stats);
        }

        // Read set4_stats
        NamespacedKey k4 = new NamespacedKey("apexsions", "set4_stats");
        if (pdc.has(k4, PersistentDataType.STRING)) {
            parseStatString(pdc.get(k4, PersistentDataType.STRING), set4Stats);
        }

        // Legacy fallback
        if (set2Stats.isEmpty() && set4Stats.isEmpty()) {
            NamespacedKey kStats = new NamespacedKey("apexsions", "set_stats");
            if (pdc.has(kStats, PersistentDataType.STRING)) {
                int req = pdc.getOrDefault(new NamespacedKey("apexsions", "set_req"), PersistentDataType.INTEGER, 4);
                if (req == 2) {
                    parseStatString(pdc.get(kStats, PersistentDataType.STRING), set2Stats);
                } else {
                    parseStatString(pdc.get(kStats, PersistentDataType.STRING), set4Stats);
                }
            }
        }
    }

    private void parseStatString(String raw, Map<KitStatType, Double> target) {
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

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null, false);
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, border);
        }

        // Slot 4: Header
        String nameDisplay = (setName == null || setName.isBlank()) ? "<dark_gray>(Belum Diatur)</dark_gray>" : "<gold>" + setName + "</gold>";
        String idDisplay = (setName == null || setName.isBlank()) ? "kosong" : setName.toLowerCase().replaceAll("[^a-z0-9_-]", "_");

        inventory.setItem(4, createItem(Material.NETHER_STAR,
                "<gold><bold>🛡 PENGATURAN TIER ARMOR SET BONUS 🛡</bold></gold>",
                List.of(
                        mm.deserialize("<gray>Nama Set: " + nameDisplay + "</gray>"),
                        mm.deserialize("<gray>ID Set: <yellow>" + idDisplay + "</yellow></gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>Atur efek bonus untuk 2 Pieces, 4 Pieces, atau keduanya!</yellow>"),
                        mm.deserialize("<gray>Jika pemain memakai 4 potong armor, kedua efek aktif bersamaan.</gray>")
                ), true));

        // Slot 20: 2-Piece Bonus Option
        List<Component> lore2 = new ArrayList<>();
        lore2.add(mm.deserialize("<gray>Efek aktif saat pemain memakai <gold>minimal 2 potong armor</gold>.</gray>"));
        lore2.add(Component.empty());
        if (set2Stats.isEmpty()) {
            lore2.add(mm.deserialize("<red>● Belum ada stat yang diatur (Non-aktif).</red>"));
        } else {
            lore2.add(mm.deserialize("<green>● Stat 2-Piece Aktif (" + set2Stats.size() + " Efek):</green>"));
            for (Map.Entry<KitStatType, Double> e : set2Stats.entrySet()) {
                lore2.add(mm.deserialize("<aqua>  - " + e.getKey().getDisplayName() + ": <gold>" + e.getKey().formatValue(e.getValue()) + "</gold></aqua>"));
            }
        }
        lore2.add(Component.empty());
        lore2.add(mm.deserialize("<yellow>▶ Klik untuk buka GUI Pengaturan Bonus 2-Piece!</yellow>"));

        inventory.setItem(20, createItem(Material.CHAINMAIL_CHESTPLATE,
                "<blue><bold>🛡 PENGATURAN BONUS 2-PIECE (HALF SET)</bold></blue>",
                lore2, !set2Stats.isEmpty()));

        // Slot 24: 4-Piece Bonus Option
        List<Component> lore4 = new ArrayList<>();
        lore4.add(mm.deserialize("<gray>Efek aktif saat pemain memakai <gold>lengkap 4 potong armor</gold>.</gray>"));
        lore4.add(Component.empty());
        if (set4Stats.isEmpty()) {
            lore4.add(mm.deserialize("<red>● Belum ada stat yang diatur (Non-aktif).</red>"));
        } else {
            lore4.add(mm.deserialize("<green>● Stat 4-Piece Aktif (" + set4Stats.size() + " Efek):</green>"));
            for (Map.Entry<KitStatType, Double> e : set4Stats.entrySet()) {
                lore4.add(mm.deserialize("<aqua>  - " + e.getKey().getDisplayName() + ": <gold>" + e.getKey().formatValue(e.getValue()) + "</gold></aqua>"));
            }
        }
        lore4.add(Component.empty());
        lore4.add(mm.deserialize("<yellow>▶ Klik untuk buka GUI Pengaturan Bonus 4-Piece!</yellow>"));

        inventory.setItem(24, createItem(Material.NETHERITE_CHESTPLATE,
                "<gold><bold>👑 PENGATURAN BONUS 4-PIECE (FULL SET)</bold></gold>",
                lore4, !set4Stats.isEmpty()));

        // Slot 40: Set Name Rotator / Info
        inventory.setItem(40, createItem(Material.NAME_TAG,
                "<yellow><bold>🏷 NAMA / ID SET: " + nameDisplay + "</bold></yellow>",
                List.of(
                        mm.deserialize("<gray>ID Set: <yellow>" + idDisplay + "</yellow></gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk ganti preset nama set!</yellow>")
                ), false));

        // Slot 41: Clear all
        inventory.setItem(41, createItem(Material.LAVA_BUCKET,
                "<red><bold>✖ HAPUS SELURUH SET BONUS</bold></red>",
                List.of(
                        mm.deserialize("<gray>Menghapus seluruh efek bonus 2-set dan 4-set dari item ini.</gray>"),
                        Component.empty(),
                        mm.deserialize("<red>▶ Klik untuk mengosongkan seluruh bonus!</red>")
                ), false));

        // Slot 45: Back
        inventory.setItem(45, createItem(Material.ARROW,
                "<gray><bold>⬅ KEMBALI TANPA MENYIMPAN</bold></gray>",
                List.of(
                        mm.deserialize("<gray>Kembali ke menu sebelumnya.</gray>")
                ), false));

        // Slot 49: Apply & Return Safely
        boolean hasAny = !set2Stats.isEmpty() || !set4Stats.isEmpty();
        List<Component> applyLore = new ArrayList<>();
        applyLore.add(mm.deserialize("<gray>Set: <gold>" + (setName.isBlank() ? "Custom Set" : setName) + "</gold></gray>"));
        applyLore.add(Component.empty());
        applyLore.add(mm.deserialize(set2Stats.isEmpty() ? "<gray>● 2-Piece: Nonaktif</gray>" : "<green>● 2-Piece: " + set2Stats.size() + " Efek Aktif</green>"));
        applyLore.add(mm.deserialize(set4Stats.isEmpty() ? "<gray>● 4-Piece: Nonaktif</gray>" : "<green>● 4-Piece: " + set4Stats.size() + " Efek Aktif</green>"));
        applyLore.add(Component.empty());
        applyLore.add(mm.deserialize("<green><bold>▶ Klik untuk simpan & terapkan ke item!</bold></green>"));

        inventory.setItem(49, createItem(Material.EMERALD_BLOCK,
                "<green><bold>✔ TERAPKAN KE ITEM</bold></green>",
                applyLore, hasAny));
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        // 1. Back (Slot 45)
        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            safeReturnToParent(false);
            return;
        }

        // 2. Open 2-Piece Tier GUI (Slot 20)
        if (slot == 20) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            new ArmorSetBonusTierGUI(plugin, player, 2, set2Stats, this).open();
            return;
        }

        // 3. Open 4-Piece Tier GUI (Slot 24)
        if (slot == 24) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            new ArmorSetBonusTierGUI(plugin, player, 4, set4Stats, this).open();
            return;
        }

        // 4. Name Rotator (Slot 40)
        if (slot == 40) {
            if (setName == null || setName.isBlank()) {
                nameIndex = 0;
            } else {
                nameIndex = (nameIndex + 1) % PRESET_NAMES.size();
            }
            this.setName = PRESET_NAMES.get(nameIndex);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.1f);
            buildGUI();
            return;
        }

        // 5. Clear All (Slot 41)
        if (slot == 41) {
            set2Stats.clear();
            set4Stats.clear();
            removeBonusFromItem();
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red>Seluruh efek armor set bonus berhasil dihapus!</red>"));
            safeReturnToParent(true);
            return;
        }

        // 6. Apply & Save (Slot 49)
        if (slot == 49) {
            if (set2Stats.isEmpty() && set4Stats.isEmpty()) {
                if (onConfigSave == null) {
                    removeBonusFromItem();
                }
                player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<yellow>Tidak ada stat bonus yang diatur. Bonus dikosongkan.</yellow>"));
            } else {
                if (setName == null || setName.isBlank()) {
                    setName = PRESET_NAMES.get(0);
                }
                if (onConfigSave == null) {
                    applyBonusToItem();
                }
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
                player.sendMessage(mm.deserialize("<green><bold>✓ BERHASIL!</bold> Armor Set Bonus <gold>" + setName + "</gold> berhasil diterapkan!</green>"));
            }
            safeReturnToParent(true);
            return;
        }
    }

    private void safeReturnToParent(boolean saveChanges) {
        if (saveChanges && onConfigSave != null) {
            onConfigSave.onSave(setName, set2Stats, set4Stats);
            return; // onConfigSave will open the creator safely!
        }

        if (returnGUI instanceof AdminItemCreatorGUI creator) {
            creator.open();
        } else if (returnGUI instanceof ItemModifierGUI modifier) {
            modifier.open();
        } else if (returnGUI != null) {
            player.openInventory(returnGUI.getInventory());
        } else {
            player.closeInventory();
        }
    }

    private void applyBonusToItem() {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String setId = setName.toLowerCase().replaceAll("[^a-z0-9_-]", "_");
        pdc.set(new NamespacedKey("apexsions", "set_id"), PersistentDataType.STRING, setId);
        pdc.set(new NamespacedKey("apexsions", "set_name"), PersistentDataType.STRING, setName);

        // Serialize set2_stats
        StringBuilder sb2 = new StringBuilder();
        for (Map.Entry<KitStatType, Double> e : set2Stats.entrySet()) {
            if (!sb2.isEmpty()) sb2.append(";");
            sb2.append(e.getKey().name()).append(":").append(e.getValue());
        }
        pdc.set(new NamespacedKey("apexsions", "set2_stats"), PersistentDataType.STRING, sb2.toString());

        // Serialize set4_stats
        StringBuilder sb4 = new StringBuilder();
        for (Map.Entry<KitStatType, Double> e : set4Stats.entrySet()) {
            if (!sb4.isEmpty()) sb4.append(";");
            sb4.append(e.getKey().name()).append(":").append(e.getValue());
        }
        pdc.set(new NamespacedKey("apexsions", "set4_stats"), PersistentDataType.STRING, sb4.toString());

        // Backwards compatibility legacy set_stats
        Map<KitStatType, Double> legacyMap = !set4Stats.isEmpty() ? set4Stats : set2Stats;
        StringBuilder sbLegacy = new StringBuilder();
        for (Map.Entry<KitStatType, Double> e : legacyMap.entrySet()) {
            if (!sbLegacy.isEmpty()) sbLegacy.append(";");
            sbLegacy.append(e.getKey().name()).append(":").append(e.getValue());
        }
        pdc.set(new NamespacedKey("apexsions", "set_stats"), PersistentDataType.STRING, sbLegacy.toString());
        pdc.set(new NamespacedKey("apexsions", "set_req"), PersistentDataType.INTEGER, !set4Stats.isEmpty() ? 4 : 2);

        // Clean existing set bonus lore thoroughly using PlainText
        AdminItemCreatorGUI.cleanSetBonusLore(meta);

        // Update Lore
        List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.add(Component.empty());
        lore.add(mm.deserialize("<gold><bold>★ SET BONUS: <yellow>" + setName.toUpperCase() + "</yellow> ★</bold></gold>"));
        if (!set2Stats.isEmpty()) {
            lore.add(mm.deserialize("<gray>Syarat: <yellow>2 Pieces (Half Set)</yellow></gray>"));
            for (Map.Entry<KitStatType, Double> e : set2Stats.entrySet()) {
                lore.add(mm.deserialize("<gray>  ● Efek: <aqua>" + e.getKey().getDisplayName() + " " + e.getKey().formatValue(e.getValue()) + "</aqua></gray>"));
            }
        }
        if (!set4Stats.isEmpty()) {
            lore.add(mm.deserialize("<gray>Syarat: <yellow>4 Pieces (Full Set)</yellow></gray>"));
            for (Map.Entry<KitStatType, Double> e : set4Stats.entrySet()) {
                lore.add(mm.deserialize("<gray>  ● Efek: <aqua>" + e.getKey().getDisplayName() + " " + e.getKey().formatValue(e.getValue()) + "</aqua></gray>"));
            }
        }

        meta.lore(lore);
        item.setItemMeta(meta);
    }

    private void removeBonusFromItem() {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.remove(new NamespacedKey("apexsions", "set_id"));
        pdc.remove(new NamespacedKey("apexsions", "set_name"));
        pdc.remove(new NamespacedKey("apexsions", "set_req"));
        pdc.remove(new NamespacedKey("apexsions", "set_stats"));
        pdc.remove(new NamespacedKey("apexsions", "set2_stats"));
        pdc.remove(new NamespacedKey("apexsions", "set4_stats"));
        pdc.remove(new NamespacedKey("apexsions", "set_type"));
        pdc.remove(new NamespacedKey("apexsions", "set_val"));

        AdminItemCreatorGUI.cleanSetBonusLore(meta);
        item.setItemMeta(meta);
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
