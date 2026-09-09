package com.apexsions.crates.util;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.config.Config;
import com.apexsions.crates.config.Keys;
import com.apexsions.crates.crate.impl.Crate;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.text.night.NightMessage;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrateUtils {

    public static final int REWARD_ITEMS_LIMIT = 27;

    @NotNull
    public static Set<Player> getPlayersForEffects(@NotNull Location location) {
        Set<Player> players = new HashSet<>(Bukkit.getServer().getOnlinePlayers());
        players.removeIf(player -> !isInEffectRange(player, location));

        return players;
    }

    public static boolean isInEffectRange(@NotNull Player player, @NotNull Location location) {
        World world = location.getWorld();
        int distance = Config.CRATE_EFFECTS_VISIBILITY_DISTANCE.get();

        return player.getWorld() == world && player.getLocation().distance(location) <= distance;
    }

    @NotNull
    public static ItemStack removeCrateTags(@NotNull ItemStack itemStack) {
        ItemUtil.editMeta(itemStack, meta -> {
            PDCUtil.remove(meta, Keys.crateId);
            PDCUtil.remove(meta, Keys.keyId);
        });
        return itemStack;
    }

    @NotNull
    public static ItemStack getQuestionStack() {
        return NightItem.asCustomHead("2705fd94a0c431927fb4e639b0fcfb49717e412285a02b439e0112da22b2e2ec").hideAllComponents().getItemStack();
    }

    @NotNull
    public static NightItem getDefaultLinkTool() {
        return NightItem.fromType(Material.BLAZE_ROD)
            .hideAllComponents()
            .setDisplayName(TagWrappers.GOLD.and(TagWrappers.BOLD).wrap("Link Tool"))
            .setLore(Lists.newList(
                TagWrappers.GRAY.wrap("Click a block to link it"),
                TagWrappers.GRAY.wrap("with the crate!")
            ));
    }
    private static final Pattern HEX_PATTERN_1 = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_PATTERN_2 = Pattern.compile("#([A-Fa-f0-9]{6})");

    @NotNull
    public static Component parseComponent(@NotNull String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // 1. If text uses MiniMessage tags (<...>)
        if (text.contains("<") && text.contains(">")) {
            try {
                return MiniMessage.miniMessage()
                        .deserialize(text)
                        .decoration(TextDecoration.ITALIC, false);
            } catch (Exception ignored) {}
        }

        // 2. Convert &#RRGGBB and #RRGGBB to §x§R§R§G§G§B§B
        String processed = text;
        Matcher matcher1 = HEX_PATTERN_1.matcher(processed);
        if (matcher1.find()) {
            StringBuffer sb = new StringBuffer();
            do {
                String hex = matcher1.group(1);
                StringBuilder replacement = new StringBuilder("§x");
                for (char c : hex.toCharArray()) {
                    replacement.append('§').append(c);
                }
                matcher1.appendReplacement(sb, replacement.toString());
            } while (matcher1.find());
            matcher1.appendTail(sb);
            processed = sb.toString();
        }

        // 3. Translate '&' to '§'
        char[] b = processed.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = '§';
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        processed = new String(b);

        return LegacyComponentSerializer.legacySection()
                .deserialize(processed)
                .decoration(TextDecoration.ITALIC, false);
    }

    public static void applyDisplayName(@NotNull ItemMeta meta, @NotNull String name) {
        if (name == null || name.isEmpty()) return;
        meta.displayName(parseComponent(name));
        try {
            ItemUtil.setCustomName(meta, name);
        } catch (Throwable ignored) {}
    }

    public static void applyLore(@NotNull ItemMeta meta, @NotNull List<String> lore) {
        if (lore == null || lore.isEmpty()) return;
        List<Component> components = new ArrayList<>(lore.size());
        for (String line : lore) {
            components.add(parseComponent(line));
        }
        meta.lore(components);
        try {
            ItemUtil.setLore(meta, lore);
        } catch (Throwable ignored) {}
    }

    @NotNull
    public static ItemStack getDefaultItem(@NotNull Crate crate) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemUtil.editMeta(item, meta -> {
            applyDisplayName(meta, crate.getName());
            if (crate.getDescription() != null && !crate.getDescription().isEmpty()) {
                applyLore(meta, crate.getDescription());
            }
        });
        return item;
    }

    @NotNull
    @Deprecated
    public static String createID(@NotNull String name) {
        String id = StringUtil.transformForID(name);
        if (id.isBlank()) id = UUID.randomUUID().toString().substring(0, 8);

        return id;
    }

    @NotNull
    public static String generateRewardID(@NotNull Crate crate, @NotNull ItemStack itemStack) {
        String itemName = Optional.ofNullable(ItemUtil.getDisplayNameSerialized(itemStack))
            .map(NightMessage::stripTags)
            .orElse(BukkitThing.getValue(itemStack.getType()));

        String name = Strings.varStyle(itemName).orElse(UUID.randomUUID().toString());

        int count = 0;
        while (crate.getReward(addCount(name, count)) != null) {
            count++;
        }

        return addCount(name, count);
    }

    public static boolean isValidCommand(@NotNull String command) {
        String firstPart = command.split(" ")[0];

        int index = firstPart.indexOf(':');
        String name = index >= 0 ? firstPart.substring(index + 1) : firstPart;

        return CommandUtil.getCommand(name).isPresent();
    }

    private static String addCount(@NotNull String str, int count) {
        return count <= 0 ? str : str + "_" + count;
    }

    public static boolean isSupportedParticle(@NotNull Particle particle) {
        return particle != Particle.VIBRATION && particle != Particle.DUST_COLOR_TRANSITION && particle != Particle.TRAIL;
    }

    public static boolean isSupportedParticleData(@NotNull UniParticle particle) {
        return particle.getParticle() != null && isSupportedParticleData(particle.getParticle().getDataType());
    }

    public static boolean isSupportedParticleData(@NotNull Class<?> clazz) {
        return clazz != Void.class && clazz != Vibration.class && clazz != Particle.DustTransition.class && clazz != Particle.Trail.class;
    }
}
