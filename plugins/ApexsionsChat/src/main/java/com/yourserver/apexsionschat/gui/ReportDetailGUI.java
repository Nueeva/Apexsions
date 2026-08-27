package com.yourserver.apexsionschat.gui;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import com.yourserver.apexsionschat.model.Report;
import com.yourserver.apexsionschat.model.ReportStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReportDetailGUI extends BaseChatGUI {

    private final ApexsionsChatPlugin plugin;
    private final Report report;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public ReportDetailGUI(ApexsionsChatPlugin plugin, Report report) {
        this.plugin = plugin;
        this.report = report;
        this.inventory = Bukkit.createInventory(this, 27, miniMessage.deserialize("<dark_gray>🛡️ Manage Report #" + report.getReportId() + "</dark_gray>"));
        build();
    }

    private void build() {
        ItemStack border = createBorderItem();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, border);
        }

        // Summary Info at Slot 4
        inventory.setItem(4, createSummaryItem());

        // Action 1: Mark Reviewing at Slot 11
        inventory.setItem(11, createActionButton("<yellow><bold>Mark Reviewing</bold></yellow>", Material.CLOCK, "<gray>Sets status to REVIEWING.</gray>"));

        // Action 2: Mark Resolved at Slot 13
        inventory.setItem(13, createActionButton("<green><bold>Resolve Report</bold></green>", Material.EMERALD_BLOCK, "<gray>Marks issue as resolved/handled.</gray>"));

        // Action 3: Mark Dismissed at Slot 15
        inventory.setItem(15, createActionButton("<red><bold>Dismiss Report</bold></red>", Material.REDSTONE_BLOCK, "<gray>Dismisses false or invalid report.</gray>"));

        // Back button at Slot 22
        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta meta = backBtn.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize("<yellow>◀ Back to Report List</yellow>"));
            backBtn.setItemMeta(meta);
        }
        inventory.setItem(22, backBtn);
    }

    private ItemStack createSummaryItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize("<gold><bold>Report Details #" + report.getReportId() + "</bold></gold>"));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Reporter: <white>" + report.getReporterName() + "</white></gray>"));
            lore.add(miniMessage.deserialize("<gray>Reported Player: <red><bold>" + report.getReportedName() + "</bold></red></gray>"));
            lore.add(miniMessage.deserialize("<gray>Reason: <yellow>" + report.getReason() + "</yellow></gray>"));
            lore.add(miniMessage.deserialize("<gray>World: <white>" + report.getWorld() + "</white></gray>"));
            lore.add(miniMessage.deserialize("<gray>Status: <yellow>" + report.getStatus().name() + "</yellow></gray>"));
            lore.add(miniMessage.deserialize("<gray>Created: <white>" + dtf.format(report.getTimestamp()) + "</white></gray>"));
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

    public Report getReport() { return report; }
}
