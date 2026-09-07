package com.apexsions.customenchants.gui;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.items.ColorUtil;
import com.apexsions.core.kit.KitStatType;
import com.apexsions.customenchants.presets.PresetManager;
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

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 54-Slot Interactive Preview GUI for saved Item & Fullset Presets.
 * Allows admins to inspect all armor pieces, tools, enchantments, and stats
 * before claiming all items or returning to the presets menu.
 */
public class PresetPreviewGUI implements InventoryHolder {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Player player;
    private final PresetManager.Preset preset;
    private final InventoryHolder previousGUI;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");
    private final Map<Integer, ItemStack> previewItemMap = new HashMap<>();

    public PresetPreviewGUI(ApexsionsCustomEnchantsPlugin plugin, Player player,
                            PresetManager.Preset preset, InventoryHolder previousGUI) {
        this.plugin = plugin;
        this.player = player;
        this.preset = preset;
        this.previousGUI = previousGUI;

        Component title = mm.deserialize("<gradient:#9b59b6:#e74c3c><bold>PREVIEW: </bold></gradient>")
                .append(ColorUtil.parse(preset.displayName()));
        this.inventory = Bukkit.createInventory(this, 54, title);
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        previewItemMap.clear();

        // 1. Borders
        ItemStack blackBorder = createGlass(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack grayBorder = createGlass(Material.GRAY_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 9; i++) inventory.setItem(i, blackBorder);
        for (int i = 36; i < 45; i++) inventory.setItem(i, blackBorder);
        for (int i = 45; i < 54; i++) inventory.setItem(i, blackBorder);

        inventory.setItem(9, blackBorder);
        inventory.setItem(14, blackBorder);
        inventory.setItem(16, blackBorder);
        inventory.setItem(17, blackBorder);

        for (int i = 18; i <= 26; i++) {
            inventory.setItem(i, grayBorder);
        }
        inventory.setItem(22, createItem(Material.CYAN_STAINED_GLASS_PANE,
                "<gradient:#3498db:#00cec9><bold>⚔ DAFTAR SENJATA & TOOLS ⚔</bold></gradient>",
                List.of(mm.deserialize("<gray>Klik item di bawah untuk mengambil satuan,</gray>"),
                        mm.deserialize("<gray>atau gunakan tombol di bawah untuk mengambil semua.</gray>")),
                false));

        inventory.setItem(27, blackBorder);
        inventory.setItem(35, blackBorder);

        // 2. Slot 4: Preset Information Header
        List<Component> headerLore = new ArrayList<>();
        headerLore.add(mm.deserialize("<gray>ID Preset: <yellow>" + preset.id() + "</yellow></gray>"));
        headerLore.add(mm.deserialize("<gray>Dibuat: <gold>" + sdf.format(new Date(preset.createdAt())) + "</gold></gray>"));
        headerLore.add(Component.empty());
        headerLore.add(mm.deserialize("<gray>Armor: <gold>" + preset.armorPieces().size() + " Potong</gold></gray>"));
        headerLore.add(mm.deserialize("<gray>Tools/Senjata: <aqua>" + preset.toolPieces().size() + " Buah</aqua></gray>"));
        headerLore.add(Component.empty());
        headerLore.add(mm.deserialize("<dark_gray>Arahkan kursor ke item untuk melihat enchant & stat</dark_gray>"));

        ItemStack headerItem = createItem(Material.CHEST_MINECART,
                preset.displayName(),
                headerLore,
                true);
        inventory.setItem(4, headerItem);

        // 3. Row 1: Armor Slots (10=Helmet, 11=Chestplate, 12=Leggings, 13=Boots)
        ItemStack helmet = null;
        ItemStack chestplate = null;
        ItemStack leggings = null;
        ItemStack boots = null;
        List<ItemStack> remainingArmor = new ArrayList<>();

        for (ItemStack piece : preset.armorPieces()) {
            if (piece == null || piece.getType().isAir()) continue;
            if (AdminItemCreatorGUI.isHelmet(piece) && helmet == null) {
                helmet = piece;
            } else if (AdminItemCreatorGUI.isChestplate(piece) && chestplate == null) {
                chestplate = piece;
            } else if (AdminItemCreatorGUI.isLeggings(piece) && leggings == null) {
                leggings = piece;
            } else if (AdminItemCreatorGUI.isBoots(piece) && boots == null) {
                boots = piece;
            } else {
                remainingArmor.add(piece);
            }
        }

        if (helmet == null && !remainingArmor.isEmpty()) helmet = remainingArmor.remove(0);
        if (chestplate == null && !remainingArmor.isEmpty()) chestplate = remainingArmor.remove(0);
        if (leggings == null && !remainingArmor.isEmpty()) leggings = remainingArmor.remove(0);
        if (boots == null && !remainingArmor.isEmpty()) boots = remainingArmor.remove(0);

        renderArmorPiece(10, helmet, Material.IRON_HELMET, "Slot Helmet Kosong");
        renderArmorPiece(11, chestplate, Material.IRON_CHESTPLATE, "Slot Chestplate Kosong");
        renderArmorPiece(12, leggings, Material.IRON_LEGGINGS, "Slot Leggings Kosong");
        renderArmorPiece(13, boots, Material.IRON_BOOTS, "Slot Boots Kosong");

        // 4. Slot 15: Armor Set Bonus & Synergy Information
        renderSetBonusInfo();

        // 5. Row 3: Tools Slots (28..34)
        int[] toolSlots = {28, 29, 30, 31, 32, 33, 34};
        List<ItemStack> tools = preset.toolPieces();
        for (int i = 0; i < toolSlots.length; i++) {
            int slot = toolSlots[i];
            if (i < tools.size() && tools.get(i) != null && !tools.get(i).getType().isAir()) {
                ItemStack original = tools.get(i);
                ItemStack display = plugin.getEnchantmentRegistry().updateLoreAndGlint(original.clone());
                inventory.setItem(slot, display);
                previewItemMap.put(slot, original);
            } else {
                inventory.setItem(slot, createItem(Material.GRAY_STAINED_GLASS_PANE,
                        "<dark_gray><italic>Slot Tool #" + (i + 1) + " Kosong</italic></dark_gray>",
                        List.of(mm.deserialize("<gray>Preset ini tidak memiliki tool di slot ini.</gray>")),
                        false));
            }
        }

        // 6. Bottom Controls (Row 5)
        // Slot 45: Balik ke Menu Awal
        inventory.setItem(45, createItem(Material.ARROW,
                "<gradient:#3498db:#2980b9><bold>⬅ BALIK KE MENU AWAL</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Kembali ke daftar seluruh preset tersimpan.</gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk kembali ke menu awal</yellow>")
                ), false));

        // Slot 47: Hapus Preset
        inventory.setItem(47, createItem(Material.RED_CONCRETE,
                "<red><bold>🗑 HAPUS PRESET INI</bold></red>",
                List.of(
                        mm.deserialize("<gray>Hapus preset </gray><gold>" + preset.id() + "</gold><gray> secara permanen.</gray>"),
                        Component.empty(),
                        mm.deserialize("<red><bold>▶ Shift + Klik Kanan untuk konfirmasi hapus!</bold></red>")
                ), false));

        // Slot 49: Ambil Seluruh Set Item
        int totalItems = preset.armorPieces().size() + preset.toolPieces().size();
        inventory.setItem(49, createItem(Material.EMERALD_BLOCK,
                "<gradient:#2ecc71:#27ae60><bold>✔ AMBIL SELURUH SET ITEM</bold></gradient>",
                List.of(
                        mm.deserialize("<gray>Klaim seluruh perlengkapan armor dan tools</gray>"),
                        mm.deserialize("<gray>ke dalam inventaris tas karaktermu.</gray>"),
                        Component.empty(),
                        mm.deserialize("<gold>● Total Komponen: <yellow>" + totalItems + " Item</yellow></gold>"),
                        Component.empty(),
                        mm.deserialize("<green><bold>▶ Klik untuk mengambil seluruh item!</bold></green>")
                ), true));

        // Slot 53: Tutup
        inventory.setItem(53, createItem(Material.BARRIER,
                "<red><bold>✖ TUTUP</bold></red>",
                List.of(mm.deserialize("<gray>Tutup menu pratinjau preset.</gray>")),
                false));
    }

    private void renderArmorPiece(int slot, ItemStack piece, Material fallbackMat, String emptyTitle) {
        if (piece != null && !piece.getType().isAir()) {
            ItemStack display = plugin.getEnchantmentRegistry().updateLoreAndGlint(piece.clone());
            inventory.setItem(slot, display);
            previewItemMap.put(slot, piece);
        } else {
            inventory.setItem(slot, createItem(fallbackMat,
                    "<dark_gray><italic>" + emptyTitle + "</italic></dark_gray>",
                    List.of(mm.deserialize("<gray>Preset ini tidak memiliki item di bagian ini.</gray>")),
                    false));
        }
    }

    private void renderSetBonusInfo() {
        Map<KitStatType, Double> set2 = new LinkedHashMap<>();
        Map<KitStatType, Double> set4 = new LinkedHashMap<>();
        String detectedSetName = null;

        for (ItemStack is : preset.armorPieces()) {
            if (is == null || !is.hasItemMeta()) continue;
            PersistentDataContainer pdc = is.getItemMeta().getPersistentDataContainer();

            if (detectedSetName == null) {
                NamespacedKey kName = new NamespacedKey("apexsions", "set_name");
                if (pdc.has(kName, PersistentDataType.STRING)) {
                    detectedSetName = pdc.get(kName, PersistentDataType.STRING);
                }
            }
            if (set2.isEmpty()) {
                NamespacedKey k2 = new NamespacedKey("apexsions", "set2_stats");
                if (pdc.has(k2, PersistentDataType.STRING)) {
                    AdminItemCreatorGUI.parseStatString(pdc.get(k2, PersistentDataType.STRING), set2);
                }
            }
            if (set4.isEmpty()) {
                NamespacedKey k4 = new NamespacedKey("apexsions", "set4_stats");
                if (pdc.has(k4, PersistentDataType.STRING)) {
                    AdminItemCreatorGUI.parseStatString(pdc.get(k4, PersistentDataType.STRING), set4);
                }
            }
        }

        boolean hasBonus = !set2.isEmpty() || !set4.isEmpty();
        List<Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<gray>Nama Sinergi: </gray>").append(detectedSetName != null && !detectedSetName.isBlank()
                ? ColorUtil.parse(detectedSetName)
                : mm.deserialize("<dark_gray>(Tidak Ada Sinergi Khusus)</dark_gray>")));
        lore.add(Component.empty());

        if (hasBonus) {
            if (!set2.isEmpty()) {
                lore.add(mm.deserialize("<green>● 2-Piece Bonus (Half Set):</green>"));
                for (Map.Entry<KitStatType, Double> e : set2.entrySet()) {
                    lore.add(mm.deserialize("<aqua>  - " + e.getKey().getDisplayName() + ": <gold>" + e.getKey().formatValue(e.getValue()) + "</gold></aqua>"));
                }
            }
            if (!set4.isEmpty()) {
                lore.add(mm.deserialize("<green>● 4-Piece Bonus (Full Set):</green>"));
                for (Map.Entry<KitStatType, Double> e : set4.entrySet()) {
                    lore.add(mm.deserialize("<aqua>  - " + e.getKey().getDisplayName() + ": <gold>" + e.getKey().formatValue(e.getValue()) + "</gold></aqua>"));
                }
            }
        } else {
            lore.add(mm.deserialize("<dark_gray>● Preset ini tidak memiliki efek Armor Set Bonus.</dark_gray>"));
        }

        Material iconMat = hasBonus ? Material.NETHER_STAR : Material.SHIELD;
        inventory.setItem(15, createItem(iconMat,
                hasBonus ? "<gold><bold>🛡 INFO ARMOR SET BONUS</bold></gold>" : "<gray><bold>🛡 INFO SET ARMOR</bold></gray>",
                lore,
                hasBonus));
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        // Slot 45: Balik ke Menu Awal
        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            if (previousGUI != null) {
                if (previousGUI instanceof AdminPresetsGUI presetsGUI) {
                    presetsGUI.open();
                } else if (previousGUI instanceof AdminItemCreatorGUI creator) {
                    creator.open();
                } else {
                    player.openInventory(previousGUI.getInventory());
                }
            } else {
                new AdminPresetsGUI(plugin, player, null).open();
            }
            return;
        }

        // Slot 47: Hapus Preset (Shift + Right Click)
        if (slot == 47) {
            if (event.getClick() == ClickType.SHIFT_RIGHT) {
                boolean deleted = plugin.getPresetManager().deletePreset(preset.id());
                if (deleted) {
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red>Preset </red>")
                            .append(ColorUtil.parse(preset.displayName()))
                            .append(mm.deserialize("<red> berhasil dihapus!</red>")));
                    if (previousGUI instanceof AdminPresetsGUI presetsGUI) {
                        presetsGUI.open();
                    } else {
                        new AdminPresetsGUI(plugin, player, previousGUI).open();
                    }
                }
            } else {
                player.sendMessage(mm.deserialize("<yellow>Gunakan <red><bold>Shift + Klik Kanan</bold></red> untuk menghapus preset ini!</yellow>"));
            }
            return;
        }

        // Slot 49: Ambil Seluruh Set Item
        if (slot == 49) {
            giveAllItems();
            return;
        }

        // Slot 53: Tutup
        if (slot == 53) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            player.closeInventory();
            return;
        }

        // Klik salah satu item pratinjau (Slot 10..13 atau 28..34)
        if (previewItemMap.containsKey(slot)) {
            ItemStack is = previewItemMap.get(slot);
            if (is != null && !is.getType().isAir()) {
                ItemStack toGive = plugin.getEnchantmentRegistry().updateLoreAndGlint(is.clone());
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(toGive);
                if (!overflow.isEmpty()) {
                    for (ItemStack drop : overflow.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    }
                }
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
                Component itemName = (toGive.hasItemMeta() && toGive.getItemMeta().hasDisplayName())
                        ? toGive.getItemMeta().displayName()
                        : Component.text(toGive.getType().name());
                player.sendMessage(mm.deserialize("<green>✓ Mengambil 1x </green>")
                        .append(itemName)
                        .append(mm.deserialize("<green> ke inventarismu!</green>")));
            }
        }
    }

    private void giveAllItems() {
        int totalGiven = 0;
        List<ItemStack> allItems = new ArrayList<>();
        for (ItemStack is : preset.armorPieces()) {
            if (is != null && !is.getType().isAir()) {
                allItems.add(plugin.getEnchantmentRegistry().updateLoreAndGlint(is.clone()));
            }
        }
        for (ItemStack is : preset.toolPieces()) {
            if (is != null && !is.getType().isAir()) {
                allItems.add(plugin.getEnchantmentRegistry().updateLoreAndGlint(is.clone()));
            }
        }

        if (allItems.isEmpty()) {
            player.sendMessage(mm.deserialize("<red>Preset ini tidak memiliki item untuk diambil!</red>"));
            return;
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
        player.sendMessage(mm.deserialize("<green><bold>✓ BERHASIL!</bold> Mengambil seluruh <yellow>" + totalGiven + " item</yellow> dari preset </green>")
                .append(ColorUtil.parse(preset.displayName()))
                .append(mm.deserialize("<green>!</green>")));
    }

    private ItemStack createGlass(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.parse(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material mat, String name, List<Component> lore, boolean glow) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.parse(name));
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
