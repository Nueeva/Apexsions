package com.apexsions.chat.gui;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.chat.model.Mail;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MailListGUI extends BaseChatGUI {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final int page;
    private final Map<Integer, Mail> slotMailMap = new HashMap<>();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public MailListGUI(ApexsionsChatPlugin plugin, int page) {
        this.plugin = plugin;
        this.page = Math.max(1, page);
        String titleStr = plugin.getConfigManager().getMailConfig().getString("mail.gui.title", "<dark_gray>📬 Your Mailbox</dark_gray>");
        this.inventory = Bukkit.createInventory(this, 45, miniMessage.deserialize(titleStr + " <gray>(Page " + this.page + ")</gray>"));
    }

    public void loadAndOpen(Player player) {
        int pageSize = 21;
        int offset = (page - 1) * pageSize;

        plugin.getMailRepository().getPlayerInboxAsync(player.getUniqueId(), pageSize, offset).thenAccept(mailList -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                build(mailList);
                player.openInventory(inventory);
            });
        });
    }

    private void build(List<Mail> mailList) {
        ItemStack border = createBorderItem(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, border);
        }

        int[] contentSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        slotMailMap.clear();
        for (int i = 0; i < mailList.size() && i < contentSlots.length; i++) {
            Mail m = mailList.get(i);
            int slot = contentSlots[i];
            slotMailMap.put(slot, m);
            inventory.setItem(slot, createMailItem(m));
        }

        // Navigation Buttons
        if (page > 1) {
            inventory.setItem(36, createNavButton("<yellow>◀ Previous Page</yellow>", Material.ARROW));
        }
        if (mailList.size() >= 21) {
            inventory.setItem(44, createNavButton("<yellow>Next Page ▶</yellow>", Material.ARROW));
        }

        // Close Button
        inventory.setItem(40, createNavButton("<red>Close</red>", Material.BARRIER));
    }

    private ItemStack createMailItem(Mail mail) {
        Material mat = mail.isRead() ? Material.BOOK : Material.WRITTEN_BOOK;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String readStatus = mail.isRead() ? "<gray>[Read]</gray>" : "<green><bold>[NEW]</bold></green>";
            meta.displayName(miniMessage.deserialize(readStatus + " <gold>" + mail.getSubject() + "</gold>"));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>From: <yellow>" + mail.getSenderName() + "</yellow></gray>"));
            lore.add(miniMessage.deserialize("<gray>Date: <white>" + dtf.format(mail.getCreatedAt()) + "</white></gray>"));
            lore.add(Component.empty());
            lore.add(miniMessage.deserialize("<yellow>⚡ Click to view message</yellow>"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNavButton(String name, Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBorderItem(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }

    public Mail getMailAtSlot(int slot) {
        return slotMailMap.get(slot);
    }

    public int getPage() { return page; }
}
