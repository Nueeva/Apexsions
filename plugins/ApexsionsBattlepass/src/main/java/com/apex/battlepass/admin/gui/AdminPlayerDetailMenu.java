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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;
import java.util.UUID;

public class AdminPlayerDetailMenu extends Gui {

    private final UUID targetUuid;
    private final String targetName;

    public AdminPlayerDetailMenu(ApexsionsBattlepass plugin, Player player, UUID targetUuid, String targetName, Gui parent) {
        super(plugin, player, plugin.getGuiConfig().getString("titles.admin-player-detail", "&8[ &4&lMANAGE: &e%player% &8]").replace("%player%", targetName != null ? targetName : "Player"), 45, parent);
        this.targetUuid = targetUuid;
        this.targetName = targetName != null ? targetName : "Player";
    }

    public AdminPlayerDetailMenu(ApexsionsBattlepass plugin, Player player, Player target, Gui parent) {
        this(plugin, player, target.getUniqueId(), target.getName(), parent);
    }

    @Override
    public void initialize() {
        fillBackground();

        int seasonId = plugin.getSeasonManager().getCurrentSeason().getId();
        PlayerData data = plugin.getPlayerManager().getPlayerDataCache().get(targetUuid);
        if (data == null) {
            // Synchronously or block load if not in memory
            try {
                data = plugin.getRepository().loadPlayerData(targetUuid, seasonId).get();
            } catch (Exception e) {
                player.sendMessage("§cError loading player data: " + e.getMessage());
                if (parent != null) parent.open();
                return;
            }
        }

        final PlayerData finalData = data;
        OfflinePlayer op = Bukkit.getOfflinePlayer(targetUuid);
        boolean isOnline = op.isOnline();
        int reqXp = plugin.getRewardManager().getRequiredXp(finalData.getLevel());

        // 1. Target Profile Card (Slot 13)
        setButton(13, new GuiButton(new ItemBuilder(Material.PLAYER_HEAD)
                .name("&e&lPROFIL: " + targetName + " " + (isOnline ? "&a● Online" : "&7○ Offline"))
                .lore(List.of(
                        "&7UUID: &8" + targetUuid,
                        " ",
                        "&7Level: &e" + finalData.getLevel() + " &8/ &f" + plugin.getRewardManager().getMaxLevel(),
                        "&7XP: &a" + finalData.getXp() + " &8/ &f" + reqXp,
                        "&7Battle Coins: &e" + plugin.getCurrencyService().format(finalData.getCurrency()),
                        "&7Pass Dimiliki: &b" + String.join(", ", finalData.getPasses()).toUpperCase(),
                        " ",
                        "&7Quests Selesai: &f" + finalData.getQuestCompleted().values().stream().filter(b -> b).count() + " quests",
                        "&7Refresh Hari Ini: &f" + finalData.getDailyRefreshCount() + " kali",
                        "&7Total Refresh: &f" + finalData.getTotalRefreshCount() + " kali"
                ))
                .build()));

        // 2. Add XP (Slot 19)
        setButton(19, new GuiButton(new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name("&a&lTAMBAH XP")
                .lore(List.of(
                        "&7Tambahkan BattlePass XP ke pemain ini.",
                        " ",
                        "&a[Klik Kiri] &7+100 XP",
                        "&e[Klik Kanan] &7+500 XP"
                ))
                .build(), event -> {
            int amount = (event.getClick() == ClickType.RIGHT) ? 500 : 100;
            if (isOnline && op.getPlayer() != null) {
                plugin.getXpService().addXp(op.getPlayer(), amount);
            } else {
                finalData.setXp(finalData.getXp() + amount);
                plugin.getRepository().savePlayerData(finalData);
            }
            player.sendMessage("§aBerhasil menambahkan §e" + amount + " XP §ake §e" + targetName);
            open();
        }));

        // 3. Set Level (Slot 20)
        setButton(20, new GuiButton(new ItemBuilder(Material.NETHER_STAR)
                .name("&6&lATUR LEVEL (+1 / -1)")
                .lore(List.of(
                        "&7Ubah level BattlePass pemain ini.",
                        " ",
                        "&a[Klik Kiri] &7+1 Level",
                        "&c[Klik Kanan] &7-1 Level"
                ))
                .build(), event -> {
            if (event.getClick() == ClickType.RIGHT) {
                finalData.setLevel(Math.max(1, finalData.getLevel() - 1));
            } else {
                finalData.setLevel(finalData.getLevel() + 1);
            }
            plugin.getRepository().savePlayerData(finalData);
            player.sendMessage("§aLevel §e" + targetName + " §asekarang: §e" + finalData.getLevel());
            open();
        }));

        // 4. Add Coins (Slot 21)
        setButton(21, new GuiButton(new ItemBuilder(Material.EMERALD)
                .name("&a&lTAMBAH BATTLE COINS")
                .lore(List.of(
                        "&7Tambahkan saldo Battle Coins pemain.",
                        " ",
                        "&a[Klik Kiri] &7+50 Coins",
                        "&e[Klik Kanan] &7+250 Coins"
                ))
                .build(), event -> {
            int amount = (event.getClick() == ClickType.RIGHT) ? 250 : 50;
            plugin.getCurrencyService().addCurrency(targetUuid, amount);
            player.sendMessage("§aBerhasil menambahkan §e" + amount + " Coins §ake §e" + targetName);
            open();
        }));

        // 5. Remove Coins (Slot 22)
        setButton(22, new GuiButton(new ItemBuilder(Material.REDSTONE)
                .name("&c&lKURANGI BATTLE COINS")
                .lore(List.of(
                        "&7Kurangkan saldo Battle Coins pemain.",
                        " ",
                        "&c[Klik Kiri] &7-50 Coins",
                        "&4[Klik Kanan] &7-250 Coins"
                ))
                .build(), event -> {
            int amount = (event.getClick() == ClickType.RIGHT) ? 250 : 50;
            plugin.getCurrencyService().removeCurrency(targetUuid, amount);
            player.sendMessage("§cBerhasil mengurangi §e" + amount + " Coins §cdari §e" + targetName);
            open();
        }));

        // 6. Give Pass Tier (Slot 23)
        setButton(23, new GuiButton(new ItemBuilder(Material.GOLDEN_HELMET)
                .name("&b&lBERIKAN PASS TIER")
                .lore(List.of(
                        "&7Buka akses pass untuk pemain ini.",
                        " ",
                        "&a[Klik Kiri] &7Berikan &6Premium Pass",
                        "&e[Klik Kanan] &7Berikan &dUltimate Pass"
                ))
                .build(), event -> {
            String passTier = (event.getClick() == ClickType.RIGHT) ? "ultimate" : "premium";
            finalData.addPass(passTier);
            plugin.getRepository().savePlayerData(finalData);
            player.sendMessage("§aBerhasil memberikan pass §e" + passTier.toUpperCase() + " §akepada §e" + targetName);
            if (isOnline && op.getPlayer() != null) {
                op.getPlayer().sendMessage("§aSelamat! Anda telah menerima §e" + passTier.toUpperCase() + " Pass§a!");
            }
            open();
        }));

        // 7. Reset Daily Refresh Count for this player (Slot 24)
        setButton(24, new GuiButton(new ItemBuilder(Material.CLOCK)
                .name("&e&lRESET REFRESH COUNTER PLAYER")
                .lore(List.of(
                        "&7Reset counter refresh hari ini untuk",
                        "&7pemain ini kembali ke 0.",
                        " ",
                        "&eKlik untuk reset counter refresh >"
                ))
                .build(), event -> {
            plugin.getShopRefreshService().resetPlayerDailyRefreshCount(finalData);
            player.sendMessage(plugin.getMessage("shop-refresh-reset-player").replace("%player%", targetName));
            open();
        }));

        // 8. Reset Progress Button (Slot 25)
        setButton(25, new GuiButton(new ItemBuilder(Material.BARRIER)
                .name("&4&lRESET PROGRESS BATTLEPASS")
                .lore(List.of(
                        "&7Reset Level, XP, Quests, dan Reward",
                        "&7pemain ini kembali ke awal Season.",
                        " ",
                        "&cKlik untuk membuka konfirmasi reset >"
                ))
                .build(), event -> {
            new AdminConfirmMenu(plugin, player, "&8[ &4&lRESET PLAYER DATA &8]",
                    List.of(
                            "&7Apakah Anda yakin ingin mereset seluruh",
                            "&7progres BattlePass pemain: &e" + targetName + "&7?"
                    ),
                    this,
                    () -> {
                        finalData.resetProgressForNewSeason(plugin.getSeasonManager().getCurrentSeason().getId());
                        plugin.getRepository().savePlayerData(finalData);
                        player.sendMessage("§aBerhasil mereset seluruh progress BattlePass §e" + targetName);
                        open();
                    },
                    this::open
            ).open();
        }));

        // Navigation
        setButton(36, new BackButton(this, parent));
        setButton(44, new CloseButton());
    }
}

