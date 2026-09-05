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
 * 100% GUI-Driven Armor Set Bonus Selector supporting multiple active stat bonuses per set.
 * Left-Click to select/add a stat, Right-Click to remove a stat.
 */
public class ArmorSetBonusPickerGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final ItemStack item;
    private final InventoryHolder returnGUI;
    private final Consumer<ItemStack> onUpdate;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // State
    private String setName = "Apexsions";
    private int requiredPieces = 4;
    private final Map<KitStatType, Double> activeStats = new LinkedHashMap<>();
    private KitStatType selectedEditingStat = KitStatType.DAMAGE_REDUCTION;
    private boolean enabled = true;

    private static final List<String> PRESET_NAMES = List.of("Apexsions", "Warlord", "Gladiator", "Titan", "Phantom", "Sovereign", "Shadow", "Immortal");
    private int nameIndex = 0;

    public ArmorSetBonusPickerGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, InventoryHolder returnGUI, Consumer<ItemStack> onUpdate) {
        this.plugin = plugin;
        this.player = player;
        this.item = item;
        this.returnGUI = returnGUI;
        this.onUpdate = onUpdate;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>🛡 PENGATURAN MULTI-STAT SET BONUS 🛡</bold></gradient>"));

        readExistingBonus();
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
        NamespacedKey kReq = new NamespacedKey("apexsions", "set_req");
        if (pdc.has(kReq, PersistentDataType.INTEGER)) {
            this.requiredPieces = pdc.getOrDefault(kReq, PersistentDataType.INTEGER, 4);
        }

        // Check multi-stat serialized string first
        NamespacedKey kStats = new NamespacedKey("apexsions", "set_stats");
        if (pdc.has(kStats, PersistentDataType.STRING)) {
            String raw = pdc.get(kStats, PersistentDataType.STRING);
            if (raw != null && !raw.isBlank()) {
                String[] parts = raw.split(";");
                for (String part : parts) {
                    String[] kv = part.split(":");
                    if (kv.length == 2) {
                        try {
                            KitStatType st = KitStatType.valueOf(kv[0]);
                            double val = Double.parseDouble(kv[1]);
                            activeStats.put(st, val);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }

        // Fallback to legacy single stat if empty
        if (activeStats.isEmpty()) {
            NamespacedKey kType = new NamespacedKey("apexsions", "set_type");
            if (pdc.has(kType, PersistentDataType.STRING)) {
                String typeStr = pdc.get(kType, PersistentDataType.STRING);
                try {
                    KitStatType st = KitStatType.valueOf(typeStr);
                    double val = pdc.getOrDefault(new NamespacedKey("apexsions", "set_val"), PersistentDataType.DOUBLE, 20.0);
                    activeStats.put(st, val);
                } catch (Exception ignored) {}
            }
        }

        if (activeStats.isEmpty()) {
            activeStats.put(KitStatType.DAMAGE_REDUCTION, 15.0);
        }

        this.selectedEditingStat = activeStats.keySet().iterator().next();
        this.enabled = true;
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

        // Section 1: Stat Types (Slots 10..15)
        inventory.setItem(1, createItem(Material.BOOK, "<yellow><bold>1. PILIH STAT BONUS (MULTI-STAT)</bold></yellow>", List.of(
                mm.deserialize("<gray>Klik Kiri: Pilih / Tambahkan Stat ke Set</gray>"),
                mm.deserialize("<red>Klik Kanan: Hapus Stat dari Set</red>")
        ), false));

        addStatButton(10, KitStatType.DAMAGE_REDUCTION, Material.SHIELD, "<aqua>Pengurangan Damage (Defense)</aqua>");
        addStatButton(11, KitStatType.ATTACK_DAMAGE_BOOST, Material.DIAMOND_SWORD, "<red>Peningkatan Serangan (Attack)</red>");
        addStatButton(12, KitStatType.DODGE_CHANCE, Material.FEATHER, "<green>Peluang Menghindar (Dodge)</green>");
        addStatButton(13, KitStatType.CRITICAL_DAMAGE_BOOST, Material.BLAZE_POWDER, "<gold>Critical Damage Boost</gold>");
        addStatButton(14, KitStatType.EXTRA_MAX_HEALTH, Material.GOLDEN_APPLE, "<light_purple>Tambahan Hati Maksimal (HP)</light_purple>");
        addStatButton(15, KitStatType.MOVEMENT_SPEED_BOOST, Material.SUGAR, "<yellow>Kecepatan Lari (Speed)</yellow>");

        // Section 2: Requirement Pieces (Slots 20 & 24)
        inventory.setItem(19, createItem(Material.BOOK, "<yellow><bold>2. PILIH SYARAT JUMLAH PIECES</bold></yellow>", List.of(
                mm.deserialize("<gray>Tentukan berapa buah armor yang wajib dipakai:</gray>")
        ), false));

        boolean is2 = (requiredPieces == 2);
        inventory.setItem(20, createItem(Material.CHAINMAIL_CHESTPLATE,
                "<gold><bold>2 Pieces (Half Set)</bold></gold>" + (is2 ? " <green><bold>[TERPILIH]</bold></green>" : ""),
                List.of(
                        mm.deserialize("<gray>Cukup memakai 2 bagian armor dari set ini.</gray>"),
                        Component.empty(),
                        mm.deserialize(is2 ? "<green>● Sedang aktif (2 Pieces).</green>" : "<yellow>▶ Klik untuk memilih 2 Pieces.</yellow>")
                ), is2));

        boolean is4 = (requiredPieces == 4);
        inventory.setItem(24, createItem(Material.NETHERITE_CHESTPLATE,
                "<gradient:#f1c40f:#e67e22><bold>4 Pieces (Full Set)</bold></gradient>" + (is4 ? " <green><bold>[TERPILIH]</bold></green>" : ""),
                List.of(
                        mm.deserialize("<gray>Wajib memakai 4 bagian armor lengkap (Helm, Baju, Celana, Sepatu).</gray>"),
                        Component.empty(),
                        mm.deserialize(is4 ? "<green>● Sedang aktif (4 Pieces).</green>" : "<yellow>▶ Klik untuk memilih 4 Pieces (Full Set).</yellow>")
                ), is4));

        // Section 3: Bonus Value (Slots 28..34) for selected editing stat
        double currentVal = activeStats.getOrDefault(selectedEditingStat, 15.0);
        inventory.setItem(27, createItem(Material.BOOK, "<yellow><bold>3. ATUR NILAI: " + selectedEditingStat.getDisplayName() + "</bold></yellow>", List.of(
                mm.deserialize("<gray>Nilai saat ini: <gold>" + selectedEditingStat.formatValue(currentVal) + "</gold></gray>"),
                mm.deserialize("<gray>Klik salah satu persentase di samping:</gray>")
        ), false));

        double[] values = {5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 50.0};
        for (int i = 0; i < values.length; i++) {
            double v = values[i];
            boolean isValSel = (Math.abs(currentVal - v) < 0.01);
            int slot = 28 + i;
            inventory.setItem(slot, createItem(Material.EXPERIENCE_BOTTLE,
                    "<gold><bold>" + selectedEditingStat.formatValue(v) + "</bold></gold>" + (isValSel ? " <green><bold>[TERPILIH]</bold></green>" : ""),
                    List.of(
                            mm.deserialize("<gray>Atur stat <yellow>" + selectedEditingStat.getDisplayName() + "</yellow> menjadi <gold>" + selectedEditingStat.formatValue(v) + "</gold>.</gray>"),
                            Component.empty(),
                            mm.deserialize(isValSel ? "<green>● Nilai ini sedang aktif.</green>" : "<yellow>▶ Klik untuk menerapkan nilai ini.</yellow>")
                    ), isValSel));
        }

        // Section 4: Set ID / Name
        inventory.setItem(40, createItem(Material.NAME_TAG, "<gradient:#3498db:#9b59b6><bold>🏷 NAMA / ID SET: " + setName + "</bold></gradient>", List.of(
                mm.deserialize("<gray>Set ID: <yellow>" + setName.toLowerCase() + "</yellow></gray>"),
                Component.empty(),
                mm.deserialize("<yellow>▶ Klik untuk ganti preset nama set!</yellow>")
        ), false));

        // Remove / Disable button
        inventory.setItem(41, createItem(Material.LAVA_BUCKET, "<red><bold>✖ HAPUS SELURUH SET BONUS</bold></red>", List.of(
                mm.deserialize("<gray>Menghapus seluruh efek set bonus dari item ini.</gray>"),
                Component.empty(),
                mm.deserialize("<red>▶ Klik untuk menonaktifkan & hapus bonus!</red>")
        ), false));

        // Save & Return Button (Slot 49)
        List<Component> summaryLore = new ArrayList<>();
        summaryLore.add(mm.deserialize("<gray>Set: <gold>" + setName + "</gold> (" + requiredPieces + " Pieces)</gray>"));
        summaryLore.add(Component.empty());
        summaryLore.add(mm.deserialize("<gray>Stat Bonus Aktif:</gray>"));
        for (Map.Entry<KitStatType, Double> e : activeStats.entrySet()) {
            summaryLore.add(mm.deserialize("<aqua>● " + e.getKey().getDisplayName() + ": <gold>" + e.getKey().formatValue(e.getValue()) + "</gold></aqua>"));
        }
        summaryLore.add(Component.empty());
        summaryLore.add(mm.deserialize("<green><bold>▶ Klik untuk simpan & terapkan ke item!</bold></green>"));

        inventory.setItem(49, createItem(Material.EMERALD_BLOCK, "<gradient:#2ecc71:#27ae60><bold>✔ TERAPKAN KE ITEM</bold></gradient>", summaryLore, true));

        // Back Button (Slot 45)
        inventory.setItem(45, createItem(Material.ARROW, "<gradient:#3498db:#2980b9><bold>⬅ KEMBALI</bold></gradient>", List.of(
                mm.deserialize("<gray>Kembali tanpa menyimpan.</gray>")
        ), false));
    }

    private void addStatButton(int slot, KitStatType type, Material mat, String title) {
        boolean isActive = activeStats.containsKey(type);
        boolean isEditing = (selectedEditingStat == type);

        String badge = "";
        if (isActive) {
            badge = " <green><bold>[" + type.formatValue(activeStats.get(type)) + "]</bold></green>";
        }
        if (isEditing) {
            badge += " <yellow><bold>[EDITING]</bold></yellow>";
        }

        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<gray>Stat: <gold>" + type.getDisplayName() + "</gold></gray>"));
        lore.add(Component.empty());
        if (isActive) {
            lore.add(mm.deserialize("<green>● Status: AKTIF (Nilai: " + type.formatValue(activeStats.get(type)) + ")</green>"));
        } else {
            lore.add(mm.deserialize("<dark_gray>● Status: Non-aktif</dark_gray>"));
        }
        lore.add(Component.empty());
        lore.add(mm.deserialize("<green>▶ Klik Kiri: Aktifkan / Pilih untuk atur nilai</green>"));
        lore.add(mm.deserialize("<red>▶ Klik R-Click: Hapus efek stat ini dari set</red>"));

        inventory.setItem(slot, createItem(mat, title + badge, lore, isActive));
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        ClickType click = event.getClick();

        // 1. Back
        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            if (returnGUI != null) {
                player.openInventory(returnGUI.getInventory());
            } else {
                player.closeInventory();
            }
            return;
        }

        // 2. Stat Types (10..15)
        if (slot >= 10 && slot <= 15) {
            KitStatType type = switch (slot) {
                case 10 -> KitStatType.DAMAGE_REDUCTION;
                case 11 -> KitStatType.ATTACK_DAMAGE_BOOST;
                case 12 -> KitStatType.DODGE_CHANCE;
                case 13 -> KitStatType.CRITICAL_DAMAGE_BOOST;
                case 14 -> KitStatType.EXTRA_MAX_HEALTH;
                case 15 -> KitStatType.MOVEMENT_SPEED_BOOST;
                default -> null;
            };

            if (type != null) {
                if (click.isRightClick()) {
                    // Remove stat
                    if (activeStats.containsKey(type)) {
                        activeStats.remove(type);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                        player.sendMessage(mm.deserialize("<red>Stat <gold>" + type.getDisplayName() + "</gold> dihapus dari set.</red>"));
                        if (selectedEditingStat == type) {
                            selectedEditingStat = activeStats.isEmpty() ? null : activeStats.keySet().iterator().next();
                        }
                        buildGUI();
                    }
                } else {
                    // Left click: Add/Select stat
                    if (!activeStats.containsKey(type)) {
                        activeStats.put(type, type.getDefaultValue());
                    }
                    this.selectedEditingStat = type;
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                    buildGUI();
                }
            }
            return;
        }

        // 3. Required Pieces
        if (slot == 20) {
            this.requiredPieces = 2;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            buildGUI();
            return;
        }
        if (slot == 24) {
            this.requiredPieces = 4;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            buildGUI();
            return;
        }

        // 4. Values (28..34)
        if (slot >= 28 && slot <= 34) {
            if (selectedEditingStat == null) {
                player.sendMessage(mm.deserialize("<red>Pilih salah satu stat di atas terlebih dahulu!</red>"));
                return;
            }
            double[] values = {5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 50.0};
            int idx = slot - 28;
            if (idx >= 0 && idx < values.length) {
                activeStats.put(selectedEditingStat, values[idx]);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                buildGUI();
            }
            return;
        }

        // 5. Preset Set Name Rotator (Slot 40)
        if (slot == 40) {
            nameIndex = (nameIndex + 1) % PRESET_NAMES.size();
            this.setName = PRESET_NAMES.get(nameIndex);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.1f);
            buildGUI();
            return;
        }

        // 6. Remove/Disable Bonus (Slot 41)
        if (slot == 41) {
            removeBonusFromItem();
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red>Armor set bonus berhasil dihapus dari item!</red>"));
            if (returnGUI != null) {
                player.openInventory(returnGUI.getInventory());
            } else {
                player.closeInventory();
            }
            return;
        }

        // 7. Save & Apply (Slot 49)
        if (slot == 49) {
            applyBonusToItem();
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            player.sendMessage(mm.deserialize("<green><bold>✓ BERHASIL!</bold> Multi-Stat Armor Set Bonus diterapkan ke item!</green>"));
            if (returnGUI != null) {
                player.openInventory(returnGUI.getInventory());
            } else {
                player.closeInventory();
            }
        }
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
        pdc.remove(new NamespacedKey("apexsions", "set_type"));
        pdc.remove(new NamespacedKey("apexsions", "set_val"));

        List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.removeIf(c -> {
            String plain = mm.serialize(c);
            return plain.contains("SET BONUS") || plain.contains("Syarat:") || plain.contains("Efek:");
        });
        meta.lore(lore);
        item.setItemMeta(meta);
        if (onUpdate != null) onUpdate.accept(item);
    }

    private void applyBonusToItem() {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String setId = setName.toLowerCase().replaceAll("[^a-z0-9_-]", "_");
        pdc.set(new NamespacedKey("apexsions", "set_id"), PersistentDataType.STRING, setId);
        pdc.set(new NamespacedKey("apexsions", "set_name"), PersistentDataType.STRING, setName);
        pdc.set(new NamespacedKey("apexsions", "set_req"), PersistentDataType.INTEGER, requiredPieces);

        // Serialize stats
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<KitStatType, Double> e : activeStats.entrySet()) {
            if (!sb.isEmpty()) sb.append(";");
            sb.append(e.getKey().name()).append(":").append(e.getValue());
        }
        pdc.set(new NamespacedKey("apexsions", "set_stats"), PersistentDataType.STRING, sb.toString());

        // Backwards compatibility primary stat
        if (!activeStats.isEmpty()) {
            Map.Entry<KitStatType, Double> first = activeStats.entrySet().iterator().next();
            pdc.set(new NamespacedKey("apexsions", "set_type"), PersistentDataType.STRING, first.getKey().name());
            pdc.set(new NamespacedKey("apexsions", "set_val"), PersistentDataType.DOUBLE, first.getValue());
        }

        // Clean existing lore
        List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.removeIf(c -> {
            String plain = mm.serialize(c);
            return plain.contains("SET BONUS") || plain.contains("Syarat:") || plain.contains("Efek:");
        });

        lore.add(Component.empty());
        lore.add(mm.deserialize("<gradient:#e74c3c:#f39c12><bold>★ SET BONUS: " + setName.toUpperCase() + " ★</bold></gradient>"));
        lore.add(mm.deserialize("<gray>Syarat: <gold>" + requiredPieces + " Pieces (" + (requiredPieces == 4 ? "Full Set" : "Half Set") + ")</gold></gray>"));
        for (Map.Entry<KitStatType, Double> e : activeStats.entrySet()) {
            lore.add(mm.deserialize("<gray>Efek: <aqua>" + e.getKey().getDisplayName() + " " + e.getKey().formatValue(e.getValue()) + "</aqua></gray>"));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        if (onUpdate != null) onUpdate.accept(item);
    }

    private ItemStack createItem(Material mat, String name, List<Component> lore, boolean glow) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            if (lore != null) meta.lore(lore);
            if (glow) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
