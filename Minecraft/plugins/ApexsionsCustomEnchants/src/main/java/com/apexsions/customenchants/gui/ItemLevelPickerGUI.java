package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.gui.input.EnchantsInputManager;
import com.apexsions.customenchants.items.ItemLevelRequirement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Interactive 54-Slot GUI for selecting and fine-tuning minimum level requirements on an item.
 */
public class ItemLevelPickerGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private ItemStack item;
    private final InventoryHolder parentGUI;
    private final Consumer<ItemStack> onUpdate;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private int selectedLevel;

    public ItemLevelPickerGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item,
                              InventoryHolder parentGUI, Consumer<ItemStack> onUpdate) {
        this.plugin = plugin;
        this.player = player;
        this.item = item;
        this.parentGUI = parentGUI;
        this.onUpdate = onUpdate;
        this.selectedLevel = ItemLevelRequirement.getRequiredLevel(item);
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#f39c12:#e67e22><bold>🎖 ATUR SYARAT MINIMAL LEVEL 🎖</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();

        // 1. Borders
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null, false);
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, border);
        }

        // 2. Slot 4: Item Live Preview
        inventory.setItem(4, item);

        // 3. Preset Buttons (Row 2: Slots 19..25)
        int[] presetLevels = {5, 10, 20, 30, 50, 75, 100};
        Material[] presetMats = {
                Material.COAL,
                Material.IRON_INGOT,
                Material.LAPIS_LAZULI,
                Material.GOLD_INGOT,
                Material.EMERALD,
                Material.NETHERITE_INGOT,
                Material.NETHER_STAR
        };
        String[] presetRanks = {
                "Pemula (Tier I)",
                "Petualang (Tier I)",
                "Prajurit (Tier II)",
                "Ksatria (Tier II)",
                "Panglima (Tier III)",
                "Bangsawan (Tier IV)",
                "Penguasa Puncak (Tier V)"
        };

        for (int i = 0; i < presetLevels.length; i++) {
            int lvl = presetLevels[i];
            int slot = 19 + i;
            boolean isCurrent = (selectedLevel == lvl);

            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Tingkat: <aqua>" + presetRanks[i] + "</aqua></gray>"));
            lore.add(Component.empty());
            if (isCurrent) {
                lore.add(mm.deserialize("<green><bold>✓ PILIHAN AKTIF SAAT INI</bold></green>"));
            } else {
                lore.add(mm.deserialize("<yellow>▶ Klik untuk tetapkan ke Level " + lvl + "+</yellow>"));
            }

            inventory.setItem(slot, createItem(presetMats[i],
                    "<gold><bold>🎖 PRESET: LEVEL " + lvl + "+</bold></gold>",
                    lore, isCurrent));
        }

        // 4. Fine-Tuning Controls (Row 4: Slots 29..33)
        // Slot 29: -10 Level
        inventory.setItem(29, createItem(Material.RED_STAINED_GLASS_PANE,
                "<red><bold>⏪ -10 LEVEL</bold></red>",
                List.of(mm.deserialize("<gray>Kurangi syarat sebanyak 10 level.</gray>")), false));

        // Slot 30: -1 Level
        inventory.setItem(30, createItem(Material.ORANGE_STAINED_GLASS_PANE,
                "<red><bold>◀ -1 LEVEL</bold></red>",
                List.of(mm.deserialize("<gray>Kurangi syarat sebanyak 1 level.</gray>")), false));

        // Slot 31: Center Status Display
        Material statusMat = selectedLevel > 0 ? Material.EXPERIENCE_BOTTLE : Material.GLASS_BOTTLE;
        String statusTitle = selectedLevel > 0
                ? "<gradient:#f1c40f:#e67e22><bold>🎖 SYARAT: LEVEL " + selectedLevel + "+</bold></gradient>"
                : "<green><bold>🎖 STATUS: BEBAS DIGUNAKAN</bold></green>";
        List<Component> statusLore = new ArrayList<>();
        if (selectedLevel > 0) {
            statusLore.add(mm.deserialize("<gray>Pemain wajib mencapai <gold>Level " + selectedLevel + "</gold></gray>"));
            statusLore.add(mm.deserialize("<gray>di <gold>ApexsionsCore</gold> agar bisa memakai item ini.</gray>"));
        } else {
            statusLore.add(mm.deserialize("<gray>Item ini tidak memiliki syarat level.</gray>"));
            statusLore.add(mm.deserialize("<gray>Semua pemain dari level 1 dapat memakainya.</gray>"));
        }
        statusLore.add(Component.empty());
        statusLore.add(mm.deserialize("<yellow>Gunakan tombol di sekitar untuk mengatur level.</yellow>"));
        inventory.setItem(31, createItem(statusMat, statusTitle, statusLore, selectedLevel > 0));

        // Slot 32: +1 Level
        inventory.setItem(32, createItem(Material.LIME_STAINED_GLASS_PANE,
                "<green><bold>+1 LEVEL ▶</bold></green>",
                List.of(mm.deserialize("<gray>Tambah syarat sebanyak 1 level.</gray>")), false));

        // Slot 33: +10 Level
        inventory.setItem(33, createItem(Material.GREEN_STAINED_GLASS_PANE,
                "<green><bold>+10 LEVEL ⏩</bold></green>",
                List.of(mm.deserialize("<gray>Tambah syarat sebanyak 10 level.</gray>")), false));

        // 5. Actions (Row 5: Slots 38, 42, 45)
        // Slot 38: Manual Custom Input
        inventory.setItem(38, createItem(Material.NAME_TAG,
                "<gradient:#3498db:#2980b9><bold>✍ KETIK LEVEL MANUAL (1 - 100)</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Ketik angka level spesifik yang diinginkan</gray>"),
                        mm.deserialize("<gray>melalui menu input / chat.</gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk memasukkan angka level</yellow>")
                ), false));

        // Slot 42: Remove Level Requirement
        inventory.setItem(42, createItem(Material.BARRIER,
                "<red><bold>❌ HAPUS SYARAT LEVEL</bold></red>",
                List.of(
                        mm.deserialize("<gray>Hapus seluruh batasan level dari item ini.</gray>"),
                        mm.deserialize("<gray>Item menjadi bebas dipakai siapapun.</gray>"),
                        Component.empty(),
                        mm.deserialize("<red>▶ Klik untuk mereset batasan level</red>")
                ), false));

        // Slot 45: Back
        inventory.setItem(45, createItem(Material.ARROW,
                "<gradient:#3498db:#2980b9><bold>⬅ SIMPAN & KEMBALI</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Simpan pengaturan level dan kembali ke menu item.</gray>")
                ), false));
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        // 1. Back / Done
        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            closeAndReturn();
            return;
        }

        // 2. Presets (19..25)
        int[] presetLevels = {5, 10, 20, 30, 50, 75, 100};
        for (int i = 0; i < presetLevels.length; i++) {
            if (slot == 19 + i) {
                applyNewLevel(presetLevels[i]);
                return;
            }
        }

        // 3. Fine Tuning
        if (slot == 29) { // -10
            applyNewLevel(Math.max(0, selectedLevel - 10));
            return;
        }
        if (slot == 30) { // -1
            applyNewLevel(Math.max(0, selectedLevel - 1));
            return;
        }
        if (slot == 32) { // +1
            applyNewLevel(Math.min(100, (selectedLevel == 0 ? 1 : selectedLevel + 1)));
            return;
        }
        if (slot == 33) { // +10
            applyNewLevel(Math.min(100, selectedLevel + 10));
            return;
        }

        // 4. Reset Level Requirement
        if (slot == 42) {
            applyNewLevel(0);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.8f, 1.2f);
            player.sendMessage(mm.deserialize("<yellow>✓ Syarat level berhasil dihapus! Item kini bebas dipakai.</yellow>"));
            return;
        }

        // 5. Manual Text Input
        if (slot == 38) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            EnchantsInputManager.openInput(
                    plugin,
                    player,
                    "INPUT SYARAT LEVEL",
                    "Masukkan batas minimal level (1 - 100):",
                    String.valueOf(selectedLevel > 0 ? selectedLevel : 10),
                    inputStr -> {
                        try {
                            int parsed = Integer.parseInt(inputStr.trim());
                            if (parsed < 1) parsed = 0;
                            if (parsed > 100) parsed = 100;
                            applyNewLevel(parsed);
                        } catch (NumberFormatException e) {
                            player.sendMessage(mm.deserialize("<red>Angka tidak valid! Silakan masukkan angka antara 1 dan 100.</red>"));
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        }
                        this.open();
                    },
                    this::open
            );
        }
    }

    private void applyNewLevel(int newLevel) {
        this.selectedLevel = newLevel;
        this.item = ItemLevelRequirement.setRequiredLevel(this.item, newLevel);
        if (onUpdate != null) {
            onUpdate.accept(this.item);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.3f);
        buildGUI();
    }

    private void closeAndReturn() {
        if (parentGUI instanceof ItemModifierGUI modifier) {
            modifier.setItem(item);
            modifier.open();
        } else if (parentGUI instanceof AdminItemCreatorGUI creator) {
            creator.open();
        } else {
            player.closeInventory();
        }
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
