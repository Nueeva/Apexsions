package com.apexsions.economy.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Intelligent player name resolver for ApexsionsEconomy.
 */
public final class PlayerResolver {

    private PlayerResolver() {}

    public static boolean isBedrockPrefix(char c) {
        return c == '.' || c == '*' || c == '_';
    }

    @NotNull
    public static String stripBedrockPrefix(@Nullable String name) {
        if (name == null || name.isEmpty()) return "";
        int start = 0;
        while (start < name.length() && isBedrockPrefix(name.charAt(start))) {
            start++;
        }
        return name.substring(start);
    }

    @Nullable
    public static Player resolveOnline(@Nullable String query) {
        if (query == null || query.isBlank()) return null;
        query = query.trim();

        try {
            UUID uuid = UUID.fromString(query);
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) return p;
        } catch (IllegalArgumentException ignored) {}

        String cleanQuery = stripBedrockPrefix(query).toLowerCase(Locale.ROOT);
        String rawQuery = query.toLowerCase(Locale.ROOT);

        Collection<? extends Player> online = Bukkit.getOnlinePlayers();
        if (online.isEmpty()) return null;

        // Tier 1: Exact matches
        for (Player p : online) {
            String rawName = p.getName().toLowerCase(Locale.ROOT);
            String cleanName = stripBedrockPrefix(p.getName()).toLowerCase(Locale.ROOT);
            if (rawName.equals(rawQuery) || cleanName.equals(cleanQuery) || rawName.equals(cleanQuery) || cleanName.equals(rawQuery)) {
                return p;
            }
        }

        // Tier 2: Prefix match (startsWith)
        List<Player> prefixMatches = new ArrayList<>();
        for (Player p : online) {
            String rawName = p.getName().toLowerCase(Locale.ROOT);
            String cleanName = stripBedrockPrefix(p.getName()).toLowerCase(Locale.ROOT);
            if (rawName.startsWith(rawQuery) || cleanName.startsWith(cleanQuery) || cleanName.startsWith(rawQuery)) {
                prefixMatches.add(p);
            }
        }
        if (prefixMatches.size() == 1) {
            return prefixMatches.get(0);
        } else if (prefixMatches.size() > 1) {
            prefixMatches.sort(Comparator.comparingInt(a -> a.getName().length()));
            return prefixMatches.get(0);
        }

        // Tier 3: Substring match (contains)
        List<Player> containsMatches = new ArrayList<>();
        for (Player p : online) {
            String rawName = p.getName().toLowerCase(Locale.ROOT);
            String cleanName = stripBedrockPrefix(p.getName()).toLowerCase(Locale.ROOT);
            if (rawName.contains(rawQuery) || cleanName.contains(cleanQuery)) {
                containsMatches.add(p);
            }
        }
        if (containsMatches.size() == 1) {
            return containsMatches.get(0);
        } else if (containsMatches.size() > 1) {
            containsMatches.sort(Comparator.comparingInt(a -> a.getName().length()));
            return containsMatches.get(0);
        }

        return null;
    }

    @Nullable
    public static OfflinePlayer resolveOffline(@Nullable String query, boolean allowUnseen) {
        if (query == null || query.isBlank()) return null;
        query = query.trim();

        Player online = resolveOnline(query);
        if (online != null) return online;

        try {
            UUID uuid = UUID.fromString(query);
            return Bukkit.getOfflinePlayer(uuid);
        } catch (IllegalArgumentException ignored) {}

        String cleanQuery = stripBedrockPrefix(query).toLowerCase(Locale.ROOT);
        String rawQuery = query.toLowerCase(Locale.ROOT);

        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        if (offlinePlayers != null && offlinePlayers.length > 0) {
            for (OfflinePlayer op : offlinePlayers) {
                if (op.getName() == null) continue;
                String rawName = op.getName().toLowerCase(Locale.ROOT);
                String cleanName = stripBedrockPrefix(op.getName()).toLowerCase(Locale.ROOT);
                if (rawName.equals(rawQuery) || cleanName.equals(cleanQuery) || rawName.equals(cleanQuery) || cleanName.equals(rawQuery)) {
                    return op;
                }
            }

            List<OfflinePlayer> prefixMatches = new ArrayList<>();
            for (OfflinePlayer op : offlinePlayers) {
                if (op.getName() == null) continue;
                String rawName = op.getName().toLowerCase(Locale.ROOT);
                String cleanName = stripBedrockPrefix(op.getName()).toLowerCase(Locale.ROOT);
                if (rawName.startsWith(rawQuery) || cleanName.startsWith(cleanQuery) || cleanName.startsWith(rawQuery)) {
                    prefixMatches.add(op);
                }
            }
            if (prefixMatches.size() == 1) {
                return prefixMatches.get(0);
            } else if (prefixMatches.size() > 1) {
                prefixMatches.sort(Comparator.comparingInt(a -> a.getName() != null ? a.getName().length() : 999));
                return prefixMatches.get(0);
            }

            List<OfflinePlayer> containsMatches = new ArrayList<>();
            for (OfflinePlayer op : offlinePlayers) {
                if (op.getName() == null) continue;
                String rawName = op.getName().toLowerCase(Locale.ROOT);
                String cleanName = stripBedrockPrefix(op.getName()).toLowerCase(Locale.ROOT);
                if (rawName.contains(rawQuery) || cleanName.contains(cleanQuery)) {
                    containsMatches.add(op);
                }
            }
            if (containsMatches.size() == 1) {
                return containsMatches.get(0);
            } else if (containsMatches.size() > 1) {
                containsMatches.sort(Comparator.comparingInt(a -> a.getName() != null ? a.getName().length() : 999));
                return containsMatches.get(0);
            }
        }

        if (allowUnseen) {
            return Bukkit.getOfflinePlayer(query);
        }
        return null;
    }

    @Nullable
    public static OfflinePlayer resolveOffline(@Nullable String query) {
        return resolveOffline(query, true);
    }

    @NotNull
    public static List<String> completePlayerNames(@Nullable CommandSender viewer, @NotNull String query) {
        String rawQuery = query.toLowerCase(Locale.ROOT);
        String cleanQuery = stripBedrockPrefix(query).toLowerCase(Locale.ROOT);

        boolean canSeeVanish = viewer == null || !(viewer instanceof Player) || viewer.hasPermission("apexsions.vanish.see");
        Player viewingPlayer = viewer instanceof Player p ? p : null;

        Set<String> results = new LinkedHashSet<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (viewingPlayer != null) {
                if (!viewingPlayer.canSee(p) || p.hasMetadata("vanished") || p.hasMetadata("vanish")) {
                    if (!canSeeVanish) continue;
                }
            }

            String fullName = p.getName();
            String cleanName = stripBedrockPrefix(fullName);

            if (fullName.toLowerCase(Locale.ROOT).startsWith(rawQuery)) {
                results.add(fullName);
            } else if (!cleanName.equals(fullName) && cleanName.toLowerCase(Locale.ROOT).startsWith(cleanQuery)) {
                results.add(fullName);
                results.add(cleanName);
            } else if (cleanName.toLowerCase(Locale.ROOT).startsWith(rawQuery)) {
                results.add(fullName);
            }
        }

        return new ArrayList<>(results);
    }

    @NotNull
    public static List<String> completePlayerNames(@NotNull String query) {
        return completePlayerNames(null, query);
    }
}
