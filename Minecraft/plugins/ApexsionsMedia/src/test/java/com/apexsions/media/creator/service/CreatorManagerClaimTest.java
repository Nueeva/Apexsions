package com.apexsions.media.creator.service;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.creator.database.CreatorRepository;
import com.apexsions.media.creator.model.CreatorClaim;
import com.apexsions.media.creator.model.CreatorProfile;
import com.apexsions.media.creator.model.Platform;
import com.apexsions.media.creator.model.VideoValidationResult;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regression test untuk C-4: reward TIDAK boleh dikirim jika klaim video
 * gagal tersimpan (mis. pelanggaran UNIQUE video_id).
 *
 * <p><b>CATATAN LINGKUNGAN:</b> test di class ini di-{@code Disabled} karena
 * membutuhkan Paper registry — alur {@code processVideoSubmission} menyentuh
 * enum {@code org.bukkit.Sound} yang static-initializer-nya memanggil
 * {@code RegistryAccess} (hanya ada di server Paper asli / MockBukkit).
 * Tanpa itu, {@code Sound.ENTITY_VILLAGER_NO} melempar
 * {@code ExceptionInInitializerError} di JVM unit-test biasa.
 * Aktifkan kembali setelah MockBukkit tersedia di classpath test.
 * Verifikasi C-4 tetap tercakup oleh {@code CreatorRepositoryClaimTest}
 * (level repository, berjalan hijau).</p>
 */
@Disabled("Butuh Paper registry/MockBukkit: org.bukkit.Sound tidak bisa diinisialisasi di unit-test biasa")
class CreatorManagerClaimTest {

    private ApexsionsMediaPlugin plugin;
    private BukkitScheduler scheduler;

    @BeforeEach
    void setUp() {
        plugin = mock(ApexsionsMediaPlugin.class);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("CreatorManagerClaimTest"));

        // Konfigurasi creator minimal: satu tier bronze agar video valid bisa match.
        FileConfiguration config = mock(FileConfiguration.class);
        when(plugin.getConfig()).thenReturn(config);
        ConfigurationSection creatorSec = mock(ConfigurationSection.class);
        when(config.getConfigurationSection("creator")).thenReturn(creatorSec);
        when(creatorSec.getStringList("required-hashtags")).thenReturn(List.of("#apexsions"));
        when(creatorSec.getInt("video-max-age-days", 14)).thenReturn(14);
        when(creatorSec.getInt("verification-code-timeout-minutes", 10)).thenReturn(10);
        ConfigurationSection tiersSec = mock(ConfigurationSection.class);
        when(creatorSec.getConfigurationSection("tiers")).thenReturn(tiersSec);
        when(tiersSec.getKeys(false)).thenReturn(Set.of("bronze"));
        ConfigurationSection bronze = mock(ConfigurationSection.class);
        when(tiersSec.getConfigurationSection("bronze")).thenReturn(bronze);
        when(bronze.getString("name", "bronze")).thenReturn("Bronze");
        when(bronze.getLong("min-views", 100)).thenReturn(100L);
        when(bronze.getLong("min-likes", 10)).thenReturn(10L);
        when(bronze.getStringList("rewards")).thenReturn(List.of("give %player% diamond 1"));
        when(bronze.getStringList("perks-description")).thenReturn(List.of());

        scheduler = mock(BukkitScheduler.class);
    }

    private VideoValidationResult validResult() {
        return VideoValidationResult.success(
                Platform.YOUTUBE, "vid123", "https://www.youtube.com/watch?v=vid12345678",
                "UC123", "UC123", "Video test #apexsions",
                500L, 50L, System.currentTimeMillis(), true, List.of("#apexsions"));
    }

    private Player mockPlayer(UUID uuid) {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("TestPlayer");
        return player;
    }

    private CreatorProfile mockLinkedProfile() {
        CreatorProfile profile = mock(CreatorProfile.class);
        when(profile.isYouTubeLinked()).thenReturn(true);
        when(profile.getYoutubeChannelId()).thenReturn("UC123");
        return profile;
    }

    @Test
    void saveClaimFails_dispatchRewardsNotCalled() {
        UUID uuid = UUID.randomUUID();
        try (MockedConstruction<CreatorRepository> repoCtor = Mockito.mockConstruction(CreatorRepository.class);
             MockedConstruction<YouTubeService> ytCtor = Mockito.mockConstruction(YouTubeService.class);
             MockedConstruction<TikTokService> ttCtor = Mockito.mockConstruction(TikTokService.class);
             MockedStatic<Bukkit> bukkit = Mockito.mockStatic(Bukkit.class)) {

            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

            CreatorManager manager = new CreatorManager(plugin);
            CreatorRepository repository = repoCtor.constructed().get(0);
            YouTubeService youTubeService = ytCtor.constructed().get(0);
            TikTokService tikTokService = ttCtor.constructed().get(0);

            Player player = mockPlayer(uuid);
            CreatorProfile linkedProfile = mockLinkedProfile();
            when(repository.loadProfile(uuid, "TestPlayer"))
                    .thenReturn(CompletableFuture.completedFuture(linkedProfile));
            when(repository.isVideoClaimed("vid123"))
                    .thenReturn(CompletableFuture.completedFuture(false));
            // Simulasi C-4: INSERT gagal (UNIQUE video_id) -> future false
            when(repository.saveClaim(any(CreatorClaim.class)))
                    .thenReturn(CompletableFuture.completedFuture(false));

            when(tikTokService.isTikTokUrl(anyString())).thenReturn(false);
            VideoValidationResult validationResult = validResult();
            when(youTubeService.validateVideo(anyString(), anyList()))
                    .thenReturn(CompletableFuture.completedFuture(validationResult));

            manager.processVideoSubmission(player, "https://www.youtube.com/watch?v=vid12345678").join();

            verify(repository).saveClaim(any(CreatorClaim.class));
            // C-4: klaim gagal tersimpan -> reward TIDAK boleh didispatch
            bukkit.verify(() -> Bukkit.dispatchCommand(any(), anyString()), never());

            // Fail-closed: pemain tetap diberi tahu kegagalannya (bukan diam).
            // Scheduler di-mock sehingga runnable harus dieksekusi manual di sini.
            ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
            verify(scheduler, atLeastOnce()).runTask(any(Plugin.class), captor.capture());
            captor.getAllValues().forEach(Runnable::run);
            verify(player, times(2)).sendMessage(any(Component.class));
        }
    }

    @Test
    void saveClaimSucceeds_rewardDispatched() {
        UUID uuid = UUID.randomUUID();
        try (MockedConstruction<CreatorRepository> repoCtor = Mockito.mockConstruction(CreatorRepository.class);
             MockedConstruction<YouTubeService> ytCtor = Mockito.mockConstruction(YouTubeService.class);
             MockedConstruction<TikTokService> ttCtor = Mockito.mockConstruction(TikTokService.class);
             MockedStatic<Bukkit> bukkit = Mockito.mockStatic(Bukkit.class)) {

            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

            CreatorManager manager = new CreatorManager(plugin);
            CreatorRepository repository = repoCtor.constructed().get(0);
            YouTubeService youTubeService = ytCtor.constructed().get(0);
            TikTokService tikTokService = ttCtor.constructed().get(0);

            Player player = mockPlayer(uuid);
            CreatorProfile linkedProfile = mockLinkedProfile();
            when(repository.loadProfile(uuid, "TestPlayer"))
                    .thenReturn(CompletableFuture.completedFuture(linkedProfile));
            when(repository.isVideoClaimed("vid123"))
                    .thenReturn(CompletableFuture.completedFuture(false));
            when(repository.saveClaim(any(CreatorClaim.class)))
                    .thenReturn(CompletableFuture.completedFuture(true));

            when(tikTokService.isTikTokUrl(anyString())).thenReturn(false);
            VideoValidationResult validationResult = validResult();
            when(youTubeService.validateVideo(anyString(), anyList()))
                    .thenReturn(CompletableFuture.completedFuture(validationResult));

            manager.processVideoSubmission(player, "https://www.youtube.com/watch?v=vid12345678").join();

            // Reward didispatch via hop ke main thread (satu-satunya runTask di jalur sukses)
            ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
            verify(scheduler, times(1)).runTask(any(Plugin.class), captor.capture());
            captor.getValue().run(); // eksekusi dispatchRewards

            bukkit.verify(() -> Bukkit.dispatchCommand(any(), eq("give TestPlayer diamond 1")), times(1));
        }
    }
}
