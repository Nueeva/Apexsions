package com.apexsions.crates.command;

import com.apexsions.crates.CratesPlugin;
import com.apexsions.crates.config.Perms;
import com.apexsions.crates.crate.impl.Crate;
import com.apexsions.crates.key.CrateKey;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.commands.command.HubCommand;
import su.nightexpress.nightcore.commands.tree.HubNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom HubCommand implementation providing native auto-filtering tab completion
 * for Minecraft Java Edition Brigadier suggestions protocol.
 */
public class ApexsionsCratesCommand extends HubCommand {

    private final CratesPlugin plugin;

    public ApexsionsCratesCommand(@NotNull CratesPlugin plugin, @NotNull HubNode node, @NotNull List<String> aliases) {
        super(plugin, node, aliases);
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0) {
            return Collections.emptyList();
        }

        List<String> candidates = new ArrayList<>();
        String current = args[args.length - 1];

        // 1. Level 1: Subcommands auto-filtering
        if (args.length == 1) {
            if (sender.hasPermission(Perms.COMMAND_OPEN)) {
                candidates.add("open");
                candidates.add("preview");
                candidates.add("shop");
            }
            if (sender.hasPermission(Perms.COMMAND_EDITOR)) {
                candidates.add("editor");
            }
            if (sender.hasPermission(Perms.COMMAND_KEY)) {
                candidates.add("key");
            }
            if (sender.hasPermission(Perms.COMMAND_GIVE)) {
                candidates.add("give");
            }
            if (sender.hasPermission(Perms.COMMAND_OPEN_FOR)) {
                candidates.add("openfor");
            }
            if (sender.hasPermission(Perms.COMMAND_DROP)) {
                candidates.add("drop");
            }
            if (sender.hasPermission(Perms.COMMAND_RESETCOOLDOWN)) {
                candidates.add("resetcooldown");
            }
            if (sender.hasPermission(Perms.COMMAND_RELOAD)) {
                candidates.add("reload");
            }

            return filterAndSort(current, candidates);
        }

        String sub = args[0].toLowerCase();

        // 2. Subcommand: open <crate>
        if (sub.equals("open")) {
            if (!sender.hasPermission(Perms.COMMAND_OPEN)) return Collections.emptyList();
            if (args.length == 2) {
                return filterAndSort(current, getCrateIds());
            }
            return Collections.emptyList();
        }

        // 3. Subcommand: preview <crate> [player]
        if (sub.equals("preview")) {
            if (!sender.hasPermission(Perms.COMMAND_PREVIEW)) return Collections.emptyList();
            if (args.length == 2) {
                return filterAndSort(current, getCrateIds());
            }
            if (args.length == 3 && sender.hasPermission(Perms.COMMAND_PREVIEW_OTHERS)) {
                return filterAndSort(current, getOnlinePlayerNames());
            }
            return Collections.emptyList();
        }

        // 4. Subcommand: give <player> <crate> [amount]
        if (sub.equals("give")) {
            if (!sender.hasPermission(Perms.COMMAND_GIVE)) return Collections.emptyList();
            if (args.length == 2) {
                return filterAndSort(current, getOnlinePlayerNames());
            }
            if (args.length == 3) {
                return filterAndSort(current, getCrateIds());
            }
            if (args.length == 4) {
                return filterAndSort(current, List.of("1", "5", "10", "32", "64"));
            }
            return Collections.emptyList();
        }

        // 5. Subcommand: openfor <player> <crate>
        if (sub.equals("openfor")) {
            if (!sender.hasPermission(Perms.COMMAND_OPEN_FOR)) return Collections.emptyList();
            if (args.length == 2) {
                return filterAndSort(current, getOnlinePlayerNames());
            }
            if (args.length == 3) {
                return filterAndSort(current, getCrateIds());
            }
            return Collections.emptyList();
        }

        // 6. Subcommand: resetcooldown <player> <crate>
        if (sub.equals("resetcooldown")) {
            if (!sender.hasPermission(Perms.COMMAND_RESETCOOLDOWN)) return Collections.emptyList();
            if (args.length == 2) {
                return filterAndSort(current, getOnlinePlayerNames());
            }
            if (args.length == 3) {
                return filterAndSort(current, getCrateIds());
            }
            return Collections.emptyList();
        }

        // 7. Subcommand: drop <crate> <x> <y> <z> <world>
        if (sub.equals("drop")) {
            if (!sender.hasPermission(Perms.COMMAND_DROP)) return Collections.emptyList();
            if (args.length == 2) {
                return filterAndSort(current, getCrateIds());
            }
            if (args.length == 3) {
                return getCoordSuggestion(sender, 0, current);
            }
            if (args.length == 4) {
                return getCoordSuggestion(sender, 1, current);
            }
            if (args.length == 5) {
                return getCoordSuggestion(sender, 2, current);
            }
            if (args.length == 6) {
                return filterAndSort(current, getWorldNames());
            }
            return Collections.emptyList();
        }

        // 8. Subcommand: key ...
        if (sub.equals("key")) {
            if (!sender.hasPermission(Perms.COMMAND_KEY)) return Collections.emptyList();

            if (args.length == 2) {
                if (sender.hasPermission(Perms.COMMAND_KEY_GIVE)) {
                    candidates.add("give");
                    candidates.add("giveall");
                }
                if (sender.hasPermission(Perms.COMMAND_KEY_TAKE)) {
                    candidates.add("take");
                }
                if (sender.hasPermission(Perms.COMMAND_KEY_SET)) {
                    candidates.add("set");
                }
                if (sender.hasPermission(Perms.COMMAND_KEY_INSPECT)) {
                    candidates.add("inspect");
                }
                if (sender.hasPermission(Perms.COMMAND_DROP_KEY)) {
                    candidates.add("drop");
                }
                return filterAndSort(current, candidates);
            }

            String keyAction = args[1].toLowerCase();

            // key give/take/set <player> <key> [amount]
            if (keyAction.equals("give") || keyAction.equals("take") || keyAction.equals("set")) {
                if (args.length == 3) {
                    return filterAndSort(current, getOnlinePlayerNames());
                }
                if (args.length == 4) {
                    return filterAndSort(current, getKeyIds());
                }
                if (args.length == 5) {
                    return filterAndSort(current, List.of("1", "5", "10", "32", "64"));
                }
                return Collections.emptyList();
            }

            // key giveall <key> [amount]
            if (keyAction.equals("giveall")) {
                if (args.length == 3) {
                    return filterAndSort(current, getKeyIds());
                }
                if (args.length == 4) {
                    return filterAndSort(current, List.of("1", "5", "10", "32", "64"));
                }
                return Collections.emptyList();
            }

            // key inspect [player]
            if (keyAction.equals("inspect")) {
                if (args.length == 3 && sender.hasPermission(Perms.COMMAND_KEY_INSPECT_OTHERS)) {
                    return filterAndSort(current, getOnlinePlayerNames());
                }
                return Collections.emptyList();
            }

            // key drop <key> <x> <y> <z> <world>
            if (keyAction.equals("drop")) {
                if (args.length == 3) {
                    return filterAndSort(current, getKeyIds());
                }
                if (args.length == 4) {
                    return getCoordSuggestion(sender, 0, current);
                }
                if (args.length == 5) {
                    return getCoordSuggestion(sender, 1, current);
                }
                if (args.length == 6) {
                    return getCoordSuggestion(sender, 2, current);
                }
                if (args.length == 7) {
                    return filterAndSort(current, getWorldNames());
                }
                return Collections.emptyList();
            }
        }

        // Fallback to super for any unhandled node
        try {
            List<String> def = super.tabComplete(sender, alias, args);
            if (def != null && !def.isEmpty()) {
                return filterAndSort(current, def);
            }
        } catch (Exception ignored) {}

        return Collections.emptyList();
    }

    private List<String> getCrateIds() {
        if (plugin.getCrateManager() == null) return Collections.emptyList();
        List<String> list = new ArrayList<>();
        for (Crate crate : plugin.getCrateManager().getCrates()) {
            list.add(crate.getId());
        }
        return list;
    }

    private List<String> getKeyIds() {
        if (plugin.getKeyManager() == null) return Collections.emptyList();
        List<String> list = new ArrayList<>();
        for (CrateKey key : plugin.getKeyManager().getKeys()) {
            list.add(key.getId());
        }
        return list;
    }

    private List<String> getOnlinePlayerNames() {
        List<String> list = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            list.add(p.getName());
        }
        return list;
    }

    private List<String> getWorldNames() {
        List<String> list = new ArrayList<>();
        for (World w : Bukkit.getWorlds()) {
            list.add(w.getName());
        }
        return list;
    }

    private List<String> getCoordSuggestion(CommandSender sender, int axis, String current) {
        if (sender instanceof Player p) {
            int val = switch (axis) {
                case 0 -> p.getLocation().getBlockX();
                case 1 -> p.getLocation().getBlockY();
                case 2 -> p.getLocation().getBlockZ();
                default -> 0;
            };
            return filterAndSort(current, List.of(String.valueOf(val)));
        }
        return filterAndSort(current, List.of("0"));
    }

    @NotNull
    private List<String> filterAndSort(@NotNull String token, @NotNull List<String> candidates) {
        List<String> result = new ArrayList<>();
        StringUtil.copyPartialMatches(token, candidates, result);
        Collections.sort(result);
        return result;
    }
}
