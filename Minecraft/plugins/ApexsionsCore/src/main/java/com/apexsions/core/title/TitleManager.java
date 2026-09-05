package com.apexsions.core.title;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.cosmetics.condition.*;
import com.apexsions.core.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Registry and manager for player titles and honorific badges.
 * Dynamically loads titles from progression/titles.yml (prestige-titles) with fallback defaults.
 */
public class TitleManager {

    private final ApexsionsCorePlugin plugin;
    private final Map<String, TitleItem> titles = new LinkedHashMap<>();

    public TitleManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        loadTitles();
    }

    public void reload() {
        titles.clear();
        loadTitles();
    }

    public void loadTitles() {
        // 1. Seed defaults first
        registerDefaultTitles();

        // 2. Load and overlay from progression/titles.yml
        FileConfiguration config = plugin.getConfigManager().getTitlesConfig();
        if (config != null) {
            ConfigurationSection section = config.getConfigurationSection("prestige-titles");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    ConfigurationSection titleSec = section.getConfigurationSection(key);
                    if (titleSec == null) continue;

                    String id = key.toLowerCase(Locale.ROOT);
                    String display = titleSec.getString("display", "<white>[" + key + "]</white>");
                    String description = titleSec.getString("description", "Gelar kehormatan.");
                    String permission = titleSec.getString("permission", "apexsions.title." + id);

                    UnlockCondition condition = null;
                    if (titleSec.contains("level")) {
                        condition = new LevelCondition(titleSec.getInt("level", 1));
                    } else if (titleSec.contains("kingdom")) {
                        String kingdom = titleSec.getString("kingdom", "ZENITHAR");
                        int kLevel = titleSec.getInt("kingdom-level", 1);
                        condition = new KingdomCondition(kingdom, kLevel);
                    } else if (titleSec.contains("monarch")) {
                        String monarchTarget = titleSec.getString("monarch", "ANY");
                        if ("ANY".equalsIgnoreCase(monarchTarget) || Boolean.parseBoolean(monarchTarget)) {
                            condition = new MonarchCondition();
                        } else {
                            condition = new MonarchCondition(monarchTarget);
                        }
                    } else if (titleSec.contains("hint")) {
                        condition = new PermissionCondition(permission, titleSec.getString("hint"));
                    } else {
                        condition = new PermissionCondition(permission, "Permission " + permission);
                    }

                    registerTitle(new TitleItem(id, display, description, permission, condition));
                }
            }
        }
    }

    private void registerDefaultTitles() {
        registerTitle(new TitleItem("novice", "<gray>Pengelana Awal</gray>", "Gelar kehormatan untuk pemain pemula di server.", "apexsions.title.novice", new LevelCondition(1)));
        registerTitle(new TitleItem("apprentice", "<green>Petualang Muda</green>", "Mulai memahami seluk beluk wilayah kerajaan.", "apexsions.title.apprentice", new LevelCondition(10)));
        registerTitle(new TitleItem("warrior", "<yellow>Ksatria Perang</yellow>", "Telah menaklukkan berbagai rintangan tempur.", "apexsions.title.warrior", new LevelCondition(25)));
        registerTitle(new TitleItem("champion", "<gradient:#f1c40f:#e67e22><bold>Pahlawan Realm</bold></gradient>", "Pahlawan teruji di seluruh penjuru kerajaan.", "apexsions.title.champion", new LevelCondition(50)));
        registerTitle(new TitleItem("legend", "<gradient:#ff007f:#7928ca><bold>Legenda Abadi</bold></gradient>", "Nama yang terukir abadi dalam sejarah peradaban.", "apexsions.title.legend", new LevelCondition(75)));
        registerTitle(new TitleItem("mythic", "<gradient:#ff0000:#ff7300:#fffb00><bold>⚡ DEWA PERANG ⚡</bold></gradient>", "Pencapaian level tertinggi puncak kejayaan.", "apexsions.title.mythic", new LevelCondition(100)));

        registerTitle(new TitleItem("zenith_blade", "<gradient:#ffe900:#f39c12><bold>Pedang Zenithar</bold></gradient>", "Ksatria elit pelindung kota emas Zenithar.", "apexsions.title.zenith_blade", new KingdomCondition("ZENITHAR", 20)));
        registerTitle(new TitleItem("sol_flame", "<gradient:#ff4d4d:#c0392b><bold>Lentera Solterra</bold></gradient>", "Penyala api semangat tempur peradaban Solterra.", "apexsions.title.sol_flame", new KingdomCondition("SOLTERRA", 20)));
        registerTitle(new TitleItem("sylva_warden", "<gradient:#87ceeb:#2ecc71><bold>Penjaga Sylvamoor</bold></gradient>", "Penjaga kedamaian alam dan rimba Sylvamoor.", "apexsions.title.sylva_warden", new KingdomCondition("SYLVAMOOR", 20)));

        registerTitle(new TitleItem("sultan", "<gradient:#2ecc71:#f1c40f><bold>💰 SULTAN KERAJAAN</bold></gradient>", "Gelar eksklusif bagi konglomerat dan donatur terkemuka.", "apexsions.title.sultan", new PermissionCondition("apexsions.title.sultan", "Rank Donatur / Sultan")));
        registerTitle(new TitleItem("raja_zenithar", "<gradient:#ffd700:#f39c12><bold>👑 Raja Zenithar</bold></gradient>", "Gelar suci bagi Penguasa Tertinggi Kerajaan Emas Zenithar.", "apexsions.title.raja_zenithar", new MonarchCondition("ZENITHAR")));
        registerTitle(new TitleItem("raja_solterra", "<gradient:#ff4757:#c0392b><bold>👑 Raja Solterra</bold></gradient>", "Gelar suci bagi Penguasa Tertinggi Kerajaan Api Solterra.", "apexsions.title.raja_solterra", new MonarchCondition("SOLTERRA")));
        registerTitle(new TitleItem("raja_sylvamoor", "<gradient:#2ed573:#1e90ff><bold>👑 Raja Sylvamoor</bold></gradient>", "Gelar suci bagi Penguasa Tertinggi Kerajaan Rimba Sylvamoor.", "apexsions.title.raja_sylvamoor", new MonarchCondition("SYLVAMOOR")));
        registerTitle(new TitleItem("monarch", "<gradient:#f1c40f:#e74c3c><bold>👑 SANG MAHARATU/RAJA</bold></gradient>", "Gelar sakral pemegang tahta tertinggi kerajaan.", "apexsions.title.monarch", new MonarchCondition()));
    }

    public void registerTitle(TitleItem item) {
        if (item != null) {
            titles.put(item.getId().toLowerCase(Locale.ROOT), item);
        }
    }

    public Optional<TitleItem> getTitle(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(titles.get(id.toLowerCase(Locale.ROOT)));
    }

    public Collection<TitleItem> getAllTitles() {
        return Collections.unmodifiableCollection(titles.values());
    }

    public boolean isTitleUnlocked(Player player, TitleItem title) {
        if (player == null || title == null) return false;
        PlayerData data = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
        return title.isUnlocked(player, data, plugin);
    }

    public void equipTitle(Player player, TitleItem title) {
        PlayerData data = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
        if (data != null) {
            data.setActiveTitle(title != null ? title.getDisplayName() : null);
        }
    }

    public void unequipTitle(Player player) {
        PlayerData data = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
        if (data != null) {
            data.setActiveTitle(null);
        }
    }
}
