package com.apexsions.media.creator.service;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.creator.database.CreatorRepository;
import com.apexsions.media.creator.model.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CreatorManager {

    private final ApexsionsMediaPlugin plugin;
    private final CreatorRepository repository;
    private final YouTubeService youTubeService;
    private final TikTokService tikTokService;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final Map<UUID, CreatorProfile> profileCache = new ConcurrentHashMap<>();
    private final List<CreatorTier> tiers = new ArrayList<>();
    private List<String> requiredHashtags = new ArrayList<>();
    private int maxVideoAgeDays = 14;
    private int verifyTimeoutMinutes = 10;

    public CreatorManager(ApexsionsMediaPlugin plugin) {
        this.plugin = plugin;
        this.repository = new CreatorRepository(plugin);
        this.youTubeService = new YouTubeService(plugin);
        this.tikTokService = new TikTokService(plugin);

        loadConfiguration();
    }

    public void loadConfiguration() {
        tiers.clear();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("creator");
        if (sec == null) return;

        this.requiredHashtags = sec.getStringList("required-hashtags");
        if (requiredHashtags.isEmpty()) {
            requiredHashtags = List.of("#apexsions", "#apexsionsmc");
        }
        this.maxVideoAgeDays = sec.getInt("video-max-age-days", 14);
        this.verifyTimeoutMinutes = sec.getInt("verification-code-timeout-minutes", 10);

        ConfigurationSection tiersSec = sec.getConfigurationSection("tiers");
        if (tiersSec != null) {
            for (String key : tiersSec.getKeys(false)) {
                ConfigurationSection t = tiersSec.getConfigurationSection(key);
                if (t == null) continue;

                String name = t.getString("name", key);
                long minViews = t.getLong("min-views", 100);
                long minLikes = t.getLong("min-likes", 10);
                List<String> rewards = t.getStringList("rewards");
                List<String> perks = t.getStringList("perks-description");

                tiers.add(new CreatorTier(key, name, minViews, minLikes, rewards, perks));
            }
        }

        // Sort descending by minViews to easily find highest eligible tier
        tiers.sort((a, b) -> Long.compare(b.getMinViews(), a.getMinViews()));
        plugin.getLogger().info("Loaded " + tiers.size() + " Creator Tiers.");
    }

    public CompletableFuture<CreatorProfile> getProfile(UUID uuid, String playerName) {
        CreatorProfile cached = profileCache.get(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return repository.loadProfile(uuid, playerName).thenApply(p -> {
            profileCache.put(uuid, p);
            return p;
        });
    }

    public CreatorProfile getCachedProfile(UUID uuid) {
        return profileCache.get(uuid);
    }

    public CompletableFuture<String> startLinking(Player player, Platform platform, String identifier) {
        String cleanId = identifier.trim();
        return getProfile(player.getUniqueId(), player.getName()).thenCompose(profile -> {
            String code = "APEX-" + (1000 + new Random().nextInt(9000));
            long expiry = System.currentTimeMillis() + (verifyTimeoutMinutes * 60 * 1000L);

            profile.setPendingPlatform(platform.name());
            profile.setPendingIdentifier(cleanId);
            profile.setPendingVerifyCode(code);
            profile.setPendingVerifyExpiry(expiry);

            return repository.saveProfile(profile).thenApply(v -> code);
        });
    }

    public CompletableFuture<Boolean> verifyLinking(Player player, Platform platform) {
        return getProfile(player.getUniqueId(), player.getName()).thenCompose(profile -> {
            if (!profile.hasPendingVerification() || !platform.name().equalsIgnoreCase(profile.getPendingPlatform())) {
                return CompletableFuture.completedFuture(false);
            }

            String identifier = profile.getPendingIdentifier();
            String code = profile.getPendingVerifyCode();

            if (platform == Platform.YOUTUBE) {
                return youTubeService.verifyChannelOwnership(identifier, code).thenCompose(verified -> {
                    if (verified) {
                        profile.setYoutubeChannelId(identifier);
                        if (identifier.startsWith("@")) {
                            profile.setYoutubeHandle(identifier);
                        }
                        profile.clearPendingVerification();
                        return repository.saveProfile(profile).thenApply(v -> true);
                    }
                    return CompletableFuture.completedFuture(false);
                });
            } else {
                // TikTok direct link verification
                profile.setTiktokUsername(identifier.replace("@", ""));
                profile.clearPendingVerification();
                return repository.saveProfile(profile).thenApply(v -> true);
            }
        });
    }

    public CompletableFuture<Void> unlinkPlatform(Player player, Platform platform) {
        return getProfile(player.getUniqueId(), player.getName()).thenCompose(profile -> {
            if (platform == Platform.YOUTUBE) {
                profile.setYoutubeChannelId(null);
                profile.setYoutubeHandle(null);
            } else {
                profile.setTiktokUsername(null);
            }
            return repository.saveProfile(profile);
        });
    }

    public CompletableFuture<Void> processVideoSubmission(Player player, String videoUrl) {
        player.sendMessage(mm.deserialize("<gradient:#3498db:#2ecc71><b>[Creator]</b></gradient> <gray>Sedang memverifikasi tautan video secara asinkron...</gray>"));

        return getProfile(player.getUniqueId(), player.getName()).thenCompose(profile -> {
            boolean isTikTok = tikTokService.isTikTokUrl(videoUrl);
            Platform platform = isTikTok ? Platform.TIKTOK : Platform.YOUTUBE;

            if (platform == Platform.YOUTUBE && !profile.isYouTubeLinked()) {
                player.sendMessage(mm.deserialize("<red><b>[Creator]</b> Kamu belum menautkan channel YouTube! Ketik <yellow>/creator link youtube &lt;ChannelID/@handle&gt;</yellow></red>"));
                return CompletableFuture.completedFuture(null);
            }

            if (platform == Platform.TIKTOK && !profile.isTikTokLinked()) {
                player.sendMessage(mm.deserialize("<red><b>[Creator]</b> Kamu belum menautkan username TikTok! Ketik <yellow>/creator link tiktok &lt;Username&gt;</yellow></red>"));
                return CompletableFuture.completedFuture(null);
            }

            CompletableFuture<VideoValidationResult> validationFuture = isTikTok
                    ? tikTokService.validateVideo(videoUrl, requiredHashtags)
                    : youTubeService.validateVideo(videoUrl, requiredHashtags);

            return validationFuture.thenCompose(result -> {
                if (!result.success()) {
                    player.sendMessage(mm.deserialize("<red><b>[Creator]</b> Verifikasi gagal: " + result.errorMessage() + "</red>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return CompletableFuture.completedFuture(null);
                }

                // Check ownership
                if (platform == Platform.YOUTUBE) {
                    String linkedId = profile.getYoutubeChannelId();
                    String linkedHandle = profile.getYoutubeHandle();
                    boolean channelMatch = (linkedId != null && linkedId.equalsIgnoreCase(result.authorOrChannelId()))
                            || (linkedHandle != null && linkedHandle.equalsIgnoreCase(result.authorName()));
                    if (!channelMatch) {
                        player.sendMessage(mm.deserialize("<red><b>[Creator]</b> Video ini bukan milik channel YouTube kamu yang terdaftar! (" + result.authorName() + ")</red>"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return CompletableFuture.completedFuture(null);
                    }
                } else {
                    String linkedTiktok = profile.getTiktokUsername();
                    if (linkedTiktok == null || !linkedTiktok.equalsIgnoreCase(result.authorOrChannelId())) {
                        player.sendMessage(mm.deserialize("<red><b>[Creator]</b> Video TikTok ini bukan diunggah oleh akun terdaftar kamu! (@" + result.authorOrChannelId() + ")</red>"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return CompletableFuture.completedFuture(null);
                    }
                }

                // Check video age
                long ageDays = ChronoUnit.DAYS.between(Instant.ofEpochMilli(result.publishedAt()), Instant.now());
                if (ageDays > maxVideoAgeDays) {
                    player.sendMessage(mm.deserialize("<red><b>[Creator]</b> Video sudah terlalu lama (" + ageDays + " hari yang lalu). Batas maksimal umur video adalah " + maxVideoAgeDays + " hari.</red>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return CompletableFuture.completedFuture(null);
                }

                // Check hashtag
                if (!result.hasRequiredHashtag()) {
                    player.sendMessage(mm.deserialize("<red><b>[Creator]</b> Video tidak memuat hashtag wajib server (" + String.join(", ", requiredHashtags) + ") di judul atau deskripsi!</red>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return CompletableFuture.completedFuture(null);
                }

                // Check if already claimed
                return repository.isVideoClaimed(result.videoId()).thenAccept(alreadyClaimed -> {
                    if (alreadyClaimed) {
                        player.sendMessage(mm.deserialize("<red><b>[Creator]</b> Video ini sudah pernah diklaim sebelumnya!</red>"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }

                    // Find highest matching tier
                    CreatorTier matchedTier = null;
                    for (CreatorTier tier : tiers) {
                        if (tier.matches(result.views(), result.likes())) {
                            matchedTier = tier;
                            break;
                        }
                    }

                    if (matchedTier == null) {
                        CreatorTier lowest = tiers.isEmpty() ? null : tiers.get(tiers.size() - 1);
                        long reqViews = lowest != null ? lowest.getMinViews() : 100;
                        long reqLikes = lowest != null ? lowest.getMinLikes() : 10;

                        player.sendMessage(mm.deserialize("<gold><b>[Creator]</b> Video terverifikasi valid, namun statistik belum mencapai Tier terendah!</gold>"));
                        player.sendMessage(mm.deserialize("<gray>Views saat ini: <yellow>" + result.views() + "/" + reqViews + "</yellow> | Likes: <yellow>" + result.likes() + "/" + reqLikes + "</yellow></gray>"));
                        player.sendMessage(mm.deserialize("<gray>Tingkatkan interaksi video kamu dan coba klaim kembali nanti!</gray>"));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
                        return;
                    }

                    // Success! Save claim and dispatch rewards
                    CreatorClaim claim = new CreatorClaim(
                            UUID.randomUUID().toString().substring(0, 12),
                            player.getUniqueId(),
                            platform,
                            result.videoId(),
                            result.videoUrl(),
                            result.views(),
                            result.likes(),
                            matchedTier.getId(),
                            System.currentTimeMillis()
                    );

                    CreatorTier finalTier = matchedTier;
                    repository.saveClaim(claim).thenRun(() -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            dispatchRewards(player, finalTier, result);
                        });
                    });
                });
            });
        });
    }

    private void dispatchRewards(Player player, CreatorTier tier, VideoValidationResult result) {
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);

        player.sendMessage(mm.deserialize("\n<gradient:#f39c12:#f1c40f><bold>✦ KLAIM HADIAH KREATOR BERHASIL! ✦</bold></gradient>"));
        player.sendMessage(mm.deserialize("<gray>Platform: " + result.platform().getMiniMessageTag() + "</gray>"));
        player.sendMessage(mm.deserialize("<gray>Judul: <white>" + result.title() + "</white></gray>"));
        player.sendMessage(mm.deserialize("<gray>Statistik: <yellow>" + result.views() + " Views</yellow> & <yellow>" + result.likes() + " Likes</yellow></gray>"));
        player.sendMessage(mm.deserialize("<gray>Tier Tercapai: </gray>" + tier.getDisplayName()));

        // Execute reward commands
        for (String cmd : tier.getRewards()) {
            String parsedCmd = cmd.replace("%player%", player.getName())
                    .replace("%uuid%", player.getUniqueId().toString())
                    .replace("%views%", String.valueOf(result.views()))
                    .replace("%likes%", String.valueOf(result.likes()));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCmd);
        }

        // Global server broadcast
        Component broadcastMsg = mm.deserialize(
                "<gradient:#f39c12:#f1c40f><b>[Creator]</b></gradient> <yellow>Selamat kepada <aqua>" + player.getName() +
                        "</aqua> atas verifikasi video " + result.platform().getDisplayName() + " mencapai tier " + tier.getDisplayName() + "!</yellow>"
        );
        Bukkit.broadcast(broadcastMsg);
    }

    public List<CreatorTier> getTiers() {
        return tiers;
    }

    public List<String> getRequiredHashtags() {
        return requiredHashtags;
    }

    public int getMaxVideoAgeDays() {
        return maxVideoAgeDays;
    }

    public CreatorRepository getRepository() {
        return repository;
    }

    public YouTubeService getYouTubeService() {
        return youTubeService;
    }

    public TikTokService getTikTokService() {
        return tikTokService;
    }

    public void shutdown() {
        repository.shutdown();
        profileCache.clear();
    }
}
