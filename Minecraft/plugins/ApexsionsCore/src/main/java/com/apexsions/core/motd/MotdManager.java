package com.apexsions.core.motd;

import com.apexsions.core.ApexsionsCorePlugin;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Random;

public class MotdManager implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Random random = new Random();

    public MotdManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onServerListPing(PaperServerListPingEvent event) {
        if (plugin.getMaintenanceManager() != null && plugin.getMaintenanceManager().isMaintenanceActive()) {
            String mLine1 = "      <gradient:#e74c3c:#c0392b><bold>✦ APEXSIONS MAINTENANCE ✦</bold></gradient> <gray>•</gray> <red><bold>[PEMELIHARAAN]</bold></red>";
            String mLine2 = "   <yellow>" + plugin.getMaintenanceManager().getMessage() + "</yellow>";
            event.motd(miniMessage.deserialize(mLine1).append(Component.newline()).append(miniMessage.deserialize(mLine2)));
            return;
        }

        FileConfiguration config = plugin.getConfigManager().getMotdConfig();
        if (!config.getBoolean("enabled", config.getBoolean("motd.enabled", true))) {
            return;
        }

        String line1 = config.getString("line-1", config.getString("motd.line-1", "      <gradient:#f1c40f:#e67e22><bold>✦ APEXSIONS ✦</bold></gradient> <gray>•</gray> <aqua><bold>[26.2]</bold></aqua>"));
        List<String> line2List = config.contains("lines-2") ? config.getStringList("lines-2") : config.getStringList("motd.lines-2");
        String line2;
        if (line2List == null || line2List.isEmpty()) {
            line2 = "   <gradient:#00c6ff:#0072ff>⚔ PERANG TIGA KERAJAAN & EKONOMI REALM ⚔</gradient>";
        } else {
            line2 = line2List.get(random.nextInt(line2List.size()));
        }

        Component motdComponent = miniMessage.deserialize(line1)
                .append(Component.newline())
                .append(miniMessage.deserialize(line2));

        event.motd(motdComponent);

        // Deduct vanished staff from visible player count
        int visiblePlayers = event.getNumPlayers();
        if (plugin.getVanishManager() != null) {
            visiblePlayers = Math.max(0, visiblePlayers - plugin.getVanishManager().getVanishedCount());
            event.setNumPlayers(visiblePlayers);
            try {
                event.getListedPlayers().removeIf(p -> p != null && plugin.getVanishManager().isVanished(p.id()));
            } catch (Throwable ignored) {}
        }

        // Player Count customization from motd.yml
        if (config.getBoolean("player-count.just-one-more-slot", false)) {
            event.setMaxPlayers(visiblePlayers + 1);
        } else if (config.getBoolean("player-count.custom-max-players", false)) {
            event.setMaxPlayers(config.getInt("player-count.max-players", 100));
        }
    }
}
