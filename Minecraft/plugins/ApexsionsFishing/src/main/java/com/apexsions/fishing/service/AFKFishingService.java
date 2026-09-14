package com.apexsions.fishing.service;

import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.model.FishRarity;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AFKFishingService {

    private final ApexsionsFishing plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // Map: Player UUID -> Active Auto-Catch Task
    private final Map<UUID, BukkitTask> activeTasks = new ConcurrentHashMap<>();

    public AFKFishingService(ApexsionsFishing plugin) {
        this.plugin = plugin;
    }

    public void registerCast(Player player, FishHook hook, ItemStack rod) {
        if (!plugin.getConfig().getBoolean("settings.afk-fishing.enabled", true)) {
            return;
        }

        if (!plugin.getRodManager().isAutoCatchRod(rod)) {
            return;
        }

        int minLevel = plugin.getRodManager().getMinLevel(rod);
        int playerLevel = plugin.getPlayerCoreLevel(player);
        if (minLevel > 0 && playerLevel < minLevel) {
            cancelCast(player);
            if (hook != null && !hook.isDead()) {
                hook.remove();
            }
            return;
        }

        cancelCast(player);

        int delaySeconds = plugin.getRodManager().getCatchSpeed(rod);
        long delayTicks = (long) delaySeconds * 20L;

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || hook.isDead() || !hook.isValid()) {
                cancelCast(player);
                return;
            }

            // Perform auto-catch
            performAutoCatch(player, hook, rod);
        }, delayTicks);

        activeTasks.put(player.getUniqueId(), task);
    }

    public void triggerBiteCatch(Player player, FishHook hook, ItemStack rod) {
        cancelCast(player);
        performAutoCatch(player, hook, rod);
    }

    public void cancelCast(Player player) {
        BukkitTask task = activeTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    public void cancelAll() {
        for (BukkitTask task : activeTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        activeTasks.clear();
    }

    private void performAutoCatch(Player player, FishHook hook, ItemStack rod) {
        activeTasks.remove(player.getUniqueId());

        if (!player.isOnline() || hook == null || hook.isDead() || !hook.isValid()) {
            return;
        }

        // 1. Water presence and depth check
        if (!hook.isInWater()) {
            return;
        }

        int minDepth = plugin.getConfig().getInt("settings.afk-fishing.min-water-depth", 2);
        if (minDepth > 1) {
            Location checkLoc = hook.getLocation().clone();
            // Start from first liquid block at or below hook to avoid bobbing surface float inaccuracies
            if (!checkLoc.getBlock().isLiquid()) {
                checkLoc.subtract(0, 0.5, 0);
            }
            boolean hasDepth = true;
            for (int d = 0; d < minDepth; d++) {
                if (!checkLoc.getBlock().isLiquid()) {
                    hasDepth = false;
                    break;
                }
                checkLoc.subtract(0, 1, 0);
            }
            if (!hasDepth) {
                player.sendMessage(mm.deserialize("<yellow>Kedalaman air terlalu dangkal untuk memancing! Minimal kedalaman " + minDepth + " blok air.</yellow>"));
                return;
            }
        }

        // 2. Validate current held rod
        boolean isOffHand = false;
        ItemStack currentRod = player.getInventory().getItemInMainHand();
        if (!plugin.getRodManager().isAutoCatchRod(currentRod)) {
            currentRod = player.getInventory().getItemInOffHand();
            isOffHand = true;
        }
        if (!plugin.getRodManager().isAutoCatchRod(currentRod)) {
            return;
        }

        Location hookLoc = hook.getLocation();

        // 3. Sound & Particle effects
        if (plugin.getConfig().getBoolean("settings.afk-fishing.sound-effects", true)) {
            player.playSound(hookLoc, Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f, 1.2f);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.4f);
        }
        if (plugin.getConfig().getBoolean("settings.afk-fishing.particle-effects", true)) {
            hookLoc.getWorld().spawnParticle(Particle.SPLASH, hookLoc, 25, 0.3, 0.2, 0.3, 0.1);
            hookLoc.getWorld().spawnParticle(Particle.BUBBLE, hookLoc, 15, 0.2, 0.2, 0.2, 0.05);
        }

        // 4. Generate Catch
        LootGenerator.CatchResult result = plugin.getLootGenerator().generateCatch(player, currentRod);

        // 5. Update player stats
        if (result.isFish) {
            plugin.getVaultStorage().getStats(player.getUniqueId()).recordCatch(
                    result.lootItem.getId(),
                    result.lootItem.getDisplayName(),
                    result.weightKg,
                    result.isSecret
            );
        }

        // 6. Broadcast if Secret or Legendary
        if (result.isSecret && plugin.getConfig().getBoolean("settings.broadcasts.secret-catch", true)) {
            Bukkit.broadcast(mm.deserialize("<newline><gradient:#ff007f:#7928ca><bold>★ APEXSIONS SECRET DISCOVERY ★</bold></gradient><newline>" +
                    "<yellow>Pemancing tangguh <white><bold>" + player.getName() + "</bold></white> berhasil menangkap <gold>" +
                    result.lootItem.getDisplayName() + "</gold> seberat <yellow><bold>" + String.format("%.2f", result.weightKg) + " kg</bold></yellow>!<newline>"));
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.9f, 1.0f);
            }
        } else if (result.lootItem.getRarity() == FishRarity.LEGENDARY && plugin.getConfig().getBoolean("settings.broadcasts.legendary-catch", true)) {
            Bukkit.broadcast(mm.deserialize("<gold><bold>[LEGENDA SAMUDRA]</bold></gold> <yellow>" + player.getName() +
                    "</yellow> menangkap <gold>" + result.lootItem.getDisplayName() + "</gold> seberat <yellow>" +
                    String.format("%.2f", result.weightKg) + " kg</yellow>!"));
        }

        // 7. Deliver item (Inventory -> Vault -> Ground)
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(result.item);
        if (!overflow.isEmpty()) {
            for (ItemStack left : overflow.values()) {
                boolean deposited = plugin.getVaultStorage().depositToVault(player, left);
                if (deposited) {
                    player.sendMessage(mm.deserialize("<gradient:#00c6ff:#0072ff>[Fishing Vault]</gradient> <yellow>Inventori penuh! Hasil pancingan otomatis disimpan ke dalam Fishing Vault Anda.</yellow>"));
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), left);
                    player.sendMessage(mm.deserialize("<red>Tas & Fishing Vault Anda penuh! Ikan dijatuhkan di sekitar Anda.</red>"));
                }
            }
        }

        player.sendMessage(mm.deserialize("<green>✦ Berhasil menangkap: </green>").append(result.item.displayName()));

        // 8. Deduct Rod Durability if breakable
        boolean rodBroken = false;
        org.bukkit.inventory.meta.ItemMeta rMeta = currentRod.getItemMeta();
        if (rMeta != null && !rMeta.isUnbreakable() && rMeta instanceof org.bukkit.inventory.meta.Damageable dmg) {
            int unbreaking = currentRod.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.UNBREAKING);
            boolean takeDmg = true;
            if (unbreaking > 0) {
                if (java.util.concurrent.ThreadLocalRandom.current().nextInt(unbreaking + 1) > 0) {
                    takeDmg = false;
                }
            }
            if (takeDmg) {
                int newDmg = dmg.getDamage() + 1;
                if (newDmg >= currentRod.getType().getMaxDurability()) {
                    if (isOffHand) {
                        player.getInventory().setItemInOffHand(null);
                    } else {
                        player.getInventory().setItemInMainHand(null);
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red><bold>PANCINGAN PATAH!</bold> Alat pancing Anda telah rusak karena kehabisan ketahanan.</red>"));
                    rodBroken = true;
                } else {
                    dmg.setDamage(newDmg);
                    currentRod.setItemMeta(dmg);
                    if (isOffHand) {
                        player.getInventory().setItemInOffHand(currentRod);
                    } else {
                        player.getInventory().setItemInMainHand(currentRod);
                    }
                }
            }
        }

        // 9. Remove old hook
        hook.remove();

        // 10. Auto-Recast Loop for continuous AFK fishing
        if (!rodBroken && plugin.getConfig().getBoolean("settings.afk-fishing.auto-recast", true)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline() || player.isDead()) return;
                ItemStack held = player.getInventory().getItemInMainHand();
                if (!plugin.getRodManager().isAutoCatchRod(held)) {
                    held = player.getInventory().getItemInOffHand();
                }
                if (plugin.getRodManager().isAutoCatchRod(held)) {
                    FishHook newHook = player.launchProjectile(FishHook.class);
                    registerCast(player, newHook, held);
                }
            }, 25L); // 1.25s recast
        }
    }

    public void cancelAllTasks() {
        for (BukkitTask task : activeTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        activeTasks.clear();
    }
}
