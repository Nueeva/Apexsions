package com.apexsions.core.moderation;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Gatekeeper listener intercepting incoming connections before any player authentication or join handling.
 * Operates at EventPriority.LOWEST to ensure banned players are rejected at the TCP socket layer,
 * preventing AuthMeReloaded or other plugins from processing unauthenticated login sessions.
 */
public class BanGateListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final BanManager banManager;

    public BanGateListener(ApexsionsCorePlugin plugin, BanManager banManager) {
        this.plugin = plugin;
        this.banManager = banManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        InetAddress address = event.getAddress();
        String hostAddress = address != null ? address.getHostAddress() : null;

        // 1. Check UUID ban
        BanRecord ban = banManager.getActiveBan(uuid);

        // 2. Check IP ban if UUID is clean
        if (ban == null && hostAddress != null) {
            ban = banManager.getActiveIpBan(hostAddress);
        }

        // 3. Reject socket if banned
        if (ban != null) {
            Component kickScreen = banManager.formatBanScreen(ban);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, kickScreen);
        }
    }
}
