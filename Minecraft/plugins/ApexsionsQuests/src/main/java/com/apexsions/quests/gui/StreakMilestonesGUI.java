package com.apexsions.quests.gui;

import com.apexsions.quests.ApexsionsQuests;
import com.apexsions.quests.model.PlayerStreakData;
import com.apexsions.quests.service.StreakService;
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

public class StreakMilestonesGUI implements QuestsGuiHolder {

    private static final int[] MILESTONE_SLOTS = {
            10, 12, 14, 16,
            20, 22, 24,
            30, 32
    };

    private final ApexsionsQuests plugin;
    private final Player player;
    private final Inventory inventory;
    private final Map<Integer, Integer> slotToDaysMap = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    public StreakMilestonesGUI(ApexsionsQuests plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#2980b9:#3498db><bold>✦ HADIAH AKUMULASI STREAK ✦</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotToDaysMap.clear();
        GuiUtils.fillStandardBorder54(inventory);

        PlayerStreakData data = plugin.getStreakService().getStreakData(player.getUniqueId());
        int currentStreak = (data != null) ? data.getCurrentStreak() : 0;

        // 1. Header Slot 4
        List<String> headerLore = List.of(
                "<gray>Streak Berjalan Anda:</gray> <gold><bold>" + currentStreak + " Hari</bold></gold>",
                "<gray>Milestone streak terus terakumulasi</gray>",
                "<gray>dan tidak pernah di-reset saat pergantian bulan!</gray>"
        );
        inventory.setItem(4, GuiUtils.createItem(Material.TOTEM_OF_UNDYING, "<gradient:#2980b9:#3498db><bold>Progres Akumulasi Streak</bold></gradient>", headerLore, true));

        // 2. Milestone Slots
        Map<Integer, StreakService.MilestoneReward> milestones = plugin.getStreakService().getMilestones();
        int idx = 0;

        for (Map.Entry<Integer, StreakService.MilestoneReward> entry : milestones.entrySet()) {
            if (idx >= MILESTONE_SLOTS.length) break;
            int days = entry.getKey();
            StreakService.MilestoneReward rew = entry.getValue();
            int slot = MILESTONE_SLOTS[idx++];
            slotToDaysMap.put(slot, days);

            boolean isClaimed = (data != null && data.isMilestoneClaimed(days));
            boolean canClaim = (currentStreak >= days && !isClaimed);

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Syarat: Pertahankan streak <yellow>" + days + " hari berturut-turut</yellow></gray>");
            lore.add("<gray>Hadiah: </gray><gold>" + rew.getDescription() + "</gold>");
            lore.add("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>");

            if (isClaimed) {
                lore.add("<green><bold>✔ SUDAH DIKLAIM</bold></green>");
                inventory.setItem(slot, GuiUtils.createItem(Material.MINECART, "<green><bold>Streak " + days + " Hari</bold></green>", lore, false));
            } else if (canClaim) {
                lore.add("<gold><bold>⭐ BISA DIKLAIM!</bold></gold>");
                lore.add("<yellow>▶ Klik untuk mengklaim hadiah milestone ini!</yellow>");
                inventory.setItem(slot, GuiUtils.createItem(Material.NETHER_STAR, "<gold><bold>⭐ Streak " + days + " Hari (Klaim!)</bold></gold>", lore, true));
            } else {
                int daysLeft = days - currentStreak;
                lore.add("<gray><bold>🔒 TERKUNCI</bold></gray>");
                lore.add("<red>Butuh " + daysLeft + " hari streak lagi untuk membuka.</red>");
                inventory.setItem(slot, GuiUtils.createItem(Material.IRON_BARS, "<gray><bold>Streak " + days + " Hari</bold></gray>", lore, false));
            }
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

        Integer days = slotToDaysMap.get(slot);
        if (days != null) {
            PlayerStreakData data = plugin.getStreakService().getStreakData(player.getUniqueId());
            int currentStreak = (data != null) ? data.getCurrentStreak() : 0;
            boolean isClaimed = (data != null && data.isMilestoneClaimed(days));

            if (currentStreak >= days && !isClaimed) {
                boolean success = plugin.getStreakService().claimMilestone(player, days);
                if (success) {
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
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
