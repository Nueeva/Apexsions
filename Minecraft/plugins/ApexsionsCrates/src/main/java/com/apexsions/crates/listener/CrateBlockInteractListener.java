package com.apexsions.crates.listener;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.crate.Crate;
import com.apexsions.crates.crate.CrateLocation;
import com.apexsions.crates.gui.CratePreviewGUI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class CrateBlockInteractListener implements Listener {

    private final ApexsionsCratesPlugin plugin;

    public CrateBlockInteractListener(ApexsionsCratesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        CrateLocation cl = plugin.getCrateManager().getCrateLocationAt(block.getLocation());
        if (cl == null) return;

        Crate crate = plugin.getCrateManager().getCrate(cl.getCrateId());
        if (crate == null) return;

        Player player = event.getPlayer();

        // 1. LEFT CLICK -> Preview Rewards
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            new CratePreviewGUI(plugin, player, crate).open();
            return;
        }

        // 2. RIGHT CLICK -> Open Crate
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);

            if (plugin.isOpening(player.getUniqueId())) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        plugin.getMessages().getString("opening-in-progress", "<red>Anda sedang membuka peti lain!</red>")
                ));
                return;
            }

            if (plugin.getCrateManager().isOnCooldown(player.getUniqueId(), crate.getId(), crate.getCooldownSeconds())) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Peti ini sedang cooldown!</red>"));
                return;
            }

            String reqKey = crate.getRequiredKeyId();
            boolean isInstant = player.isSneaking();

            // Check Physical Key first
            if (plugin.getKeyManager().hasPhysicalKey(player, reqKey, 1)) {
                if (plugin.getKeyManager().takePhysicalKey(player, reqKey, 1)) {
                    plugin.getCrateManager().applyCooldown(player.getUniqueId(), crate.getId(), crate.getCooldownSeconds());
                    if (isInstant) {
                        plugin.openCrateInstant(player, crate, false);
                    } else {
                        plugin.startOpeningSession(player, crate, false, block.getLocation());
                    }
                }
                return;
            }

            // Check Virtual Key
            plugin.getRepository().takeVirtualKeys(player.getUniqueId(), reqKey, 1).thenAccept(success -> {
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    if (success) {
                        plugin.getCrateManager().applyCooldown(player.getUniqueId(), crate.getId(), crate.getCooldownSeconds());
                        if (isInstant) {
                            plugin.openCrateInstant(player, crate, true);
                        } else {
                            plugin.startOpeningSession(player, crate, true, block.getLocation());
                        }
                    } else {
                        // Locked feedback: pushback and sound
                        player.playSound(block.getLocation(), org.bukkit.Sound.BLOCK_CHEST_LOCKED, 1.0f, 0.8f);
                        org.bukkit.util.Vector push = player.getLocation().toVector()
                                .subtract(block.getLocation().add(0.5, 0.5, 0.5).toVector())
                                .normalize().multiply(0.45).setY(0.2);
                        if (!Double.isNaN(push.getX()) && !Double.isNaN(push.getZ())) {
                            player.setVelocity(push);
                        }

                        player.sendActionBar(MiniMessage.miniMessage().deserialize(
                                "<red>Peti Terkunci! Anda butuh <gold>Kunci " + reqKey + "</gold></red>"
                        ));

                        String msg = plugin.getMessages().getString("need-key", "<red>Anda membutuhkan kunci <yellow>%key%</yellow> untuk membuka peti ini!</red>");
                        player.sendMessage(MiniMessage.miniMessage().deserialize(msg.replace("%key%", reqKey)));
                    }
                });
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        CrateLocation cl = plugin.getCrateManager().getCrateLocationAt(block.getLocation());
        if (cl == null) return;

        Player player = event.getPlayer();
        if (player.hasPermission("apexsions.crates.admin") && player.isSneaking()) {
            plugin.getCrateManager().removeLocation(cl);
            plugin.getRepository().deleteLocation(cl);
            plugin.getHologramManager().removeHologram(cl);
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.getMessages().getString("crate-removed-success", "<yellow>Lokasi peti berhasil dihapus.</yellow>")
            ));
        } else {
            event.setCancelled(true);
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<red>Peti ini dilindungi! Gunakan <yellow>Shift + Hancurkan</yellow> jika Anda Admin untuk menghapusnya.</red>"
            ));
        }
    }
}
