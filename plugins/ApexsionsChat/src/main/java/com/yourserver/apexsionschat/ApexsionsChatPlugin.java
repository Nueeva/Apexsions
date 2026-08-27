package com.yourserver.apexsionschat;

import com.yourserver.apexsionschat.announcement.AnnouncementManager;
import com.yourserver.apexsionschat.api.ApexsionsChatAPI;
import com.yourserver.apexsionschat.api.ApexsionsChatProvider;
import com.yourserver.apexsionschat.channel.ChannelManager;
import com.yourserver.apexsionschat.channel.ChatChannel;
import com.yourserver.apexsionschat.chat.ChatFormatter;
import com.yourserver.apexsionschat.chat.ChatListener;
import com.yourserver.apexsionschat.chat.ItemShowcaseService;
import com.yourserver.apexsionschat.chat.MentionParser;
import com.yourserver.apexsionschat.command.*;
import com.yourserver.apexsionschat.config.ChatConfigManager;
import com.yourserver.apexsionschat.database.ChatDatabaseManager;
import com.yourserver.apexsionschat.database.MailRepository;
import com.yourserver.apexsionschat.database.ModerationLogRepository;
import com.yourserver.apexsionschat.database.ReportRepository;
import com.yourserver.apexsionschat.game.ChatGameManager;
import com.yourserver.apexsionschat.gui.GUIListener;
import com.yourserver.apexsionschat.integration.ApexsionsCoreHook;
import com.yourserver.apexsionschat.integration.LuckPermsHook;
import com.yourserver.apexsionschat.integration.PlaceholderApiHook;
import com.yourserver.apexsionschat.integration.VaultHook;
import com.yourserver.apexsionschat.model.Mail;
import com.yourserver.apexsionschat.model.Report;
import com.yourserver.apexsionschat.moderation.ModerationEngine;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ApexsionsChatPlugin extends JavaPlugin implements ApexsionsChatAPI {

    private static ApexsionsChatPlugin instance;

    private ChatConfigManager configManager;
    private ChatDatabaseManager databaseManager;
    private ReportRepository reportRepository;
    private MailRepository mailRepository;
    private ModerationLogRepository moderationLogRepository;

    private ApexsionsCoreHook apexsionsCoreHook;
    private LuckPermsHook luckPermsHook;
    private VaultHook vaultHook;
    private PlaceholderApiHook placeholderApiHook;

    private ChannelManager channelManager;
    private ModerationEngine moderationEngine;
    private MentionParser mentionParser;
    private ItemShowcaseService itemShowcaseService;
    private ChatFormatter chatFormatter;
    private ChatGameManager gameManager;
    private AnnouncementManager announcementManager;

    @Override
    public void onEnable() {
        instance = this;
        long startMs = System.currentTimeMillis();

        getLogger().info("========================================");
        getLogger().info("Starting ApexsionsChat (Paper 26.2)...");
        getLogger().info("========================================");

        // 1. Configurations
        this.configManager = new ChatConfigManager(this);
        this.configManager.loadAll();

        // 2. Database
        this.databaseManager = new ChatDatabaseManager(this);
        this.databaseManager.initialize();

        // 3. Repositories
        this.reportRepository = new ReportRepository(this, databaseManager);
        this.mailRepository = new MailRepository(this, databaseManager);
        this.moderationLogRepository = new ModerationLogRepository(this, databaseManager);

        // 4. Hooks
        this.apexsionsCoreHook = new ApexsionsCoreHook(this);
        this.luckPermsHook = new LuckPermsHook(this);
        this.vaultHook = new VaultHook(this);
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.placeholderApiHook = new PlaceholderApiHook(this);
            this.placeholderApiHook.register();
            getLogger().info("Registered PlaceholderAPI expansion (%apexsionschat_*%).");
        }

        // 5. Chat & Community Services
        this.channelManager = new ChannelManager(this);
        this.moderationEngine = new ModerationEngine(this);
        this.mentionParser = new MentionParser(this);
        this.itemShowcaseService = new ItemShowcaseService(this);
        this.chatFormatter = new ChatFormatter(this);
        this.gameManager = new ChatGameManager(this);
        this.announcementManager = new AnnouncementManager(this);

        // 6. Register Public API
        ApexsionsChatProvider.register(this);

        // 7. Register Commands & Listeners
        registerCommands();
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        // 8. Start Schedulers
        this.gameManager.startScheduler();
        this.announcementManager.startScheduler();

        long elapsed = System.currentTimeMillis() - startMs;
        getLogger().info("========================================");
        getLogger().info("ApexsionsChat enabled successfully in " + elapsed + "ms!");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) gameManager.stopScheduler();
        if (announcementManager != null) announcementManager.stopScheduler();
        if (placeholderApiHook != null) placeholderApiHook.unregister();
        if (databaseManager != null) databaseManager.shutdown();

        ApexsionsChatProvider.unregister();
        getLogger().info("ApexsionsChat disabled.");
        instance = null;
    }

    private void registerCommands() {
        ChannelCommand chCmd = new ChannelCommand(this);
        registerCmd("channel", chCmd);
        registerCmd("global", chCmd);
        registerCmd("kingdomchat", chCmd);
        registerCmd("staffchat", chCmd);

        ShowItemCommand showCmd = new ShowItemCommand(this);
        registerCmd("showitem", showCmd);

        ReportCommand repCmd = new ReportCommand(this);
        registerCmd("report", repCmd);

        ReportsAdminCommand repAdminCmd = new ReportsAdminCommand(this);
        registerCmd("reports", repAdminCmd);

        MailCommand mailCmd = new MailCommand(this);
        registerCmd("mail", mailCmd);

        ApexsionsChatAdminCommand adminCmd = new ApexsionsChatAdminCommand(this);
        registerCmd("apexsionschat", adminCmd);
    }

    private void registerCmd(String name, Object executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            if (executor instanceof org.bukkit.command.CommandExecutor ce) {
                cmd.setExecutor(ce);
            }
            if (executor instanceof org.bukkit.command.TabCompleter tc) {
                cmd.setTabCompleter(tc);
            }
        }
    }

    // ==========================================
    // ApexsionsChatAPI Implementation
    // ==========================================

    @Override
    public @NotNull CompletableFuture<Long> sendMail(@NotNull UUID senderUuid, @NotNull String senderName,
                                                     @NotNull UUID recipientUuid, @NotNull String recipientName,
                                                     @NotNull String subject, @NotNull String body) {
        Mail mail = new Mail(senderUuid, senderName, recipientUuid, recipientName, subject, body);
        return mailRepository.sendMailAsync(mail);
    }

    @Override
    public @NotNull CompletableFuture<Integer> getUnreadMailCount(@NotNull UUID playerUuid) {
        return mailRepository.countUnreadMailAsync(playerUuid);
    }

    @Override
    public @NotNull CompletableFuture<Long> createReport(@NotNull Report report) {
        return reportRepository.createReportAsync(report);
    }

    @Override
    public @NotNull ChatChannel getPlayerChannel(@NotNull Player player) {
        return channelManager.getPlayerChannel(player);
    }

    @Override
    public boolean setPlayerChannel(@NotNull Player player, @NotNull String channelId) {
        return channelManager.setPlayerChannel(player, channelId);
    }

    @Override
    public void broadcastAnnouncement(@NotNull String miniMessageContent) {
        Bukkit.broadcast(MiniMessage.miniMessage().deserialize(miniMessageContent));
    }

    // ==========================================
    // Getters
    // ==========================================

    public static ApexsionsChatPlugin getInstance() { return instance; }

    public ChatConfigManager getConfigManager() { return configManager; }
    public ChatDatabaseManager getDatabaseManager() { return databaseManager; }
    public ReportRepository getReportRepository() { return reportRepository; }
    public MailRepository getMailRepository() { return mailRepository; }
    public ModerationLogRepository getModerationLogRepository() { return moderationLogRepository; }

    public ApexsionsCoreHook getApexsionsCoreHook() { return apexsionsCoreHook; }
    public LuckPermsHook getLuckPermsHook() { return luckPermsHook; }
    public VaultHook getVaultHook() { return vaultHook; }

    public ChannelManager getChannelManager() { return channelManager; }
    public ModerationEngine getModerationEngine() { return moderationEngine; }
    public MentionParser getMentionParser() { return mentionParser; }
    public ItemShowcaseService getItemShowcaseService() { return itemShowcaseService; }
    public ChatFormatter getChatFormatter() { return chatFormatter; }
    public ChatGameManager getGameManager() { return gameManager; }
    public AnnouncementManager getAnnouncementManager() { return announcementManager; }
}
