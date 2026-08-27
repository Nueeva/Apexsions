package com.yourserver.apexsionschat.config;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ChatConfigManager {

    private final ApexsionsChatPlugin plugin;
    private FileConfiguration mainConfig;
    private FileConfiguration channelsConfig;
    private FileConfiguration moderationConfig;
    private FileConfiguration gamesConfig;
    private FileConfiguration announcementsConfig;
    private FileConfiguration mailConfig;
    private FileConfiguration reportsConfig;

    public ChatConfigManager(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        this.mainConfig = loadConfig("config.yml");
        this.channelsConfig = loadConfig("channels.yml");
        this.moderationConfig = loadConfig("moderation.yml");
        this.gamesConfig = loadConfig("games.yml");
        this.announcementsConfig = loadConfig("announcements.yml");
        this.mailConfig = loadConfig("mail.yml");
        this.reportsConfig = loadConfig("reports.yml");
    }

    private FileConfiguration loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource(fileName, false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        InputStream defStream = plugin.getResource(fileName);
        if (defStream != null) {
            config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defStream, StandardCharsets.UTF_8)));
        }
        return config;
    }

    public FileConfiguration getMainConfig() { return mainConfig; }
    public FileConfiguration getChannelsConfig() { return channelsConfig; }
    public FileConfiguration getModerationConfig() { return moderationConfig; }
    public FileConfiguration getGamesConfig() { return gamesConfig; }
    public FileConfiguration getAnnouncementsConfig() { return announcementsConfig; }
    public FileConfiguration getMailConfig() { return mailConfig; }
    public FileConfiguration getReportsConfig() { return reportsConfig; }
}
