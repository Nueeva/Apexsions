package com.apexsions.core.integration;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.api.ApexsionsCoreAPI;
import com.apexsions.core.cache.PlayerCache;
import com.apexsions.core.config.ConfigManager;
import com.apexsions.core.database.DatabaseManager;
import com.apexsions.core.database.PlayerRepository;
import com.apexsions.core.level.LevelManager;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.player.PlayerDataService;
import com.apexsions.core.region.Region;
import com.apexsions.core.region.RegionManager;
import com.apexsions.core.region.TerritoryPolygon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * End-to-End Behavioral Tests for:
 * 1. SQLite PlayerData persistence across reconnects (when TEXT/TIMESTAMP columns store epoch ms strings).
 * 2. Non-destructive insertIfAbsent and database failure recovery (never resetting existing level/kingdom).
 * 3. TPA restrictions across kingdoms, outside kingdom territories (Wilderness/Sions), and Bedrock prefix resolution.
 * 4. /sethome, /home, and /back restrictions in Kerajaan Sions (Terra Interdicta), Wilderness, and foreign kingdoms.
 */
class PlayerDataAndTeleportRestrictionsTest {

    @TempDir
    Path tempDir;

    private ApexsionsCorePlugin plugin;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private PlayerRepository playerRepository;
    private PlayerCache playerCache;
    private PlayerDataService playerDataService;
    private LevelManager levelManager;
    private RegionManager regionManager;
    private ApexsionsCoreAPI api;

    private World world;
    private Region zenithar;
    private Region solterra;
    private Region sions;

    @BeforeEach
    void setUp() {
        plugin = mock(ApexsionsCorePlugin.class);
        configManager = mock(ConfigManager.class);
        levelManager = mock(LevelManager.class);
        regionManager = mock(RegionManager.class);
        api = mock(ApexsionsCoreAPI.class);

        when(plugin.getLogger()).thenReturn(Logger.getLogger("ApexsionsCoreTest"));
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getLevelManager()).thenReturn(levelManager);
        when(plugin.getRegionManager()).thenReturn(regionManager);
        when(plugin.getApi()).thenReturn(api);
        when(configManager.getDbType()).thenReturn("sqlite");

        databaseManager = new DatabaseManager(plugin, configManager);
        databaseManager.initialize();

        playerRepository = new PlayerRepository(plugin, databaseManager);
        playerCache = new PlayerCache();
        playerDataService = new PlayerDataService(plugin, playerRepository, playerCache);

        world = mock(World.class);
        when(world.getName()).thenReturn("world");

        // Define polygons for Zenithar (0..500, 0..500), Solterra (1000..1500, 1000..1500), and Sions (-6500..-5500, -4000..-3000)
        zenithar = new Region(
                UUID.fromString("3e2f5b1a-7c9d-4e8a-b1c2-d3e4f5a6b7c8"),
                "ZENITHAR", "Kerajaan Zenithar", "world",
                250.0, 70.0, 250.0, 0f, 0f, true
        );
        zenithar.setPolygon(new TerritoryPolygon(List.of(
                new TerritoryPolygon.Point2D(0, 0),
                new TerritoryPolygon.Point2D(500, 0),
                new TerritoryPolygon.Point2D(500, 500),
                new TerritoryPolygon.Point2D(0, 500)
        ), -64, 320));

        solterra = new Region(
                UUID.fromString("9b8a7c6d-5e4f-3a2b-1c0d-e9f8a7b6c5d4"),
                "SOLTERRA", "Kerajaan Solterra", "world",
                1250.0, 70.0, 1250.0, 0f, 0f, true
        );
        solterra.setPolygon(new TerritoryPolygon(List.of(
                new TerritoryPolygon.Point2D(1000, 1000),
                new TerritoryPolygon.Point2D(1500, 1000),
                new TerritoryPolygon.Point2D(1500, 1500),
                new TerritoryPolygon.Point2D(1000, 1500)
        ), -64, 320));

        sions = new Region(
                UUID.fromString("f1e2d3c4-b5a6-4789-8012-3456789abcde"),
                "SIONS", "Kerajaan Sions", "world",
                -6000.0, 90.0, -3500.0, 0f, 0f, true
        );
        sions.setPolygon(new TerritoryPolygon(List.of(
                new TerritoryPolygon.Point2D(-6500, -4000),
                new TerritoryPolygon.Point2D(-5500, -4000),
                new TerritoryPolygon.Point2D(-5500, -3000),
                new TerritoryPolygon.Point2D(-6500, -3000)
        ), -64, 320));

        when(regionManager.getRegion("ZENITHAR")).thenReturn(Optional.of(zenithar));
        when(regionManager.getRegion("SOLTERRA")).thenReturn(Optional.of(solterra));
        when(regionManager.getRegion("SIONS")).thenReturn(Optional.of(sions));
        when(regionManager.getRegion(zenithar.getId())).thenReturn(Optional.of(zenithar));
        when(regionManager.getRegion(solterra.getId())).thenReturn(Optional.of(solterra));
        when(regionManager.getRegion(sions.getId())).thenReturn(Optional.of(sions));

        when(regionManager.getRegionAt(any(Location.class))).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            if (loc == null) return Optional.empty();
            if (zenithar.containsLocation(loc)) return Optional.of(zenithar);
            if (solterra.containsLocation(loc)) return Optional.of(solterra);
            if (sions.containsLocation(loc)) return Optional.of(sions);
            return Optional.empty();
        });
    }

    @AfterEach
    void tearDown() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
    }

    @Test
    @DisplayName("Player level, XP, and kingdom persist across SQLite reconnect even with legacy TEXT epoch timestamps")
    void testPlayerLevelAndKingdomPersistAcrossSqliteReconnectWithTextTimestamps() throws Exception {
        // Simulate legacy SQLite table where TIMESTAMPTZ was converted to TEXT affinity
        try (Connection conn = databaseManager.getConnection(); Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS players");
            st.execute("CREATE TABLE players (" +
                    "uuid UUID PRIMARY KEY, " +
                    "username VARCHAR(16) NOT NULL, " +
                    "level INTEGER NOT NULL DEFAULT 1, " +
                    "xp BIGINT NOT NULL DEFAULT 0, " +
                    "region_id UUID, " +
                    "claimed_rewards TEXT DEFAULT '', " +
                    "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        }

        UUID playerUuid = UUID.fromString("00000000-0000-0000-0009-01f3b260f5b5");
        String username = ".Kingambit6204";
        UUID kingdomId = zenithar.getId();

        // 1. Player joins, reaches Level 32, 48500 XP, pledges to Zenithar, and saves
        PlayerData initial = new PlayerData(playerUuid, username, 32, 48500L, kingdomId, Instant.now(), Instant.now());
        initial.setRewardClaimed(5);
        initial.setRewardClaimed(10);
        playerDataService.save(initial).join();

        // 2. Simulate player quit (cache evicted)
        playerCache.invalidate(playerUuid);
        assertTrue(playerDataService.getCached(playerUuid).isEmpty());

        // 3. Simulate player reconnect: loadOrCreate must read from SQLite without failing on epoch-ms TEXT timestamp
        PlayerData reconnected = playerDataService.loadOrCreate(playerUuid, username).join();
        assertEquals(32, reconnected.getLevel(), "Player level must not reset to 1 on reconnect");
        assertEquals(48500L, reconnected.getXp(), "Player XP must not reset to 0 on reconnect");
        assertEquals(kingdomId, reconnected.getRegionId(), "Player kingdom must not reset to null on reconnect");
        assertTrue(reconnected.isRewardClaimed(5) && reconnected.isRewardClaimed(10));
        assertTrue(playerDataService.getCached(playerUuid).isPresent(), "Loaded player must be cached after reconnect");

        // 4. Verify database row itself remains intact
        Optional<PlayerData> fromDb = playerRepository.findByUuid(playerUuid).join();
        assertTrue(fromDb.isPresent());
        assertEquals(32, fromDb.get().getLevel());
        assertEquals(48500L, fromDb.get().getXp());
        assertEquals(kingdomId, fromDb.get().getRegionId());
    }

    @Test
    @DisplayName("insertIfAbsent never overwrites an existing player's level, XP, or kingdom with default values")
    void testInsertIfAbsentNeverOverwritesExistingPlayerRow() {
        UUID playerUuid = UUID.fromString("00000000-0000-0000-0009-01f1f72b1bd1");
        String username = ".iYanz65";
        UUID kingdomId = solterra.getId();

        PlayerData existing = new PlayerData(playerUuid, username, 27, 19400L, kingdomId, Instant.now(), Instant.now());
        playerRepository.save(existing).join();

        // Attempt insertIfAbsent with a fresh default profile (Level 1, XP 0, Region null)
        PlayerData defaultProfile = PlayerData.createDefault(playerUuid, username);
        boolean inserted = playerRepository.insertIfAbsent(defaultProfile).join();
        assertFalse(inserted, "insertIfAbsent must return false when player UUID already exists");

        Optional<PlayerData> afterAttempt = playerRepository.findByUuid(playerUuid).join();
        assertTrue(afterAttempt.isPresent());
        assertEquals(27, afterAttempt.get().getLevel(), "Existing level must remain 27");
        assertEquals(19400L, afterAttempt.get().getXp(), "Existing XP must remain 19400");
        assertEquals(kingdomId, afterAttempt.get().getRegionId(), "Existing kingdom must remain Solterra");
    }

    @Test
    @DisplayName("DatabaseManager.parseTimestampValue handles epoch millis strings, SQL timestamps, and ISO-8601")
    void testParseTimestampValueFormats() {
        Timestamp fromEpochString = DatabaseManager.parseTimestampValue("1790948536465");
        assertNotNull(fromEpochString);
        assertEquals(1790948536465L, fromEpochString.getTime());

        Timestamp fromSqlString = DatabaseManager.parseTimestampValue("2026-10-03 14:22:10");
        assertNotNull(fromSqlString);

        Timestamp fromIsoString = DatabaseManager.parseTimestampValue("2026-10-03T14:22:10Z");
        assertNotNull(fromIsoString);
        assertEquals(Instant.parse("2026-10-03T14:22:10Z"), fromIsoString.toInstant());
    }

    @Test
    @DisplayName("TPA is blocked outside kingdom territories, across different kingdoms, and for unpledged players")
    void testTpaRestrictionsAcrossKingdomsAndOutsideTerritory() {
        TpaRestrictionListener listener = new TpaRestrictionListener(plugin);

        Player zenitharPlayer1 = createMockPlayer(UUID.randomUUID(), "Rifqi", new Location(world, 200, 70, 200));
        Player zenitharPlayer2 = createMockPlayer(UUID.randomUUID(), ".Kingambit6204", new Location(world, 300, 70, 300));
        Player solterraPlayer = createMockPlayer(UUID.randomUUID(), ".iYanz65", new Location(world, 1200, 70, 1200));
        Player unpledgedPlayer = createMockPlayer(UUID.randomUUID(), "Newbie", new Location(world, 200, 70, 200));

        when(api.getPlayerRegionKey(zenitharPlayer1.getUniqueId())).thenReturn("ZENITHAR");
        when(api.getPlayerRegionKey(zenitharPlayer2.getUniqueId())).thenReturn("ZENITHAR");
        when(api.getPlayerRegionKey(solterraPlayer.getUniqueId())).thenReturn("SOLTERRA");
        when(api.getPlayerRegionKey(unpledgedPlayer.getUniqueId())).thenReturn("NONE");

        // 1. Valid: Both in ZENITHAR and physically inside Zenithar polygon
        assertNull(listener.validateTpa(zenitharPlayer1, zenitharPlayer2),
                "Same-kingdom players both inside their kingdom territory should be allowed to TPA");

        // 2. Blocked: Cross-kingdom TPA (Zenithar -> Solterra)
        String crossKingdomErr = listener.validateTpa(zenitharPlayer1, solterraPlayer);
        assertNotNull(crossKingdomErr, "Cross-kingdom TPA must be rejected");
        assertTrue(crossKingdomErr.contains("sesama anggota kerajaan"));

        // 3. Blocked: Unpledged player (NONE)
        String unpledgedErr = listener.validateTpa(unpledgedPlayer, zenitharPlayer1);
        assertNotNull(unpledgedErr, "Unpledged player TPA must be rejected");

        // 4. Blocked: Sender is in Wilderness (x=5000, z=5000)
        when(zenitharPlayer1.getLocation()).thenReturn(new Location(world, 5000, 70, 5000));
        String senderOutsideErr = listener.validateTpa(zenitharPlayer1, zenitharPlayer2);
        assertNotNull(senderOutsideErr, "TPA must be rejected when sender is outside kingdom territory (Wilderness)");
        assertTrue(senderOutsideErr.contains("di luar wilayah teritorial"));

        // 5. Blocked: Target is inside Kerajaan Sions (x=-6133, z=-3486)
        when(zenitharPlayer1.getLocation()).thenReturn(new Location(world, 200, 70, 200));
        when(zenitharPlayer2.getLocation()).thenReturn(new Location(world, -6133, 92, -3486));
        String targetInSionsErr = listener.validateTpa(zenitharPlayer1, zenitharPlayer2);
        assertNotNull(targetInSionsErr, "TPA must be rejected when target is inside Sions / outside their kingdom");
        assertTrue(targetInSionsErr.contains("di luar wilayah teritorial"));
    }

    @Test
    @DisplayName("/sethome, /home, and /back are blocked in Kerajaan Sions, Wilderness, and foreign kingdoms")
    void testSethomeAndTeleportBlockedInSionsWildernessAndForeignKingdom() {
        TpaRestrictionListener listener = new TpaRestrictionListener(plugin);

        Location insideZenithar = new Location(world, 250, 70, 250);
        Location insideSolterra = new Location(world, 1250, 70, 1250);
        Location insideSions = new Location(world, -6133, 92, -3486);
        Location insideWilderness = new Location(world, 4000, 70, 4000);

        Player player = createMockPlayer(UUID.randomUUID(), ".Kingambit6204", insideZenithar);
        when(api.getPlayerRegionKey(player.getUniqueId())).thenReturn("ZENITHAR");

        // 1. /sethome inside own kingdom (Zenithar) -> ALLOWED
        assertNull(listener.validateSethomeLocation(player, insideZenithar),
                "/sethome inside player's own playable kingdom must be allowed");

        // 2. /sethome inside Kerajaan Sions (Terra Interdicta) -> BLOCKED
        String sionsErr = listener.validateSethomeLocation(player, insideSions);
        assertNotNull(sionsErr, "/sethome in Kerajaan Sions must be blocked");
        assertTrue(sionsErr.contains("Terra Interdicta") || sionsErr.contains("Kerajaan Sions"));

        // 3. /sethome inside Wilderness -> BLOCKED
        String wildErr = listener.validateSethomeLocation(player, insideWilderness);
        assertNotNull(wildErr, "/sethome in Wilderness must be blocked");
        assertTrue(wildErr.contains("Wilderness"));

        // 4. /sethome inside foreign kingdom (Solterra) -> BLOCKED
        String foreignErr = listener.validateSethomeLocation(player, insideSolterra);
        assertNotNull(foreignErr, "/sethome in foreign kingdom must be blocked");
        assertTrue(foreignErr.contains("kerajaan asing"));

        // 5. /sethome when player has no kingdom (NONE) -> BLOCKED
        when(api.getPlayerRegionKey(player.getUniqueId())).thenReturn("NONE");
        String noKingdomErr = listener.validateSethomeLocation(player, insideZenithar);
        assertNotNull(noKingdomErr, "/sethome without a kingdom must be blocked");

        // 6. End-to-End Command + PlayerTeleportEvent lifecycle for /home to Sions or Wilderness
        when(api.getPlayerRegionKey(player.getUniqueId())).thenReturn("ZENITHAR");
        PlayerCommandPreprocessEvent homeCmd = new PlayerCommandPreprocessEvent(player, "/home bebek", new java.util.HashSet<>());
        listener.onCommandPreprocess(homeCmd);
        assertFalse(homeCmd.isCancelled(), "/home command preprocess is allowed to proceed to destination check");

        PlayerTeleportEvent tpToSions = new PlayerTeleportEvent(
                player, insideZenithar, insideSions, PlayerTeleportEvent.TeleportCause.COMMAND
        );
        listener.onPlayerTeleport(tpToSions);
        assertTrue(tpToSions.isCancelled(), "Teleporting via /home to a location in Sions must be cancelled");

        // Trigger /back to Wilderness -> must also be cancelled on PlayerTeleportEvent
        PlayerCommandPreprocessEvent backCmd = new PlayerCommandPreprocessEvent(player, "/back", new java.util.HashSet<>());
        listener.onCommandPreprocess(backCmd);
        PlayerTeleportEvent tpToWild = new PlayerTeleportEvent(
                player, insideZenithar, insideWilderness, PlayerTeleportEvent.TeleportCause.COMMAND
        );
        listener.onPlayerTeleport(tpToWild);
        assertTrue(tpToWild.isCancelled(), "Teleporting via /back to Wilderness must be cancelled");
    }

    private Player createMockPlayer(UUID uuid, String name, Location loc) {
        Player p = mock(Player.class);
        when(p.getUniqueId()).thenReturn(uuid);
        when(p.getName()).thenReturn(name);
        when(p.getLocation()).thenReturn(loc);
        when(p.isOnline()).thenReturn(true);
        when(p.isOp()).thenReturn(false);
        when(p.hasPermission(anyString())).thenReturn(false);
        return p;
    }
}
