package com.apexsions.customenchants.listener;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Core event listener handling custom enchantment gameplay abilities.
 */
public class EnchantEventListener implements Listener {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final NamespacedKey keyOverloadHealth;
    private final Map<UUID, RageTracker> rageTrackers = new ConcurrentHashMap<>();

    private record RageTracker(UUID targetId, int comboCount, long lastHitTime) {}

    public EnchantEventListener(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
        this.keyOverloadHealth = new NamespacedKey(plugin, "enchant_overload_health");

        // Scan overload and gears every second
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::scanArmorEnchants, 20L, 20L);
    }

    public void scanArmorEnchants() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            ItemStack chest = player.getInventory().getChestplate();
            CustomEnchant overload = plugin.getEnchantmentRegistry().getEnchantment("overload");
            int lvl = overload != null ? plugin.getEnchantmentRegistry().getEnchantLevel(chest, overload) : 0;

            AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
            if (attr != null) {
                if (lvl > 0) {
                    double extraHealth = lvl * 4.0; // 2 hearts per level
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

            // Gears (Speed on boots)
            ItemStack boots = player.getInventory().getBoots();
            CustomEnchant gears = plugin.getEnchantmentRegistry().getEnchantment("gears");
            int gearsLvl = gears != null ? plugin.getEnchantmentRegistry().getEnchantLevel(boots, gears) : 0;
            if (gearsLvl > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, gearsLvl - 1, false, false, false));
            }

            // Springs (Jump Boost on boots)
            CustomEnchant springs = plugin.getEnchantmentRegistry().getEnchantment("springs");
            int springsLvl = springs != null ? plugin.getEnchantmentRegistry().getEnchantLevel(boots, springs) : 0;
            if (springsLvl > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, springsLvl - 1, false, false, false));
            }

            // Haste (when holding tool with haste)
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            CustomEnchant haste = plugin.getEnchantmentRegistry().getEnchantment("haste");
            int hasteLvl = haste != null ? plugin.getEnchantmentRegistry().getEnchantLevel(mainHand, haste) : 0;
            if (hasteLvl > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, hasteLvl - 1, false, false, false));
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

            // 1. Bleed
            checkAndApply(weapon, "bleed", lvl -> {
                victim.getWorld().spawnParticle(Particle.DUST, victim.getLocation().add(0, 1, 0), 15, 0.2, 0.4, 0.2, new Particle.DustOptions(Color.RED, 1.5f));
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (victim.isValid() && !victim.isDead()) {
                        victim.damage(lvl * 1.5, attacker);
                    }
                }, 20L);
            });

            // 2. Lifesteal
            checkAndApply(weapon, "lifesteal", lvl -> {
                double heal = lvl * 1.0;
                double maxHealth = attacker.getAttribute(Attribute.MAX_HEALTH).getValue();
                attacker.setHealth(Math.min(maxHealth, attacker.getHealth() + heal));
                attacker.getWorld().spawnParticle(Particle.HEART, attacker.getLocation().add(0, 2, 0), 2);
            });

            // 3. Vampire
            checkAndApply(weapon, "vampire", lvl -> {
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40 * lvl, 0));
            });

            // 4. Cleave
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

            // 5. Rage
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

            // 6. Blind
            checkAndApply(weapon, "blind", lvl -> {
                if (ThreadLocalRandom.current().nextInt(100) < 20 * lvl) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40 * lvl, 0));
                }
            });

            // 7. Paralyze
            checkAndApply(weapon, "paralyze", lvl -> {
                if (ThreadLocalRandom.current().nextInt(100) < 15 * lvl) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40 * lvl, 1));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 40 * lvl, 1));
                }
            });

            // 8. Disarm
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
        }

        // Defender Armor Enchantments
        if (victim instanceof Player defPlayer) {
            final Player finalDefPlayer = defPlayer;
            final double baseDamage = event.getDamage();
            ItemStack chest = defPlayer.getInventory().getChestplate();

            // Cactus
            for (ItemStack piece : defPlayer.getInventory().getArmorContents()) {
                checkAndApply(piece, "cactus", lvl -> {
                    if (attacker != null && ThreadLocalRandom.current().nextInt(100) < 25 * lvl) {
                        attacker.damage(baseDamage * 0.25, finalDefPlayer);
                    }
                });
            }

            // Enlightened
            for (ItemStack piece : defPlayer.getInventory().getArmorContents()) {
                checkAndApply(piece, "enlightened", lvl -> {
                    if (ThreadLocalRandom.current().nextInt(100) < 15 * lvl) {
                        double maxH = finalDefPlayer.getAttribute(Attribute.MAX_HEALTH).getValue();
                        finalDefPlayer.setHealth(Math.min(maxH, finalDefPlayer.getHealth() + (lvl * 2.0)));
                        finalDefPlayer.getWorld().spawnParticle(Particle.HEART, finalDefPlayer.getLocation().add(0, 2, 0), 2);
                    }
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFatalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // 1. Obsidianshield (Fire / Lava immunity)
        if (event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
            ItemStack chest = player.getInventory().getChestplate();
            CustomEnchant ob = plugin.getEnchantmentRegistry().getEnchantment("obsidianshield");
            if (ob != null && plugin.getEnchantmentRegistry().getEnchantLevel(chest, ob) > 0) {
                event.setCancelled(true);
                player.setFireTicks(0);
                return;
            }
        }

        // 2. Phoenix (Fatal save)
        if (event.getFinalDamage() >= player.getHealth()) {
            ItemStack chest = player.getInventory().getChestplate();
            CustomEnchant phoenix = plugin.getEnchantmentRegistry().getEnchantment("phoenix");
            if (phoenix != null && plugin.getEnchantmentRegistry().getEnchantLevel(chest, phoenix) > 0) {
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

        // Inquisitive (EXP Multiplier)
        checkAndApply(weapon, "inquisitive", lvl -> {
            event.setDroppedExp((int) (event.getDroppedExp() * (1.0 + (lvl * 0.5))));
        });

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

        // 1. AutoSmelt
        checkAndApply(tool, "autosmelt", lvl -> {
            Collection<ItemStack> drops = block.getDrops(tool, player);
            List<ItemStack> newDrops = new ArrayList<>();
            boolean smelted = false;

            for (ItemStack drop : drops) {
                ItemStack smeltedItem = switch (drop.getType()) {
                    case RAW_IRON -> new ItemStack(Material.IRON_INGOT, drop.getAmount());
                    case RAW_GOLD -> new ItemStack(Material.GOLD_INGOT, drop.getAmount());
                    case RAW_COPPER -> new ItemStack(Material.COPPER_INGOT, drop.getAmount());
                    case COBBLESTONE -> new ItemStack(Material.STONE, drop.getAmount());
                    default -> drop;
                };
                if (smeltedItem != drop) smelted = true;
                newDrops.add(smeltedItem);
            }

            if (smelted) {
                event.setDropItems(false);
                for (ItemStack is : newDrops) {
                    block.getWorld().dropItemNaturally(block.getLocation(), is);
                }
                block.getWorld().spawnParticle(Particle.FLAME, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.02);
            }
        });

        // 2. Telepathy
        checkAndApply(tool, "telepathy", lvl -> {
            event.setDropItems(false);
            Collection<ItemStack> drops = block.getDrops(tool, player);
            for (ItemStack drop : drops) {
                HashMap<Integer, ItemStack> left = player.getInventory().addItem(drop);
                for (ItemStack rem : left.values()) {
                    block.getWorld().dropItemNaturally(block.getLocation(), rem);
                }
            }
        });
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        CustomEnchant unb = plugin.getEnchantmentRegistry().getEnchantment("unbreakable");
        if (unb != null && plugin.getEnchantmentRegistry().getEnchantLevel(item, unb) > 0) {
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
    }

    private void checkAndApply(ItemStack item, String enchantId, java.util.function.Consumer<Integer> action) {
        if (item == null || item.getType().isAir()) return;
        CustomEnchant enchant = plugin.getEnchantmentRegistry().getEnchantment(enchantId);
        if (enchant == null) return;
        int lvl = plugin.getEnchantmentRegistry().getEnchantLevel(item, enchant);
        if (lvl > 0) {
            action.accept(lvl);
        }
    }
}
