package com.yourserver.apexsionschat.gui;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import com.yourserver.apexsionschat.channel.ChatChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modern 27-slot GUI allowing players to customize personal chat preferences
 * (Mention pings, audio alerts, visual channel switcher).
 */
public class ChatSettingsGUI extends BaseChatGUI {

    private final ApexsionsChatPlugin plugin;
    private final Player player;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ChatSettingsGUI(ApexsionsChatPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 27, miniMessage.deserialize("<gradient:#3498db:#2980b9><bold>💬 PENGATURAN OBROLAN</bold></gradient>"));
        build();
    }

    private void build() {
        ItemStack border = createBorderItem();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, border);
        }

        // Slot 11: Mention Pings Toggle
        boolean pingsEnabled = plugin.getConfigManager().getMainConfig().getBoolean("mentions.sound.enabled", true);
        ItemStack pingItem = new ItemStack(pingsEnabled ? Material.NOTE_BLOCK : Material.JUKEBOX);
        ItemMeta pingMeta = pingItem.getItemMeta();
        if (pingMeta != null) {
            pingMeta.displayName(miniMessage.deserialize("<yellow><bold>🔔 Notifikasi Mention (@Player)</bold></yellow>"));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Membunyikan audio saat namamu di-mention di chat.</gray>"));
            lore.add(miniMessage.deserialize(""));
            lore.add(miniMessage.deserialize("<gray>Status: " + (pingsEnabled ? "<green><bold>AKTIF</bold></green>" : "<red><bold>NONAKTIF</bold></red>") + "</gray>"));
            lore.add(miniMessage.deserialize("<yellow>» Klik untuk toggle</yellow>"));
            pingMeta.lore(lore);
            pingItem.setItemMeta(pingMeta);
        }
        inventory.setItem(11, pingItem);

        // Slot 13: Channel Switcher
        ChatChannel curChannel = plugin.getChannelManager().getPlayerChannel(player);
        ItemStack channelItem = new ItemStack(Material.COMPASS);
        ItemMeta cMeta = channelItem.getItemMeta();
        if (cMeta != null) {
            cMeta.displayName(miniMessage.deserialize("<aqua><bold>🌐 Saluran Obrolan Aktif</bold></aqua>"));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Saluran tujuan saat mengetik di obrolan umum.</gray>"));
            lore.add(miniMessage.deserialize(""));
            lore.add(miniMessage.deserialize("<gray>Channel Saat Ini: <gold><bold>" + (curChannel != null ? curChannel.getName() : "Global") + "</bold></gold></gray>"));
            lore.add(miniMessage.deserialize("<yellow>» Klik untuk berganti (Global ➜ Kingdom ➜ Staff)</yellow>"));
            cMeta.lore(lore);
            channelItem.setItemMeta(cMeta);
        }
        inventory.setItem(13, channelItem);

        // Slot 15: Sound Alerts Toggle
        ItemStack alertItem = new ItemStack(Material.BELL);
        ItemMeta aMeta = alertItem.getItemMeta();
        if (aMeta != null) {
            aMeta.displayName(miniMessage.deserialize("<gold><bold>🔊 Suara Pesan Masuk</bold></gold>"));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Efek suara lembut saat menerima pesan baru.</gray>"));
            lore.add(miniMessage.deserialize(""));
            lore.add(miniMessage.deserialize("<green><bold>AKTIF</bold></green>"));
            lore.add(miniMessage.deserialize("<yellow>» Klik untuk uji coba suara</yellow>"));
            aMeta.lore(lore);
            alertItem.setItemMeta(aMeta);
        }
        inventory.setItem(15, alertItem);

        // Slot 22: Close
        ItemStack closeBtn = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeBtn.getItemMeta();
        if (closeMeta != null) {
            closeMeta.displayName(miniMessage.deserialize("<red><bold>✖ TUTUP</bold></red>"));
            closeBtn.setItemMeta(closeMeta);
        }
        inventory.setItem(22, closeBtn);
    }

    private ItemStack createBorderItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }
}
