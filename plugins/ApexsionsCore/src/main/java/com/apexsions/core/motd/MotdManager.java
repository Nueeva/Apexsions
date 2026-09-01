package com.apexsions.core.motd;

import com.apexsions.core.ApexsionsCorePlugin;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MotdManager implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Random random = new Random();

    public MotdManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onServerListPing(PaperServerListPingEvent event) {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("motd.enabled", true)) {
            return;
        }

        String line1 = config.getString("motd.line-1", "      <gradient:#f1c40f:#e67e22><bold>✦ APEXSIONS KINGDOM ✦</bold></gradient> <gray>•</gray> <aqua><bold>[1.21.4]</bold></aqua>");
        List<String> line2List = config.getStringList("motd.lines-2");
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

        // Hover Player List Sample
        List<String> hoverLines = config.getStringList("motd.hover-sample");
        if (hoverLines != null && !hoverLines.isEmpty()) {
            List<PlayerProfile> sampleProfiles = new ArrayList<>();
            for (String hLine : hoverLines) {
                // In Paper 1.21, PlayerProfile name supports legacy / component text
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(hLine)));
                sampleProfiles.add(profile);
            }
            event.getPlayerSample().clear();
            event.getPlayerSample().addAll(sampleProfiles);
        }
    }
}
