package com.apexsions.core.gui.input;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * Native text input fallback using Bukkit's Anvil GUI text field,
 * guaranteeing a real text typing interface without chest keypad slots.
 */
public class AnvilTextInputGUI implements InventoryHolder, Listener {

    private final Plugin plugin;
    private final Player player;
    private final String title;
    private final String prompt;
    private final Consumer<String> onInput;
    private final Runnable onCancel;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private boolean submitted = false;

    public AnvilTextInputGUI(Plugin plugin, Player player, String title, String prompt, String defaultText,
                             Consumer<String> onInput, Runnable onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.title = title != null ? title : "Input Teks";
        this.prompt = prompt != null ? prompt : "Ketik di sini...";
        this.onInput = onInput;
        this.onCancel = onCancel;

        Component titleComp = mm.deserialize("<gradient:#f39c12:#d35400><bold>" + this.title + "</bold></gradient>");
        this.inventory = Bukkit.createInventory(this, InventoryType.ANVIL, titleComp);

        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        if (meta != null) {
            String initialText = (defaultText != null && !defaultText.isBlank()) ? defaultText.trim() : "Ketik teks di sini";
            meta.displayName(Component.text(initialText));
            meta.lore(List.of(
                    mm.deserialize("<gray>" + prompt + "</gray>"),
                    Component.empty(),
                    mm.deserialize("<yellow>▶ Ketik teks pada kolom di atas</yellow>"),
                    mm.deserialize("<green>▶ Klik slot hasil di kanan untuk kirim</green>")
            ));
            paper.setItemMeta(meta);
        }
        inventory.setItem(0, paper);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.6f, 1.2f);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;

        // Slot 2 is the result slot in an anvil
        if (e.getRawSlot() == 2) {
            e.setCancelled(true);
            String inputResult = "";
            if (e.getInventory() instanceof AnvilInventory anvil) {
                inputResult = anvil.getRenameText();
            }
            if (inputResult == null || inputResult.isBlank()) {
                ItemStack res = e.getCurrentItem();
                if (res != null && res.hasItemMeta() && res.getItemMeta().hasDisplayName()) {
                    inputResult = PlainTextComponentSerializer.plainText().serialize(res.getItemMeta().displayName());
                }
            }
            final String finalVal = (inputResult != null) ? inputResult.trim() : "";
            submitted = true;
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.4f);
            if (onInput != null) {
                onInput.accept(finalVal);
            }
            return;
        }

        if (e.getRawSlot() == 0 || e.getRawSlot() == 1) {
            e.setCancelled(true);
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

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
