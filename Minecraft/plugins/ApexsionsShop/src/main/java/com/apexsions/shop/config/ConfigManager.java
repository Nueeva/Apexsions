package com.apexsions.shop.config;

import com.apexsions.shop.ApexsionsShop;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {

    private final ApexsionsShop plugin;

    private FileConfiguration messagesConfig;
    private FileConfiguration guiConfig;
    private FileConfiguration marketsConfig;

    private File messagesFile;
    private File guiFile;
    private File marketsFile;

    public ConfigManager(ApexsionsShop plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        mergeDefaults(messagesConfig, "messages.yml");

        guiFile = new File(plugin.getDataFolder(), "gui.yml");
        if (!guiFile.exists()) {
            plugin.saveResource("gui.yml", false);
        }
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
        mergeDefaults(guiConfig, "gui.yml");

        marketsFile = new File(plugin.getDataFolder(), "markets.yml");
        if (!marketsFile.exists()) {
            plugin.saveResource("markets.yml", false);
        }
        marketsConfig = YamlConfiguration.loadConfiguration(marketsFile);
        mergeDefaults(marketsConfig, "markets.yml");
    }

    private void mergeDefaults(FileConfiguration config, String resourcePath) {
        InputStream defStream = plugin.getResource(resourcePath);
        if (defStream != null) {
            config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defStream, StandardCharsets.UTF_8)));
        }
    }

    public FileConfiguration getMainConfig() {
        return plugin.getConfig();
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig != null ? messagesConfig : plugin.getConfig();
    }

    public FileConfiguration getGuiConfig() {
        return guiConfig != null ? guiConfig : plugin.getConfig();
    }

    public FileConfiguration getMarketsConfig() {
        return marketsConfig != null ? marketsConfig : plugin.getConfig();
    }

    public String getMessage(String key, String defaultMsg) {
        String prefix = getMessagesConfig().getString("prefix", "<gold><bold>ApexsionsShop</bold></gold> <dark_gray>»</dark_gray> ");
        String msg = getMessagesConfig().getString(key, defaultMsg);
        return prefix + msg;
    }

    public String getRawMessage(String key, String defaultMsg) {
        return getMessagesConfig().getString(key, defaultMsg);
    }
}
