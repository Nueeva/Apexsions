package com.apexsions.crate.integration;

import com.apexsions.core.admin.AdminModule;
import com.apexsions.crate.ApexsionsCratePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class ApexsionsCrateAdminModule implements AdminModule {

    private final ApexsionsCratePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ApexsionsCrateAdminModule(ApexsionsCratePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "crate";
    }

    @Override
    public Component getDisplayName() {
        return mm.deserialize("<gradient:#f39c12:#e74c3c><bold>🎁 APEXSIONS CRATES</bold></gradient>");
    }

    @Override
    public Material getIcon() {
        return Material.CHEST;
    }

    @Override
    public List<Component> getDescription(Player player) {
        return List.of(
                mm.deserialize("<gray>Kelola Crate, Kunci, Milestones & Editor.</gray>"),
                mm.deserialize("<yellow>▶ Klik untuk membuka Editor Crate Interaktif</yellow>")
        );
    }

    @Override
    public String getPermission() {
        return "apexsions.admin";
    }

    @Override
    public int getPriority() {
        return 65;
    }

    @Override
    public void open(Player admin) {
        plugin.getEditorManager().openEditor(admin);
    }
}
