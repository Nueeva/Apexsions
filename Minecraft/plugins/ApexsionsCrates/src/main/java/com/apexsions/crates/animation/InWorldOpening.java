package com.apexsions.crates.animation;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.api.CrateMilestoneEvent;
import com.apexsions.crates.api.CrateRewardWinEvent;
import com.apexsions.crates.crate.Crate;
import com.apexsions.crates.crate.CrateLocation;
import com.apexsions.crates.milestone.Milestone;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Display;
import org.bukkit.entity.Firework;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

public class InWorldOpening extends OpeningSession {

    private final Location crateLoc;
    private ItemDisplay displayEntity;
    private BukkitTask animTask;
    private int ticks = 0;

    public InWorldOpening(ApexsionsCratesPlugin plugin, Player player, Crate crate, boolean virtualKey, Location crateLoc) {
        super(plugin, player, crate, virtualKey);
        this.crateLoc = crateLoc != null ? crateLoc.clone().add(0.5, 1.0, 0.5) : player.getLocation().add(0, 1.0, 0);
    }

    @Override
    public void start() {
        World world = crateLoc.getWorld();
        if (world == null) {
            finish();
            return;
        }

        player.playSound(crateLoc, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        player.playSound(crateLoc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);

        ItemStack displayItem = winningReward.createDisplayItem(0);
        try {
            displayEntity = world.spawn(crateLoc, ItemDisplay.class, entity -> {
                entity.setItemStack(displayItem);
                entity.setBillboard(Display.Billboard.CENTER);
            });
        } catch (Exception ignored) {}

        animTask = new BukkitRunnable() {
            @Override
            public void run() {
                ticks += 2;
                if (displayEntity != null && displayEntity.isValid()) {
                    displayEntity.teleport(crateLoc.clone().add(0, (ticks * 0.03), 0));
                }

                // Spiral particle effect
                double angle = ticks * 0.3;
                double x = Math.cos(angle) * 0.7;
                double z = Math.sin(angle) * 0.7;
                world.spawnParticle(Particle.WITCH, crateLoc.clone().add(x, (ticks * 0.03), z), 3, 0, 0, 0, 0);
                world.spawnParticle(Particle.PORTAL, crateLoc.clone().add(-x, (ticks * 0.03), -z), 3, 0, 0, 0, 0);

                player.playSound(crateLoc, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f + (ticks * 0.02f));

                if (ticks >= 40) { // 2 seconds
                    finish();
                }
            }
        }.runTaskTimer(plugin, 1L, 2L);
    }

    @Override
    public void skip() {
        finish();
    }

    @Override
    public void cancel() {
        finish();
    }

    private void finish() {
        if (completed) return;
        completed = true;
        if (animTask != null) animTask.cancel();
        if (displayEntity != null && displayEntity.isValid()) {
            displayEntity.remove();
        }

        // 1. Give reward
        winningReward.give(player);

        // 2. Sound & fireworks
        player.playSound(crateLoc, Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        spawnFirework(player);

        // 3. Title
        Component titleMain = winningReward.getDisplayName();
        Component subTitle = MiniMessage.miniMessage().deserialize("<gray>Kelangkaan: </gray>" + winningReward.getRarity().getFormattedName());
        player.showTitle(Title.title(titleMain, subTitle, Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(400))));

        // 4. Chat notification
        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<gradient:#f1c40f:#e67e22><bold>SELAMAT!</bold></gradient> <gray>Anda mendapatkan </gray>" +
                        winningReward.getName() + " <gray>dari Peti </gray>" + crate.getName() + "<gray>!</gray>"
        ));

        // 5. Broadcast
        if (winningReward.isBroadcast() || winningReward.getRarity().getTier() >= 5) {
            Bukkit.broadcast(MiniMessage.miniMessage().deserialize(
                    "<gradient:#f1c40f:#e67e22><bold>CRATES</bold></gradient> <dark_gray>»</dark_gray> <aqua>" +
                            player.getName() + "</aqua> <gray>memenangkan </gray>" + winningReward.getName() +
                            " <gray>dari Peti </gray>" + crate.getName() + "<gray>!</gray>"
            ));
        }

        // 6. Milestone & stats
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

        // 7. Fire win event
        Bukkit.getPluginManager().callEvent(new CrateRewardWinEvent(player, crate, winningReward));

        plugin.unregisterSession(player.getUniqueId());
    }

    private void spawnFirework(Player player) {
        try {
            Firework fw = player.getWorld().spawn(crateLoc, Firework.class);
            FireworkMeta fwm = fw.getFireworkMeta();
            fwm.addEffect(FireworkEffect.builder()
                    .withColor(Color.FUCHSIA, Color.AQUA)
                    .with(FireworkEffect.Type.BURST)
                    .build());
            fwm.setPower(0);
            fw.setFireworkMeta(fwm);
        } catch (Exception ignored) {}
    }
}
