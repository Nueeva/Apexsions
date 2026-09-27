package com.apexsions.shop.contract;

import com.apexsions.shop.ApexsionsShop;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages daily/periodic Royal Trade Orders & Quotas.
 * Provides micro-goals for players, limits single-item dumping,
 * and distributes wealth and kingdom reputation through trade.
 */
public class TradeContractService {

    private final ApexsionsShop plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final File dataFile;
    private YamlConfiguration dataConfig;

    private final List<TradeContract> activeContracts = new ArrayList<>();
    private final Map<UUID, Map<String, Integer>> playerProgress = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> completedContracts = new ConcurrentHashMap<>();

    private long nextResetTime = 0L;
    private BukkitTask rotationTask;

    public TradeContractService(ApexsionsShop plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "contracts_data.yml");
    }

    public void start() {
        loadData();
        setupDefaultContracts();

        long intervalMinutes = plugin.getConfigManager().getMarketsConfig().getLong("trade-contracts.reset-interval-minutes", 720L); // 12 Hours
        long intervalTicks = Math.max(20L * 60L, intervalMinutes * 60L * 20L);

        if (nextResetTime <= System.currentTimeMillis()) {
            rotateContracts(false);
        }

        rotationTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> rotateContracts(true), intervalTicks, intervalTicks);
    }

    public void stop() {
        if (rotationTask != null) {
            rotationTask.cancel();
            rotationTask = null;
        }
        saveData();
    }

    private void setupDefaultContracts() {
        activeContracts.clear();

        // 1. Solterra Contracts (Desert Fortress & Metallic Foundries)
        activeContracts.add(new TradeContract(
                "solterra_copper",
                "SOLTERRA",
                "Solterra",
                "#e74c3c",
                "Logistik Baja Benteng Gurun",
                "Gubernur Militer Solterra membutuhkan 64 Copper Ingot untuk perbaikan baju zirah penjaga pos luar.",
                Material.COPPER_INGOT,
                64,
                4500.0,
                250,
                Material.COPPER_INGOT
        ));

        activeContracts.add(new TradeContract(
                "solterra_iron",
                "SOLTERRA",
                "Solterra",
                "#e74c3c",
                "Besi Tempa Pasukan Perbatasan",
                "Pandai besi benteng gurun memerlukan 32 Batang Besi murni untuk menempai mata tombak.",
                Material.IRON_INGOT,
                32,
                6000.0,
                350,
                Material.IRON_INGOT
        ));

        // 2. Zenithar Contracts (Gilded City, Solar Spires & Aristocrats)
        activeContracts.add(new TradeContract(
                "zenithar_quartz",
                "ZENITHAR",
                "Zenithar",
                "#f39c12",
                "Marmer Kuil Surya Zenithar",
                "Arsitek Zenithar memesan 64 Nether Quartz murni untuk ornamen pilar Menara Emas.",
                Material.QUARTZ,
                64,
                7500.0,
                400,
                Material.QUARTZ
        ));

        activeContracts.add(new TradeContract(
                "zenithar_bread",
                "ZENITHAR",
                "Zenithar",
                "#f39c12",
                "Bahan Pangan Kota Metropolitan",
                "Dewan Perdagangan Zenithar menjamin ketersediaan 64 Roti segar bagi warga distrik pasar.",
                Material.BREAD,
                64,
                4000.0,
                200,
                Material.BREAD
        ));

        // 3. Sylvamoor Contracts (Verdant Deepwood & Herbal Conservation)
        activeContracts.add(new TradeContract(
                "sylvamoor_herbs",
                "SYLVAMOOR",
                "Sylvamoor",
                "#2ecc71",
                "Herbal & Tebu Konservasi Deepwood",
                "Para druid Sylvamoor membutuhkan 48 Tebu (Sugar Cane) untuk ramuan penumbuh bibit sakral.",
                Material.SUGAR_CANE,
                48,
                5000.0,
                300,
                Material.SUGAR_CANE
        ));

        activeContracts.add(new TradeContract(
                "sylvamoor_wood",
                "SYLVAMOOR",
                "Sylvamoor",
                "#2ecc71",
                "Kayu Konstruksi Kanopi Hutan",
                "Penjaga Hutan membutuhkan 64 Balok Kayu Oak untuk memperbaiki jembatan gantung antar-pohon.",
                Material.OAK_LOG,
                64,
                5500.0,
                300,
                Material.OAK_LOG
        ));
    }

    public void rotateContracts(boolean announce) {
        long durationMinutes = plugin.getConfigManager().getMarketsConfig().getLong("trade-contracts.reset-interval-minutes", 720L);
        this.nextResetTime = System.currentTimeMillis() + (durationMinutes * 60L * 1000L);

        // Clear player daily progress on new rotation cycle
        playerProgress.clear();
        completedContracts.clear();
        saveData();

        setupDefaultContracts();

        if (announce) {
            String msg = "\n<gradient:#f1c40f:#e67e22><bold>[ KONTRAK DAGANG KERAJAAN ]</bold></gradient> <white>Daftar Kontrak Ekspor Baru Telah Tersedia!</white>\n" +
                    " <gray>Penuhi kuota ekspor kerajaan untuk meraih bonus Rupiah berlimpah dan XP Kerajaan.</gray>\n" +
                    " <dark_gray>Buka melalui: <gold>/shop contracts</gold> atau menu utama <yellow>/shop</yellow></dark_gray>\n";

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(miniMessage.deserialize(msg));
                p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.9f, 1.1f);
            }
        }
    }

    public List<TradeContract> getActiveContracts() {
        return Collections.unmodifiableList(activeContracts);
    }

    public int getPlayerProgress(UUID uuid, String contractId) {
        return playerProgress.getOrDefault(uuid, Collections.emptyMap()).getOrDefault(contractId, 0);
    }

    public boolean isContractCompleted(UUID uuid, String contractId) {
        return completedContracts.getOrDefault(uuid, Collections.emptySet()).contains(contractId);
    }

    /**
     * Attempts to deliver items from player's inventory towards the specified contract.
     */
    public boolean deliverItems(Player player, TradeContract contract) {
        if (player == null || contract == null) return false;
        UUID uuid = player.getUniqueId();

        if (isContractCompleted(uuid, contract.getId())) {
            player.sendMessage(miniMessage.deserialize("<red>Kamu sudah menyelesaikan kontrak ekspor ini pada periode ini!</red>"));
            return false;
        }

        int current = getPlayerProgress(uuid, contract.getId());
        int needed = contract.getRequiredAmount() - current;
        if (needed <= 0) return false;

        // Count how many matching items player has in inventory
        int countInInv = 0;
        for (ItemStack is : player.getInventory().getContents()) {
            if (is != null && is.getType() == contract.getRequiredMaterial()) {
                countInInv += is.getAmount();
            }
        }

        if (countInInv <= 0) {
            player.sendMessage(miniMessage.deserialize("<red>Kamu tidak memiliki <gold>" + contract.getRequiredMaterial().name() + "</gold> di inventori!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
            return false;
        }

        int toTake = Math.min(countInInv, needed);
        int remainingToTake = toTake;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack is = player.getInventory().getItem(i);
            if (is != null && is.getType() == contract.getRequiredMaterial()) {
                int stackAmount = is.getAmount();
                if (stackAmount <= remainingToTake) {
                    player.getInventory().setItem(i, null);
                    remainingToTake -= stackAmount;
                } else {
                    is.setAmount(stackAmount - remainingToTake);
                    remainingToTake = 0;
                }
                if (remainingToTake <= 0) break;
            }
        }

        int newProgress = current + toTake;
        playerProgress.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(contract.getId(), newProgress);

        if (newProgress >= contract.getRequiredAmount()) {
            // Contract Completed!
            completedContracts.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).add(contract.getId());

            // Payouts
            plugin.getEconomyHook().deposit(player, contract.getRewardRupiah());
            plugin.getKingdomCoreHook().addXp(uuid, contract.getRewardKingdomXp());

            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

            String formattedReward = plugin.getEconomyHook().format(contract.getRewardRupiah());
            player.sendMessage(miniMessage.deserialize(
                    "\n<green><bold>[✔ KONTRAK SELESAI]</bold></green> <gold><bold>" + contract.getTitle() + "</bold></gold>\n" +
                    " <gray>Imbalan Diterima:</gray> <green><bold>" + formattedReward + "</bold></green> <dark_gray>•</dark_gray> <gold><bold>+" + contract.getRewardKingdomXp() + " XP Kerajaan</bold></gold>\n" +
                    " <yellow>Terima kasih atas kontribusi pasokan bagi kemakmuran " + contract.getKingdomDisplayName() + "!</yellow>\n"
            ));
        } else {
            // Partial Delivery
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
            player.sendMessage(miniMessage.deserialize(
                    "<green>[✔]</green> <gray>Berhasil menyetor <gold>" + toTake + "x " + contract.getRequiredMaterial().name() + "</gold> untuk kontrak <yellow>" + contract.getTitle() + "</yellow>! <dark_gray>(Progres: " + newProgress + "/" + contract.getRequiredAmount() + ")</dark_gray></gray>"
            ));
        }

        saveDataAsync();
        return true;
    }

    public long getRemainingSeconds() {
        long diff = nextResetTime - System.currentTimeMillis();
        return Math.max(0, diff / 1000L);
    }

    public String getFormattedRemainingTime() {
        long totalSeconds = getRemainingSeconds();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02dj %02dm %02dd", hours, minutes, seconds);
        } else {
            return String.format("%02dm %02dd", minutes, seconds);
        }
    }

    private void loadData() {
        if (!dataFile.exists()) {
            dataConfig = new YamlConfiguration();
            return;
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        this.nextResetTime = dataConfig.getLong("nextResetTime", 0L);

        if (dataConfig.contains("progress")) {
            for (String uuidStr : dataConfig.getConfigurationSection("progress").getKeys(false)) {
                try {
                    UUID u = UUID.fromString(uuidStr);
                    Map<String, Integer> progMap = new ConcurrentHashMap<>();
                    for (String cId : dataConfig.getConfigurationSection("progress." + uuidStr).getKeys(false)) {
                        progMap.put(cId, dataConfig.getInt("progress." + uuidStr + "." + cId));
                    }
                    playerProgress.put(u, progMap);
                } catch (Exception ignored) {}
            }
        }

        if (dataConfig.contains("completed")) {
            for (String uuidStr : dataConfig.getConfigurationSection("completed").getKeys(false)) {
                try {
                    UUID u = UUID.fromString(uuidStr);
                    List<String> done = dataConfig.getStringList("completed." + uuidStr);
                    completedContracts.put(u, new HashSet<>(done));
                } catch (Exception ignored) {}
            }
        }
    }

    public void saveData() {
        if (dataConfig == null) dataConfig = new YamlConfiguration();
        dataConfig.set("nextResetTime", nextResetTime);

        for (Map.Entry<UUID, Map<String, Integer>> entry : playerProgress.entrySet()) {
            for (Map.Entry<String, Integer> prog : entry.getValue().entrySet()) {
                dataConfig.set("progress." + entry.getKey().toString() + "." + prog.getKey(), prog.getValue());
            }
        }

        for (Map.Entry<UUID, Set<String>> entry : completedContracts.entrySet()) {
            dataConfig.set("completed." + entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save contracts_data.yml: " + e.getMessage());
        }
    }

    public void saveDataAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveData);
    }
}
