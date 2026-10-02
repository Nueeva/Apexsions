package com.apexsions.shop.dynamic;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.dynamic.event.MarketEvent;
import com.apexsions.shop.dynamic.event.MarketEventService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitTask;

/**
 * Periodically broadcasts the currently active market event.
 */
public class MarketBroadcastService {

    private final ApexsionsShop plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private BukkitTask broadcastTask;

    // M-14 (audit fix): daftar MARKET_TRENDS statis dengan klaim palsu
    // (mis. "Harga jual ore naik +20%", "harga beli makanan lebih murah -15%") DIHAPUS.
    // Broadcast lama tidak mengubah multiplier apapun — murni flavor text acak tiap
    // 20 menit yang bisa disalahpahami pemain sebagai info ekonomi nyata.
    // Broadcast ini sekarang mengumumkan event pasar AKTIF dari MarketEventService,
    // yang multiplier-nya benar-benar diterapkan di DynamicPriceCalculator
    // (calculateBuyPrice/calculateSellPrice), sehingga info yang diterima pemain
    // selalu nyata dan dapat ditindaklanjuti. Tidak ada sistem multiplier baru
    // yang dibuat — hanya pemakaian ulang data event yang sudah ada.

    public MarketBroadcastService(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (broadcastTask != null) {
            broadcastTask.cancel();
        }

        // Broadcast every 20 minutes (24000 ticks)
        long intervalTicks = 20 * 60 * 20L;
        broadcastTask = Bukkit.getScheduler().runTaskTimer(plugin, this::broadcastActiveEvent, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (broadcastTask != null) {
            broadcastTask.cancel();
            broadcastTask = null;
        }
    }

    /**
     * M-14 (audit fix): announces the REAL currently-active market event from
     * {@link MarketEventService} — name, description, and remaining time — instead of
     * the previous random flavor texts with fabricated price claims. The event's
     * sell multipliers are genuinely applied to prices, so players can act on this.
     */
    private void broadcastActiveEvent() {
        MarketEventService eventService = plugin.getMarketEventService();
        if (eventService == null) {
            return;
        }
        MarketEvent active = eventService.getActiveEvent();
        if (active == null) {
            return;
        }

        String msg = "<dark_gray>[<gradient:#f1c40f:#e67e22><bold>Pasar Kerajaan</bold></gradient>]</dark_gray> "
                + "<white>❖ <bold>" + active.getDisplayName() + "</bold></white>\n"
                + " <gray>" + active.getDescription() + "</gray>\n"
                + " <dark_gray>Sisa waktu: <yellow>" + eventService.getFormattedRemainingTime()
                + "</yellow> • Cek rincian di <gold>/shop</gold></dark_gray>";
        Bukkit.broadcast(miniMessage.deserialize(msg));
        for (var p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6f, 1.2f);
        }
    }
}
