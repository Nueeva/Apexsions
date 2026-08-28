package com.apexsions.chat.gui;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.chat.model.Report;
import com.apexsions.chat.model.ReportStatus;
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

public class ReportListGUI extends BaseChatGUI {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final int page;
    private final Map<Integer, Report> slotReportMap = new HashMap<>();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public ReportListGUI(ApexsionsChatPlugin plugin, int page) {
        this.plugin = plugin;
        this.page = Math.max(1, page);
        String titleStr = plugin.getConfigManager().getReportsConfig().getString("reports.gui.title", "<dark_gray>🛡️ Staff Report Inbox</dark_gray>");
        this.inventory = Bukkit.createInventory(this, 45, miniMessage.deserialize(titleStr + " <gray>(Page " + this.page + ")</gray>"));
    }

    public void loadAndOpen(Player player) {
        int pageSize = 28; // Slots 10-16, 19-25, 28-34, 37-43
        int offset = (page - 1) * pageSize;

        plugin.getReportRepository().getOpenReportsAsync(pageSize, offset).thenAccept(reports -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                build(reports);
                player.openInventory(inventory);
            });
        });
    }

    private void build(List<Report> reports) {
        ItemStack border = createBorderItem(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, border);
        }

        int[] contentSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        slotReportMap.clear();
        for (int i = 0; i < reports.size() && i < contentSlots.length; i++) {
            Report rep = reports.get(i);
            int slot = contentSlots[i];
            slotReportMap.put(slot, rep);
            inventory.setItem(slot, createReportCard(rep));
        }

        // Navigation Buttons
        if (page > 1) {
            inventory.setItem(36, createNavButton("<yellow>◀ Previous Page</yellow>", Material.ARROW));
        }
        if (reports.size() >= 21) {
            inventory.setItem(44, createNavButton("<yellow>Next Page ▶</yellow>", Material.ARROW));
        }

        // Close Button
        inventory.setItem(40, createNavButton("<red>Close</red>", Material.BARRIER));
    }

    private ItemStack createReportCard(Report report) {
        Material mat = report.getStatus() == ReportStatus.REVIEWING ? Material.WRITABLE_BOOK : Material.PAPER;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize("<gold><bold>Report #" + report.getReportId() + "</bold></gold> <gray>(" + report.getStatus().name() + ")</gray>"));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Reporter: <white>" + report.getReporterName() + "</white></gray>"));
            lore.add(miniMessage.deserialize("<gray>Reported Player: <red><bold>" + report.getReportedName() + "</bold></red></gray>"));
            lore.add(miniMessage.deserialize("<gray>Reason: <yellow>" + report.getReason() + "</yellow></gray>"));
            lore.add(miniMessage.deserialize("<gray>World: <white>" + report.getWorld() + "</white></gray>"));
            lore.add(miniMessage.deserialize("<gray>Date: <white>" + dtf.format(report.getTimestamp()) + "</white></gray>"));
            lore.add(Component.empty());
            lore.add(miniMessage.deserialize("<yellow>⚡ Click to review details & take action</yellow>"));
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

    public Report getReportAtSlot(int slot) {
        return slotReportMap.get(slot);
    }

    public int getPage() { return page; }
}
