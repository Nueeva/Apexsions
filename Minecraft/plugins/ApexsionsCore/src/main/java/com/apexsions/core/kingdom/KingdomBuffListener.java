package com.apexsions.core.kingdom;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.region.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Event-driven listener enforcing kingdom buffs and debuffs:
 * - Sylvamoor: Poison resistance (5%), fire debuff (+15%), PvP damage dealt -10%, PvE -5%,
 *              mob drop rate +7%, farmland moisture stability, defense +8% & damage reduction 5%.
 * - Solterra: Damage +15%, critical +10%, defense +2%, incoming damage vulnerability +8%,
 *             hunger exhaustion +7%, farmland accelerated drying.
 * - Zenithar: Damage +6%, defense 6%, critical reduction 5%, poison vulnerability +7%,
 *             food restores less hunger.
 */
public class KingdomBuffListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final KingdomBuffManager buffManager;

    public KingdomBuffListener(ApexsionsCorePlugin plugin, KingdomBuffManager buffManager) {
        this.plugin = plugin;
        this.buffManager = buffManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> buffManager.applyBuffs(event.getPlayer()), 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> buffManager.applyBuffs(event.getPlayer()), 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        buffManager.removeBuffs(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKingdomChoose(com.apexsions.core.event.KingdomRegionChooseEvent event) {
        buffManager.applyBuffs(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKingdomChange(com.apexsions.core.event.KingdomRegionChangeEvent event) {
        buffManager.applyBuffs(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED) return;

        PotionEffect effect = event.getNewEffect();
        if (effect == null || !effect.getType().equals(PotionEffectType.POISON)) return;

        String kingdom = buffManager.getPlayerKingdomKey(player.getUniqueId());
        if (kingdom.equalsIgnoreCase("SYLVAMOOR")) {
            // Sylvamoor: Durasi poison berkurang 5%
            int modifiedDuration = Math.max(20, (int) Math.round(effect.getDuration() * 0.95));
            if (modifiedDuration != effect.getDuration()) {
                event.setCancelled(true);
                PotionEffect newEffect = new PotionEffect(
                        effect.getType(),
                        modifiedDuration,
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.hasParticles(),
                        effect.hasIcon()
                );
                Bukkit.getScheduler().runTask(plugin, () -> player.addPotionEffect(newEffect));
            }
        } else if (kingdom.equalsIgnoreCase("ZENITHAR")) {
            // Zenithar Debuff: Durasi racun & wither bertambah 15% (Fisik steril bangsawan istana rentan racun liar & pembusukan)
            if (effect.getType() == PotionEffectType.POISON || effect.getType() == PotionEffectType.WITHER) {
                int modifiedDuration = (int) Math.ceil(effect.getDuration() * 1.15);
                if (modifiedDuration != effect.getDuration()) {
                    event.setCancelled(true);
                    PotionEffect newEffect = new PotionEffect(
                            effect.getType(),
                            modifiedDuration,
                            effect.getAmplifier(),
                            effect.isAmbient(),
                            effect.hasParticles(),
                            effect.hasIcon()
                    );
                    Bukkit.getScheduler().runTask(plugin, () -> player.addPotionEffect(newEffect));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        String kingdom = buffManager.getPlayerKingdomKey(player.getUniqueId());
        if (kingdom.equalsIgnoreCase("NONE")) return;

        EntityDamageEvent.DamageCause cause = event.getCause();

        // 1. Poison Damage checks
        if (cause == EntityDamageEvent.DamageCause.POISON) {
            if (kingdom.equalsIgnoreCase("SYLVAMOOR")) {
                // Sylvamoor Blessing: Racun rimba hanya mengurangi darah hingga tersisa 3 hati (6.0 HP), tidak sampai sekarat setengah hati
                double currentHealth = player.getHealth();
                if (currentHealth <= 6.0) {
                    event.setCancelled(true);
                    return;
                } else if (currentHealth - event.getDamage() < 6.0) {
                    event.setDamage(Math.max(0.0, currentHealth - 6.0));
                }
            } else if (kingdom.equalsIgnoreCase("ZENITHAR")) {
                // Zenithar Debuff: Kerentanan racun +15%
                event.setDamage(event.getDamage() * 1.15);
            }
            return;
        }

        // 2. Wither Damage checks
        if (cause == EntityDamageEvent.DamageCause.WITHER) {
            if (kingdom.equalsIgnoreCase("ZENITHAR")) {
                // Zenithar Debuff: Kerentanan pembusukan kutukan Wither +15%
                event.setDamage(event.getDamage() * 1.15);
                return;
            }
        }

        // 3. Fire Damage checks
        if (cause == EntityDamageEvent.DamageCause.FIRE
                || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA
                || cause == EntityDamageEvent.DamageCause.HOT_FLOOR
                || cause == EntityDamageEvent.DamageCause.CAMPFIRE) {
            if (kingdom.equalsIgnoreCase("SYLVAMOOR")) {
                // Sylvamoor: Damage terbakar +15% (debuff murni)
                event.setDamage(event.getDamage() * 1.15);
                return;
            }
        }

        // 4. Defense & Incoming Damage adjustments
        switch (kingdom) {
            case "SYLVAMOOR" -> {
                // Sylvamoor: Pertahanan rimba 15% (multiplier 0.85)
                event.setDamage(event.getDamage() * 0.85);
            }
            case "SOLTERRA" -> {
                // Solterra: Defense +2%, namun Damage Diterima +8% (Debuff net vulnerability ~5.8%)
                event.setDamage(event.getDamage() * 0.98 * 1.08);
            }
            case "ZENITHAR" -> {
                // Zenithar: Reduksi damage masuk 20% (multiplier 0.80 - Menggantikan Anti-Crit)
                event.setDamage(event.getDamage() * 0.80);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // A. Attacker adjustments (Supports both direct melee and ranged projectile attacks)
        Player attacker = null;
        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof org.bukkit.entity.Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
        }

        if (attacker != null) {
            String attackerKingdom = buffManager.getPlayerKingdomKey(attacker.getUniqueId());
            switch (attackerKingdom) {
                case "SOLTERRA" -> {
                    // Damage +15%
                    double dmg = event.getDamage() * 1.15;
                    // Critical Damage +10%
                    if (event.isCritical()) {
                        dmg *= 1.10;
                    }
                    event.setDamage(dmg);
                }
                case "SYLVAMOOR" -> {
                    if (event.getEntity() instanceof Player) {
                        // Damage ke player -10%
                        event.setDamage(event.getDamage() * 0.90);
                    } else {
                        // Damage ke mob -5%
                        event.setDamage(event.getDamage() * 0.95);
                    }
                }
                case "ZENITHAR" -> {
                    // Damage +6%
                    event.setDamage(event.getDamage() * 1.06);
                }
            }
        }

        // B. Defender adjustments: Zenithar Anti-Crit telah digantikan dengan reduksi damage flat 20% pada onEntityDamage.
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        String kingdom = buffManager.getPlayerKingdomKey(killer.getUniqueId());
        if (kingdom.equalsIgnoreCase("SYLVAMOOR")) {
            // Drop rate mob +7%
            if (ThreadLocalRandom.current().nextDouble() < 0.07 && !event.getDrops().isEmpty()) {
                int randomIndex = ThreadLocalRandom.current().nextInt(event.getDrops().size());
                ItemStack randomDrop = event.getDrops().get(randomIndex);
                if (randomDrop != null && randomDrop.getType() != Material.AIR) {
                    ItemStack bonus = randomDrop.clone();
                    bonus.setAmount(1);
                    event.getDrops().add(bonus);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMoistureChange(MoistureChangeEvent event) {
        Location loc = event.getBlock().getLocation();
        Optional<Region> regionOpt = plugin.getRegionManager().getRegionAt(loc);
        if (regionOpt.isEmpty()) return;

        String key = regionOpt.get().getKey().toUpperCase();
        if (!(event.getBlock().getBlockData() instanceof Farmland currentFarmland)) return;

        int currentMoisture = currentFarmland.getMoisture();

        if (event.getNewState().getBlockData() instanceof Farmland newFarmland) {
            int newMoisture = newFarmland.getMoisture();

            // 1. Hidrasi alami (kelembapan meningkat karena air di dekatnya)
            // JANGAN DIBATALKAN agar tanah menyerap air secara alami tanpa mematikan tanaman!
            if (newMoisture > currentMoisture) {
                return;
            }

            // 2. Pengeringan tanah (kelembapan menurun)
            if (key.equals("SYLVAMOOR")) {
                // Sylvamoor Buff: Kelembapan tanaman stabil (tidak mengering)
                // Cukup batalkan event penurunan kelembapan tanpa memanggil setBlockData() re-entrant!
                event.setCancelled(true);
            } else if (key.equals("SOLTERRA")) {
                // Solterra Debuff: Tumbuhan agak lebih cepat kering
                if (newMoisture > 0 && ThreadLocalRandom.current().nextDouble() < 0.30) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (event.getBlock().getBlockData() instanceof Farmland f && f.getMoisture() > 0) {
                            f.setMoisture(Math.max(0, f.getMoisture() - 1));
                            event.getBlock().setBlockData(f, true);
                        }
                    });
                }
            }
        } else if (event.getNewState().getType() == Material.DIRT) {
            // Farmland berusaha kembali menjadi DIRT karena kekeringan
            if (key.equals("SYLVAMOOR")) {
                // Di Sylvamoor, proteksi farmland agar tidak berubah menjadi tanah biasa jika ada tanaman di atasnya
                Block above = event.getBlock().getRelative(0, 1, 0);
                if (!above.getType().isAir()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        String kingdom = buffManager.getPlayerKingdomKey(player.getUniqueId());
        if (kingdom.equalsIgnoreCase("SOLTERRA")) {
            // Hunger bar cepat berkurang +7%
            if (event.getFoodLevel() < player.getFoodLevel()) {
                if (ThreadLocalRandom.current().nextDouble() < 0.07) {
                    event.setFoodLevel(Math.max(0, event.getFoodLevel() - 1));
                }
            }
        } else if (kingdom.equalsIgnoreCase("ZENITHAR")) {
            // Zenithar Aristocratic Metabolism: Cepat lelah di luar santapan istana (+12% hunger exhaustion)
            if (event.getFoodLevel() < player.getFoodLevel()) {
                if (ThreadLocalRandom.current().nextDouble() < 0.12) {
                    event.setFoodLevel(Math.max(0, event.getFoodLevel() - 1));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareAnvil(org.bukkit.event.inventory.PrepareAnvilEvent event) {
        if (!(event.getView().getPlayer() instanceof Player player)) return;
        String kingdom = buffManager.getPlayerKingdomKey(player.getUniqueId());
        if (kingdom.equalsIgnoreCase("ZENITHAR")) {
            // Aristocratic Pride Debuff: Penempaan barang mewah istana rumit (+1 Level EXP repair cost)
            org.bukkit.inventory.AnvilInventory inv = event.getInventory();
            int cost = inv.getRepairCost();
            if (cost > 0) {
                inv.setRepairCost(cost + 1);
            }
        }
    }
}
