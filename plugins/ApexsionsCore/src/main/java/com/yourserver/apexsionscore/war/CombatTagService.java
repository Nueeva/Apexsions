package com.yourserver.apexsionscore.war;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage PvP Combat Tagging (15s duration).
 * Prevents players from teleporting or logging out safely while engaged in combat.
 */
public class CombatTagService implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, Long> combatTags = new ConcurrentHashMap<>();
    private static final long COMBAT_DURATION_MS = 15_000L; // 15 seconds

    public CombatTagService(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public boolean isCombatTagged(UUID uuid) {
        Long expireTime = combatTags.get(uuid);
        if (expireTime == null) return false;
        if (System.currentTimeMillis() > expireTime) {
            combatTags.remove(uuid);
            return false;
        }
        return true;
    }

    public long getRemainingSeconds(UUID uuid) {
        Long expireTime = combatTags.get(uuid);
        if (expireTime == null) return 0;
        long diff = expireTime - System.currentTimeMillis();
        if (diff <= 0) {
            combatTags.remove(uuid);
            return 0;
        }
        return (diff + 999) / 1000;
    }

    public void tagPlayer(Player player) {
        boolean wasTagged = isCombatTagged(player.getUniqueId());
        combatTags.put(player.getUniqueId(), System.currentTimeMillis() + COMBAT_DURATION_MS);

        if (!wasTagged) {
            player.sendMessage(miniMessage.deserialize("<red>⚔ <bold>MODE TEMPUR!</bold> Kamu dalam combat tag selama <yellow>15 detik</yellow>. Seluruh fitur teleportasi dinonaktifkan!</red>"));
        }
    }

    public void removeTag(UUID uuid) {
        combatTags.remove(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = null;
        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
        }

        if (attacker != null && !attacker.getUniqueId().equals(victim.getUniqueId())) {
            tagPlayer(victim);
            tagPlayer(attacker);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isCombatTagged(player.getUniqueId())) {
            // Combat log punishment if needed: kill player or log warning
            player.setHealth(0.0);
            Bukkit.broadcast(miniMessage.deserialize("<red>☠ <yellow>" + player.getName() + "</yellow> keluar dari server saat dalam pertempuran (Combat Log)!</red>"));
            combatTags.remove(player.getUniqueId());
        }
    }
}
