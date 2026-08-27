package com.apex.battlepass.admin.gui;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.navigation.CloseButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public class AdminStatsMenu extends Gui {

    public AdminStatsMenu(ApexsionsBattlepass plugin, Player player, Gui parent) {
        super(plugin, player, plugin.getGuiConfig().getString("titles.admin-stats", "&8[ &4&lABP MONITORING & STATISTICS &8]"), 45, parent);
    }

    @Override
    public void initialize() {
        fillBackground();

        Collection<PlayerData> allData = plugin.getPlayerManager().getPlayerDataCache().values();
        int totalTracked = allData.size();
        int onlineCount = Bukkit.getOnlinePlayers().size();

        int totalLevels = 0;
        int minLevel = Integer.MAX_VALUE;
        int maxLevel = 0;
        long totalCoins = 0;
        int totalDailyRefreshes = 0;
        int totalLifetimeRefreshes = 0;
        int totalQuestsCompleted = 0;

        int premiumCount = 0;
        int ultimateCount = 0;

        for (PlayerData d : allData) {
            int lvl = d.getLevel();
            totalLevels += lvl;
            if (lvl < minLevel) minLevel = lvl;
            if (lvl > maxLevel) maxLevel = lvl;

            totalCoins += d.getCurrency();
            totalDailyRefreshes += d.getDailyRefreshCount();
            totalLifetimeRefreshes += d.getTotalRefreshCount();
            totalQuestsCompleted += (int) d.getQuestCompleted().values().stream().filter(b -> b).count();

            if (d.hasPass("premium")) premiumCount++;
            if (d.hasPass("ultimate")) ultimateCount++;
        }

        if (minLevel == Integer.MAX_VALUE) minLevel = 1;
        double avgLevel = totalTracked > 0 ? ((double) totalLevels / totalTracked) : 1.0;
        double avgCoins = totalTracked > 0 ? ((double) totalCoins / totalTracked) : 0;

        // 1. Players & Level Balancing Card (Slot 11)
        setButton(11, new GuiButton(new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name("&e&lSTATISTIK LEVEL & PEMAIN")
                .lore(List.of(
                        "&7Pemain Online: &b" + onlineCount,
                        "&7Pemain Terdata di Memori: &f" + totalTracked,
                        " ",
                        "&7Rata-rata Level: &a" + String.format("%.1f", avgLevel),
                        "&7Level Terendah: &f" + minLevel,
                        "&7Level Tertinggi: &e" + maxLevel + " &8/ &f" + plugin.getRewardManager().getMaxLevel()
                ))
                .build()));

        // 2. Economy & Currency Circulation Card (Slot 13)
        setButton(13, new GuiButton(new ItemBuilder(Material.SUNFLOWER)
                .name("&6&lSTATISTIK EKONOMI & COINS")
                .lore(List.of(
                        "&7Total Coins Beredar: &e" + plugin.getCurrencyService().format((int) Math.min(Integer.MAX_VALUE, totalCoins)),
                        "&7Rata-rata Coins/Player: &f" + String.format("%.1f", avgCoins),
                        " ",
                        "&7Total Quests Selesai: &a" + totalQuestsCompleted + " quests",
                        "&7Integrasi ApexsionsEconomy: &aTerhubung"
                ))
                .build()));

        // 3. Shop Refresh Activity Card (Slot 15)
        setButton(15, new GuiButton(new ItemBuilder(Material.CLOCK)
                .name("&d&lAKTIVITAS REFRESH SHOP")
                .lore(List.of(
                        "&7Refresh Hari Ini (Semua Player): &e" + totalDailyRefreshes + " kali",
                        "&7Total Refresh Seumur Hidup: &b" + totalLifetimeRefreshes + " kali",
                        " ",
                        "&7Base Refresh Cost: &f" + plugin.getShopRefreshService().getBaseCost() + " Coins",
                        "&7Min - Max Cost: &f" + plugin.getShopRefreshService().getMinCost() + " - " + plugin.getShopRefreshService().getMaxCost() + " Coins",
                        "&7Cooldown: &f" + plugin.getShopRefreshService().getCooldownSeconds() + " detik"
                ))
                .build()));

        // 4. Pass Adoption Card (Slot 22)
        setButton(22, new GuiButton(new ItemBuilder(Material.GOLDEN_HELMET)
                .name("&b&lDISTRIBUSI PASS TIERS")
                .lore(List.of(
                        "&7Pemain dengan Premium Pass: &e" + premiumCount + " &8(" + (totalTracked > 0 ? (premiumCount * 100 / totalTracked) : 0) + "%)",
                        "&7Pemain dengan Ultimate Pass: &d" + ultimateCount + " &8(" + (totalTracked > 0 ? (ultimateCount * 100 / totalTracked) : 0) + "%)",
                        " ",
                        "&7Total Pass Terdaftar: &f" + plugin.getPassManager().getPasses().size()
                ))
                .build()));

        // Navigation
        setButton(36, new BackButton(this, parent));
        setButton(44, new CloseButton());
    }
}
