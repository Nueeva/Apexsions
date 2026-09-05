package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
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
 * 54-Slot GUI for modifying a specific armor or tool item selected in AdminItemCreatorGUI.
 * Allows launching Custom Enchants Picker, Vanilla Enchants Picker, and Armor Set Bonus Picker.
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
        buildGUI();
        player.openInventory(inventory);
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

        // Slot 22: Rename Item in Chat
        inventory.setItem(22, createItem(Material.NAME_TAG,
                "<gradient:#f1c40f:#e67e22><bold>🏷 UBAH NAMA ITEM DI CHAT</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Ubah nama item ini secara spesifik.</gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk mengetik nama baru di chat!</yellow>")
                ), false));

        // Slot 23: Armor or Tool Set Bonus Picker
        boolean isArmor = AdminItemCreatorGUI.isArmor(item);
        boolean isTool = AdminItemCreatorGUI.isToolOrWeapon(item);
        if (isArmor) {
            inventory.setItem(23, createItem(Material.NETHERITE_CHESTPLATE,
                    "<gradient:#e74c3c:#f39c12><bold>🛡 PENGATURAN ARMOR SET BONUS</bold></gradient>",
                    List.of(
                            mm.deserialize("<gray>Atur stat 2-Piece, 4-Piece, atau keduanya,</gray>"),
                            mm.deserialize("<gray>dengan sub-menu persentase per stat visual!</gray>"),
                            Component.empty(),
                            mm.deserialize("<yellow>▶ Klik untuk mengatur Armor Set Bonus</yellow>")
                    ), true));
        } else if (isTool) {
            inventory.setItem(23, createItem(Material.NETHERITE_SWORD,
                    "<gradient:#3498db:#e67e22><bold>⚔ PENGATURAN TOOL SET BONUS</bold></gradient>",
                    List.of(
                            mm.deserialize("<gray>Atur bonus atribut & sinergi unik item ini</gray>"),
                            mm.deserialize("<gray>yang aktif bila memakai set armor yang cocok!</gray>"),
                            Component.empty(),
                            mm.deserialize("<yellow>▶ Klik untuk mengatur Tool Set Bonus</yellow>")
                    ), true));
        } else {
            inventory.setItem(23, createItem(Material.BARRIER,
                    "<dark_gray><bold>🛡 SET BONUS (TIDAK TERSEDIA)</bold></dark_gray>",
                    List.of(
                            mm.deserialize("<gray>Set bonus hanya dapat dipasang pada Armor & Tools.</gray>")
                    ), false));
        }

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
            creatorGUI.updateItem(sourceSlot, item);
            creatorGUI.open();
            return;
        }

        // Custom Enchants Picker
        if (slot == 19) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            new CustomEnchantPickerGUI(plugin, player, item, this, updated -> {
                this.item = updated;
                buildGUI();
            }).open();
            return;
        }

        // Vanilla Enchants Picker
        if (slot == 21) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            new VanillaEnchantPickerGUI(plugin, player, item, this, updated -> {
                this.item = updated;
                buildGUI();
            }).open();
            return;
        }

        // Slot 22: Rename Item in Chat
        if (slot == 22) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            plugin.getItemRenameManager().startSession(
                    player,
                    "Ketik nama baru untuk item ini di chat (bisa menggunakan & atau MiniMessage):",
                    newName -> {
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            Component c;
                            if (newName.contains("<") && newName.contains(">")) {
                                c = mm.deserialize(newName);
                            } else if (newName.contains("&")) {
                                c = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(newName);
                            } else {
                                c = mm.deserialize("<gold><bold>" + newName + "</bold></gold>");
                            }
                            meta.displayName(c);
                            item.setItemMeta(meta);
                            player.sendMessage(mm.deserialize("<green>✓ Nama item berhasil diubah!</green>"));
                        }
                        this.open();
                    },
                    this::open
            );
            return;
        }

        // Slot 23: Armor Set Bonus Picker or Tool Bonus Picker
        if (slot == 23) {
            if (AdminItemCreatorGUI.isArmor(item)) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                new ArmorSetBonusPickerGUI(plugin, player, item, this, updated -> {
                    this.item = updated;
                    buildGUI();
                }).open();
                return;
            } else if (AdminItemCreatorGUI.isToolOrWeapon(item)) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                String cId = creatorGUI.getGlobalSetId();
                String cName = creatorGUI.getGlobalSetName();
                new ToolBonusPickerGUI(plugin, player, item, cId, cName, this, updated -> {
                    this.item = updated;
                    buildGUI();
                }).open();
                return;
            }
        }

        // Reset Enchants
        if (slot == 25) {
            for (CustomEnchant ce : plugin.getEnchantmentRegistry().getAllEnchantments()) {
                item = plugin.getEnchantmentRegistry().removeEnchant(item, ce);
            }
            for (Enchantment ve : new ArrayList<>(item.getEnchantments().keySet())) {
                item.removeEnchantment(ve);
            }
            player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<yellow>Seluruh enchantment berhasil dihapus dari item!</yellow>"));
            buildGUI();
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
