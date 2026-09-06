package com.apexsions.crates;

import com.apexsions.core.api.ApexsionsCoreProvider;
import com.apexsions.crates.animation.InWorldOpening;
import com.apexsions.crates.animation.InstantOpening;
import com.apexsions.crates.animation.OpeningSession;
import com.apexsions.crates.animation.RouletteOpening;
import com.apexsions.crates.api.ApexsionsCratesAPI;
import com.apexsions.crates.api.ApexsionsCratesProvider;
import com.apexsions.crates.api.CrateOpenEvent;
import com.apexsions.crates.commands.CrateAdminCommand;
import com.apexsions.crates.commands.CrateCommand;
import com.apexsions.crates.crate.Crate;
import com.apexsions.crates.crate.CrateAnimationType;
import com.apexsions.crates.crate.CrateLocation;
import com.apexsions.crates.crate.CrateManager;
import com.apexsions.crates.database.CratesRepository;
import com.apexsions.crates.gui.CratePreviewGUI;
import com.apexsions.crates.gui.CratesGUIListener;
import com.apexsions.crates.hologram.CrateHologramManager;
import com.apexsions.crates.integration.ApexsionsCratesAdminModule;
import com.apexsions.crates.integration.ApexsionsCratesPAPI;
import com.apexsions.crates.integration.ApexsionsIntegrationListener;
import com.apexsions.crates.key.CrateKey;
import com.apexsions.crates.key.KeyManager;
import com.apexsions.crates.listener.CrateBlockInteractListener;
import com.apexsions.crates.listener.CratePlayerConnectionListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ApexsionsCratesPlugin extends JavaPlugin implements ApexsionsCratesAPI {

    private static ApexsionsCratesPlugin instance;

    private CratesRepository repository;
    private KeyManager keyManager;
    private CrateManager crateManager;
    private CrateHologramManager hologramManager;

    private FileConfiguration messages;
    private final Map<UUID, OpeningSession> activeSessions = new ConcurrentHashMap<>();

    public static ApexsionsCratesPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // 1. Save defaults
        saveDefaultConfig();
        saveResourceIfNotExists("keys.yml");
        saveResourceIfNotExists("messages.yml");
        saveResourceIfNotExists("crates/luxury.yml");
        saveResourceIfNotExists("crates/novice.yml");

        loadMessages();

        // 2. Initialize Database & Managers
        this.repository = new CratesRepository(this);
        this.repository.init();

        this.keyManager = new KeyManager(this);
        File keysFile = new File(getDataFolder(), "keys.yml");
        this.keyManager.loadKeys(YamlConfiguration.loadConfiguration(keysFile));

        this.crateManager = new CrateManager(this);
        this.crateManager.loadCrates();

        this.hologramManager = new CrateHologramManager(this);

        // 3. Load Crate Locations from DB
        this.repository.loadLocations().thenAccept(locs -> {
            Bukkit.getScheduler().runTask(this, () -> {
                for (CrateLocation cl : locs) {
                    crateManager.addLocation(cl);
                }
                hologramManager.spawnAllHolograms();
                getLogger().info("Restored " + locs.size() + " in-world crate locations & holograms.");
            });
        });

        // 4. Register API Provider
        ApexsionsCratesProvider.register(this);

        // 5. Register Listeners
        getServer().getPluginManager().registerEvents(new CrateBlockInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new CratesGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new CratePlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new ApexsionsIntegrationListener(this), this);

        // 6. Register Commands
        CrateCommand crateCmd = new CrateCommand(this);
        PluginCommand crate = getCommand("crate");
        if (crate != null) {
            crate.setExecutor(crateCmd);
            crate.setTabCompleter(crateCmd);
        }

        CrateAdminCommand acratesCmd = new CrateAdminCommand(this);
        PluginCommand acrates = getCommand("acrates");
        if (acrates != null) {
            acrates.setExecutor(acratesCmd);
            acrates.setTabCompleter(acratesCmd);
        }

        // 7. Register PAPI expansion if available
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new ApexsionsCratesPAPI(this).register();
        }

        // 8. Register into Master Admin Hub (/admingui)
        if (ApexsionsCoreProvider.isAvailable()) {
            try {
                ApexsionsCoreProvider.get().registerAdminModule(new ApexsionsCratesAdminModule(this));
                getLogger().info("Successfully registered ApexsionsCrates into ApexsionsCore Master Admin Hub.");
            } catch (Exception e) {
                getLogger().warning("Failed to register into Master Admin Hub: " + e.getMessage());
            }
        }

        getLogger().info("==================================================");
        getLogger().info("  ApexsionsCrates v" + getDescription().getVersion() + " Enabled Successfully");
        getLogger().info("  Native Luxury Crate & Milestone Architecture");
        getLogger().info("==================================================");
    }

    @Override
    public void onDisable() {
        // Complete/cancel active sessions
        for (OpeningSession session : activeSessions.values()) {
            try {
                session.skip();
            } catch (Exception ignored) {}
        }
        activeSessions.clear();

        // Remove holograms
        if (hologramManager != null) {
            hologramManager.removeAllHolograms();
        }

        // Close DB
        if (repository != null) {
            repository.close();
        }

        ApexsionsCratesProvider.unregister();
        instance = null;
    }

    public void reloadPlugin() {
        reloadConfig();
        loadMessages();

        File keysFile = new File(getDataFolder(), "keys.yml");
        keyManager.loadKeys(YamlConfiguration.loadConfiguration(keysFile));
        crateManager.loadCrates();

        hologramManager.removeAllHolograms();
        hologramManager.spawnAllHolograms();
    }

    private void loadMessages() {
        File f = new File(getDataFolder(), "messages.yml");
        if (f.exists()) {
            this.messages = YamlConfiguration.loadConfiguration(f);
        } else {
            this.messages = new YamlConfiguration();
        }
    }

    private void saveResourceIfNotExists(String resourcePath) {
        File file = new File(getDataFolder(), resourcePath);
        if (!file.exists()) {
            saveResource(resourcePath, false);
        }
    }

    public CratesRepository getRepository() {
        return repository;
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }

    public CrateManager getCrateManager() {
        return crateManager;
    }

    public CrateHologramManager getHologramManager() {
        return hologramManager;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public boolean isOpening(UUID uuid) {
        return activeSessions.containsKey(uuid);
    }

    public OpeningSession getSession(UUID uuid) {
        return activeSessions.get(uuid);
    }

    public void unregisterSession(UUID uuid) {
        activeSessions.remove(uuid);
    }

    public void startOpeningSession(Player player, Crate crate, boolean virtualKey, Location crateLoc) {
        CrateOpenEvent event = new CrateOpenEvent(player, crate, virtualKey);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        OpeningSession session;
        if (crate.getAnimationType() == CrateAnimationType.INSTANT) {
            session = new InstantOpening(this, player, crate, virtualKey);
        } else if (crate.getAnimationType() == CrateAnimationType.IN_WORLD) {
            session = new InWorldOpening(this, player, crate, virtualKey, crateLoc);
        } else {
            session = new RouletteOpening(this, player, crate, virtualKey);
        }

        activeSessions.put(player.getUniqueId(), session);
        session.start();
    }

    // --- ApexsionsCratesAPI Implementation ---

    @Override
    public Collection<Crate> getCrates() {
        return crateManager.getCrates();
    }

    @Override
    public Crate getCrate(String id) {
        return crateManager.getCrate(id);
    }

    @Override
    public Collection<CrateKey> getKeys() {
        return keyManager.getKeys();
    }

    @Override
    public CrateKey getKey(String id) {
        return keyManager.getKey(id);
    }

    @Override
    public CompletableFuture<Integer> getVirtualKeys(UUID uuid, String keyId) {
        return repository.getVirtualKeys(uuid, keyId);
    }

    @Override
    public CompletableFuture<Void> setVirtualKeys(UUID uuid, String keyId, int amount) {
        return repository.setVirtualKeys(uuid, keyId, amount);
    }

    @Override
    public CompletableFuture<Void> addVirtualKeys(UUID uuid, String keyId, int amount) {
        return repository.addVirtualKeys(uuid, keyId, amount);
    }

    @Override
    public CompletableFuture<Boolean> takeVirtualKeys(UUID uuid, String keyId, int amount) {
        return repository.takeVirtualKeys(uuid, keyId, amount);
    }

    @Override
    public CompletableFuture<Integer> getCrateOpenCount(UUID uuid, String crateId) {
        return repository.getCrateOpenCount(uuid, crateId);
    }

    @Override
    public boolean openCrate(Player player, Crate crate, boolean useVirtualKey) {
        if (player == null || crate == null) return false;
        if (isOpening(player.getUniqueId())) return false;

        String reqKey = crate.getRequiredKeyId();
        if (useVirtualKey) {
            repository.takeVirtualKeys(player.getUniqueId(), reqKey, 1).thenAccept(success -> {
                Bukkit.getScheduler().runTask(this, () -> {
                    if (success) {
                        startOpeningSession(player, crate, true, null);
                    } else {
                        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(
                                "<red>Saldo kunci virtual <yellow>" + reqKey + "</yellow> Anda tidak mencukupi!</red>"
                        ));
                    }
                });
            });
            return true;
        } else {
            if (keyManager.takePhysicalKey(player, reqKey, 1)) {
                startOpeningSession(player, crate, false, null);
                return true;
            }
            return false;
        }
    }

    @Override
    public void previewCrate(Player player, Crate crate) {
        if (player != null && crate != null) {
            new CratePreviewGUI(this, player, crate).open();
        }
    }

    @Override
    public void setCrateLocation(Location location, String crateId) {
        if (location == null || crateId == null) return;
        CrateLocation cl = CrateLocation.fromLocation(location, crateId);
        crateManager.addLocation(cl);
        repository.saveLocation(cl);
        hologramManager.spawnHologram(cl);
    }

    @Override
    public void removeCrateLocation(Location location) {
        if (location == null) return;
        CrateLocation cl = crateManager.getCrateLocationAt(location);
        if (cl != null) {
            crateManager.removeLocation(cl);
            repository.deleteLocation(cl);
            hologramManager.removeHologram(cl);
        }
    }

    @Override
    public boolean isCrateLocation(Location location) {
        return crateManager.isCrateLocation(location);
    }

    @Override
    public Crate getCrateAt(Location location) {
        return crateManager.getCrateAt(location);
    }
}
