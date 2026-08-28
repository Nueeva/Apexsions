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
            Bukkit.getPluginManager().registerEvents(this, plugin);
            this.hooked = true;
            plugin.getLogger().info("Successfully hooked into Citizens2 and registered KingdomGuideTrait.");
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed registering Citizens integration: " + t.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onNPCRightClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        if (npc == null) return;

        // Check if NPC has custom trait OR matching name
        boolean isGuide = npc.hasTrait(KingdomGuideTrait.class);
        if (!isGuide && npc.getName() != null) {
            String nameLower = npc.getName().toLowerCase();
            if (nameLower.contains("kingdom") || nameLower.contains("guide") || nameLower.contains("pledge") || nameLower.contains("penjaga")) {
                isGuide = true;
            }
        }

        if (isGuide) {
            Player player = event.getClicker();
            Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
            if (dataOpt.isPresent() && !dataOpt.get().hasRegion()) {
                plugin.getRegionSelectionGUI().open(player);
            } else {
                plugin.getKingdomProfileGUI().open(player);
            }
        }
    }

    public boolean isHooked() {
        return hooked;
    }
}
