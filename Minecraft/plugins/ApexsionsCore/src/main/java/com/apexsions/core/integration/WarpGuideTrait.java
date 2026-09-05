package com.apexsions.core.integration;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.gui.warp.WarpGUI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

/**
 * Citizens trait that turns an NPC into an interactive Warp List NPC.
 */
@TraitName("warp-guide")
public class WarpGuideTrait extends Trait {

    public WarpGuideTrait() {
        super("warp-guide");
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        if (event.getNPC() != this.getNPC()) return;

        Player player = event.getClicker();
        ApexsionsCorePlugin plugin = ApexsionsCorePlugin.getInstance();
        if (plugin == null) return;

        player.openInventory(new WarpGUI(plugin, player).getInventory());
    }
}
