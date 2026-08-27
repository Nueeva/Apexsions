package com.apex.economy.trade;

import com.apex.economy.ApexsionsEconomy;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TradeManager implements Listener {

    private final ApexsionsEconomy plugin;
    // Key: Target UUID -> Map<Sender UUID, TradeRequest>
    private final Map<UUID, Map<UUID, TradeRequest>> pendingRequests = new ConcurrentHashMap<>();
    // Key: Player UUID -> Active TradeSession
    private final Map<UUID, TradeSession> activeSessions = new ConcurrentHashMap<>();
    // Key: Player UUID -> Trade Enabled boolean
    private final Map<UUID, Boolean> tradeToggleCache = new ConcurrentHashMap<>();

    public TradeManager(ApexsionsEconomy plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startCleanupTask();
    }

    public boolean isTradeEnabled(UUID uuid) {
        if (tradeToggleCache.containsKey(uuid)) {
            return tradeToggleCache.get(uuid);
        }
        try {
            boolean enabled = plugin.getRepository().loadTradeEnabled(uuid).get();
            tradeToggleCache.put(uuid, enabled);
            return enabled;
        } catch (Exception e) {
            return true;
        }
    }

    public void setTradeEnabled(UUID uuid, boolean enabled) {
        tradeToggleCache.put(uuid, enabled);
        plugin.getRepository().saveTradeEnabled(uuid, enabled);
    }

    public boolean toggleTrade(Player player) {
        boolean current = isTradeEnabled(player.getUniqueId());
        boolean newState = !current;
        setTradeEnabled(player.getUniqueId(), newState);

        if (newState) {
            player.sendMessage("Â§a[âœ”] Fitur trade Anda sekarang Â§eAKTIFÂ§a. Pemain lain dapat mengirimkan permintaan trade.");
        } else {
            player.sendMessage("Â§c[âœ–] Fitur trade Anda sekarang Â§eNONAKTIFÂ§c. Pemain lain tidak dapat mengirimkan permintaan trade ke Anda.");
        }
        return newState;
    }

    private void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, Map<UUID, TradeRequest>> entry : pendingRequests.entrySet()) {
                entry.getValue().entrySet().removeIf(reqEntry -> {
                    if (reqEntry.getValue().isExpired()) {
                        Player sender = Bukkit.getPlayer(reqEntry.getValue().getSenderUuid());
                        if (sender != null && sender.isOnline()) {
                            sender.sendMessage("Â§c[!] Permintaan trade ke Â§e" + reqEntry.getValue().getTargetName() + " Â§ctelah kedaluwarsa.");
                        }
                        return true;
                    }
                    return false;
                });
            }
        }, 100L, 100L); // every 5 seconds
    }

    public synchronized boolean sendRequest(Player sender, Player target) {
        if (sender == null || target == null || !sender.isOnline() || !target.isOnline()) return false;

        if (sender.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage("Â§cAnda tidak dapat mengajak diri sendiri untuk trade!");
            return false;
        }

        if (!isTradeEnabled(sender.getUniqueId())) {
            sender.sendMessage("Â§cAnda sedang menonaktifkan fitur trade! Aktifkan kembali dengan Â§e/trade toggle Â§cuntuk mengajak pemain lain.");
            return false;
        }

        if (!isTradeEnabled(target.getUniqueId())) {
            sender.sendMessage("Â§cPemain Â§e" + target.getName() + " Â§csedang menonaktifkan permintaan trade.");
            return false;
        }

        if (isInTrade(sender)) {
            sender.sendMessage("Â§cAnda sedang berada dalam sesi trade!");
            return false;
        }

        if (isInTrade(target)) {
            sender.sendMessage("Â§cPemain Â§e" + target.getName() + " Â§csedang berada dalam sesi trade lain!");
            return false;
        }

        // If target already sent a request to sender, accept it immediately
        Map<UUID, TradeRequest> myRequests = pendingRequests.get(sender.getUniqueId());
        if (myRequests != null && myRequests.containsKey(target.getUniqueId())) {
            TradeRequest incoming = myRequests.remove(target.getUniqueId());
            if (incoming != null && !incoming.isExpired()) {
                startTradeSession(target, sender);
                return true;
            }
        }

        TradeRequest request = new TradeRequest(sender.getUniqueId(), sender.getName(), target.getUniqueId(), target.getName(), 60);
        pendingRequests.computeIfAbsent(target.getUniqueId(), k -> new ConcurrentHashMap<>()).put(sender.getUniqueId(), request);

        sender.sendMessage("Â§a[âœ”] Permintaan trade telah dikirim ke Â§e" + target.getName() + "Â§a. Menunggu persetujuan (60 detik)...");

        // Send rich message with interactive buttons to target
        target.sendMessage("Â§8=======================================");
        target.sendMessage("Â§6Â§l[TRADE] Â§e" + sender.getName() + " Â§7mengajak Anda untuk melakukan trade!");


        try {
            TextComponent acceptBtn = new TextComponent("[âœ” SETUJU]");
            acceptBtn.setColor(ChatColor.GREEN);
            acceptBtn.setBold(true);
            acceptBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade accept " + sender.getName()));
            acceptBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Klik untuk menerima permintaan trade dari " + sender.getName()).color(ChatColor.GREEN).create()));

            TextComponent space = new TextComponent("   ");

            TextComponent denyBtn = new TextComponent("[âœ– TOLAK]");
            denyBtn.setColor(ChatColor.RED);
            denyBtn.setBold(true);
            denyBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade deny " + sender.getName()));
            denyBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Klik untuk menolak permintaan trade dari " + sender.getName()).color(ChatColor.RED).create()));

            TextComponent fullMessage = new TextComponent("         ");
            fullMessage.addExtra(acceptBtn);
            fullMessage.addExtra(space);
            fullMessage.addExtra(denyBtn);

            target.spigot().sendMessage(fullMessage);
        } catch (Throwable t) {
            target.sendMessage("Â§7Ketik Â§a/trade accept " + sender.getName() + " Â§7untuk setuju, atau Â§c/trade deny " + sender.getName() + " Â§7untuk menolak.");
        }

        target.sendMessage("Â§8=======================================");
        return true;
    }


    public synchronized boolean acceptRequest(Player target, String senderName) {
        if (target == null) return false;
        Map<UUID, TradeRequest> requests = pendingRequests.get(target.getUniqueId());
        if (requests == null || requests.isEmpty()) {
            target.sendMessage("Â§cAnda tidak memiliki permintaan trade yang masuk!");
            return false;
        }

        TradeRequest matchingRequest = null;
        if (senderName != null && !senderName.isEmpty()) {
            for (TradeRequest req : requests.values()) {
                if (req.getSenderName().equalsIgnoreCase(senderName)) {
                    matchingRequest = req;
                    break;
                }
            }
        } else {
            // Get most recent
            for (TradeRequest req : requests.values()) {
                if (!req.isExpired()) {
                    matchingRequest = req;
                    break;
                }
            }
        }

        if (matchingRequest == null || matchingRequest.isExpired()) {
            target.sendMessage("Â§cPermintaan trade dari pemain tersebut tidak ditemukan atau sudah kedaluwarsa!");
            return false;
        }

        Player sender = Bukkit.getPlayer(matchingRequest.getSenderUuid());
        if (sender == null || !sender.isOnline()) {
            target.sendMessage("Â§cPemain yang mengajak trade sedang offline!");
            requests.remove(matchingRequest.getSenderUuid());
            return false;
        }

        if (isInTrade(target)) {
            target.sendMessage("Â§cAnda sedang berada dalam sesi trade lain!");
            return false;
        }

        if (isInTrade(sender)) {
            target.sendMessage("Â§cPemain Â§e" + sender.getName() + " Â§csedang berada dalam sesi trade lain!");
            return false;
        }

        requests.remove(matchingRequest.getSenderUuid());
        startTradeSession(sender, target);
        return true;
    }

    public synchronized boolean denyRequest(Player target, String senderName) {
        if (target == null) return false;
        Map<UUID, TradeRequest> requests = pendingRequests.get(target.getUniqueId());
        if (requests == null || requests.isEmpty()) {
            target.sendMessage("Â§cAnda tidak memiliki permintaan trade yang masuk!");
            return false;
        }

        TradeRequest matchingRequest = null;
        if (senderName != null && !senderName.isEmpty()) {
            for (TradeRequest req : requests.values()) {
                if (req.getSenderName().equalsIgnoreCase(senderName)) {
                    matchingRequest = req;
                    break;
                }
            }
        } else {
            for (TradeRequest req : requests.values()) {
                matchingRequest = req;
                break;
            }
        }

        if (matchingRequest == null) {
            target.sendMessage("Â§cPermintaan trade dari pemain tersebut tidak ditemukan!");
            return false;
        }

        requests.remove(matchingRequest.getSenderUuid());
        target.sendMessage("Â§e[!] Anda telah menolak permintaan trade dari Â§6" + matchingRequest.getSenderName() + "Â§e.");

        Player sender = Bukkit.getPlayer(matchingRequest.getSenderUuid());
        if (sender != null && sender.isOnline()) {
            sender.sendMessage("Â§c[!] Â§e" + target.getName() + " Â§ctelah menolak permintaan trade Anda.");
        }
        return true;
    }

    public synchronized void startTradeSession(Player player1, Player player2) {
        TradeSession session = new TradeSession(plugin, player1, player2);
        activeSessions.put(player1.getUniqueId(), session);
        activeSessions.put(player2.getUniqueId(), session);

        player1.sendMessage("Â§a[âœ”] Sesi trade dengan Â§e" + player2.getName() + " Â§adimulai!");
        player2.sendMessage("Â§a[âœ”] Sesi trade dengan Â§e" + player1.getName() + " Â§adimulai!");

        session.start();
    }

    public synchronized void endSession(TradeSession session) {
        if (session != null) {
            if (session.getPlayer1() != null) activeSessions.remove(session.getPlayer1().getUniqueId());
            if (session.getPlayer2() != null) activeSessions.remove(session.getPlayer2().getUniqueId());
        }
    }

    public boolean isInTrade(Player player) {
        return player != null && activeSessions.containsKey(player.getUniqueId());
    }

    public TradeSession getActiveSession(Player player) {
        if (player == null) return null;
        return activeSessions.get(player.getUniqueId());
    }

    public void cancelAllTradesOnDisable() {
        Set<TradeSession> uniqueSessions = new HashSet<>(activeSessions.values());
        for (TradeSession session : uniqueSessions) {
            session.cancelTrade(null, "Server sedang reload atau dimatikan");
        }
        activeSessions.clear();
        pendingRequests.clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        TradeSession session = activeSessions.get(player.getUniqueId());
        if (session != null) {
            session.cancelTrade(player, "Pemain keluar dari server");
        }
        pendingRequests.remove(player.getUniqueId());
    }
}
