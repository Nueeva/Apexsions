package com.apexsions.core.level.xp.handlers;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.core.level.xp.XpSourceHandler;
import com.apexsions.core.level.xp.antiabuse.PvpKillTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Handles XP awarded from defeating players in PvP combat.
 */
public class PlayerKillXpHandler implements XpSourceHandler {

    private final ApexsionsCorePlugin plugin;
    private final PvpKillTracker pvpTracker;

    public PlayerKillXpHandler(ApexsionsCorePlugin plugin, PvpKillTracker pvpTracker) {
        this.plugin = plugin;
        this.pvpTracker = pvpTracker;
    }

    @Override
    public XpSource getSource() {
        return XpSource.PLAYER_KILL;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getXpConfig().getBoolean("sources.player-kill.enabled", true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!isEnabled()) return;

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer.equals(victim)) return;

        // Anti-abuse PvP farming check
        if (!pvpTracker.canGainPvpXp(killer.getUniqueId(), victim.getUniqueId())) {
            return;
        }

        long amount = plugin.getXpConfig().getLong("sources.player-kill.default", 25L);
        plugin.getLevelManager().addXp(killer.getUniqueId(), amount, XpSource.PLAYER_KILL);
    }
}
