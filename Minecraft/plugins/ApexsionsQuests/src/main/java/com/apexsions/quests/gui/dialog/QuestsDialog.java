package com.apexsions.quests.gui.dialog;

import com.apexsions.quests.ApexsionsQuests;
import com.apexsions.quests.model.PlayerQuestProgress;
import com.apexsions.quests.model.PlayerStreakData;
import com.apexsions.quests.model.Quest;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class QuestsDialog {

    public static void open(ApexsionsQuests plugin, Player player) {
        if (player == null || !player.isOnline()) return;

        List<PlayerQuestProgress> quests = plugin.getQuestManager().getPlayerQuests(player.getUniqueId());
        PlayerStreakData streakData = plugin.getStreakService().getStreakData(player.getUniqueId());

        int rerollsUsed = streakData != null ? streakData.getRerollsUsedToday() : 0;
        int maxRerolls = plugin.getConfig().getInt("quests.daily-rerolls-allowed", 1);
        int rerollsLeft = Math.max(0, maxRerolls - rerollsUsed);

        String title = "<gradient:#e67e22:#f39c12><bold>UNIVERSAL DAILY QUESTS</bold></gradient>";
        StringBuilder desc = new StringBuilder();
        desc.append("<gray>Tanggal:</gray> <yellow>").append(LocalDate.now()).append("</yellow> | ");
        desc.append("<gray>Jatah Reroll:</gray> <gold>").append(rerollsLeft).append("/").append(maxRerolls).append("</gold>\n");
        desc.append("<gray>Selesaikan misi harian untuk mendapatkan <green>Uang Rupiah</green>, <aqua>Core XP</aqua>, dan <yellow>Pass XP</yellow>!</gray>");

        List<NativeDialogAdapter.DialogButtonData> buttons = new ArrayList<>();

        if (quests == null || quests.isEmpty()) {
            buttons.add(new NativeDialogAdapter.DialogButtonData(
                    "<red>Belum ada misi yang dimuat. Klik untuk memuat.</red>",
                    "",
                    () -> {
                        plugin.getQuestManager().ensureDailyQuestsAssigned(player);
                        open(plugin, player);
                    }
            ));
        } else {
            for (PlayerQuestProgress p : quests) {
                Quest q = plugin.getQuestManager().getQuest(p.getQuestId());
                if (q == null) continue;

                String btnLabel;
                String tooltip = q.getDescription() + " (Hadiah: Rp " + String.format("%,.0f", q.getRewardMoney())
                        + " | +" + q.getRewardCoreXp() + " Core XP | +" + q.getRewardPassXp() + " Pass XP)";

                if (p.isClaimed()) {
                    btnLabel = "<green>✔ [TERKLAIM] " + q.getTitle() + " (" + p.getCurrentProgress() + "/" + p.getTargetProgress() + ")</green>";
                } else if (p.isCompleted()) {
                    btnLabel = "<gold>⭐ [KLAIM SEKARANG] " + q.getTitle() + " (" + p.getCurrentProgress() + "/" + p.getTargetProgress() + ")</gold>";
                } else {
                    btnLabel = "<yellow>⏳ " + q.getTitle() + " [" + p.getCurrentProgress() + "/" + p.getTargetProgress() + "]</yellow>";
                }

                buttons.add(new NativeDialogAdapter.DialogButtonData(
                        btnLabel,
                        tooltip,
                        () -> {
                            if (p.isCompleted() && !p.isClaimed()) {
                                plugin.getQuestManager().claimReward(player, p.getQuestId());
                                open(plugin, player);
                            } else if (!p.isCompleted() && rerollsLeft > 0) {
                                openQuestDetailDialog(plugin, player, p, q);
                            }
                        }
                ));
            }
        }

        NativeDialogAdapter.DialogButtonData exitBtn = new NativeDialogAdapter.DialogButtonData(
                "<red>✖ Tutup</red>",
                "Tutup menu misi",
                () -> {}
        );

        org.bukkit.inventory.ItemStack questIcon = new org.bukkit.inventory.ItemStack(org.bukkit.Material.WRITTEN_BOOK);
        NativeDialogAdapter.showMultiActionDialog(plugin, player, questIcon, title, desc.toString(), buttons, exitBtn, 2);
    }

    public static void openQuestDetailDialog(ApexsionsQuests plugin, Player player, PlayerQuestProgress progress, Quest quest) {
        String title = "<gradient:#e67e22:#f39c12><bold>DETAIL MISI: " + quest.getTitle().toUpperCase() + "</bold></gradient>";
        StringBuilder desc = new StringBuilder();
        desc.append("<gray>Deskripsi:</gray> <white>").append(quest.getDescription()).append("</white>\n");
        desc.append("<gray>Progres:</gray> <gold><bold>").append(progress.getCurrentProgress()).append(" / ").append(progress.getTargetProgress()).append("</bold></gold>\n\n");
        desc.append("<yellow>Total Hadiah:</yellow>\n");
        desc.append(" <green>• Saldo Rupiah: Rp ").append(String.format("%,.0f", quest.getRewardMoney())).append("</green>\n");
        desc.append(" <aqua>• Core Level XP: +").append(quest.getRewardCoreXp()).append(" XP</aqua>\n");
        desc.append(" <yellow>• BattlePass XP: +").append(quest.getRewardPassXp()).append(" Pass XP</yellow>");

        List<NativeDialogAdapter.DialogButtonData> buttons = new ArrayList<>();

        // Reroll button if not completed
        if (!progress.isCompleted()) {
            buttons.add(new NativeDialogAdapter.DialogButtonData(
                    "<aqua>🔄 Ganti Misi Ini (Reroll)</aqua>",
                    "Acak ulang misi ini dengan misi baru",
                    () -> {
                        plugin.getQuestManager().rerollQuest(player, quest.getId());
                        open(plugin, player);
                    }
            ));
        }

        NativeDialogAdapter.DialogButtonData backBtn = new NativeDialogAdapter.DialogButtonData(
                "<yellow>⬅ Kembali ke Daftar Misi</yellow>",
                "Kembali",
                () -> open(plugin, player)
        );

        org.bukkit.Material mat = org.bukkit.Material.BOOK;
        if (quest.getType() != null) {
            switch (quest.getType()) {
                case MINE_BLOCK, BREAK_BLOCK -> mat = org.bukkit.Material.DIAMOND_PICKAXE;
                case KILL_ENTITY, KILL_PLAYER -> mat = org.bukkit.Material.DIAMOND_SWORD;
                case HARVEST_CROPS, PLANT_CROPS -> mat = org.bukkit.Material.DIAMOND_HOE;
                case FISH -> mat = org.bukkit.Material.FISHING_ROD;
                case CRAFT_ITEM, SMELT_ITEM -> mat = org.bukkit.Material.CRAFTING_TABLE;
                case BREED_ANIMALS, VILLAGER_TRADE -> mat = org.bukkit.Material.EMERALD;
                case EXP_GAIN -> mat = org.bukkit.Material.EXPERIENCE_BOTTLE;
                case TRAVEL_DISTANCE -> mat = org.bukkit.Material.COMPASS;
                default -> mat = org.bukkit.Material.BOOK;
            }
        }
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(mat);

        NativeDialogAdapter.showMultiActionDialog(plugin, player, item, title, desc.toString(), buttons, backBtn, 1);
    }
}
