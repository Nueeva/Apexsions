package com.apexsions.media.gui;

import com.apexsions.media.banner.MediaBanner;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
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

public class MediaConfirmGUI implements InventoryHolder {

    private final MediaBanner banner;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MediaConfirmGUI(MediaBanner banner, Player player) {
        this.banner = banner;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 27, miniMessage.deserialize("<gradient:#3498db:#2980b9><bold>🔗 KONFIRMASI LINK</bold></gradient>"));
        build();
    }

    private void build() {
        ItemStack border = createBorder();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, border);
        }

        // Slot 4: Banner Info
        ItemStack info = new ItemStack(Material.PAINTING);
        ItemMeta iMeta = info.getItemMeta();
        if (iMeta != null) {
            iMeta.displayName(miniMessage.deserialize("<gold><bold>" + banner.getId().toUpperCase() + "</bold></gold>"));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Tautan URL terlampir:</gray>"));
            lore.add(miniMessage.deserialize("<aqua>" + (banner.getLinkUrl() != null ? banner.getLinkUrl() : "Tidak ada link") + "</aqua>"));
            lore.add(miniMessage.deserialize(""));
            lore.add(miniMessage.deserialize("<yellow>Pilih salah satu tindakan di bawah ini:</yellow>"));
            iMeta.lore(lore);
            info.setItemMeta(iMeta);
        }
        inventory.setItem(4, info);

        // Slot 11: Buka di Browser (Sends chat clickable link)
        ItemStack openItem = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta oMeta = openItem.getItemMeta();
        if (oMeta != null) {
            oMeta.displayName(miniMessage.deserialize("<green><bold>🌐 BUKA DI BROWSER</bold></green>"));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Membuka tautan website langsung</gray>"));
            lore.add(miniMessage.deserialize("<gray>di peramban internet Anda.</gray>"));
            lore.add(miniMessage.deserialize(""));
            lore.add(miniMessage.deserialize("<yellow>» Klik untuk membuka</yellow>"));
            oMeta.lore(lore);
            openItem.setItemMeta(oMeta);
        }
        inventory.setItem(11, openItem);

        // Slot 13: Salin URL ke Clipboard
        ItemStack copyItem = new ItemStack(Material.CYAN_CONCRETE);
        ItemMeta cMeta = copyItem.getItemMeta();
        if (cMeta != null) {
            cMeta.displayName(miniMessage.deserialize("<aqua><bold>📋 SALIN KE CLIPBOARD</bold></aqua>"));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Menyalin teks tautan URL</gray>"));
            lore.add(miniMessage.deserialize("<gray>ke papan klip (clipboard) Anda.</gray>"));
            lore.add(miniMessage.deserialize(""));
            lore.add(miniMessage.deserialize("<yellow>» Klik untuk menyalin</yellow>"));
            cMeta.lore(lore);
            copyItem.setItemMeta(cMeta);
        }
        inventory.setItem(13, copyItem);

        // Slot 15: Batal / Tutup
        ItemStack closeItem = new ItemStack(Material.RED_CONCRETE);
        ItemMeta xMeta = closeItem.getItemMeta();
        if (xMeta != null) {
            xMeta.displayName(miniMessage.deserialize("<red><bold>✖ BATAL / TUTUP</bold></red>"));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Menutup menu ini.</gray>"));
            xMeta.lore(lore);
            closeItem.setItemMeta(xMeta);
        }
        inventory.setItem(15, closeItem);
    }

    private ItemStack createBorder() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        if (slot == 11) { // Buka URL
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            String url = banner.getLinkUrl();
            if (url != null && !url.isBlank()) {
                Component linkComp = miniMessage.deserialize(
                        "\n<gradient:#2ecc71:#27ae60><bold>🌐 TAUTAN RESMI DIBUKA:</bold></gradient>\n" +
                                "<gray>Klik tautan di bawah untuk membuka di browser:</gray>\n" +
                                "<yellow>» </yellow><underlined><aqua>" + url + "</aqua></underlined>\n"
                ).clickEvent(ClickEvent.openUrl(url)).hoverEvent(HoverEvent.showText(miniMessage.deserialize("<green>Klik untuk membuka " + url + "</green>")));
                player.sendMessage(linkComp);
            }
        } else if (slot == 13) { // Salin URL
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.8f);
            String url = banner.getLinkUrl();
            if (url != null && !url.isBlank()) {
                Component copyComp = miniMessage.deserialize(
                        "\n<gradient:#3498db:#2980b9><bold>📋 SALIN TAUTAN:</bold></gradient>\n" +
                                "<gray>Klik pesan ini untuk menyalin ke clipboard:</gray>\n" +
                                "<yellow>» </yellow><underlined><aqua>" + url + "</aqua></underlined>\n"
                ).clickEvent(ClickEvent.copyToClipboard(url)).hoverEvent(HoverEvent.showText(miniMessage.deserialize("<aqua>Klik untuk menyalin teks URL</aqua>")));
                player.sendMessage(copyComp);
            }
        } else if (slot == 15) { // Batal
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
