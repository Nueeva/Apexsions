package com.apexsions.core.grave;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GraveSystemTest {

    private MockedStatic<Bukkit> mockedBukkit;
    private Server server;
    private World mockWorld;

    @BeforeEach
    void setUp() {
        mockedBukkit = mockStatic(Bukkit.class);
        server = mock(Server.class);
        mockWorld = mock(World.class);

        when(mockWorld.getName()).thenReturn("world");
        mockedBukkit.when(Bukkit::getServer).thenReturn(server);
        mockedBukkit.when(() -> Bukkit.getWorld("world")).thenReturn(mockWorld);
    }

    @AfterEach
    void tearDown() {
        mockedBukkit.close();
    }

    @Test
    void testGraveRecordCreationAndRemaining() {
        UUID owner = UUID.randomUUID();
        Location deathLoc = new Location(mockWorld, -9437.0, -33.0, -4015.0);
        GraveRecord record = GraveRecord.create(owner, "Kingambit", deathLoc, new ArrayList<>(), 21, "fell from a high place", 1800000L);

        assertNotNull(record.getId());
        assertEquals("Kingambit", record.getOwnerName());
        assertEquals(21, record.getXp());
        assertEquals("world", record.getWorldName());
        assertEquals(-9437.0, record.getX(), 0.001);
        assertEquals(-33.0, record.getY(), 0.001);
        assertEquals(-4015.0, record.getZ(), 0.001);
        assertFalse(record.isExpired());
        assertTrue(record.remainingMillis() > 1700000L);
    }

    @Test
    void testElevationDifferenceDetection() {
        // Simulating Kingambit's scenario:
        // Grave is at Y = -33 (bottom of ravine/cave)
        // Player is at Y = -1 (surface/ledge above)
        Location graveLoc = new Location(mockWorld, -9437.47, -33.0, -4015.63);
        Location playerLoc = new Location(mockWorld, -9431.0, -1.0, -4017.0);

        GraveRecord record = GraveRecord.create(UUID.randomUUID(), "Kingambit", graveLoc, new ArrayList<>(), 21, "fell from a high place", 1800000L);

        double dist = record.distanceTo(playerLoc);
        assertTrue(dist > 30.0, "Distance should be > 30 blocks due to vertical delta");

        int diffY = (int) Math.round(record.getY() - playerLoc.getY());
        assertEquals(-32, diffY, "Grave must be 32 blocks below the player");
        assertTrue(diffY <= -3, "Should trigger the 'gali ke bawah' guidance");
    }

    @Test
    void testMultiGraveSorting() {
        UUID owner = UUID.randomUUID();
        long now = System.currentTimeMillis();

        GraveRecord oldGrave = new GraveRecord(UUID.randomUUID().toString(), owner, "Kingambit", "world",
                100, 60, 100, 0, 0, new ArrayList<>(), 10, "hit by zombie", now - 60000, now + 1800000, false);
        GraveRecord newGrave = new GraveRecord(UUID.randomUUID().toString(), owner, "Kingambit", "world",
                200, 60, 200, 0, 0, new ArrayList<>(), 20, "fell from height", now, now + 1800000, false);

        List<GraveRecord> graves = new ArrayList<>(List.of(oldGrave, newGrave));
        graves.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

        assertEquals(newGrave, graves.get(0), "Newest grave should come first");
        assertEquals(oldGrave, graves.get(1), "Older grave should come second");
    }
}
