package com.apexsions.core.bounty;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages the player bounty economy: contributions, claims, refunds, and
 * bounty-hunter statistics. Economy access is reflective so ApexsionsEconomy
 * stays an optional soft-dependency, with a Vault fallback for online players.
 */
public class BountyManager {

    public static final String CURRENCY = "rupiah";
    private static final UUID ADMIN_UUID = new UUID(0L, 0L);

    private final ApexsionsCorePlugin plugin;
    private final BountyRepository repository;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, TargetBounty> active = new ConcurrentHashMap<>();
    private final Map<UUID, Long> sessionStart = new ConcurrentHashMap<>();

    private volatile Object economyApi;

    public BountyManager(@NotNull ApexsionsCorePlugin plugin, @NotNull BountyRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    // --- Configuration ---

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("bounty.enabled", true);
    }

    public double getMinAmount() {
        return plugin.getConfig().getDouble("bounty.min-amount", 1000.0);
    }

    public double getTaxPercent() {
        return plugin.getConfig().getDouble("bounty.tax-percent", 5.0);
    }

    public boolean blockSameKingdom() {
        return plugin.getConfig().getBoolean("bounty.block-same-kingdom", true);
    }

    public boolean blockSameIp() {
        return plugin.getConfig().getBoolean("bounty.block-same-ip", true);
    }

    public long getMinOnlineTimeSeconds() {
        return plugin.getConfig().getLong("bounty.min-online-time-seconds", 300L);
    }

    public boolean broadcastClaim() {
        return plugin.getConfig().getBoolean("bounty.broadcast-claim", true);
    }

    public int getListLimit() {
        return Math.max(1, plugin.getConfig().getInt("bounty.list-limit", 10));
    }

    // --- Lifecycle ---

    public void start() {
        repository.loadActiveBounties().thenAccept(rows -> {
            // Merge into the live map so bounties placed during the async load survive.
            for (Bounty b : rows) {
                if (!b.active()) continue;
                TargetBounty tb = active.computeIfAbsent(b.targetUuid(),
                        k -> new TargetBounty(b.targetUuid(), b.targetName()));
                tb.setTargetName(b.targetName());
                tb.add(b.placerUuid(), b.placerName(), b.amount());
            }
            if (!rows.isEmpty()) {
                plugin.getLogger().info("Loaded " + rows.size() + " active bounty contribution(s).");
            }
        });
    }

    public void onJoin(@NotNull Player player) {
        sessionStart.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void onQuit(@NotNull Player player) {
        sessionStart.remove(player.getUniqueId());
    }

    // --- Queries ---

    @Nullable
    public TargetBounty getBounty(@NotNull UUID targetUuid) {
        return active.get(targetUuid);
    }

    @NotNull
    public List<TargetBounty> getTopTargets(int limit) {
        List<TargetBounty> list = new ArrayList<>(active.values());
        list.sort(Comparator.comparingDouble(TargetBounty::total).reversed());
        if (list.size() > limit) {
            return new ArrayList<>(list.subList(0, limit));
        }
        return list;
    }

    // --- Placement ---

    /**
     * Places (or extends) a bounty. Withdraws the amount from the placer first.
     */
    public boolean placeBounty(@NotNull Player placer, @NotNull OfflinePlayer target, double amount) {
        if (!isEnabled()) {
            placer.sendMessage(mm.deserialize("<red>Sistem bounty saat ini dinonaktifkan.</red>"));
            return false;
        }
        if (!economyAvailable()) {
            placer.sendMessage(mm.deserialize("<red>Ekonomi tidak tersedia; bounty tidak dapat dipasang.</red>"));
            return false;
        }
        if (target.getUniqueId().equals(placer.getUniqueId())) {
            placer.sendMessage(mm.deserialize("<red>Anda tidak dapat memasang bounty untuk diri sendiri.</red>"));
            return false;
        }
        if (amount < getMinAmount()) {
            placer.sendMessage(mm.deserialize("<red>Jumlah minimum bounty adalah <gold>" + format(getMinAmount()) + "</gold>.</red>"));
            return false;
        }
        if (!withdraw(placer.getUniqueId(), amount)) {
            placer.sendMessage(mm.deserialize("<red>Saldo Anda tidak cukup untuk memasang bounty sebesar <gold>" + format(amount) + "</gold>.</red>"));
            return false;
        }

        String targetName = target.getName() != null ? target.getName() : target.getUniqueId().toString();
        Bounty row = new Bounty(UUID.randomUUID().toString(), target.getUniqueId(), targetName,
                placer.getUniqueId(), placer.getName(), amount, true, null, 0L);
        repository.insertBounty(row);

        TargetBounty tb = active.computeIfAbsent(target.getUniqueId(),
                k -> new TargetBounty(target.getUniqueId(), targetName));
        tb.setTargetName(targetName);
        tb.add(placer.getUniqueId(), placer.getName(), amount);

        placer.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>BOUNTY DIPASANG!</bold></gradient> <gray>Anda memasang <gold>"
                + format(amount) + "</gold> untuk <yellow>" + targetName + "</yellow>. Total bounty: <gold>" + format(tb.total()) + "</gold>.</gray>"));
        return true;
    }

    /**
     * Admin-set bounty (no withdrawal, for events).
     */
    public void setAdminBounty(@NotNull OfflinePlayer target, double amount) {
        String targetName = target.getName() != null ? target.getName() : target.getUniqueId().toString();
        Bounty row = new Bounty(UUID.randomUUID().toString(), target.getUniqueId(), targetName,
                ADMIN_UUID, "ADMIN", amount, true, null, 0L);
        repository.insertBounty(row);

        TargetBounty tb = active.computeIfAbsent(target.getUniqueId(),
                k -> new TargetBounty(target.getUniqueId(), targetName));
        tb.setTargetName(targetName);
        tb.add(ADMIN_UUID, "ADMIN", amount);
    }

    // --- Claims ---

    /**
     * Handles a PvP kill: pays out the victim's bounty to the killer when all guards pass.
     */
    public void handleKill(@NotNull Player victim, @NotNull Player killer) {
        if (!isEnabled() || killer.equals(victim)) {
            return;
        }
        TargetBounty tb = active.get(victim.getUniqueId());
        if (tb == null || tb.total() <= 0) {
            return;
        }
        if (blockSameIp() && sameIp(victim, killer)) {
            return;
        }
        if (blockSameKingdom() && sameKingdom(victim.getUniqueId(), killer.getUniqueId())) {
            return;
        }
        long minOnline = getMinOnlineTimeSeconds();
        if (minOnline > 0) {
            Long joinedAt = sessionStart.get(killer.getUniqueId());
            if (joinedAt != null && (System.currentTimeMillis() - joinedAt) / 1000L < minOnline) {
                return;
            }
        }

        double gross = tb.total();
        double tax = Math.max(0.0, gross * getTaxPercent() / 100.0);
        double net = Math.max(0.0, gross - tax);

        if (economyAvailable() && net > 0) {
            deposit(killer.getUniqueId(), net);
        }
        repository.deactivateTarget(victim.getUniqueId(), killer.getUniqueId());
        repository.addHunterClaim(killer.getUniqueId(), killer.getName(), net);
        active.remove(victim.getUniqueId());

        killer.sendMessage(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>BOUNTY DIKLAIM!</bold></gradient> <gray>Anda mendapat <gold>"
                + format(net) + "</gold> dari bounty <yellow>" + tb.getTargetName() + "</yellow>.</gray>"));
        if (broadcastClaim()) {
            Component msg = mm.deserialize("<gradient:#e74c3c:#f1c40f><bold>☠ BOUNTY DIKLAIM!</bold></gradient> <yellow>" + killer.getName()
                    + "</yellow> <gray>memburu</gray> <red>" + tb.getTargetName() + "</red> <gray>dan mengantongi</gray> <gold>" + format(net) + "</gold>!</gray>");
            Bukkit.broadcast(msg);
        }
        if (plugin.getWebBridgeService() != null) {
            plugin.getWebBridgeService().publishEventAsync("BOUNTY_CLAIMED", killer.getName(), tb.getTargetName(),
                    killer.getName() + " memburu " + tb.getTargetName() + " dan mengantongi " + format(net));
        }
    }

    /**
     * Refunds all contributors and removes a target's bounty (admin action).
     */
    public boolean clearBounty(@NotNull UUID targetUuid) {
        TargetBounty tb = active.remove(targetUuid);
        if (tb == null) {
            return false;
        }
        if (economyAvailable()) {
            for (TargetBounty.Contributor c : tb.contributors()) {
                deposit(c.uuid(), c.amount());
            }
        }
        repository.deactivateTarget(targetUuid, null);
        return true;
    }

    // --- Economy bridge ---

    public boolean economyAvailable() {
        return resolveEconomy() != null || vaultAvailable();
    }

    @Nullable
    private Object resolveEconomy() {
        if (economyApi != null) {
            return economyApi;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("ApexsionsEconomy")) {
            return null;
        }
        try {
            Class<?> providerClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyProvider");
            Object available = providerClass.getMethod("isAvailable").invoke(null);
            if (Boolean.TRUE.equals(available)) {
                economyApi = providerClass.getMethod("get").invoke(null);
            }
        } catch (Throwable t) {
            plugin.getLogger().log(Level.FINE, "ApexsionsEconomy API unavailable for bounty", t);
        }
        return economyApi;
    }

    private boolean vaultAvailable() {
        return plugin.getVaultHook() != null && plugin.getVaultHook().hasEconomy();
    }

    public double getBalance(@NotNull UUID uuid) {
        Object api = resolveEconomy();
        if (api != null) {
            try {
                Class<?> apiClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyAPI");
                Method m = apiClass.getMethod("getBalance", UUID.class, String.class);
                return ((Number) m.invoke(api, uuid, CURRENCY)).doubleValue();
            } catch (Throwable ignored) {
                // fall through to Vault
            }
        }
        Player online = Bukkit.getPlayer(uuid);
        if (online != null && vaultAvailable()) {
            return plugin.getVaultHook().getBalance(online);
        }
        return 0.0;
    }

    public boolean withdraw(@NotNull UUID uuid, double amount) {
        Object api = resolveEconomy();
        if (api != null) {
            try {
                Class<?> apiClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyAPI");
                Method m = apiClass.getMethod("withdraw", UUID.class, String.class, double.class);
                return Boolean.TRUE.equals(m.invoke(api, uuid, CURRENCY, amount));
            } catch (Throwable ignored) {
                // fall through to Vault
            }
        }
        Player online = Bukkit.getPlayer(uuid);
        if (online != null && vaultAvailable() && getBalance(uuid) >= amount) {
            plugin.getVaultHook().withdraw(online, amount);
            return true;
        }
        return false;
    }

    public void deposit(@NotNull UUID uuid, double amount) {
        if (amount <= 0) return;
        Object api = resolveEconomy();
        if (api != null) {
            try {
                Class<?> apiClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyAPI");
                Method m = apiClass.getMethod("deposit", UUID.class, String.class, double.class);
                m.invoke(api, uuid, CURRENCY, amount);
                return;
            } catch (Throwable ignored) {
                // fall through to Vault
            }
        }
        Player online = Bukkit.getPlayer(uuid);
        if (online != null && vaultAvailable()) {
            plugin.getVaultHook().deposit(online, amount);
        }
    }

    @NotNull
    public String format(double amount) {
        Object api = resolveEconomy();
        if (api != null) {
            try {
                Class<?> apiClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyAPI");
                Method m = apiClass.getMethod("format", double.class, String.class);
                Object out = m.invoke(api, amount, CURRENCY);
                if (out != null) {
                    return out.toString();
                }
            } catch (Throwable ignored) {
                // fall through
            }
        }
        return "Rp" + String.format(java.util.Locale.ROOT, "%,.0f", amount);
    }

    // --- Anti-abuse helpers ---

    private boolean sameIp(@NotNull Player a, @NotNull Player b) {
        try {
            if (a.getAddress() == null || b.getAddress() == null) return false;
            return a.getAddress().getAddress().getHostAddress()
                    .equals(b.getAddress().getAddress().getHostAddress());
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean sameKingdom(@NotNull UUID a, @NotNull UUID b) {
        var service = plugin.getPlayerDataService();
        if (service == null) {
            return false;
        }
        return service.getCached(a).flatMap(d1 -> service.getCached(b).map(d2 -> {
            UUID r1 = d1.getRegionId();
            return r1 != null && r1.equals(d2.getRegionId());
        })).orElse(false);
    }

    // --- Leaderboards (async) ---

    public void sendHunterLeaderboard(@NotNull Player player) {
        repository.loadHunterLeaderboard(getListLimit()).thenAccept(list ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(mm.deserialize("<dark_gray>══════════════════════════════════</dark_gray>"));
                    player.sendMessage(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>⚔ PEMBURU BOUNTY TERHEBAT</bold></gradient>"));
                    if (list.isEmpty()) {
                        player.sendMessage(mm.deserialize("<gray>Belum ada bounty yang diklaim.</gray>"));
                    } else {
                        int rank = 1;
                        for (BountyRepository.HunterStat stat : list) {
                            player.sendMessage(mm.deserialize("<yellow>#" + rank + "</yellow> <white>" + stat.name()
                                    + "</white> <dark_gray>|</dark_gray> <gold>" + format(stat.totalClaimed())
                                    + "</gold> <gray>(" + stat.kills() + " kill)</gray>"));
                            rank++;
                        }
                    }
                    player.sendMessage(mm.deserialize("<dark_gray>══════════════════════════════════</dark_gray>"));
                }));
    }
}
