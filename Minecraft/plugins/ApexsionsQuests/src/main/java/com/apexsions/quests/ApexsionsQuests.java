package com.apexsions.quests;

import com.apexsions.quests.command.DailyCommand;
import com.apexsions.quests.command.QuestsAdminCommand;
import com.apexsions.quests.command.QuestsCommand;
import com.apexsions.quests.database.DatabaseManager;
import com.apexsions.quests.database.QuestsRepository;
import com.apexsions.quests.integration.PlaceholderAPIExpansion;
import com.apexsions.quests.listener.PlayerJoinListener;
import com.apexsions.quests.listener.QuestEventListener;
import com.apexsions.quests.model.PlayerStreakData;
import com.apexsions.quests.service.CalendarService;
import com.apexsions.quests.service.QuestManager;
import com.apexsions.quests.service.StreakService;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ApexsionsQuests extends JavaPlugin {

    private static ApexsionsQuests instance;

    private DatabaseManager databaseManager;
    private QuestsRepository repository;
    private CalendarService calendarService;
    private StreakService streakService;
    private QuestManager questManager;

    private FileConfiguration messagesConfig;
    private File messagesFile;

    public static ApexsionsQuests getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        loadMessages();

        // 1. Initialize Database & Repository
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.initialize();
        this.repository = new QuestsRepository(this, databaseManager);

        // 2. Initialize Services
        this.calendarService = new CalendarService(this);
        this.streakService = new StreakService(this);
        this.questManager = new QuestManager(this);

        // 3. Register Listeners
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new QuestEventListener(this), this);

        // 4. Register Commands
        if (getCommand("daily") != null) {
            getCommand("daily").setExecutor(new DailyCommand(this));
        }
        if (getCommand("quests") != null) {
            getCommand("quests").setExecutor(new QuestsCommand(this));
        }
        if (getCommand("questsadmin") != null) {
            QuestsAdminCommand adminCmd = new QuestsAdminCommand(this);
            getCommand("questsadmin").setExecutor(adminCmd);
            getCommand("questsadmin").setTabCompleter(adminCmd);
        }

        // 5. PlaceholderAPI Hook
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIExpansion(this).register();
        }

        // 6. Online players warm-up
        for (Player p : Bukkit.getOnlinePlayers()) {
            repository.loadStreakData(p.getUniqueId()).thenAccept(data -> {
                streakService.setStreakData(p.getUniqueId(), data);
                questManager.ensureDailyQuestsAssigned(p);
            });
        }

        getLogger().info("ApexsionsQuests v1.0.0 berhasil diaktifkan dengan Native Dialog GUI & Adaptive Dual-UI!");
    }

    @Override
    public void onDisable() {
        // Save all cached streak data
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerStreakData data = streakService.getStreakData(p.getUniqueId());
            if (data != null) {
                repository.saveStreakData(data).join();
            }
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("ApexsionsQuests dinonaktifkan.");
    }

    public void loadMessages() {
        this.messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMsg(String path) {
        if (messagesConfig == null) loadMessages();
        String prefix = messagesConfig.getString("prefix", "");
        return prefix + messagesConfig.getString(path, path);
    }

    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public QuestsRepository getRepository() { return repository; }
    public CalendarService getCalendarService() { return calendarService; }
    public StreakService getStreakService() { return streakService; }
    public QuestManager getQuestManager() { return questManager; }
}
