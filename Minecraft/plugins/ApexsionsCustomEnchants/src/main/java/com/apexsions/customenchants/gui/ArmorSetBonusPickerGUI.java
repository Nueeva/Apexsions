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
 * 100% GUI-Driven Armor Set Bonus Selector.
 * Allows choosing Stat Type, Required Pieces, and Bonus Value using visual buttons without any left/right clicks.
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
    private String setName = "Warlord";
    private KitStatType selectedStat = KitStatType.DAMAGE_REDUCTION;
    private int requiredPieces = 4;
    private double bonusValue = 20.0;
    private boolean enabled = true;

    private static final List<String> PRESET_NAMES = List.of("Warlord", "Gladiator", "Titan", "Phantom", "Sovereign", "Shadow", "Immortal");
    private int nameIndex = 0;

    public ArmorSetBonusPickerGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, InventoryHolder returnGUI, Consumer<ItemStack> onUpdate) {
        this.plugin = plugin;
        this.player = player;
        this.item = item;
        this.returnGUI = returnGUI;
        this.onUpdate = onUpdate;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>🛡 PENGATURAN ARMOR SET BONUS 🛡</bold></gradient>"));

        readExistingBonus();
        buildGUI();
    }

    private void readExistingBonus() {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey kType = new NamespacedKey("apexsions", "set_type");
        if (pdc.has(kType, PersistentDataType.STRING)) {
            String typeStr = pdc.get(kType, PersistentDataType.STRING);
            try {
                this.selectedStat = KitStatType.valueOf(typeStr);
                this.enabled = true;
            } catch (Exception ignored) {}

            NamespacedKey kName = new NamespacedKey("apexsions", "set_name");
            if (pdc.has(kName, PersistentDataType.STRING)) {
                this.setName = pdc.get(kName, PersistentDataType.STRING);
            }
            NamespacedKey kReq = new NamespacedKey("apexsions", "set_req");
            if (pdc.has(kReq, PersistentDataType.INTEGER)) {
                this.requiredPieces = pdc.getOrDefault(kReq, PersistentDataType.INTEGER, 4);
            }
            NamespacedKey kVal = new NamespacedKey("apexsions", "set_val");
            if (pdc.has(kVal, PersistentDataType.DOUBLE)) {
                this.bonusValue = pdc.getOrDefault(kVal, PersistentDataType.DOUBLE, 20.0);
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

        // Section 1: Stat Type (Slots 10..15)
        inventory.setItem(1, createItem(Material.BOOK, "<yellow><bold>1. PILIH TIPE STAT BONUS</bold></yellow>", List.of(
                mm.deserialize("<gray>Klik salah satu tombol stat di bawah ini:</gray>")
        ), false));

        addStatButton(10, KitStatType.DAMAGE_REDUCTION, Material.SHIELD, "<aqua>Pengurangan Damage (Defense)</aqua>");
        addStatButton(11, KitStatType.ATTACK_DAMAGE_BOOST, Material.DIAMOND_SWORD, "<red>Peningkatan Serangan (Attack)</red>");
        addStatButton(12, KitStatType.DODGE_CHANCE, Material.FEATHER, "<green>Peluang Menghindar (Dodge)</green>");
        addStatButton(13, KitStatType.CRITICAL_DAMAGE_BOOST, Material.BLAZE_POWDER, "<gold>Critical Damage Boost</gold>");
        addStatButton(14, KitStatType.EXTRA_MAX_HEALTH, Material.GOLDEN_APPLE, "<light_purple>Tambahan Hati Maksimal (HP)</light_purple>");
        addStatButton(15, KitStatType.MOVEMENT_SPEED_BOOST, Material.SUGAR, "<yellow>Kecepatan Lari (Speed)</yellow>");

        // Section 2: Requirement Pieces (Slots 21 & 23)
        inventory.setItem(19, createItem(Material.BOOK, "<yellow><bold>2. PILIH SYARAT JUMLAH PIECES</bold></yellow>", List.of(
                mm.deserialize("<gray>Tentukan berapa buah armor yang wajib dipakai:</gray>")
        ), false));

        boolean is2 = (requiredPieces == 2);
        inventory.setItem(21, createItem(Material.IRON_CHESTPLATE,
                "<gold><bold>2 PIECES (Set Separuh)</bold></gold>" + (is2 ? " <green><bold>[TERPILIH]</bold></green>" : ""),
                List.of(
                        mm.deserialize("<gray>Bonus aktif jika pemain mengenakan minimal 2 piece armor.</gray>"),
                        Component.empty(),
                        mm.deserialize(is2 ? "<green>● Opsi ini aktif.</green>" : "<yellow>▶ Klik untuk memilih syarat 2 pieces</yellow>")
                ), is2));

        boolean is4 = (requiredPieces == 4);
        inventory.setItem(23, createItem(Material.NETHERITE_CHESTPLATE,
                "<gold><bold>4 PIECES (Fullset Armor)</bold></gold>" + (is4 ? " <green><bold>[TERPILIH]</bold></green>" : ""),
                List.of(
                        mm.deserialize("<gray>Bonus aktif jika pemain mengenakan full set 4 piece armor.</gray>"),
                        Component.empty(),
                        mm.deserialize(is4 ? "<green>● Opsi ini aktif.</green>" : "<yellow>▶ Klik untuk memilih syarat 4 pieces (Fullset)</yellow>")
                ), is4));

        // Section 3: Bonus Value (Slots 28..34)
        inventory.setItem(27, createItem(Material.BOOK, "<yellow><bold>3. PILIH BESARAN NILAI BONUS</bold></yellow>", List.of(
                mm.deserialize("<gray>Klik salah satu preset persentase/nilai di bawah ini:</gray>")
        ), false));

        double[] values = {5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 50.0};
        int[] valSlots = {28, 29, 30, 31, 32, 33, 34};
        for (int i = 0; i < values.length; i++) {
            double v = values[i];
            boolean isSel = (Math.abs(bonusValue - v) < 0.1);
            String displayVal = selectedStat == KitStatType.EXTRA_MAX_HEALTH ? ("+" + (int) (v / 5) * 2 + " HP (" + (int) v + "%)") : ("+" + (int) v + "%");
            inventory.setItem(valSlots[i], createItem(Material.EMERALD,
                    "<green><bold>" + displayVal + "</bold></green>" + (isSel ? " <yellow><bold>[TERPILIH]</bold></yellow>" : ""),
                    List.of(
                            mm.deserialize("<gray>Besaran Bonus: <gold>" + displayVal + "</gold></gray>"),
                            Component.empty(),
                            mm.deserialize(isSel ? "<green>● Nilai ini aktif.</green>" : "<yellow>▶ Klik untuk memilih nilai " + displayVal + "</yellow>")
                    ), isSel));
        }

        // Section 4: Controls (Row 5)
        // Slot 38: Set Name
        inventory.setItem(38, createItem(Material.NAME_TAG,
                "<gold><bold>Nama Set: " + setName + "</bold></gold>",
                List.of(
                        mm.deserialize("<gray>Klik untuk mengganti preset nama set armor.</gray>"),
                        mm.deserialize("<gray>Saat ini: <yellow>" + setName + "</yellow></gray>")
                ), false));

        // Slot 40: Disable / Remove Set Bonus
        inventory.setItem(40, createItem(Material.BARRIER,
                "<red><bold>✖ HAPUS / NONAKTIFKAN SET BONUS</bold></red>",
                List.of(
                        mm.deserialize("<gray>Menghapus seluruh tag set bonus dari item ini.</gray>"),
                        Component.empty(),
                        mm.deserialize("<red>▶ Klik untuk menonaktifkan</red>")
                ), false));

        // Slot 42: Apply / Save
        String statName = selectedStat.getDisplayName();
        inventory.setItem(42, createItem(Material.NETHER_STAR,
                "<green><bold>✔ TERAPKAN SET BONUS KE ITEM</bold></green>",
                List.of(
                        mm.deserialize("<gray>Nama Set: <yellow>" + setName + "</yellow></gray>"),
                        mm.deserialize("<gray>Tipe Stat: <aqua>" + statName + "</aqua></gray>"),
                        mm.deserialize("<gray>Syarat: <gold>" + requiredPieces + " Pieces</gold></gray>"),
                        mm.deserialize("<gray>Nilai Bonus: <green>+" + (int) bonusValue + "%</green></gray>"),
                        Component.empty(),
                        mm.deserialize("<green>▶ Klik untuk menyimpan & terapkan ke item!</green>")
                ), true));

        // Slot 45: Back
        inventory.setItem(45, createItem(Material.ARROW, "<gradient:#3498db:#2980b9><bold>⬅ KEMBALI</bold></gradient>", List.of(
                mm.deserialize("<gray>Kembali tanpa menyimpan.</gray>")
        ), false));
    }

    private void addStatButton(int slot, KitStatType type, Material mat, String title) {
        boolean isSel = (selectedStat == type);
        inventory.setItem(slot, createItem(mat,
                title + (isSel ? " <green><bold>[TERPILIH]</bold></green>" : ""),
                List.of(
                        mm.deserialize("<gray>Stat: <gold>" + type.getDisplayName() + "</gold></gray>"),
                        Component.empty(),
                        mm.deserialize(isSel ? "<green>● Tipe ini sedang aktif.</green>" : "<yellow>▶ Klik untuk memilih tipe " + type.getDisplayName() + "</yellow>")
                ), isSel));
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

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

        // 2. Stat Type selection (Slots 10..15)
        Map<Integer, KitStatType> statMap = Map.of(
                10, KitStatType.DAMAGE_REDUCTION,
                11, KitStatType.ATTACK_DAMAGE_BOOST,
                12, KitStatType.DODGE_CHANCE,
                13, KitStatType.CRITICAL_DAMAGE_BOOST,
                14, KitStatType.EXTRA_MAX_HEALTH,
                15, KitStatType.MOVEMENT_SPEED_BOOST
        );
        if (statMap.containsKey(slot)) {
            this.selectedStat = statMap.get(slot);
            this.enabled = true;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.4f);
            buildGUI();
            return;
        }

        // 3. Pieces selection (Slots 21 & 23)
        if (slot == 21) {
            this.requiredPieces = 2;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.4f);
            buildGUI();
            return;
        }
        if (slot == 23) {
            this.requiredPieces = 4;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.4f);
            buildGUI();
            return;
        }

        // 4. Value selection (Slots 28..34)
        double[] values = {5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 50.0};
        int[] valSlots = {28, 29, 30, 31, 32, 33, 34};
        for (int i = 0; i < valSlots.length; i++) {
            if (slot == valSlots[i]) {
                this.bonusValue = values[i];
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.4f);
                buildGUI();
                return;
            }
        }

        // 5. Preset Name cycle (Slot 38)
        if (slot == 38) {
            nameIndex = (nameIndex + 1) % PRESET_NAMES.size();
            this.setName = PRESET_NAMES.get(nameIndex);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            buildGUI();
            return;
        }

        // 6. Disable / Remove Bonus (Slot 40)
        if (slot == 40) {
            removeBonusFromItem(item);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
            player.sendMessage(mm.deserialize("<yellow>Armor Set Bonus berhasil dihapus dari item!</yellow>"));
            if (onUpdate != null) onUpdate.accept(item);
            if (returnGUI != null) {
                player.openInventory(returnGUI.getInventory());
            } else {
                player.closeInventory();
            }
            return;
        }

        // 7. Apply Bonus to Item (Slot 42)
        if (slot == 42) {
            applyBonusToItem(item);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.4f);
            player.sendMessage(mm.deserialize("<green>Berhasil menerapkan Set Bonus <gold>" + setName + "</gold> (<aqua>" + selectedStat.getDisplayName() + " +" + (int) bonusValue + "%</aqua>)!</green>"));
            if (onUpdate != null) onUpdate.accept(item);
            if (returnGUI != null) {
                player.openInventory(returnGUI.getInventory());
            } else {
                player.closeInventory();
            }
        }
    }

    private void applyBonusToItem(ItemStack is) {
        if (is == null) return;
        ItemMeta meta = is.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey("apexsions", "set_id"), PersistentDataType.STRING, setName.toLowerCase());
        pdc.set(new NamespacedKey("apexsions", "set_name"), PersistentDataType.STRING, setName);
        pdc.set(new NamespacedKey("apexsions", "set_type"), PersistentDataType.STRING, selectedStat.name());
        pdc.set(new NamespacedKey("apexsions", "set_val"), PersistentDataType.DOUBLE, bonusValue);
        pdc.set(new NamespacedKey("apexsions", "set_req"), PersistentDataType.INTEGER, requiredPieces);

        // Rebuild Lore to show Set Bonus
        List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        // Remove existing set bonus lines
        lore.removeIf(c -> {
            String plain = mm.serialize(c);
            return plain.contains("SET BONUS") || plain.contains("Set Bonus:") || plain.contains("Pieces:");
        });

        lore.add(Component.empty());
        lore.add(mm.deserialize("<gradient:#e74c3c:#f39c12><bold>★ SET BONUS: " + setName.toUpperCase() + " ★</bold></gradient>"));
        lore.add(mm.deserialize("<gray>Syarat: <gold>" + requiredPieces + " Pieces</gold></gray>"));
        lore.add(mm.deserialize("<gray>Efek: <aqua>" + selectedStat.getDisplayName() + " +" + (int) bonusValue + "%</aqua></gray>"));

        meta.lore(lore);
        is.setItemMeta(meta);
    }

    private void removeBonusFromItem(ItemStack is) {
        if (is == null) return;
        ItemMeta meta = is.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.remove(new NamespacedKey("apexsions", "set_id"));
        pdc.remove(new NamespacedKey("apexsions", "set_name"));
        pdc.remove(new NamespacedKey("apexsions", "set_type"));
        pdc.remove(new NamespacedKey("apexsions", "set_val"));
        pdc.remove(new NamespacedKey("apexsions", "set_req"));

        if (meta.hasLore() && meta.lore() != null) {
            List<Component> lore = new ArrayList<>(meta.lore());
            lore.removeIf(c -> {
                String plain = mm.serialize(c);
                return plain.contains("SET BONUS") || plain.contains("Set Bonus:") || plain.contains("Pieces:");
            });
            meta.lore(lore);
        }
        is.setItemMeta(meta);
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

    public String getSetName() { return setName; }
    public KitStatType getSelectedStat() { return selectedStat; }
    public int getRequiredPieces() { return requiredPieces; }
    public double getBonusValue() { return bonusValue; }
    public boolean isEnabled() { return enabled; }
}
