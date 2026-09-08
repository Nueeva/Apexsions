package com.apexsions.core.maintenance;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.logging.Level;

public class MaintenanceManager implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private boolean active = false;
    private String message = "Server sedang dalam pemeliharaan teknis terencana. Silakan kembali beberapa saat lagi.";
    private String reason = "Pemeliharaan berkala";
    private boolean allowStaff = true;

    public MaintenanceManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isMaintenanceActive() {
        return active;
    }

    public String getMessage() {
        return message;
    }

    public String getReason() {
        return reason;
    }

    public boolean isAllowStaff() {
        return allowStaff;
    }

    public void enableMaintenance(String reason, String customMessage, boolean allowStaff) {
        this.active = true;
        if (reason != null && !reason.isBlank()) {
            this.reason = reason;
        }
        if (customMessage != null && !customMessage.isBlank()) {
            this.message = customMessage;
        }
        this.allowStaff = allowStaff;

        plugin.getLogger().log(Level.INFO, "[Maintenance] Maintenance Mode ACTIVATED: " + this.reason);
        
        // Broadcast in-game notification to online players
        Component alert = miniMessage.deserialize("<gradient:#e74c3c:#c0392b><bold>✦ APEXSIONS MAINTENANCE MODE ✦</bold></gradient>\n<yellow>Server telah beralih ke mode pemeliharaan: <white>" + this.reason + "</white></yellow>");
        Bukkit.broadcast(alert);
    }

    public void disableMaintenance() {
        this.active = false;
        plugin.getLogger().log(Level.INFO, "[Maintenance] Maintenance Mode DEACTIVATED. Server accessible to all players.");

        Component alert = miniMessage.deserialize("<gradient:#2ecc71:#27ae60><bold>✦ APEXSIONS REALM TERBUKA ✦</bold></gradient>\n<green>Mode pemeliharaan telah dinonaktifkan. Selamat datang kembali!</green>");
        Bukkit.broadcast(alert);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!active) {
            return;
        }

        Player player = event.getPlayer();

        // Check if staff can bypass
        if (allowStaff) {
            if (player.hasPermission("apexsions.maintenance.bypass") || 
                player.hasPermission("apexsions.admin") || 
                player.isOp()) {
                plugin.getLogger().info("[Maintenance] Staff " + player.getName() + " bypassed maintenance login gate.");
                return;
            }
        }

        Component kickNotice = miniMessage.deserialize(
                "<gradient:#e74c3c:#c0392b><bold>✦ APEXSIONS MAINTENANCE ✦</bold></gradient>\n\n" +
                "<yellow>" + message + "</yellow>\n\n" +
                "<gray>Alasan: <white>" + reason + "</white></gray>\n" +
                "<dark_gray>Server sedang dalam pemeliharaan teknis. Pantau info resmi di web.apexsions.my.id</dark_gray>"
        );

        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickNotice);
        plugin.getLogger().info("[Maintenance] Denied connection for player " + player.getName() + " (Maintenance Active).");
    }
}
