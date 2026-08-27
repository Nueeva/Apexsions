package com.yourserver.apexsionscore.integration;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import com.yourserver.apexsionscore.region.Region;
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
 * Enforces Kingdom and Territory spatial restrictions on EssentialsX TPA commands.
 * - Both players must belong to the exact same kingdom.
 * - Both players must be physically inside their kingdom's territory polygon.
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

        // 1. Intercept /tpa, /tpahere, /tpask
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

            // Validate Kingdom & Territory
            String failureReason = validateTpa(sender, target);
            if (failureReason != null) {
                event.setCancelled(true);
                sender.sendMessage(miniMessage.deserialize(failureReason));
                return;
            }

            // Track request for /tpaccept verification
            pendingTpaRequests.put(target.getUniqueId(), sender.getUniqueId());
        }

        // 2. Intercept /tpaccept, /tpyes
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

    /**
     * Validates whether two players meet kingdom and territory requirements for TPA.
     * @return MiniMessage error string if invalid, or null if valid.
     */
    public String validateTpa(Player sender, Player target) {
        String p1Key = plugin.getApi().getPlayerRegionKey(sender.getUniqueId());
        String p2Key = plugin.getApi().getPlayerRegionKey(target.getUniqueId());

        // 1. Kingdom Membership Check
        if (p1Key.equalsIgnoreCase("NONE") || p2Key.equalsIgnoreCase("NONE")) {
            return "<red>✖ Teleportasi (TPA) gagal! Kedua pemain wajib terdaftar dalam sebuah kerajaan.</red>";
        }

        if (!p1Key.equalsIgnoreCase(p2Key)) {
            return "<red>✖ Teleportasi (TPA) ditolak! Anda hanya dapat melakukan TPA ke sesama anggota kerajaan (<gold>" + p1Key + "</gold>).</red>";
        }

        // 2. Physical Territory Polygon Check
        Optional<Region> regOpt = plugin.getRegionManager().getRegion(p1Key);
        if (regOpt.isEmpty()) {
            return "<red>✖ Kerajaan <gold>" + p1Key + "</gold> tidak ditemukan di sistem.</red>";
        }

        Region reg = regOpt.get();
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
