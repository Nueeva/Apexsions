package com.apexsions.crates;

import com.apexsions.core.api.ApexsionsCoreProvider;
import com.apexsions.crates.command.CrateShopCommand;
import com.apexsions.crates.config.Config;
import com.apexsions.crates.config.Perms;
import com.apexsions.crates.integration.ApexsionsCratesAdminModule;
import com.apexsions.crates.integration.ApexsionsIntegrationListener;
import com.apexsions.crates.shop.CrateKeyShopManager;
import com.apexsions.crates.shop.gui.CrateKeyShopListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.PluginDetails;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class ApexsionsCratesPlugin extends CratesPlugin {

    private static ApexsionsCratesPlugin instance;
    private CrateKeyShopManager keyShopManager;

    public static ApexsionsCratesPlugin getInstance() {
        return instance;
    }

    @Override
    @NotNull
    protected PluginDetails getDefaultDetails() {
        return PluginDetails.create("ApexsionsCrates", new String[]{"crate", "crates", "apexsionscrates", "case", "cases"})
                .setConfigClass(Config.class)
                .setPermissionsClass(Perms.class);
    }

    @Override
    public void enable() {
        instance = this;
        super.enable();

        // Initialize Crate Key Shop System
        this.keyShopManager = new CrateKeyShopManager(this);
        this.keyShopManager.load();

        // Register GUI click listener
        getServer().getPluginManager().registerEvents(new CrateKeyShopListener(), this);

        // Register event listener for Core and Battlepass integrations
        getServer().getPluginManager().registerEvents(new ApexsionsIntegrationListener(this), this);

        // Register /crateshop command
        registerCrateShopCommand();

        // Register into Master Admin Hub (/admingui)
        if (ApexsionsCoreProvider.isAvailable()) {
            try {
                ApexsionsCoreProvider.get().registerAdminModule(new ApexsionsCratesAdminModule(this));
                getLogger().info("Successfully registered ApexsionsCrates into ApexsionsCore Master Admin Hub.");
            } catch (Exception e) {
                getLogger().warning("Failed to register into ApexsionsCore Master Admin Hub: " + e.getMessage());
            }
        }

        getLogger().info("ApexsionsCrates v" + getDescription().getVersion() + " enabled successfully with full ecosystem integrations!");
    }

    private void registerCrateShopCommand() {
        CrateShopCommand shopCmd = new CrateShopCommand(this);
        PluginCommand cmd = getCommand("crateshop");
        if (cmd != null) {
            cmd.setExecutor(shopCmd);
            cmd.setTabCompleter(shopCmd);
        } else {
            // Dynamic CommandMap registration fallback
            try {
                Field cmdMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                cmdMapField.setAccessible(true);
                CommandMap commandMap = (CommandMap) cmdMapField.get(Bukkit.getServer());
                org.bukkit.command.defaults.BukkitCommand dynamicCmd = new org.bukkit.command.defaults.BukkitCommand(
                        "crateshop",
                        "Buka Toko Pembelian Crate Keys Resmi Apexsions",
                        "/crateshop [admin]",
                        List.of("keyshop", "cratekeyshop")
                ) {
                    @Override
                    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                        return shopCmd.onCommand(sender, this, commandLabel, args);
                    }

                    @Override
                    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
                        List<String> list = shopCmd.onTabComplete(sender, this, alias, args);
                        return list != null ? list : Collections.emptyList();
                    }
                };
                commandMap.register("apexsionscrates", dynamicCmd);
            } catch (Exception e) {
                getLogger().warning("Failed to register dynamic /crateshop command: " + e.getMessage());
            }
        }
    }

    @NotNull
    public CrateKeyShopManager getKeyShopManager() {
        return keyShopManager;
    }

    @Override
    public void disable() {
        if (this.keyShopManager != null) {
            this.keyShopManager.save();
        }
        super.disable();
        instance = null;
    }
}
