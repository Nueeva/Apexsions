package com.yourserver.apexsionschat.gui;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import com.yourserver.apexsionschat.model.Mail;
import com.yourserver.apexsionschat.model.Report;
import com.yourserver.apexsionschat.model.ReportStatus;
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
            Report report = reportDetailGUI.getReport();
            if (slot == 22) {
                new ReportListGUI(plugin, 1).loadAndOpen(player);
                return;
            }
            if (slot == 11) { // Mark Reviewing
                plugin.getReportRepository().updateReportStatusAsync(
                        report.getReportId(), ReportStatus.REVIEWING, player.getUniqueId(), player.getName(), "Under staff review"
                ).thenAccept(success -> {
                    player.sendMessage(miniMessage.deserialize("<green>✔ Report #" + report.getReportId() + " marked as <yellow>REVIEWING</yellow>.</green>"));
                    player.closeInventory();
                });
                return;
            }
            if (slot == 13) { // Mark Resolved
                plugin.getReportRepository().updateReportStatusAsync(
                        report.getReportId(), ReportStatus.RESOLVED, player.getUniqueId(), player.getName(), "Resolved by " + player.getName()
                ).thenAccept(success -> {
                    player.sendMessage(miniMessage.deserialize("<green>✔ Report #" + report.getReportId() + " marked as <green>RESOLVED</green>.</green>"));
                    player.closeInventory();
                });
                return;
            }
            if (slot == 15) { // Mark Dismissed
                plugin.getReportRepository().updateReportStatusAsync(
                        report.getReportId(), ReportStatus.DISMISSED, player.getUniqueId(), player.getName(), "Dismissed by " + player.getName()
                ).thenAccept(success -> {
                    player.sendMessage(miniMessage.deserialize("<yellow>✔ Report #" + report.getReportId() + " marked as <red>DISMISSED</red>.</yellow>"));
                    player.closeInventory();
                });
                return;
            }
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
    }
}
