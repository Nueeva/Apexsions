package com.apexsions.quests.gui.dialog;

import com.apexsions.quests.ApexsionsQuests;
import com.apexsions.quests.model.PlayerStreakData;
import com.apexsions.quests.service.CalendarService;
import com.apexsions.quests.service.StreakService;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DailyLoginDialog {

    public static void open(ApexsionsQuests plugin, Player player) {
        if (player == null || !player.isOnline()) return;

        PlayerStreakData data = plugin.getStreakService().getStreakData(player.getUniqueId());
        if (data == null) return;

        int today = LocalDate.now().getDayOfMonth();
        boolean todayClaimed = data.isDayClaimed(today);
        int currentStreak = data.getCurrentStreak();
        double passiveBoost = plugin.getStreakService().getPassiveBonusMultiplier(player.getUniqueId()) * 100;

        String title = "<gradient:#f39c12:#f1c40f><bold>KALENDER HADIAH HARIAN</bold></gradient>";
        StringBuilder desc = new StringBuilder();
        desc.append("<yellow>Streak Anda:</yellow> <gold><bold>").append(currentStreak).append(" Hari</bold></gold>");
        if (passiveBoost > 0) {
            desc.append(" <green>(+").append(String.format("%.0f", passiveBoost)).append("% Boost Pendapatan)</green>");
        }
        desc.append("\n<gray>Streak Freeze Tersedia:</gray> <aqua>").append(data.getStreakFreezeCount()).append("x</aqua>");
        desc.append("\n<gray>Status Hari Ini (Hari ke-").append(today).append("):</gray> ")
                .append(todayClaimed ? "<green><bold>SUDAH DIKLAIM</bold></green>" : "<gold><bold>BISA DIKLAIM SEKARANG!</bold></gold>");

        List<NativeDialogAdapter.DialogButtonData> buttons = new ArrayList<>();

        // 1. Primary Action: Claim Today
        if (!todayClaimed) {
            buttons.add(new NativeDialogAdapter.DialogButtonData(
                    "<green><bold>🎁 KLAIM HADIAH HARI INI</bold></green>",
                    "Klik untuk mengklaim hadiah hari ini",
                    () -> {
                        plugin.getCalendarService().claimToday(player);
                        open(plugin, player);
                    }
            ));
        } else {
            buttons.add(new NativeDialogAdapter.DialogButtonData(
                    "<gray>✔ Hari Ini Sudah Diklaim</gray>",
                    "Kembali besok untuk klaim hari berikutnya",
                    () -> {}
            ));
        }

        // 2. Navigation to 4 Weeks
        buttons.add(new NativeDialogAdapter.DialogButtonData(
                "<yellow>📜 Minggu 1: Awakening (Hari 1-7)</yellow>",
                "Buka detail hadiah Minggu 1",
                () -> openWeekDialog(plugin, player, 1, 1, 7)
        ));

        buttons.add(new NativeDialogAdapter.DialogButtonData(
                "<yellow>📜 Minggu 2: Prosperity (Hari 8-14)</yellow>",
                "Buka detail hadiah Minggu 2",
                () -> openWeekDialog(plugin, player, 2, 8, 14)
        ));

        buttons.add(new NativeDialogAdapter.DialogButtonData(
                "<yellow>📜 Minggu 3: Valor (Hari 15-21)</yellow>",
                "Buka detail hadiah Minggu 3",
                () -> openWeekDialog(plugin, player, 3, 15, 21)
        ));

        buttons.add(new NativeDialogAdapter.DialogButtonData(
                "<yellow>📜 Minggu 4: Supremacy (Hari 22-28)</yellow>",
                "Buka detail hadiah Minggu 4",
                () -> openWeekDialog(plugin, player, 4, 22, 28)
        ));

        // 3. Finale Days (29-31)
        buttons.add(new NativeDialogAdapter.DialogButtonData(
                "<gold><bold>⭐ The Golden Apex Finale (Hari 29-31)</bold></gold>",
                "Buka hadiah finale & fitur catch-up",
                () -> openFinaleDialog(plugin, player)
        ));

        // 4. Cumulative Streak Milestones
        buttons.add(new NativeDialogAdapter.DialogButtonData(
                "<aqua><bold>🏆 Hadiah Akumulasi Streak</bold></aqua>",
                "Klaim hadiah streak berturut-turut tanpa putus",
                () -> openMilestonesDialog(plugin, player)
        ));

        NativeDialogAdapter.DialogButtonData exitBtn = new NativeDialogAdapter.DialogButtonData(
                "<red>✖ Tutup</red>",
                "Tutup menu kalender",
                () -> {}
        );

        org.bukkit.inventory.ItemStack clockIcon = new org.bukkit.inventory.ItemStack(org.bukkit.Material.CLOCK);
        NativeDialogAdapter.showMultiActionDialog(plugin, player, clockIcon, title, desc.toString(), buttons, exitBtn, 2);
    }

    public static void openWeekDialog(ApexsionsQuests plugin, Player player, int weekNum, int startDay, int endDay) {
        PlayerStreakData data = plugin.getStreakService().getStreakData(player.getUniqueId());
        if (data == null) return;

        int today = LocalDate.now().getDayOfMonth();
        String title = "<gradient:#f39c12:#f1c40f><bold>KALENDER MINGGU KE-" + weekNum + "</bold></gradient>";
        StringBuilder desc = new StringBuilder("<gray>Daftar hadiah hari ke-" + startDay + " s/d " + endDay + ":</gray>\n");

        List<NativeDialogAdapter.DialogButtonData> buttons = new ArrayList<>();

        for (int day = startDay; day <= endDay; day++) {
            final int targetDay = day;
            boolean isClaimed = data.isDayClaimed(day);
            boolean isToday = (day == today);
            CalendarService.DayReward reward = plugin.getCalendarService().getReward(day);
            String rewardDesc = (reward != null) ? reward.getDescription() : "Hadiah Harian";

            String btnLabel;
            if (isClaimed) {
                btnLabel = "<green>✔ Hari " + day + ": " + rewardDesc + "</green>";
            } else if (isToday) {
                btnLabel = "<gold>⭐ [HARI INI] Hari " + day + ": " + rewardDesc + "</gold>";
            } else if (day < today) {
                btnLabel = "<red>✖ Hari " + day + " (Terlewat): " + rewardDesc + "</red>";
            } else {
                btnLabel = "<gray>🔒 Hari " + day + ": " + rewardDesc + "</gray>";
            }

            buttons.add(new NativeDialogAdapter.DialogButtonData(
                    btnLabel,
                    rewardDesc,
                    () -> {
                        if (isToday && !isClaimed) {
                            plugin.getCalendarService().claimToday(player);
                            openWeekDialog(plugin, player, weekNum, startDay, endDay);
                        }
                    }
            ));
        }

        NativeDialogAdapter.DialogButtonData backBtn = new NativeDialogAdapter.DialogButtonData(
                "<yellow>⬅ Kembali ke Menu Utama</yellow>",
                "Kembali ke menu kalender utama",
                () -> open(plugin, player)
        );

        org.bukkit.inventory.ItemStack chestIcon = new org.bukkit.inventory.ItemStack(org.bukkit.Material.CHEST);
        NativeDialogAdapter.showMultiActionDialog(plugin, player, chestIcon, title, desc.toString(), buttons, backBtn, 2);
    }

    public static void openFinaleDialog(ApexsionsQuests plugin, Player player) {
        PlayerStreakData data = plugin.getStreakService().getStreakData(player.getUniqueId());
        if (data == null) return;

        int today = LocalDate.now().getDayOfMonth();
        String title = "<gold><bold>⭐ THE GOLDEN APEX FINALE ⭐</bold></gold>";
        StringBuilder desc = new StringBuilder("<yellow>Puncak akhir bulan peradaban (Hari 29-31)!</yellow>\n");
        desc.append("<gray>Pemain yang mempertahankan streak dapat mengambil Zenith Box eksklusif.\n");
        desc.append("Hari terlewat di minggu 1-4 dapat ditebus dengan fitur Catch-up!</gray>");

        List<NativeDialogAdapter.DialogButtonData> buttons = new ArrayList<>();

        for (int day = 29; day <= 31; day++) {
            final int d = day;
            CalendarService.DayReward rew = plugin.getCalendarService().getReward(day);
            if (rew != null) {
                boolean claimed = data.isDayClaimed(day);
                boolean isToday = (day == today);
                String label;
                if (claimed) {
                    label = "<green>✔ Hari " + day + ": " + rew.getDescription() + "</green>";
                } else if (isToday) {
                    label = "<gold>⭐ [KLAIM SEKARANG] Hari " + day + ": " + rew.getDescription() + "</gold>";
                } else {
                    label = "<gray>🔒 Hari " + day + ": " + rew.getDescription() + "</gray>";
                }

                buttons.add(new NativeDialogAdapter.DialogButtonData(
                        label,
                        rew.getDescription(),
                        () -> {
                            if (isToday && !claimed) {
                                plugin.getCalendarService().claimToday(player);
                                openFinaleDialog(plugin, player);
                            }
                        }
                ));
            }
        }

        // Catch-up missed day option
        buttons.add(new NativeDialogAdapter.DialogButtonData(
                "<aqua>🔄 Tebus Hari Terlewat (Catch-up)</aqua>",
                "Tebus hadiah dari hari-hari sebelumnya yang sempat terlewat",
                () -> openCatchUpListDialog(plugin, player)
        ));

        NativeDialogAdapter.DialogButtonData backBtn = new NativeDialogAdapter.DialogButtonData(
                "<yellow>⬅ Kembali ke Menu Utama</yellow>",
                "Kembali",
                () -> open(plugin, player)
        );

        org.bukkit.inventory.ItemStack finaleIcon = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHER_STAR);
        NativeDialogAdapter.showMultiActionDialog(plugin, player, finaleIcon, title, desc.toString(), buttons, backBtn, 2);
    }

    public static void openCatchUpListDialog(ApexsionsQuests plugin, Player player) {
        PlayerStreakData data = plugin.getStreakService().getStreakData(player.getUniqueId());
        if (data == null) return;

        int today = LocalDate.now().getDayOfMonth();
        double cost = plugin.getConfig().getDouble("calendar.catch-up-cost", 5000.0);

        String title = "<aqua><bold>PILIH HARI UNTUK DITEBUS (CATCH-UP)</bold></aqua>";
        String desc = "<gray>Biaya tebus: <gold>Rp " + String.format("%,.0f", cost) + "</gold> per hari.</gray>";

        List<NativeDialogAdapter.DialogButtonData> buttons = new ArrayList<>();

        for (int day = 1; day < today; day++) {
            final int d = day;
            if (!data.isDayClaimed(day)) {
                CalendarService.DayReward r = plugin.getCalendarService().getReward(day);
                String rDesc = (r != null) ? r.getDescription() : "Hadiah Harian";
                buttons.add(new NativeDialogAdapter.DialogButtonData(
                        "<yellow>Tebus Hari ke-" + day + ": " + rDesc + "</yellow>",
                        "Klik untuk menebus dengan saldo Rp " + String.format("%,.0f", cost),
                        () -> {
                            plugin.getCalendarService().catchUpClaim(player, d);
                            openCatchUpListDialog(plugin, player);
                        }
                ));
            }
        }

        if (buttons.isEmpty()) {
            buttons.add(new NativeDialogAdapter.DialogButtonData(
                    "<green>Semua Hari Sudah Terklaim! Tidak ada hari yang terlewat.</green>",
                    "",
                    () -> {}
            ));
        }

        NativeDialogAdapter.DialogButtonData backBtn = new NativeDialogAdapter.DialogButtonData(
                "<yellow>⬅ Kembali ke Finale</yellow>",
                "Kembali",
                () -> openFinaleDialog(plugin, player)
        );

        org.bukkit.inventory.ItemStack goldIcon = new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLD_INGOT);
        NativeDialogAdapter.showMultiActionDialog(plugin, player, goldIcon, title, desc, buttons, backBtn, 2);
    }

    public static void openMilestonesDialog(ApexsionsQuests plugin, Player player) {
        PlayerStreakData data = plugin.getStreakService().getStreakData(player.getUniqueId());
        if (data == null) return;

        String title = "<gradient:#2980b9:#3498db><bold>HADIAH AKUMULASI STREAK</bold></gradient>";
        String desc = "<yellow>Streak Anda saat ini: <gold><bold>" + data.getCurrentStreak() + " Hari</bold></gold></yellow>\n"
                + "<gray>Milestone streak terus terakumulasi tanpa di-reset saat ganti bulan!</gray>";

        List<NativeDialogAdapter.DialogButtonData> buttons = new ArrayList<>();

        for (Map.Entry<Integer, StreakService.MilestoneReward> entry : plugin.getStreakService().getMilestones().entrySet()) {
            final int days = entry.getKey();
            StreakService.MilestoneReward rew = entry.getValue();
            boolean claimed = data.isMilestoneClaimed(days);
            boolean canClaim = data.getCurrentStreak() >= days && !claimed;

            String label;
            if (claimed) {
                label = "<green>✔ Streak " + days + " Hari: " + rew.getDescription() + "</green>";
            } else if (canClaim) {
                label = "<gold>⭐ [KLAIM SEKARANG] Streak " + days + " Hari: " + rew.getDescription() + "</gold>";
            } else {
                label = "<gray>🔒 Streak " + days + " Hari: " + rew.getDescription() + "</gray>";
            }

            buttons.add(new NativeDialogAdapter.DialogButtonData(
                    label,
                    rew.getDescription(),
                    () -> {
                        if (canClaim) {
                            plugin.getStreakService().claimMilestone(player, days);
                            openMilestonesDialog(plugin, player);
                        }
                    }
            ));
        }

        NativeDialogAdapter.DialogButtonData backBtn = new NativeDialogAdapter.DialogButtonData(
                "<yellow>⬅ Kembali ke Menu Utama</yellow>",
                "Kembali",
                () -> open(plugin, player)
        );

        org.bukkit.inventory.ItemStack totemIcon = new org.bukkit.inventory.ItemStack(org.bukkit.Material.TOTEM_OF_UNDYING);
        NativeDialogAdapter.showMultiActionDialog(plugin, player, totemIcon, title, desc, buttons, backBtn, 2);
    }
}
