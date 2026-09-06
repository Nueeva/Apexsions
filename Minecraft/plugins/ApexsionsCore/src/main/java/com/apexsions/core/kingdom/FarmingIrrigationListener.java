package com.apexsions.core.kingdom;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.HashMap;

/**
 * Interactive farming, crop hydration, and manual irrigation system:
 * 1. Water Bottle Irrigation: Right-click Farmland or Crop with a Water Bottle to hydrate it to level 7.
 * 2. Splash Water Potion: Throw a water splash potion to irrigate a 5x5 farmland area.
 * 3. Water Bucket Sneak: Sneak + Right-click Farmland with a Water Bucket to safely water 3x3 without washing crops away.
 * 4. Instant Hydration Near Water: Tilling dirt or planting crops within 4 blocks of water immediately sets moisture to 7.
 */
public class FarmingIrrigationListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public FarmingIrrigationListener(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND && event.getHand() != EquipmentSlot.OFF_HAND) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        // --- 1. Water Bottle Watering ---
        if (isWaterBottle(item)) {
            Block targetFarmland = resolveFarmland(clickedBlock);
            if (targetFarmland != null) {
                event.setCancelled(true);
                hydrateFarmland(targetFarmland);

                // Small 25% chance to stimulate crop growth when watered
                Block above = targetFarmland.getRelative(BlockFace.UP);
                if (above.getBlockData() instanceof org.bukkit.block.data.Ageable ageable) {
                    if (ageable.getAge() < ageable.getMaximumAge() && java.util.concurrent.ThreadLocalRandom.current().nextDouble() < 0.25) {
                        ageable.setAge(ageable.getAge() + 1);
                        above.setBlockData(ageable, true);
                    }
                }

                // Sound & particle feedback
                Location effectLoc = targetFarmland.getLocation().add(0.5, 1.0, 0.5);
                World world = targetFarmland.getWorld();
                world.spawnParticle(Particle.SPLASH, effectLoc, 20, 0.3, 0.2, 0.3, 0.1);
                world.spawnParticle(Particle.FALLING_WATER, effectLoc, 10, 0.2, 0.2, 0.2, 0.05);
                world.spawnParticle(Particle.HAPPY_VILLAGER, effectLoc, 8, 0.3, 0.2, 0.3, 0.0);

                player.playSound(effectLoc, Sound.ITEM_BOTTLE_EMPTY, 1.0f, 1.2f);
                player.playSound(effectLoc, Sound.ENTITY_PLAYER_SPLASH, 0.4f, 1.5f);

                // Consume water bottle and return empty glass bottle
                if (player.getGameMode() != GameMode.CREATIVE) {
                    if (item.getAmount() <= 1) {
                        if (event.getHand() == EquipmentSlot.HAND) {
                            player.getInventory().setItemInMainHand(new ItemStack(Material.GLASS_BOTTLE));
                        } else {
                            player.getInventory().setItemInOffHand(new ItemStack(Material.GLASS_BOTTLE));
                        }
                    } else {
                        item.setAmount(item.getAmount() - 1);
                        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
                        if (!leftover.isEmpty()) {
                            leftover.values().forEach(b -> player.getWorld().dropItemNaturally(player.getLocation(), b));
                        }
                    }
                }

                player.sendActionBar(mm.deserialize("<aqua>💧 Lahan pertanian berhasil disiram hingga subur dan basah!</aqua>"));
                return;
            }
        }

        // --- 2. Water Bucket Sneak Watering (3x3 Area) ---
        if (item.getType() == Material.WATER_BUCKET && player.isSneaking()) {
            Block targetFarmland = resolveFarmland(clickedBlock);
            if (targetFarmland != null) {
                event.setCancelled(true);
                int watered = 0;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Block b = targetFarmland.getRelative(dx, 0, dz);
                        if (b.getType() == Material.FARMLAND) {
                            hydrateFarmland(b);
                            watered++;
                        }
                    }
                }

                Location loc = targetFarmland.getLocation().add(0.5, 1.0, 0.5);
                targetFarmland.getWorld().spawnParticle(Particle.SPLASH, loc, 35, 1.0, 0.2, 1.0, 0.1);
                player.playSound(loc, Sound.ITEM_BUCKET_EMPTY, 0.8f, 1.2f);
                player.sendActionBar(mm.deserialize("<aqua>🌊 " + watered + " blok lahan pertanian berhasil disiram dengan ember air!</aqua>"));
                return;
            }
        }

        // --- 3. Instant Hydration When Hoeing Dirt Near Water ---
        if (item.getType().name().endsWith("_HOE")) {
            Material type = clickedBlock.getType();
            if (type == Material.DIRT || type == Material.GRASS_BLOCK || type == Material.DIRT_PATH
                    || type == Material.COARSE_DIRT || type == Material.ROOTED_DIRT) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (clickedBlock.getType() == Material.FARMLAND && isNearWater(clickedBlock)) {
                        hydrateFarmland(clickedBlock);
                        clickedBlock.getWorld().spawnParticle(Particle.SPLASH, clickedBlock.getLocation().add(0.5, 1.0, 0.5), 8, 0.2, 0.1, 0.2, 0.05);
                        player.sendActionBar(mm.deserialize("<blue>💧 Tanah langsung menyerap kelembapan dari sumber air terdekat!</blue>"));
                    }
                }, 1L);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placed = event.getBlockPlaced();
        if (isCropBlock(placed.getType())) {
            Block below = placed.getRelative(BlockFace.DOWN);
            if (below.getType() == Material.FARMLAND && isNearWater(below)) {
                hydrateFarmland(below);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        ItemStack item = potion.getItem();
        if (!isWaterBottle(item)) return;

        Location center = potion.getLocation();
        Block centerBlock = center.getBlock();
        int radius = 2; // 5x5 footprint
        int irrigated = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    Block b = centerBlock.getRelative(dx, dy, dz);
                    if (b.getType() == Material.FARMLAND) {
                        hydrateFarmland(b);
                        irrigated++;
                    }
                }
            }
        }

        if (irrigated > 0) {
            center.getWorld().spawnParticle(Particle.SPLASH, center, 40, 1.2, 0.4, 1.2, 0.1);
            center.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, center, 15, 1.2, 0.4, 1.2, 0.0);
        }
    }

    private void hydrateFarmland(Block block) {
        if (block.getBlockData() instanceof Farmland farmland) {
            farmland.setMoisture(7);
            block.setBlockData(farmland, true);
        }
    }

    private Block resolveFarmland(Block clicked) {
        if (clicked.getType() == Material.FARMLAND) {
            return clicked;
        }
        if (isCropBlock(clicked.getType()) || clicked.getBlockData() instanceof org.bukkit.block.data.Ageable) {
            Block below = clicked.getRelative(BlockFace.DOWN);
            if (below.getType() == Material.FARMLAND) {
                return below;
            }
        }
        return null;
    }

    private boolean isWaterBottle(ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.POTION && item.getType() != Material.SPLASH_POTION) return false;

        if (item.getItemMeta() instanceof PotionMeta meta) {
            try {
                if (meta.getBasePotionType() != null && meta.getBasePotionType() == PotionType.WATER) {
                    return true;
                }
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    private boolean isCropBlock(Material material) {
        return material == Material.WHEAT
                || material == Material.CARROTS
                || material == Material.POTATOES
                || material == Material.BEETROOTS
                || material == Material.PUMPKIN_STEM
                || material == Material.ATTACHED_PUMPKIN_STEM
                || material == Material.MELON_STEM
                || material == Material.ATTACHED_MELON_STEM
                || material == Material.TORCHFLOWER_CROP
                || material == Material.PITCHER_CROP
                || material.name().contains("SEEDS");
    }

    public static boolean isNearWater(Block block) {
        World world = block.getWorld();
        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();

        for (int x = bx - 4; x <= bx + 4; x++) {
            for (int z = bz - 4; z <= bz + 4; z++) {
                for (int y = by; y <= by + 1; y++) {
                    Block candidate = world.getBlockAt(x, y, z);
                    if (candidate.getType() == Material.WATER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
