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
            player.sendMessage("§a[✔] Fitur trade Anda sekarang §eAKTIF§a. Pemain lain dapat mengirimkan permintaan trade.");
        } else {
            player.sendMessage("§c[✖] Fitur trade Anda sekarang §eNONAKTIF§c. Pemain lain tidak dapat mengirimkan permintaan trade ke Anda.");
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
                            sender.sendMessage("§c[!] Permintaan trade ke §e" + reqEntry.getValue().getTargetName() + " §ctelah kedaluwarsa.");
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
            sender.sendMessage("§cAnda tidak dapat mengajak diri sendiri untuk trade!");
            return false;
        }

        if (!isTradeEnabled(sender.getUniqueId())) {
            sender.sendMessage("§cAnda sedang menonaktifkan fitur trade! Aktifkan kembali dengan §e/trade toggle §cuntuk mengajak pemain lain.");
            return false;
        }

        if (!isTradeEnabled(target.getUniqueId())) {
            sender.sendMessage("§cPemain §e" + target.getName() + " §csedang menonaktifkan permintaan trade.");
            return false;
        }

        if (isInTrade(sender)) {
            sender.sendMessage("§cAnda sedang berada dalam sesi trade!");
            return false;
        }

        if (isInTrade(target)) {
            sender.sendMessage("§cPemain §e" + target.getName() + " §csedang berada dalam sesi trade lain!");
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

        sender.sendMessage("§a[✔] Permintaan trade telah dikirim ke §e" + target.getName() + "§a. Menunggu persetujuan (60 detik)...");

        // Send rich message with interactive buttons to target
        target.sendMessage("§8=======================================");
        target.sendMessage("§6§l[TRADE] §e" + sender.getName() + " §7mengajak Anda untuk melakukan trade!");


        try {
            TextComponent acceptBtn = new TextComponent("[✔ SETUJU]");
            acceptBtn.setColor(ChatColor.GREEN);
            acceptBtn.setBold(true);
            acceptBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade accept " + sender.getName()));
            acceptBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Klik untuk menerima permintaan trade dari " + sender.getName()).color(ChatColor.GREEN).create()));

            TextComponent space = new TextComponent("   ");

            TextComponent denyBtn = new TextComponent("[✖ TOLAK]");
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
            target.sendMessage("§7Ketik §a/trade accept " + sender.getName() + " §7untuk setuju, atau §c/trade deny " + sender.getName() + " §7untuk menolak.");
        }

        target.sendMessage("§8=======================================");
        return true;
    }


    public synchronized boolean acceptRequest(Player target, String senderName) {
        if (target == null) return false;
        Map<UUID, TradeRequest> requests = pendingRequests.get(target.getUniqueId());
        if (requests == null || requests.isEmpty()) {
            target.sendMessage("§cAnda tidak memiliki permintaan trade yang masuk!");
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
            target.sendMessage("§cPermintaan trade dari pemain tersebut tidak ditemukan atau sudah kedaluwarsa!");
            return false;
        }

        Player sender = Bukkit.getPlayer(matchingRequest.getSenderUuid());
        if (sender == null || !sender.isOnline()) {
            target.sendMessage("§cPemain yang mengajak trade sedang offline!");
            requests.remove(matchingRequest.getSenderUuid());
            return false;
        }

        if (isInTrade(target)) {
            target.sendMessage("§cAnda sedang berada dalam sesi trade lain!");
            return false;
        }

        if (isInTrade(sender)) {
            target.sendMessage("§cPemain §e" + sender.getName() + " §csedang berada dalam sesi trade lain!");
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
            target.sendMessage("§cAnda tidak memiliki permintaan trade yang masuk!");
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
            target.sendMessage("§cPermintaan trade dari pemain tersebut tidak ditemukan!");
            return false;
        }

        requests.remove(matchingRequest.getSenderUuid());
        target.sendMessage("§e[!] Anda telah menolak permintaan trade dari §6" + matchingRequest.getSenderName() + "§e.");

        Player sender = Bukkit.getPlayer(matchingRequest.getSenderUuid());
        if (sender != null && sender.isOnline()) {
            sender.sendMessage("§c[!] §e" + target.getName() + " §ctelah menolak permintaan trade Anda.");
        }
        return true;
    }

    public synchronized void startTradeSession(Player player1, Player player2) {
        TradeSession session = new TradeSession(plugin, player1, player2);
        activeSessions.put(player1.getUniqueId(), session);
        activeSessions.put(player2.getUniqueId(), session);

        player1.sendMessage("§a[✔] Sesi trade dengan §e" + player2.getName() + " §adimulai!");
        player2.sendMessage("§a[✔] Sesi trade dengan §e" + player1.getName() + " §adimulai!");

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
