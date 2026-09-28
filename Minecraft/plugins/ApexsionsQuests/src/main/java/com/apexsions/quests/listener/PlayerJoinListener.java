package com.apexsions.quests.listener;

import com.apexsions.quests.ApexsionsQuests;
import com.apexsions.quests.gui.dialog.DailyLoginDialog;
import com.apexsions.quests.model.PlayerStreakData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final ApexsionsQuests plugin;

    public PlayerJoinListener(ApexsionsQuests plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // 1. Load streak data async
        plugin.getRepository().loadStreakData(uuid).thenAccept(data -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                plugin.getStreakService().setStreakData(uuid, data);

                // 2. Ensure daily quests assigned for today
                plugin.getQuestManager().ensureDailyQuestsAssigned(player);

                // 3. Auto-prompt Daily Login Dialog if today is unclaimed
                boolean autoPrompt = plugin.getConfig().getBoolean("calendar.auto-prompt-on-join", true);
                if (autoPrompt && !plugin.getCalendarService().isTodayClaimed(data)) {
                    // Delay 30 ticks so player screen and client world finish loading
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline()) {
                            DailyLoginDialog.open(plugin, player);
                        }
                    }, 30L);
                }
            });
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PlayerStreakData data = plugin.getStreakService().getStreakData(uuid);
        if (data != null) {
            plugin.getRepository().saveStreakData(data);
            plugin.getStreakService().removeStreakData(uuid);
        }
        plugin.getQuestManager().removePlayerQuests(uuid);
    }
}
