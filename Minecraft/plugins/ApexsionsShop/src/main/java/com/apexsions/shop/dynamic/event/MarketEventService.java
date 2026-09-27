package com.apexsions.shop.dynamic.event;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.category.ShopItem;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

/**
 * Manages periodic Macroeconomic Events across the Apexsions Realm.
 * Simulates real-world dynamic economic shifts and player incentives.
 */
public class MarketEventService {

    private final ApexsionsShop plugin;
    private final Random random = new Random();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private MarketEvent activeEvent = MarketEvent.MARKET_EQUILIBRIUM;
    private long eventExpiresAt = 0L;
    private BukkitTask rotationTask;

    public MarketEventService(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (rotationTask != null) {
            rotationTask.cancel();
        }

        long intervalMinutes = plugin.getConfigManager().getMarketsConfig().getLong("economic-events.rotation-interval-minutes", 240L);
        long intervalTicks = Math.max(20L * 60L, intervalMinutes * 60L * 20L);

        // Initialize first random event
        rotateEvent(false);

        rotationTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> rotateEvent(true), intervalTicks, intervalTicks);
    }

    public void stop() {
        if (rotationTask != null) {
            rotationTask.cancel();
            rotationTask = null;
        }
    }

    /**
     * Rotates to a new economic event, broadcasts to all players, and plays ambient fanfare.
     */
    public void rotateEvent(boolean announce) {
        MarketEvent[] events = MarketEvent.values();
        MarketEvent nextEvent;
        do {
            nextEvent = events[random.nextInt(events.length)];
        } while (events.length > 1 && nextEvent == activeEvent);

        this.activeEvent = nextEvent;
        long durationMinutes = plugin.getConfigManager().getMarketsConfig().getLong("economic-events.rotation-interval-minutes", 240L);
        this.eventExpiresAt = System.currentTimeMillis() + (durationMinutes * 60L * 1000L);

        if (announce) {
            broadcastEventChange();
        }
    }

    /**
     * Broadcasts cinematic MiniMessage alert to realm.
     */
    public void broadcastEventChange() {
        String msg = "\n<gradient:#f1c40f:#e67e22><bold>[ PASAR APEXSIONS ]</bold></gradient> <white>Peristiwa Ekonomi Realm Telah Bergeser!</white>\n" +
                " <gradient:" + activeEvent.getPrimaryColor() + ":" + activeEvent.getSecondaryColor() + "><bold>❖ " + activeEvent.getDisplayName() + "</bold></gradient>\n" +
                " <gray>" + activeEvent.getDescription() + "</gray>\n" +
                " <dark_gray>Sisa Waktu: <yellow>" + getFormattedRemainingTime() + "</yellow> • Cek rincian di <gold>/shop</gold></dark_gray>\n";

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(miniMessage.deserialize(msg));
            online.playSound(online.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);
        }
    }

    public double getEventSellMultiplier(ShopItem item) {
        if (item == null || activeEvent == null) return 1.0;
        return activeEvent.getSellMultiplier(item.getCategory());
    }

    public double getEventBuyMultiplier(ShopItem item) {
        if (item == null || activeEvent == null) return 1.0;
        return activeEvent.getBuyMultiplier(item.getCategory());
    }

    public MarketEvent getActiveEvent() {
        return activeEvent;
    }

    public void setActiveEvent(MarketEvent event) {
        if (event != null) {
            this.activeEvent = event;
            broadcastEventChange();
        }
    }

    public long getRemainingTimeSeconds() {
        long diff = eventExpiresAt - System.currentTimeMillis();
        return Math.max(0, diff / 1000L);
    }

    public String getFormattedRemainingTime() {
        long totalSeconds = getRemainingTimeSeconds();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02dj %02dm %02dd", hours, minutes, seconds);
        } else {
            return String.format("%02dm %02dd", minutes, seconds);
        }
    }
}
