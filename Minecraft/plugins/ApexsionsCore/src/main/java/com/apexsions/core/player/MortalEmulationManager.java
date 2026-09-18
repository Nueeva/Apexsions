package com.apexsions.core.player;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service allowing Upper Realm (The Aetherial Conclave) staff to temporarily
 * emulate being a mortal citizen of Zenithar, Solterra, or Sylvamoor for testing
 * and inspection purposes without breaking canonical database state.
 */
public class MortalEmulationManager {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, String> activeEmulations = new ConcurrentHashMap<>();

    public MortalEmulationManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Activates mortal emulation mode for a staff member.
     *
     * @param player     Staff player
     * @param kingdomKey ZENITHAR, SOLTERRA, or SYLVAMOOR
     * @return true if successfully enabled
     */
    public boolean setEmulation(Player player, String kingdomKey) {
        if (player == null || kingdomKey == null) return false;
        String upper = kingdomKey.toUpperCase(Locale.ROOT);

        if (!plugin.getRegionManager().isPlayableKingdom(upper)) {
            player.sendMessage(mm.deserialize("<red>✕ Kerajaan mortal tidak valid! Pilihan: ZENITHAR, SOLTERRA, SYLVAMOOR</red>"));
            return false;
        }

        activeEmulations.put(player.getUniqueId(), upper);

        // Apply visual and audio cues
        player.sendMessage(mm.deserialize(
                "<gradient:#00f2fe:#4facfe><bold>✦ MORTAL INCARNATION MODE ✦</bold></gradient>\n" +
                "<aqua>Anda kini bermanifestasi dan menyamar sebagai warga <gold><bold>" + upper + "</bold></gold>.\n" +
                "Buff/debuff, proteksi teritori, dan antarmuka akan membaca Anda sebagai warga " + upper + ".\n" +
                "Ketik <yellow>/ac emulate off</yellow> untuk kembali ke entitas murni Aetherion.</aqua>"
        ));
        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 0.8f, 1.4f);

        // Refresh kingdom buffs immediately
        if (plugin.getKingdomBuffManager() != null) {
            plugin.getKingdomBuffManager().applyBuffs(player);
        }

        // Refresh nametag and display
        if (plugin.getRankAnimationManager() != null) {
            plugin.getRankAnimationManager().updatePlayerNameplate(player);
        }

        return true;
    }

    /**
     * Clears mortal emulation mode, reverting the player back to pure Aetherion.
     *
     * @param player Staff player
     */
    public void clearEmulation(Player player) {
        if (player == null) return;
        if (activeEmulations.remove(player.getUniqueId()) != null) {
            player.sendMessage(mm.deserialize(
                    "<gradient:#00f2fe:#4facfe><bold>✦ THE AETHERIAL CONCLAVE ✦</bold></gradient> \n" +
                    "<aqua>Manifestasi fana diakhiri. Anda telah kembali ke eksistensi murni dimensi atas <bold>Aetherion</bold>.</aqua>"
            ));
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 1.5f);

            // Re-normalize buffs (remove kingdom buffs)
            if (plugin.getKingdomBuffManager() != null) {
                plugin.getKingdomBuffManager().applyBuffs(player);
            }

            // Refresh nametag and display
            if (plugin.getRankAnimationManager() != null) {
                plugin.getRankAnimationManager().updatePlayerNameplate(player);
            }
        } else {
            player.sendMessage(mm.deserialize("<yellow>Anda saat ini tidak sedang dalam mode simulasi warga fana.</yellow>"));
        }
    }

    /**
     * Activates mortal emulation mode for a staff member by UUID.
     */
    public boolean setEmulation(UUID uuid, String kingdomKey) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            return setEmulation(p, kingdomKey);
        }
        if (uuid == null || kingdomKey == null) return false;
        String upper = kingdomKey.toUpperCase(Locale.ROOT);
        if (!plugin.getRegionManager().isPlayableKingdom(upper)) return false;
        activeEmulations.put(uuid, upper);
        return true;
    }

    /**
     * Clears mortal emulation mode by UUID.
     */
    public void clearEmulation(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            clearEmulation(p);
        } else if (uuid != null) {
            activeEmulations.remove(uuid);
        }
    }

    /**
     * Checks if a player currently has an active mortal emulation.
     */
    public boolean isEmulating(UUID uuid) {
        return uuid != null && activeEmulations.containsKey(uuid);
    }

    /**
     * Gets the emulated kingdom key if active.
     */
    public Optional<String> getEmulatedKingdom(UUID uuid) {
        if (uuid == null) return Optional.empty();
        return Optional.ofNullable(activeEmulations.get(uuid));
    }

    /**
     * Clears emulation on quit to avoid memory leaks.
     */
    public void handleQuit(UUID uuid) {
        if (uuid != null) {
            activeEmulations.remove(uuid);
        }
    }
}
