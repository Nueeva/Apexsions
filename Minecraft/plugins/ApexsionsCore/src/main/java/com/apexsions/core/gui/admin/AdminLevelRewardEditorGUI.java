package com.apexsions.core.gui.admin;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.level.reward.Reward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminLevelRewardEditorGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final int level;
    private final int returnPage;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private static final int[] CENTER_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public AdminLevelRewardEditorGUI(ApexsionsCorePlugin plugin, Player player, int level, int returnPage) {
        this.plugin = plugin;
        this.player = player;
        this.level = level;
        this.returnPage = returnPage;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#c0392b:#e74c3c><bold>⚙ EDITOR HADIAH: LEVEL " + level + " ⚙</bold></gradient>"));
        build();
    }

    public void build() {
        inventory.clear();

        // 1. Fill border
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<gray> </gray>", List.of());
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        Optional<Reward> rewardOpt = plugin.getRewardManager().getReward(level);
        List<ItemStack> items = rewardOpt.map(r -> new ArrayList<>(r.getItems())).orElseGet(ArrayList::new);
        List<String> commands = rewardOpt.map(Reward::getCommands).orElseGet(List::of);
        long reqXp = plugin.getLevelFormula().getXpForLevel(level);

        // Header (Row 0)
        // Slot 0: Level Info
        inventory.setItem(0, createItem(Material.EXPERIENCE_BOTTLE, "<gold><bold>LEVEL " + level + "</bold></gold>", List.of(
                mm.deserialize("<gray>Required XP: <yellow>" + (reqXp == Long.MAX_VALUE ? "MAX" : reqXp + " XP") + "</yellow></gray>"),
                mm.deserialize("<gray>Milestone: " + (((level % 10 == 1 && level > 1) || level == 100) ? "<gold>Ya</gold>" : "<aqua>Tidak</aqua>") + "</gray>")
        )));

        // Slot 4: Drag & Drop Helper
        inventory.setItem(4, createItem(Material.HOPPER, "<gradient:#2ecc71:#27ae60><bold>[💡] DRAG & DROP ITEM KE SINI</bold></gradient>", List.of(
                mm.deserialize("<gray>Seret item dari inventory Anda ke slot kosong di tengah.</gray>"),
                mm.deserialize("<gray>● <white>Drag & Drop</white> item ke slot kosong untuk menambahkan.</gray>"),
                mm.deserialize("<gray>● Atau <white>Shift-Klik</white> item di inventory Anda.</gray>"),
                mm.deserialize("<gray>● <yellow>Cek Stackable:</yellow> Senjata/armor dikunci 1x otomatis.</gray>"),
                mm.deserialize("<gray>● <yellow>Klik Kiri</yellow> item stackable untuk menambah (+1).</gray>"),
                mm.deserialize("<gray>● <red>Klik Kanan</red> item yang sudah ada untuk menghapusnya.</gray>")
        )));

        // Slot 8: Summary
        inventory.setItem(8, createItem(Material.BEACON, "<yellow><bold>TOTAL HADIAH: " + (items.size() + commands.size()) + "</bold></yellow>", List.of(
                mm.deserialize("<gray>Item Fisik: <green>" + items.size() + " Item</green></gray>"),
                mm.deserialize("<gray>Perintah: <light_purple>" + commands.size() + " Command</light_purple></gray>")
        )));

        // 2. Render Existing Items in Center Slots with BattlePass stackable checks
        int idx = 0;
        for (int i = 0; i < items.size() && idx < CENTER_SLOTS.length; i++) {
            ItemStack is = items.get(i);
            if (is == null || is.getType().isAir()) continue;

            boolean isStackable = is.getMaxStackSize() > 1;

            ItemStack display = is.clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                lore.add(mm.deserialize(""));
                lore.add(mm.deserialize("<gray>Jumlah: <green>" + is.getAmount() + "x</green></gray>"));
                lore.add(mm.deserialize("<gray>Stackable: " + (isStackable ? "<green>Ya (Maks " + is.getMaxStackSize() + ")</green>" : "<red>Tidak (Maks 1)</red>") + "</gray>"));
                lore.add(mm.deserialize(""));
                if (isStackable) {
                    lore.add(mm.deserialize("<yellow><bold>[KLIK KIRI]</bold></yellow> <gray>Tambah jumlah (+1)</gray>"));
                } else {
                    lore.add(mm.deserialize("<dark_gray><bold>[KLIK KIRI]</bold> Terkunci x1 (Non-stackable)</dark_gray>"));
                }
                lore.add(mm.deserialize("<red><bold>[KLIK KANAN]</bold></red> <gray>Hapus item ini dari hadiah</gray>"));
                meta.lore(lore);
                display.setItemMeta(meta);
            }
            inventory.setItem(CENTER_SLOTS[idx++], display);
        }

        // Remaining center slots remain AIR so player can drop items into them

        // 3. Navigation Controls (Row 5)
        // Slot 45: Back
        inventory.setItem(45, createItem(Material.ARROW, "<gradient:#3498db:#2980b9><bold>⬅ KEMBALI KE DAFTAR LEVEL</bold></gradient>", List.of(
                mm.deserialize("<gray>Kembali ke daftar level.</gray>")
        )));

        // Slot 49: Commands info
        List<Component> cmdLore = new ArrayList<>();
        cmdLore.add(mm.deserialize("<gray>Perintah konsol saat level ini diklaim:</gray>"));
        if (commands.isEmpty()) {
            cmdLore.add(mm.deserialize("<dark_gray><i>Tidak ada perintah khusus.</i></dark_gray>"));
        } else {
            for (String cmd : commands) {
                cmdLore.add(mm.deserialize(" <gold>•</gold> <white>" + cmd + "</white>"));
            }
        }
        inventory.setItem(49, createItem(Material.COMMAND_BLOCK, "<light_purple><bold>Daftar Perintah Hadiah</bold></light_purple>", cmdLore));

        // Slot 53: Close
        inventory.setItem(53, createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>", List.of(
                mm.deserialize("<gray>Tutup menu editor.</gray>")
        )));
    }

    public void handleClick(InventoryClickEvent e) {
        int rawSlot = e.getRawSlot();

        // 1. Check Top & Bottom Border Controls
        if (rawSlot == 53) {
            e.setCancelled(true);
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }

        if (rawSlot == 45) {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            player.openInventory(new AdminLevelRewardListGUI(plugin, player, returnPage).getInventory());
            return;
        }

        if (rawSlot == 0 || rawSlot == 4 || rawSlot == 8 || rawSlot == 49 || isBorderSlot(rawSlot)) {
            e.setCancelled(true);
            return;
        }

        // 2. Shift-Clicking from Player Inventory to Add Item
        if (e.isShiftClick() && e.getClickedInventory() == player.getInventory()) {
            e.setCancelled(true);
            ItemStack clicked = e.getCurrentItem();
            if (clicked != null && !clicked.getType().isAir()) {
                addItem(clicked.clone());
            }
            return;
        }

        // 3. Clicking Center Slots in the Editor Inventory
        if (rawSlot >= 0 && rawSlot < 54 && isCenterSlot(rawSlot)) {
            e.setCancelled(true);
            ItemStack cursor = e.getCursor();
            ItemStack current = e.getCurrentItem();

            int centerIndex = getCenterIndex(rawSlot);
            Optional<Reward> rewardOpt = plugin.getRewardManager().getReward(level);
            List<ItemStack> items = rewardOpt.map(r -> new ArrayList<>(r.getItems())).orElseGet(ArrayList::new);

            // A. Placing new item from cursor into empty slot
            if (current == null || current.getType().isAir()) {
                if (cursor != null && !cursor.getType().isAir()) {
                    addItem(cursor.clone());
                    e.getView().setCursor(new ItemStack(Material.AIR));
                }
                return;
            }

            // B. Interacting with existing reward item
            if (centerIndex >= 0 && centerIndex < items.size()) {
                if (e.isRightClick()) {
                    // Remove item
                    plugin.getRewardManager().removeItemFromReward(level, centerIndex);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<yellow><b>[Reward Editor]</b> Item berhasil dihapus dari hadiah Level " + level + ".</yellow>"));
                    build();
                } else if (e.isLeftClick()) {
                    // Check if item is stackable before increasing amount
                    ItemStack existing = items.get(centerIndex);
                    boolean isStackable = existing.getMaxStackSize() > 1;

                    if (!isStackable) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        player.sendMessage(mm.deserialize("<red><b>[Reward Editor]</b> Item ini tidak dapat di-stack (alat/armor/senjata), jumlah terkunci pada 1x!</red>"));
                        return;
                    }

                    if (existing.getAmount() >= existing.getMaxStackSize()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        player.sendMessage(mm.deserialize("<yellow><b>[Reward Editor]</b> Item sudah mencapai batas maksimum stack (" + existing.getMaxStackSize() + ").</yellow>"));
                        return;
                    }

                    existing.setAmount(existing.getAmount() + 1);
                    plugin.getRewardManager().saveRewardItems(level, items);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                    build();
                }
            }
        }
    }

    private void addItem(ItemStack item) {
        boolean isStackable = item.getMaxStackSize() > 1;
        if (!isStackable) {
            item.setAmount(1);
            player.sendMessage(mm.deserialize("<yellow><b>[Reward Editor]</b> Item non-stackable terdeteksi (seperti alat/senjata/armor). Jumlah otomatis dikunci ke 1x.</yellow>"));
        } else {
            item.setAmount(Math.min(item.getAmount(), item.getMaxStackSize()));
        }

        plugin.getRewardManager().addItemToReward(level, item);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.5f);
        player.sendMessage(mm.deserialize("<green><b>[Reward Editor]</b> Berhasil menambahkan <white>" + item.getAmount() + "x " + item.getType().name() + "</white> ke hadiah Level " + level + "!</green>"));
        build();
    }

    private boolean isBorderSlot(int slot) {
        if (slot < 0 || slot >= 54) return false;
        return (slot < 9 || slot >= 45 || slot % 9 == 0 || slot % 9 == 8);
    }

    private boolean isCenterSlot(int slot) {
        for (int s : CENTER_SLOTS) {
            if (s == slot) return true;
        }
        return false;
    }

    private int getCenterIndex(int slot) {
        for (int i = 0; i < CENTER_SLOTS.length; i++) {
            if (CENTER_SLOTS[i] == slot) return i;
        }
        return -1;
    }

    private ItemStack createItem(Material mat, String name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
