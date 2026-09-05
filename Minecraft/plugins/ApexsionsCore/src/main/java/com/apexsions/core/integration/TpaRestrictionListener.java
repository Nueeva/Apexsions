package com.apexsions.core.integration;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.region.Region;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enforces Kingdom, Territory, Combat Tag, and War restrictions on teleportation commands:
 * - EssentialsX TPA commands (/tpa, /tpahere, /tpask, /tpaccept, /tpyes)
 * - Navigation commands (/spawn, /lobby, /hub, /kingdom spawn)
 */
public class TpaRestrictionListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Tracks recent TPA requests (Target UUID -> Sender UUID) for /tpaccept re-verification
    private final Map<UUID, UUID> pendingTpaRequests = new ConcurrentHashMap<>();

    public TpaRestrictionListener(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().trim();
        if (!msg.startsWith("/")) return;

        String[] parts = msg.substring(1).split("\\s+");
        if (parts.length == 0) return;

        String cmd = parts[0].toLowerCase(Locale.ROOT);
        // Strip namespace if present (e.g. /essentials:tpa)
        if (cmd.contains(":")) {
            cmd = cmd.substring(cmd.indexOf(':') + 1);
        }

        Player sender = event.getPlayer();

        // 1. Check Combat Tag on Any Teleport Command
        if (isTeleportCommand(cmd, parts)) {
            if (plugin.getCombatTagService() != null && plugin.getCombatTagService().isCombatTagged(sender.getUniqueId())) {
                if (!sender.hasPermission("apexsionscore.admin.bypass.tpa")) {
                    long remaining = plugin.getCombatTagService().getRemainingSeconds(sender.getUniqueId());
                    event.setCancelled(true);
                    sender.sendMessage(miniMessage.deserialize("<red>⚔ Kamu sedang dalam mode tempur (Combat Tag: <yellow>" + remaining + "s</yellow>)! Teleportasi <yellow>/" + cmd + "</yellow> dinonaktifkan.</red>"));
                    return;
                }
            }
        }

        // 2. Intercept /tpa, /tpahere, /tpask
        if (cmd.equals("tpa") || cmd.equals("tpahere") || cmd.equals("tpask")) {
            if (parts.length < 2) {
                return; // Let Essentials show command usage
            }

            String targetName = parts[1];
            Player target = Bukkit.getPlayer(targetName);
            if (target == null || !target.isOnline()) {
                return; // Let Essentials handle offline/unknown player
            }

            if (target.getUniqueId().equals(sender.getUniqueId())) {
                return; // Let Essentials handle self-tpa
            }

            // Bypass permission for admins
            if (sender.hasPermission("apexsionscore.admin.bypass.tpa")) {
                return;
            }

            // Validate Kingdom, Territory, Combat & War
            String failureReason = validateTpa(sender, target);
            if (failureReason != null) {
                event.setCancelled(true);
                sender.sendMessage(miniMessage.deserialize(failureReason));
                return;
            }

            // Track request for /tpaccept verification
            pendingTpaRequests.put(target.getUniqueId(), sender.getUniqueId());
        }

        // 3. Intercept /tpaccept, /tpyes
        else if (cmd.equals("tpaccept") || cmd.equals("tpyes")) {
            UUID senderUuid = pendingTpaRequests.get(sender.getUniqueId());
            if (senderUuid != null) {
                Player requester = Bukkit.getPlayer(senderUuid);
                if (requester != null && requester.isOnline()) {
                    if (!sender.hasPermission("apexsionscore.admin.bypass.tpa")) {
                        String failureReason = validateTpa(requester, sender);
                        if (failureReason != null) {
                            event.setCancelled(true);
                            sender.sendMessage(miniMessage.deserialize(failureReason));
                            requester.sendMessage(miniMessage.deserialize(failureReason));
                            pendingTpaRequests.remove(sender.getUniqueId());
                        }
                    }
                }
            }
        }
    }

    private boolean isTeleportCommand(String cmd, String[] parts) {
        if (cmd.equals("tpa") || cmd.equals("tpahere") || cmd.equals("tpask") ||
                cmd.equals("tpaccept") || cmd.equals("tpyes") || cmd.equals("rtp") ||
                cmd.equals("wild") || cmd.equals("wilderness") || cmd.equals("krtp") ||
                cmd.equals("spawn") || cmd.equals("lobby") || cmd.equals("hub") ||
                cmd.equals("home") || cmd.equals("sethome") || cmd.equals("warp") || cmd.equals("back")) {
            return true;
        }

        if (cmd.equals("kingdom") || cmd.equals("k") || cmd.equals("region")) {
            return parts.length >= 2 && (parts[1].equalsIgnoreCase("spawn") || parts[1].equalsIgnoreCase("warp") || parts[1].equalsIgnoreCase("rtp"));
        }

        return false;
    }

    /**
     * Validates whether two players meet kingdom, territory, and war requirements for TPA.
     * @return MiniMessage error string if invalid, or null if valid.
     */
    public String validateTpa(Player sender, Player target) {
        // 1. Combat Tag Check on Both Players
        if (plugin.getCombatTagService() != null) {
            if (plugin.getCombatTagService().isCombatTagged(sender.getUniqueId())) {
                return "<red>✖ Teleportasi ditolak! Anda sedang dalam mode tempur (Combat Tag).</red>";
            }
            if (plugin.getCombatTagService().isCombatTagged(target.getUniqueId())) {
                return "<red>✖ Teleportasi ditolak! Pemain tujuan (<yellow>" + target.getName() + "</yellow>) sedang dalam mode tempur.</red>";
            }
        }

        String p1Key = plugin.getApi().getPlayerRegionKey(sender.getUniqueId());
        String p2Key = plugin.getApi().getPlayerRegionKey(target.getUniqueId());

        // 2. Kingdom Membership Check
        if (p1Key.equalsIgnoreCase("NONE") || p2Key.equalsIgnoreCase("NONE")) {
            return "<red>✖ Teleportasi (TPA) gagal! Kedua pemain wajib terdaftar dalam sebuah kerajaan.</red>";
        }

        if (!p1Key.equalsIgnoreCase(p2Key)) {
            return "<red>✖ Teleportasi (TPA) ditolak! Anda hanya dapat melakukan TPA ke sesama anggota kerajaan (<gold>" + p1Key + "</gold>).</red>";
        }

        // 3. Physical Territory Polygon Check
        Optional<Region> regOpt = plugin.getRegionManager().getRegion(p1Key);
        if (regOpt.isEmpty()) {
            return "<red>✖ Kerajaan <gold>" + p1Key + "</gold> tidak ditemukan di sistem.</red>";
        }

        Region reg = regOpt.get();

        // 4. War Status Check in Territory
        if (plugin.getWarManager() != null && plugin.getWarManager().isWarActiveInTerritory(reg)) {
            return "<dark_red>⚔ Teleportasi (TPA) diblokir! Wilayah kerajaan <yellow>" + reg.getDisplayName() + "</yellow> sedang dalam keadaan PERANG (WAR)!</dark_red>";
        }

        boolean senderInside = reg.containsLocation(sender.getLocation());
        boolean targetInside = reg.containsLocation(target.getLocation());

        if (!senderInside && !targetInside) {
            return "<red>✖ Teleportasi (TPA) gagal! Kedua pemain sedang berada di luar wilayah teritorial kerajaan <gold>" + reg.getDisplayName() + "</gold>!</red>";
        }

        if (!senderInside) {
            return "<red>✖ Teleportasi (TPA) gagal! Anda berada di luar wilayah teritorial kerajaan <gold>" + reg.getDisplayName() + "</gold>!</red>";
        }

        if (!targetInside) {
            return "<red>✖ Teleportasi (TPA) gagal! Pemain target (<yellow>" + target.getName() + "</yellow>) sedang berada di luar wilayah teritorial kerajaan!</red>";
        }

        return null; // Valid!
    }
}
