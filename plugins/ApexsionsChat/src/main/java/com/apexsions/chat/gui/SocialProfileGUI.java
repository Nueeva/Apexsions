package com.apexsions.chat.gui;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.core.api.PlayerChatProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 27-Slot Interactive Social & Player Action Hub.
 * Triggered by clicking on a player's chat message / ID-Card.
 */
public class SocialProfileGUI extends BaseChatGUI {

    private final ApexsionsChatPlugin plugin;
    private final Player viewer;
    private final Player target;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public SocialProfileGUI(ApexsionsChatPlugin plugin, Player viewer, Player target) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.target = target;
        this.inventory = Bukkit.createInventory(this, 27, mm.deserialize("<gradient:#3498db:#2ecc71><bold>PROFIL SOSIAL: " + target.getName() + "</bold></gradient>"));
        build();
    }

    private void build() {
        ItemStack border = createBorderItem();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, border);
        }

        // Slot 4: Player Profile Card
        inventory.setItem(4, createPlayerCard());

        // Slot 10: Direct Whisper / Message
        inventory.setItem(10, createActionItem(Material.WRITABLE_BOOK,
                "<gradient:#f1c40f:#e67e22><bold>💬 KIRIM BISIKAN (/msg)</bold></gradient>",
                List.of("<gray>Kirim pesan pribadi rahasia ke " + target.getName() + ".</gray>", "<yellow>▶ Klik untuk memulai bisikan</yellow>")));

        // Slot 12: Barter / Trade Request
        inventory.setItem(12, createActionItem(Material.GOLD_INGOT,
                "<gradient:#2ecc71:#27ae60><bold>🤝 AJAK BARTER (/trade)</bold></gradient>",
                List.of("<gray>Buka antarmuka tukar item & uang yang aman.</gray>", "<yellow>▶ Klik untuk kirim ajakan barter</yellow>")));

        // Slot 14: Send Mail / Letter
        inventory.setItem(14, createActionItem(Material.PAPER,
                "<gradient:#00d2d3:#54a0ff><bold>✉ KIRIM SURAT (/mail send)</bold></gradient>",
                List.of("<gray>Kirim surat resmi ke kotak pos pemain ini.</gray>", "<yellow>▶ Klik untuk menulis surat</yellow>")));

        // Slot 16: Report Player
        inventory.setItem(16, createActionItem(Material.REDSTONE,
                "<gradient:#e74c3c:#c0392b><bold>🚨 LAPORKAN PEMAIN (/report)</bold></gradient>",
                List.of("<gray>Laporkan perilaku atau pelanggaran pemain ini ke staf.</gray>", "<red>▶ Klik untuk membuat laporan</red>")));

        // Slot 22: Close
        ItemStack closeBtn = new ItemStack(Material.BARRIER);
        ItemMeta meta = closeBtn.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize("<red><bold>✖ TUTUP</bold></red>"));
            closeBtn.setItemMeta(meta);
        }
        inventory.setItem(22, closeBtn);
    }

    private ItemStack createPlayerCard() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        if (sm != null) {
            sm.setOwningPlayer(target);
            PlayerChatProfile profile = plugin.getApexsionsCoreHook().getPlayerChatProfile(target.getUniqueId());

            String pName = profile != null ? profile.playerName() : target.getName();
            String title = profile != null && profile.activeTitle() != null ? profile.activeTitle() : (profile != null ? profile.levelTitle() : "Wanderer");
            String rank = profile != null ? profile.rank() : "Member";
            int level = profile != null ? profile.level() : 1;
            String kingdom = profile != null ? profile.kingdomDisplayName() : "Belum Memilih";
            double balance = profile != null ? profile.balanceRupiah() : 0.0;

            sm.displayName(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>👑 " + pName + "</bold></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Gelar:</gray> " + title));
            lore.add(mm.deserialize("<gray>Rank Donatur:</gray> <yellow>" + rank + "</yellow>"));
            lore.add(mm.deserialize("<gray>Kerajaan:</gray> <gold><bold>" + kingdom + "</bold></gold>" + (profile != null && profile.isMonarch() ? " <yellow><bold>[RAJA]</bold></yellow>" : "")));
            lore.add(mm.deserialize("<gray>Level Karakter:</gray> <yellow>Lv. " + level + "</yellow>"));
            lore.add(mm.deserialize("<gray>Saldo Rupiah:</gray> <green><bold>Rp " + String.format("%,.0f", balance) + "</bold></green>"));
            lore.add(mm.deserialize("<gray>Status Ping:</gray> <aqua>" + target.getPing() + "ms</aqua>"));
            sm.lore(lore);
            head.setItemMeta(sm);
        }
        return head;
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (!target.isOnline()) {
            viewer.sendMessage(mm.deserialize("<red>Pemain target sudah keluar dari server.</red>"));
            viewer.closeInventory();
            return;
        }

        if (slot == 10) { // Whisper /msg
            viewer.closeInventory();
            viewer.sendMessage(mm.deserialize("<yellow>Ketik pesanmu di chat dengan format: <white>/msg " + target.getName() + " <pesan></white></yellow>"));
            viewer.playSound(viewer.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            return;
        }

        if (slot == 12) { // Trade
            viewer.closeInventory();
            viewer.performCommand("trade " + target.getName());
            viewer.playSound(viewer.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            return;
        }

        if (slot == 14) { // Mail
            viewer.closeInventory();
            viewer.sendMessage(mm.deserialize("<yellow>Gunakan perintah <white>/mail send " + target.getName() + " <pesan></white> untuk mengirim surat resmi.</yellow>"));
            viewer.playSound(viewer.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            return;
        }

        if (slot == 16) { // Report
            viewer.closeInventory();
            viewer.sendMessage(mm.deserialize("<red>Gunakan format: <white>/report " + target.getName() + " <alasan></white> untuk melaporkan pemain.</red>"));
            viewer.playSound(viewer.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 1.0f);
            return;
        }

        if (slot == 22) {
            viewer.closeInventory();
        }
    }

    private ItemStack createActionItem(Material mat, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            List<Component> components = new ArrayList<>();
            for (String l : loreLines) {
                components.add(mm.deserialize(l));
            }
            meta.lore(components);
            item.setItemMeta(meta);
        }
        return item;
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
