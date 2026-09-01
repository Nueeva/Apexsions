package com.apexsions.core.integration;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Optional;

/**
 * Soft-dependency hook for Citizens2 NPC plugin.
 */
public class CitizensHook implements Listener {

    private final ApexsionsCorePlugin plugin;
    private boolean hooked = false;

    public CitizensHook(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
            return;
        }

        try {
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(KingdomGuideTrait.class).withName("kingdom-guide"));
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(RankGuideTrait.class).withName("rank-guide"));
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(WarpGuideTrait.class).withName("warp-guide"));
            Bukkit.getPluginManager().registerEvents(this, plugin);
            this.hooked = true;
            plugin.getLogger().info("Successfully hooked into Citizens2 and registered custom Apexsions NPC traits (kingdom-guide, rank-guide, warp-guide).");
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed registering Citizens integration: " + t.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onNPCRightClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        if (npc == null) return;
        Player player = event.getClicker();
        String nameLower = npc.getName() != null ? npc.getName().toLowerCase() : "";

        // 1. Kingdom Guide (Mulai Bermain / Pilih Kerajaan)
        boolean isKingdomGuide = npc.hasTrait(KingdomGuideTrait.class)
                || nameLower.contains("mulai")
                || nameLower.contains("kerajaan")
                || nameLower.contains("kingdom")
                || nameLower.contains("pledge")
                || nameLower.contains("penjaga");

        if (isKingdomGuide) {
            Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
            if (dataOpt.isPresent() && !dataOpt.get().hasRegion()) {
                plugin.getRegionSelectionGUI().open(player);
            } else {
                plugin.getKingdomProfileGUI().open(player);
            }
            return;
        }

        // 2. Rank List (Daftar Pangkat & Donatur)
        boolean isRankGuide = npc.hasTrait(RankGuideTrait.class)
                || nameLower.contains("rank")
                || nameLower.contains("pangkat")
                || nameLower.contains("donatur")
                || nameLower.contains("donasi")
                || nameLower.contains("store");

        if (isRankGuide) {
            new com.apexsions.core.gui.rank.RankListGUI(plugin, player).open();
            return;
        }

        // 3. Warp List (Navigasi Teleportasi Realm)
        boolean isWarpGuide = npc.hasTrait(WarpGuideTrait.class)
                || nameLower.contains("warp")
                || nameLower.contains("teleport")
                || nameLower.contains("destinasi")
                || nameLower.contains("lokasi");

        if (isWarpGuide) {
            player.openInventory(new com.apexsions.core.gui.warp.WarpGUI(plugin, player).getInventory());
        }
    }

    public boolean isHooked() {
        return hooked;
    }
}
