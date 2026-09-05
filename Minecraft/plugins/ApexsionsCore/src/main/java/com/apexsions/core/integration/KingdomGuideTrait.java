package com.apexsions.core.integration;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Optional;

/**
 * Citizens trait that turns any NPC into an interactive Kingdom Guide NPC.
 */
@TraitName("kingdom-guide")
public class KingdomGuideTrait extends Trait {

    public KingdomGuideTrait() {
        super("kingdom-guide");
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        if (event.getNPC() != this.getNPC()) return;

        Player player = event.getClicker();
        ApexsionsCorePlugin plugin = ApexsionsCorePlugin.getInstance();
        if (plugin == null) return;

        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
        if (dataOpt.isPresent() && !dataOpt.get().hasRegion()) {
            plugin.getRegionSelectionGUI().open(player);
        } else {
            plugin.getKingdomProfileGUI().open(player);
        }
    }
}
