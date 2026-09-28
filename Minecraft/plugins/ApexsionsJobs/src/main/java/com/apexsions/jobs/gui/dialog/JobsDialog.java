package com.apexsions.jobs.gui.dialog;

import com.apexsions.jobs.ApexsionsJobs;
import com.apexsions.jobs.model.JobDefinition;
import com.apexsions.jobs.model.PlayerJobData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JobsDialog {

    public static void open(ApexsionsJobs plugin, Player player) {
        if (player == null || !player.isOnline()) return;

        Map<String, PlayerJobData> playerJobs = plugin.getJobService().getPlayerJobs(player.getUniqueId());
        PlayerJobData active = plugin.getJobService().getActiveJobData(player);

        String title = "<gradient:#27ae60:#2ecc71><bold>PROFESI PERADABAN APEXSIONS</bold></gradient>";
        StringBuilder desc = new StringBuilder();
        if (active != null) {
            JobDefinition def = plugin.getJobService().getDefinition(active.getJobId());
            String jobName = def != null ? def.getName() : active.getJobId();
            desc.append("<yellow>Profesi Aktif:</yellow> <gold><bold>").append(jobName)
                    .append("</bold></gold> <aqua>(Lv. ").append(active.getLevel()).append(")</aqua>\n");
            desc.append("<gray>Gaji Hari Ini:</gray> <green>Rp ").append(String.format("%,.0f", active.getDailyEarnings())).append("</green>\n");
        } else {
            desc.append("<red>Anda belum memiliki profesi aktif!</red> Silakan pilih salah satu profesi di bawah.\n");
        }
        desc.append("<gray>Pilih salah satu profesi untuk melihat detail, gaji, atau bergabung:</gray>");

        List<NativeDialogAdapter.DialogButtonData> buttons = new ArrayList<>();

        for (JobDefinition def : plugin.getJobService().getDefinitions().values()) {
            PlayerJobData data = playerJobs.get(def.getId().toLowerCase());
            int lvl = data != null ? data.getLevel() : 1;
            boolean isActive = data != null && data.isActive();

            String label;
            if (isActive) {
                label = "<green>✔ [AKTIF] " + def.getName() + " (Lv. " + lvl + ")</green>";
            } else {
                label = "<yellow>🔨 " + def.getName() + " (Lv. " + lvl + ") - " + def.getTitle() + "</yellow>";
            }

            buttons.add(new NativeDialogAdapter.DialogButtonData(
                    label,
                    def.getDescription(),
                    () -> openJobDetailDialog(plugin, player, def, data)
            ));
        }

        NativeDialogAdapter.DialogButtonData exitBtn = new NativeDialogAdapter.DialogButtonData(
                "<red>✖ Tutup</red>",
                "Tutup menu pekerjaan",
                () -> {}
        );

        NativeDialogAdapter.showMultiActionDialog(plugin, player, title, desc.toString(), buttons, exitBtn, 1);
    }

    public static void openJobDetailDialog(ApexsionsJobs plugin, Player player, JobDefinition def, PlayerJobData data) {
        int lvl = data != null ? data.getLevel() : 1;
        double exp = data != null ? data.getExp() : 0.0;
        double reqExp = data != null ? data.getRequiredExp() : (100.0 * lvl * 1.25);
        boolean isActive = data != null && data.isActive();

        double baseCap = plugin.getConfig().getDouble("anti-abuse.daily-base-cap", 150000.0);
        double capPerLvl = plugin.getConfig().getDouble("anti-abuse.daily-cap-per-level", 10000.0);
        double dailyCap = baseCap + (lvl * capPerLvl);
        double dailyEarned = data != null ? data.getDailyEarnings() : 0.0;

        String title = "<gradient:#27ae60:#2ecc71><bold>DETAIL PROFESI: " + def.getName().toUpperCase() + "</bold></gradient>";
        StringBuilder desc = new StringBuilder();
        desc.append("<gold><bold>").append(def.getTitle()).append("</bold></gold>\n");
        desc.append("<gray>").append(def.getDescription()).append("</gray>\n\n");
        desc.append("<yellow>Statistik Anda:</yellow>\n");
        desc.append(" <aqua>• Level Profesi: Lv. ").append(lvl).append(" / ").append(def.getMaxLevel()).append("</aqua>\n");
        desc.append(" <aqua>• Progres EXP: ").append(String.format("%.0f", exp)).append(" / ").append(String.format("%.0f", reqExp)).append(" EXP</aqua>\n");
        desc.append(" <green>• Batas Gaji Harian: Rp ").append(String.format("%,.0f", dailyEarned)).append(" / Rp ").append(String.format("%,.0f", dailyCap)).append("</green>\n");
        desc.append(" <yellow>• Bonus Gaji Level: +").append(lvl * 5).append("%</yellow>\n");
        desc.append(" <gray>• Status: ").append(isActive ? "<green><bold>SEDANG AKTIF</bold></green>" : "<gray>Tidak Aktif</gray>").append("</gray>");

        List<NativeDialogAdapter.DialogButtonData> buttons = new ArrayList<>();

        if (!isActive) {
            buttons.add(new NativeDialogAdapter.DialogButtonData(
                    "<green><bold>💼 BERGABUNG DENGAN PROFESI INI</bold></green>",
                    "Jadikan " + def.getName() + " sebagai profesi aktif Anda",
                    () -> {
                        plugin.getJobService().joinJob(player, def.getId());
                        open(plugin, player);
                    }
            ));
        } else {
            buttons.add(new NativeDialogAdapter.DialogButtonData(
                    "<red><bold>🚪 KELUAR DARI PROFESI INI</bold></red>",
                    "Nonaktifkan profesi " + def.getName(),
                    () -> {
                        plugin.getJobService().leaveJob(player, def.getId());
                        open(plugin, player);
                    }
            ));
        }

        NativeDialogAdapter.DialogButtonData backBtn = new NativeDialogAdapter.DialogButtonData(
                "<yellow>⬅ Kembali ke Daftar Profesi</yellow>",
                "Kembali",
                () -> open(plugin, player)
        );

        NativeDialogAdapter.showMultiActionDialog(plugin, player, title, desc.toString(), buttons, backBtn, 1);
    }
}
