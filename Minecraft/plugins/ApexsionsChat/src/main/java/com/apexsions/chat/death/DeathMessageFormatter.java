package com.apexsions.chat.death;

import com.apexsions.chat.ApexsionsChatPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DeathMessageFormatter {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public DeathMessageFormatter(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    public Component format(PlayerDeathEvent event) {
        Player victim = event.getPlayer();
        FileConfiguration config = plugin.getConfigManager().getMainConfig();

        // 1. Resolve Victim Info
        Component victimRank = resolveRank(victim);
        Component victimName = Component.text(victim.getName(), NamedTextColor.WHITE).decorate(TextDecoration.BOLD);
        String victimKingdom = resolveKingdom(victim);

        // 2. Resolve Damage Cause & Killer
        EntityDamageEvent lastDamage = victim.getLastDamageCause();
        EntityDamageEvent.DamageCause cause = lastDamage != null ? lastDamage.getCause() : EntityDamageEvent.DamageCause.CUSTOM;

        Entity killerEntity = null;
        if (lastDamage instanceof EntityDamageByEntityEvent edbe) {
            Entity damager = edbe.getDamager();
            if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
                killerEntity = shooter;
            } else {
                killerEntity = damager;
            }
        }
        if (killerEntity == null && victim.getKiller() != null) {
            killerEntity = victim.getKiller();
        }

        // 3. Match template
        String templateKey = "generic";
        Component killerRank = Component.empty();
        Component killerName = Component.empty();
        Component weaponComponent = Component.empty();
        boolean hasWeapon = false;

        if (killerEntity instanceof Player killerPlayer) {
            killerRank = resolveRank(killerPlayer);
            killerName = Component.text(killerPlayer.getName(), NamedTextColor.RED).decorate(TextDecoration.BOLD);

            ItemStack weapon = killerPlayer.getInventory().getItemInMainHand();
            if (isValidWeapon(weapon)) {
                hasWeapon = true;
                weaponComponent = buildWeaponComponent(weapon);
                templateKey = "pvp";
            } else {
                templateKey = "pvp-bare-hands";
            }
        } else if (killerEntity instanceof LivingEntity killerMob) {
            if (killerMob.customName() != null) {
                killerName = killerMob.customName();
            } else {
                String typeName = killerMob.getType().name().replace("_", " ").toLowerCase();
                killerName = Component.text(capitalize(typeName), NamedTextColor.RED);
            }

            ItemStack weapon = killerMob.getEquipment() != null ? killerMob.getEquipment().getItemInMainHand() : null;
            if (isValidWeapon(weapon)) {
                hasWeapon = true;
                weaponComponent = buildWeaponComponent(weapon);
                templateKey = "mob";
            } else {
                templateKey = "mob-bare-hands";
            }
        } else if (cause == EntityDamageEvent.DamageCause.FALL) {
            templateKey = "fall";
        } else if (cause == EntityDamageEvent.DamageCause.LAVA) {
            templateKey = "lava";
        } else if (cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.CAMPFIRE) {
            templateKey = "fire";
        } else if (cause == EntityDamageEvent.DamageCause.DROWNING) {
            templateKey = "drown";
        } else if (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            templateKey = "explosion";
        } else if (cause == EntityDamageEvent.DamageCause.VOID) {
            templateKey = "void";
        } else if (cause == EntityDamageEvent.DamageCause.SUICIDE || cause == EntityDamageEvent.DamageCause.KILL) {
            templateKey = "suicide";
        } else if (cause == EntityDamageEvent.DamageCause.MAGIC || cause == EntityDamageEvent.DamageCause.POISON) {
            templateKey = "magic";
        } else if (cause == EntityDamageEvent.DamageCause.WITHER) {
            templateKey = "wither";
        } else if (cause == EntityDamageEvent.DamageCause.STARVATION) {
            templateKey = "starvation";
        } else if (cause == EntityDamageEvent.DamageCause.LIGHTNING) {
            templateKey = "lightning";
        } else if (cause == EntityDamageEvent.DamageCause.FLY_INTO_WALL || cause == EntityDamageEvent.DamageCause.SUFFOCATION || cause == EntityDamageEvent.DamageCause.CRAMMING) {
            templateKey = "suffocation";
        } else if (cause == EntityDamageEvent.DamageCause.SONIC_BOOM) {
            templateKey = "sonic-boom";
        }

        String rawTemplate = config.getString(
                "death-messages." + templateKey,
                config.getString("death-messages.generic",
                        "<dark_gray>[</dark_gray><red><bold>☠</bold></red><dark_gray>]</dark_gray> <victim_rank> <victim> <gray>telah gugur ke alam baka</gray>")
        );

        return miniMessage.deserialize(
                rawTemplate,
                Placeholder.component("victim_rank", victimRank),
                Placeholder.component("victim", victimName),
                Placeholder.unparsed("victim_kingdom", victimKingdom),
                Placeholder.component("killer_rank", killerRank),
                Placeholder.component("killer", killerName),
                Placeholder.component("weapon", weaponComponent)
        );
    }

    private boolean isValidWeapon(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }

    public Component buildWeaponComponent(ItemStack item) {
        if (!isValidWeapon(item)) {
            return Component.empty();
        }

        Component nameComponent;
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            nameComponent = item.getItemMeta().displayName();
        } else {
            String typeName = item.getType().name().replace("_", " ").toLowerCase();
            nameComponent = Component.text(capitalize(typeName), NamedTextColor.GOLD);
        }

        Component bracketed = Component.text("[", NamedTextColor.GRAY)
                .append(nameComponent)
                .append(Component.text("]", NamedTextColor.GRAY));

        try {
            return bracketed.hoverEvent(item.asHoverEvent());
        } catch (Throwable t) {
            return bracketed;
        }
    }

    private Component resolveRank(Player player) {
        String rankStr = "<gray>[Wanderer]</gray>";
        if (plugin.getApexsionsCoreHook() != null && plugin.getApexsionsCoreHook().isAvailable()) {
            var prof = plugin.getApexsionsCoreHook().getPlayerChatProfile(player.getUniqueId());
            if (prof != null && prof.rank() != null) {
                rankStr = prof.rank();
            }
        } else if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isAvailable()) {
            rankStr = plugin.getLuckPermsHook().getPlayerRank(player);
        }
        return miniMessage.deserialize(rankStr);
    }

    private String resolveKingdom(Player player) {
        if (plugin.getApexsionsCoreHook() != null && plugin.getApexsionsCoreHook().isAvailable()) {
            var prof = plugin.getApexsionsCoreHook().getPlayerChatProfile(player.getUniqueId());
            if (prof != null && prof.kingdomDisplayName() != null) {
                return prof.kingdomDisplayName();
            }
        }
        return "Belum Memilih";
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return Arrays.stream(text.split(" "))
                .filter(w -> !w.isEmpty())
                .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
