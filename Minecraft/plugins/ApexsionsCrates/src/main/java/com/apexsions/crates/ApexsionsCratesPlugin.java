package com.apexsions.crates;

import com.apexsions.core.api.ApexsionsCoreProvider;
import com.apexsions.crates.integration.ApexsionsCratesAdminModule;
import com.apexsions.crates.integration.ApexsionsIntegrationListener;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.config.Config;
import su.nightexpress.excellentcrates.config.Perms;
import su.nightexpress.nightcore.config.PluginDetails;

public class ApexsionsCratesPlugin extends CratesPlugin {

    private static ApexsionsCratesPlugin instance;

    public static ApexsionsCratesPlugin getInstance() {
        return instance;
    }

    @Override
    @NotNull
    protected PluginDetails getDefaultDetails() {
        return PluginDetails.create("ApexsionsCrates", new String[]{"crate", "crates", "apexsionscrate", "apexsionscrates", "ecrates", "excellentcrates", "case", "cases"})
                .setConfigClass(Config.class)
                .setPermissionsClass(Perms.class);
    }

    @Override
    public void enable() {
        instance = this;
        super.enable();

        // Register event listener for Core and Battlepass integrations
        getServer().getPluginManager().registerEvents(new ApexsionsIntegrationListener(this), this);

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

    @Override
    public void disable() {
        super.disable();
        instance = null;
    }
}
