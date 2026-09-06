package com.apexsions.customenchants.gui.input;

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

public class VirtualKeypadGUI implements InventoryHolder, Listener {

    private final Plugin plugin;
    private final Player player;
    private final String title;
    private final String prompt;
    private final Consumer<String> onInput;
    private final Runnable onCancel;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final StringBuilder buffer = new StringBuilder();
    private boolean submitted = false;

    public VirtualKeypadGUI(Plugin plugin, Player player, String title, String prompt, String defaultText,
                            Consumer<String> onInput, Runnable onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.title = title != null ? title : "RENAME ITEM";
        this.prompt = prompt != null ? prompt : "Ketik nama baru:";
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

        String displayContent = buffer.isEmpty() ? "<dark_gray>[ Kolom Kosong ]</dark_gray>" : "<yellow><bold>" + buffer + "</bold></yellow>";
        List<String> headerLore = new ArrayList<>();
        headerLore.add("<gray>Petunjuk: <white>" + prompt + "</white></gray>");
        headerLore.add("");
        headerLore.add("<gray>Nama Saat Ini: " + displayContent);
        headerLore.add("<gray>Panjang Karakter: <gold>" + buffer.length() + "</gold></gray>");
        headerLore.add("<gray>Dukungan: <yellow>&a, &6, &l, atau tag MiniMessage</yellow></gray>");
        headerLore.add("");
        headerLore.add("<green>▶ Tekan tombol di bawah untuk mengetik nama.</green>");
        headerLore.add("<aqua>▶ Klik tombol centang hijau jika sudah selesai.</aqua>");

        inventory.setItem(4, createItem(Material.NAME_TAG, "<gradient:#f1c40f:#2ecc71><bold>🏷 FORM GANTI NAMA ITEM</bold></gradient>", headerLore));

        // Alphanumeric keys
        String keys = "1234567890QWERTYUIOPASDFGHJKLZXCVBNM_&";
        int[] keySlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        for (int i = 0; i < keySlots.length && i < keys.length(); i++) {
            char c = keys.charAt(i);
            Material mat = Character.isDigit(c) ? Material.CYAN_TERRACOTTA : (c == '&' ? Material.EMERALD : Material.LIGHT_GRAY_CONCRETE);
            inventory.setItem(keySlots[i], createItem(mat, "<yellow><bold>" + c + "</bold></yellow>", List.of("<gray>Ketik <white>" + c + "</white></gray>")));
        }

        inventory.setItem(46, createItem(Material.RED_CONCRETE, "<red><bold>❌ BATAL</bold></red>", List.of("<gray>Batal dan kembali ke menu sebelumnya.</gray>")));
        inventory.setItem(48, createItem(Material.ORANGE_CONCRETE, "<gold><bold>⬅ HAPUS (BACKSPACE)</bold></gold>", List.of("<gray>Hapus karakter terakhir.</gray>")));
        inventory.setItem(49, createItem(Material.BARRIER, "<yellow><bold>🗑 BERSIHKAN SEMUA</bold></yellow>", List.of("<gray>Hapus seluruh teks.</gray>")));
        inventory.setItem(50, createItem(Material.WHITE_CARPET, "<white><bold>␣ SPASI</bold></white>", List.of("<gray>Tambahkan spasi.</gray>")));
        inventory.setItem(52, createItem(Material.LIME_CONCRETE, "<green><bold>✔ SELESAI (OK)</bold></green>", List.of("<gray>Kirim dan simpan nama item.</gray>")));
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

        if (slot == 50) {
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
                player.sendMessage(mm.deserialize("<red>Error: " + e.getMessage() + "</red>"));
                if (onCancel != null) onCancel.run();
            }
            return;
        }

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
