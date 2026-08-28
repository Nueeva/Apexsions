package com.apexsions.chat.announcement;

import com.apexsions.chat.ApexsionsChatPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;

public class AnnouncementManager {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private BukkitTask task;
    private int currentIndex = 0;
    private final Random random = new Random();

    public AnnouncementManager(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    public void startScheduler() {
        stopScheduler();

        FileConfiguration config = plugin.getConfigManager().getAnnouncementsConfig();
        if (!config.getBoolean("announcements.enabled", true)) {
            return;
        }

        int intervalSec = config.getInt("announcements.interval-seconds", 600);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::broadcastNext, intervalSec * 20L, intervalSec * 20L);
    }

    public void stopScheduler() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    public void broadcastNext() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;

        FileConfiguration config = plugin.getConfigManager().getAnnouncementsConfig();
        List<String> messages = config.getStringList("announcements.messages");
        if (messages.isEmpty()) return;

        String prefix = config.getString("announcements.prefix", "<gradient:#06b6d4:#3b82f6><bold>ANNOUNCEMENT</bold></gradient> <dark_gray>»</dark_gray> ");
        boolean randomOrder = config.getBoolean("announcements.random-order", false);

        String msg;
        if (randomOrder) {
            msg = messages.get(random.nextInt(messages.size()));
        } else {
            msg = messages.get(currentIndex % messages.size());
            currentIndex++;
        }

        Component component = miniMessage.deserialize(prefix + msg);
        Bukkit.broadcast(component);
    }
}
