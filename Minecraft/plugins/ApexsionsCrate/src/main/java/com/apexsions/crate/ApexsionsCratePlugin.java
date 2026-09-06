package com.apexsions.crate;

import com.apexsions.core.api.ApexsionsCoreProvider;
import com.apexsions.crate.integration.ApexsionsCrateAdminModule;
import com.apexsions.crate.integration.ApexsionsIntegrationListener;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.config.Config;
import su.nightexpress.excellentcrates.config.Perms;
import su.nightexpress.nightcore.config.PluginDetails;

public class ApexsionsCratePlugin extends CratesPlugin {

    private static ApexsionsCratePlugin instance;

    public static ApexsionsCratePlugin getInstance() {
        return instance;
    }

    @Override
    @NotNull
    protected PluginDetails getDefaultDetails() {
        return PluginDetails.create("ApexsionsCrate", new String[]{"crate", "crates", "apexsionscrate", "ecrates", "excellentcrates", "case", "cases"})
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
                ApexsionsCoreProvider.get().registerAdminModule(new ApexsionsCrateAdminModule(this));
                getLogger().info("Successfully registered ApexsionsCrate into ApexsionsCore Master Admin Hub.");
            } catch (Exception e) {
                getLogger().warning("Failed to register into ApexsionsCore Master Admin Hub: " + e.getMessage());
            }
        }

        getLogger().info("ApexsionsCrate v" + getDescription().getVersion() + " enabled successfully with full ecosystem integrations!");
    }

    @Override
    public void disable() {
        super.disable();
        instance = null;
    }
}
