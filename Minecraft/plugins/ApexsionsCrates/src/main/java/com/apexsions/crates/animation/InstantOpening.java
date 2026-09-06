package com.apexsions.crates.animation;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.api.CrateMilestoneEvent;
import com.apexsions.crates.api.CrateRewardWinEvent;
import com.apexsions.crates.crate.Crate;
import com.apexsions.crates.milestone.Milestone;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.time.Duration;

public class InstantOpening extends OpeningSession {

    public InstantOpening(ApexsionsCratesPlugin plugin, Player player, Crate crate, boolean virtualKey) {
        super(plugin, player, crate, virtualKey);
    }

    @Override
    public void start() {
        finish();
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

        // 1. Give reward
        winningReward.give(player);

        // 2. Play particle & sound
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
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
            Firework fw = player.getWorld().spawn(player.getLocation().add(0, 1, 0), Firework.class);
            FireworkMeta fwm = fw.getFireworkMeta();
            fwm.addEffect(FireworkEffect.builder()
                    .withColor(Color.YELLOW, Color.ORANGE)
                    .with(FireworkEffect.Type.BALL)
                    .build());
            fwm.setPower(0);
            fw.setFireworkMeta(fwm);
        } catch (Exception ignored) {}
    }
}
