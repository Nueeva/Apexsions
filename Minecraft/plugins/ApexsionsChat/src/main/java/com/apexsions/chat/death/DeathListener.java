package com.apexsions.chat.death;

import com.apexsions.chat.ApexsionsChatPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final ApexsionsChatPlugin plugin;
    private final DeathMessageFormatter formatter;

    public DeathListener(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
        this.formatter = new DeathMessageFormatter(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfigManager().getMainConfig().getBoolean("death-messages.enabled", true)) {
            return;
        }

        Player victim = event.getPlayer();

        // 1. Format luxury death message
        Component formattedMessage = formatter.format(event);

        // 2. Set as official Paper death message (replaces vanilla/plain death message)
        event.deathMessage(formattedMessage);

        // 3. Optional subtle death sound for victim / world
        if (plugin.getConfigManager().getMainConfig().getBoolean("sounds.death.enabled", false)) {
            String soundName = plugin.getConfigManager().getMainConfig().getString("sounds.death.sound", "ENTITY_PLAYER_DEATH");
            try {
                Sound sound = Sound.valueOf(soundName);
                victim.playSound(victim.getLocation(), sound, 1.0f, 1.0f);
            } catch (Exception ignored) {}
        }
    }
}
