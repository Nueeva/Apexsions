package com.yourserver.apexsionschat.gui;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import com.yourserver.apexsionschat.model.Mail;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MailDetailGUI extends BaseChatGUI {

    private final ApexsionsChatPlugin plugin;
    private final Mail mail;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public MailDetailGUI(ApexsionsChatPlugin plugin, Mail mail) {
        this.plugin = plugin;
        this.mail = mail;
        this.inventory = Bukkit.createInventory(this, 27, miniMessage.deserialize("<dark_gray>📬 Letter Details</dark_gray>"));
        build();
    }

    private void build() {
        ItemStack border = createBorderItem();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, border);
        }

        // Letter preview item at Slot 4
        inventory.setItem(4, createLetterPreview());

        // Action: "Read as Book" at Slot 11
        inventory.setItem(11, createActionButton(
                "<gold><bold>📖 Collect as Book Item</bold></gold>",
                Material.WRITTEN_BOOK,
                "<gray>Places a physical letter in your inventory.</gray>"
        ));

        // Action: "Delete Mail" at Slot 15
        inventory.setItem(15, createActionButton(
                "<red><bold>🗑 Delete / Archive</bold></red>",
                Material.LAVA_BUCKET,
                "<gray>Permanently removes this letter from your mailbox.</gray>"
        ));

        // Back button at Slot 22
        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta meta = backBtn.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize("<yellow>◀ Back to Mailbox</yellow>"));
            backBtn.setItemMeta(meta);
        }
        inventory.setItem(22, backBtn);
    }

    private ItemStack createLetterPreview() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize("<gold><bold>" + mail.getSubject() + "</bold></gold>"));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>From: <yellow>" + mail.getSenderName() + "</yellow></gray>"));
            lore.add(miniMessage.deserialize("<gray>Date: <white>" + dtf.format(mail.getCreatedAt()) + "</white></gray>"));
            lore.add(Component.empty());
            lore.add(miniMessage.deserialize("<gray>Body Preview:</gray>"));

            // Split body into lines for display
            String[] words = mail.getBody().split(" ");
            StringBuilder line = new StringBuilder();
            for (String w : words) {
                if (line.length() + w.length() > 30) {
                    lore.add(miniMessage.deserialize("<white>" + line.toString().trim() + "</white>"));
                    line = new StringBuilder();
                }
                line.append(w).append(" ");
            }
            if (line.length() > 0) {
                lore.add(miniMessage.deserialize("<white>" + line.toString().trim() + "</white>"));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createActionButton(String name, Material mat, String desc) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(name));
            meta.lore(Collections.singletonList(miniMessage.deserialize(desc)));
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

    public void deliverMailBook(Player player) {
        // Safe inventory check
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(miniMessage.deserialize("<red>✖ Your inventory is full! Please make space before taking the letter book.</red>"));
            return;
        }

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta != null) {
            meta.title(miniMessage.deserialize("<gold>" + mail.getSubject() + "</gold>"));
            meta.author(miniMessage.deserialize(mail.getSenderName()));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Sent on: <gold>" + dtf.format(mail.getCreatedAt()) + "</gold></gray>"));
            meta.lore(lore);

            Component pageContent = miniMessage.deserialize(
                    "<bold>From:</bold> " + mail.getSenderName() + "\n" +
                    "<bold>Date:</bold> " + dtf.format(mail.getCreatedAt()) + "\n" +
                    "<bold>Subject:</bold> " + mail.getSubject() + "\n\n" +
                    mail.getBody()
            );
            meta.addPages(pageContent);
            book.setItemMeta(meta);
        }

        player.getInventory().addItem(book);
        player.sendMessage(miniMessage.deserialize("<green>✔ Letter placed in your inventory as a readable book item.</green>"));
    }

    public Mail getMail() { return mail; }
}
