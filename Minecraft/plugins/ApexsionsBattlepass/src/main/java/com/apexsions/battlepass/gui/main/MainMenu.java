package com.apexsions.battlepass.gui.main;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.expshop.gui.ExpPackageMenu;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.player.PlayerData;
import com.apexsions.battlepass.quest.gui.QuestMainMenu;
import com.apexsions.battlepass.reward.gui.RewardsMenu;
import com.apexsions.battlepass.shop.gui.DailyShopMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainMenu extends Gui {

    public MainMenu(ApexsionsBattlepass plugin, Player player) {
        super(plugin, player, plugin.getGuiConfig().getString("titles.main", "&8[ &6&lAPEXSIONS BATTLEPASS &8]"), 45, null);
    }

    @Override
    public void initialize() {
        fillBackground();

        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data == null) return;

        int reqXp = plugin.getRewardManager().getRequiredXp(data.getLevel());
        String xpBar = buildProgressBar(data.getXp(), reqXp);
        Set<String> effectivePasses = plugin.getPassManager().getEffectivePasses(data.getPasses());
        long completedQuests = data.getQuestCompleted().values().stream().filter(b -> b).count();

        double rupiahBal = 0.0;
        try {
            rupiahBal = com.apexsions.economy.api.ApexsionsEconomyProvider.get().getBalance(player.getUniqueId(), "rupiah");
        } catch (Throwable t) {
            rupiahBal = data.getCurrency();
        }

        // 1. Focused Player Statistics Card (Slot 13)
        List<String> bpInfoLore = new ArrayList<>();
        bpInfoLore.add("&7Nama: &f" + player.getName());
        bpInfoLore.add("&7Saldo: &eRp." + String.format("%,.0f", rupiahBal));
        bpInfoLore.add("&7Jenis Pass: &b" + String.join(", ", effectivePasses).toUpperCase());
        bpInfoLore.add(" ");
        bpInfoLore.add("&7Level: &e" + data.getLevel() + " &8/ &f" + plugin.getRewardManager().getMaxLevel());
        bpInfoLore.add("&7Exp: &a" + data.getXp() + " &8/ &f" + reqXp + " XP");
        bpInfoLore.add("&7Progress: " + xpBar);
        bpInfoLore.add("&7Sisa Waktu Season: &e" + plugin.getSeasonManager().getTimeLeftFormatted());

        setButton(13, new GuiButton(new ItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(player)
                .name("&6&lInfo BP Kamu")
                .lore(bpInfoLore)
                .build()));

        // 2. Rewards Button (Slot 19)
        setButton(19, new GuiButton(new ItemBuilder(Material.CHEST)
                .name("&b&lKLAIM HADIAH / REWARDS")
                .lore(List.of(
                        "&7Buka level dan klaim seluruh hadiah pass Anda!",
                        " ",
                        "&eKlik untuk membuka Menu Rewards >"
                ))
                .build(), event -> {
            new RewardsMenu(plugin, player, this).open();
        }));

        // 3. Quests Button (Slot 21)
        setButton(21, new GuiButton(new ItemBuilder(Material.WRITABLE_BOOK)
                .name("&a&lBATTLEPASS QUESTS")
                .lore(List.of(
                        "&7Kerjakan Daily, Weekly, Special Week, & Monthly Quests.",
                        "&7Reset Harian: &e" + plugin.getQuestManager().getPeriodService().getDailyResetTimeLeft(),
                        " ",
                        "&eKlik untuk membuka Menu Quests >"
                ))
                .build(), event -> {
            new QuestMainMenu(plugin, player, this).open();
        }));

        // 4. Battle Shop Button (Slot 23)
        setButton(23, new GuiButton(new ItemBuilder(Material.EMERALD)
                .name("&c&lBATTLEPASS SHOP")
                .lore(List.of(
                        "&7Beli item eksklusif harian, mingguan, & bulanan.",
                        "&7Tersedia tombol acak / refresh rotasi item!",
                        " ",
                        "&eKlik untuk membuka Shop >"
                ))
                .build(), event -> {
            new DailyShopMenu(plugin, player, this).open();
        }));

        // 5. EXP Shop Button (Slot 25)
        setButton(25, new GuiButton(new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name("&6&lEXP SHOP")
                .lore(List.of(
                        "&7Tingkatkan level lebih cepat dengan paket EXP instan.",
                        " ",
                        "&eKlik untuk membuka EXP Shop >"
                ))
                .build(), event -> {
            new ExpPackageMenu(plugin, player, this).open();
        }));

        // 6. Leaderboard Button (Slot 31)
        setButton(31, new GuiButton(new ItemBuilder(Material.NETHER_STAR)
                .name("&e&lTOP LEADERBOARD BATTLEPASS")
                .lore(List.of(
                        "&7Lihat peringkat ranking level dan XP pemain se-server.",
                        " ",
                        "&eKlik untuk melihat Leaderboard >"
                ))
                .build(), event -> {
            new BattlePassLeaderboardMenu(plugin, player, this).open();
        }));

        // 7. Close Button (Slot 40)
        setButton(40, new CloseButton());
    }

    private static String buildProgressBar(int current, int max) {
        int totalBars = 10;
        int progress = max > 0 ? (int) (((double) current / max) * totalBars) : 0;
        progress = Math.min(totalBars, Math.max(0, progress));
        int percent = max > 0 ? (int) (((double) current / max) * 100) : 0;
        percent = Math.min(100, Math.max(0, percent));

        StringBuilder sb = new StringBuilder("&a");
        for (int i = 0; i < progress; i++) {
            sb.append("\u2588");
        }
        sb.append("&7");
        for (int i = progress; i < totalBars; i++) {
            sb.append("\u2588");
        }
        sb.append(" &f(").append(percent).append("%)");
        return sb.toString();
    }
}

