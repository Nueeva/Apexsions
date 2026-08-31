package com.apexsions.core.title;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.cosmetics.condition.*;
import com.apexsions.core.player.PlayerData;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Registry and manager for player titles and honorific badges.
 */
public class TitleManager {

    private final ApexsionsCorePlugin plugin;
    private final Map<String, TitleItem> titles = new LinkedHashMap<>();

    public TitleManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        registerDefaultTitles();
    }

    private void registerDefaultTitles() {
        // Level Progression Titles
        registerTitle(new TitleItem("novice", "<gray>[Pengelana Awal]</gray>", "Gelar kehormatan untuk pemain pemula di server.", new LevelCondition(1)));
        registerTitle(new TitleItem("apprentice", "<green>[Petualang Muda]</green>", "Mulai memahami seluk beluk wilayah kerajaan.", new LevelCondition(10)));
        registerTitle(new TitleItem("warrior", "<yellow>[Ksatria Perang]</yellow>", "Telah menaklukkan berbagai rintangan tempur.", new LevelCondition(25)));
        registerTitle(new TitleItem("champion", "<gradient:#f1c40f:#e67e22><bold>[Pahlawan Realm]</bold></gradient>", "Pahlawan teruji di seluruh penjuru kerajaan.", new LevelCondition(50)));
        registerTitle(new TitleItem("legend", "<gradient:#ff007f:#7928ca><bold>[Legenda Abadi]</bold></gradient>", "Nama yang terukir abadi dalam sejarah peradaban.", new LevelCondition(75)));
        registerTitle(new TitleItem("mythic", "<gradient:#ff0000:#ff7300:#fffb00><bold>[⚡ DEWA PERANG ⚡]</bold></gradient>", "Pencapaian level tertinggi puncak kejayaan.", new LevelCondition(100)));

        // Kingdom Affiliation Titles
        registerTitle(new TitleItem("zenith_blade", "<gradient:#ffe900:#f39c12><bold>[Pedang Zenithar]</bold></gradient>", "Ksatria elit pelindung kota emas Zenithar.", new KingdomCondition("ZENITHAR", 20)));
        registerTitle(new TitleItem("sol_flame", "<gradient:#ff4d4d:#c0392b><bold>[Lentera Solterra]</bold></gradient>", "Penyala api semangat tempur peradaban Solterra.", new KingdomCondition("SOLTERRA", 20)));
        registerTitle(new TitleItem("sylva_warden", "<gradient:#87ceeb:#2ecc71><bold>[Penjaga Sylvamoor]</bold></gradient>", "Penjaga kedamaian alam dan rimba Sylvamoor.", new KingdomCondition("SYLVAMOOR", 20)));

        // Prestige & Special Titles
        registerTitle(new TitleItem("sultan", "<gradient:#2ecc71:#f1c40f><bold>[💰 SULTAN KERAJAAN]</bold></gradient>", "Gelar eksklusif bagi konglomerat dan donatur terkemuka.", new PermissionCondition("apexsions.title.sultan", "Rank Donatur / Sultan")));
        registerTitle(new TitleItem("monarch", "<gradient:#f1c40f:#e74c3c><bold>[👑 SANG MAHARATU/RAJA]</bold></gradient>", "Gelar sakral pemegang tahta tertinggi kerajaan.", new MonarchCondition()));
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
