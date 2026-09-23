package com.apexsions.core.security;

import com.apexsions.core.ApexsionsCorePlugin;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Intercepts /pl, /plugins, /ver, /about commands.
 * - Regular players receive a spoofed plugin list containing all 38 provinces in Indonesia.
 * - Server admins/OPs can view the genuine server plugin list.
 */
public class PluginSecurityListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // 38 Provinsi Resmi di Indonesia
    private static final List<String> INDONESIA_PROVINCES = List.of(
            "Aceh",
            "Sumatera Utara",
            "Sumatera Barat",
            "Riau",
            "Kepulauan Riau",
            "Jambi",
            "Sumatera Selatan",
            "Kepulauan Bangka Belitung",
            "Bengkulu",
            "Lampung",
            "DKI Jakarta",
            "Jawa Barat",
            "Banten",
            "Jawa Tengah",
            "DI Yogyakarta",
            "Jawa Timur",
            "Bali",
            "Nusa Tenggara Barat",
            "Nusa Tenggara Timur",
            "Kalimantan Barat",
            "Kalimantan Tengah",
            "Kalimantan Selatan",
            "Kalimantan Timur",
            "Kalimantan Utara",
            "Sulawesi Utara",
            "Gorontalo",
            "Sulawesi Tengah",
            "Sulawesi Barat",
            "Sulawesi Selatan",
            "Sulawesi Tenggara",
            "Maluku",
            "Maluku Utara",
            "Papua",
            "Papua Barat",
            "Papua Tengah",
            "Papua Pegunungan",
            "Papua Selatan",
            "Papua Barat Daya"
    );

    private static final Set<String> PLUGIN_COMMANDS = Set.of(
            "pl",
            "plugins",
            "plugin",
            "bukkit:pl",
            "bukkit:plugins",
            "bukkit:plugin"
    );

    private static final Set<String> VERSION_COMMANDS = Set.of(
            "ver",
            "version",
            "about",
            "icanhasbukkit",
            "bukkit:ver",
            "bukkit:version",
            "bukkit:about"
    );

    private final Component spoofedPluginsMessage;

    public PluginSecurityListener(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        this.spoofedPluginsMessage = buildSpoofedPluginsMessage();
    }

    private Component buildSpoofedPluginsMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("<white>Server Plugins (").append(INDONESIA_PROVINCES.size()).append("): </white>");
        for (int i = 0; i < INDONESIA_PROVINCES.size(); i++) {
            String province = INDONESIA_PROVINCES.get(i);
            sb.append("<green><hover:show_text:'<gold>Provinsi ").append(province)
              .append("</gold><newline><gray>Wilayah Kedaulatan Republik Indonesia</gray>'>")
              .append(province)
              .append("</hover></green>");
            if (i < INDONESIA_PROVINCES.size() - 1) {
                sb.append("<white>, </white>");
            }
        }
        return miniMessage.deserialize(sb.toString());
    }

    private boolean isAdmin(Player player) {
        return player.isOp() || player.hasPermission("apexsions.admin");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        // Admin / OP diizinkan melihat list plugin server asli
        if (isAdmin(player)) {
            return;
        }

        String raw = event.getMessage().trim();
        if (raw.startsWith("/")) {
            raw = raw.substring(1);
        }

        String[] parts = raw.split(" ");
        String baseCmd = parts[0].toLowerCase(Locale.ROOT);

        if (PLUGIN_COMMANDS.contains(baseCmd)) {
            event.setCancelled(true);
            player.sendMessage(spoofedPluginsMessage);
        } else if (VERSION_COMMANDS.contains(baseCmd)) {
            event.setCancelled(true);
            player.sendMessage(miniMessage.deserialize(
                    "<white>This server is running <gold>Apexsions Core Engine</gold> (Paper 1.21.4 - The Peak Civilizations)</white>"
            ));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncTabComplete(AsyncTabCompleteEvent event) {
        if (!(event.getSender() instanceof Player player)) {
            return;
        }

        if (isAdmin(player)) {
            return;
        }

        String buffer = event.getBuffer().trim().toLowerCase(Locale.ROOT);
        if (buffer.startsWith("/")) {
            buffer = buffer.substring(1);
        }

        String[] parts = buffer.split(" ");
        if (parts.length > 0 && PLUGIN_COMMANDS.contains(parts[0])) {
            String currentArg = parts.length > 1 ? parts[parts.length - 1].toLowerCase(Locale.ROOT) : "";
            List<String> matches = INDONESIA_PROVINCES.stream()
                    .filter(p -> p.toLowerCase(Locale.ROOT).startsWith(currentArg))
                    .collect(Collectors.toList());
            event.setCompletions(matches.isEmpty() ? INDONESIA_PROVINCES : matches);
            event.setHandled(true);
        }
    }
}
