package com.apexsions.fishing.gui.dialog;

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
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Native in-game GUI numeric adjuster for ApexsionsFishing.
 * Used as a 100% native GUI alternative to chat input when modal dialogs are unavailable or bypassed.
 */
public class NumericAdjusterGUI implements InventoryHolder {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    private final Plugin plugin;
    private final Player player;
    private final String title;
    private final String prompt;
    private final int defaultValue;
    private final int min;
    private final int max;
    private final Consumer<Integer> onNumber;
    private final Runnable onCancel;
    private final Inventory inventory;

    private int currentValue;

    public NumericAdjusterGUI(Plugin plugin, Player player, String title, String prompt,
                              int defaultValue, int min, int max,
                              Consumer<Integer> onNumber, Runnable onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.title = title != null ? title : "Pengatur Angka";
        this.prompt = prompt != null ? prompt : "Sesuaikan nilai numerik:";
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.currentValue = Math.clamp(defaultValue, min, max);
        this.onNumber = onNumber;
        this.onCancel = onCancel;

        Component guiTitle = mm.deserialize("<gradient:#00c6ff:#0072ff><bold>" + this.title + "</bold></gradient>");
        this.inventory = Bukkit.createInventory(this, 27, guiTitle);
        render();
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void render() {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>", null);
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        // Slot 4: Info Item
        inventory.setItem(4, createItem(Material.KNOWLEDGE_BOOK,
                "<yellow><bold>" + title + "</bold></yellow>",
                List.of(
                        "<gray>" + prompt + "</gray>",
                        "",
                        "<dark_gray>●</dark_gray> <gray>Batas Minimal: <white>" + min + "</white></gray>",
                        "<dark_gray>●</dark_gray> <gray>Batas Maksimal: <white>" + max + "</white></gray>",
                        "<dark_gray>●</dark_gray> <gray>Nilai Bawaan: <gold>" + defaultValue + "</gold></gray>"
                )));

        // Decrement buttons
        inventory.setItem(10, createItem(Material.RED_TERRACOTTA, "<red><bold>-100</bold></red>", List.of("<gray>Kurangi nilai sebesar 100</gray>")));
        inventory.setItem(11, createItem(Material.ORANGE_TERRACOTTA, "<gold><bold>-10</bold></gold>", List.of("<gray>Kurangi nilai sebesar 10</gray>")));
        inventory.setItem(12, createItem(Material.YELLOW_TERRACOTTA, "<yellow><bold>-1</bold></yellow>", List.of("<gray>Kurangi nilai sebesar 1</gray>")));

        // Slot 13: Current Value Display
        inventory.setItem(13, createItem(Material.EMERALD_BLOCK,
                "<green><bold>NILAI SAAT INI</bold></green>",
                List.of(
                        "",
                        "<white><bold>  ▶  " + currentValue + "  ◀</bold></white>",
                        "",
                        "<yellow>Klik tombol di kiri/kanan untuk menyesuaikan nilai.</yellow>"
                )));

        // Increment buttons
        inventory.setItem(14, createItem(Material.LIME_TERRACOTTA, "<green><bold>+1</bold></green>", List.of("<gray>Tambahkan nilai sebesar 1</gray>")));
        inventory.setItem(15, createItem(Material.GREEN_TERRACOTTA, "<dark_green><bold>+10</bold></dark_green>", List.of("<gray>Tambahkan nilai sebesar 10</gray>")));
        inventory.setItem(16, createItem(Material.CYAN_TERRACOTTA, "<aqua><bold>+100</bold></aqua>", List.of("<gray>Tambahkan nilai sebesar 100</gray>")));

        // Bottom control buttons
        inventory.setItem(20, createItem(Material.BARRIER, "<red><bold>✖ BATALKAN</bold></red>", List.of("<gray>Batalkan dan kembali ke menu pembuat.</gray>")));
        inventory.setItem(22, createItem(Material.CLOCK, "<yellow><bold>↺ RESET DEFAULT</bold></yellow>", List.of("<gray>Kembalikan nilai ke: <gold>" + defaultValue + "</gold></gray>")));
        inventory.setItem(24, createItem(Material.NETHER_STAR, "<green><bold>✔ TERAPKAN NILAI</bold></green>", List.of("<gray>Simpan nilai <green>" + currentValue + "</green> dan kembali.</gray>")));
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        switch (slot) {
            case 10 -> adjust(-100);
            case 11 -> adjust(-10);
            case 12 -> adjust(-1);
            case 14 -> adjust(1);
            case 15 -> adjust(10);
            case 16 -> adjust(100);
            case 20 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
                player.closeInventory();
                if (onCancel != null) {
                    Bukkit.getScheduler().runTask(plugin, onCancel);
                }
            }
            case 22 -> {
                currentValue = Math.clamp(defaultValue, min, max);
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.2f);
                render();
            }
            case 24 -> {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.4f);
                player.closeInventory();
                if (onNumber != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> onNumber.accept(currentValue));
                }
            }
        }
    }

    private void adjust(int delta) {
        long target = (long) currentValue + delta;
        if (target < min) target = min;
        if (target > max) target = max;
        currentValue = (int) target;

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, delta > 0 ? 1.4f : 0.8f);
        render();
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            if (lore != null && !lore.isEmpty()) {
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
