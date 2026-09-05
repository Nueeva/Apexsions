package com.apexsions.core.level.xp.handlers;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.core.level.xp.XpSourceHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles specific XP awarded from killing every individual mob entity type.
 */
public class MobKillXpHandler implements XpSourceHandler {

    private final ApexsionsCorePlugin plugin;
    private final Map<EntityType, Long> customAmounts = new HashMap<>();

    public MobKillXpHandler(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        customAmounts.clear();
        ConfigurationSection section = plugin.getXpConfig().getConfigurationSection("sources.mob-kill.custom-amounts");
        if (section != null) {
            for (String entityName : section.getKeys(false)) {
                try {
                    EntityType type = EntityType.valueOf(entityName.toUpperCase());
                    customAmounts.put(type, section.getLong(entityName, 8L));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    @Override
    public XpSource getSource() {
        return XpSource.MOB_KILL;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getXpConfig().getBoolean("sources.mob-kill.enabled", true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isEnabled()) return;

        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;
        if (entity instanceof Player) return; // Handled by PlayerKillXpHandler

        EntityType type = entity.getType();
        long amount;

        if (customAmounts.containsKey(type)) {
            amount = customAmounts.get(type);
        } else if (entity instanceof Monster) {
            amount = plugin.getXpConfig().getLong("sources.mob-kill.hostile-default", 8L);
            if (entity instanceof Boss) {
                double multiplier = plugin.getXpConfig().getDouble("sources.mob-kill.boss-multiplier", 1.0);
                amount = (long) (amount * multiplier);
            }
        } else if (entity instanceof Animals || entity instanceof WaterMob) {
            amount = plugin.getXpConfig().getLong("sources.mob-kill.passive-default", 2L);
        } else {
            amount = plugin.getXpConfig().getLong("sources.mob-kill.hostile-default", 5L);
        }

        if (amount > 0) {
            plugin.getLevelManager().addXp(killer.getUniqueId(), amount, XpSource.MOB_KILL);
        }
    }
}
