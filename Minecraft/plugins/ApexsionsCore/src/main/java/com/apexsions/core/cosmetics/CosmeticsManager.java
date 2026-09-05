package com.apexsions.core.cosmetics;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.cosmetics.condition.*;
import com.apexsions.core.player.PlayerData;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * High-performance Particle Cosmetics Engine.
 * Synchronously and safely renders Head Auras, Footstep Trails, and Kill Effects on active players with zero TPS overhead.
 */
public class CosmeticsManager implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final Map<String, CosmeticItem> cosmetics = new LinkedHashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();
    private BukkitTask particleTask;
    private double currentAngle = 0.0;
    private long tickCounter = 0;

    public CosmeticsManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        registerDefaultCosmetics();
    }

    private void registerDefaultCosmetics() {
        // ════════════════ HEAD AURAS (Interval: 3 ticks) ════════════════
        registerCosmetic(new CosmeticItem("zenith_halo", CosmeticType.AURA,
                "<gradient:#ffe900:#f39c12><bold>👑 Zenithar Sun Halo</bold></gradient>",
                "Cincin cahaya surya keemasan berputar di atas kepala.",
                Material.GLOWSTONE, Particle.END_ROD, new KingdomCondition("ZENITHAR", 10), 3));

        registerCosmetic(new CosmeticItem("sol_flame", CosmeticType.AURA,
                "<gradient:#ff4d4d:#c0392b><bold>🔥 Solterra Flame Crown</bold></gradient>",
                "Mahkota api abadi yang menyala gagah perkasa.",
                Material.BLAZE_POWDER, Particle.FLAME, new KingdomCondition("SOLTERRA", 10), 3));

        registerCosmetic(new CosmeticItem("sylva_blossom", CosmeticType.AURA,
                "<gradient:#87ceeb:#2ecc71><bold>🌸 Sylvamoor Petal Breeze</bold></gradient>",
                "Pusaran kelopak bunga sakura dan angin segar alam.",
                Material.CHERRY_LEAVES, Particle.CHERRY_LEAVES, new KingdomCondition("SYLVAMOOR", 10), 3));

        registerCosmetic(new CosmeticItem("void_orbit", CosmeticType.AURA,
                "<gradient:#9b59b6:#34495e><bold>🌌 Void Star Orbit</bold></gradient>",
                "Partikel bintang kehampaan berputar di sekitar kepalamu.",
                Material.ENDER_EYE, Particle.PORTAL, new LevelCondition(35), 3));

        registerCosmetic(new CosmeticItem("celestial_halo", CosmeticType.AURA,
                "<gradient:#00dfd8:#007adf><bold>⚡ Celestial Spark Halo</bold></gradient>",
                "Lingkaran petir kosmik surgawi berenergi tinggi.",
                Material.BEACON, Particle.ELECTRIC_SPARK, new LevelCondition(60), 3));

        // ════════════════ FOOTSTEP TRAILS (Interval: 2 ticks) ════════════════
        registerCosmetic(new CosmeticItem("flame_step", CosmeticType.TRAIL,
                "<gradient:#ff9f43:#ee5253><bold>🔥 Jejak Api Berkobar</bold></gradient>",
                "Meninggalkan jejak bara api setiap kali melangkah.",
                Material.FIRE_CHARGE, Particle.FLAME, new LevelCondition(15), 2));

        registerCosmetic(new CosmeticItem("cherry_step", CosmeticType.TRAIL,
                "<gradient:#ff9ff3:#f368e0><bold>🌸 Jejak Kelopak Sakura</bold></gradient>",
                "Guguran daun bunga sakura yang elok di setiap jejak langkah.",
                Material.CHERRY_SAPLING, Particle.CHERRY_LEAVES, new LevelCondition(20), 2));

        registerCosmetic(new CosmeticItem("rune_step", CosmeticType.TRAIL,
                "<gradient:#54a0ff:#5f27cd><bold>✨ Jejak Runic Mystic</bold></gradient>",
                "Huruf rune sihir kuno terpancar di lantai saat berlari.",
                Material.ENCHANTING_TABLE, Particle.ENCHANT, new LevelCondition(30), 2));

        registerCosmetic(new CosmeticItem("spark_step", CosmeticType.TRAIL,
                "<gradient:#feca57:#ff9f43><bold>⚡ Jejak Listrik Kilat</bold></gradient>",
                "Percikan kilat statis berkecepatan tinggi di kakimu.",
                Material.LIGHTNING_ROD, Particle.ELECTRIC_SPARK, new LevelCondition(45), 2));

        registerCosmetic(new CosmeticItem("gold_step", CosmeticType.TRAIL,
                "<gradient:#ffeaa7:#fdcb6e><bold>💰 Jejak Kilau Emas VIP</bold></gradient>",
                "Partikel debu emas bertaburan mengikuti pergerakanmu.",
                Material.GOLD_BLOCK, Particle.WAX_ON, new PermissionCondition("apexsions.cosmetics.vip", "Rank VIP / Donatur"), 2));

        // ════════════════ KILL EFFECTS ════════════════
        registerCosmetic(new CosmeticItem("lightning_kill", CosmeticType.KILL_EFFECT,
                "<gradient:#00d2d3:#54a0ff><bold>⚡ Sambaran Petir Kosmetik</bold></gradient>",
                "Petir kosmik menyambar lokasi musuh yang kamu kalahkan.",
                Material.NETHERITE_SWORD, Particle.ELECTRIC_SPARK, new LevelCondition(25), 1));

        registerCosmetic(new CosmeticItem("confetti_kill", CosmeticType.KILL_EFFECT,
                "<gradient:#ff9ff3:#feca57><bold>🎉 Ledakan Kembang Api Emas</bold></gradient>",
                "Ledakan konfeti kembang api emas meriah saat eliminasi musuh.",
                Material.FIREWORK_ROCKET, Particle.TOTEM_OF_UNDYING, new LevelCondition(50), 1));

        registerCosmetic(new CosmeticItem("blood_kill", CosmeticType.KILL_EFFECT,
                "<gradient:#ee5253:#c0392b><bold>🩸 Kabut Darah Ksatria</bold></gradient>",
                "Kabut darah merah pekat meletup saat musuh tersungkur.",
                Material.REDSTONE, Particle.CRIMSON_SPORE, new LevelCondition(70), 1));
    }

    public void registerCosmetic(CosmeticItem item) {
        if (item != null) {
            cosmetics.put(item.getId().toLowerCase(Locale.ROOT), item);
        }
    }

    public Optional<CosmeticItem> getCosmetic(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(cosmetics.get(id.toLowerCase(Locale.ROOT)));
    }

    public List<CosmeticItem> getCosmeticsByType(CosmeticType type) {
        List<CosmeticItem> list = new ArrayList<>();
        for (CosmeticItem item : cosmetics.values()) {
            if (item.getType() == type) {
                list.add(item);
            }
        }
        return list;
    }

    public void start() {
        stop();
        // Sync repeating task on Main Server Thread (every 2 ticks = 100ms)
        particleTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickParticles, 10L, 2L);
    }

    public void stop() {
        if (particleTask != null && !particleTask.isCancelled()) {
            particleTask.cancel();
            particleTask = null;
        }
    }

    private void tickParticles() {
        tickCounter++;
        currentAngle += 0.25;
        if (currentAngle > Math.PI * 2) {
            currentAngle = 0.0;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isValid() || player.isDead()) continue;

            PlayerData data = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
            if (data == null) continue;

            // 1. Tick Head Aura (Interval check)
            if (data.getActiveAura() != null) {
                CosmeticItem aura = cosmetics.get(data.getActiveAura().toLowerCase(Locale.ROOT));
                if (aura != null && aura.getType() == CosmeticType.AURA) {
                    if (tickCounter % aura.getIntervalTicks() == 0) {
                        spawnAuraParticles(player, aura.getParticle());
                    }
                }
            }

            // 2. Tick Footstep Trail (Interval check & motion check)
            if (data.getActiveTrail() != null) {
                CosmeticItem trail = cosmetics.get(data.getActiveTrail().toLowerCase(Locale.ROOT));
                if (trail != null && trail.getType() == CosmeticType.TRAIL) {
                    if (tickCounter % trail.getIntervalTicks() == 0) {
                        if (player.getVelocity().lengthSquared() > 0.001 || player.isSprinting()) {
                            player.getWorld().spawnParticle(trail.getParticle(), player.getLocation().add(0, 0.1, 0), 2, 0.15, 0.05, 0.15, 0.02);
                        }
                    }
                }
            }
        }
    }

    private void spawnAuraParticles(Player player, Particle particle) {
        Location headLoc = player.getLocation().add(0, 2.15, 0);
        double radius = 0.45;

        // Dual-orbit geometry
        double x1 = Math.cos(currentAngle) * radius;
        double z1 = Math.sin(currentAngle) * radius;
        double x2 = Math.cos(currentAngle + Math.PI) * radius;
        double z2 = Math.sin(currentAngle + Math.PI) * radius;

        player.getWorld().spawnParticle(particle, headLoc.clone().add(x1, 0, z1), 1, 0, 0, 0, 0);
        player.getWorld().spawnParticle(particle, headLoc.clone().add(x2, 0, z2), 1, 0, 0, 0, 0);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;

        PlayerData data = plugin.getPlayerDataService().getCached(killer.getUniqueId()).orElse(null);
        if (data == null || data.getActiveKillEffect() == null) return;

        CosmeticItem killFx = cosmetics.get(data.getActiveKillEffect().toLowerCase(Locale.ROOT));
        if (killFx == null || killFx.getType() != CosmeticType.KILL_EFFECT) return;

        Location deathLoc = victim.getLocation();
        switch (killFx.getId()) {
            case "lightning_kill" -> {
                deathLoc.getWorld().strikeLightningEffect(deathLoc);
                deathLoc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, deathLoc.add(0, 1, 0), 25, 0.5, 0.5, 0.5, 0.1);
            }
            case "confetti_kill" -> {
                deathLoc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, deathLoc.add(0, 1, 0), 40, 0.6, 0.8, 0.6, 0.2);
                deathLoc.getWorld().spawnParticle(Particle.FIREWORK, deathLoc, 20, 0.4, 0.4, 0.4, 0.05);
                killer.playSound(killer.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, 1.2f);
            }
            case "blood_kill" -> {
                deathLoc.getWorld().spawnParticle(Particle.CRIMSON_SPORE, deathLoc.add(0, 1, 0), 50, 0.5, 0.7, 0.5, 0.1);
                killer.playSound(killer.getLocation(), Sound.ENTITY_WITHER_HURT, 0.7f, 1.4f);
            }
            default -> deathLoc.getWorld().spawnParticle(killFx.getParticle(), deathLoc.add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
        }
    }

    public boolean isCosmeticUnlocked(Player player, CosmeticItem item) {
        if (player == null || item == null) return false;
        PlayerData data = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
        return item.isUnlocked(player, data, plugin);
    }

    public void setCosmetic(Player player, CosmeticItem item) {
        PlayerData data = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
        if (data == null || item == null) return;

        switch (item.getType()) {
            case AURA -> data.setActiveAura(item.getId());
            case TRAIL -> data.setActiveTrail(item.getId());
            case KILL_EFFECT -> data.setActiveKillEffect(item.getId());
        }
    }

    public void clearCosmetic(Player player, CosmeticType type) {
        PlayerData data = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
        if (data == null) return;

        switch (type) {
            case AURA -> data.setActiveAura(null);
            case TRAIL -> data.setActiveTrail(null);
            case KILL_EFFECT -> data.setActiveKillEffect(null);
        }
    }
}
