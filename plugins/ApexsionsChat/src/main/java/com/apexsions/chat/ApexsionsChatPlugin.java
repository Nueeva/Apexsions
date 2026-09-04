package com.apexsions.chat;

import com.apexsions.chat.announcement.AnnouncementManager;
import com.apexsions.chat.api.ApexsionsChatAPI;
import com.apexsions.chat.api.ApexsionsChatProvider;
import com.apexsions.chat.channel.ChannelManager;
import com.apexsions.chat.channel.ChatChannel;
import com.apexsions.chat.chat.ChatFormatter;
import com.apexsions.chat.chat.ChatListener;
import com.apexsions.chat.chat.ItemShowcaseService;
import com.apexsions.chat.chat.MentionParser;
import com.apexsions.chat.command.*;
import com.apexsions.chat.config.ChatConfigManager;
import com.apexsions.chat.database.ChatDatabaseManager;
import com.apexsions.chat.database.MailRepository;
import com.apexsions.chat.database.ModerationLogRepository;
import com.apexsions.chat.database.NicknameRepository;
import com.apexsions.chat.database.ReportRepository;
import com.apexsions.chat.game.ChatGameManager;
import com.apexsions.chat.gui.GUIListener;
import com.apexsions.chat.integration.ApexsionsCoreHook;
import com.apexsions.chat.integration.LuckPermsHook;
import com.apexsions.chat.integration.PlaceholderApiHook;
import com.apexsions.chat.integration.VaultHook;
import com.apexsions.chat.nick.NicknameListener;
import com.apexsions.chat.nick.NicknameService;
import com.apexsions.chat.nick.gui.NickColorGUI;
import com.apexsions.chat.model.Mail;
import com.apexsions.chat.model.Report;
import com.apexsions.chat.moderation.ModerationEngine;
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
    private NicknameRepository nicknameRepository;

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
    private NicknameService nicknameService;
    private NickColorGUI nickColorGUI;

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
        this.nicknameRepository = new NicknameRepository(this, databaseManager);

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
        this.nicknameService = new NicknameService(this, nicknameRepository);
        this.nickColorGUI = new NickColorGUI(this);

        // 6. Register Public API
        ApexsionsChatProvider.register(this);

        // 7. Register Commands & Listeners
        registerCommands();
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new NicknameListener(this), this);
        getServer().getPluginManager().registerEvents(nickColorGUI, this);

        for (Player p : Bukkit.getOnlinePlayers()) {
            nicknameService.loadPlayer(p);
        }

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
        if (nicknameService != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                nicknameService.unloadPlayer(p.getUniqueId());
            }
        }
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

        NickCommand nickCmd = new NickCommand(this);
        registerCmd("nick", nickCmd);

        RealNameCommand realNameCmd = new RealNameCommand(this);
        registerCmd("realname", realNameCmd);
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
    public NicknameService getNicknameService() { return nicknameService; }
    public NickColorGUI getNickColorGUI() { return nickColorGUI; }
}
