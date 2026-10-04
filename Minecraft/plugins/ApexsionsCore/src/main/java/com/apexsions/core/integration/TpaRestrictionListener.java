package com.apexsions.core.integration;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.api.Permissions;
import com.apexsions.core.claim.ClaimRole;
import com.apexsions.core.region.Region;
import com.apexsions.core.util.PlayerResolver;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enforces Kingdom, Territory, Sions (Terra Interdicta), Wilderness, Claim, Combat Tag, and War restrictions on:
 * - EssentialsX TPA commands (/tpa, /etpa, /tpahere, /etpahere, /tpask, /etpask, /call, /ecall, /tpaccept, /etpaccept, /tpyes, /etpyes, /tpaall, /tpall)
 * - EssentialsX Home & Back commands (/sethome, /esethome, /createhome, /ecreatehome, /home, /ehome, /homes, /ehomes, /back, /eback, /return, /ereturn)
 * - EssentialsX Random Teleport aliases (/tpr, /etpr, /erandomteleport, /ewild, /ewilderness, /ertp) -> routed to KingdomRtpService
 * - Actual teleport execution via PlayerTeleportEvent (prevents delayed warmup or saved home/back teleports into Sions, Wilderness, or foreign kingdoms)
 */
public class TpaRestrictionListener implements Listener {

    private static final long PENDING_TPA_TTL_MS = 180_000L; // 3 minutes
    private static final long PENDING_TELEPORT_WINDOW_MS = 12_000L; // 12 seconds warmup window

    private static final Set<String> TPA_REQUEST_COMMANDS = Set.of(
            "tpa", "etpa", "tpask", "etpask", "call", "ecall", "tpahere", "etpahere"
    );
    private static final Set<String> TPA_ACCEPT_COMMANDS = Set.of(
            "tpaccept", "etpaccept", "tpyes", "etpyes"
    );
    private static final Set<String> TPA_MASS_COMMANDS = Set.of(
            "tpaall", "etpaall", "tpall", "etpall"
    );
    private static final Set<String> SETHOME_COMMANDS = Set.of(
            "sethome", "esethome", "createhome", "ecreatehome"
    );
    private static final Set<String> HOME_TELEPORT_COMMANDS = Set.of(
            "home", "ehome", "homes", "ehomes"
    );
    private static final Set<String> BACK_TELEPORT_COMMANDS = Set.of(
            "back", "eback", "return", "ereturn"
    );
    private static final Set<String> ESSENTIALS_RTP_COMMANDS = Set.of(
            "tpr", "etpr", "erandomteleport", "ewild", "ewilderness", "ertp"
    );
    private static final Set<String> AUTHORIZED_NAVIGATION_COMMANDS = Set.of(
            "lobby", "spawn", "hub", "warp", "warps", "rtp", "wild", "wilderness",
            "krtp", "randomteleport", "rtpkingdom", "kingdom", "k", "region", "sions", "deathcoords"
    );

    public record PendingTpaRequest(UUID requesterUuid, boolean tpaHere, long createdAtMs) {}
    public record PendingCommandTeleport(String commandType, long expiresAtMs) {}

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Tracks recent TPA requests (Target UUID -> PendingTpaRequest) for /tpaccept re-verification
    private final Map<UUID, PendingTpaRequest> pendingTpaRequests = new ConcurrentHashMap<>();

    // Tracks pending command teleports (/home, /back, /tpa) to validate destination on PlayerTeleportEvent
    private final Map<UUID, PendingCommandTeleport> restrictedCommandTeleportUntil = new ConcurrentHashMap<>();

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
        // Strip namespace if present (e.g. /essentials:tpa, /essentials:sethome)
        if (cmd.contains(":")) {
            cmd = cmd.substring(cmd.indexOf(':') + 1);
        }

        Player sender = event.getPlayer();

        // 1. Check Combat Tag on Any Teleport Command
        if (isTeleportCommand(cmd, parts)) {
            if (plugin.getCombatTagService() != null && plugin.getCombatTagService().isCombatTagged(sender.getUniqueId())) {
                boolean hasBypass = hasTpaBypass(sender) || sender.hasPermission(Permissions.BYPASS_COMBAT);
                if (!hasBypass) {
                    long remaining = plugin.getCombatTagService().getRemainingSeconds(sender.getUniqueId());
                    event.setCancelled(true);
                    sender.sendMessage(miniMessage.deserialize("<red>⚔ Kamu sedang dalam mode tempur (Combat Tag: <yellow>" + remaining + "s</yellow>)! Teleportasi <yellow>/" + cmd + "</yellow> dinonaktifkan.</red>"));
                    return;
                }
            }
        }

        // Clear any pending restricted teleport marker if player invokes an authorized server navigation command
        if (AUTHORIZED_NAVIGATION_COMMANDS.contains(cmd)) {
            restrictedCommandTeleportUntil.remove(sender.getUniqueId());
        }

        // 2. Route EssentialsX /tpr and /ewild aliases directly to Apexsions KingdomRtpService
        if (ESSENTIALS_RTP_COMMANDS.contains(cmd)) {
            event.setCancelled(true);
            restrictedCommandTeleportUntil.remove(sender.getUniqueId());
            if (plugin.getKingdomRtpService() != null) {
                if (parts.length >= 2 && hasAdminBypass(sender)) {
                    plugin.getKingdomRtpService().executeRtpTargeted(sender, parts[1]);
                } else {
                    plugin.getKingdomRtpService().executeRtp(sender);
                }
            }
            return;
        }

        // 3. Block mass TPA commands (/tpaall, /tpall) for non-bypass players
        if (TPA_MASS_COMMANDS.contains(cmd)) {
            if (!hasTpaBypass(sender)) {
                event.setCancelled(true);
                sender.sendMessage(miniMessage.deserialize("<red>✖ Ditolak! Perintah <yellow>/" + cmd + "</yellow> hanya dapat dijalankan oleh Otoritas Server.</red>"));
            }
            return;
        }

        // 4. Intercept /tpa, /etpa, /tpahere, /etpahere, /tpask, /etpask, /call, /ecall
        if (TPA_REQUEST_COMMANDS.contains(cmd)) {
            if (parts.length < 2) {
                return; // Let Essentials show command usage
            }

            if (hasTpaBypass(sender)) {
                return;
            }

            // 4a. Validate sender's own kingdom membership and physical territory BEFORE resolving target
            String senderCheck = validateSenderKingdomAndTerritory(sender);
            if (senderCheck != null) {
                event.setCancelled(true);
                sender.sendMessage(miniMessage.deserialize(senderCheck));
                playDeniedSound(sender);
                return;
            }

            // 4b. Resolve target using PlayerResolver (supports Bedrock '.' prefix and '. name' spacing)
            String rawTargetQuery = extractRawTargetQuery(parts);
            Player target = resolveTargetPlayer(parts);
            if (target == null || !target.isOnline() || (plugin.getVanishManager() != null && plugin.getVanishManager().isVanished(target))) {
                event.setCancelled(true);
                sender.sendMessage(miniMessage.deserialize("<red>✖ Teleportasi (TPA) gagal! Pemain <yellow>" + rawTargetQuery + "</yellow> tidak ditemukan atau sedang offline.</red>"));
                return;
            }

            if (target.getUniqueId().equals(sender.getUniqueId())) {
                event.setCancelled(true);
                sender.sendMessage(miniMessage.deserialize("<red>✖ Anda tidak dapat mengirim permintaan teleportasi ke diri sendiri!</red>"));
                return;
            }

            // 4c. Validate full Kingdom, Territory, Combat & War rules between sender and target
            String failureReason = validateTpa(sender, target);
            if (failureReason != null) {
                event.setCancelled(true);
                sender.sendMessage(miniMessage.deserialize(failureReason));
                playDeniedSound(sender);
                return;
            }

            // Normalize command with exact online Bukkit username so EssentialsX matches the validated player
            event.setMessage("/" + cmd + " " + target.getName());

            // Track request for /tpaccept verification
            boolean isTpaHere = cmd.contains("here");
            pendingTpaRequests.put(target.getUniqueId(), new PendingTpaRequest(sender.getUniqueId(), isTpaHere, System.currentTimeMillis()));
            return;
        }

        // 5. Intercept /tpaccept, /etpaccept, /tpyes, /etpyes
        if (TPA_ACCEPT_COMMANDS.contains(cmd)) {
            if (hasTpaBypass(sender)) {
                return;
            }

            // Accepter must always be in a playable kingdom and physically inside their own kingdom territory
            String accepterCheck = validateSenderKingdomAndTerritory(sender);
            if (accepterCheck != null) {
                event.setCancelled(true);
                sender.sendMessage(miniMessage.deserialize(accepterCheck));
                playDeniedSound(sender);
                pendingTpaRequests.remove(sender.getUniqueId());
                return;
            }

            PendingTpaRequest pending = pendingTpaRequests.get(sender.getUniqueId());
            if (pending != null && (System.currentTimeMillis() - pending.createdAtMs()) > PENDING_TPA_TTL_MS) {
                pendingTpaRequests.remove(sender.getUniqueId());
                pending = null;
            }

            Player requester = null;
            if (parts.length >= 2) {
                requester = resolveTargetPlayer(parts);
            } else if (pending != null) {
                requester = Bukkit.getPlayer(pending.requesterUuid());
            }

            if (requester == null || !requester.isOnline()) {
                event.setCancelled(true);
                sender.sendMessage(miniMessage.deserialize("<red>✖ Tidak ada permintaan teleportasi (TPA) aktif yang valid untuk diterima.</red>"));
                pendingTpaRequests.remove(sender.getUniqueId());
                return;
            }

            if (!hasTpaBypass(requester)) {
                String failureReason = validateTpa(requester, sender);
                if (failureReason != null) {
                    event.setCancelled(true);
                    sender.sendMessage(miniMessage.deserialize(failureReason));
                    requester.sendMessage(miniMessage.deserialize(failureReason));
                    playDeniedSound(sender);
                    pendingTpaRequests.remove(sender.getUniqueId());
                    return;
                }

                long expiresAt = System.currentTimeMillis() + PENDING_TELEPORT_WINDOW_MS;
                restrictedCommandTeleportUntil.put(requester.getUniqueId(), new PendingCommandTeleport("TPA", expiresAt));
                restrictedCommandTeleportUntil.put(sender.getUniqueId(), new PendingCommandTeleport("TPA", expiresAt));
            }
            return;
        }

        // 6. Intercept /sethome, /esethome, /createhome, /ecreatehome
        if (SETHOME_COMMANDS.contains(cmd)) {
            String sethomeError = validateSethomeLocation(sender, sender.getLocation());
            if (sethomeError != null) {
                event.setCancelled(true);
                sender.sendMessage(miniMessage.deserialize(sethomeError));
                playDeniedSound(sender);
            }
            return;
        }

        // 7. Intercept /home, /ehome, /homes, /ehomes and /back, /eback, /return, /ereturn
        if (HOME_TELEPORT_COMMANDS.contains(cmd) || BACK_TELEPORT_COMMANDS.contains(cmd)) {
            if (!hasHomeBypass(sender)) {
                String playerKingdomKey = plugin.getApi() != null ? plugin.getApi().getPlayerRegionKey(sender.getUniqueId()) : "NONE";
                if (playerKingdomKey == null || playerKingdomKey.equalsIgnoreCase("NONE")) {
                    event.setCancelled(true);
                    sender.sendMessage(miniMessage.deserialize("<red>✖ Ditolak! Anda wajib memilih dan bergabung ke dalam kerajaan terlebih dahulu sebelum menggunakan <yellow>/" + cmd + "</yellow>!</red>"));
                    playDeniedSound(sender);
                    return;
                }
                String type = HOME_TELEPORT_COMMANDS.contains(cmd) ? "HOME" : "BACK";
                restrictedCommandTeleportUntil.put(sender.getUniqueId(),
                        new PendingCommandTeleport(type, System.currentTimeMillis() + PENDING_TELEPORT_WINDOW_MS));
            }
        }
    }

    /**
     * Enforces destination territory boundaries when a restricted command teleport (/home, /back, /tpa)
     * actually executes (immediately or after EssentialsX warmup).
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND
                && event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
            return;
        }

        Player player = event.getPlayer();
        PendingCommandTeleport pending = restrictedCommandTeleportUntil.get(player.getUniqueId());
        if (pending == null) {
            return;
        }

        if (System.currentTimeMillis() > pending.expiresAtMs()) {
            restrictedCommandTeleportUntil.remove(player.getUniqueId());
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || to.getWorld() == null) {
            return;
        }

        // Ignore sub-block movement or head rotation during warmup
        if (from != null && from.getWorld() != null && from.getWorld().equals(to.getWorld()) && from.distanceSquared(to) < 1.0) {
            return;
        }

        restrictedCommandTeleportUntil.remove(player.getUniqueId());

        if (hasHomeBypass(player)) {
            return;
        }

        String error = validateTeleportDestination(player, from, to, pending.commandType());
        if (error != null) {
            event.setCancelled(true);
            player.sendMessage(miniMessage.deserialize(error));
            playDeniedSound(player);
        }
    }

    private void playDeniedSound(Player player) {
        if (Bukkit.getServer() == null || player == null) {
            return;
        }
        try {
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 0.6f, 1.2f);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Validates whether a player is allowed to /sethome at {@code loc}.
     * Blocks:
     * 1. Players without a kingdom (NONE)
     * 2. Wilderness (outside any defined region)
     * 3. Non-playable regions (Kerajaan Sions / Terra Interdicta)
     * 4. Foreign kingdoms (regions not matching player's pledged kingdom)
     * 5. Land claims owned by another player without Builder trust
     *
     * @return MiniMessage error string if disallowed, or {@code null} if allowed.
     */
    public String validateSethomeLocation(Player sender, Location loc) {
        if (hasHomeBypass(sender)) {
            return null;
        }

        String playerKingdomKey = plugin.getApi() != null ? plugin.getApi().getPlayerRegionKey(sender.getUniqueId()) : "NONE";
        if (playerKingdomKey == null || playerKingdomKey.equalsIgnoreCase("NONE")) {
            return "<red>✖ Ditolak! Anda wajib memilih dan bergabung ke dalam kerajaan terlebih dahulu sebelum dapat memasang <yellow>/sethome</yellow>!</red>";
        }

        if (plugin.getRegionManager() == null || loc == null) {
            return "<red>✖ Ditolak! Wilayah kerajaan belum siap.</red>";
        }

        Optional<Region> curRegionOpt = plugin.getRegionManager().getRegionAt(loc);
        if (curRegionOpt.isEmpty()) {
            return "<red>✖ Ditolak! Anda tidak dapat memasang <yellow>/sethome</yellow> di area <gold>Wilderness</gold> (di luar wilayah kerajaan)! Gunakan <yellow>/sethome</yellow> hanya di dalam wilayah kerajaan Anda sendiri.</red>";
        }

        Region curRegion = curRegionOpt.get();
        if (!curRegion.isPlayable()) {
            return "<dark_red>✖ Ditolak! Anda dilarang keras memasang <yellow>/sethome</yellow> di zona terlarang <gold>" + curRegion.getDisplayName() + "</gold> (<red>Terra Interdicta</red>)!</dark_red>";
        }

        if (!curRegion.getKey().equalsIgnoreCase(playerKingdomKey)) {
            return "<red>✖ Ditolak! Anda tidak dapat memasang <yellow>/sethome</yellow> di dalam teritori kedaulatan kerajaan asing (<gold>" + curRegion.getDisplayName() + "</gold>)!</red>";
        }

        if (plugin.getClaimManager() != null) {
            var claimOpt = plugin.getClaimManager().getClaimAt(loc);
            if (claimOpt.isPresent()) {
                var claim = claimOpt.get();
                if (!claim.isOwner(sender.getUniqueId()) && !claim.getRole(sender.getUniqueId()).isAtLeast(ClaimRole.BUILDER)) {
                    return "<red>✖ Ditolak! Anda tidak dapat memasang <yellow>/sethome</yellow> di dalam wilayah terproteksi milik <gold>" + claim.getOwnerName() + "</gold> tanpa izin Builder!</red>";
                }
            }
        }

        return null;
    }

    /**
     * Validates the destination of a /home, /back, or /tpa teleport on PlayerTeleportEvent.
     */
    public String validateTeleportDestination(Player player, Location from, Location to, String commandType) {
        if (hasHomeBypass(player)) {
            return null;
        }

        String playerKingdomKey = plugin.getApi() != null ? plugin.getApi().getPlayerRegionKey(player.getUniqueId()) : "NONE";
        if (playerKingdomKey == null || playerKingdomKey.equalsIgnoreCase("NONE")) {
            return "<red>✖ Teleportasi dibatalkan! Anda wajib terdaftar dalam sebuah kerajaan.</red>";
        }

        if (plugin.getRegionManager() == null || to == null) {
            return null;
        }

        Optional<Region> destRegionOpt = plugin.getRegionManager().getRegionAt(to);
        if (destRegionOpt.isEmpty()) {
            return "<red>✖ Teleportasi (<yellow>" + commandType + "</yellow>) dibatalkan! Lokasi tujuan berada di area <gold>Wilderness</gold> (di luar wilayah kerajaan Anda)!</red>";
        }

        Region destRegion = destRegionOpt.get();
        if (!destRegion.isPlayable()) {
            return "<dark_red>✖ Teleportasi (<yellow>" + commandType + "</yellow>) dibatalkan! Lokasi tujuan berada di zona terlarang <gold>" + destRegion.getDisplayName() + "</gold> (<red>Terra Interdicta</red>)!</dark_red>";
        }

        if (!destRegion.getKey().equalsIgnoreCase(playerKingdomKey)) {
            return "<red>✖ Teleportasi (<yellow>" + commandType + "</yellow>) dibatalkan! Lokasi tujuan berada di dalam teritori kerajaan asing (<gold>" + destRegion.getDisplayName() + "</gold>)!</red>";
        }

        if ("TPA".equalsIgnoreCase(commandType) && from != null && !destRegion.containsLocation(from)) {
            return "<red>✖ Teleportasi (TPA) dibatalkan! Anda berada di luar wilayah teritorial kerajaan <gold>" + destRegion.getDisplayName() + "</gold>!</red>";
        }

        if ("HOME".equalsIgnoreCase(commandType) && plugin.getClaimManager() != null) {
            var claimOpt = plugin.getClaimManager().getClaimAt(to);
            if (claimOpt.isPresent()) {
                var claim = claimOpt.get();
                if (!claim.isOwner(player.getUniqueId()) && !claim.getRole(player.getUniqueId()).isAtLeast(ClaimRole.BUILDER)) {
                    return "<red>✖ Teleportasi (<yellow>/home</yellow>) dibatalkan! Lokasi home berada di dalam klaim milik <gold>" + claim.getOwnerName() + "</gold> tanpa izin Builder!</red>";
                }
            }
        }

        return null;
    }

    /**
     * Validates that a single player has joined a playable kingdom and is physically inside their kingdom territory.
     */
    public String validateSenderKingdomAndTerritory(Player player) {
        if (hasTpaBypass(player)) {
            return null;
        }

        String pKey = plugin.getApi() != null ? plugin.getApi().getPlayerRegionKey(player.getUniqueId()) : "NONE";
        if (pKey == null || pKey.equalsIgnoreCase("NONE")) {
            return "<red>✖ Teleportasi (TPA) gagal! Anda wajib memilih dan bergabung ke dalam sebuah kerajaan terlebih dahulu.</red>";
        }

        Optional<Region> regOpt = plugin.getRegionManager().getRegion(pKey);
        if (regOpt.isEmpty() || !regOpt.get().isPlayable()) {
            return "<red>✖ Teleportasi (TPA) gagal! Kerajaan <gold>" + pKey + "</gold> tidak valid untuk TPA.</red>";
        }

        Region reg = regOpt.get();
        if (!reg.containsLocation(player.getLocation())) {
            return "<red>✖ Teleportasi (TPA) gagal! Anda sedang berada di luar wilayah teritorial kerajaan <gold>" + reg.getDisplayName() + "</gold>!</red>";
        }

        return null;
    }

    private boolean hasTpaBypass(Player player) {
        return player.isOp()
                || player.hasPermission(Permissions.TPA_BYPASS)
                || player.hasPermission(Permissions.ADMIN)
                || (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isConclaveStaff(player));
    }

    private boolean hasHomeBypass(Player player) {
        return player.isOp()
                || player.hasPermission(Permissions.ADMIN)
                || player.hasPermission(Permissions.ADMIN_BYPASS_CLAIM)
                || player.hasPermission(Permissions.TPA_BYPASS)
                || (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isConclaveStaff(player));
    }

    private boolean hasAdminBypass(Player player) {
        return player.isOp()
                || player.hasPermission(Permissions.ADMIN)
                || (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isConclaveStaff(player));
    }

    private String extractRawTargetQuery(String[] parts) {
        if (parts.length >= 3 && (parts[1].equals(".") || parts[1].equals("*") || parts[1].equals("_"))) {
            return parts[1] + parts[2];
        }
        return parts.length >= 2 ? parts[1] : "";
    }

    private Player resolveTargetPlayer(String[] parts) {
        String query = extractRawTargetQuery(parts);
        if (query.isEmpty()) {
            return null;
        }
        return PlayerResolver.resolveOnline(query);
    }

    private boolean isTeleportCommand(String cmd, String[] parts) {
        if (TPA_REQUEST_COMMANDS.contains(cmd)
                || TPA_ACCEPT_COMMANDS.contains(cmd)
                || TPA_MASS_COMMANDS.contains(cmd)
                || SETHOME_COMMANDS.contains(cmd)
                || HOME_TELEPORT_COMMANDS.contains(cmd)
                || BACK_TELEPORT_COMMANDS.contains(cmd)
                || ESSENTIALS_RTP_COMMANDS.contains(cmd)
                || AUTHORIZED_NAVIGATION_COMMANDS.contains(cmd)) {
            if (cmd.equals("kingdom") || cmd.equals("k") || cmd.equals("region")) {
                return parts.length >= 2 && (parts[1].equalsIgnoreCase("spawn")
                        || parts[1].equalsIgnoreCase("warp")
                        || parts[1].equalsIgnoreCase("rtp"));
            }
            return true;
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

        // Check if either player is Conclave staff or has admin bypass
        if (hasTpaBypass(sender) || hasTpaBypass(target)) {
            return null; // Conclave staff bypasses kingdom mismatch and territory bounds
        }

        String p1Key = plugin.getApi().getPlayerRegionKey(sender.getUniqueId());
        String p2Key = plugin.getApi().getPlayerRegionKey(target.getUniqueId());

        // 2. Kingdom Membership Check
        if (p1Key == null || p2Key == null || p1Key.equalsIgnoreCase("NONE") || p2Key.equalsIgnoreCase("NONE")) {
            return "<red>✖ Teleportasi (TPA) gagal! Kedua pemain wajib terdaftar dalam sebuah kerajaan.</red>";
        }

        if (!p1Key.equalsIgnoreCase(p2Key)) {
            return "<red>✖ Teleportasi (TPA) ditolak! Anda hanya dapat melakukan TPA ke sesama anggota kerajaan (<gold>" + p1Key + "</gold>).</red>";
        }

        // 3. Physical Territory Polygon Check
        Optional<Region> regOpt = plugin.getRegionManager().getRegion(p1Key);
        if (regOpt.isEmpty() || !regOpt.get().isPlayable()) {
            return "<red>✖ Kerajaan <gold>" + p1Key + "</gold> tidak ditemukan atau bukan kerajaan bermain.</red>";
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
