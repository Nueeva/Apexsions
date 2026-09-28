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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DailyCalendarGUI implements QuestsGuiHolder {

    private final ApexsionsQuests plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public DailyCalendarGUI(ApexsionsQuests plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#f39c12:#f1c40f><bold>✦ KALENDER HADIAH HARIAN ✦</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        GuiUtils.fillStandardBorder54(inventory);

        PlayerStreakData data = plugin.getStreakService().getStreakData(player.getUniqueId());
        int today = LocalDate.now().getDayOfMonth();
        boolean todayClaimed = (data != null && data.isDayClaimed(today));
        int currentStreak = (data != null) ? data.getCurrentStreak() : 0;
        int freezeCount = (data != null) ? data.getStreakFreezeCount() : 0;
        double passiveBoost = plugin.getStreakService().getPassiveBonusMultiplier(player.getUniqueId()) * 100;

        // 1. Header Slot 4: Player Head & Streak Info
        List<String> headLore = new ArrayList<>();
        headLore.add("<gray>Pemain:</gray> <yellow>" + player.getName() + "</yellow>");
        headLore.add("<gray>Streak Berjalan:</gray> <gold><bold>" + currentStreak + " Hari</bold></gold>");
        if (passiveBoost > 0) {
            headLore.add("<gray>Boost Pendapatan:</gray> <green>+" + String.format("%.0f", passiveBoost) + "% Saldo & XP</green>");
        }
        headLore.add("<gray>Streak Freeze Tersedia:</gray> <aqua>" + freezeCount + "x</aqua>");
        headLore.add("<gray>Status Hari Ini (Hari ke-" + today + "):</gray> "
                + (todayClaimed ? "<green><bold>SUDAH DIKLAIM</bold></green>" : "<gold><bold>BISA DIKLAIM SEKARANG!</bold></gold>"));
        headLore.add("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>");
        headLore.add("<yellow>Klaim setiap hari untuk mempertahankan</yellow>");
        headLore.add("<yellow>streak dan membuka hadiah akumulasi!</yellow>");

        inventory.setItem(4, GuiUtils.createHead(player, "<gradient:#f39c12:#f1c40f><bold>Profil Streak & Kalender</bold></gradient>", headLore));

        // 2. Slot 20: Primary Action - Claim Today
        CalendarService.DayReward todayReward = plugin.getCalendarService().getReward(today);
        String rewardDesc = (todayReward != null) ? todayReward.getDescription() : "Hadiah Hari Ini";

        List<String> claimLore = new ArrayList<>();
        claimLore.add("<gray>Hari ke-" + today + " Bulan Ini</gray>");
        claimLore.add("<gray>Hadiah: </gray><gold>" + rewardDesc + "</gold>");
        claimLore.add("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>");
        if (!todayClaimed) {
            claimLore.add("<green><bold>▶ KLIK UNTUK MENGKLAIM SEKARANG!</bold></green>");
            inventory.setItem(20, GuiUtils.createItem(Material.CHEST_MINECART, "<green><bold>🎁 KLAIM HADIAH HARI INI</bold></green>", claimLore, true));
        } else {
            claimLore.add("<gray>✔ Sudah berhasil diklaim hari ini.</gray>");
            claimLore.add("<yellow>Datang kembali besok pukul 00:00 WIB!</yellow>");
            inventory.setItem(20, GuiUtils.createItem(Material.MINECART, "<gray>✔ Hadiah Hari Ini Sudah Terklaim</gray>", claimLore, false));
        }

        // 3. Navigation Buttons for Weeks
        // Slot 22: Week 1 (Hari 1-7)
        List<String> w1Lore = List.of(
                "<gray>Rentang: Hari ke-1 s/d ke-7</gray>",
                "<gray>Tema: <yellow>Awakening of Civilizations</yellow></gray>",
                "<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>",
                "<yellow>▶ Klik untuk melihat grid hadiah Minggu 1</yellow>"
        );
        inventory.setItem(22, GuiUtils.createItem(Material.BOOK, "<gold><bold>📜 Minggu 1: Awakening</bold></gold>", w1Lore, today <= 7));

        // Slot 23: Week 2 (Hari 8-14)
        List<String> w2Lore = List.of(
                "<gray>Rentang: Hari ke-8 s/d ke-14</gray>",
                "<gray>Tema: <yellow>Prosperity of the Realm</yellow></gray>",
                "<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>",
                "<yellow>▶ Klik untuk melihat grid hadiah Minggu 2</yellow>"
        );
        inventory.setItem(23, GuiUtils.createItem(Material.BOOK, "<gold><bold>📜 Minggu 2: Prosperity</bold></gold>", w2Lore, today >= 8 && today <= 14));

        // Slot 24: Week 3 (Hari 15-21)
        List<String> w3Lore = List.of(
                "<gray>Rentang: Hari ke-15 s/d ke-21</gray>",
                "<gray>Tema: <yellow>Valor of the Champions</yellow></gray>",
                "<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>",
                "<yellow>▶ Klik untuk melihat grid hadiah Minggu 3</yellow>"
        );
        inventory.setItem(24, GuiUtils.createItem(Material.BOOK, "<gold><bold>📜 Minggu 3: Valor</bold></gold>", w3Lore, today >= 15 && today <= 21));

        // Slot 25: Week 4 (Hari 22-28)
        List<String> w4Lore = List.of(
                "<gray>Rentang: Hari ke-22 s/d ke-28</gray>",
                "<gray>Tema: <yellow>Supremacy of the Apex</yellow></gray>",
                "<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>",
                "<yellow>▶ Klik untuk melihat grid hadiah Minggu 4</yellow>"
        );
        inventory.setItem(25, GuiUtils.createItem(Material.BOOK, "<gold><bold>📜 Minggu 4: Supremacy</bold></gold>", w4Lore, today >= 22 && today <= 28));

        // 4. Slot 31: The Golden Apex Finale (Hari 29-31)
        List<String> finaleLore = List.of(
                "<gray>Rentang: Hari ke-29 s/d ke-31</gray>",
                "<gray>Puncak akhir bulan peradaban!</gray>",
                "<yellow>Berisi Zenith Box & hadiah legendaris.</yellow>",
                "<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>",
                "<yellow>▶ Klik untuk melihat hadiah Finale & Catch-up</yellow>"
        );
        inventory.setItem(31, GuiUtils.createItem(Material.NETHER_STAR, "<gradient:#f1c40f:#e67e22><bold>⭐ The Golden Apex Finale</bold></gradient>", finaleLore, today >= 29));

        // 5. Slot 33: Cumulative Streak Milestones
        List<String> milestoneLore = List.of(
                "<gray>Milestone: 3, 7, 14, 21, 28, 30, 60, 90, 100 Hari</gray>",
                "<gray>Streak terakumulasi tanpa di-reset saat ganti bulan.</gray>",
                "<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>",
                "<aqua>▶ Klik untuk melihat & klaim Hadiah Milestone</aqua>"
        );
        inventory.setItem(33, GuiUtils.createItem(Material.TOTEM_OF_UNDYING, "<aqua><bold>🏆 Hadiah Akumulasi Streak</bold></aqua>", milestoneLore, true));

        // 6. Slot 48: Catch-up Missed Days Shortcut
        double catchUpCost = plugin.getConfig().getDouble("calendar.catch-up-cost", 5000.0);
        List<String> catchUpLore = List.of(
                "<gray>Sempat terlewat login beberapa hari?</gray>",
                "<gray>Tebus hadiah hari yang terlewat!</gray>",
                "<gold>Biaya Tebus: Rp " + String.format("%,.0f", catchUpCost) + " / hari</gold>",
                "<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>",
                "<yellow>▶ Klik untuk memilih hari yang ingin ditebus</yellow>"
        );
        inventory.setItem(48, GuiUtils.createItem(Material.GOLD_INGOT, "<gold><bold>🔄 Tebus Hari Terlewat (Catch-up)</bold></gold>", catchUpLore, false));

        // 7. Slot 49: Close Button
        inventory.setItem(49, GuiUtils.createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>", List.of("<gray>Tutup menu kalender</gray>"), false));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slot == 49) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (slot == 20) {
            int today = LocalDate.now().getDayOfMonth();
            PlayerStreakData data = plugin.getStreakService().getStreakData(player.getUniqueId());
            if (data != null && !data.isDayClaimed(today)) {
                boolean success = plugin.getCalendarService().claimToday(player);
                if (success) {
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
                buildGUI();
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
            return;
        }

        if (slot == 22) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            new WeekCalendarGUI(plugin, player, 1, 1, 7).open();
            return;
        }

        if (slot == 23) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            new WeekCalendarGUI(plugin, player, 2, 8, 14).open();
            return;
        }

        if (slot == 24) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            new WeekCalendarGUI(plugin, player, 3, 15, 21).open();
            return;
        }

        if (slot == 25) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            new WeekCalendarGUI(plugin, player, 4, 22, 28).open();
            return;
        }

        if (slot == 31) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            new WeekCalendarGUI(plugin, player, 5, 29, 31).open();
            return;
        }

        if (slot == 33) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            new StreakMilestonesGUI(plugin, player).open();
            return;
        }

        if (slot == 48) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            new CatchUpGUI(plugin, player).open();
            return;
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
