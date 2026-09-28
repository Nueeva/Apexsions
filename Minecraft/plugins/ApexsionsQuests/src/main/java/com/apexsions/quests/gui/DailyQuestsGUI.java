package com.apexsions.quests.gui;

import com.apexsions.quests.ApexsionsQuests;
import com.apexsions.quests.model.PlayerQuestProgress;
import com.apexsions.quests.model.PlayerStreakData;
import com.apexsions.quests.model.Quest;
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

public class DailyQuestsGUI implements QuestsGuiHolder {

    private static final int[] QUEST_SLOTS = {20, 22, 24, 21, 23};

    private final ApexsionsQuests plugin;
    private final Player player;
    private final Inventory inventory;
    private final Map<Integer, String> slotToQuestIdMap = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    public DailyQuestsGUI(ApexsionsQuests plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e67e22:#f39c12><bold>✦ UNIVERSAL DAILY QUESTS ✦</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();
        slotToQuestIdMap.clear();
        GuiUtils.fillStandardBorder54(inventory);

        List<PlayerQuestProgress> quests = plugin.getQuestManager().getPlayerQuests(player.getUniqueId());
        PlayerStreakData streakData = plugin.getStreakService().getStreakData(player.getUniqueId());

        int rerollsUsed = (streakData != null) ? streakData.getRerollsUsedToday() : 0;
        int maxRerolls = plugin.getConfig().getInt("quests.daily-rerolls-allowed", 1);
        int rerollsLeft = Math.max(0, maxRerolls - rerollsUsed);

        // 1. Header Slot 4
        List<String> headerLore = List.of(
                "<gray>Tanggal:</gray> <yellow>" + LocalDate.now() + "</yellow>",
                "<gray>Jatah Reroll Hari Ini:</gray> <gold><bold>" + rerollsLeft + " / " + maxRerolls + "</bold></gold>",
                "<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>",
                "<gray>Selesaikan misi harian untuk mendapatkan</gray>",
                "<green>Uang Rupiah</green>, <aqua>Core Level XP</aqua>, dan <yellow>Pass XP</yellow>!",
                "<gray>Misi diperbarui setiap hari pukul 00:00 WIB.</gray>"
        );
        inventory.setItem(4, GuiUtils.createItem(Material.WRITTEN_BOOK, "<gradient:#e67e22:#f39c12><bold>Katalog Misi Harian Peradaban</bold></gradient>", headerLore, false));

        // 2. Quest Slots
        if (quests == null || quests.isEmpty()) {
            List<String> emptyLore = List.of(
                    "<gray>Belum ada misi yang terdaftar hari ini.</gray>",
                    "<yellow>▶ Klik untuk menginisialisasi misi harian!</yellow>"
            );
            inventory.setItem(22, GuiUtils.createItem(Material.COMPASS, "<yellow><bold>Muat Misi Harian</bold></yellow>", emptyLore, true));
        } else {
            int slotIdx = 0;
            for (PlayerQuestProgress p : quests) {
                if (slotIdx >= QUEST_SLOTS.length) break;
                Quest q = plugin.getQuestManager().getQuest(p.getQuestId());
                if (q == null) continue;

                int slot = QUEST_SLOTS[slotIdx++];
                slotToQuestIdMap.put(slot, q.getId());

                Material mat = Material.BOOK;
                if (q.getType() != null) {
                    switch (q.getType()) {
                        case MINE_BLOCK, BREAK_BLOCK -> mat = Material.DIAMOND_PICKAXE;
                        case KILL_ENTITY, KILL_PLAYER -> mat = Material.DIAMOND_SWORD;
                        case HARVEST_CROPS, PLANT_CROPS -> mat = Material.DIAMOND_HOE;
                        case FISH -> mat = Material.FISHING_ROD;
                        case CRAFT_ITEM, SMELT_ITEM -> mat = Material.CRAFTING_TABLE;
                        case BREED_ANIMALS, VILLAGER_TRADE -> mat = Material.EMERALD;
                        case EXP_GAIN -> mat = Material.EXPERIENCE_BOTTLE;
                        case TRAVEL_DISTANCE -> mat = Material.COMPASS;
                        default -> mat = Material.BOOK;
                    }
                }

                int current = p.getCurrentProgress();
                int target = p.getTargetProgress();
                String progressBar = createProgressBar(current, target, 10);

                List<String> lore = new ArrayList<>();
                lore.add("<gray>Tujuan: </gray><white>" + q.getDescription() + "</white>");
                lore.add("<gray>Progres: </gray>" + progressBar + " <gold>" + current + " / " + target + "</gold>");
                lore.add("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>");
                lore.add("<yellow>Hadiah Penyelesaian:</yellow>");
                lore.add(" <green>• Saldo Rupiah: Rp " + String.format("%,.0f", q.getRewardMoney()) + "</green>");
                lore.add(" <aqua>• Core Level XP: +" + q.getRewardCoreXp() + " XP</aqua>");
                lore.add(" <yellow>• BattlePass XP: +" + q.getRewardPassXp() + " Pass XP</yellow>");
                lore.add("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>");

                if (p.isClaimed()) {
                    lore.add("<green><bold>✔ HADIAH SUDAH DIKLAIM</bold></green>");
                    inventory.setItem(slot, GuiUtils.createItem(Material.MINECART, "<green><bold>✔ " + q.getTitle() + "</bold></green>", lore, false));
                } else if (p.isCompleted()) {
                    lore.add("<gold><bold>⭐ SELESAI!</bold></gold>");
                    lore.add("<green><bold>▶ KLIK KIRI: KLAIM HADIAH SEKARANG!</bold></green>");
                    inventory.setItem(slot, GuiUtils.createItem(mat, "<gold><bold>⭐ [KLAIM] " + q.getTitle() + "</bold></gold>", lore, true));
                } else {
                    lore.add("<yellow>⏳ Sedang Berlangsung (" + current + "/" + target + ")</yellow>");
                    if (rerollsLeft > 0) {
                        lore.add("<aqua>▶ Klik Kanan: Acak Ulang Misi (Reroll)</aqua>");
                    }
                    inventory.setItem(slot, GuiUtils.createItem(mat, "<yellow><bold>" + q.getTitle() + "</bold></yellow>", lore, false));
                }
            }
        }

        // 3. Reroll Info Slot 40
        List<String> rerollLore = List.of(
                "<gray>Sisa Jatah Reroll: <gold>" + rerollsLeft + " / " + maxRerolls + "</gold></gray>",
                "<gray>Misi yang sedang berlangsung dapat diacak ulang</gray>",
                "<gray>jika Anda merasa misinya terlalu sulit.</gray>"
        );
        inventory.setItem(40, GuiUtils.createItem(Material.CLOCK, "<aqua><bold>Informasi Reroll Misi</bold></aqua>", rerollLore, false));

        // 4. Close Button Slot 49
        inventory.setItem(49, GuiUtils.createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>", List.of("<gray>Tutup menu misi</gray>"), false));
    }

    private String createProgressBar(int current, int max, int totalBars) {
        float percent = (max > 0) ? Math.min(1.0f, (float) current / max) : 1.0f;
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

        if (slot == 22 && slotToQuestIdMap.isEmpty()) {
            plugin.getQuestManager().ensureDailyQuestsAssigned(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            buildGUI();
            return;
        }

        String questId = slotToQuestIdMap.get(slot);
        if (questId != null) {
            PlayerQuestProgress progress = null;
            for (PlayerQuestProgress p : plugin.getQuestManager().getPlayerQuests(player.getUniqueId())) {
                if (p.getQuestId().equalsIgnoreCase(questId)) {
                    progress = p;
                    break;
                }
            }
            if (progress != null) {
                if (progress.isCompleted() && !progress.isClaimed()) {
                    // Left click to claim
                    boolean claimed = plugin.getQuestManager().claimReward(player, questId);
                    if (claimed) {
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
                    } else {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                    buildGUI();
                } else if (!progress.isCompleted()) {
                    if (event.isRightClick() || event.isShiftClick()) {
                        // Reroll
                        PlayerStreakData streakData = plugin.getStreakService().getStreakData(player.getUniqueId());
                        int rerollsUsed = (streakData != null) ? streakData.getRerollsUsedToday() : 0;
                        int maxRerolls = plugin.getConfig().getInt("quests.daily-rerolls-allowed", 1);

                        if (rerollsUsed < maxRerolls) {
                            boolean rerolled = plugin.getQuestManager().rerollQuest(player, questId);
                            if (rerolled) {
                                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.2f);
                            } else {
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                            }
                            buildGUI();
                        } else {
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        }
                    } else {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
                    }
                }
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
