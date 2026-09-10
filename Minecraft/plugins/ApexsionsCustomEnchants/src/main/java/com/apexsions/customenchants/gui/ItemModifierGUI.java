package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import com.apexsions.customenchants.gui.dialog.ItemEditDialogFlow;
import com.apexsions.customenchants.gui.input.NativeDialogAdapter;
import com.apexsions.customenchants.items.ColorUtil;
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
import java.util.Map;

/**
 * GUI & Native Dialog for modifying a specific armor or tool item selected in AdminItemCreatorGUI.
 * Opens natively as a Dialog GUI (NightCore / Paper 1.21.4+ / Bedrock Form) with item preview at top-center,
 * with graceful fallback to a 54-slot chest GUI if Dialogs are unsupported.
 */
public class ItemModifierGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private ItemStack item;
    private final int sourceSlot;
    private final AdminItemCreatorGUI creatorGUI;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ItemModifierGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, int sourceSlot, AdminItemCreatorGUI creatorGUI) {
        this.plugin = plugin;
        this.player = player;
        this.item = item;
        this.sourceSlot = sourceSlot;
        this.creatorGUI = creatorGUI;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>🛠 EDIT ITEM & ENCHANTS 🛠</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        if (creatorGUI != null) {
            creatorGUI.setNavigatingSubGUI(true);
        }
        if (openDialogModifier()) {
            return;
        }
        buildGUI();
        player.openInventory(inventory);
    }

    public boolean openDialogModifier() {
        if (item == null || player == null || !player.isOnline()) return false;
        return ItemEditDialogFlow.openRoot(plugin, player, item, sourceSlot, creatorGUI);
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null, false);
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, border);
        }

        // Slot 4: Item Preview
        inventory.setItem(4, item);

        // Slot 19: Custom Enchants Picker
        Map<CustomEnchant, Integer> activeCE = plugin.getEnchantmentRegistry().getEnchantsOnItem(item);
        inventory.setItem(19, createItem(Material.FIREWORK_STAR,
                "<gradient:#9b59b6:#e74c3c><bold>🔮 KELOLA CUSTOM ENCHANTS</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Buka katalog 182 Custom Enchantments</gray>"),
                        mm.deserialize("<gray>dengan visual Firework Star sesuai rarity!</gray>"),
                        Component.empty(),
                        mm.deserialize("<gray>Sihir aktif saat ini: <gold>" + activeCE.size() + " Custom Enchants</gold></gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk memilih Custom Enchants via GUI</yellow>")
                ), true));

        // Slot 20: Minimum Level Requirement Picker
        int minLevel = com.apexsions.customenchants.items.ItemLevelRequirement.getRequiredLevel(item);
        inventory.setItem(20, createItem(Material.EXPERIENCE_BOTTLE,
                "<gradient:#f39c12:#e67e22><bold>🎖 ATUR SYARAT MINIMAL LEVEL</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Tentukan batas level pemain di <gold>ApexsionsCore</gold></gray>"),
                        mm.deserialize("<gray>agar dapat mengenakan atau memakai item ini.</gray>"),
                        Component.empty(),
                        mm.deserialize("<gray>Status saat ini: " + (minLevel > 0 ? "<gold><bold>Level " + minLevel + "+</bold></gold>" : "<green>Bebas Dipakai</green>") + "</gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk mengatur syarat level item ini</yellow>")
                ), minLevel > 0));

        // Slot 21: Vanilla Enchants Picker
        int activeVanilla = item.getEnchantments().size();
        inventory.setItem(21, createItem(Material.ENCHANTED_BOOK,
                "<gradient:#f1c40f:#e67e22><bold>📜 KELOLA VANILLA ENCHANTS</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Buka katalog sihir Vanilla (Sharpness, Protection, dll)</gray>"),
                        mm.deserialize("<gray>dan tentukan levelnya melalui tombol GUI!</gray>"),
                        Component.empty(),
                        mm.deserialize("<gray>Enchant vanilla aktif: <aqua>" + activeVanilla + " Enchants</aqua></gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk memilih Vanilla Enchants via GUI</yellow>")
                ), false));

        // Slot 22: Rename Item via GUI
        inventory.setItem(22, createItem(Material.NAME_TAG,
                "<gradient:#f1c40f:#e67e22><bold>🏷 UBAH NAMA ITEM</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Ubah nama item ini secara spesifik.</gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk ubah nama baru via GUI!</yellow>")
                ), false));

        // Slot 23: Tool Set Bonus Picker (Armor Set Bonus is configured in its own dedicated GUI)
        boolean isTool = AdminItemCreatorGUI.isToolOrWeapon(item);
        if (isTool) {
            inventory.setItem(23, createItem(Material.NETHERITE_SWORD,
                    "<gradient:#3498db:#e67e22><bold>⚔ PENGATURAN TOOL SET BONUS</bold></gradient>",
                    List.of(
                            mm.deserialize("<gray>Atur bonus atribut & sinergi unik item ini</gray>"),
                            mm.deserialize("<gray>yang aktif bila memakai set armor yang cocok!</gray>"),
                            Component.empty(),
                            mm.deserialize("<yellow>▶ Klik untuk mengatur Tool Set Bonus</yellow>")
                    ), true));
        } else {
            inventory.setItem(23, createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of(), false));
        }

        // Slot 24: Selective Enchant Remover
        int totalActive = activeCE.size() + activeVanilla;
        inventory.setItem(24, createItem(Material.SHEARS,
                "<gradient:#e74c3c:#c0392b><bold>✂ HAPUS ENCHANT TERTENTU</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Lepas sihir atau enchant satu per satu</gray>"),
                        mm.deserialize("<gray>tanpa perlu me-reset seluruh enchant item!</gray>"),
                        Component.empty(),
                        mm.deserialize("<gray>Sihir aktif saat ini: <gold>" + totalActive + " Enchant</gold></gray>"),
                        Component.empty(),
                        mm.deserialize(totalActive > 0 ? "<yellow>▶ Klik untuk memilih sihir yang ingin dilepas</yellow>" : "<dark_gray>Tidak ada enchant untuk dihapus.</dark_gray>")
                ), totalActive > 0));

        // Slot 25: Reset / Clear Enchants
        inventory.setItem(25, createItem(Material.CAULDRON,
                "<red><bold>🗑 RESET SEMUA ENCHANT</bold></red>",
                List.of(
                        mm.deserialize("<gray>Hapus seluruh custom enchants dan vanilla enchants dari item ini.</gray>"),
                        Component.empty(),
                        mm.deserialize("<red>▶ Klik untuk me-reset item</red>")
                ), false));

        // Slot 45: Back
        inventory.setItem(45, createItem(Material.ARROW, "<gradient:#3498db:#2980b9><bold>⬅ SELESAI & KEMBALI KE ITEM CREATOR</bold></gradient>", List.of(
                mm.deserialize("<gray>Simpan perubahan item dan kembali ke creator.</gray>")
        ), false));
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        // Back to Creator
        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            if (creatorGUI != null) {
                creatorGUI.updateItem(sourceSlot, item);
                creatorGUI.open();
            }
            return;
        }

        // Custom Enchants Picker
        if (slot == 19) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            new CustomEnchantPickerGUI(plugin, player, item, this, updated -> {
                this.item = updated;
                if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, this.item);
                this.open();
            }).open();
            return;
        }

        // Slot 20: Minimum Level Requirement Picker
        if (slot == 20) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            new ItemLevelPickerGUI(plugin, player, item, this, updated -> {
                this.item = updated;
                if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, this.item);
                this.open();
            }).open();
            return;
        }

        // Vanilla Enchants Picker
        if (slot == 21) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            new VanillaEnchantPickerGUI(plugin, player, item, this, updated -> {
                this.item = updated;
                if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, this.item);
                this.open();
            }).open();
            return;
        }

        // Slot 22: Rename Item via GUI
        if (slot == 22) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            plugin.getItemRenameManager().startSession(
                    player,
                    "Masukkan nama baru untuk item ini (bisa menggunakan & atau MiniMessage):",
                    newName -> {
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.displayName(ColorUtil.parse(newName));
                            item.setItemMeta(meta);
                            if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, item);
                            player.sendMessage(mm.deserialize("<green>✓ Nama item berhasil diubah!</green>"));
                        }
                        this.open();
                    },
                    this::open
            );
            return;
        }

        // Slot 23: Tool Bonus Picker (Dialog GUI Flow)
        if (slot == 23) {
            if (AdminItemCreatorGUI.isToolOrWeapon(item)) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                player.closeInventory();
                ItemEditDialogFlow.openToolBonus(plugin, player, item, sourceSlot, creatorGUI);
                return;
            }
        }

        // Slot 24: Selective Enchant Remover
        if (slot == 24) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            new RemoveEnchantsGUI(plugin, player, item, this, updated -> {
                this.item = updated;
                if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, this.item);
                this.open();
            }).open();
            return;
        }

        // Reset Enchants
        if (slot == 25) {
            for (CustomEnchant ce : plugin.getEnchantmentRegistry().getAllEnchantments()) {
                item = plugin.getEnchantmentRegistry().removeEnchant(item, ce);
            }
            for (Enchantment ve : new ArrayList<>(item.getEnchantments().keySet())) {
                item.removeEnchantment(ve);
            }
            if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, item);
            player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<yellow>Seluruh enchantment berhasil dihapus dari item!</yellow>"));
            this.open();
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
