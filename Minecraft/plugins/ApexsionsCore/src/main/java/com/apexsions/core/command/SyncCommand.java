package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.integration.web.WebBridgeService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Command executor for /sync.
 * Immediately synchronizes player's in-game stats, civilization level, balances, and BattlePass to web platform.
 */
public class SyncCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 5000; // 5 seconds cooldown

    public SyncCommand(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Perintah ini hanya dapat dijalankan oleh pemain di dalam game.");
            return true;
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long lastSync = cooldowns.getOrDefault(uuid, 0L);

        if (now - lastSync < COOLDOWN_MS) {
            long remainingSec = ((COOLDOWN_MS - (now - lastSync)) / 1000) + 1;
            player.sendMessage(miniMessage.deserialize("<red>Harap tunggu <yellow>" + remainingSec + "</yellow> detik sebelum melakukan sinkronisasi ulang.</red>"));
            return true;
        }

        cooldowns.put(uuid, now);

        WebBridgeService bridge = plugin.getWebBridgeService();
        if (bridge == null) {
            player.sendMessage(miniMessage.deserialize("<red>Layanan Web Bridge saat ini tidak aktif di server.</red>"));
            return true;
        }

        player.sendMessage(miniMessage.deserialize("<gradient:#f39c12:#f1c40f><bold>APEXSIONS SYNC</bold></gradient> <dark_gray>»</dark_gray> <gray>Menyinkronkan data profil dan BattlePass Anda ke portal web...</gray>"));

        bridge.syncPlayerAsync(player);

        player.sendMessage(miniMessage.deserialize("<gradient:#f39c12:#f1c40f><bold>APEXSIONS SYNC</bold></gradient> <dark_gray>»</dark_gray> <green>Sinkronisasi berhasil! Data akun telah dikirim ke portal web:</green>"));
        
        // Detailed feedback so player can confirm values
        try {
            int civLvl = plugin.getPlayerDataService().getCached(uuid).map(com.apexsions.core.player.PlayerData::getLevel).orElse(1);
            long civXp = plugin.getPlayerDataService().getCached(uuid).map(com.apexsions.core.player.PlayerData::getXp).orElse(0L);
            long reqCivXp = plugin.getLevelManager() != null ? plugin.getLevelManager().getRequiredXpForNextLevel(civLvl) : 500;
            
            player.sendMessage(miniMessage.deserialize(" <dark_gray>•</dark_gray> <gray>Level Peradaban:</gray> <gold>Lv. " + civLvl + "</gold> <dark_gray>(" + civXp + "/" + reqCivXp + " XP)</dark_gray>"));
        } catch (Throwable ignored) {}

        try {
            if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("ApexsionsBattlepass")) {
                Class<?> bpProviderClass = Class.forName("com.apexsions.battlepass.api.ApexsionsBattlepassProvider");
                Object bpApi = bpProviderClass.getMethod("get").invoke(null);
                if (bpApi != null) {
                    int bpTier = (int) bpApi.getClass().getMethod("getPlayerTier", UUID.class).invoke(bpApi, uuid);
                    int bpXp = (int) bpApi.getClass().getMethod("getPlayerXp", UUID.class).invoke(bpApi, uuid);
                    boolean isPrem = (boolean) bpApi.getClass().getMethod("hasPremiumPass", UUID.class).invoke(bpApi, uuid);
                    String badge = isPrem ? "<gold>[PREMIUM PASS]</gold>" : "<gray>[FREE PASS]</gray>";
                    player.sendMessage(miniMessage.deserialize(" <dark_gray>•</dark_gray> <gray>BattlePass:</gray> <yellow>Tier " + bpTier + "</yellow> <dark_gray>(" + bpXp + " XP)</dark_gray> " + badge));
                }
            }
        } catch (Throwable ignored) {}

        player.sendMessage(miniMessage.deserialize(" <dark_gray>•</dark_gray> <gray>Cek profil web:</gray> <click:open_url:'http://web.apexsions.my.id/profile'><underlined><aqua>web.apexsions.my.id/profile</aqua></underlined></click>"));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
