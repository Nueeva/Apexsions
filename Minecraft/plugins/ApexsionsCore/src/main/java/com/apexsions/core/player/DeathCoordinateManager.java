package com.apexsions.core.player;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player death location tracking, past NBT retrieval, compass pointing,
 * interactive chat cards, and BlueMap 3D web atlas integration.
 */
public class DeathCoordinateManager {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm;
    private final Map<UUID, DeathRecord> activeDeathRecords = new ConcurrentHashMap<>();

    public DeathCoordinateManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        this.mm = MiniMessage.miniMessage();
    }

    /**
     * Records a real-time death event.
     */
    public void recordDeath(@NotNull Player player, @Nullable PlayerDeathEvent event) {
        Location loc = player.getLocation();
        if (loc.getWorld() == null) {
            return;
        }

        String cause = "Kematian Tragis";
        if (event != null && event.deathMessage() != null) {
            cause = PlainTextComponentSerializer.plainText().serialize(event.deathMessage());
        } else if (player.getLastDamageCause() != null) {
            cause = player.getLastDamageCause().getCause().name().replace('_', ' ');
        }

        DeathRecord record = new DeathRecord(
                player.getUniqueId(),
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch(),
                cause,
                System.currentTimeMillis()
        );

        activeDeathRecords.put(player.getUniqueId(), record);
    }

    /**
     * Gets the latest death record for a player.
     * Checks active session cache first, then seamlessly falls back to vanilla
     * player NBT (getLastDeathLocation), guaranteeing past deaths are retrieved.
     */
    @Nullable
    public DeathRecord getLatestDeathRecord(@NotNull UUID uuid) {
        DeathRecord cached = activeDeathRecords.get(uuid);
        if (cached != null) {
            return cached;
        }

        // Fallback: Read native Paper / Minecraft 1.19+ player NBT data
        Location nbtLoc = null;
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            nbtLoc = online.getLastDeathLocation();
        } else {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            nbtLoc = offline.getLastDeathLocation();
        }

        if (nbtLoc != null && nbtLoc.getWorld() != null) {
            DeathRecord retroRecord = new DeathRecord(
                    uuid,
                    nbtLoc.getWorld().getName(),
                    nbtLoc.getX(),
                    nbtLoc.getY(),
                    nbtLoc.getZ(),
                    nbtLoc.getYaw(),
                    nbtLoc.getPitch(),
                    "Kematian Terdahulu (Tercatat di Data Pemain)",
                    0L
            );
            activeDeathRecords.put(uuid, retroRecord);
            return retroRecord;
        }

        return null;
    }

    /**
     * Gets raw Bukkit Location of latest death.
     */
    @Nullable
    public Location getDeathLocation(@NotNull UUID uuid) {
        DeathRecord record = getLatestDeathRecord(uuid);
        return record != null ? record.toLocation() : null;
    }

    /**
     * Sets the player's held compass to point towards the death coordinates.
     */
    public boolean pointCompassToDeath(@NotNull Player player, @Nullable DeathRecord record) {
        if (record == null) {
            record = getLatestDeathRecord(player.getUniqueId());
        }
        if (record == null) {
            player.sendMessage(mm.deserialize("<red>❌ Belum ada catatan kematian yang terekam untuk Anda.</red>"));
            return false;
        }

        Location loc = record.toLocation();
        if (loc == null || loc.getWorld() == null) {
            player.sendMessage(mm.deserialize("<red>❌ Dunia tempat kematian (" + record.worldName() + ") tidak ditemukan.</red>"));
            return false;
        }

        if (!player.getWorld().getName().equalsIgnoreCase(record.worldName())) {
            player.sendMessage(mm.deserialize("<gold>⚠ Perhatian:</gold> <gray>Titik kematian berada di dimensi <yellow>" + record.getDimensionDisplay() + "</yellow>, sedangkan Anda saat ini berada di <yellow>" + player.getWorld().getName() + "</yellow>.</gray>"));
        }

        player.setCompassTarget(loc);
        player.sendMessage(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>🧭 KOMPAS DIARAHKAN!</bold></gradient> <gray>Jarum kompas Anda kini mengarah ke titik kematian: <gold>X: " + (int) record.x() + ", Y: " + (int) record.y() + ", Z: " + (int) record.z() + "</gold>.</gray>"));
        return true;
    }

    /**
     * Sends the full, rich interactive death notification card to a player.
     */
    public void sendSelfDeathNotification(@NotNull Player player) {
        DeathRecord record = getLatestDeathRecord(player.getUniqueId());
        if (record == null) {
            player.sendMessage(mm.deserialize("<red>❌ Belum ada catatan lokasi kematian yang ditemukan.</red>"));
            return;
        }

        double distance = record.getDistance(player.getLocation());
        String distStr = distance >= 0 ? String.format(java.util.Locale.ROOT, "%,.1f", distance) + " blok" : "Dimensi Berbeda (" + record.getDimensionDisplay() + ")";
        String blueMapUrl = generateBlueMapUrl(record);

        Component header = mm.deserialize("<dark_gray>══════════════════════════════════════════════════</dark_gray>");
        Component title = mm.deserialize("<gradient:#e74c3c:#c0392b><bold>☠ KOORDINAT LOKASI KEMATIAN ANDA</bold></gradient>");
        Component worldLine = mm.deserialize("<gray>Dimensi   :</gray> <yellow>" + record.getDimensionDisplay() + "</yellow>");
        Component coordLine = mm.deserialize("<gray>Koordinat :</gray> <gold><bold>X: " + (int) record.x() + ", Y: " + (int) record.y() + ", Z: " + (int) record.z() + "</bold></gold>");
        Component distLine = mm.deserialize("<gray>Jarak     :</gray> <aqua>" + distStr + "</aqua>");
        Component timeLine = mm.deserialize("<gray>Waktu     :</gray> <white>" + record.getTimeAgoFormatted() + "</white>");
        Component causeLine = mm.deserialize("<gray>Penyebab  :</gray> <red>" + (record.deathCause() != null ? record.deathCause() : "Tidak diketahui") + "</red>");

        Component btnCompass = mm.deserialize("<gold><bold>[🧭 SET ARAH KOMPAS]</bold></gold>")
                .clickEvent(ClickEvent.runCommand("/deathcoords compass"))
                .hoverEvent(HoverEvent.showText(mm.deserialize("<yellow>Klik untuk mengarahkan jarum kompasmu ke koordinat ini</yellow>")));

        Component btnMap = mm.deserialize("<blue><bold>[🗺 BUKA PETA 3D]</bold></blue>")
                .clickEvent(ClickEvent.openUrl(blueMapUrl))
                .hoverEvent(HoverEvent.showText(mm.deserialize("<aqua>Buka visualisasi satelit BlueMap di browser: <white>" + blueMapUrl + "</white></aqua>")));

        Component actions = Component.text(" ").append(btnCompass).append(Component.text("   ")).append(btnMap);
        Component footer = mm.deserialize("<dark_gray>══════════════════════════════════════════════════</dark_gray>");

        player.sendMessage(Component.empty());
        player.sendMessage(header);
        player.sendMessage(title);
        player.sendMessage(worldLine);
        player.sendMessage(coordLine);
        player.sendMessage(distLine);
        player.sendMessage(timeLine);
        player.sendMessage(causeLine);
        player.sendMessage(Component.empty());
        player.sendMessage(actions);
        player.sendMessage(footer);
        player.sendMessage(Component.empty());
    }

    /**
     * Sends the staff / admin interactive inspection card for another player's death.
     */
    public void sendAdminDeathNotification(@NotNull Player admin, @NotNull OfflinePlayer target) {
        DeathRecord record = getLatestDeathRecord(target.getUniqueId());
        String targetName = target.getName() != null ? target.getName() : target.getUniqueId().toString();

        if (record == null) {
            admin.sendMessage(mm.deserialize("<red>❌ Tidak ada data kematian yang terekam untuk pemain <yellow>" + targetName + "</yellow>.</red>"));
            return;
        }

        double distance = record.getDistance(admin.getLocation());
        String distStr = distance >= 0 ? String.format(java.util.Locale.ROOT, "%,.1f", distance) + " blok" : "Dimensi Berbeda (" + record.getDimensionDisplay() + ")";
        String blueMapUrl = generateBlueMapUrl(record);

        Component header = mm.deserialize("<dark_gray>══════════════════════════════════════════════════</dark_gray>");
        Component title = mm.deserialize("<gradient:#e74c3c:#c0392b><bold>☠ INSPEKSI KOORDINAT KEMATIAN: " + targetName + "</bold></gradient>");
        Component worldLine = mm.deserialize("<gray>Dimensi   :</gray> <yellow>" + record.getDimensionDisplay() + "</yellow>");
        Component coordLine = mm.deserialize("<gray>Koordinat :</gray> <gold><bold>X: " + (int) record.x() + ", Y: " + (int) record.y() + ", Z: " + (int) record.z() + "</bold></gold>");
        Component distLine = mm.deserialize("<gray>Jarak     :</gray> <aqua>" + distStr + "</aqua>");
        Component timeLine = mm.deserialize("<gray>Waktu     :</gray> <white>" + record.getTimeAgoFormatted() + "</white>");
        Component causeLine = mm.deserialize("<gray>Penyebab  :</gray> <red>" + (record.deathCause() != null ? record.deathCause() : "Tidak diketahui") + "</red>");

        Component btnTp = mm.deserialize("<red><bold>[🚀 TELEPORT ADMIN]</bold></red>")
                .clickEvent(ClickEvent.runCommand("/deathcoords tp " + targetName))
                .hoverEvent(HoverEvent.showText(mm.deserialize("<red>Bypass teleportasi admin langsung ke koordinat kematian " + targetName + "</red>")));

        Component btnCompass = mm.deserialize("<gold><bold>[🧭 SET KOMPAS]</bold></gold>")
                .clickEvent(ClickEvent.runCommand("/deathcoords compass " + targetName))
                .hoverEvent(HoverEvent.showText(mm.deserialize("<yellow>Arahkan kompas admin ke titik ini</yellow>")));

        Component btnMap = mm.deserialize("<blue><bold>[🗺 BUKA PETA 3D]</bold></blue>")
                .clickEvent(ClickEvent.openUrl(blueMapUrl))
                .hoverEvent(HoverEvent.showText(mm.deserialize("<aqua>Buka satelit 3D BlueMap: <white>" + blueMapUrl + "</white></aqua>")));

        Component actions = Component.text(" ").append(btnTp).append(Component.text("  ")).append(btnCompass).append(Component.text("  ")).append(btnMap);
        Component footer = mm.deserialize("<dark_gray>══════════════════════════════════════════════════</dark_gray>");

        admin.sendMessage(Component.empty());
        admin.sendMessage(header);
        admin.sendMessage(title);
        admin.sendMessage(worldLine);
        admin.sendMessage(coordLine);
        admin.sendMessage(distLine);
        admin.sendMessage(timeLine);
        admin.sendMessage(causeLine);
        admin.sendMessage(Component.empty());
        admin.sendMessage(actions);
        admin.sendMessage(footer);
        admin.sendMessage(Component.empty());
    }

    /**
     * Generates a BlueMap satellite URL for a given death record.
     */
    @NotNull
    public String generateBlueMapUrl(@NotNull DeathRecord record) {
        String baseUrl = plugin.getConfig().getString("death-coords.bluemap-base-url", "http://apexsions.com:32076/");
        baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        if (baseUrl.contains("#")) {
            baseUrl = baseUrl.split("#")[0];
            baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        }

        int x = (int) record.x();
        int z = (int) record.z();
        return baseUrl + "/#" + record.worldName() + ":" + x + ":100:" + z + ":500:0:0:0:0:perspective";
    }

    public boolean isNotifyOnRespawn() {
        return plugin.getConfig().getBoolean("death-coords.notify-on-respawn", true);
    }

    public long getNotifyDelayTicks() {
        return plugin.getConfig().getLong("death-coords.notify-delay-ticks", 20L);
    }
}
