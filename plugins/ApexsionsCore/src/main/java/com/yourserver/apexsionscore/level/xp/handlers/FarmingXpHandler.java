package com.yourserver.apexsionscore.level.xp.handlers;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import com.yourserver.apexsionscore.level.xp.XpSource;
import com.yourserver.apexsionscore.level.xp.XpSourceHandler;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles XP awarded from harvesting mature crops and breeding specific animals.
 */
public class FarmingXpHandler implements XpSourceHandler {

    private final ApexsionsCorePlugin plugin;
    private final Map<Material, Long> cropAmounts = new HashMap<>();
    private final Map<EntityType, Long> breedingAmounts = new HashMap<>();

    public FarmingXpHandler(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        cropAmounts.clear();
        ConfigurationSection cropSec = plugin.getXpConfig().getConfigurationSection("sources.farming.custom-amounts");
        if (cropSec != null) {
            for (String matName : cropSec.getKeys(false)) {
                Material mat = Material.matchMaterial(matName);
                if (mat != null) {
                    cropAmounts.put(mat, cropSec.getLong(matName, 3L));
                }
            }
        }

        breedingAmounts.clear();
        ConfigurationSection breedSec = plugin.getXpConfig().getConfigurationSection("sources.farming.breeding-amounts");
        if (breedSec != null) {
            for (String entityName : breedSec.getKeys(false)) {
                try {
                    EntityType type = EntityType.valueOf(entityName.toUpperCase());
                    breedingAmounts.put(type, breedSec.getLong(entityName, 5L));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    @Override
    public XpSource getSource() {
        return XpSource.FARMING;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getXpConfig().getBoolean("sources.farming.enabled", true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCropHarvest(BlockBreakEvent event) {
        if (!isEnabled()) return;

        Block block = event.getBlock();
        BlockData data = block.getBlockData();

        if (data instanceof Ageable ageable) {
            // Only reward fully mature crops
            if (ageable.getAge() == ageable.getMaximumAge()) {
                Material mat = block.getType();
                long amount = cropAmounts.getOrDefault(mat, plugin.getXpConfig().getLong("sources.farming.default", 3L));
                if (amount > 0) {
                    Player player = event.getPlayer();
                    plugin.getLevelManager().addXp(player.getUniqueId(), amount, XpSource.FARMING);
                }
            }
        } else {
            // Non-ageable crops like Sugar Cane, Cactus, Bamboo, Melon, Pumpkin
            Material mat = block.getType();
            if (cropAmounts.containsKey(mat)) {
                long amount = cropAmounts.get(mat);
                if (amount > 0) {
                    Player player = event.getPlayer();
                    plugin.getLevelManager().addXp(player.getUniqueId(), amount, XpSource.FARMING);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAnimalBreed(EntityBreedEvent event) {
        if (!isEnabled()) return;

        if (event.getBreeder() instanceof Player player) {
            EntityType entityType = event.getEntityType();
            long amount = breedingAmounts.getOrDefault(entityType, plugin.getXpConfig().getLong("sources.farming.breed-default", 5L));
            if (amount > 0) {
                plugin.getLevelManager().addXp(player.getUniqueId(), amount, XpSource.FARMING);
            }
        }
    }
}
