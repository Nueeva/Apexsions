package com.apexsions.battlepass.admin.gui;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminMainMenu extends Gui {

    public AdminMainMenu(ApexsionsBattlepass plugin, Player player) {
        super(plugin, player, plugin.getGuiConfig().getString("titles.admin-main", "&8[ &4&lABP ADMIN PANEL &8]"), 54, null);
    }

    @Override
    public void initialize() {
        fillBackground();

        // 1. Admin System Status Banner (Slot 4)
        String seasonName = plugin.getSeasonManager().getCurrentSeason().getName();
        String seasonState = plugin.getSeasonManager().getSeasonState().name();
        String timeLeft = plugin.getSeasonManager().getTimeLeftFormatted();
        int onlinePlayers = Bukkit.getOnlinePlayers().size();

        setButton(4, new GuiButton(new ItemBuilder(Material.COMMAND_BLOCK)
                .name("&4&lAPEXSIONS BATTLEPASS &8- &c&lADMIN PANEL")
                .lore(List.of(
                        "&7Panel kendali penuh administrasi BattlePass.",
                        " ",
                        "&7Season Aktif: &e" + seasonName + " &8(&f" + seasonState + "&8)",
                        "&7Sisa Waktu: &a" + timeLeft,
                        "&7Player Online: &b" + onlinePlayers,
                        "&7Zona Waktu: &f" + plugin.getSeasonManager().getZoneId().getId()
                ))
                .build()));

        // 2. BattlePass Management (Slot 20)
        setButton(20, new GuiButton(new ItemBuilder(Material.BEACON)
                .name("&6&l1. BATTLEPASS MANAGEMENT")
                .lore(List.of(
                        "&7Kelola konfigurasi Season, periode,",
                        "&7status transisi, dan formula level/XP.",
                        " ",
                        "&eKlik untuk membuka Season Settings >"
                ))
                .build(), event -> {
            new AdminSeasonMenu(plugin, player, this).open();
        }));

        // 3. Quest Management (Slot 21)
        setButton(21, new GuiButton(new ItemBuilder(Material.WRITABLE_BOOK)
                .name("&a&l2. QUEST MANAGEMENT")
                .lore(List.of(
                        "&7Lihat & kelola Daily Quests, Weekly Quests,",
                        "&7Special Week (Week 5), dan Monthly Quests.",
                        " ",
                        "&eKlik untuk membuka Quest Management >"
                ))
                .build(), event -> {
            new AdminQuestMenu(plugin, player, this).open();
        }));

        // 4. Reward Management (Slot 22)
        setButton(22, new GuiButton(new ItemBuilder(Material.CHEST)
                .name("&b&l3. REWARD MANAGEMENT")
                .lore(List.of(
                        "&7Inspeksi reward Free, Premium, Premium+,",
                        "&7dan Ultimate untuk seluruh 50-100 level.",
                        " ",
                        "&eKlik untuk membuka Reward Management >"
                ))
                .build(), event -> {
            new AdminRewardMenu(plugin, player, this).open();
        }));

        // 5. Shop Management (Slot 23)
        setButton(23, new GuiButton(new ItemBuilder(Material.EMERALD)
                .name("&c&l4. SHOP & REFRESH MANAGEMENT")
                .lore(List.of(
                        "&7Kelola BattlePass Shop, item pool/katalog,",
                        "&7dan atur parameter Dynamic Refresh Cost.",
                        " ",
                        "&eKlik untuk membuka Shop Management >"
                ))
                .build(), event -> {
            new AdminShopMenu(plugin, player, this).open();
        }));

        // 6. Player Management (Slot 24)
        setButton(24, new GuiButton(new ItemBuilder(Material.PLAYER_HEAD)
                .name("&d&l5. PLAYER MANAGEMENT")
                .lore(List.of(
                        "&7Kelola data player online: ubah XP/level,",
                        "&7tambah/kurang coins, berikan pass, reset.",
                        " ",
                        "&eKlik untuk membuka Player Management >"
                ))
                .build(), event -> {
            new AdminPlayerMenu(plugin, player, this).open();
        }));

        // 7. Monitoring & Statistics (Slot 31)
        setButton(31, new GuiButton(new ItemBuilder(Material.SPYGLASS)
                .name("&e&l6. MONITORING & STATISTICS")
                .lore(List.of(
                        "&7Lihat statistik balancing, peredaran coins,",
                        "&7total refresh shop, dan progres player.",
                        " ",
                        "&eKlik untuk membuka Statistics >"
                ))
                .build(), event -> {
            new AdminStatsMenu(plugin, player, this).open();
        }));

        // 8. Quick Action: Reload Configs (Slot 32)
        setButton(32, new GuiButton(new ItemBuilder(Material.REDSTONE_BLOCK)
                .name("&4&lRELOAD CONFIGURATIONS")
                .lore(List.of(
                        "&7Muat ulang seluruh konfigurasi YAML",
                        "&7(config, messages, gui, seasons, quests, shop).",
                        " ",
                        "&cKlik untuk reload plugin >"
                ))
                .build(), event -> {
            plugin.reloadAllConfigurations();
            player.sendMessage(plugin.getMessage("plugin-reloaded"));
            open(); // Re-render menu
        }));

        // Navigation
        setButton(49, new CloseButton());
    }
}
