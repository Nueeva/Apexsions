package com.yourserver.apexsionschat.game;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ChatGameManager {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final AtomicReference<ChatGame> activeGame = new AtomicReference<>(null);
    private final AtomicBoolean winnerClaimed = new AtomicBoolean(false);
    private long gameStartTime = 0L;
    private BukkitTask timeoutTask;
    private BukkitTask loopTask;
    private final Random random = new Random();

    public ChatGameManager(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    public void startScheduler() {
        stopScheduler();
        scheduleNextGame();
    }

    public void stopScheduler() {
        if (loopTask != null && !loopTask.isCancelled()) {
            loopTask.cancel();
        }
        if (timeoutTask != null && !timeoutTask.isCancelled()) {
            timeoutTask.cancel();
        }
        activeGame.set(null);
    }

    private void scheduleNextGame() {
        FileConfiguration config = plugin.getConfigManager().getGamesConfig();
        if (!config.getBoolean("games.enabled", true)) {
            return;
        }

        int minSec = config.getInt("games.minimum-interval-seconds", 300);
        int maxSec = config.getInt("games.maximum-interval-seconds", 600);
        int delaySec = minSec + random.nextInt(Math.max(1, (maxSec - minSec) + 1));

        loopTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                scheduleNextGame();
                return;
            }
            startRandomGame();
        }, delaySec * 20L);
    }

    public void startRandomGame() {
        if (activeGame.get() != null) return;

        FileConfiguration config = plugin.getConfigManager().getGamesConfig();
        int unscrambleWeight = config.getInt("games.unscramble.weight", 50);
        int mathWeight = config.getInt("games.math.weight", 50);
        int totalWeight = unscrambleWeight + mathWeight;

        ChatGame game;
        if (random.nextInt(totalWeight) < unscrambleWeight) {
            List<String> pool = config.getStringList("games.unscramble.words");
            game = new UnscrambleGame(pool);
        } else {
            int minOp = config.getInt("games.math.min-operators", 1);
            int maxOp = config.getInt("games.math.max-operators", 3);
            List<String> ops = config.getStringList("games.math.operators");
            int minNum = config.getInt("games.math.min-number", 1);
            int maxNum = config.getInt("games.math.max-number", 50);
            game = new QuickMathGame(minOp, maxOp, ops, minNum, maxNum);
        }

        winnerClaimed.set(false);
        activeGame.set(game);
        gameStartTime = System.currentTimeMillis();

        // Broadcast prompt to all online players
        Component prompt = game.getPromptComponent();
        Bukkit.broadcast(prompt);

        // Sound alert
        if (plugin.getConfigManager().getMainConfig().getBoolean("sounds.game-start.enabled", true)) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
            }
        }

        // Timeout task
        int timeoutSec = config.getInt("games.timeout-seconds", 45);
        timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ChatGame current = activeGame.getAndSet(null);
            if (current != null && !winnerClaimed.get()) {
                Bukkit.broadcast(miniMessage.deserialize(
                        "<gray>[<gold>⚡</gold>] <red>Time's up! Nobody answered the chat game in time. The answer was:</red> <yellow><bold>"
                                + current.getAnswer() + "</bold></yellow>"
                ));
            }
            scheduleNextGame();
        }, timeoutSec * 20L);
    }

    public boolean checkAnswer(Player player, String message) {
        ChatGame game = activeGame.get();
        if (game == null) return false;

        if (game.isCorrect(message)) {
            // Atomic check to prevent race conditions
            if (winnerClaimed.compareAndSet(false, true)) {
                activeGame.set(null);
                if (timeoutTask != null) timeoutTask.cancel();

                double elapsedSeconds = (System.currentTimeMillis() - gameStartTime) / 1000.0;
                String timeFormatted = String.format("%.1f", elapsedSeconds);

                // Broadcast win
                Bukkit.broadcast(miniMessage.deserialize(
                        "<dark_gray>══════════════════════════════════════════</dark_gray>\n" +
                        "<gradient:#22c55e:#16a34a><bold>🎉 CHAT GAME WINNER! 🎉</bold></gradient>\n" +
                        "<white>" + player.getName() + "</white> <gray>answered correctly in</gray> <yellow>" + timeFormatted + "s</yellow><gray>!</gray>\n" +
                        "<gray>Answer:</gray> <yellow><bold>" + game.getAnswer() + "</bold></yellow>\n" +
                        "<dark_gray>══════════════════════════════════════════</dark_gray>"
                ));

                // Sound
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                }

                // Grant rewards
                grantRewards(player);

                // Schedule next game
                scheduleNextGame();
                return true;
            }
        }
        return false;
    }

    private void grantRewards(Player player) {
        FileConfiguration config = plugin.getConfigManager().getGamesConfig();

        // 1. ApexsionsCore XP Reward
        if (config.getBoolean("games.rewards.xp.enabled", true)) {
            long xp = config.getLong("games.rewards.xp.amount", 150);
            plugin.getApexsionsCoreHook().addXp(player.getUniqueId(), xp);
            player.sendMessage(miniMessage.deserialize("<green>+<yellow>" + xp + " XP</yellow> rewarded for winning the chat game!</green>"));
        }

        // 2. Vault Economy Reward
        if (config.getBoolean("games.rewards.vault-money.enabled", false)) {
            double money = config.getDouble("games.rewards.vault-money.amount", 500);
            plugin.getVaultHook().deposit(player, money);
            player.sendMessage(miniMessage.deserialize("<green>+$" + money + " added to your account!</green>"));
        }

        // 3. Command Rewards
        List<String> commands = config.getStringList("games.rewards.commands");
        for (String cmd : commands) {
            String executable = cmd.replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), executable);
        }
    }

    public boolean isGameActive() {
        return activeGame.get() != null;
    }
}
