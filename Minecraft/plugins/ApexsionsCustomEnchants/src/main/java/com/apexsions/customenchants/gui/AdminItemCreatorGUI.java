package com.apexsions.customenchants.gui;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.kit.KitStatType;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 54-Slot Interactive GUI Builder for Admin Item Creation (Equipment, Custom Enchants, Vanilla Enchants, and Armor Set Bonuses).
 */
public class AdminItemCreatorGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // Creator State
    private Material selectedBase = Material.NETHERITE_SWORD;
    private final Map<CustomEnchant, Integer> selectedCustomEnchants = new LinkedHashMap<>();
    private final Map<Enchantment, Integer> selectedVanillaEnchants = new LinkedHashMap<>();

    // Custom Armor Set Bonus State (if armor)
    private boolean setBonusEnabled = false;
    private String customSetName = "Warlord";
    private KitStatType statType = KitStatType.DAMAGE_REDUCTION;
    private int requiredPieces = 4;
    private double statValue = 20.0;

    private static final List<Material> BASE_OPTIONS = List.of(
            Material.NETHERITE_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_AXE,
            Material.NETHERITE_PICKAXE, Material.BOW, Material.CROSSBOW,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE,
            Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS
    );

    public AdminItemCreatorGUI(ApexsionsCustomEnchantsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e67e22:#f1c40f><bold>🔨 ADMIN ITEM CREATOR 🔨</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Header slot 4: Current Preview Item
        ItemStack preview = buildCurrentItem();
        inventory.setItem(4, preview);

        // Slot 10: Step 1: Base Equipment Selector
        inventory.setItem(10, createItem(selectedBase, "<gradient:#3498db:#2ecc71><bold>STEP 1: PILIH BASE ITEM</bold></gradient>", List.of(
                "<gray>Base Item Terpilih:</gray> <yellow>" + selectedBase.name() + "</yellow>",
                "",
                "<yellow>▶ Klik untuk ganti jenis peralatan!</yellow>"
        )));

        // Slot 12: Step 2: Custom Enchants Selector
        List<String> cLore = new ArrayList<>();
        cLore.add("<gray>Daftar Custom Enchants Terpasang:</gray>");
        if (selectedCustomEnchants.isEmpty()) {
            cLore.add("<dark_gray>• Belum ada sihir custom</dark_gray>");
        } else {
            for (Map.Entry<CustomEnchant, Integer> entry : selectedCustomEnchants.entrySet()) {
                cLore.add("<gold>• " + entry.getKey().getDisplayName() + " " + CustomEnchant.toRoman(entry.getValue()) + "</gold>");
            }
        }
        cLore.add("");
        cLore.add("<yellow>▶ [Klik Kiri] Tambah / Siklus Sihir Custom</yellow>");
        cLore.add("<red>▶ [Klik Kanan] Reset Custom Enchants</red>");
        inventory.setItem(12, createItem(Material.ENCHANTED_BOOK, "<gradient:#9b59b6:#8e44ad><bold>STEP 2: CUSTOM ENCHANTS</bold></gradient>", cLore));

        // Slot 14: Step 3: Vanilla Enchants Selector
        List<String> vLore = new ArrayList<>();
        vLore.add("<gray>Daftar Vanilla Enchants Terpasang:</gray>");
        if (selectedVanillaEnchants.isEmpty()) {
            vLore.add("<dark_gray>• Belum ada vanilla enchant</dark_gray>");
        } else {
            for (Map.Entry<Enchantment, Integer> entry : selectedVanillaEnchants.entrySet()) {
                vLore.add("<aqua>• " + entry.getKey().getKey().getKey().toUpperCase() + " " + CustomEnchant.toRoman(entry.getValue()) + "</aqua>");
            }
        }
        vLore.add("");
        vLore.add("<yellow>▶ [Klik Kiri] Tambah Vanilla Enchants (Sharpness, Prot, dll)</yellow>");
        vLore.add("<red>▶ [Klik Kanan] Reset Vanilla Enchants</red>");
        inventory.setItem(14, createItem(Material.BOOK, "<gradient:#3498db:#9b59b6><bold>STEP 3: VANILLA ENCHANTS</bold></gradient>", vLore));

        // Slot 16: Step 4: Armor Set Bonus (If armor)
        if (isArmor(selectedBase)) {
            List<String> sLore = new ArrayList<>();
            sLore.add("<gray>Status Set Bonus:</gray> " + (setBonusEnabled ? "<green><bold>AKTIF</bold></green>" : "<red><bold>NON-AKTIF</bold></red>"));
            sLore.add("<gray>Nama Set:</gray> <gold>" + customSetName + " Set</gold>");
            sLore.add("<gray>Tipe Stat:</gray> <yellow>" + statType.getDisplayName() + "</yellow>");
            sLore.add("<gray>Nilai Efek:</gray> <yellow>" + statType.formatValue(statValue) + "</yellow>");
            sLore.add("<gray>Syarat Keping:</gray> <yellow>" + requiredPieces + " Pieces</yellow>");
            sLore.add("");
            sLore.add("<yellow>▶ [Klik Kiri] Ganti Tipe Stat</yellow>");
            sLore.add("<aqua>▶ [Shift+Kiri] Ganti Nilai / Pieces</aqua>");
            sLore.add("<red>▶ [Klik Kanan] Toggle Aktif/Nonaktif</red>");
            inventory.setItem(16, createItem(Material.SHIELD, "<gradient:#f1c40f:#e67e22><bold>STEP 4: ARMOR SET BONUS</bold></gradient>", sLore));
        } else {
            inventory.setItem(16, createItem(Material.BARRIER, "<gray><italic>Hanya Tersedia Untuk Armor</italic></gray>", List.of(
                    "<gray>Ganti base item ke helm, baju, celana,</gray>",
                    "<gray>atau sepatu untuk mengaktifkan set bonus.</gray>"
            )));
        }

        // Row 3: Preset Templates (Slots 29, 31, 33)
        inventory.setItem(29, createItem(Material.NETHERITE_SWORD, "<gold><bold>Preset: God Blade</bold></gold>", List.of(
                "<gray>Sharpness 20, Rage IV, Bleed III, Lifesteal III</gray>",
                "<yellow>▶ Klik untuk pasang preset pedang!</yellow>"
        )));

        inventory.setItem(31, createItem(Material.NETHERITE_CHESTPLATE, "<gold><bold>Preset: Warlord Armor</bold></gold>", List.of(
                "<gray>Protection 12, Overload III, Obsidianshield I</gray>",
                "<gray>Set Bonus: -25% Damage Reduction (4 Pieces)</gray>",
                "<yellow>▶ Klik untuk pasang preset armor!</yellow>"
        )));

        inventory.setItem(33, createItem(Material.NETHERITE_PICKAXE, "<gold><bold>Preset: Excavator Pickaxe</bold></gold>", List.of(
                "<gray>Efficiency 20, Fortune 12, Telepathy I, Harvest III</gray>",
                "<yellow>▶ Klik untuk pasang preset alat!</yellow>"
        )));

        // Bottom Controls
        // Slot 48: Cancel / Close
        inventory.setItem(48, createItem(Material.BARRIER, "<red><bold>✖ BATAL</bold></red>", List.of("<gray>Tutup item creator.</gray>")));

        // Slot 50: Selesai & Klaim ke Inventory
        inventory.setItem(50, createItem(Material.EMERALD_BLOCK, "<gradient:#2ecc71:#27ae60><bold>✔ SELESAI & AMBIL ITEM</bold></gradient>", List.of(
                "<gray>Bikin item ini sekarang dan langsung</gray>",
                "<gray>berikan ke inventory admin.</gray>",
                "",
                "<green>▶ Klik untuk klaim item!</green>"
        )));
    }

    private ItemStack buildCurrentItem() {
        ItemStack item = new ItemStack(selectedBase);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Apply Vanilla Enchants
        for (Map.Entry<Enchantment, Integer> entry : selectedVanillaEnchants.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        // Apply Custom Set Bonus PDC if armor
        if (isArmor(selectedBase) && setBonusEnabled) {
            ApexsionsCorePlugin core = ApexsionsCorePlugin.getInstance();
            if (core != null) {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                pdc.set(core.getKitManager().getKeySetId(), PersistentDataType.STRING, customSetName.toLowerCase());
                pdc.set(core.getKitManager().getKeySetName(), PersistentDataType.STRING, customSetName);
                pdc.set(core.getKitManager().getKeySetType(), PersistentDataType.STRING, statType.name());
                pdc.set(core.getKitManager().getKeySetVal(), PersistentDataType.DOUBLE, statValue);
                pdc.set(core.getKitManager().getKeySetReq(), PersistentDataType.INTEGER, requiredPieces);
            }
        }

        item.setItemMeta(meta);

        // Apply Custom Enchants through Registry
        for (Map.Entry<CustomEnchant, Integer> entry : selectedCustomEnchants.entrySet()) {
            item = plugin.getEnchantmentRegistry().applyEnchant(item, entry.getKey(), entry.getValue());
        }

        // Append Set Bonus Lore if enabled
        if (isArmor(selectedBase) && setBonusEnabled) {
            ItemMeta updatedMeta = item.getItemMeta();
            if (updatedMeta != null) {
                List<Component> lore = updatedMeta.hasLore() && updatedMeta.lore() != null ? new ArrayList<>(updatedMeta.lore()) : new ArrayList<>();
                lore.add(Component.empty());
                lore.add(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>✦ SET BONUS: [" + customSetName + "] ✦</bold></gradient>"));
                lore.add(mm.deserialize("<gray>Efek (" + requiredPieces + " Set): <yellow>" + statType.formatValue(statValue) + " " + statType.getDisplayName() + "</yellow></gray>"));
                updatedMeta.lore(lore);
                item.setItemMeta(updatedMeta);
            }
        }

        return item;
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 48) { // Cancel
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (slot == 50) { // Finish and Give
            ItemStack finalItem = buildCurrentItem();
            player.getInventory().addItem(finalItem);
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            player.sendMessage(mm.deserialize("<green><bold>✓ ITEM BERHASIL DIBUAT!</bold> Item custom telah dimasukkan ke inventorymu.</green>"));
            return;
        }

        if (slot == 10) { // Cycle base item
            int idx = BASE_OPTIONS.indexOf(selectedBase);
            selectedBase = BASE_OPTIONS.get((idx + 1) % BASE_OPTIONS.size());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            buildGUI();
            return;
        }

        if (slot == 12) { // Custom enchants
            if (event.isRightClick()) {
                selectedCustomEnchants.clear();
            } else {
                cycleCustomEnchantForBase();
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            buildGUI();
            return;
        }

        if (slot == 14) { // Vanilla enchants
            if (event.isRightClick()) {
                selectedVanillaEnchants.clear();
            } else {
                cycleVanillaEnchantForBase();
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            buildGUI();
            return;
        }

        if (slot == 16 && isArmor(selectedBase)) { // Armor Set Bonus
            if (event.isRightClick()) {
                setBonusEnabled = !setBonusEnabled;
            } else if (event.isShiftClick()) {
                requiredPieces = requiredPieces == 4 ? 2 : 4;
                statValue = statValue >= 30.0 ? 10.0 : statValue + 5.0;
            } else {
                KitStatType[] vals = KitStatType.values();
                statType = vals[(statType.ordinal() + 1) % vals.length];
                statValue = statType.getDefaultValue();
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            buildGUI();
            return;
        }

        // Presets
        if (slot == 29) { // God Blade
            selectedBase = Material.NETHERITE_SWORD;
            selectedCustomEnchants.clear();
            addCustomEnchant("rage", 4);
            addCustomEnchant("bleed", 3);
            addCustomEnchant("lifesteal", 3);
            selectedVanillaEnchants.clear();
            selectedVanillaEnchants.put(Enchantment.SHARPNESS, 20);
            selectedVanillaEnchants.put(Enchantment.UNBREAKING, 12);
            setBonusEnabled = false;
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.4f);
            buildGUI();
            return;
        }

        if (slot == 31) { // Warlord Armor
            selectedBase = Material.NETHERITE_CHESTPLATE;
            selectedCustomEnchants.clear();
            addCustomEnchant("overload", 3);
            addCustomEnchant("obsidianshield", 1);
            selectedVanillaEnchants.clear();
            selectedVanillaEnchants.put(Enchantment.PROTECTION, 12);
            selectedVanillaEnchants.put(Enchantment.UNBREAKING, 12);
            setBonusEnabled = true;
            customSetName = "Warlord";
            statType = KitStatType.DAMAGE_REDUCTION;
            statValue = 25.0;
            requiredPieces = 4;
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.4f);
            buildGUI();
            return;
        }

        if (slot == 33) { // Excavator Pickaxe
            selectedBase = Material.NETHERITE_PICKAXE;
            selectedCustomEnchants.clear();
            addCustomEnchant("telepathy", 1);
            addCustomEnchant("autosmelt", 3);
            addCustomEnchant("haste", 3);
            selectedVanillaEnchants.clear();
            selectedVanillaEnchants.put(Enchantment.EFFICIENCY, 20);
            selectedVanillaEnchants.put(Enchantment.FORTUNE, 12);
            setBonusEnabled = false;
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.4f);
            buildGUI();
        }
    }

    private void addCustomEnchant(String id, int level) {
        CustomEnchant ce = plugin.getEnchantmentRegistry().getEnchantment(id);
        if (ce != null) {
            selectedCustomEnchants.put(ce, level);
        }
    }

    private void cycleCustomEnchantForBase() {
        ItemStack dummy = new ItemStack(selectedBase);
        for (CustomEnchant ce : plugin.getEnchantmentRegistry().getAllEnchantments()) {
            if (ce.canApplyTo(dummy) && !selectedCustomEnchants.containsKey(ce)) {
                selectedCustomEnchants.put(ce, ce.getMaxLevel());
                return;
            }
        }
    }

    private void cycleVanillaEnchantForBase() {
        if (selectedBase.name().endsWith("_SWORD")) {
            selectedVanillaEnchants.put(Enchantment.SHARPNESS, 20);
            selectedVanillaEnchants.put(Enchantment.UNBREAKING, 12);
        } else if (isArmor(selectedBase)) {
            selectedVanillaEnchants.put(Enchantment.PROTECTION, 12);
            selectedVanillaEnchants.put(Enchantment.UNBREAKING, 12);
        } else {
            selectedVanillaEnchants.put(Enchantment.EFFICIENCY, 20);
            selectedVanillaEnchants.put(Enchantment.UNBREAKING, 12);
        }
    }

    private boolean isArmor(Material mat) {
        String n = mat.name();
        return n.endsWith("_HELMET") || n.endsWith("_CHESTPLATE") || n.endsWith("_LEGGINGS") || n.endsWith("_BOOTS");
    }

    private ItemStack createItem(Material mat, String name, List<String> loreLines) {
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

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
