package com.apexsions.battlepass.pass;

import com.apexsions.battlepass.ApexsionsBattlepass;
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
        }

        saveDefaultPassFile("passes/citizen.yml");
        saveDefaultPassFile("passes/sio.yml");
        saveDefaultPassFile("passes/exsio.yml");

        File[] files = passesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null && files.length > 0) {
            for (File file : files) {
                loadPassFile(file);
            }
        }

        // Ensure the 3 core passes exist
        if (!passes.containsKey("citizen")) {
            passes.put("citizen", new PassTier("citizen", "&f&lCitizen Pass", "apexsionsbattlepass.pass.citizen", true, Material.BOOK, List.of("&7Pass bawaan gratis untuk semua warga Apexsions."), 10, List.of("citizen")));
        }
        if (!passes.containsKey("sio")) {
            passes.put("sio", new PassTier("sio", "&6&lSio Pass", "apexsionsbattlepass.pass.sio", false, Material.GOLD_BLOCK, List.of("&7Pass berbayar eksklusif Apexsions!"), 20, List.of("citizen", "sio")));
        }
        if (!passes.containsKey("exsio")) {
            passes.put("exsio", new PassTier("exsio", "&d&lExsio Pass", "apexsionsbattlepass.pass.exsio", false, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, List.of("&7Pass berbayar tertinggi dengan reward paling langka!"), 30, List.of("citizen", "sio", "exsio")));
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
            boolean defaultOwned = config.getBoolean("default-owned", id.equals("citizen") || id.equals("free"));
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
        if (id == null) return null;
        String lower = id.toLowerCase();
        if (passes.containsKey(lower)) {
            return passes.get(lower);
        }
        // Aliases / Fallbacks
        if (lower.equals("free")) return passes.get("citizen");
        if (lower.equals("premium") || lower.equals("premium-plus")) return passes.get("sio");
        if (lower.equals("ultimate") || lower.equals("vip") || lower.equals("elite")) return passes.get("exsio");
        return null;
    }

    public boolean canAccessRewardTier(Set<String> playerPasses, String rewardTier) {
        if (playerPasses == null || rewardTier == null) return false;
        String req = normalizePassId(rewardTier);

        // Citizen pass is free & owned by all
        if (req.equals("citizen")) return true;

        for (String p : playerPasses) {
            String normP = normalizePassId(p);
            if (normP.equals(req)) {
                return true;
            }
            if (normP.equals("exsio")) {
                return true; // Exsio unlocks everything (exsio, sio, citizen)
            }
            if (normP.equals("sio") && (req.equals("sio") || req.equals("citizen"))) {
                return true;
            }

            PassTier ownedTier = getPass(normP);
            if (ownedTier != null) {
                if (ownedTier.getRewardAccess().contains(req)) {
                    return true;
                }
                PassTier reqTier = getPass(req);
                if (reqTier != null && ownedTier.getPriority() >= reqTier.getPriority()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String normalizePassId(String id) {
        if (id == null) return "citizen";
        String lower = id.toLowerCase().trim();
        return switch (lower) {
            case "free" -> "citizen";
            case "premium", "premium-plus" -> "sio";
            case "ultimate", "vip", "elite" -> "exsio";
            default -> lower;
        };
    }

    public Set<String> getEffectivePasses(Set<String> playerPasses) {
        Set<String> effective = new HashSet<>();
        effective.add("citizen");
        if (playerPasses == null) return effective;

        int highestPriority = -1;
        for (String p : playerPasses) {
            String norm = normalizePassId(p);
            effective.add(norm);
            PassTier tier = getPass(norm);
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

    public PassTier getHighestPassTier(Set<String> playerPasses) {
        if (playerPasses == null || playerPasses.isEmpty()) {
            return getPass("citizen");
        }
        PassTier highest = getPass("citizen");
        int highestPriority = highest != null ? highest.getPriority() : 0;
        for (String p : playerPasses) {
            PassTier tier = getPass(p);
            if (tier != null && tier.getPriority() > highestPriority) {
                highest = tier;
                highestPriority = tier.getPriority();
            }
        }
        return highest != null ? highest : getPass("citizen");
    }
}
