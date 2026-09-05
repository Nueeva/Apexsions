package com.apexsions.chat.gui;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.chat.model.Mail;
import com.apexsions.chat.model.Report;
import com.apexsions.chat.model.ReportStatus;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public GUIListener(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BaseChatGUI)) {
            return;
        }

        // Cancel all clicks in ApexsionsChat GUIs to prevent taking display items
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }

        BaseChatGUI holder = (BaseChatGUI) event.getInventory().getHolder();

        // 1. Item Showcase GUI
        if (holder instanceof ItemShowcaseGUI) {
            if (slot == 22) {
                player.closeInventory();
            }
            return;
        }

        // 2. Report List GUI
        if (holder instanceof ReportListGUI reportListGUI) {
            if (slot == 40) {
                player.closeInventory();
                return;
            }
            if (slot == 38) {
                player.closeInventory();
                player.performCommand("admingui");
                return;
            }
            if (slot == 36 && reportListGUI.getPage() > 1) {
                new ReportListGUI(plugin, reportListGUI.getPage() - 1).loadAndOpen(player);
                return;
            }
            if (slot == 44) {
                new ReportListGUI(plugin, reportListGUI.getPage() + 1).loadAndOpen(player);
                return;
            }

            Report clickedReport = reportListGUI.getReportAtSlot(slot);
            if (clickedReport != null) {
                player.openInventory(new ReportDetailGUI(plugin, clickedReport).getInventory());
            }
            return;
        }

        // 3. Report Detail GUI
        if (holder instanceof ReportDetailGUI reportDetailGUI) {
            reportDetailGUI.handleClick(player, event);
            return;
        }

        // 4. Mail List GUI
        if (holder instanceof MailListGUI mailListGUI) {
            if (slot == 40) {
                player.closeInventory();
                return;
            }
            if (slot == 36 && mailListGUI.getPage() > 1) {
                new MailListGUI(plugin, mailListGUI.getPage() - 1).loadAndOpen(player);
                return;
            }
            if (slot == 44) {
                new MailListGUI(plugin, mailListGUI.getPage() + 1).loadAndOpen(player);
                return;
            }

            Mail clickedMail = mailListGUI.getMailAtSlot(slot);
            if (clickedMail != null) {
                // Mark read in background
                if (!clickedMail.isRead()) {
                    plugin.getMailRepository().markMailAsReadAsync(clickedMail.getMailId());
                    clickedMail.setRead(true);
                }
                player.openInventory(new MailDetailGUI(plugin, clickedMail).getInventory());
            }
            return;
        }

        // 5. Mail Detail GUI
        if (holder instanceof MailDetailGUI mailDetailGUI) {
            Mail mail = mailDetailGUI.getMail();
            if (slot == 22) {
                new MailListGUI(plugin, 1).loadAndOpen(player);
                return;
            }
            if (slot == 11) { // Collect as Book Item
                mailDetailGUI.deliverMailBook(player);
                return;
            }
            if (slot == 15) { // Archive / Delete
                plugin.getMailRepository().deleteMailAsync(mail.getMailId()).thenAccept(success -> {
                    player.sendMessage(miniMessage.deserialize("<yellow>Letter deleted from your mailbox.</yellow>"));
                    new MailListGUI(plugin, 1).loadAndOpen(player);
                });
                return;
            }
        }

        // 6. Chat Settings GUI
        if (holder instanceof ChatSettingsGUI) {
            if (slot == 22) {
                player.closeInventory();
                return;
            }
            if (slot == 11) { // Toggle Mention Sound
                boolean current = plugin.getConfigManager().getMainConfig().getBoolean("mentions.sound.enabled", true);
                plugin.getConfigManager().getMainConfig().set("mentions.sound.enabled", !current);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                player.openInventory(new ChatSettingsGUI(plugin, player).getInventory());
                return;
            }
            if (slot == 13) { // Switch Channel (Global -> Kingdom -> Staff -> Global)
                var cur = plugin.getChannelManager().getPlayerChannel(player);
                String next = (cur != null && cur.getName().equalsIgnoreCase("Global")) ? "kingdom" :
                        (cur != null && cur.getName().equalsIgnoreCase("Kingdom")) ? "staff" : "global";
                plugin.getChannelManager().setPlayerChannel(player, next);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                player.openInventory(new ChatSettingsGUI(plugin, player).getInventory());
                return;
            }
            if (slot == 15) { // Test Alert Sound
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.8f);
                player.sendMessage(miniMessage.deserialize("<green>🔊 Audio alert test berhasil dibunyikan!</green>"));
                return;
            }
        }

        // 7. Social Profile GUI
        if (holder instanceof SocialProfileGUI socialProfileGUI) {
            socialProfileGUI.handleClick(event);
        }
    }
}
