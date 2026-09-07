package com.apexsions.core.level.xp.handlers;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.core.level.xp.XpSourceHandler;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;

import java.util.Map;

/**
 * Handles XP awarded from enchanting items in Enchantment Tables.
 * Awards base XP based on table tier and additional XP based on the levels of all enchantments received.
 */
public class EnchantingXpHandler implements XpSourceHandler {

    private final ApexsionsCorePlugin plugin;

    public EnchantingXpHandler(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public XpSource getSource() {
        return XpSource.ENCHANTING;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getXpConfig().getBoolean("sources.enchanting.enabled", true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        if (!isEnabled()) return;

        Player player = event.getEnchanter();
        int button = event.whichButton(); // 0 (Low), 1 (Medium), 2 (High / Level 30)

        // 1. Base Table Tier XP
        long tierXp = switch (button) {
            case 0 -> plugin.getXpConfig().getLong("sources.enchanting.table-tier-1", 15L);
            case 1 -> plugin.getXpConfig().getLong("sources.enchanting.table-tier-2", 35L);
            case 2 -> plugin.getXpConfig().getLong("sources.enchanting.table-tier-3", 75L);
            default -> plugin.getXpConfig().getLong("sources.enchanting.default", 15L);
        };

        // 2. XP from each enchantment added to the item
        long enchantsXp = 0L;
        Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();
        if (enchants != null) {
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                int level = entry.getValue();
                enchantsXp += calculateEnchantLevelXp(level);
            }
        }

        long totalAmount = tierXp + enchantsXp;
        plugin.getLevelManager().addXp(player.getUniqueId(), totalAmount, XpSource.ENCHANTING);
    }

    /**
     * Calculates XP awarded for an enchantment of a specific level (Vanilla I-V & Custom VI-XX+).
     */
    public long calculateEnchantLevelXp(int level) {
        if (level <= 0) return 0L;

        // Vanilla Levels (1 - 5)
        if (level == 1) return plugin.getXpConfig().getLong("sources.enchanting.vanilla-levels.level-1", 15L);
        if (level == 2) return plugin.getXpConfig().getLong("sources.enchanting.vanilla-levels.level-2", 30L);
        if (level == 3) return plugin.getXpConfig().getLong("sources.enchanting.vanilla-levels.level-3", 50L);
        if (level == 4) return plugin.getXpConfig().getLong("sources.enchanting.vanilla-levels.level-4", 80L);
        if (level == 5) return plugin.getXpConfig().getLong("sources.enchanting.vanilla-levels.level-5", 120L);

        // Custom Levels (6 - 20+)
        long maxCap = plugin.getXpConfig().getLong("sources.enchanting.custom-levels.max-enchant-xp", 1500L);

        if (level <= 10) {
            long perLvl = plugin.getXpConfig().getLong("sources.enchanting.custom-levels.level-6-10-per-level", 40L);
            return Math.min(maxCap, 120L + ((level - 5) * perLvl));
        }
        if (level <= 15) {
            long perLvl = plugin.getXpConfig().getLong("sources.enchanting.custom-levels.level-11-15-per-level", 60L);
            return Math.min(maxCap, 320L + ((level - 10) * perLvl));
        }
        if (level <= 20) {
            long perLvl = plugin.getXpConfig().getLong("sources.enchanting.custom-levels.level-16-20-per-level", 100L);
            return Math.min(maxCap, 620L + ((level - 15) * perLvl));
        }

        return maxCap;
    }
}
