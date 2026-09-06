package com.apexsions.core.gui.input;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Universal interactive GUI Keyboard & Keypad for player text/numeric input.
 * Ensures player NEVER has to type in open chat, providing a rich, responsive interface.
 */
public class VirtualKeypadGUI implements InventoryHolder, Listener {

    private final Plugin plugin;
    private final Player player;
    private final String title;
    private final String prompt;
    private final boolean numericOnly;
    private final Consumer<String> onInput;
    private final Runnable onCancel;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final StringBuilder buffer = new StringBuilder();
    private boolean submitted = false;

    public VirtualKeypadGUI(Plugin plugin, Player player, String title, String prompt, String defaultText,
                            boolean numericOnly, Consumer<String> onInput, Runnable onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.title = title != null ? title : "INPUT FORM";
        this.prompt = prompt != null ? prompt : "Masukkan nilai:";
        this.numericOnly = numericOnly;
        this.onInput = onInput;
        this.onCancel = onCancel;

        if (defaultText != null && !defaultText.isBlank()) {
            this.buffer.append(defaultText.trim());
        }

        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#f39c12:#d35400><bold>" + this.title + "</bold></gradient>"));
        Bukkit.getPluginManager().registerEvents(this, plugin);
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 9; i++) inventory.setItem(i, border);
        for (int i = 45; i < 54; i++) inventory.setItem(i, border);

        // Header Slot 4: Current Typed Text Display
        String displayContent = buffer.isEmpty() ? "<dark_gray>[ Kolom Kosong ]</dark_gray>" : "<yellow><bold>" + buffer + "</bold></yellow>";
        List<String> headerLore = new ArrayList<>();
        headerLore.add("<gray>Petunjuk: <white>" + prompt + "</white></gray>");
        headerLore.add("");
        headerLore.add("<gray>Teks Saat Ini: " + displayContent);
        headerLore.add("<gray>Panjang Karakter: <gold>" + buffer.length() + "</gold></gray>");
        headerLore.add("");
        headerLore.add("<green>▶ Tekan tombol di bawah untuk mengetik / mengubah nilai.</green>");
        headerLore.add("<aqua>▶ Klik tombol centang hijau jika sudah selesai.</aqua>");

        inventory.setItem(4, createItem(Material.NAME_TAG, "<gradient:#f1c40f:#2ecc71><bold>📋 FORM INPUT TEKS</bold></gradient>", headerLore));

        if (numericOnly) {
            buildNumericLayout();
        } else {
            buildAlphanumericLayout();
        }

        // Bottom Controls
        // Slot 46: ❌ Cancel
        inventory.setItem(46, createItem(Material.RED_CONCRETE, "<red><bold>❌ BATAL</bold></red>",
                List.of("<gray>Batal dan kembali ke menu sebelumnya.</gray>")));

        // Slot 48: ⬅ Backspace
        inventory.setItem(48, createItem(Material.ORANGE_CONCRETE, "<gold><bold>⬅ HAPUS (BACKSPACE)</bold></gold>",
                List.of("<gray>Hapus karakter terakhir yang diketik.</gray>")));

        // Slot 49: 🗑 Clear All
        inventory.setItem(49, createItem(Material.BARRIER, "<yellow><bold>🗑 BERSIHKAN SEMUA</bold></yellow>",
                List.of("<gray>Hapus seluruh teks di kolom input.</gray>")));

        // Slot 50: ␣ Space (only for text)
        if (!numericOnly) {
            inventory.setItem(50, createItem(Material.WHITE_CARPET, "<white><bold>␣ SPASI</bold></white>",
                    List.of("<gray>Tambahkan spasi.</gray>")));
        }

        // Slot 52: ✔ Confirm / OK
        inventory.setItem(52, createItem(Material.LIME_CONCRETE, "<green><bold>✔ SELESAI (OK)</bold></green>",
                List.of("<gray>Kirim dan proses nilai yang sudah diketik.</gray>")));
    }

    private void buildNumericLayout() {
        // Numpad 1-9 in slots 12-14, 21-23, 30-32, and 0 in slot 39
        int[] numSlots = {12, 13, 14, 21, 22, 23, 30, 31, 32};
        for (int i = 0; i < 9; i++) {
            char digit = (char) ('1' + i);
            inventory.setItem(numSlots[i], createItem(Material.LIGHT_BLUE_CONCRETE, "<aqua><bold>" + digit + "</bold></aqua>", List.of("<gray>Klik untuk mengetik angka <white>" + digit + "</white></gray>")));
        }
        inventory.setItem(39, createItem(Material.LIGHT_BLUE_CONCRETE, "<aqua><bold>0</bold></aqua>", List.of("<gray>Klik untuk mengetik angka <white>0</white></gray>")));
        inventory.setItem(40, createItem(Material.CYAN_CONCRETE, "<aqua><bold>.</bold></aqua>", List.of("<gray>Koma desimal</gray>")));

        // Quick Multipliers on right side
        inventory.setItem(16, createItem(Material.GOLD_NUGGET, "<yellow><bold>+100</bold></yellow>", List.of("<gray>Tambah +100</gray>")));
        inventory.setItem(25, createItem(Material.GOLD_INGOT, "<gold><bold>+1.000 (1k)</bold></gold>", List.of("<gray>Tambah +1.000</gray>")));
        inventory.setItem(34, createItem(Material.GOLD_BLOCK, "<gradient:#f39c12:#d35400><bold>+10.000 (10k)</bold></gradient>", List.of("<gray>Tambah +10.000</gray>")));
        inventory.setItem(43, createItem(Material.EMERALD, "<green><bold>+100.000 (100k)</bold></green>", List.of("<gray>Tambah +100.000</gray>")));

        // Left quick presets
        inventory.setItem(10, createItem(Material.IRON_NUGGET, "<white><bold>+1</bold></white>", List.of("<gray>Tambah +1</gray>")));
        inventory.setItem(19, createItem(Material.IRON_INGOT, "<white><bold>+10</bold></white>", List.of("<gray>Tambah +10</gray>")));
        inventory.setItem(28, createItem(Material.DIAMOND, "<aqua><bold>+1.000.000 (1m)</bold></aqua>", List.of("<gray>Tambah +1 Juta</gray>")));
    }

    private void buildAlphanumericLayout() {
        // Row 2 & 3: Letters A-Z & Digits 0-9
        String keys = "1234567890QWERTYUIOPASDFGHJKLZXCVBNM_";
        int[] keySlots = {
                10, 11, 12, 13, 14, 15, 16, // 1-7
                19, 20, 21, 22, 23, 24, 25, // 8, 9, 0, Q, W, E, R
                28, 29, 30, 31, 32, 33, 34, // T, Y, U, I, O, P, A
                37, 38, 39, 40, 41, 42, 43  // S, D, F, G, H, J, K...
        };

        for (int i = 0; i < keySlots.length && i < keys.length(); i++) {
            char c = keys.charAt(i);
            Material mat = Character.isDigit(c) ? Material.CYAN_TERRACOTTA : Material.LIGHT_GRAY_CONCRETE;
            inventory.setItem(keySlots[i], createItem(mat, "<yellow><bold>" + c + "</bold></yellow>", List.of("<gray>Klik untuk mengetik <white>" + c + "</white></gray>")));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.equals(player)) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        ItemStack clicked = inventory.getItem(slot);
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        // Slot 46: Cancel
        if (slot == 46) {
            submitted = true;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 0.8f);
            player.closeInventory();
            if (onCancel != null) onCancel.run();
            return;
        }

        // Slot 48: Backspace
        if (slot == 48) {
            if (!buffer.isEmpty()) {
                buffer.deleteCharAt(buffer.length() - 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.3f);
                buildGUI();
            }
            return;
        }

        // Slot 49: Clear All
        if (slot == 49) {
            if (!buffer.isEmpty()) {
                buffer.setLength(0);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
                buildGUI();
            }
            return;
        }

        // Slot 50: Space
        if (slot == 50 && !numericOnly) {
            buffer.append(' ');
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.5f);
            buildGUI();
            return;
        }

        // Slot 52: Confirm / Submit
        if (slot == 52) {
            submitted = true;
            String result = buffer.toString().trim();
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
            player.closeInventory();
            try {
                onInput.accept(result);
            } catch (Exception e) {
                player.sendMessage(mm.deserialize("<red>Error: " + e.getMessage() + "</red>"));
                if (onCancel != null) onCancel.run();
            }
            return;
        }

        // Quick Numeric Buttons
        if (numericOnly) {
            if (slot == 10) { addNumeric(1); return; }
            if (slot == 19) { addNumeric(10); return; }
            if (slot == 16) { addNumeric(100); return; }
            if (slot == 25) { addNumeric(1000); return; }
            if (slot == 34) { addNumeric(10000); return; }
            if (slot == 43) { addNumeric(100000); return; }
            if (slot == 28) { addNumeric(1000000); return; }
        }

        // Character keys
        ItemMeta meta = clicked.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            String plainName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(meta.displayName()).trim();
            if (plainName.length() == 1) {
                char c = plainName.charAt(0);
                buffer.append(c);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.4f);
                buildGUI();
            }
        }
    }

    private void addNumeric(long add) {
        try {
            long current = buffer.isEmpty() ? 0 : Long.parseLong(buffer.toString());
            current += add;
            buffer.setLength(0);
            buffer.append(current);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.4f);
            buildGUI();
        } catch (NumberFormatException ignored) {}
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            org.bukkit.event.HandlerList.unregisterAll(this);
            if (!submitted && onCancel != null) {
                Bukkit.getScheduler().runTask(plugin, onCancel);
            }
        }
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            if (lore != null) {
                List<Component> compLore = new ArrayList<>();
                for (String line : lore) {
                    compLore.add(mm.deserialize(line));
                }
                meta.lore(compLore);
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
