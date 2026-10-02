package com.apexsions.media.creator.database;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.creator.model.CreatorClaim;
import com.apexsions.media.creator.model.Platform;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Regression test untuk C-4 di level repository: memakai SQLite sungguhan
 * (file sementara) untuk membuktikan klaim ganda {@code video_id} (UNIQUE)
 * menghasilkan future {@code false} — bukan exception yang tertelan dan
 * bukan future sukses palsu.
 */
class CreatorRepositoryClaimTest {

    @TempDir
    private Path tempDir;

    private CreatorRepository repository;

    @BeforeEach
    void setUp() {
        ApexsionsMediaPlugin plugin = mock(ApexsionsMediaPlugin.class);
        FileConfiguration config = mock(FileConfiguration.class);
        when(plugin.getConfig()).thenReturn(config);
        when(config.getString("database.type", "SQLITE")).thenReturn("SQLITE");
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getLogger()).thenReturn(Logger.getLogger("CreatorRepositoryClaimTest"));

        repository = new CreatorRepository(plugin);
    }

    @AfterEach
    void tearDown() {
        repository.shutdown();
    }

    private CreatorClaim claim(String claimId, String videoId) {
        return new CreatorClaim(claimId, UUID.randomUUID(), Platform.YOUTUBE,
                videoId, "https://www.youtube.com/watch?v=" + videoId,
                500L, 50L, "bronze", System.currentTimeMillis());
    }

    @Test
    void saveClaim_firstInsert_returnsTrue() {
        assertTrue(repository.saveClaim(claim("claim-1", "vid-abc")).join());
        assertTrue(repository.isVideoClaimed("vid-abc").join());
    }

    @Test
    void saveClaim_duplicateVideoId_returnsFalse() {
        assertTrue(repository.saveClaim(claim("claim-1", "vid-dup")).join());

        // Pelanggaran UNIQUE(video_id): harus false, bukan sukses palsu
        assertFalse(repository.saveClaim(claim("claim-2", "vid-dup")).join());

        // Klaim asli tetap tercatat tepat satu kali
        assertTrue(repository.isVideoClaimed("vid-dup").join());
        assertFalse(repository.isVideoClaimed("vid-lain").join());
    }
}
