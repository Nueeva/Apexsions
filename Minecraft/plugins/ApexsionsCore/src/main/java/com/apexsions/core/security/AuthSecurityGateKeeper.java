package com.apexsions.core.security;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.integration.AuthMeHook;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gatekeeper enforcing strict pre-login isolation and defending against Auth Bypass,
 * Session Hijacking, and Staff Account Takeovers.
 */
public class AuthSecurityGateKeeper implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private static final Set<String> ALLOWED_AUTH_COMMANDS = new HashSet<>(Arrays.asList(
            "login", "l", "log", "register", "reg", "email", "captcha", "2fa", "totp"
    ));

    // Anti-Brute-Force & Staff Account Protection
    private final Map<String, Integer> failedLoginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> ipTempBlocks = new ConcurrentHashMap<>();
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long TEMP_BLOCK_DURATION_MS = 10 * 60 * 1000L; // 10 minutes

    public AuthSecurityGateKeeper(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isStaff(Player player) {
        if (player.isOp()) return true;
        if (player.hasPermission("apexsions.admin") || player.hasPermission("apexsions.staff")) {
            return true;
        }
        if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isAvailable()) {
            String rank = plugin.getLuckPermsHook().getPlayerRankKey(player);
            if (rank != null) {
                String r = rank.toLowerCase().trim();
                return r.equals("ancestor") || r.equals("architect") || r.equals("overseer") ||
                       r.equals("warden") || r.equals("herald") || r.equals("admin") || r.equals("mod");
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String ip = event.getAddress().getHostAddress();
        Long blockedUntil = ipTempBlocks.get(ip);
        if (blockedUntil != null) {
            if (System.currentTimeMillis() < blockedUntil) {
                long remainingSec = (blockedUntil - System.currentTimeMillis()) / 1000L;
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        mm.deserialize("<red>[Apexsions Security] IP Anda diblokir sementara karena percobaan login gagal berulang kali.<newline><gray>Coba lagi dalam " + remainingSec + " detik.</gray></red>"));
                return;
            } else {
                ipTempBlocks.remove(ip);
                failedLoginAttempts.remove(ip);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (AuthMeHook.isAuthenticated(player)) {
            return;
        }

        String raw = event.getMessage().trim();
        if (raw.startsWith("/")) {
            raw = raw.substring(1);
        }

        String[] parts = raw.split(" ");
        String baseCmd = parts[0].toLowerCase();

        // Handle namespace prefixes like /authme:login
        if (baseCmd.contains(":")) {
            baseCmd = baseCmd.substring(baseCmd.indexOf(":") + 1);
        }

        // Allow legitimate authentication commands only
        if (ALLOWED_AUTH_COMMANDS.contains(baseCmd)) {
            return;
        }

        // Block all bypass attempts
        event.setCancelled(true);
        player.sendMessage(mm.deserialize("<red>⚠ Akses ditolak! Anda wajib login terlebih dahulu menggunakan <yellow>/login <kata_sandi></yellow> untuk menjalankan perintah.</red>"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommandMonitor(PlayerCommandPreprocessEvent event) {
        // Monitor failed login attempts for Anti-Brute-Force
        String raw = event.getMessage().trim().toLowerCase();
        if (raw.startsWith("/login") || raw.startsWith("/l ")) {
            Player player = event.getPlayer();
            String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown";

            // If player is a Staff account, enforce zero-tolerance brute-force defense
            if (isStaff(player)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline() && !AuthMeHook.isAuthenticated(player)) {
                        // Still unauthenticated after login command execution -> failed password!
                        int fails = failedLoginAttempts.getOrDefault(ip, 0) + 1;
                        failedLoginAttempts.put(ip, fails);

                        if (fails >= MAX_FAILED_ATTEMPTS) {
                            ipTempBlocks.put(ip, System.currentTimeMillis() + TEMP_BLOCK_DURATION_MS);
                            player.kick(mm.deserialize("<red>[Apexsions Security] Percobaan login akun Staf gagal 3x berturut-turut.<newline><yellow>Koneksi diputus & IP diblokir sementara 10 menit demi keamanan.</yellow></red>"));

                            // Broadcast high-priority alert to console and online staff
                            String alert = "<gold>[<red>Apexsions Staff Shield</red>]</gold> <red>PERINGATAN:</red> <yellow>Upaya brute-force terdeteksi pada akun staf <gold>" +
                                    player.getName() + "</gold> dari IP <gray>" + ip + "</gray>! IP diblokir sementara.</yellow>";

                            Bukkit.getOnlinePlayers().stream()
                                    .filter(p -> p.hasPermission("apexsions.admin") || p.isOp())
                                    .forEach(staff -> staff.sendMessage(mm.deserialize(alert)));

                            plugin.getLogger().severe("[Staff Shield] Brute-force attempt detected on staff account " + player.getName() + " from IP " + ip);
                        }
                    } else if (player.isOnline() && AuthMeHook.isAuthenticated(player)) {
                        // Successful login, clear failed counter
                        failedLoginAttempts.remove(ip);
                    }
                }, 10L); // check 0.5s later after AuthMe processes password
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (AuthMeHook.isAuthenticated(player)) {
            return;
        }

        // Freeze player in-place before login (prevent moving away or freecam glitch)
        if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!AuthMeHook.isAuthenticated(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player && !AuthMeHook.isAuthenticated(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && !AuthMeHook.isAuthenticated(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!AuthMeHook.isAuthenticated(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && !AuthMeHook.isAuthenticated(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker && !AuthMeHook.isAuthenticated(attacker)) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof Player victim && !AuthMeHook.isAuthenticated(victim)) {
            event.setCancelled(true);
        }
    }
}
