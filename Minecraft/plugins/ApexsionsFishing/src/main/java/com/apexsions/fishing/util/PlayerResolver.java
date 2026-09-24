package com.apexsions.fishing.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Universal player resolver for ApexsionsFishing.
 * Supports partial matching, Bedrock Geyser prefixes ('.'), and safe offline lookups.
 */
public final class PlayerResolver {

    private PlayerResolver() {}

    @Nullable
    public static Player resolveOnline(@Nullable String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        String raw = query.trim();
        String stripped = stripBedrockPrefix(raw).toLowerCase(Locale.ROOT);
        String lowerRaw = raw.toLowerCase(Locale.ROOT);

        Player exactPlayer = Bukkit.getPlayerExact(raw);
        if (exactPlayer != null) {
            return exactPlayer;
        }

        Player bestPrefix = null;
        Player bestContains = null;

        for (Player online : Bukkit.getOnlinePlayers()) {
            String pName = online.getName();
            String pLower = pName.toLowerCase(Locale.ROOT);
            String pStripped = stripBedrockPrefix(pName).toLowerCase(Locale.ROOT);

            if (pLower.equals(lowerRaw) || pStripped.equals(stripped)) {
                return online;
            }

            if (bestPrefix == null) {
                if (pLower.startsWith(lowerRaw) || pStripped.startsWith(stripped) || pStripped.startsWith(lowerRaw)) {
                    bestPrefix = online;
                }
            }

            if (bestContains == null) {
                if (pLower.contains(lowerRaw) || pStripped.contains(stripped)) {
                    bestContains = online;
                }
            }
        }

        if (bestPrefix != null) {
            return bestPrefix;
        }
        return bestContains;
    }

    @Nullable
    public static OfflinePlayer resolveOffline(@Nullable String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        Player online = resolveOnline(query);
        if (online != null) {
            return online;
        }

        String raw = query.trim();
        String stripped = stripBedrockPrefix(raw).toLowerCase(Locale.ROOT);
        String lowerRaw = raw.toLowerCase(Locale.ROOT);

        OfflinePlayer bestPrefix = null;
        OfflinePlayer bestContains = null;

        for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
            String opName = op.getName();
            if (opName == null) continue;

            String opLower = opName.toLowerCase(Locale.ROOT);
            String opStripped = stripBedrockPrefix(opName).toLowerCase(Locale.ROOT);

            if (opLower.equals(lowerRaw) || opStripped.equals(stripped)) {
                return op;
            }

            if (bestPrefix == null) {
                if (opLower.startsWith(lowerRaw) || opStripped.startsWith(stripped) || opStripped.startsWith(lowerRaw)) {
                    bestPrefix = op;
                }
            }

            if (bestContains == null) {
                if (opLower.contains(lowerRaw) || opStripped.contains(stripped)) {
                    bestContains = op;
                }
            }
        }

        if (bestPrefix != null) {
            return bestPrefix;
        }
        if (bestContains != null) {
            return bestContains;
        }

        return null;
    }

    @NotNull
    public static List<String> completePlayerNames(@NotNull CommandSender sender, @Nullable String currentArg) {
        List<String> result = new ArrayList<>();
        String query = (currentArg == null ? "" : currentArg.trim().toLowerCase(Locale.ROOT));
        String strippedQuery = stripBedrockPrefix(query);

        Player senderPlayer = (sender instanceof Player p) ? p : null;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (senderPlayer != null && !senderPlayer.canSee(p)) {
                continue;
            }

            String name = p.getName();
            String lowerName = name.toLowerCase(Locale.ROOT);
            String strippedName = stripBedrockPrefix(name).toLowerCase(Locale.ROOT);

            if (query.isEmpty() || lowerName.startsWith(query) || strippedName.startsWith(strippedQuery) || strippedName.startsWith(query)) {
                result.add(name);
                if (name.startsWith(".") && (query.isEmpty() || strippedName.startsWith(strippedQuery) || strippedName.startsWith(query))) {
                    result.add(name.substring(1));
                }
            }
        }

        return result;
    }

    @NotNull
    public static String stripBedrockPrefix(@NotNull String name) {
        if (name.startsWith(".")) {
            return name.substring(1);
        }
        return name;
    }
}
