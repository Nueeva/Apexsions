package com.apexsions.core.api;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.level.LevelManager;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.player.PlayerDataService;
import com.apexsions.core.region.Region;
import com.apexsions.core.region.RegionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the public ApexsionsCoreAPI interface.
 */
public class ApexsionsCoreAPIImpl implements ApexsionsCoreAPI {

    private final ApexsionsCorePlugin plugin;

    public ApexsionsCoreAPIImpl(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable PlayerData getPlayerData(@NotNull UUID uuid) {
        return plugin.getPlayerDataService().getCached(uuid).orElse(null);
    }

    @Override
    public @NotNull CompletableFuture<Optional<PlayerData>> getPlayerDataAsync(@NotNull UUID uuid) {
        Optional<PlayerData> cached = plugin.getPlayerDataService().getCached(uuid);
        if (cached.isPresent()) {
            return CompletableFuture.completedFuture(cached);
        }
        return plugin.getPlayerRepository().findByUuid(uuid);
    }

    @Override
    public @Nullable Region getRegion(@NotNull UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        if (data != null && data.getRegionId() != null) {
            return plugin.getRegionManager().getRegion(data.getRegionId()).orElse(null);
        }
        return null;
    }

    @Override
    public @NotNull String getPlayerRegionKey(@NotNull UUID uuid) {
        Region reg = getRegion(uuid);
        return reg != null ? reg.getKey() : "NONE";
    }

    @Override
    public @NotNull String getLevelTitle(@NotNull UUID uuid) {
        return plugin.getLevelManager().getLevelTitle(uuid);
    }

    @Override
    public int getLevel(@NotNull UUID uuid) {
        return plugin.getLevelManager().getLevel(uuid);
    }

    @Override
    public long getXp(@NotNull UUID uuid) {
        return plugin.getLevelManager().getXp(uuid);
    }

    @Override
    public void addXp(@NotNull UUID uuid, long amount, @NotNull XpSource source) {
        plugin.getLevelManager().addXp(uuid, amount, source);
    }

    @Override
    public void setLevel(@NotNull UUID uuid, int level) {
        plugin.getLevelManager().setLevel(uuid, level);
    }

    @Override
    public void setXp(@NotNull UUID uuid, long xp) {
        plugin.getLevelManager().setXp(uuid, xp);
    }

    @Override
    public void setRegion(@NotNull UUID uuid, @NotNull Region region) {
        plugin.getPlayerDataService().updateRegion(uuid, region.getId());
    }

    @Override
    public @NotNull Optional<Region> getKingdomAt(@NotNull org.bukkit.Location location) {
        return plugin.getRegionManager().getRegionAt(location);
    }

    @Override
    public boolean isInKingdomTerritory(@NotNull org.bukkit.entity.Player player, @NotNull Region region) {
        return region.containsLocation(player.getLocation());
    }

    @Override
    public @NotNull LevelManager getLevelManager() {
        return plugin.getLevelManager();
    }

    @Override
    public @NotNull RegionManager getRegionManager() {
        return plugin.getRegionManager();
    }

    @Override
    public @NotNull PlayerDataService getPlayerDataService() {
        return plugin.getPlayerDataService();
    }

    @Override
    public @NotNull com.apexsions.core.admin.AdminHubManager getAdminHubManager() {
        return plugin.getAdminHubManager();
    }

    @Override
    public void registerAdminModule(@NotNull com.apexsions.core.admin.AdminModule module) {
        plugin.getAdminHubManager().registerModule(module);
    }

    @Override
    public @NotNull PlayerChatProfile getPlayerChatProfile(@NotNull UUID uuid) {
        PlayerData data = plugin.getPlayerDataService().getCached(uuid).orElse(null);
        org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(uuid);
        String pName = player != null ? player.getName() : (data != null ? data.getUsername() : "Player");

        int level = data != null ? data.getLevel() : 1;
        long xp = data != null ? data.getXp() : 0;
        long reqXp = plugin.getLevelFormula().getRequiredXpForNextLevel(level);
        String levelTitle = plugin.getLevelManager().getLevelTitle(uuid);
        String activeTitle = data != null ? data.getActiveTitle() : null;

        String rankKey = (player != null && plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isAvailable())
                ? plugin.getLuckPermsHook().getPlayerRankKey(player)
                : (player != null && player.isOp() ? "ancestor" : "wanderer");

        String rank = switch (rankKey.toLowerCase().trim()) {
            case "ancestor", "owner" -> "<gradient:#8B0000:#FF0000><bold>[👑 ANCESTOR]</bold></gradient>";
            case "warden", "admin", "headadmin" -> "<gradient:#1e3c72:#2a5298><bold>[🛡 WARDEN]</bold></gradient>";
            case "herald", "mod", "moderator" -> "<gradient:#f857a6:#ff5858><bold>[📜 HERALD]</bold></gradient>";
            case "sions" -> "<gradient:#00FFFF:#FFD700><bold>[✦ SIONS]</bold></gradient>";
            case "emperor" -> "<gradient:#e52d27:#b31217><bold>[⚔ EMPEROR]</bold></gradient>";
            case "sovereign" -> "<gradient:#f39c12:#f1c40f><bold>[⚜ SOVEREIGN]</bold></gradient>";
            case "archon" -> "<gradient:#00c6ff:#0072ff><bold>[💎 ARCHON]</bold></gradient>";
            case "ascendant" -> "<gradient:#11998e:#38ef7d><bold>[☘ ASCENDANT]</bold></gradient>";
            default -> "<gray>[Wanderer]</gray>";
        };

        String kingdomKey = "NONE";
        String kingdomDisplay = "Belum Memilih";
        if (data != null && data.getRegionId() != null) {
            Region r = plugin.getRegionManager().getRegion(data.getRegionId()).orElse(null);
            if (r != null) {
                kingdomKey = r.getKey();
                kingdomDisplay = r.getDisplayName();
            }
        }

        String kingName = plugin.getConfigManager().getKingdomKing(kingdomKey);
        boolean isMonarch = kingName != null && pName.equalsIgnoreCase(kingName);

        double balance = (player != null && plugin.getVaultHook() != null && plugin.getVaultHook().hasEconomy())
                ? plugin.getVaultHook().getBalance(player)
                : 0.0;

        int ping = player != null ? player.getPing() : 0;
        int hp = player != null ? (int) Math.ceil(player.getHealth()) : 20;
        int maxHp = player != null ? (int) Math.ceil(player.getMaxHealth()) : 20;

        return new PlayerChatProfile(
                uuid,
                pName,
                level,
                xp,
                reqXp,
                levelTitle,
                activeTitle,
                rank,
                kingdomKey,
                kingdomDisplay,
                isMonarch,
                balance,
                ping,
                hp,
                maxHp
        );
    }

    @Override
    public double getKingdomTax(@NotNull String kingdomKey) {
        if (kingdomKey == null || kingdomKey.equalsIgnoreCase("NONE")) {
            return 10.0;
        }
        String upper = kingdomKey.toUpperCase(java.util.Locale.ROOT);
        org.bukkit.configuration.ConfigurationSection sec = plugin.getConfigManager().getKingdomsConfig().getConfigurationSection("regions." + upper);
        if (sec != null && sec.contains("tax-percent")) {
            return sec.getDouble("tax-percent", 10.0);
        }
        return switch (upper) {
            case "ZENITHAR" -> 25.0;
            case "SOLTERRA" -> 20.0;
            case "SYLVAMOOR" -> 15.0;
            default -> 10.0;
        };
    }
}
