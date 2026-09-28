package com.apexsions.jobs.gui;

import com.apexsions.jobs.ApexsionsJobs;
import com.apexsions.jobs.model.JobDefinition;
import com.apexsions.jobs.model.JobRewardItem;
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
import java.util.List;

public class JobDetailGUI implements JobsGuiHolder {

    private static final int[] ACTION_SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final ApexsionsJobs plugin;
    private final Player player;
    private final JobDefinition def;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public JobDetailGUI(ApexsionsJobs plugin, Player player, JobDefinition def) {
        this.plugin = plugin;
        this.player = player;
        this.def = def;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#27ae60:#2ecc71><bold>DETAIL: " + def.getName().toUpperCase() + "</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        JobsGuiUtils.fillStandardBorder54(inventory);

        PlayerJobData data = plugin.getJobService().getPlayerJobs(player.getUniqueId()).get(def.getId().toLowerCase());
        PlayerJobData active = plugin.getJobService().getActiveJobData(player);
        boolean isActive = (active != null && active.getJobId().equalsIgnoreCase(def.getId()));

        int lvl = (data != null) ? data.getLevel() : 1;
        double exp = (data != null) ? data.getExp() : 0.0;
        double reqExp = (data != null) ? data.getRequiredExp() : (100.0 * lvl * 1.25);

        double baseCap = plugin.getConfig().getDouble("anti-abuse.daily-base-cap", 150000.0);
        double capPerLvl = plugin.getConfig().getDouble("anti-abuse.daily-cap-per-level", 10000.0);
        double dailyCap = baseCap + (lvl * capPerLvl);
        double dailyEarned = (data != null) ? data.getDailyEarnings() : 0.0;

        Material iconMat = switch (def.getId().toLowerCase()) {
            case "miner" -> Material.DIAMOND_PICKAXE;
            case "hunter" -> Material.DIAMOND_SWORD;
            case "lumberjack" -> Material.DIAMOND_AXE;
            case "farmer" -> Material.DIAMOND_HOE;
            case "fisherman" -> Material.FISHING_ROD;
            case "blacksmith" -> Material.ANVIL;
            default -> Material.IRON_PICKAXE;
        };

        // 1. Header Slot 4
        List<String> headLore = new ArrayList<>();
        headLore.add("<gold><bold>" + def.getTitle() + "</bold></gold>");
        headLore.add("<gray>" + def.getDescription() + "</gray>");
        headLore.add("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>");
        headLore.add("<yellow>Statistik Anda:</yellow>");
        headLore.add(" <aqua>• Tingkat: Lv. " + lvl + " / " + def.getMaxLevel() + "</aqua>");
        headLore.add(" <aqua>• Progres EXP: " + String.format("%.0f", exp) + " / " + String.format("%.0f", reqExp) + " EXP</aqua>");
        headLore.add(" <green>• Batas Gaji Harian: Rp " + String.format("%,.0f", dailyEarned) + " / Rp " + String.format("%,.0f", dailyCap) + "</green>");
        headLore.add(" <yellow>• Bonus Gaji Tingkat: +" + (lvl * 5) + "%</yellow>");
        headLore.add(" <gray>• Status: " + (isActive ? "<green><bold>SEDANG AKTIF</bold></green>" : "<gray>Tidak Aktif</gray>") + "</gray>");

        inventory.setItem(4, JobsGuiUtils.createItem(iconMat, "<gradient:#27ae60:#2ecc71><bold>" + def.getName() + "</bold></gradient>", headLore, isActive));

        // 2. Action Slots (Show reward table)
        int idx = 0;
        for (JobRewardItem rewardItem : def.getRewards().values()) {
            if (idx >= ACTION_SLOTS.length) break;
            int slot = ACTION_SLOTS[idx++];

            Material itemMat = Material.matchMaterial(rewardItem.getTarget());
            if (itemMat == null) itemMat = Material.PAPER;

            double payWithBonus = rewardItem.getPay() * (1.0 + (lvl * 0.05));

            List<String> actionLore = List.of(
                    "<gray>Tindakan: <yellow>" + rewardItem.getTarget().replace("_", " ") + "</yellow></gray>",
                    "<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>",
                    "<green>• Upah: Rp " + String.format("%.2f", payWithBonus) + "</green> <gray>(Base: Rp " + String.format("%.0f", rewardItem.getPay()) + ")</gray>",
                    "<aqua>• EXP: +" + String.format("%.0f", rewardItem.getExp()) + " Job EXP</aqua>"
            );

            inventory.setItem(slot, JobsGuiUtils.createItem(itemMat, "<gold><bold>" + rewardItem.getTarget().replace("_", " ") + "</bold></gold>", actionLore, false));
        }

        // 3. Action Button (Join or Leave) Slot 48
        if (isActive) {
            List<String> leaveLore = List.of(
                    "<gray>Nonaktifkan profesi " + def.getName() + ".</gray>",
                    "<gray>Level dan EXP Anda akan tetap tersimpan.</gray>",
                    "<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>",
                    "<red><bold>▶ KLIK UNTUK KELUAR DARI PROFESI</bold></red>"
            );
            inventory.setItem(48, JobsGuiUtils.createItem(Material.RED_CONCRETE, "<red><bold>🚪 KELUAR DARI PROFESI</bold></red>", leaveLore, false));
        } else {
            List<String> joinLore = List.of(
                    "<gray>Jadikan " + def.getName() + " sebagai profesi aktif Anda.</gray>",
                    "<gray>Anda akan mulai mendapatkan upah & EXP saat bekerja.</gray>",
                    "<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>",
                    "<green><bold>▶ KLIK UNTUK BERGABUNG DENGAN PROFESI</bold></green>"
            );
            inventory.setItem(48, JobsGuiUtils.createItem(Material.LIME_CONCRETE, "<green><bold>💼 BERGABUNG DENGAN PROFESI</bold></green>", joinLore, true));
        }

        // 4. Back Button Slot 49
        inventory.setItem(49, JobsGuiUtils.createItem(Material.ARROW, "<yellow><bold>⬅ KEMBALI KE DAFTAR PROFESI</bold></yellow>", List.of("<gray>Kembali ke menu utama profesi</gray>"), false));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slot == 49) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            new JobsMainGUI(plugin, player).open();
            return;
        }

        if (slot == 48) {
            PlayerJobData active = plugin.getJobService().getActiveJobData(player);
            boolean isActive = (active != null && active.getJobId().equalsIgnoreCase(def.getId()));

            if (isActive) {
                boolean left = plugin.getJobService().leaveJob(player, def.getId());
                if (left) {
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.8f, 1.0f);
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
            } else {
                boolean joined = plugin.getJobService().joinJob(player, def.getId());
                if (joined) {
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
            }
            buildGUI();
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
