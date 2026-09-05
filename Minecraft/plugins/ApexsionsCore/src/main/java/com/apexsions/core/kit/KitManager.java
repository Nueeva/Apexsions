package com.apexsions.core.kit;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing all server kits, persistence, rank checks, and cooldowns.
 */
public class KitManager {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<String, Kit> kits = new LinkedHashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    private final NamespacedKey keyKitId;
    private final NamespacedKey keySetId;
    private final NamespacedKey keySetName;
    private final NamespacedKey keySetType;
    private final NamespacedKey keySetVal;
    private final NamespacedKey keySetReq;
    private final NamespacedKey keySetStats;
    private final NamespacedKey keyToolBonus;

    private File kitsFile;
    private FileConfiguration kitsConfig;
    private File cooldownsFile;
    private FileConfiguration cooldownsConfig;

    public KitManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        this.keyKitId = new NamespacedKey(plugin, "kit_id");
        this.keySetId = new NamespacedKey(plugin, "set_id");
        this.keySetName = new NamespacedKey(plugin, "set_name");
        this.keySetType = new NamespacedKey(plugin, "set_type");
        this.keySetVal = new NamespacedKey(plugin, "set_val");
        this.keySetReq = new NamespacedKey(plugin, "set_req");
        this.keySetStats = new NamespacedKey(plugin, "set_stats");
        this.keyToolBonus = new NamespacedKey(plugin, "tool_bonus");

        initFiles();
        loadKits();
        loadCooldowns();
    }

    private void initFiles() {
        File kitsFolder = new File(plugin.getDataFolder(), "kits");
        if (!kitsFolder.exists()) {
            kitsFolder.mkdirs();
        }

        kitsFile = new File(kitsFolder, "kits.yml");
        if (!kitsFile.exists()) {
            createDefaultKitsFile();
        }
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);

        cooldownsFile = new File(kitsFolder, "cooldowns.yml");
        if (!cooldownsFile.exists()) {
            try {
                cooldownsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create cooldowns.yml: " + e.getMessage());
            }
        }
        cooldownsConfig = YamlConfiguration.loadConfiguration(cooldownsFile);
    }

    private void createDefaultKitsFile() {
        try {
            kitsFile.createNewFile();
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(kitsFile);

            // Default Wanderer Kit
            cfg.set("kits.wanderer.display-name", "<gradient:#f1c40f:#e67e22><bold>📦 KIT WANDERER</bold></gradient>");
            cfg.set("kits.wanderer.required-rank", "wanderer");
            cfg.set("kits.wanderer.cooldown", 86400); // 24 hours
            cfg.set("kits.wanderer.icon", "LEATHER_CHESTPLATE");

            ItemStack helm = new ItemStack(Material.CHAINMAIL_HELMET);
            ItemStack chest = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
            ItemStack legs = new ItemStack(Material.CHAINMAIL_LEGGINGS);
            ItemStack boots = new ItemStack(Material.CHAINMAIL_BOOTS);

            cfg.set("kits.wanderer.armor.helmet", helm);
            cfg.set("kits.wanderer.armor.chestplate", chest);
            cfg.set("kits.wanderer.armor.leggings", legs);
            cfg.set("kits.wanderer.armor.boots", boots);

            List<ItemStack> extra = List.of(
                    new ItemStack(Material.IRON_SWORD),
                    new ItemStack(Material.COOKED_BEEF, 32),
                    new ItemStack(Material.IRON_PICKAXE)
            );
            cfg.set("kits.wanderer.extra-items", extra);

            cfg.set("kits.wanderer.set-bonus.enabled", true);
            cfg.set("kits.wanderer.set-bonus.id", "wanderer");
            cfg.set("kits.wanderer.set-bonus.name", "Wanderer Explorer");
            cfg.set("kits.wanderer.set-bonus.type", "DAMAGE_REDUCTION");
            cfg.set("kits.wanderer.set-bonus.value", 15.0);
            cfg.set("kits.wanderer.set-bonus.required-pieces", 4);

            cfg.save(kitsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create default kits.yml: " + e.getMessage());
        }
    }

    public void loadKits() {
        kits.clear();
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);

        ConfigurationSection sec = kitsConfig.getConfigurationSection("kits");
        if (sec == null) return;

        for (String id : sec.getKeys(false)) {
            try {
                String dName = sec.getString(id + ".display-name", id);
                String rank = sec.getString(id + ".required-rank", "wanderer");
                long cd = sec.getLong(id + ".cooldown", 86400);
                String iconName = sec.getString(id + ".icon", "CHEST");
                Material mat = Material.matchMaterial(iconName);
                if (mat == null) mat = Material.CHEST;

                Kit kit = new Kit(id, dName, rank, cd, mat);

                // Armor
                if (sec.isSet(id + ".armor.helmet")) kit.setHelmet(sec.getItemStack(id + ".armor.helmet"));
                if (sec.isSet(id + ".armor.chestplate")) kit.setChestplate(sec.getItemStack(id + ".armor.chestplate"));
                if (sec.isSet(id + ".armor.leggings")) kit.setLeggings(sec.getItemStack(id + ".armor.leggings"));
                if (sec.isSet(id + ".armor.boots")) kit.setBoots(sec.getItemStack(id + ".armor.boots"));

                // Extra Items
                List<?> list = sec.getList(id + ".extra-items");
                if (list != null) {
                    List<ItemStack> extra = new ArrayList<>();
                    for (Object obj : list) {
                        if (obj instanceof ItemStack is) {
                            extra.add(is);
                        }
                    }
                    kit.setExtraItems(extra);
                }

                // Set Bonus
                if (sec.getBoolean(id + ".set-bonus.enabled", false)) {
                    String setId = sec.getString(id + ".set-bonus.id", id);
                    String sName = sec.getString(id + ".set-bonus.name", dName);
                    String sTypeStr = sec.getString(id + ".set-bonus.type", "DAMAGE_REDUCTION");
                    KitStatType sType;
                    try {
                        sType = KitStatType.valueOf(sTypeStr.toUpperCase());
                    } catch (Exception e) {
                        sType = KitStatType.DAMAGE_REDUCTION;
                    }
                    double val = sec.getDouble(id + ".set-bonus.value", 15.0);
                    int req = sec.getInt(id + ".set-bonus.required-pieces", 4);
                    kit.setSetBonus(new KitArmorSetBonus(setId, sName, sType, val, req));
                }

                kits.put(id.toLowerCase(), kit);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load kit '" + id + "': " + e.getMessage());
            }
        }
        plugin.getLogger().info("Loaded " + kits.size() + " server kits successfully.");
    }

    public void saveKit(Kit kit) {
        if (kit == null) return;
        kits.put(kit.getId().toLowerCase(), kit);

        String path = "kits." + kit.getId().toLowerCase();
        kitsConfig.set(path + ".display-name", kit.getDisplayName());
        kitsConfig.set(path + ".required-rank", kit.getRequiredRank());
        kitsConfig.set(path + ".cooldown", kit.getCooldownSeconds());
        kitsConfig.set(path + ".icon", kit.getDisplayIcon().name());

        kitsConfig.set(path + ".armor.helmet", kit.getHelmet());
        kitsConfig.set(path + ".armor.chestplate", kit.getChestplate());
        kitsConfig.set(path + ".armor.leggings", kit.getLeggings());
        kitsConfig.set(path + ".armor.boots", kit.getBoots());
        kitsConfig.set(path + ".extra-items", kit.getExtraItems());

        if (kit.getSetBonus() != null) {
            kitsConfig.set(path + ".set-bonus.enabled", true);
            kitsConfig.set(path + ".set-bonus.id", kit.getSetBonus().getSetId());
            kitsConfig.set(path + ".set-bonus.name", kit.getSetBonus().getSetName());
            kitsConfig.set(path + ".set-bonus.type", kit.getSetBonus().getStatType().name());
            kitsConfig.set(path + ".set-bonus.value", kit.getSetBonus().getValue());
            kitsConfig.set(path + ".set-bonus.required-pieces", kit.getSetBonus().getRequiredPieces());
        } else {
            kitsConfig.set(path + ".set-bonus", null);
        }

        try {
            kitsConfig.save(kitsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save kit '" + kit.getId() + "': " + e.getMessage());
        }
    }

    public boolean deleteKit(String kitId) {
        String id = kitId.toLowerCase().trim();
        if (!kits.containsKey(id)) return false;
        kits.remove(id);
        kitsConfig.set("kits." + id, null);
        try {
            kitsConfig.save(kitsFile);
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to delete kit '" + id + "': " + e.getMessage());
            return false;
        }
    }

    public Kit getKit(String id) {
        if (id == null) return null;
        return kits.get(id.toLowerCase().trim());
    }

    public Collection<Kit> getAllKits() {
        return Collections.unmodifiableCollection(kits.values());
    }

    public boolean canClaim(Player player, Kit kit) {
        if (player.hasPermission("apexsions.admin") || player.isOp()) {
            return true;
        }

        // Rank weight check
        String pRank = plugin.getLuckPermsHook().getPlayerRankKey(player);
        int pWeight = plugin.getLuckPermsHook().getRankWeight(pRank);
        int reqWeight = plugin.getLuckPermsHook().getRankWeight(kit.getRequiredRank());

        if (pWeight < reqWeight && !player.hasPermission("apexsions.kit." + kit.getId())) {
            return false;
        }

        // Cooldown check
        long expiry = getCooldownExpiry(player.getUniqueId(), kit.getId());
        return System.currentTimeMillis() >= expiry;
    }

    public long getRemainingCooldownSeconds(Player player, Kit kit) {
        if (player.hasPermission("apexsions.admin") || player.isOp()) return 0;
        long expiry = getCooldownExpiry(player.getUniqueId(), kit.getId());
        long remaining = (expiry - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    public String formatRemainingCooldown(long seconds) {
        if (seconds <= 0) return "Siap";
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        if (h > 0) {
            return h + "j " + m + "m";
        } else if (m > 0) {
            return m + "m " + s + "d";
        } else {
            return s + "d";
        }
    }

    public boolean claimKit(Player player, Kit kit) {
        if (!canClaim(player, kit)) {
            return false;
        }

        // 1. Prepare Armor with PDC and formatted Set Bonus
        ItemStack helm = prepareArmorPiece(kit, kit.getHelmet(), "Helmet");
        ItemStack chest = prepareArmorPiece(kit, kit.getChestplate(), "Chestplate");
        ItemStack legs = prepareArmorPiece(kit, kit.getLeggings(), "Leggings");
        ItemStack boots = prepareArmorPiece(kit, kit.getBoots(), "Boots");

        // 2. Deliver Armor
        if (helm != null) deliverItem(player, helm);
        if (chest != null) deliverItem(player, chest);
        if (legs != null) deliverItem(player, legs);
        if (boots != null) deliverItem(player, boots);

        // 3. Deliver Extra Items
        for (ItemStack extra : kit.getExtraItems()) {
            if (extra != null && extra.getType() != Material.AIR) {
                deliverItem(player, extra.clone());
            }
        }

        // 4. Record Cooldown
        setCooldown(player.getUniqueId(), kit.getId(), kit.getCooldownSeconds());

        // 5. Feedback
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        player.sendMessage(mm.deserialize("<green><bold>✓ BERHASIL!</bold> Kamu telah mengklaim kit <gold>" + kit.getDisplayName() + "</gold>!</green>"));
        return true;
    }

    public ItemStack prepareArmorPiece(Kit kit, ItemStack item, String slotName) {
        if (item == null || item.getType() == Material.AIR) return null;
        ItemStack piece = item.clone();
        ItemMeta meta = piece.getItemMeta();
        if (meta == null) return piece;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (kit != null) {
            pdc.set(keyKitId, PersistentDataType.STRING, kit.getId());
        }

        KitArmorSetBonus bonus = kit != null ? kit.getSetBonus() : null;
        if (bonus != null) {
            pdc.set(keySetId, PersistentDataType.STRING, bonus.getSetId());
            pdc.set(keySetName, PersistentDataType.STRING, bonus.getSetName());
            pdc.set(keySetType, PersistentDataType.STRING, bonus.getStatType().name());
            pdc.set(keySetVal, PersistentDataType.DOUBLE, bonus.getValue());
            pdc.set(keySetReq, PersistentDataType.INTEGER, bonus.getRequiredPieces());

            // Format Lore for Set Bonus
            List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Component.empty());
            lore.add(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>✦ SET BONUS: [" + bonus.getSetName() + "] ✦</bold></gradient>"));
            lore.add(mm.deserialize("<gray>Efek (" + bonus.getRequiredPieces() + " Set): <yellow>" + bonus.getStatType().formatValue(bonus.getValue()) + " " + bonus.getStatType().getDisplayName() + "</yellow></gray>"));
            meta.lore(lore);
        }

        piece.setItemMeta(meta);
        return piece;
    }

    public ItemStack applyCustomSetBonus(ItemStack piece, String setId, String setName, KitStatType statType, double value, int requiredPieces) {
        if (piece == null || piece.getType() == Material.AIR) return null;
        ItemStack item = piece.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(keySetId, PersistentDataType.STRING, setId);
        pdc.set(keySetName, PersistentDataType.STRING, setName);
        pdc.set(keySetType, PersistentDataType.STRING, statType.name());
        pdc.set(keySetVal, PersistentDataType.DOUBLE, value);
        pdc.set(keySetReq, PersistentDataType.INTEGER, requiredPieces);

        List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.add(Component.empty());
        lore.add(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>✦ SET BONUS: [" + setName + "] ✦</bold></gradient>"));
        lore.add(mm.deserialize("<gray>Efek (" + requiredPieces + " Set): <yellow>" + statType.formatValue(value) + " " + statType.getDisplayName() + "</yellow></gray>"));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    private void deliverItem(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            player.sendMessage(mm.deserialize("<yellow>⚠️ Sebagian item kit dijatuhkan di kakimu karena tas penuh!</yellow>"));
        }
    }

    private long getCooldownExpiry(UUID uuid, String kitId) {
        Map<String, Long> pMap = cooldowns.get(uuid);
        if (pMap == null) return 0;
        return pMap.getOrDefault(kitId.toLowerCase(), 0L);
    }

    public void setCooldown(UUID uuid, String kitId, long seconds) {
        cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(kitId.toLowerCase(), System.currentTimeMillis() + (seconds * 1000L));
        saveCooldownsAsync();
    }

    public void resetCooldown(UUID uuid, String kitId) {
        Map<String, Long> pMap = cooldowns.get(uuid);
        if (pMap != null) {
            pMap.remove(kitId.toLowerCase());
            saveCooldownsAsync();
        }
    }

    private void loadCooldowns() {
        cooldowns.clear();
        cooldownsConfig = YamlConfiguration.loadConfiguration(cooldownsFile);
        ConfigurationSection sec = cooldownsConfig.getConfigurationSection("cooldowns");
        if (sec == null) return;

        for (String uStr : sec.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uStr);
                ConfigurationSection kitSec = sec.getConfigurationSection(uStr);
                if (kitSec != null) {
                    Map<String, Long> pMap = new ConcurrentHashMap<>();
                    for (String kId : kitSec.getKeys(false)) {
                        pMap.put(kId.toLowerCase(), kitSec.getLong(kId));
                    }
                    cooldowns.put(uuid, pMap);
                }
            } catch (Exception ignored) {}
        }
    }

    public void saveCooldownsAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (cooldownsFile) {
                try {
                    cooldownsConfig.set("cooldowns", null);
                    long now = System.currentTimeMillis();
                    for (Map.Entry<UUID, Map<String, Long>> entry : cooldowns.entrySet()) {
                        String uStr = entry.getKey().toString();
                        for (Map.Entry<String, Long> kitEntry : entry.getValue().entrySet()) {
                            if (kitEntry.getValue() > now) {
                                cooldownsConfig.set("cooldowns." + uStr + "." + kitEntry.getKey(), kitEntry.getValue());
                            }
                        }
                    }
                    cooldownsConfig.save(cooldownsFile);
                } catch (IOException e) {
                    plugin.getLogger().warning("Failed to save cooldowns.yml: " + e.getMessage());
                }
            }
        });
    }

    public NamespacedKey getKeyKitId() {
        return keyKitId;
    }

    public NamespacedKey getKeySetId() {
        return keySetId;
    }

    public NamespacedKey getKeySetName() {
        return keySetName;
    }

    public NamespacedKey getKeySetType() {
        return keySetType;
    }

    public NamespacedKey getKeySetVal() {
        return keySetVal;
    }

    public NamespacedKey getKeySetReq() {
        return keySetReq;
    }

    public NamespacedKey getKeySetStats() {
        return keySetStats;
    }

    public NamespacedKey getKeyToolBonus() {
        return keyToolBonus;
    }
}
