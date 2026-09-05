package com.apexsions.chat.config;

import com.apexsions.chat.ApexsionsChatPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Manages modular configuration files for ApexsionsChat across categorized folders:
 * channels/, moderation/, broadcast/, games/, mail/, and root config.yml.
 * Includes dual-path fallback loader to preserve existing server data.
 */
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
        this.mainConfig = loadConfig("config.yml", "config.yml");
        this.channelsConfig = loadConfig("channels/channels.yml", "channels.yml");
        this.moderationConfig = loadConfig("moderation/moderation.yml", "moderation.yml");
        this.reportsConfig = loadConfig("moderation/reports.yml", "reports.yml");
        this.gamesConfig = loadConfig("games/games.yml", "games.yml");
        this.announcementsConfig = loadConfig("broadcast/announcements.yml", "announcements.yml");
        this.mailConfig = loadConfig("mail/mail.yml", "mail.yml");
    }

    private FileConfiguration loadConfig(String targetPath, String legacyPath) {
        File file = new File(plugin.getDataFolder(), targetPath);
        File legacyFile = legacyPath != null ? new File(plugin.getDataFolder(), legacyPath) : null;

        // If legacy file exists and target doesn't, use legacy file for backward compatibility
        if (!file.exists() && legacyFile != null && legacyFile.exists()) {
            file = legacyFile;
        } else if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                plugin.saveResource(targetPath, false);
            } catch (Exception e) {
                if (legacyPath != null) {
                    try {
                        plugin.saveResource(legacyPath, false);
                    } catch (Exception ignored) {}
                }
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        InputStream defStream = plugin.getResource(targetPath);
        if (defStream == null && legacyPath != null) {
            defStream = plugin.getResource(legacyPath);
        }
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
