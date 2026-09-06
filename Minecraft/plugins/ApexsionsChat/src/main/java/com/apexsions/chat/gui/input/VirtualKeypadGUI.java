package com.apexsions.chat.gui.input;

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
        this.title = title != null ? title : "INPUT FORM";
        this.prompt = prompt != null ? prompt : "Ketik pesanmu:";
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

    private void buildGUI() {
        inventory.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, border);
        }

        // Slot 4: Display Screen
        String currentText = buffer.toString();
        List<String> displayLore = new ArrayList<>();
        displayLore.add("<gray>" + prompt + "</gray>");
        displayLore.add("");
        displayLore.add("<dark_gray>Teks saat ini:</dark_gray>");
        displayLore.add("<white><bold>" + (currentText.isEmpty() ? "<dark_gray>(Kosong)</dark_gray>" : currentText) + "</bold></white>");
        displayLore.add("");
        displayLore.add("<yellow>Gunakan keypad angka/huruf di bawah untuk mengetik.</yellow>");

        inventory.setItem(4, createItem(Material.PAPER, "<gold><bold>🖥 INPUT DISPLAY</bold></gold>", displayLore));

        // Digits 1-9
        int[] numSlots = {19, 20, 21, 28, 29, 30, 37, 38, 39};
        for (int i = 0; i < 9; i++) {
            int digit = i + 1;
            inventory.setItem(numSlots[i], createItem(Material.LIGHT_GRAY_CONCRETE, "<yellow><bold>" + digit + "</bold></yellow>",
                    List.of("<gray>Klik untuk mengetik angka " + digit + "</gray>")));
        }

        // Digit 0
        inventory.setItem(47, createItem(Material.LIGHT_GRAY_CONCRETE, "<yellow><bold>0</bold></yellow>",
                List.of("<gray>Klik untuk mengetik angka 0</gray>")));

        // Slot 46: Spacebar
        inventory.setItem(46, createItem(Material.WHITE_CONCRETE, "<aqua><bold>[ SPASI ]</bold></aqua>",
                List.of("<gray>Klik untuk menambahkan spasi</gray>")));

        // Slot 48: Backspace
        inventory.setItem(48, createItem(Material.ORANGE_CONCRETE, "<gold><bold>⌫ HAPUS (BACKSPACE)</bold></gold>",
                List.of("<gray>Hapus karakter terakhir</gray>")));

        // Slot 23: Clear All
        inventory.setItem(23, createItem(Material.RED_CONCRETE, "<red><bold>✖ BERSIHKAN SEMUA</bold></red>",
                List.of("<gray>Hapus seluruh teks input</gray>")));

        // Quick add numbers
        inventory.setItem(25, createItem(Material.GOLD_NUGGET, "<yellow><bold>+1.000</bold></yellow>",
                List.of("<gray>Tambahkan nominal seribu</gray>")));
        inventory.setItem(34, createItem(Material.GOLD_INGOT, "<gold><bold>+10.000</bold></gold>",
                List.of("<gray>Tambahkan nominal sepuluh ribu</gray>")));
        inventory.setItem(43, createItem(Material.DIAMOND, "<aqua><bold>+100.000</bold></aqua>",
                List.of("<gray>Tambahkan nominal seratus ribu</gray>")));

        // Slot 45: Cancel
        inventory.setItem(45, createItem(Material.BARRIER, "<red><bold>✖ BATAL</bold></red>",
                List.of("<gray>Tutup tanpa mengirim</gray>")));

        // Slot 53: Submit
        inventory.setItem(53, createItem(Material.EMERALD_BLOCK, "<green><bold>✔ SELESAI & KIRIM</bold></green>",
                List.of("<gray>Kirim teks input saat ini</gray>")));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;
        e.setCancelled(true);

        int slot = e.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        // Digits 1-9
        int[] numSlots = {19, 20, 21, 28, 29, 30, 37, 38, 39};
        for (int i = 0; i < 9; i++) {
            if (slot == numSlots[i]) {
                buffer.append(i + 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                buildGUI();
                return;
            }
        }

        // Digit 0
        if (slot == 47) {
            buffer.append(0);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            buildGUI();
            return;
        }

        // Spacebar
        if (slot == 46) {
            buffer.append(' ');
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            buildGUI();
            return;
        }

        // Backspace
        if (slot == 48) {
            if (!buffer.isEmpty()) {
                buffer.deleteCharAt(buffer.length() - 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 0.9f);
                buildGUI();
            }
            return;
        }

        // Clear All
        if (slot == 23) {
            buffer.setLength(0);
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.8f, 1.0f);
            buildGUI();
            return;
        }

        // Quick add numbers
        if (slot == 25 || slot == 34 || slot == 43) {
            long add = slot == 25 ? 1000 : (slot == 34 ? 10000 : 100000);
            long cur = 0;
            try {
                cur = Long.parseLong(buffer.toString().trim());
            } catch (Exception ignored) {}
            cur += add;
            buffer.setLength(0);
            buffer.append(cur);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
            buildGUI();
            return;
        }

        // Cancel
        if (slot == 45) {
            this.submitted = true;
            player.closeInventory();
            if (onCancel != null) onCancel.run();
            return;
        }

        // Submit
        if (slot == 53) {
            this.submitted = true;
            player.closeInventory();
            String res = buffer.toString().trim();
            if (onInput != null) {
                onInput.accept(res);
            }
            return;
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() != this) return;
        org.bukkit.event.HandlerList.unregisterAll(this);
        if (!submitted && onCancel != null) {
            Bukkit.getScheduler().runTask(plugin, onCancel);
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
