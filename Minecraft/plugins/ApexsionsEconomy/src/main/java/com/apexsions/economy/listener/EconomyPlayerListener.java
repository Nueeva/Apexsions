package com.apexsions.economy.listener;

import com.apexsions.economy.ApexsionsEconomy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EconomyPlayerListener implements Listener {

    private final ApexsionsEconomy plugin;

    public EconomyPlayerListener(ApexsionsEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Preload balances asynchronously (M-1): never block the join/main thread.
        // Cache-only reads self-heal once the preload completes.
        plugin.getCurrencyService().preloadBalances(event.getPlayer().getUniqueId());
    }
}
