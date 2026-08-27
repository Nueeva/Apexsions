package com.apex.battlepass.pass;

import com.apex.battlepass.ApexsionsBattlepass;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class PassManager {

    private final ApexsionsBattlepass plugin;
    private final Map<String, PassTier> passes = new LinkedHashMap<>();

    public PassManager(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
        loadPasses();
    }

    public void loadPasses() {
        passes.clear();
        File passesFolder = new File(plugin.getDataFolder(), "passes");
        if (!passesFolder.exists()) {
            passesFolder.mkdirs();
            // Save defaults
            saveDefaultPassFile("passes/free.yml");
            saveDefaultPassFile("passes/premium.yml");
            saveDefaultPassFile("passes/premium-plus.yml");
            saveDefaultPassFile("passes/ultimate.yml");
            saveDefaultPassFile("passes/vip.yml");
            saveDefaultPassFile("passes/elite.yml");
        }

        File[] files = passesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null && files.length > 0) {
            for (File file : files) {
                loadPassFile(file);
            }
        } else {
            // If empty, save default files
            saveDefaultPassFile("passes/free.yml");
            saveDefaultPassFile("passes/premium.yml");
            saveDefaultPassFile("passes/premium-plus.yml");
            saveDefaultPassFile("passes/ultimate.yml");
            saveDefaultPassFile("passes/vip.yml");
            saveDefaultPassFile("passes/elite.yml");
            files = passesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    loadPassFile(file);
                }
            }
        }
    }

    private void saveDefaultPassFile(String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            try {
                plugin.saveResource(path, false);
            } catch (Exception ignored) {}
        }
    }

    private void loadPassFile(File file) {
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String id = config.getString("id", file.getName().replace(".yml", "")).toLowerCase();
            String displayName = config.getString("display-name", id);
            String permission = config.getString("permission", "apexsionsbattlepass.pass." + id);
            boolean defaultOwned = config.getBoolean("default-owned", id.equals("free"));
            String matStr = config.getString("icon.material", config.getString("icon", "PAPER"));
            Material mat = Material.matchMaterial(matStr);
            List<String> lore = config.getStringList("lore");
            int priority = config.getInt("priority", 0);
            List<String> rewardAccess = config.getStringList("reward-access");
            if (rewardAccess.isEmpty()) {
                rewardAccess = List.of(id);
            }

            PassTier tier = new PassTier(id, displayName, permission, defaultOwned, mat, lore, priority, rewardAccess);
            passes.put(id, tier);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load pass file " + file.getName() + ": " + e.getMessage());
        }
    }

    public Map<String, PassTier> getPasses() {
        return passes;
    }

    public PassTier getPass(String id) {
        return passes.get(id.toLowerCase());
    }

    public boolean canAccessRewardTier(Set<String> playerPasses, String rewardTier) {
        if (playerPasses == null || rewardTier == null) return false;
        String req = rewardTier.toLowerCase();
        PassTier reqTier = getPass(req);
        int reqPriority = reqTier != null ? reqTier.getPriority() : 0;

        for (String p : playerPasses) {
            if (p.equalsIgnoreCase(req)) {
                return true;
            }
            PassTier ownedTier = getPass(p);
            if (ownedTier != null) {
                if (ownedTier.getRewardAccess().contains(req)) {
                    return true;
                }
                // Hierarchy inheritance: higher or equal priority grants access to lower priority passes
                if (reqTier != null && ownedTier.getPriority() >= reqPriority) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<String> getEffectivePasses(Set<String> playerPasses) {
        Set<String> effective = new HashSet<>();
        if (playerPasses == null) return effective;

        int highestPriority = -1;
        for (String p : playerPasses) {
            effective.add(p.toLowerCase());
            PassTier tier = getPass(p);
            if (tier != null) {
                effective.addAll(tier.getRewardAccess());
                if (tier.getPriority() > highestPriority) {
                    highestPriority = tier.getPriority();
                }
            }
        }

        if (highestPriority >= 0) {
            for (PassTier t : passes.values()) {
                if (t.getPriority() <= highestPriority) {
                    effective.add(t.getId());
                }
            }
        }
        return effective;
    }
}
