package com.apexsions.jobs.gui;

import com.apexsions.jobs.ApexsionsJobs;
import com.apexsions.jobs.model.JobDefinition;
import com.apexsions.jobs.model.PlayerJobData;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobsMainGUI implements JobsGuiHolder {

    private final ApexsionsJobs plugin;
    private final Player player;
    private final Inventory inventory;
    private final Map<Integer, String> slotToJobIdMap = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    private static final Map<String, Integer> JOB_SLOTS = Map.of(
            "miner", 20,
            "hunter", 22,
            "lumberjack", 24,
            "farmer", 29,
            "fisherman", 31,
            "blacksmith", 33
    );

    public JobsMainGUI(ApexsionsJobs plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#27ae60:#2ecc71><bold>✦ PROFESI PERADABAN APEXSIONS ✦</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotToJobIdMap.clear();
        JobsGuiUtils.fillStandardBorder54(inventory);

        Map<String, PlayerJobData> playerJobs = plugin.getJobService().getPlayerJobs(player.getUniqueId());
        PlayerJobData active = plugin.getJobService().getActiveJobData(player);

        // 1. Header Slot 4
        List<String> headLore = new ArrayList<>();
        headLore.add("<gray>Pemain:</gray> <yellow>" + player.getName() + "</yellow>");
        if (active != null) {
            JobDefinition def = plugin.getJobService().getDefinition(active.getJobId());
            String jobName = (def != null) ? def.getName() : active.getJobId();
            double baseCap = plugin.getConfig().getDouble("anti-abuse.daily-base-cap", 150000.0);
            double capPerLvl = plugin.getConfig().getDouble("anti-abuse.daily-cap-per-level", 10000.0);
            double dailyCap = baseCap + (active.getLevel() * capPerLvl);

            headLore.add("<gray>Profesi Aktif:</gray> <gold><bold>" + jobName + "</bold></gold> <aqua>(Lv. " + active.getLevel() + ")</aqua>");
            headLore.add("<gray>Gaji Hari Ini:</gray> <green>Rp " + String.format("%,.0f", active.getDailyEarnings()) + "</green> / <gray>Rp " + String.format("%,.0f", dailyCap) + "</gray>");
            headLore.add("<gray>Progres EXP:</gray> <yellow>" + String.format("%.0f", active.getExp()) + "</yellow> / <gray>" + String.format("%.0f", active.getRequiredExp()) + " EXP</gray>");
        } else {
            headLore.add("<red><bold>Belum Memiliki Profesi Aktif</bold></red>");
            headLore.add("<yellow>Pilih salah satu profesi di bawah untuk mulai bekerja!</yellow>");
        }
        headLore.add("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>");
        headLore.add("<gray>Setiap pemain hanya dapat mengaktifkan 1 profesi.</gray>");
        headLore.add("<gray>Level dan EXP tersimpan saat berganti profesi.</gray>");

        inventory.setItem(4, JobsGuiUtils.createHead(player, "<gradient:#27ae60:#2ecc71><bold>Status Profesi Anda</bold></gradient>", headLore));

        // 2. Profession Slots
        for (JobDefinition def : plugin.getJobService().getDefinitions().values()) {
            String id = def.getId().toLowerCase();
            Integer slot = JOB_SLOTS.get(id);
            if (slot == null) continue;

            slotToJobIdMap.put(slot, id);
            PlayerJobData data = (playerJobs != null) ? playerJobs.get(id) : null;
            int lvl = (data != null) ? data.getLevel() : 1;
            double exp = (data != null) ? data.getExp() : 0.0;
            double reqExp = (data != null) ? data.getRequiredExp() : (100.0 * lvl * 1.25);
            boolean isActive = (data != null && data.isActive());

            double baseCap = plugin.getConfig().getDouble("anti-abuse.daily-base-cap", 150000.0);
            double capPerLvl = plugin.getConfig().getDouble("anti-abuse.daily-cap-per-level", 10000.0);
            double dailyCap = baseCap + (lvl * capPerLvl);
            double dailyEarned = (data != null) ? data.getDailyEarnings() : 0.0;

            Material iconMat = switch (id) {
                case "miner" -> Material.DIAMOND_PICKAXE;
                case "hunter" -> Material.DIAMOND_SWORD;
                case "lumberjack" -> Material.DIAMOND_AXE;
                case "farmer" -> Material.DIAMOND_HOE;
                case "fisherman" -> Material.FISHING_ROD;
                case "blacksmith" -> Material.ANVIL;
                default -> Material.IRON_PICKAXE;
            };

            String progressBar = createProgressBar(exp, reqExp, 10);

            List<String> lore = new ArrayList<>();
            lore.add("<gold><bold>" + def.getTitle() + "</bold></gold>");
            lore.add("<gray>" + def.getDescription() + "</gray>");
            lore.add("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>");
            lore.add("<yellow>Statistik Profesi:</yellow>");
            lore.add(" <aqua>• Tingkat: Lv. " + lvl + " / " + def.getMaxLevel() + "</aqua>");
            lore.add(" <aqua>• EXP: </aqua>" + progressBar + " <yellow>" + String.format("%.0f", exp) + "/" + String.format("%.0f", reqExp) + "</yellow>");
            lore.add(" <green>• Gaji Hari Ini: Rp " + String.format("%,.0f", dailyEarned) + " / Rp " + String.format("%,.0f", dailyCap) + "</green>");
            lore.add(" <yellow>• Bonus Level: +" + (lvl * 5) + "% Gaji</yellow>");
            lore.add("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>");

            if (isActive) {
                lore.add("<green><bold>✔ SEDANG AKTIF</bold></green>");
                lore.add("<yellow>▶ Klik Kiri: Lihat Detail Tindakan & Gaji</yellow>");
                lore.add("<red>▶ Shift + Klik Kanan: Keluar dari Profesi</red>");
                inventory.setItem(slot, JobsGuiUtils.createItem(iconMat, "<green><bold>[AKTIF] " + def.getName() + " (Lv. " + lvl + ")</bold></green>", lore, true));
            } else {
                lore.add("<gray>Status: Tidak Aktif</gray>");
                lore.add("<yellow>▶ Klik Kiri: Lihat Detail Tindakan & Gaji</yellow>");
                lore.add("<green><bold>▶ Klik Kanan: Langsung Bergabung!</bold></green>");
                inventory.setItem(slot, JobsGuiUtils.createItem(iconMat, "<gold><bold>" + def.getName() + " (Lv. " + lvl + ")</bold></gold>", lore, false));
            }
        }

        // 3. Info Anti-Abuse Slot 40
        List<String> infoLore = List.of(
                "<gray>Sistem Keamanan Gaji Apexsions:</gray>",
                "<gray>• Batas gaji harian per profesi mencegah inflasi.</gray>",
                "<gray>• Diminishing returns aktif pada tindakan berulang.</gray>",
                "<gray>• Blok yang dipasang pemain tidak menghasilkan uang.</gray>"
        );
        inventory.setItem(40, JobsGuiUtils.createItem(Material.SHIELD, "<aqua><bold>Pedoman Integritas Profesi</bold></aqua>", infoLore, false));

        // 4. Close Button Slot 49
        inventory.setItem(49, JobsGuiUtils.createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>", List.of("<gray>Tutup menu profesi</gray>"), false));
    }

    private String createProgressBar(double current, double max, int totalBars) {
        float percent = (max > 0) ? Math.min(1.0f, (float) (current / max)) : 1.0f;
        int filled = (int) (totalBars * percent);
        StringBuilder sb = new StringBuilder("<green>");
        for (int i = 0; i < filled; i++) {
            sb.append("■");
        }
        sb.append("</green><gray>");
        for (int i = filled; i < totalBars; i++) {
            sb.append("□");
        }
        sb.append("</gray>");
        return sb.toString();
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slot == 49) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        String jobId = slotToJobIdMap.get(slot);
        if (jobId != null) {
            JobDefinition def = plugin.getJobService().getDefinition(jobId);
            if (def == null) return;

            PlayerJobData active = plugin.getJobService().getActiveJobData(player);
            boolean isThisActive = (active != null && active.getJobId().equalsIgnoreCase(jobId));

            if (event.isLeftClick()) {
                // Open Detail
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
                new JobDetailGUI(plugin, player, def).open();
            } else if (event.isRightClick()) {
                if (event.isShiftClick() && isThisActive) {
                    // Leave
                    boolean left = plugin.getJobService().leaveJob(player, jobId);
                    if (left) {
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.8f, 1.0f);
                    } else {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                    buildGUI();
                } else if (!isThisActive) {
                    // Join
                    boolean joined = plugin.getJobService().joinJob(player, jobId);
                    if (joined) {
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
                    } else {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                    buildGUI();
                }
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
