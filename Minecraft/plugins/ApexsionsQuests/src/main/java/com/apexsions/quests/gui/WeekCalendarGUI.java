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

public class WeekCalendarGUI implements QuestsGuiHolder {

    private final ApexsionsQuests plugin;
    private final Player player;
    private final int weekNum;
    private final int startDay;
    private final int endDay;
    private final Inventory inventory;
    private final Map<Integer, Integer> slotToDayMap = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    private static final int[] DAY_SLOTS_7 = {20, 21, 22, 23, 24, 25, 26};
    private static final int[] DAY_SLOTS_3 = {21, 22, 23};

    public WeekCalendarGUI(ApexsionsQuests plugin, Player player, int weekNum, int startDay, int endDay) {
        this.plugin = plugin;
        this.player = player;
        this.weekNum = weekNum;
        this.startDay = startDay;
        this.endDay = endDay;
        String titleText = (weekNum == 5)
                ? "<gradient:#f1c40f:#e67e22><bold>THE GOLDEN APEX FINALE (29-31)</bold></gradient>"
                : "<gradient:#f39c12:#f1c40f><bold>KALENDER: MINGGU KE-" + weekNum + " (" + startDay + "-" + endDay + ")</bold></gradient>";
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize(titleText));
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

        // 1. Header Slot 4
        List<String> headerLore = List.of(
                "<gray>Rentang: Hari ke-" + startDay + " s/d ke-" + endDay + "</gray>",
                "<gray>Hari Ini: <yellow>Hari ke-" + today + "</yellow></gray>",
                "<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>",
                "<yellow>Klik slot hari ini jika belum diklaim!</yellow>"
        );
        inventory.setItem(4, GuiUtils.createItem(Material.CLOCK, "<gold><bold>Jadwal Minggu ke-" + weekNum + "</bold></gold>", headerLore, false));

        // 2. Day Slots
        int count = endDay - startDay + 1;
        int[] slots = (count <= 3) ? DAY_SLOTS_3 : DAY_SLOTS_7;

        for (int i = 0; i < count && i < slots.length; i++) {
            int day = startDay + i;
            int slot = slots[i];
            slotToDayMap.put(slot, day);

            boolean isClaimed = (data != null && data.isDayClaimed(day));
            boolean isToday = (day == today);
            boolean isPast = (day < today);
            CalendarService.DayReward reward = plugin.getCalendarService().getReward(day);
            String rewardDesc = (reward != null) ? reward.getDescription() : "Hadiah Harian";

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Hadiah: </gray><gold>" + rewardDesc + "</gold>");
            lore.add("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>");

            if (isClaimed) {
                lore.add("<green><bold>✔ SUDAH DIKLAIM</bold></green>");
                inventory.setItem(slot, GuiUtils.createItem(Material.LIME_STAINED_GLASS_PANE, "<green><bold>Hari ke-" + day + "</bold></green>", lore, false));
            } else if (isToday) {
                lore.add("<gold><bold>⭐ HARI INI!</bold></gold>");
                lore.add("<yellow>▶ Klik untuk mengklaim hadiah sekarang!</yellow>");
                inventory.setItem(slot, GuiUtils.createItem(Material.GOLD_BLOCK, "<gold><bold>⭐ Hari ke-" + day + " (Klaim Sekarang)</bold></gold>", lore, true));
            } else if (isPast) {
                lore.add("<red><bold>✖ TERLEWAT</bold></red>");
                lore.add("<yellow>▶ Klik untuk menebus hari ini (Catch-up)!</yellow>");
                inventory.setItem(slot, GuiUtils.createItem(Material.RED_STAINED_GLASS_PANE, "<red><bold>Hari ke-" + day + " (Terlewat)</bold></red>", lore, false));
            } else {
                lore.add("<gray><bold>🔒 TERKUNCI</bold></gray>");
                lore.add("<gray>Akan terbuka pada hari ke-" + day + ".</gray>");
                inventory.setItem(slot, GuiUtils.createItem(Material.GRAY_STAINED_GLASS_PANE, "<gray><bold>Hari ke-" + day + "</bold></gray>", lore, false));
            }
        }

        // 3. Navigation
        inventory.setItem(45, GuiUtils.createItem(Material.ARROW, "<yellow><bold>⬅ KEMBALI</bold></yellow>", List.of("<gray>Kembali ke kalender utama</gray>"), false));
        inventory.setItem(49, GuiUtils.createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>", List.of("<gray>Tutup menu kalender</gray>"), false));
        inventory.setItem(53, GuiUtils.createItem(Material.EMERALD, "<aqua><bold>🔄 Fitur Catch-up</bold></aqua>", List.of("<gray>Tebus hadiah dari hari yang terlewat</gray>"), false));
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

        if (slot == 53) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            new CatchUpGUI(plugin, player).open();
            return;
        }

        Integer day = slotToDayMap.get(slot);
        if (day != null) {
            int today = LocalDate.now().getDayOfMonth();
            PlayerStreakData data = plugin.getStreakService().getStreakData(player.getUniqueId());

            if (day == today && (data == null || !data.isDayClaimed(day))) {
                boolean success = plugin.getCalendarService().claimToday(player);
                if (success) {
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
                buildGUI();
            } else if (day < today && (data == null || !data.isDayClaimed(day))) {
                // Direct catch-up claim for this day!
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
                boolean caughtUp = plugin.getCalendarService().catchUpClaim(player, day);
                if (caughtUp) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
                buildGUI();
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
