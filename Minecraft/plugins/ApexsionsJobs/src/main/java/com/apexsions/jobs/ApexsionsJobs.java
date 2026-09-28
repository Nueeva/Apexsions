package com.apexsions.jobs;

import com.apexsions.jobs.command.JobsAdminCommand;
import com.apexsions.jobs.command.JobsCommand;
import com.apexsions.jobs.database.JobsDatabaseManager;
import com.apexsions.jobs.database.JobsRepository;
import com.apexsions.jobs.integration.PlaceholderAPIExpansion;
import com.apexsions.jobs.listener.JobActivityListener;
import com.apexsions.jobs.listener.PlayerSessionListener;
import com.apexsions.jobs.model.PlayerJobData;
import com.apexsions.jobs.service.JobService;
import com.apexsions.jobs.service.PlacedBlockTracker;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

public class ApexsionsJobs extends JavaPlugin {

    private static ApexsionsJobs instance;

    private JobsDatabaseManager databaseManager;
    private JobsRepository repository;
    private PlacedBlockTracker placedBlockTracker;
    private JobService jobService;

    private FileConfiguration messagesConfig;
    private File messagesFile;

    public static ApexsionsJobs getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        loadMessages();

        // 1. Initialize Database & Repository
        this.databaseManager = new JobsDatabaseManager(this);
        this.databaseManager.initialize();
        this.repository = new JobsRepository(this, databaseManager);

        // 2. Initialize Services & Anti-Abuse Tracker
        this.placedBlockTracker = new PlacedBlockTracker(repository);
        this.jobService = new JobService(this);

        // 3. Register Listeners
        Bukkit.getPluginManager().registerEvents(new PlayerSessionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new JobActivityListener(this), this);
        Bukkit.getPluginManager().registerEvents(new com.apexsions.jobs.gui.JobsGuiListener(), this);

        // 4. Register Commands
        if (getCommand("jobs") != null) {
            getCommand("jobs").setExecutor(new JobsCommand(this));
        }
        if (getCommand("jobsadmin") != null) {
            JobsAdminCommand adminCmd = new JobsAdminCommand(this);
            getCommand("jobsadmin").setExecutor(adminCmd);
            getCommand("jobsadmin").setTabCompleter(adminCmd);
        }

        // 5. PlaceholderAPI Hook
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIExpansion(this).register();
        }

        // 6. Online players warm-up
        for (Player p : Bukkit.getOnlinePlayers()) {
            repository.loadPlayerJobs(p.getUniqueId()).thenAccept(jobs -> {
                jobService.setPlayerJobs(p.getUniqueId(), jobs);
            });
        }

        getLogger().info("ApexsionsJobs v1.0.0 berhasil diaktifkan dengan Native Dialog GUI & Anti-Exploit Guard!");
    }

    @Override
    public void onDisable() {
        // Save all cached player jobs
        for (Player p : Bukkit.getOnlinePlayers()) {
            Map<String, PlayerJobData> map = jobService.getPlayerJobs(p.getUniqueId());
            if (map != null) {
                for (PlayerJobData data : map.values()) {
                    repository.savePlayerJob(data).join();
                }
            }
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("ApexsionsJobs dinonaktifkan.");
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

    public JobsDatabaseManager getDatabaseManager() { return databaseManager; }
    public JobsRepository getRepository() { return repository; }
    public PlacedBlockTracker getPlacedBlockTracker() { return placedBlockTracker; }
    public JobService getJobService() { return jobService; }
}
