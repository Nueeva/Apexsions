package com.apexsions.fishing.listener;

import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.model.FishRarity;
import com.apexsions.fishing.service.LootGenerator;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class FishingListener implements Listener {

    private final ApexsionsFishing plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public FishingListener(ApexsionsFishing plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack rod = player.getInventory().getItemInMainHand();
        if (rod.getType() != org.bukkit.Material.FISHING_ROD) {
            rod = player.getInventory().getItemInOffHand();
        }

        switch (event.getState()) {
            case FISHING:
                // Hook cast into water
                if (event.getHook() != null && plugin.getRodManager().isAutoCatchRod(rod)) {
                    plugin.getAfkFishingService().registerCast(player, event.getHook(), rod);
                }
                break;

            case BITE:
                // Auto-catch trigger on bite!
                if (plugin.getRodManager().isAutoCatchRod(rod) && event.getHook() != null) {
                    plugin.getAfkFishingService().triggerBiteCatch(player, event.getHook(), rod);
                }
                break;

            case REEL_IN:
            case IN_GROUND:
            case FAILED_ATTEMPT:
                plugin.getAfkFishingService().cancelCast(player);
                break;

            case CAUGHT_FISH:
                plugin.getAfkFishingService().cancelCast(player);
                // If player manually caught something, replace vanilla drop with our Rarity & Weight catch
                if (event.getCaught() instanceof Item caughtEntity) {
                    LootGenerator.CatchResult res = plugin.getLootGenerator().generateCatch(player, rod);
                    caughtEntity.setItemStack(res.item);

                    if (res.isFish) {
                        plugin.getVaultStorage().getStats(player.getUniqueId()).recordCatch(
                                res.lootItem.getId(),
                                res.lootItem.getDisplayName(),
                                res.weightKg,
                                res.isSecret
                        );
                    }

                    if (res.isSecret && plugin.getConfig().getBoolean("settings.broadcasts.secret-catch", true)) {
                        Bukkit.broadcast(mm.deserialize("<newline><gradient:#ff007f:#7928ca><bold>★ APEXSIONS SECRET DISCOVERY ★</bold></gradient><newline>" +
                                "<yellow>Pemancing <white><bold>" + player.getName() + "</bold></white> menangkap <gold>" +
                                res.lootItem.getDisplayName() + "</gold> seberat <yellow><bold>" + String.format("%.2f", res.weightKg) + " kg</bold></yellow>!<newline>"));
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.9f, 1.0f);
                        }
                    }
                }
                break;

            default:
                break;
        }
    }

    @EventHandler
    public void onItemHeldChange(org.bukkit.event.player.PlayerItemHeldEvent event) {
        plugin.getAfkFishingService().cancelCast(event.getPlayer());
    }

    @EventHandler
    public void onSwapHand(org.bukkit.event.player.PlayerSwapHandItemsEvent event) {
        plugin.getAfkFishingService().cancelCast(event.getPlayer());
    }

    @EventHandler
    public void onDropItem(org.bukkit.event.player.PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() == org.bukkit.Material.FISHING_ROD) {
            plugin.getAfkFishingService().cancelCast(event.getPlayer());
        }
    }

    @EventHandler
    public void onTeleport(org.bukkit.event.player.PlayerTeleportEvent event) {
        plugin.getAfkFishingService().cancelCast(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getAfkFishingService().cancelCast(event.getPlayer());
        plugin.getVaultStorage().savePlayerData(event.getPlayer().getUniqueId());
    }
}
