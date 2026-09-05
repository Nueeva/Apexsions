package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.presets.PresetManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 54-Slot GUI for browsing, taking, and deleting saved Item Creator presets.
 */
public class AdminPresetsGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final InventoryHolder returnGUI;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Integer, PresetManager.Preset> slotPresetMap = new HashMap<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");

    public AdminPresetsGUI(ApexsionsCustomEnchantsPlugin plugin, Player player, InventoryHolder returnGUI) {
        this.plugin = plugin;
        this.player = player;
        this.returnGUI = returnGUI;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#9b59b6:#e74c3c><bold>📦 PRESET SET ARMOR & TOOLS</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotPresetMap.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null, false);
        for (int i = 0; i < 9; i++) inventory.setItem(i, border);
        for (int i = 45; i < 54; i++) inventory.setItem(i, border);

        List<PresetManager.Preset> list = new ArrayList<>(plugin.getPresetManager().getPresets());
        list.sort(Comparator.comparingLong(PresetManager.Preset::createdAt).reversed());

        int slot = 9;
        for (PresetManager.Preset p : list) {
            if (slot >= 45) break;
            slotPresetMap.put(slot, p);

            Material iconMat = Material.NETHERITE_CHESTPLATE;
            if (!p.armorPieces().isEmpty() && p.armorPieces().get(0) != null) {
                iconMat = p.armorPieces().get(0).getType();
            } else if (!p.toolPieces().isEmpty() && p.toolPieces().get(0) != null) {
                iconMat = p.toolPieces().get(0).getType();
            }

            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>ID Preset: <yellow>" + p.id() + "</yellow></gray>"));
            lore.add(mm.deserialize("<gray>Dibuat pada: <gold>" + sdf.format(new Date(p.createdAt())) + "</gold></gray>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<gray>Isi Set:</gray>"));
            lore.add(mm.deserialize("<gold>● " + p.armorPieces().size() + " Potong Armor</gold>"));
            lore.add(mm.deserialize("<aqua>● " + p.toolPieces().size() + " Senjata / Tools</aqua>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<green><bold>▶ Klik Kiri: Ambil seluruh item set!</bold></green>"));
            lore.add(mm.deserialize("<red><bold>▶ Shift + Klik Kanan: Hapus preset ini!</bold></red>"));

            ItemStack item = createItem(iconMat, p.displayName(), lore, true);
            inventory.setItem(slot++, item);
        }

        if (list.isEmpty()) {
            inventory.setItem(22, createItem(Material.BARRIER, "<red><bold>Belum Ada Preset Tersimpan</bold></red>", List.of(
                    mm.deserialize("<gray>Simpan set melalui tombol toggle di Item Creator!</gray>")
            ), false));
        }

        // Controls
        // Slot 45: Back
        inventory.setItem(45, createItem(Material.ARROW, "<gradient:#3498db:#2980b9><bold>⬅ KEMBALI</bold></gradient>", List.of(
                mm.deserialize("<gray>Kembali ke Item Creator.</gray>")
        ), false));

        // Slot 49: Info
        inventory.setItem(49, createItem(Material.BOOK, "<gold><bold>Informasi Preset</bold></gold>", List.of(
                mm.deserialize("<gray>Total Preset: <yellow>" + list.size() + "</yellow></gray>"),
                mm.deserialize("<gray>Semua preset menyimpan enchant & lore secara utuh.</gray>")
        ), false));

        // Slot 53: Close
        inventory.setItem(53, createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>", null, false));
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            if (returnGUI != null) {
                if (returnGUI instanceof AdminItemCreatorGUI creator) {
                    creator.open();
                } else {
                    player.openInventory(returnGUI.getInventory());
                }
            } else {
                player.closeInventory();
            }
            return;
        }

        if (slot == 53) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            player.closeInventory();
            return;
        }

        PresetManager.Preset p = slotPresetMap.get(slot);
        if (p == null) return;

        if (event.getClick() == ClickType.SHIFT_RIGHT) {
            // Delete preset
            boolean deleted = plugin.getPresetManager().deletePreset(p.id());
            if (deleted) {
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<red>Preset <gold>" + p.id() + "</gold> berhasil dihapus!</red>"));
                buildGUI();
            }
            return;
        }

        if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT) {
            // Claim all items in preset
            int totalGiven = 0;
            List<ItemStack> allItems = new ArrayList<>();
            for (ItemStack is : p.armorPieces()) {
                if (is != null && !is.getType().isAir()) {
                    allItems.add(plugin.getEnchantmentRegistry().updateLoreAndGlint(is.clone()));
                }
            }
            for (ItemStack is : p.toolPieces()) {
                if (is != null && !is.getType().isAir()) {
                    allItems.add(plugin.getEnchantmentRegistry().updateLoreAndGlint(is.clone()));
                }
            }

            for (ItemStack is : allItems) {
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(is);
                if (!overflow.isEmpty()) {
                    for (ItemStack drop : overflow.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    }
                }
                totalGiven++;
            }

            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            player.sendMessage(mm.deserialize("<green><bold>✓ BERHASIL!</bold> Mengambil <yellow>" + totalGiven + " item</yellow> dari preset <gold>" + p.displayName() + "</gold>!</green>"));
        }
    }

    private ItemStack createItem(Material mat, String name, List<Component> lore, boolean glow) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            if (lore != null) meta.lore(lore);
            if (glow) {
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
