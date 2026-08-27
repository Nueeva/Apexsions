package com.apex.economy.listener;

import com.apex.economy.ApexsionsEconomy;
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
        // Preload balances and ensure player is saved in database
        plugin.getCurrencyService().getBalance(event.getPlayer().getUniqueId(), "rupiah");
        plugin.getCurrencyService().getBalance(event.getPlayer().getUniqueId(), "diamond");
    }
}
