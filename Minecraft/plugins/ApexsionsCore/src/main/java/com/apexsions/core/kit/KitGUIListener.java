package com.apexsions.core.kit;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Event listener routing inventory interactions for Kit-related GUIs and amount chat input.
 */
public class KitGUIListener implements Listener {

    private final MiniMessage mm = MiniMessage.miniMessage();

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof KitUserGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof KitPreviewGUI gui) {
            gui.handleClick(event);
        } else if (holder instanceof KitAdminCreatorGUI gui) {
            gui.handleClick(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof KitAdminCreatorGUI gui) {
            gui.handleClose(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof KitAdminCreatorGUI gui) {
            gui.handleDrag(event);
        } else if (holder instanceof KitUserGUI || holder instanceof KitPreviewGUI) {
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot < event.getInventory().getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        KitAdminCreatorGUI.AmountChatSession session = KitAdminCreatorGUI.activeAmountSessions.remove(player.getUniqueId());
        if (session == null) return;

        event.setCancelled(true);
        String text = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        if (text.equalsIgnoreCase("cancel")) {
            player.sendMessage(mm.deserialize("<red>Pengubahan jumlah item dibatalkan.</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            Bukkit.getScheduler().runTask(session.gui().getPlugin(), session.gui()::open);
            return;
        }

        try {
            int amount = Integer.parseInt(text);
            int max = session.item().getMaxStackSize();
            if (amount < 1 || amount > max) {
                player.sendMessage(mm.deserialize("<red>Jumlah harus antara 1 sampai " + max + "!</red>"));
                Bukkit.getScheduler().runTask(session.gui().getPlugin(), session.gui()::open);
                return;
            }

            session.item().setAmount(amount);
            player.sendMessage(mm.deserialize("<green>✓ Berhasil mengubah jumlah item menjadi <gold>" + amount + "</gold>!</green>"));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
            Bukkit.getScheduler().runTask(session.gui().getPlugin(), session.gui()::open);
        } catch (NumberFormatException e) {
            player.sendMessage(mm.deserialize("<red>Input bukan angka yang valid!</red>"));
            Bukkit.getScheduler().runTask(session.gui().getPlugin(), session.gui()::open);
        }
    }
}
