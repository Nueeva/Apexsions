package com.apexsions.core.grave;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.DeathRecord;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages player graves: virtual item storage, visible hologram + interaction
 * markers, timed expiry, and safe release of items without data loss.
 */
public class GraveManager {

    private final ApexsionsCorePlugin plugin;
    private final GraveRepository repository;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final NamespacedKey graveKey;

    private final Map<String, GraveRecord> activeGraves = new ConcurrentHashMap<>();
    private final Map<UUID, String> ownerIndex = new ConcurrentHashMap<>();
    private final Map<String, UUID> textDisplayIds = new ConcurrentHashMap<>();
    private final Map<String, UUID> interactionIds = new ConcurrentHashMap<>();

    private BukkitTask expiryTask;

    public GraveManager(@NotNull ApexsionsCorePlugin plugin, @NotNull GraveRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
        this.graveKey = new NamespacedKey(plugin, "grave_id");
    }

    public NamespacedKey getGraveKey() {
        return graveKey;
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("grave.enabled", true);
    }

    public long getDurationMillis() {
        return Math.max(0L, plugin.getConfig().getLong("grave.duration-seconds", 1800L)) * 1000L;
    }

    public double getInteractDistance() {
        return plugin.getConfig().getDouble("grave.interact-distance", 3.5);
    }

    public boolean allowKingdomMembers() {
        return plugin.getConfig().getBoolean("grave.allow-kingdom-members", false);
    }

    public boolean autoCompassOnRespawn() {
        return plugin.getConfig().getBoolean("grave.auto-compass-on-respawn", true);
    }

    /**
     * Loads persisted active graves and re-spawns their markers on the main thread.
     */
    public void start() {
        startExpiryTask();
        repository.loadActive().thenAccept(loaded -> Bukkit.getScheduler().runTask(plugin, () -> {
            for (GraveRecord grave : loaded) {
                if (grave.isExpired()) {
                    releaseGrave(grave, true);
                    repository.markCollected(grave.getId());
                } else {
                    activeGraves.put(grave.getId(), grave);
                    ownerIndex.put(grave.getOwnerUuid(), grave.getId());
                    spawnMarker(grave);
                }
            }
            if (!loaded.isEmpty()) {
                plugin.getLogger().info("Loaded " + loaded.size() + " active grave(s) from database.");
            }
        }));
    }

    private void startExpiryTask() {
        if (expiryTask != null) {
            expiryTask.cancel();
        }
        expiryTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (GraveRecord grave : new ArrayList<>(activeGraves.values())) {
                if (grave.isExpired()) {
                    releaseGrave(grave, true);
                    repository.markCollected(grave.getId());
                } else {
                    updateHologram(grave);
                }
            }
        }, 20L, 20L);
    }

    /**
     * Called from the death listener after vanilla drops have been cleared.
     */
    public void createGrave(@NotNull Player player, @NotNull Location loc, @NotNull List<ItemStack> items,
                            int xp, @Nullable String cause) {
        if (!isEnabled()) {
            return;
        }
        if (items.isEmpty() && xp <= 0) {
            return;
        }

        // Release any pre-existing active grave for this owner first (no data loss).
        releaseOwnedGraveSilently(player.getUniqueId());

        GraveRecord grave = GraveRecord.create(player.getUniqueId(), player.getName(), loc, items, xp, cause, getDurationMillis());
        activeGraves.put(grave.getId(), grave);
        ownerIndex.put(grave.getOwnerUuid(), grave.getId());
        repository.save(grave);

        spawnMarker(grave);

        player.sendMessage(mm.deserialize(
                "<gradient:#e74c3c:#c0392b><bold>☠ NISAN TERBENTUK!</bold></gradient> <gray>Item & XP Anda tersimpan aman di nisan.</gray>"));
        player.sendMessage(mm.deserialize(
                "<gray>Lokasi: <gold>X: " + (int) grave.getX() + ", Y: " + (int) grave.getY() + ", Z: " + (int) grave.getZ()
                        + "</gold> <dark_gray>|</dark_gray> <yellow>Ambil dengan tap nisan atau <click:run_command:'/grave'><white><bold>[AMBIL NISAN]</bold></white></click></yellow></gray>"));
        player.sendMessage(mm.deserialize(
                "<gray>Nisan bertahan selama <aqua>" + grave.remainingFormatted() + "</aqua>. Gunakan <gold>/grave compass</gold> untuk navigasi.</gray>"));
    }

    private void spawnMarker(GraveRecord grave) {
        Location loc = grave.toLocation();
        if (loc == null || loc.getWorld() == null) {
            return;
        }
        World world = loc.getWorld();
        try {
            TextDisplay display = world.spawn(loc.clone().add(0, 1.1, 0), TextDisplay.class, td -> {
                td.text(buildHologram(grave));
                td.setBillboard(Display.Billboard.CENTER);
                td.setInvulnerable(true);
                td.setPersistent(false);
                td.setGravity(false);
                td.getPersistentDataContainer().set(graveKey, PersistentDataType.STRING, grave.getId());
            });
            textDisplayIds.put(grave.getId(), display.getUniqueId());
        } catch (Throwable t) {
            plugin.getLogger().log(Level.FINE, "Could not spawn grave hologram", t);
        }

        try {
            Interaction interaction = world.spawn(loc.clone(), Interaction.class, i -> {
                i.setInteractionWidth(1.0f);
                i.setInteractionHeight(1.0f);
                i.setResponsive(true);
                i.setInvulnerable(true);
                i.setPersistent(false);
                i.setGravity(false);
                i.getPersistentDataContainer().set(graveKey, PersistentDataType.STRING, grave.getId());
            });
            interactionIds.put(grave.getId(), interaction.getUniqueId());
        } catch (Throwable t) {
            plugin.getLogger().log(Level.FINE, "Interaction entity unavailable; /grave fallback active", t);
        }
    }

    private net.kyori.adventure.text.Component buildHologram(GraveRecord grave) {
        return mm.deserialize("<gradient:#e74c3c:#c0392b><bold>☠ NISAN " + grave.getOwnerName() + "</bold></gradient>\n"
                + "<gray>Berakhir dalam <aqua>" + grave.remainingFormatted() + "</aqua></gray>\n"
                + "<yellow><bold>[TAP / KLIK UNTUK AMBIL]</bold></yellow>");
    }

    private void updateHologram(GraveRecord grave) {
        UUID id = textDisplayIds.get(grave.getId());
        if (id == null) {
            return;
        }
        Entity entity = Bukkit.getEntity(id);
        if (entity instanceof TextDisplay display && display.isValid()) {
            display.text(buildHologram(grave));
        }
    }

    public void removeMarkers(String graveId) {
        UUID textId = textDisplayIds.remove(graveId);
        if (textId != null) {
            Entity e = Bukkit.getEntity(textId);
            if (e != null) {
                e.remove();
            }
        }
        UUID interactionId = interactionIds.remove(graveId);
        if (interactionId != null) {
            Entity e = Bukkit.getEntity(interactionId);
            if (e != null) {
                e.remove();
            }
        }
    }

    @Nullable
    public GraveRecord getGraveById(@Nullable String id) {
        return id != null ? activeGraves.get(id) : null;
    }

    @Nullable
    public GraveRecord getActiveGrave(@NotNull UUID owner) {
        String id = ownerIndex.get(owner);
        return id != null ? activeGraves.get(id) : null;
    }

    @NotNull
    public List<GraveRecord> getGraves(@NotNull UUID owner) {
        List<GraveRecord> list = new ArrayList<>();
        GraveRecord own = getActiveGrave(owner);
        if (own != null) {
            list.add(own);
        }
        return list;
    }

    @NotNull
    public Collection<GraveRecord> getActiveGraves() {
        return activeGraves.values();
    }

    @Nullable
    public GraveRecord findNearest(@NotNull Player player, double maxDistance) {
        GraveRecord best = null;
        double bestDist = maxDistance;
        for (GraveRecord grave : activeGraves.values()) {
            double dist = grave.distanceTo(player.getLocation());
            if (dist >= 0 && dist <= bestDist) {
                bestDist = dist;
                best = grave;
            }
        }
        if (best != null) {
            return best;
        }
        return null;
    }

    /**
     * Determines whether the collector is permitted to loot the grave.
     */
    public boolean canCollect(@NotNull Player player, @NotNull GraveRecord grave) {
        if (player.getUniqueId().equals(grave.getOwnerUuid()) || player.hasPermission("apexsions.grave.admin")) {
            return true;
        }
        if (!allowKingdomMembers()) {
            return false;
        }
        return sameKingdom(player.getUniqueId(), grave.getOwnerUuid());
    }

    private boolean sameKingdom(UUID a, UUID b) {
        var service = plugin.getPlayerDataService();
        if (service == null) {
            return false;
        }
        return service.getCached(a).flatMap(d1 -> service.getCached(b).map(d2 -> {
            UUID r1 = d1.getRegionId();
            return r1 != null && r1.equals(d2.getRegionId());
        })).orElse(false);
    }

    /**
     * Collects items from a grave for the given player, returning true when successful.
     */
    public boolean collect(@NotNull Player player, @NotNull GraveRecord grave, boolean bypassDistance) {
        if (grave.isCollected() || !activeGraves.containsKey(grave.getId())) {
            player.sendMessage(mm.deserialize("<red> Nisan tersebut sudah tidak ada.</red>"));
            return false;
        }
        if (!canCollect(player, grave)) {
            player.sendMessage(mm.deserialize("<red>❌ Nisan ini bukan milik Anda.</red>"));
            return false;
        }
        if (!bypassDistance && grave.distanceTo(player.getLocation()) > getInteractDistance()) {
            player.sendMessage(mm.deserialize("<red>❌ Anda terlalu jauh dari nisan. Dekati lokasi terlebih dahulu.</red>"));
            return false;
        }

        giveItems(player, grave.getItems());
        if (grave.getXp() > 0) {
            player.giveExp(grave.getXp());
            grave.setXp(0);
        }
        grave.setCollected(true);
        activeGraves.remove(grave.getId());
        ownerIndex.remove(grave.getOwnerUuid(), grave.getId());
        removeMarkers(grave.getId());
        repository.markCollected(grave.getId());

        player.sendMessage(mm.deserialize(
                "<gradient:#2ecc71:#27ae60><bold>✔ NISAN DIAMBIL!</bold></gradient> <gray>Seluruh item & XP telah dikembalikan.</gray>"));
        return true;
    }

    private void giveItems(Player player, List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(items.toArray(new ItemStack[0]));
        if (!leftovers.isEmpty()) {
            Location dropAt = player.getLocation();
            World world = dropAt.getWorld();
            if (world != null) {
                for (ItemStack leftover : leftovers.values()) {
                    world.dropItemNaturally(dropAt, leftover);
                }
            }
        }
    }

    /**
     * Releases a grave without a collector: gives to owner if online, otherwise
     * drops the items at the grave location. Never silently discards items.
     */
    public void releaseGrave(@NotNull GraveRecord grave, boolean removeEntities) {
        Player owner = Bukkit.getPlayer(grave.getOwnerUuid());
        List<ItemStack> items = grave.getItems();
        if (owner != null && owner.isOnline()) {
            giveItems(owner, items);
            if (grave.getXp() > 0) {
                owner.giveExp(grave.getXp());
            }
            owner.sendMessage(mm.deserialize("<gray>⚠ Nisan Anda telah dilepas dan seluruh isinya dikembalikan ke inventori.</gray>"));
        } else {
            Location loc = grave.toLocation();
            if (loc != null && loc.getWorld() != null) {
                for (ItemStack item : items) {
                    loc.getWorld().dropItemNaturally(loc, item);
                }
                if (grave.getXp() > 0) {
                    loc.getWorld().spawn(loc, org.bukkit.entity.ExperienceOrb.class, orb -> orb.setExperience(grave.getXp()));
                }
            } else {
                plugin.getLogger().warning("Grave " + grave.getId() + " world '" + grave.getWorldName()
                        + "' unavailable; items cannot be released and remain in database.");
                return;
            }
        }
        grave.setItems(new ArrayList<>());
        grave.setXp(0);
        grave.setCollected(true);
        if (removeEntities) {
            activeGraves.remove(grave.getId());
            ownerIndex.remove(grave.getOwnerUuid(), grave.getId());
            removeMarkers(grave.getId());
        }
    }

    private void releaseOwnedGraveSilently(UUID owner) {
        GraveRecord old = getActiveGrave(owner);
        if (old != null) {
            releaseGrave(old, true);
            repository.markCollected(old.getId());
        }
    }

    /**
     * Admin-forced release of a player's active grave.
     */
    public boolean forceRelease(@NotNull UUID owner) {
        GraveRecord grave = getActiveGrave(owner);
        if (grave == null) {
            return false;
        }
        releaseGrave(grave, true);
        repository.markCollected(grave.getId());
        return true;
    }

    /**
     * Points the player's compass to their active grave (or their latest death location).
     */
    public boolean pointCompassToGrave(@NotNull Player player) {
        GraveRecord grave = getActiveGrave(player.getUniqueId());
        if (grave != null) {
            return plugin.getDeathCoordinateManager().pointCompassToDeath(player,
                    new DeathRecord(grave.getOwnerUuid(), grave.getWorldName(), grave.getX(), grave.getY(), grave.getZ(),
                            grave.getYaw(), grave.getPitch(), grave.getCause(), grave.getCreatedAt()));
        }
        return plugin.getDeathCoordinateManager().pointCompassToDeath(player, null);
    }

    /**
     * Persists all active graves on shutdown so no items are lost across restarts.
     */
    public void shutdown() {
        if (expiryTask != null) {
            expiryTask.cancel();
            expiryTask = null;
        }
        for (GraveRecord grave : activeGraves.values()) {
            try {
                repository.save(grave).join();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed persisting grave " + grave.getId() + " on shutdown", e);
            }
        }
        for (String id : new ArrayList<>(textDisplayIds.keySet())) {
            removeMarkers(id);
        }
    }
}