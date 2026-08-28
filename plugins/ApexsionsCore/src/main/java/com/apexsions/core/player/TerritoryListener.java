package com.apexsions.core.player;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.region.Region;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens for player movement across BlueMap kingdom boundaries and displays territory entry notifications.
 */
public class TerritoryListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, String> lastTerritory = new ConcurrentHashMap<>();

    public TerritoryListener(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        // Only check when block coordinates change
        if (from.getBlockX() == to.getBlockX() &&
            from.getBlockY() == to.getBlockY() &&
            from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        Optional<Region> regionOpt = plugin.getRegionManager().getRegionAt(to);
        String currentKey = regionOpt.map(Region::getKey).orElse("WILDERNESS");

        String previousKey = lastTerritory.put(player.getUniqueId(), currentKey);

        // If territory changed, notify player via ActionBar
        if (previousKey != null && !previousKey.equalsIgnoreCase(currentKey)) {
            if (regionOpt.isPresent()) {
                Region region = regionOpt.get();
                String message;
                Sound sound = Sound.BLOCK_NOTE_BLOCK_CHIME;

                switch (region.getKey()) {
                    case "ZENITHAR":
                        message = "<yellow><bold>⚑ Territory: </bold><gold>Kingdom of Zenithar</gold></yellow> <gray>(Celestial Realm)</gray>";
                        break;
                    case "SOLTERRA":
                        message = "<red><bold>⚑ Territory: </bold><dark_red>Kingdom of Solterra</dark_red></red> <gray>(Crimson Empire)</gray>";
                        break;
                    case "SYLVAMOOR":
                        message = "<aqua><bold>⚑ Territory: </bold><blue>Kingdom of Sylvamoor</blue></aqua> <gray>(Azure Realm)</gray>";
                        break;
                    default:
                        message = "<gold><bold>⚑ Territory: </bold>" + region.getDisplayName() + "</gold>";
                        break;
                }

                player.sendActionBar(miniMessage.deserialize(message));
                player.playSound(player.getLocation(), sound, 0.5f, 1.2f);
            } else if (!previousKey.equalsIgnoreCase("WILDERNESS")) {
                player.sendActionBar(miniMessage.deserialize("<gray><bold>⚑ Territory: </bold>Wilderness (Neutral Zone)</gray>"));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastTerritory.remove(event.getPlayer().getUniqueId());
    }

    public Optional<String> getCurrentTerritory(UUID playerId) {
        return Optional.ofNullable(lastTerritory.get(playerId));
    }
}
