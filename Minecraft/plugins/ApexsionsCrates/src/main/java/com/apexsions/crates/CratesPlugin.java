package com.apexsions.crates;

import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.api.addon.CratesAddon;
import com.apexsions.crates.command.BaseCommands;
import com.apexsions.crates.config.Config;
import com.apexsions.crates.config.Keys;
import com.apexsions.crates.config.Lang;
import com.apexsions.crates.config.Perms;
import com.apexsions.crates.crate.CrateManager;
import com.apexsions.crates.crate.cost.type.impl.EcoCostType;
import com.apexsions.crates.data.DataHandler;
import com.apexsions.crates.data.DataManager;
import com.apexsions.crates.dialog.DialogRegistry;
import com.apexsions.crates.editor.EditorManager;
import com.apexsions.crates.hologram.HologramManager;
import com.apexsions.crates.hooks.impl.PlaceholderHook;
import com.apexsions.crates.key.KeyManager;
import com.apexsions.crates.opening.OpeningManager;
import com.apexsions.crates.opening.ProviderRegistry;
import com.apexsions.crates.registry.CratesRegistries;
import com.apexsions.crates.user.UserManager;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.commands.command.NightCommand;
import su.nightexpress.nightcore.config.PluginDetails;
import su.nightexpress.nightcore.util.Plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class CratesPlugin extends NightPlugin {

    private final List<CratesAddon> addons = new ArrayList<>();

    private DialogRegistry dialogRegistry;

    private DataHandler dataHandler;
    private DataManager dataManager;
    private UserManager userManager;

    private HologramManager hologramManager;
    private OpeningManager  openingManager;
    private KeyManager      keyManager;
    private CrateManager    crateManager;
    private EditorManager   editorManager;

    private CrateLogger crateLogger;

    @Override
    @NotNull
    protected PluginDetails getDefaultDetails() {
        return PluginDetails.create("Crates", new String[]{"crates", "ecrates", "apexsionscrates", "crate", "case", "cases"})
            .setConfigClass(Config.class)
            .setPermissionsClass(Perms.class);
    }

    @Override
    protected boolean disableCommandManager() {
        return true;
    }

    @Override
    protected void onStartup() {
        CratesAPI.load(this);
        Keys.load(this);
    }

    @Override
    protected void addRegistries() {
        this.registerLang(Lang.class);
    }

    @Override
    public void enable() {
        this.crateLogger = new CrateLogger(this);
        this.dialogRegistry = new DialogRegistry(this);

        ProviderRegistry.load();
        CratesRegistries.load(this);
        CratesRegistries.registerCostType(new EcoCostType(this, this.dialogRegistry));
        this.proceedAddons(CratesAddon::onInit);

        this.dataHandler = new DataHandler(this);
        this.dataHandler.setup();

        this.dataManager = new DataManager(this);
        this.dataManager.setup();

        this.userManager = new UserManager(this, this.dataHandler);
        this.userManager.setup();

        if (Config.HOLOGRAMS_ENABLED.get()) {
            this.hologramManager = new HologramManager(this);
            this.hologramManager.setup();
        }

        this.openingManager = new OpeningManager(this);
        this.openingManager.setup();

        this.keyManager = new KeyManager(this, this.dialogRegistry);
        this.keyManager.setup();

        this.crateManager = new CrateManager(this, this.dialogRegistry);
        this.crateManager.setup();

        this.editorManager = new EditorManager(this, this.dialogRegistry);
        this.editorManager.setup();

        this.dataHandler.updateRewardLimits();

        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.setup(this);
        }



        this.loadCommands();
        this.proceedAddons(CratesAddon::onLoad);
    }

    @Override
    public void disable() {
        if (this.editorManager != null) this.editorManager.shutdown();
        if (this.openingManager != null) this.openingManager.shutdown();
        if (this.keyManager != null) this.keyManager.shutdown();
        if (this.crateManager != null) this.crateManager.shutdown();
        //if (this.menuManager != null) this.menuManager.shutdown();
        if (this.hologramManager != null) this.hologramManager.shutdown();
        if (this.userManager != null) this.userManager.shutdown();
        if (this.dataManager != null) this.dataManager.shutdown();
        if (this.dataHandler != null) this.dataHandler.shutdown();
        if (this.dialogRegistry != null) this.dialogRegistry.clear();

        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.shutdown();
        }

        CratesRegistries.clear();
        ProviderRegistry.clear();
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();
        Keys.clear();
        CratesAPI.clear();
    }

    protected void loadCommands() {
        this.rootCommand = NightCommand.create(
                this,
                this.getDetails().getCommandAliases(),
                su.nightexpress.nightcore.commands.builder.HubNodeBuilder::new,
                (node, aliases) -> new com.apexsions.crates.command.ApexsionsCratesCommand(this, node, aliases),
                builder -> new BaseCommands(this).load(builder)
        );
    }

    public void registerAddon(@NotNull CratesAddon addon) {
        this.addons.add(addon);
    }

    private void proceedAddons(@NotNull Consumer<CratesAddon> consumer) {
        this.addons.forEach(consumer);
    }

    @NotNull
    public List<CratesAddon> getAddons() {
        return this.addons;
    }

    public boolean hasHolograms() {
        return this.hologramManager != null && this.hologramManager.hasHandler();
    }

    @NotNull
    public Optional<HologramManager> getHologramManager() {
        return Optional.ofNullable(this.hologramManager);
    }

    @NotNull
    public CrateLogger getCrateLogger() {
        return this.crateLogger;
    }

    @NotNull
    public DataHandler getDataHandler() {
        return this.dataHandler;
    }

    @NotNull
    public DataManager getDataManager() {
        return this.dataManager;
    }

    @NotNull
    public UserManager getUserManager() {
        return this.userManager;
    }

    @NotNull
    public OpeningManager getOpeningManager() {
        return this.openingManager;
    }

    @NotNull
    public EditorManager getEditorManager() {
        return this.editorManager;
    }

    @NotNull
    public KeyManager getKeyManager() {
        return this.keyManager;
    }

    @NotNull
    public CrateManager getCrateManager() {
        return this.crateManager;
    }
}
