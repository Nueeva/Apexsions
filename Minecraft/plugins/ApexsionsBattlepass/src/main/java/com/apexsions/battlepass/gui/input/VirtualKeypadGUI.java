package com.apexsions.battlepass.gui.input;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class VirtualKeypadGUI implements InventoryHolder, Listener {

    private final Plugin plugin;
    private final Player player;
    private final String title;
    private final String prompt;
    private final boolean numericOnly;
    private final Consumer<String> onInput;
    private final Runnable onCancel;
    private final Inventory inventory;

    private final StringBuilder buffer = new StringBuilder();
    private boolean submitted = false;

    public VirtualKeypadGUI(Plugin plugin, Player player, String title, String prompt, String defaultText,
                            boolean numericOnly, Consumer<String> onInput, Runnable onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.title = title != null ? title : "BATTLEPASS INPUT";
        this.prompt = prompt != null ? prompt : "Masukkan nilai:";
        this.numericOnly = numericOnly;
        this.onInput = onInput;
        this.onCancel = onCancel;

        if (defaultText != null && !defaultText.isBlank()) {
            this.buffer.append(defaultText.trim());
        }

        String safeTitle = ChatColor.translateAlternateColorCodes('&', this.title);
        if (safeTitle.length() > 32) safeTitle = safeTitle.substring(0, 32);
        this.inventory = Bukkit.createInventory(this, 54, safeTitle);
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

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 9; i++) inventory.setItem(i, border);
        for (int i = 45; i < 54; i++) inventory.setItem(i, border);

        String displayContent = buffer.isEmpty() ? "§8[ Kolom Kosong ]" : "§e§l" + buffer;
        List<String> headerLore = new ArrayList<>();
        headerLore.add("§7Petunjuk: §f" + prompt);
        headerLore.add("");
        headerLore.add("§7Nilai Saat Ini: " + displayContent);
        headerLore.add("§7Panjang Karakter: §6" + buffer.length());
        headerLore.add("");
        headerLore.add("§a▶ Klik tombol di bawah untuk mengetik / mengubah nilai.");
        headerLore.add("§b▶ Klik tombol centang hijau jika sudah selesai.");

        inventory.setItem(4, createItem(Material.NAME_TAG, "§6§lFORM INPUT BATTLEPASS", headerLore));

        if (numericOnly) {
            buildNumericLayout();
        } else {
            buildAlphanumericLayout();
        }

        inventory.setItem(46, createItem(Material.RED_CONCRETE, "§c§lBATAL", List.of("§7Batal dan kembali ke menu sebelumnya.")));
        inventory.setItem(48, createItem(Material.ORANGE_CONCRETE, "§6§lHAPUS (BACKSPACE)", List.of("§7Hapus karakter terakhir.")));
        inventory.setItem(49, createItem(Material.BARRIER, "§e§lBERSIHKAN SEMUA", List.of("§7Hapus seluruh teks.")));

        if (!numericOnly) {
            inventory.setItem(50, createItem(Material.WHITE_CARPET, "§f§lSPASI", List.of("§7Tambahkan spasi.")));
        }

        inventory.setItem(52, createItem(Material.LIME_CONCRETE, "§a§lSELESAI (OK)", List.of("§7Kirim dan proses nilai.")));
    }

    private void buildNumericLayout() {
        int[] numSlots = {12, 13, 14, 21, 22, 23, 30, 31, 32};
        for (int i = 0; i < 9; i++) {
            char digit = (char) ('1' + i);
            inventory.setItem(numSlots[i], createItem(Material.LIGHT_BLUE_CONCRETE, "§b§l" + digit, List.of("§7Ketik " + digit)));
        }
        inventory.setItem(39, createItem(Material.LIGHT_BLUE_CONCRETE, "§b§l0", List.of("§7Ketik 0")));
        inventory.setItem(40, createItem(Material.CYAN_CONCRETE, "§3§l.", List.of("§7Koma desimal")));

        inventory.setItem(16, createItem(Material.GOLD_NUGGET, "§e§l+100", List.of("§7Tambah +100")));
        inventory.setItem(25, createItem(Material.GOLD_INGOT, "§6§l+1.000 (1k)", List.of("§7Tambah +1.000")));
        inventory.setItem(34, createItem(Material.GOLD_BLOCK, "§6§l+10.000 (10k)", List.of("§7Tambah +10.000")));
        inventory.setItem(43, createItem(Material.EMERALD, "§a§l+100.000 (100k)", List.of("§7Tambah +100.000")));

        inventory.setItem(10, createItem(Material.IRON_NUGGET, "§f§l+1", List.of("§7Tambah +1")));
        inventory.setItem(19, createItem(Material.IRON_INGOT, "§f§l+10", List.of("§7Tambah +10")));
        inventory.setItem(28, createItem(Material.DIAMOND, "§b§l+1.000.000 (1m)", List.of("§7Tambah +1 Juta")));
    }

    private void buildAlphanumericLayout() {
        String keys = "1234567890QWERTYUIOPASDFGHJKLZXCVBNM_";
        int[] keySlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        for (int i = 0; i < keySlots.length && i < keys.length(); i++) {
            char c = keys.charAt(i);
            Material mat = Character.isDigit(c) ? Material.CYAN_TERRACOTTA : Material.LIGHT_GRAY_CONCRETE;
            inventory.setItem(keySlots[i], createItem(mat, "§e§l" + c, List.of("§7Ketik " + c)));
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

        if (slot == 46) {
            submitted = true;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 0.8f);
            player.closeInventory();
            if (onCancel != null) onCancel.run();
            return;
        }

        if (slot == 48) {
            if (!buffer.isEmpty()) {
                buffer.deleteCharAt(buffer.length() - 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.3f);
                buildGUI();
            }
            return;
        }

        if (slot == 49) {
            if (!buffer.isEmpty()) {
                buffer.setLength(0);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
                buildGUI();
            }
            return;
        }

        if (slot == 50 && !numericOnly) {
            buffer.append(' ');
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.5f);
            buildGUI();
            return;
        }

        if (slot == 52) {
            submitted = true;
            String result = buffer.toString().trim();
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
            player.closeInventory();
            try {
                onInput.accept(result);
            } catch (Exception e) {
                player.sendMessage("§cError: " + e.getMessage());
                if (onCancel != null) onCancel.run();
            }
            return;
        }

        if (numericOnly) {
            if (slot == 10) { addNumeric(1); return; }
            if (slot == 19) { addNumeric(10); return; }
            if (slot == 16) { addNumeric(100); return; }
            if (slot == 25) { addNumeric(1000); return; }
            if (slot == 34) { addNumeric(10000); return; }
            if (slot == 43) { addNumeric(100000); return; }
            if (slot == 28) { addNumeric(1000000); return; }
        }

        ItemMeta meta = clicked.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            String stripped = ChatColor.stripColor(meta.getDisplayName()).trim();
            if (stripped.length() == 1) {
                char c = stripped.charAt(0);
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
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
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
