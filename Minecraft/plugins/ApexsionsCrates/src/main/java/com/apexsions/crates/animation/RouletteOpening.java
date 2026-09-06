package com.apexsions.crates.animation;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.api.CrateMilestoneEvent;
import com.apexsions.crates.api.CrateRewardWinEvent;
import com.apexsions.crates.crate.Crate;
import com.apexsions.crates.milestone.Milestone;
import com.apexsions.crates.reward.Reward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class RouletteOpening extends OpeningSession implements InventoryHolder {

    private final Inventory inventory;
    private final List<Reward> track = new ArrayList<>();
    private int step = 0;
    private final int totalSteps = 38;
    private BukkitTask task;

    public RouletteOpening(ApexsionsCratesPlugin plugin, Player player, Crate crate, boolean virtualKey) {
        super(plugin, player, crate, virtualKey);
        this.inventory = Bukkit.createInventory(this, 27, MiniMessage.miniMessage().deserialize(crate.getName()));
        prepareTrack();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    private void prepareTrack() {
        // Build an exciting random track, placing winningReward exactly at target step for slot 13!
        // Slot 13 is the 5th slot in the middle row (9,10,11,12, 13 ,14,15,16,17).
        // Since track shifts left each step, the item at index (step + 4) will land at slot 13!
        int targetIndex = totalSteps + 4;
        List<Reward> allRewards = new ArrayList<>(crate.getRewards());

        for (int i = 0; i < targetIndex + 10; i++) {
            if (i == targetIndex) {
                track.add(winningReward);
            } else {
                track.add(crate.rollReward());
            }
        }
    }

    @Override
    public void start() {
        renderBorders();
        updateRow(0);
        player.openInventory(inventory);
        scheduleNextStep(1);
    }

    private void scheduleNextStep(int delayTicks) {
        if (completed || !player.isOnline()) return;

        task = new BukkitRunnable() {
            @Override
            public void run() {
                step++;
                updateRow(step);

                // Play tick sound with pitch curve
                float pitch = 0.8f + ((float) step / (float) totalSteps) * 0.9f;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, pitch);

                if (step >= totalSteps) {
                    finish();
                } else {
                    // Deceleration physics formula
                    int nextDelay = 1;
                    if (step > 33) nextDelay = 12;
                    else if (step > 30) nextDelay = 8;
                    else if (step > 26) nextDelay = 5;
                    else if (step > 20) nextDelay = 3;
                    else if (step > 15) nextDelay = 2;

                    scheduleNextStep(nextDelay);
                }
            }
        }.runTaskLater(plugin, delayTicks);
    }

    private void renderBorders() {
        ItemStack darkGlass = createPane(Material.BLACK_STAINED_GLASS_PANE, "<gray> </gray>");
        ItemStack topIndicator = createPane(Material.HOPPER, "<gold>▼ <yellow><bold>HADIAH ANDA</bold></yellow> ▼</gold>");
        ItemStack skipBtn = createPane(Material.SPECTRAL_ARROW, "<gradient:#f39c12:#e74c3c><bold>⚡ SKIP ANIMASI</bold></gradient>");

        // Top row (0-8)
        for (int i = 0; i < 9; i++) {
            if (i == 4) {
                inventory.setItem(i, topIndicator);
            } else {
                inventory.setItem(i, darkGlass);
            }
        }

        // Bottom row (18-26)
        for (int i = 18; i < 27; i++) {
            if (i == 22) {
                inventory.setItem(i, skipBtn);
            } else {
                inventory.setItem(i, darkGlass);
            }
        }
    }

    private void updateRow(int currentStep) {
        // Middle row: slots 9 through 17
        for (int col = 0; col < 9; col++) {
            int trackIndex = currentStep + col;
            if (trackIndex < track.size()) {
                Reward r = track.get(trackIndex);
                inventory.setItem(9 + col, r.createDisplayItem(crate.getRewardChance(r)));
            }
        }
    }

    private ItemStack createPane(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MiniMessage.miniMessage().deserialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void skip() {
        if (completed) return;
        if (task != null) task.cancel();
        step = totalSteps;
        updateRow(step);
        finish();
    }

    @Override
    public void cancel() {
        if (completed) return;
        if (task != null) task.cancel();
        finish();
    }

    private void finish() {
        if (completed) return;
        completed = true;
        if (task != null) task.cancel();

        // 1. Give reward
        winningReward.give(player);

        // 2. Play celebratory sounds and firework
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
        spawnFirework(player);

        // 3. Show title
        Component titleMain = winningReward.getDisplayName();
        Component subTitle = MiniMessage.miniMessage().deserialize("<gray>Kelangkaan: </gray>" + winningReward.getRarity().getFormattedName());
        player.showTitle(Title.title(titleMain, subTitle, Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(3), Duration.ofMillis(500))));

        // 4. Chat notification
        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<gradient:#f1c40f:#e67e22><bold>SELAMAT!</bold></gradient> <gray>Anda mendapatkan </gray>" +
                        winningReward.getName() + " <gray>dari Peti </gray>" + crate.getName() + "<gray>!</gray>"
        ));

        // 5. Global broadcast if broadcast enabled
        if (winningReward.isBroadcast() || winningReward.getRarity().getTier() >= 5) {
            Bukkit.broadcast(MiniMessage.miniMessage().deserialize(
                    "<gradient:#f1c40f:#e67e22><bold>CRATES</bold></gradient> <dark_gray>»</dark_gray> <aqua>" +
                            player.getName() + "</aqua> <gray>memenangkan </gray>" + winningReward.getName() +
                            " <gray>dari Peti </gray>" + crate.getName() + "<gray>!</gray>"
            ));
        }

        // 6. Update database stats & milestones
        plugin.getRepository().incrementCrateOpenCount(player.getUniqueId(), crate.getId()).thenAccept(newCount -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Milestone ms = crate.getMilestone(newCount);
                if (ms != null) {
                    ms.award(player);
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<gradient:#3498db:#2ecc71><bold>MILESTONE TERCAPAI!</bold></gradient> <gray>Anda telah membuka </gray><yellow>" +
                                    newCount + "x</yellow> <gray>Peti </gray>" + crate.getName() + "<gray>!</gray>"
                    ));
                    Bukkit.getPluginManager().callEvent(new CrateMilestoneEvent(player, crate, ms, newCount));
                }
            });
        });

        // 7. Fire custom event
        Bukkit.getPluginManager().callEvent(new CrateRewardWinEvent(player, crate, winningReward));

        // Unregister session from plugin
        plugin.unregisterSession(player.getUniqueId());

        // Close after 2.5 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.getOpenInventory().getTopInventory().equals(inventory)) {
                player.closeInventory();
            }
        }, 50L);
    }

    private void spawnFirework(Player player) {
        try {
            Firework fw = player.getWorld().spawn(player.getLocation().add(0, 1, 0), Firework.class);
            FireworkMeta fwm = fw.getFireworkMeta();
            fwm.addEffect(FireworkEffect.builder()
                    .withColor(Color.ORANGE, Color.YELLOW, Color.WHITE)
                    .withFade(Color.PURPLE)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .flicker(true)
                    .trail(true)
                    .build());
            fwm.setPower(0);
            fw.setFireworkMeta(fwm);
        } catch (Exception ignored) {}
    }
}
