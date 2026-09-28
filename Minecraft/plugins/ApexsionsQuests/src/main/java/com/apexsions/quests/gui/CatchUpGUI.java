package com.apexsions.quests.gui;

import com.apexsions.quests.ApexsionsQuests;
import com.apexsions.quests.model.PlayerStreakData;
import com.apexsions.quests.service.CalendarService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatchUpGUI implements QuestsGuiHolder {

    private static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final ApexsionsQuests plugin;
    private final Player player;
    private final Inventory inventory;
    private final Map<Integer, Integer> slotToDayMap = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    public CatchUpGUI(ApexsionsQuests plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#3498db:#2980b9><bold>✦ TEBUS HARI TERLEWAT (CATCH-UP) ✦</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotToDayMap.clear();
        GuiUtils.fillStandardBorder54(inventory);

        PlayerStreakData data = plugin.getStreakService().getStreakData(player.getUniqueId());
        int today = LocalDate.now().getDayOfMonth();
        double cost = plugin.getConfig().getDouble("calendar.catch-up-cost", 5000.0);

        // 1. Header Slot 4
        List<String> headerLore = List.of(
                "<gray>Biaya Tebus: <gold>Rp " + String.format("%,.0f", cost) + "</gold> per hari.</gray>",
                "<gray>Tebus hari-hari yang sempat terlewat</gray>",
                "<gray>agar tidak kehilangan hadiah bulan ini!</gray>"
        );
        inventory.setItem(4, GuiUtils.createItem(Material.GOLD_INGOT, "<gold><bold>Katalog Catch-up Harian</bold></gold>", headerLore, false));

        // 2. Populate Missed Days
        int slotIndex = 0;
        for (int day = 1; day < today && slotIndex < CONTENT_SLOTS.length; day++) {
            if (data == null || !data.isDayClaimed(day)) {
                int slot = CONTENT_SLOTS[slotIndex++];
                slotToDayMap.put(slot, day);

                CalendarService.DayReward r = plugin.getCalendarService().getReward(day);
                String rDesc = (r != null) ? r.getDescription() : "Hadiah Harian";

                List<String> lore = new ArrayList<>();
                lore.add("<gray>Hadiah: </gray><gold>" + rDesc + "</gold>");
                lore.add("<gray>Biaya Tebus:</gray> <gold>Rp " + String.format("%,.0f", cost) + "</gold>");
                lore.add("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>");
                lore.add("<yellow>▶ Klik untuk menebus hari ini sekarang!</yellow>");

                inventory.setItem(slot, GuiUtils.createItem(Material.CHEST, "<yellow><bold>Tebus Hari ke-" + day + "</bold></yellow>", lore, false));
            }
        }

        if (slotToDayMap.isEmpty()) {
            List<String> emptyLore = List.of(
                    "<gray>Semua hari sebelum hari ini sudah terklaim!</gray>",
                    "<gray>Anda tidak memiliki hari yang terlewat.</gray>"
            );
            inventory.setItem(22, GuiUtils.createItem(Material.EMERALD_BLOCK, "<green><bold>Semua Hadiah Aman!</bold></green>", emptyLore, false));
        }

        // Navigation
        inventory.setItem(45, GuiUtils.createItem(Material.ARROW, "<yellow><bold>⬅ KEMBALI</bold></yellow>", List.of("<gray>Kembali ke kalender utama</gray>"), false));
        inventory.setItem(49, GuiUtils.createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>", List.of("<gray>Tutup menu</gray>"), false));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            new DailyCalendarGUI(plugin, player).open();
            return;
        }

        if (slot == 49) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        Integer day = slotToDayMap.get(slot);
        if (day != null) {
            boolean caughtUp = plugin.getCalendarService().catchUpClaim(player, day);
            if (caughtUp) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
            buildGUI();
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
