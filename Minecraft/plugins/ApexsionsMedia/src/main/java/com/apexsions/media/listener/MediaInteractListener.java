package com.apexsions.media.listener;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.banner.MediaBanner;
import com.apexsions.media.gui.MediaConfirmGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MediaInteractListener implements Listener {

    private final ApexsionsMediaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public MediaInteractListener(ApexsionsMediaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (e.getPlayer().isOnline()) {
                plugin.getBannerManager().sendAllBannersToPlayer(e.getPlayer());
            }
        }, 15L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (e.getPlayer().isOnline()) {
                plugin.getBannerManager().sendAllBannersToPlayer(e.getPlayer());
            }
        }, 10L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof ItemFrame frame)) return;

        MediaBanner banner = plugin.getBannerManager().getBannerByFrame(frame.getUniqueId());
        if (banner != null) {
            e.setCancelled(true);
            triggerBannerAction(e.getPlayer(), banner);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        MediaBanner banner = plugin.getRaytraceService().getHoveredBanner(player);

        if (banner != null && e.getAction().name().contains("RIGHT_CLICK")) {
            e.setCancelled(true);
            triggerBannerAction(player, banner);
        }
    }

    private void triggerBannerAction(Player player, MediaBanner banner) {
        if (!player.hasPermission("apexsionsmedia.interact")) return;

        long now = System.currentTimeMillis();
        long last = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long cooldownMs = plugin.getConfig().getLong("interaction.cooldown-seconds", 2L) * 1000L;

        if (now - last < cooldownMs) {
            return;
        }
        cooldowns.put(player.getUniqueId(), now);

        String link = banner.getLinkUrl();
        if (link == null || link.isBlank()) {
            player.sendMessage(miniMessage.deserialize("<yellow>Banner <gold>" + banner.getId() + "</gold> tidak memiliki tautan URL yang terpasang.</yellow>"));
            return;
        }

        MediaBanner.ClickMode mode = banner.getClickMode();

        if (mode == MediaBanner.ClickMode.GUI_CONFIRM) {
            player.openInventory(new MediaConfirmGUI(banner, player).getInventory());
        } else {
            // Mode CHAT_PROMPT or DIRECT_URL
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.4f);

            Component openBtn = miniMessage.deserialize("<green><bold>[🌐 BUKA URL]</bold></green>")
                    .clickEvent(ClickEvent.openUrl(link))
                    .hoverEvent(HoverEvent.showText(miniMessage.deserialize("<green>Buka link di peramban web: <underlined>" + link + "</underlined></green>")));

            Component copyBtn = miniMessage.deserialize("<aqua><bold>[📋 SALIN LINK]</bold></aqua>")
                    .clickEvent(ClickEvent.copyToClipboard(link))
                    .hoverEvent(HoverEvent.showText(miniMessage.deserialize("<aqua>Salin URL ke clipboard</aqua>")));

            Component header = miniMessage.deserialize(
                    "\n<gradient:#f39c12:#f1c40f><bold>✦ INFORMASI TAUTAN BANNER ✦</bold></gradient>\n" +
                            "<gray>Tautan: </gray><yellow>" + link + "</yellow>\n" +
                            "<gray>Pilih aksi: </gray>"
            );

            Component message = header.append(openBtn).append(miniMessage.deserialize("  ")).append(copyBtn).append(miniMessage.deserialize("\n"));
            player.sendMessage(message);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFrameDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof ItemFrame frame) {
            if (plugin.getBannerManager().getBannerByFrame(frame.getUniqueId()) != null) {
                if (e.getDamager() instanceof Player p && p.hasPermission("apexsionsmedia.admin") && p.isSneaking()) {
                    return; // Allow sneak-breaking by admin
                }
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent e) {
        if (e.getEntity() instanceof ItemFrame frame) {
            if (plugin.getBannerManager().getBannerByFrame(frame.getUniqueId()) != null) {
                if (e instanceof HangingBreakByEntityEvent hbe && hbe.getRemover() instanceof Player p && p.hasPermission("apexsionsmedia.admin") && p.isSneaking()) {
                    return;
                }
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof MediaConfirmGUI gui) {
            gui.handleClick(e);
        } else if (e.getInventory().getHolder() instanceof com.apexsions.media.gui.MediaAdminGUI adminGUI) {
            adminGUI.handleClick(e);
        }
    }
}
