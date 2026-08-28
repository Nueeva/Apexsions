package com.apexsions.core.war;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.region.Region;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.Optional;

/**
 * Manages active Kingdom Wars, declaring hostilities between realms,
 * and enforcing war-state teleportation & gameplay restrictions.
 */
public class WarManager {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private boolean warActive = false;
    private Region kingdom1;
    private Region kingdom2;
    private long warEndTime = 0L;
    private BukkitTask warTimerTask;

    public WarManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isWarActive() {
        if (!warActive) return false;
        if (System.currentTimeMillis() > warEndTime) {
            stopWar();
            return false;
        }
        return true;
    }

    public boolean isWarActiveBetween(Region r1, Region r2) {
        if (!isWarActive() || r1 == null || r2 == null) return false;
        return (r1.equals(kingdom1) && r2.equals(kingdom2)) || (r1.equals(kingdom2) && r2.equals(kingdom1));
    }

    public boolean isWarActiveInTerritory(Region territory) {
        if (!isWarActive() || territory == null) return false;
        return territory.equals(kingdom1) || territory.equals(kingdom2);
    }

    public void startWar(Region k1, Region k2, long durationMinutes) {
        if (k1 == null || k2 == null || k1.equals(k2)) {
            return;
        }

        this.warActive = true;
        this.kingdom1 = k1;
        this.kingdom2 = k2;
        this.warEndTime = System.currentTimeMillis() + (durationMinutes * 60_000L);

        // Broadcast to all players
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f);
            p.sendMessage(miniMessage.deserialize("<dark_red><bold>====================================================</bold></dark_red>"));
            p.sendMessage(miniMessage.deserialize("<red>⚔ <bold>PERANG KERAJAAN DIMULAI!</bold> ⚔</red>"));
            p.sendMessage(miniMessage.deserialize("<gray>Deklarasi perang antara <yellow>" + k1.getDisplayName() + "</yellow> dan <yellow>" + k2.getDisplayName() + "</yellow>!</gray>"));
            p.sendMessage(miniMessage.deserialize("<gold>Durasi Perang: <white>" + durationMinutes + " Menit</white></gold>"));
            p.sendMessage(miniMessage.deserialize("<red>Seluruh fitur teleportasi di wilayah perang dinonaktifkan!</red>"));
            p.sendMessage(miniMessage.deserialize("<dark_red><bold>====================================================</bold></dark_red>"));

            Title title = Title.title(
                    miniMessage.deserialize("<dark_red><bold>⚔ WAR DECLARED! ⚔</bold></dark_red>"),
                    miniMessage.deserialize("<yellow>" + k1.getKey() + "</yellow> <red>VS</red> <aqua>" + k2.getKey() + "</aqua>"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(1000))
            );
            p.showTitle(title);
        }

        if (warTimerTask != null) {
            warTimerTask.cancel();
        }

        warTimerTask = Bukkit.getScheduler().runTaskLater(plugin, this::stopWar, durationMinutes * 60 * 20L);
    }

    public void stopWar() {
        if (!warActive) return;

        this.warActive = false;
        if (warTimerTask != null) {
            warTimerTask.cancel();
            warTimerTask = null;
        }

        String k1Name = kingdom1 != null ? kingdom1.getDisplayName() : "Kerajaan 1";
        String k2Name = kingdom2 != null ? kingdom2.getDisplayName() : "Kerajaan 2";

        this.kingdom1 = null;
        this.kingdom2 = null;
        this.warEndTime = 0L;

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            p.sendMessage(miniMessage.deserialize("<green>☮ <bold>PERANG TELAH BERAKHIR!</bold> Gencatan senjata antara <yellow>" + k1Name + "</yellow> dan <yellow>" + k2Name + "</yellow> telah berlaku.</green>"));
        }
    }

    public Optional<Region> getKingdom1() {
        return Optional.ofNullable(kingdom1);
    }

    public Optional<Region> getKingdom2() {
        return Optional.ofNullable(kingdom2);
    }

    public long getRemainingSeconds() {
        if (!isWarActive()) return 0L;
        long diff = warEndTime - System.currentTimeMillis();
        return Math.max(0L, (diff + 999) / 1000);
    }
}
