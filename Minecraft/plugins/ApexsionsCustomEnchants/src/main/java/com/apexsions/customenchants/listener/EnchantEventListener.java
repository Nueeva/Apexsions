package com.apexsions.customenchants.listener;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Core event listener handling custom enchantment gameplay abilities matching AdvancedEnchantments.
 */
public class EnchantEventListener implements Listener {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final NamespacedKey keyOverloadHealth;
    private final Map<UUID, RageTracker> rageTrackers = new ConcurrentHashMap<>();
    private final Set<UUID> flightGrantedByWings = ConcurrentHashMap.newKeySet();
    private final Set<Location> processingBlocks = ConcurrentHashMap.newKeySet();

    private record RageTracker(UUID targetId, int comboCount, long lastHitTime) {}

    public EnchantEventListener(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
        this.keyOverloadHealth = new NamespacedKey(plugin, "enchant_overload_health");

        // Scan armor effects, wings flight, and static buffs every 20 ticks (1 second)
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::scanArmorEnchants, 20L, 20L);
    }

    public void scanArmorEnchants() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            ItemStack boots = player.getInventory().getBoots();
            ItemStack chest = player.getInventory().getChestplate();
            ItemStack helm = player.getInventory().getHelmet();
            ItemStack[] armor = player.getInventory().getArmorContents();

            // 1. Wings (Flight on Boots)
            int wingsLvl = getEnchantLevel(boots, "wings");
            if (wingsLvl > 0) {
                if (!player.getAllowFlight()) {
                    player.setAllowFlight(true);
                }
                flightGrantedByWings.add(player.getUniqueId());
                if (player.isFlying()) {
                    Location loc = player.getLocation().add(0, 0.1, 0);
                    player.getWorld().spawnParticle(Particle.CLOUD, loc, 2, 0.1, 0.05, 0.1, 0.01);
                }
            } else if (flightGrantedByWings.remove(player.getUniqueId())) {
                if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                    player.setFlying(false);
                    player.setAllowFlight(false);
                    player.sendMessage(mm.deserialize("<red><bold>✖ WINGS OFF</bold> Efek Wings berakhir. Terbang dinonaktifkan.</red>"));
                }
            }

            // 2. Overload (Extra Hearts)
            int overloadLvl = getEnchantLevel(chest, "overload");
            if (overloadLvl == 0) {
                for (ItemStack piece : armor) {
                    int ol = getEnchantLevel(piece, "overload");
                    if (ol > overloadLvl) overloadLvl = ol;
                }
            }
            AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
            if (attr != null) {
                if (overloadLvl > 0) {
                    double extraHealth = overloadLvl * 4.0; // 2 hearts per level
                    boolean hasMod = false;
                    for (AttributeModifier mod : attr.getModifiers()) {
                        if (mod.getKey().equals(keyOverloadHealth)) {
                            hasMod = true;
                            break;
                        }
                    }
                    if (!hasMod) {
                        attr.addModifier(new AttributeModifier(keyOverloadHealth, extraHealth, AttributeModifier.Operation.ADD_NUMBER));
                    }
                } else {
                    for (AttributeModifier mod : new ArrayList<>(attr.getModifiers())) {
                        if (mod.getKey().equals(keyOverloadHealth)) {
                            attr.removeModifier(mod);
                        }
                    }
                }
            }

            // 3. Gears (Speed on Boots)
            int gearsLvl = Math.max(getEnchantLevel(boots, "gears"), getEnchantLevel(boots, "speed"));
            if (gearsLvl > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, gearsLvl - 1, false, false, false));
            }

            // 4. Springs (Jump Boost on Boots)
            int springsLvl = getEnchantLevel(boots, "springs");
            if (springsLvl > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, springsLvl - 1, false, false, false));
            }

            // 5. Antigravity (Jump Boost II + Slow Falling on Boots)
            int antiGravLvl = getEnchantLevel(boots, "antigravity");
            if (antiGravLvl > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 1, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0, false, false, false));
            }

            // 6. Glowing (Permanent Night Vision on Helmets)
            int glowingLvl = getEnchantLevel(helm, "glowing");
            if (glowingLvl > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0, false, false, false));
            }

            // 7. Aquatic / Rebreather (Permanent Water Breathing on Helmets)
            int aquaticLvl = Math.max(getEnchantLevel(helm, "aquatic"), getEnchantLevel(helm, "rebreather"));
            if (aquaticLvl > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 60, 0, false, false, false));
            }

            // 8. Obsidianshield (Permanent Fire Resistance on Armor)
            int obLvl = 0;
            for (ItemStack piece : armor) {
                int l = getEnchantLevel(piece, "obsidianshield");
                if (l > obLvl) obLvl = l;
            }
            if (obLvl > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0, false, false, false));
            }

            // 9. Implants / Feed (Restores hunger & saturation on Helmet)
            int implantsLvl = Math.max(getEnchantLevel(helm, "implants"), getEnchantLevel(helm, "feed"));
            if (implantsLvl > 0 && player.getFoodLevel() < 20) {
                if (ThreadLocalRandom.current().nextInt(100) < 25 * implantsLvl) {
                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1));
                    player.setSaturation(Math.min(20.0f, player.getSaturation() + 1.0f));
                }
            }

            // 10. Haste / Hasten (Haste when holding tool/weapon)
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            int hasteLvl = Math.max(getEnchantLevel(mainHand, "haste"), getEnchantLevel(mainHand, "hasten"));
            if (hasteLvl > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, hasteLvl - 1, false, false, false));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArmorChange(PlayerArmorChangeEvent event) {
        if (event.getSlotType() == PlayerArmorChangeEvent.SlotType.FEET) {
            Player player = event.getPlayer();
            ItemStack oldBoots = event.getOldItem();
            ItemStack newBoots = event.getNewItem();

            int newWings = getEnchantLevel(newBoots, "wings");
            int oldWings = getEnchantLevel(oldBoots, "wings");

            if (newWings > 0) {
                player.setAllowFlight(true);
                flightGrantedByWings.add(player.getUniqueId());
            } else if (oldWings > 0 && newWings <= 0) {
                if (flightGrantedByWings.remove(player.getUniqueId())) {
                    if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                        player.setFlying(false);
                        player.setAllowFlight(false);
                        player.sendMessage(mm.deserialize("<red><bold>✖ WINGS OFF</bold> Efek Wings berakhir. Terbang dinonaktifkan.</red>"));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        ItemStack boots = player.getInventory().getBoots();
        int wingsLvl = getEnchantLevel(boots, "wings");
        if (wingsLvl > 0) {
            player.setAllowFlight(true);
            flightGrantedByWings.add(player.getUniqueId());
        } else if (flightGrantedByWings.contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.setFlying(false);
            player.setAllowFlight(false);
            flightGrantedByWings.remove(player.getUniqueId());
            player.sendMessage(mm.deserialize("<red><bold>✖ WINGS OFF</bold> Efek Wings berakhir. Terbang dinonaktifkan.</red>"));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (flightGrantedByWings.remove(player.getUniqueId())) {
            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }
        rageTrackers.remove(player.getUniqueId());
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (event.getNewGameMode() == GameMode.SURVIVAL || event.getNewGameMode() == GameMode.ADVENTURE) {
            ItemStack boots = player.getInventory().getBoots();
            if (getEnchantLevel(boots, "wings") <= 0 && flightGrantedByWings.remove(player.getUniqueId())) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (flightGrantedByWings.remove(player.getUniqueId())) {
            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Player tempAttacker = null;
        if (event.getDamager() instanceof Player p) {
            tempAttacker = p;
        } else if (event.getDamager() instanceof Arrow arrow && arrow.getShooter() instanceof Player p) {
            tempAttacker = p;
        }

        final Player attacker = tempAttacker;
        final LivingEntity victim = event.getEntity() instanceof LivingEntity le ? le : null;

        if (attacker != null && victim != null) {
            ItemStack weapon = attacker.getInventory().getItemInMainHand();

            // 1. Strike / Thunderlord (Lightning Strike)
            checkAndApply(weapon, "strike", lvl -> {
                if (ThreadLocalRandom.current().nextInt(100) < 20 * lvl) {
                    victim.getWorld().strikeLightningEffect(victim.getLocation());
                    victim.damage(lvl * 2.0, attacker);
                }
            });
            checkAndApply(weapon, "thunderlord", lvl -> {
                if (ThreadLocalRandom.current().nextInt(100) < 25 * lvl) {
                    victim.getWorld().strikeLightningEffect(victim.getLocation());
                    victim.damage(lvl * 2.0, attacker);
                }
            });

            // 2. Freeze / Ice Aspect / Permafrost (Slow + Freeze Ticks)
            checkAndApply(weapon, "iceaspect", lvl -> {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40 * lvl, lvl - 1));
                victim.setFreezeTicks(Math.max(victim.getFreezeTicks(), 80 * lvl));
                victim.getWorld().spawnParticle(Particle.SNOWFLAKE, victim.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.05);
            });
            checkAndApply(weapon, "permafrost", lvl -> {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40 * lvl, lvl - 1));
                victim.setFreezeTicks(Math.max(victim.getFreezeTicks(), 80 * lvl));
                victim.getWorld().spawnParticle(Particle.SNOWFLAKE, victim.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.05);
            });

            // 3. Bleed / Twinge
            checkAndApply(weapon, "bleed", lvl -> {
                victim.getWorld().spawnParticle(Particle.DUST, victim.getLocation().add(0, 1, 0), 15, 0.2, 0.4, 0.2, new Particle.DustOptions(Color.RED, 1.5f));
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (victim.isValid() && !victim.isDead()) {
                        victim.damage(lvl * 1.5, attacker);
                    }
                }, 20L);
            });
            checkAndApply(weapon, "twinge", lvl -> {
                victim.getWorld().spawnParticle(Particle.DUST, victim.getLocation().add(0, 1, 0), 15, 0.2, 0.4, 0.2, new Particle.DustOptions(Color.RED, 1.5f));
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (victim.isValid() && !victim.isDead()) {
                        victim.damage(lvl * 1.5, attacker);
                    }
                }, 20L);
            });

            // 4. Lifesteal / Vampire
            checkAndApply(weapon, "lifesteal", lvl -> {
                double heal = lvl * 1.2;
                double maxHealth = attacker.getAttribute(Attribute.MAX_HEALTH).getValue();
                attacker.setHealth(Math.min(maxHealth, attacker.getHealth() + heal));
                attacker.getWorld().spawnParticle(Particle.HEART, attacker.getLocation().add(0, 2, 0), 2);
            });
            checkAndApply(weapon, "vampire", lvl -> {
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40 * lvl, 0));
            });

            // 5. Cleave / Ambit (AoE Sweep)
            checkAndApply(weapon, "cleave", lvl -> {
                double range = 2.0 + lvl;
                double cleaveDamage = event.getDamage() * 0.4 * lvl;
                for (Entity e : victim.getNearbyEntities(range, range, range)) {
                    if (e instanceof LivingEntity le && !e.equals(attacker) && !e.equals(victim)) {
                        le.damage(cleaveDamage, attacker);
                        le.getWorld().spawnParticle(Particle.SWEEP_ATTACK, le.getLocation().add(0, 1, 0), 1);
                    }
                }
            });
            checkAndApply(weapon, "ambit", lvl -> {
                double range = 2.0 + lvl;
                double cleaveDamage = event.getDamage() * 0.4 * lvl;
                for (Entity e : victim.getNearbyEntities(range, range, range)) {
                    if (e instanceof Monster m && !e.equals(victim)) {
                        m.damage(cleaveDamage, attacker);
                        m.getWorld().spawnParticle(Particle.SWEEP_ATTACK, m.getLocation().add(0, 1, 0), 1);
                    }
                }
            });

            // 6. Rage (Combo Damage Multiplier)
            checkAndApply(weapon, "rage", lvl -> {
                RageTracker tracker = rageTrackers.get(attacker.getUniqueId());
                long now = System.currentTimeMillis();
                int combo = 1;
                if (tracker != null && tracker.targetId.equals(victim.getUniqueId()) && now - tracker.lastHitTime < 3000) {
                    combo = Math.min(5, tracker.comboCount + 1);
                }
                rageTrackers.put(attacker.getUniqueId(), new RageTracker(victim.getUniqueId(), combo, now));
                double bonus = (combo * 0.1) * lvl;
                event.setDamage(event.getDamage() * (1.0 + bonus));
            });

            // 7. Blind
            checkAndApply(weapon, "blind", lvl -> {
                if (ThreadLocalRandom.current().nextInt(100) < 20 * lvl) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40 * lvl, 0));
                }
            });

            // 8. Paralyze
            checkAndApply(weapon, "paralyze", lvl -> {
                if (ThreadLocalRandom.current().nextInt(100) < 15 * lvl) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40 * lvl, 1));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 40 * lvl, 1));
                }
            });

            // 9. Disarm / Neutralize
            checkAndApply(weapon, "disarm", lvl -> {
                if (victim instanceof Player pVictim && ThreadLocalRandom.current().nextInt(100) < 5 * lvl) {
                    ItemStack vHand = pVictim.getInventory().getItemInMainHand();
                    if (vHand.getType() != Material.AIR) {
                        pVictim.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                        pVictim.getInventory().addItem(vHand);
                        pVictim.playSound(pVictim.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1.0f, 0.8f);
                        pVictim.sendMessage(mm.deserialize("<red><bold>⚔ DISARMED!</bold> Senjatamu terlempar dari tangan!</red>"));
                    }
                }
            });
            checkAndApply(weapon, "neutralize", lvl -> {
                if (victim instanceof Player pVictim && ThreadLocalRandom.current().nextInt(100) < 5 * lvl) {
                    ItemStack vHand = pVictim.getInventory().getItemInMainHand();
                    if (vHand.getType() != Material.AIR) {
                        pVictim.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                        pVictim.getInventory().addItem(vHand);
                        pVictim.playSound(pVictim.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1.0f, 0.8f);
                        pVictim.sendMessage(mm.deserialize("<red><bold>⚔ DISARMED!</bold> Senjatamu terlempar dari tangan!</red>"));
                    }
                }
            });

            // 10. Confuse (Nausea)
            checkAndApply(weapon, "confuse", lvl -> {
                if (ThreadLocalRandom.current().nextInt(100) < 20 * lvl) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60 * lvl, 0));
                }
            });

            // 11. Molten / Inflame / Immolation / Spark (Fire)
            checkAndApply(weapon, "molten", lvl -> victim.setFireTicks(Math.max(victim.getFireTicks(), 40 * lvl)));
            checkAndApply(weapon, "inflame", lvl -> victim.setFireTicks(Math.max(victim.getFireTicks(), 40 * lvl)));
            checkAndApply(weapon, "immolation", lvl -> victim.setFireTicks(Math.max(victim.getFireTicks(), 40 * lvl)));
            checkAndApply(weapon, "spark", lvl -> victim.setFireTicks(Math.max(victim.getFireTicks(), 40 * lvl)));

            // 12. Poisoned Hook (Poison)
            checkAndApply(weapon, "poisoned hook", lvl -> victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40 * lvl, 0)));

            // 13. Chaos (Wither)
            checkAndApply(weapon, "chaos", lvl -> victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40 * lvl, 0)));

            // 14. Critical (Damage multiplier)
            checkAndApply(weapon, "critical", lvl -> {
                if (ThreadLocalRandom.current().nextInt(100) < 20 * lvl) {
                    event.setDamage(event.getDamage() * 1.5);
                    victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.1);
                }
            });

            // 15. Double Strike
            checkAndApply(weapon, "doublestrike", lvl -> {
                if (ThreadLocalRandom.current().nextInt(100) < 15 * lvl) {
                    event.setDamage(event.getDamage() * 1.6);
                    attacker.sendMessage(mm.deserialize("<gold><bold>⚔ DOUBLE STRIKE!</bold></gold>"));
                }
            });

            // 16. Impact (Trident double damage)
            checkAndApply(weapon, "impact", lvl -> {
                if (ThreadLocalRandom.current().nextInt(100) < 20 * lvl) {
                    event.setDamage(event.getDamage() * 2.0);
                    victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1, 0), 10);
                }
            });

            // 17. Famine (Hunger effect to victim)
            checkAndApply(weapon, "famine", lvl -> {
                if (ThreadLocalRandom.current().nextInt(100) < 20 * lvl) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 60 * lvl, 0));
                }
            });

            // 18. Berserk (Strength + Mining fatigue to attacker)
            checkAndApply(weapon, "berserk", lvl -> {
                if (ThreadLocalRandom.current().nextInt(100) < 15 * lvl) {
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40 * lvl, lvl - 1));
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 40 * lvl, 0));
                }
            });

            // 19. Forcefield (Push victim away)
            checkAndApply(weapon, "forcefield", lvl -> {
                if (ThreadLocalRandom.current().nextInt(100) < 15 * lvl) {
                    Vector push = victim.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize().multiply(1.2 * lvl).setY(0.3);
                    victim.setVelocity(push);
                    victim.getWorld().spawnParticle(Particle.EXPLOSION, victim.getLocation().add(0, 1, 0), 2);
                }
            });

            // 20. Epicness (Sound + particle effects)
            checkAndApply(weapon, "epicness", lvl -> {
                victim.getWorld().spawnParticle(Particle.FIREWORK, victim.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
                attacker.playSound(victim.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, 1.2f);
            });
        }

        // Defender Armor Enchantments
        if (victim instanceof Player defPlayer) {
            final Player finalDefPlayer = defPlayer;
            final double baseDamage = event.getDamage();
            ItemStack[] armor = defPlayer.getInventory().getArmorContents();

            // Dodge
            for (ItemStack piece : armor) {
                checkAndApply(piece, "dodge", lvl -> {
                    if (ThreadLocalRandom.current().nextInt(100) < 5 * lvl) {
                        event.setCancelled(true);
                        finalDefPlayer.getWorld().spawnParticle(Particle.POOF, finalDefPlayer.getLocation().add(0, 1, 0), 5);
                        finalDefPlayer.playSound(finalDefPlayer.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.5f);
                        finalDefPlayer.sendMessage(mm.deserialize("<aqua><bold>💨 DODGE!</bold> Berhasil menghindar dari serangan!</aqua>"));
                    }
                });
                if (event.isCancelled()) return;
            }

            // Block
            if (attacker != null) {
                checkAndApply(defPlayer.getInventory().getItemInMainHand(), "block", lvl -> {
                    if (ThreadLocalRandom.current().nextInt(100) < 10 * lvl) {
                        event.setCancelled(true);
                        attacker.damage(lvl * 1.5, finalDefPlayer);
                        finalDefPlayer.playSound(finalDefPlayer.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
                        finalDefPlayer.sendMessage(mm.deserialize("<aqua><bold>🛡 BLOCKED & COUNTERED!</bold></aqua>"));
                    }
                });
                if (event.isCancelled()) return;
            }

            // Cactus
            for (ItemStack piece : armor) {
                checkAndApply(piece, "cactus", lvl -> {
                    if (attacker != null && ThreadLocalRandom.current().nextInt(100) < 25 * lvl) {
                        attacker.damage(baseDamage * 0.25, finalDefPlayer);
                    }
                });
            }

            // Enlightened
            for (ItemStack piece : armor) {
                checkAndApply(piece, "enlightened", lvl -> {
                    if (ThreadLocalRandom.current().nextInt(100) < 15 * lvl) {
                        double maxH = finalDefPlayer.getAttribute(Attribute.MAX_HEALTH).getValue();
                        finalDefPlayer.setHealth(Math.min(maxH, finalDefPlayer.getHealth() + (lvl * 2.0)));
                        finalDefPlayer.getWorld().spawnParticle(Particle.HEART, finalDefPlayer.getLocation().add(0, 2, 0), 2);
                    }
                });
            }

            // Armored (Sword damage reduction)
            if (attacker != null && attacker.getInventory().getItemInMainHand().getType().name().endsWith("_SWORD")) {
                for (ItemStack piece : armor) {
                    checkAndApply(piece, "armored", lvl -> event.setDamage(event.getDamage() * (1.0 - (0.02 * lvl))));
                }
            }

            // Tank (Axe damage reduction)
            if (attacker != null && attacker.getInventory().getItemInMainHand().getType().name().endsWith("_AXE")) {
                for (ItemStack piece : armor) {
                    checkAndApply(piece, "tank", lvl -> event.setDamage(event.getDamage() * (1.0 - (0.02 * lvl))));
                }
            }

            // Heavy (Bow damage reduction)
            if (event.getDamager() instanceof Arrow) {
                for (ItemStack piece : armor) {
                    checkAndApply(piece, "heavy", lvl -> event.setDamage(event.getDamage() * (1.0 - (0.02 * lvl))));
                }
            }

            // Safeguard (Resistance on hit)
            for (ItemStack piece : armor) {
                checkAndApply(piece, "safeguard", lvl -> {
                    if (ThreadLocalRandom.current().nextInt(100) < 15 * lvl) {
                        finalDefPlayer.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40 * lvl, 0));
                    }
                });
            }

            // Reflect (Reflect damage back to attacker)
            for (ItemStack piece : armor) {
                checkAndApply(piece, "reflect", lvl -> {
                    if (attacker != null && ThreadLocalRandom.current().nextInt(100) < 8 * lvl) {
                        event.setCancelled(true);
                        attacker.damage(baseDamage, finalDefPlayer);
                        finalDefPlayer.playSound(finalDefPlayer.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.5f);
                        finalDefPlayer.sendMessage(mm.deserialize("<gold><bold>⚡ REFLECT!</bold> Kamu memantulkan kembali serangan lawan!</gold>"));
                    }
                });
                if (event.isCancelled()) return;
            }

            // Ward (Absorb incoming damage)
            for (ItemStack piece : armor) {
                checkAndApply(piece, "ward", lvl -> {
                    if (ThreadLocalRandom.current().nextInt(100) < 10 * lvl) {
                        event.setDamage(event.getDamage() * 0.5);
                        finalDefPlayer.getWorld().spawnParticle(Particle.ENCHANTED_HIT, finalDefPlayer.getLocation().add(0, 1, 0), 10);
                    }
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFatalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // 1. Fall Damage Negation (Wings / Jelly Legs / Antigravity)
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            ItemStack boots = player.getInventory().getBoots();
            int wings = getEnchantLevel(boots, "wings");
            int jelly = getEnchantLevel(boots, "jellylegs");
            int anti = getEnchantLevel(boots, "antigravity");
            if (wings > 0 || jelly > 0 || anti > 0) {
                event.setCancelled(true);
                return;
            }
        }

        // 2. Obsidianshield (Fire / Lava immunity)
        if (event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
            for (ItemStack piece : player.getInventory().getArmorContents()) {
                if (getEnchantLevel(piece, "obsidianshield") > 0) {
                    event.setCancelled(true);
                    player.setFireTicks(0);
                    return;
                }
            }
        }

        // 3. Phoenix (Fatal save)
        if (event.getFinalDamage() >= player.getHealth()) {
            ItemStack chest = player.getInventory().getChestplate();
            int phoenixLvl = getEnchantLevel(chest, "phoenix");
            if (phoenixLvl == 0) {
                for (ItemStack piece : player.getInventory().getArmorContents()) {
                    int pl = getEnchantLevel(piece, "phoenix");
                    if (pl > phoenixLvl) phoenixLvl = pl;
                }
            }
            if (phoenixLvl > 0) {
                event.setCancelled(true);
                player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
                player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
                player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.2);
                player.sendMessage(mm.deserialize("<gradient:#e67e22:#f1c40f><bold>🔥 PHOENIX REBIRTH!</bold> Sihir Phoenix menyelamatkanmu dari kematian!</gradient>"));
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();

        // Inquisitive / Experience (EXP Multiplier)
        int expLvl = Math.max(getEnchantLevel(weapon, "inquisitive"), getEnchantLevel(weapon, "experience"));
        if (expLvl > 0) {
            event.setDroppedExp((int) (event.getDroppedExp() * (1.0 + (expLvl * 0.5))));
        }

        // Decapitation
        checkAndApply(weapon, "decapitation", lvl -> {
            if (ThreadLocalRandom.current().nextInt(100) < 10 * lvl) {
                Material head = switch (event.getEntityType()) {
                    case ZOMBIE -> Material.ZOMBIE_HEAD;
                    case SKELETON -> Material.SKELETON_SKULL;
                    case CREEPER -> Material.CREEPER_HEAD;
                    case WITHER_SKELETON -> Material.WITHER_SKELETON_SKULL;
                    case PIGLIN -> Material.PIGLIN_HEAD;
                    case PLAYER -> Material.PLAYER_HEAD;
                    default -> null;
                };
                if (head != null) {
                    event.getDrops().add(new ItemStack(head));
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        Block block = event.getBlock();

        if (processingBlocks.contains(block.getLocation())) return;

        // 1. AutoSmelt / Smelting
        int smeltLvl = Math.max(getEnchantLevel(tool, "autosmelt"), getEnchantLevel(tool, "smelting"));
        if (smeltLvl > 0) {
            Collection<ItemStack> drops = block.getDrops(tool, player);
            List<ItemStack> newDrops = new ArrayList<>();
            boolean smelted = false;

            for (ItemStack drop : drops) {
                ItemStack smeltedItem = getSmeltedProduct(drop);
                if (!smeltedItem.isSimilar(drop)) smelted = true;
                newDrops.add(smeltedItem);
            }

            if (smelted) {
                event.setDropItems(false);
                for (ItemStack is : newDrops) {
                    block.getWorld().dropItemNaturally(block.getLocation(), is);
                }
                block.getWorld().spawnParticle(Particle.FLAME, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.02);
            }
        }

        // 2. Telepathy
        int telepathyLvl = getEnchantLevel(tool, "telepathy");
        if (telepathyLvl > 0) {
            event.setDropItems(false);
            Collection<ItemStack> drops = block.getDrops(tool, player);
            for (ItemStack drop : drops) {
                ItemStack itemToAdd = (smeltLvl > 0) ? getSmeltedProduct(drop) : drop;
                HashMap<Integer, ItemStack> left = player.getInventory().addItem(itemToAdd);
                for (ItemStack rem : left.values()) {
                    block.getWorld().dropItemNaturally(block.getLocation(), rem);
                }
            }
        }

        // 3. Replanter (Auto-Replant mature crops)
        int replanterLvl = getEnchantLevel(tool, "replanter");
        if (replanterLvl > 0 && block.getBlockData() instanceof org.bukkit.block.data.Ageable ageable) {
            if (ageable.getAge() >= ageable.getMaximumAge()) {
                Material cropType = block.getType();
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    block.setType(cropType);
                    if (block.getBlockData() instanceof org.bukkit.block.data.Ageable newAge) {
                        newAge.setAge(0);
                        block.setBlockData(newAge);
                        block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 0.2, 0.5), 3, 0.2, 0.1, 0.2);
                    }
                });
            }
        }

        // 4. Timber (Axes)
        int timberLvl = getEnchantLevel(tool, "timber");
        if (timberLvl > 0 && !processingBlocks.contains(block.getLocation())) {
            String name = block.getType().name();
            if (name.endsWith("_LOG") || name.endsWith("_WOOD")) {
                breakTree(block, tool, player);
            }
        }

        // 5. Trench / Blast (Pickaxes / Shovels 3x3)
        int trenchLvl = Math.max(getEnchantLevel(tool, "trench"), getEnchantLevel(tool, "blast"));
        if (trenchLvl > 0 && !processingBlocks.contains(block.getLocation())) {
            breakTrench(block, tool, player);
        }

        // 6. Replenish (Food while mining)
        int replenishLvl = getEnchantLevel(tool, "replenish");
        if (replenishLvl > 0 && player.getFoodLevel() < 20) {
            if (ThreadLocalRandom.current().nextInt(100) < 10 * replenishLvl) {
                player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1));
            }
        }

        // 7. Harvest (3x3 crop harvesting for Hoes)
        int harvestLvl = getEnchantLevel(tool, "harvest");
        if (harvestLvl > 0 && block.getBlockData() instanceof org.bukkit.block.data.Ageable) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    Block nearby = block.getRelative(dx, 0, dz);
                    if (nearby.getBlockData() instanceof org.bukkit.block.data.Ageable ageable && ageable.getAge() >= ageable.getMaximumAge()) {
                        processingBlocks.add(nearby.getLocation());
                        try {
                            nearby.breakNaturally(tool);
                        } finally {
                            processingBlocks.remove(nearby.getLocation());
                        }
                    }
                }
            }
        }

        // 8. Gemify (Ore blocks turn into raw mineral blocks)
        int gemifyLvl = getEnchantLevel(tool, "gemify");
        if (gemifyLvl > 0 && ThreadLocalRandom.current().nextInt(100) < 10 * gemifyLvl) {
            Material mineralBlock = switch (block.getType()) {
                case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> Material.DIAMOND_BLOCK;
                case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.IRON_BLOCK;
                case GOLD_ORE, DEEPSLATE_GOLD_ORE -> Material.GOLD_BLOCK;
                case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> Material.EMERALD_BLOCK;
                case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.COPPER_BLOCK;
                case COAL_ORE, DEEPSLATE_COAL_ORE -> Material.COAL_BLOCK;
                case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> Material.REDSTONE_BLOCK;
                case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> Material.LAPIS_BLOCK;
                default -> null;
            };
            if (mineralBlock != null) {
                event.setDropItems(false);
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(mineralBlock));
                block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.1);
                player.playSound(block.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 1.4f);
                player.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>💎 GEMIFY!</bold> Bongkahan bijih berubah menjadi blok mineral murni!</gradient>"));
            }
        }
    }

    private void breakTree(Block origin, ItemStack tool, Player player) {
        Queue<Block> queue = new LinkedList<>();
        Set<Block> logsToBreak = new HashSet<>();
        queue.add(origin);
        logsToBreak.add(origin);

        while (!queue.isEmpty() && logsToBreak.size() < 64) {
            Block current = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Block neighbor = current.getRelative(dx, dy, dz);
                        if (!logsToBreak.contains(neighbor)) {
                            String name = neighbor.getType().name();
                            if (name.endsWith("_LOG") || name.endsWith("_WOOD")) {
                                logsToBreak.add(neighbor);
                                queue.add(neighbor);
                                if (logsToBreak.size() >= 64) break;
                            }
                        }
                    }
                    if (logsToBreak.size() >= 64) break;
                }
                if (logsToBreak.size() >= 64) break;
            }
        }

        for (Block b : logsToBreak) {
            if (b.equals(origin)) continue;
            processingBlocks.add(b.getLocation());
            try {
                b.breakNaturally(tool);
            } finally {
                processingBlocks.remove(b.getLocation());
            }
        }
    }

    private void breakTrench(Block origin, ItemStack tool, Player player) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block b = origin.getRelative(x, y, z);
                    if (b.getType().isAir() || b.getType() == Material.BEDROCK || b.getType() == Material.BARRIER) continue;
                    if (b.getType().getHardness() < 0) continue;

                    processingBlocks.add(b.getLocation());
                    try {
                        b.breakNaturally(tool);
                    } finally {
                        processingBlocks.remove(b.getLocation());
                    }
                }
            }
        }
    }

    private ItemStack getSmeltedProduct(ItemStack drop) {
        if (drop == null || drop.getType().isAir()) return drop;
        Material mat = switch (drop.getType()) {
            case RAW_IRON -> Material.IRON_INGOT;
            case RAW_GOLD -> Material.GOLD_INGOT;
            case RAW_COPPER -> Material.COPPER_INGOT;
            case COBBLESTONE -> Material.STONE;
            case COBBLED_DEEPSLATE -> Material.DEEPSLATE;
            case SAND, RED_SAND -> Material.GLASS;
            case CLAY_BALL -> Material.BRICK;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            case PORKCHOP -> Material.COOKED_PORKCHOP;
            case BEEF -> Material.COOKED_BEEF;
            case CHICKEN -> Material.COOKED_CHICKEN;
            case MUTTON -> Material.COOKED_MUTTON;
            case COD -> Material.COOKED_COD;
            case SALMON -> Material.COOKED_SALMON;
            case POTATO -> Material.BAKED_POTATO;
            case OAK_LOG, BIRCH_LOG, SPRUCE_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG, MANGROVE_LOG, CHERRY_LOG -> Material.CHARCOAL;
            default -> drop.getType();
        };
        if (mat != drop.getType()) {
            return new ItemStack(mat, drop.getAmount());
        }
        return drop;
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        int unb = Math.max(getEnchantLevel(item, "unbreakable"), getEnchantLevel(item, "abiding"));
        if (unb > 0) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack bow = event.getBow();

        // MultiArrow
        checkAndApply(bow, "multiarrow", lvl -> {
            Vector dir = event.getProjectile().getVelocity();
            for (int i = 0; i < lvl; i++) {
                Arrow extra = player.launchProjectile(Arrow.class);
                extra.setVelocity(dir.clone().rotateAroundY(Math.toRadians((i + 1) * 6)));
            }
        });

        // Sniper
        checkAndApply(bow, "sniper", lvl -> {
            event.getProjectile().setVelocity(event.getProjectile().getVelocity().multiply(1.0 + (lvl * 0.25)));
        });

        // Hellfire / Missile (Flame & Explosive arrow)
        int hellLvl = Math.max(getEnchantLevel(bow, "hellfire"), getEnchantLevel(bow, "missile"));
        if (hellLvl > 0 && event.getProjectile() instanceof Arrow arrow) {
            arrow.setVisualFire(true);
            arrow.setFireTicks(200);
        }

        // Frenzy (Haste & Speed on shoot)
        checkAndApply(bow, "frenzy", lvl -> {
            if (ThreadLocalRandom.current().nextInt(100) < 25 * lvl) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 60 * lvl, lvl - 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60 * lvl, 0));
            }
        });
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player player)) return;

        ItemStack bow = player.getInventory().getItemInMainHand();
        if (bow.getType() != Material.BOW && bow.getType() != Material.CROSSBOW) {
            bow = player.getInventory().getItemInOffHand();
        }

        checkAndApply(bow, "explosive", lvl -> {
            if (ThreadLocalRandom.current().nextInt(100) < 20 * lvl) {
                Location loc = arrow.getLocation();
                loc.getWorld().createExplosion(loc, 1.5f + (lvl * 0.5f), false, false);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack rod = player.getInventory().getItemInMainHand();
        if (rod.getType() != Material.FISHING_ROD) {
            rod = player.getInventory().getItemInOffHand();
        }
        if (rod.getType() != Material.FISHING_ROD) return;

        // 1. AutoReel (Automatically reels in when fish bites)
        int autoReelLvl = getEnchantLevel(rod, "autoreel");
        if (autoReelLvl > 0 && event.getState() == PlayerFishEvent.State.BITE) {
            FishHook hook = event.getHook();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (hook.isValid()) {
                    boolean retrieved = false;
                    try {
                        java.lang.reflect.Method m = hook.getClass().getMethod("retrieve", EquipmentSlot.class);
                        m.invoke(hook, EquipmentSlot.HAND);
                        retrieved = true;
                    } catch (Throwable ignored) {}
                    if (!retrieved) {
                        try {
                            java.lang.reflect.Method m = hook.getClass().getMethod("retrieve", ItemStack.class);
                            m.invoke(hook, player.getInventory().getItemInMainHand());
                            retrieved = true;
                        } catch (Throwable ignored) {}
                    }
                    if (!retrieved) {
                        hook.pullHookedEntity();
                        hook.remove();
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 1.2f);
                    player.sendMessage(mm.deserialize("<gold><bold>🎣 AUTO REEL!</bold> Kail otomatis menarik tangkapan!</gold>"));
                }
            });
        }

        // 2. Bait (Double drops on CAUGHT_FISH)
        checkAndApply(rod, "bait", lvl -> {
            if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Item caughtItem) {
                if (ThreadLocalRandom.current().nextInt(100) < 25 * lvl) {
                    ItemStack duplicate = caughtItem.getItemStack().clone();
                    player.getWorld().dropItemNaturally(caughtItem.getLocation(), duplicate);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.5f);
                    player.sendMessage(mm.deserialize("<gold><bold>🎣 BAIT BONUS!</bold> Umpan ajaib melipatgandakan hasil tangkapanmu!</gold>"));
                }
            }
        });

        // 3. Hook (Extra EXP on CAUGHT_FISH)
        checkAndApply(rod, "hook", lvl -> {
            if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
                event.setExpToDrop((int) (event.getExpToDrop() * (1.0 + (lvl * 0.75))));
            }
        });

        // 4. Lucky (Luck effect while fishing)
        checkAndApply(rod, "lucky", lvl -> {
            if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH || event.getState() == PlayerFishEvent.State.BITE) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 200 * lvl, 0));
            }
        });

        // 5. Sharpness Hook (Damage entity on hook)
        checkAndApply(rod, "sharpnesshook", lvl -> {
            if (event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY && event.getCaught() instanceof LivingEntity victim) {
                victim.damage(lvl * 2.5, player);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);
            }
        });

        // 6. Poisoned Hook
        checkAndApply(rod, "poisonedhook", lvl -> {
            if (event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY && event.getCaught() instanceof LivingEntity victim) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60 * lvl, 0));
            }
        });

        // 7. Fire Hook
        checkAndApply(rod, "firehook", lvl -> {
            if (event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY && event.getCaught() instanceof LivingEntity victim) {
                victim.setFireTicks(60 * lvl);
            }
        });

        // 8. Snap (Pull hooked entity towards player)
        checkAndApply(rod, "snap", lvl -> {
            if (event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY && event.getCaught() instanceof LivingEntity victim) {
                Vector pull = player.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize().multiply(1.0 + (0.3 * lvl)).setY(0.4);
                victim.setVelocity(pull);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.FARMLAND) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();

        int planterLvl = getEnchantLevel(tool, "planter");
        int potatoLvl = getEnchantLevel(tool, "potatoplanter");
        int carrotLvl = getEnchantLevel(tool, "carrotplanter");

        if (planterLvl <= 0 && potatoLvl <= 0 && carrotLvl <= 0) return;

        // Determine seed and crop type
        Material seedType = null;
        Material cropType = null;

        if (planterLvl > 0 && (tool.getType() == Material.WHEAT_SEEDS || off.getType() == Material.WHEAT_SEEDS)) {
            seedType = Material.WHEAT_SEEDS;
            cropType = Material.WHEAT;
        } else if (potatoLvl > 0 && (tool.getType() == Material.POTATO || off.getType() == Material.POTATO)) {
            seedType = Material.POTATO;
            cropType = Material.POTATOES;
        } else if (carrotLvl > 0 && (tool.getType() == Material.CARROT || off.getType() == Material.CARROT)) {
            seedType = Material.CARROT;
            cropType = Material.CARROTS;
        }

        if (seedType == null || cropType == null) return;

        boolean plantedAny = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block farm = block.getRelative(dx, 0, dz);
                Block above = farm.getRelative(0, 1, 0);
                if (farm.getType() == Material.FARMLAND && above.getType().isAir()) {
                    if (player.getGameMode() == GameMode.CREATIVE || player.getInventory().containsAtLeast(new ItemStack(seedType), 1)) {
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            player.getInventory().removeItem(new ItemStack(seedType, 1));
                        }
                        above.setType(cropType);
                        plantedAny = true;
                    }
                }
            }
        }
        if (plantedAny) {
            player.playSound(block.getLocation(), Sound.ITEM_CROP_PLANT, 1.0f, 1.0f);
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 1.0, 0.5), 8, 0.5, 0.2, 0.5);
        }
    }

    private int getEnchantLevel(ItemStack item, String enchantId) {
        return plugin.getEnchantmentRegistry().getEnchantLevel(item, enchantId);
    }

    private void checkAndApply(ItemStack item, String enchantId, java.util.function.Consumer<Integer> action) {
        int lvl = getEnchantLevel(item, enchantId);
        if (lvl > 0) {
            action.accept(lvl);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) return;

        int button = event.whichButton(); // 0 (Low), 1 (Medium), 2 (High / Level 30)

        // Custom enchant roll chance based on button clicked:
        // Button 0 (Exp Level 1-10): 25%
        // Button 1 (Exp Level 10-20): 50%
        // Button 2 (Exp Level 30): 80%
        int baseChance = switch (button) {
            case 0 -> 25;
            case 1 -> 50;
            case 2 -> 80;
            default -> 30;
        };

        boolean wonCustomEnchant = ThreadLocalRandom.current().nextInt(100) < baseChance;

        // Schedule lore & glint update on next tick (to format high level vanilla enchants in Roman numerals)
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getEnchantmentRegistry().updateLoreAndGlint(item);
            player.updateInventory();
        });

        if (!wonCustomEnchant) {
            return;
        }

        // Special case: Player is enchanting a regular BOOK
        if (item.getType() == Material.BOOK) {
            List<CustomEnchant> bookEligible = plugin.getEnchantmentRegistry().getAllEnchantments().stream()
                    .filter(CustomEnchant::isEnchantable) // Strictly excludes wings
                    .toList();
            if (!bookEligible.isEmpty()) {
                String targetTier = rollTierForButton(button);
                CustomEnchant chosen = pickEnchantByTierFallback(bookEligible, targetTier);
                if (chosen == null) {
                    chosen = bookEligible.get(ThreadLocalRandom.current().nextInt(bookEligible.size()));
                }
                final CustomEnchant finalBookEnchant = chosen;
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    ItemStack customBook = plugin.getEnchantBookManager().createBook(finalBookEnchant, 1, 80, 20);
                    player.getInventory().addItem(customBook);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.8f);
                    player.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>✨ ENCHANTING TABLE BONUS BOOK!</bold></gradient> " +
                            "Kamu juga mendapatkan Buku Sihir <color:" + finalBookEnchant.getGroup().getColor() + "><bold>" + finalBookEnchant.getDisplayName() + " I</bold></color>!"));
                });
            }
            return;
        }

        // Filter eligible custom enchantments for equipment:
        // 1. isEnchantable() == true (Strictly excludes wings and non-table enchants)
        // 2. canApplyTo(item) == true (Item type must match)
        // 3. current level on item < maxLevel
        List<CustomEnchant> eligible = plugin.getEnchantmentRegistry().getAllEnchantments().stream()
                .filter(CustomEnchant::isEnchantable)
                .filter(e -> e.canApplyTo(item))
                .filter(e -> getEnchantLevel(item, e.getId()) < e.getMaxLevel())
                .toList();

        if (eligible.isEmpty()) {
            return;
        }

        // Determine target rarity tier based on button
        String targetTier = rollTierForButton(button);

        // Pick enchantment matching targetTier, with fallback to other tiers
        CustomEnchant chosen = pickEnchantByTierFallback(eligible, targetTier);
        if (chosen == null) {
            chosen = eligible.get(ThreadLocalRandom.current().nextInt(eligible.size()));
        }

        int currentLvl = getEnchantLevel(item, chosen.getId());
        int newLvl = currentLvl + 1;
        // On Level 30 enchant (button 2), 20% bonus chance to start at Level II directly if maxLevel >= 2 and fresh
        if (button == 2 && chosen.getMaxLevel() >= 2 && currentLvl == 0 && ThreadLocalRandom.current().nextInt(100) < 20) {
            newLvl = 2;
        }

        final CustomEnchant firstEnchant = chosen;
        final int firstLvl = newLvl;

        // Apply immediately to item meta
        plugin.getEnchantmentRegistry().applyEnchantDirect(item, firstEnchant, firstLvl);

        // Also schedule on next tick to ensure compatibility with CraftBukkit ContainerEnchantTable post-processing
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getEnchantmentRegistry().applyEnchantDirect(item, firstEnchant, firstLvl);
            plugin.getEnchantmentRegistry().updateLoreAndGlint(item);
            player.updateInventory();
        });

        // Audio-visual celebratory feedback
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.8f);
        player.spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1.2, 0), 25, 0.4, 0.4, 0.4, 0.1);

        String roman = CustomEnchant.toRoman(firstLvl);
        String grpColor = firstEnchant.getGroup().getColor();
        player.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>✨ ENCHANTING TABLE BONUS!</bold></gradient> " +
                "Itemmu mendapatkan sihir khusus <color:" + grpColor + "><bold>" + firstEnchant.getDisplayName() + " " + roman + "</bold></color> " +
                "<gray>(" + firstEnchant.getGroup().getDisplayName() + ")</gray>!"));

        // Button 2 (Level 30) exclusive: 15% chance to roll a 2nd custom enchant!
        if (button == 2 && ThreadLocalRandom.current().nextInt(100) < 15) {
            List<CustomEnchant> secondEligible = eligible.stream()
                    .filter(e -> !e.getId().equalsIgnoreCase(firstEnchant.getId()))
                    .toList();
            if (!secondEligible.isEmpty()) {
                String secondTier = rollTierForButton(button);
                CustomEnchant secondChosen = pickEnchantByTierFallback(secondEligible, secondTier);
                if (secondChosen != null) {
                    int secLvl = getEnchantLevel(item, secondChosen.getId()) + 1;
                    final CustomEnchant finalSecond = secondChosen;
                    final int finalSecLvl = secLvl;

                    plugin.getEnchantmentRegistry().applyEnchantDirect(item, finalSecond, finalSecLvl);
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        plugin.getEnchantmentRegistry().applyEnchantDirect(item, finalSecond, finalSecLvl);
                        plugin.getEnchantmentRegistry().updateLoreAndGlint(item);
                        player.updateInventory();
                    });

                    player.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>✨ DOUBLE BONUS!</bold></gradient> " +
                            "Itemmu juga mendapatkan <color:" + finalSecond.getGroup().getColor() + "><bold>" + finalSecond.getDisplayName() + " " + CustomEnchant.toRoman(finalSecLvl) + "</bold></color>!"));
                }
            }
        }
    }

    private String rollTierForButton(int button) {
        int roll = ThreadLocalRandom.current().nextInt(100);
        return switch (button) {
            case 0 -> { // Button 0 (Top / Low cost)
                if (roll < 75) yield "SIMPLE";
                if (roll < 95) yield "UNIQUE";
                yield "ELITE";
            }
            case 1 -> { // Button 1 (Middle cost)
                if (roll < 35) yield "SIMPLE";
                if (roll < 75) yield "UNIQUE";
                if (roll < 93) yield "ELITE";
                if (roll < 99) yield "ULTIMATE";
                yield "LEGENDARY";
            }
            case 2 -> { // Button 2 (Level 30 / Max cost)
                if (roll < 15) yield "SIMPLE";
                if (roll < 40) yield "UNIQUE";
                if (roll < 75) yield "ELITE";
                if (roll < 93) yield "ULTIMATE";
                if (roll < 99) yield "LEGENDARY";
                yield "FABLED";
            }
            default -> "SIMPLE";
        };
    }

    private CustomEnchant pickEnchantByTierFallback(List<CustomEnchant> pool, String targetTier) {
        List<CustomEnchant> inTier = pool.stream()
                .filter(e -> e.getGroup().getId().equalsIgnoreCase(targetTier))
                .toList();
        if (!inTier.isEmpty()) {
            return inTier.get(ThreadLocalRandom.current().nextInt(inTier.size()));
        }

        // Ordered fallbacks
        String[] tiers = {"FABLED", "LEGENDARY", "ULTIMATE", "ELITE", "UNIQUE", "SIMPLE"};
        for (String t : tiers) {
            List<CustomEnchant> list = pool.stream()
                    .filter(e -> e.getGroup().getId().equalsIgnoreCase(t))
                    .toList();
            if (!list.isEmpty()) {
                return list.get(ThreadLocalRandom.current().nextInt(list.size()));
            }
        }
        return pool.isEmpty() ? null : pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }
}
