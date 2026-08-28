package com.apex.shop.dynamic;

import com.apex.shop.ApexsionsShop;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

/**
 * Periodically broadcasts market fluctuations, commodity booms, and price trends.
 */
public class MarketBroadcastService {

    private final ApexsionsShop plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Random random = new Random();
    private BukkitTask broadcastTask;

    private static final String[] MARKET_TRENDS = {
            "<gold>📈 <bold>TREN PASAR:</bold> Permintaan hasil tambang di <yellow>Zenithar</yellow> sedang melonjak tinggi! Harga jual ore naik <green>+20%</green>!</gold>",
            "<aqua>🌧 <bold>PENGARUH CUACA:</bold> Musim hujan memicu kenaikan permintaan bibit pertanian di <yellow>Solterra</yellow>! Segera jual hasil panenmu!</aqua>",
            "<light_purple>✨ <bold>PASAR MISTIS:</bold> Permintaan mob drops langka di <yellow>Sylvamoor</yellow> meningkat drastis! Manfaatkan kesempatan emas ini!</light_purple>",
            "<green>🌾 <bold>PASOKAN MELIMPAH:</bold> Panen raya di dataran Solterra membuat harga beli makanan menjadi lebih murah <yellow>-15%</yellow>!</green>",
            "<red>⚔ <bold>EKONOMI PERANG:</bold> Persediaan ingot dan perlengkapan senjata di seluruh kerajaan sedang diburu para prajurit!</red>"
    };

    public MarketBroadcastService(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (broadcastTask != null) {
            broadcastTask.cancel();
        }

        // Broadcast every 20 minutes (24000 ticks)
        long intervalTicks = 20 * 60 * 20L;
        broadcastTask = Bukkit.getScheduler().runTaskTimer(plugin, this::broadcastRandomTrend, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (broadcastTask != null) {
            broadcastTask.cancel();
            broadcastTask = null;
        }
    }

    private void broadcastRandomTrend() {
        String msg = MARKET_TRENDS[random.nextInt(MARKET_TRENDS.length)];
        Bukkit.broadcast(miniMessage.deserialize("<dark_gray>[<gradient:#f1c40f:#e67e22><bold>Pasar Kerajaan</bold></gradient>]</dark_gray> " + msg));
        for (var p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6f, 1.2f);
        }
    }
}
