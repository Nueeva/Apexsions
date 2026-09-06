package com.apexsions.crates.crate;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.milestone.Milestone;
import com.apexsions.crates.reward.Reward;
import com.apexsions.crates.reward.RewardRarity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CrateManager {

    private final ApexsionsCratesPlugin plugin;
    private final Map<String, Crate> crates = new LinkedHashMap<>();
    private final Set<CrateLocation> locations = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public CrateManager(ApexsionsCratesPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadCrates() {
        crates.clear();
        File cratesFolder = new File(plugin.getDataFolder(), "crates");
        if (!cratesFolder.exists()) {
            cratesFolder.mkdirs();
            plugin.saveResource("crates/luxury.yml", false);
            plugin.saveResource("crates/novice.yml", false);
        }

        File[] files = cratesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) return;

        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String id = config.getString("id", file.getName().replace(".yml", "")).toLowerCase();
                String name = config.getString("name", "Peti " + id);
                Material blockMat = Material.matchMaterial(config.getString("block-material", "CHEST"));
                if (blockMat == null) blockMat = Material.CHEST;
                CrateAnimationType animType = CrateAnimationType.fromString(config.getString("animation-type", "ROULETTE"));
                String reqKey = config.getString("required-key", id);
                int cooldown = config.getInt("cooldown-seconds", 0);

                // Hologram
                boolean holoEnabled = config.getBoolean("hologram.enabled", true);
                List<String> holoLines = config.getStringList("hologram.lines");

                // Milestones
                Map<Integer, Milestone> milestones = new TreeMap<>();
                ConfigurationSection mileSec = config.getConfigurationSection("milestones");
                if (mileSec != null) {
                    for (String key : mileSec.getKeys(false)) {
                        try {
                            int opens = Integer.parseInt(key);
                            ConfigurationSection ms = mileSec.getConfigurationSection(key);
                            if (ms != null) {
                                String msName = ms.getString("name", "Milestone " + opens);
                                List<String> cmds = ms.getStringList("reward-commands");
                                String bcast = ms.getString("broadcast");
                                milestones.put(opens, new Milestone(opens, msName, cmds, bcast));
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }

                // Rewards
                Map<String, Reward> rewards = new LinkedHashMap<>();
                ConfigurationSection rewSec = config.getConfigurationSection("rewards");
                if (rewSec != null) {
                    for (String rewKey : rewSec.getKeys(false)) {
                        ConfigurationSection rs = rewSec.getConfigurationSection(rewKey);
                        if (rs == null) continue;

                        String rewName = rs.getString("name", rewKey);
                        RewardRarity rarity = RewardRarity.fromString(rs.getString("rarity", "COMMON"));
                        double weight = rs.getDouble("weight", 10.0);
                        Material mat = Material.matchMaterial(rs.getString("material", "CHEST"));
                        if (mat == null) mat = Material.CHEST;
                        int amount = rs.getInt("amount", 1);
                        List<String> cmds = rs.getStringList("commands");
                        String ecoCurr = rs.getString("economy-currency");
                        double ecoAmt = rs.getDouble("economy-amount", 0.0);
                        long coreXp = rs.getLong("core-xp", 0L);
                        int bpXp = rs.getInt("battlepass-xp", 0);
                        boolean bcast = rs.getBoolean("broadcast", false);
                        int cmd = rs.getInt("custom-model-data", 0);

                        Reward reward = new Reward(rewKey.toLowerCase(), rewName, rarity, weight, mat, amount,
                                cmds, ecoCurr, ecoAmt, coreXp, bpXp, bcast, cmd);
                        rewards.put(rewKey.toLowerCase(), reward);
                    }
                }

                Crate crate = new Crate(id, name, blockMat, animType, reqKey, rewards, milestones, holoEnabled, holoLines, cooldown);
                crates.put(id, crate);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load crate file " + file.getName() + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + crates.size() + " crates from crates/ folder.");
    }

    public Collection<Crate> getCrates() {
        return Collections.unmodifiableCollection(crates.values());
    }

    public Crate getCrate(String id) {
        if (id == null) return null;
        return crates.get(id.toLowerCase());
    }

    public void addLocation(CrateLocation loc) {
        locations.add(loc);
    }

    public void removeLocation(CrateLocation loc) {
        locations.remove(loc);
    }

    public Set<CrateLocation> getLocations() {
        return Collections.unmodifiableSet(locations);
    }

    public CrateLocation getCrateLocationAt(Location loc) {
        if (loc == null) return null;
        for (CrateLocation cl : locations) {
            if (cl.matches(loc)) {
                return cl;
            }
        }
        return null;
    }

    public Crate getCrateAt(Location loc) {
        CrateLocation cl = getCrateLocationAt(loc);
        if (cl == null) return null;
        return getCrate(cl.getCrateId());
    }

    public boolean isCrateLocation(Location loc) {
        return getCrateLocationAt(loc) != null;
    }

    public boolean isOnCooldown(UUID uuid, String crateId, int cooldownSeconds) {
        if (cooldownSeconds <= 0) return false;
        Map<String, Long> playerCds = cooldowns.get(uuid);
        if (playerCds == null) return false;
        Long expire = playerCds.get(crateId.toLowerCase());
        if (expire == null) return false;
        return System.currentTimeMillis() < expire;
    }

    public void applyCooldown(UUID uuid, String crateId, int cooldownSeconds) {
        if (cooldownSeconds <= 0) return;
        cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(crateId.toLowerCase(), System.currentTimeMillis() + (cooldownSeconds * 1000L));
    }
}
