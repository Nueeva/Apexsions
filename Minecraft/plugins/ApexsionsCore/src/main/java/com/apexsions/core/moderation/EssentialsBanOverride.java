package com.apexsions.core.moderation;

import com.apexsions.core.ApexsionsCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Ensures EssentialsX moderation commands (ban, tempban, unban, pardon, banip, unbanip)
 * are neutralized so that ApexsionsCore acts as the single authoritative moderation engine.
 */
public class EssentialsBanOverride {

    private static final List<String> OVERRIDDEN_COMMANDS = List.of(
            "ban", "tempban", "unban", "pardon", "banip", "unbanip", "pardonip", "checkban", "banlist"
    );

    public static void overrideEssentials(ApexsionsCorePlugin plugin) {
        try {
            CommandMap commandMap = Bukkit.getCommandMap();
            Method getKnownCommandsMethod = commandMap.getClass().getMethod("getKnownCommands");
            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) getKnownCommandsMethod.invoke(commandMap);

            if (knownCommands != null) {
                for (String cmdName : OVERRIDDEN_COMMANDS) {
                    Command existing = knownCommands.get(cmdName);
                    if (existing != null && !existing.getClass().getName().contains("Apexsions")) {
                        // Check if it's from Essentials or vanilla
                        plugin.getLogger().info("[EssentialsBanOverride] Unregistering conflicting command '/" + cmdName + "' (" + existing.getClass().getName() + ") to enforce ApexsionsCore authority.");
                        existing.unregister(commandMap);
                        knownCommands.remove(cmdName);
                        knownCommands.remove("essentials:" + cmdName);
                        knownCommands.remove("minecraft:" + cmdName);
                    }
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().log(Level.FINE, "[EssentialsBanOverride] CommandMap override notice: " + t.getMessage());
        }
    }
}
