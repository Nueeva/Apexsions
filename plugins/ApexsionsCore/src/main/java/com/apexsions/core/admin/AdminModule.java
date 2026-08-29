package com.apexsions.core.admin;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents an administrative module registered in the Centralized Admin Hub.
 */
public interface AdminModule {

    /**
     * Unique identifier of the module (e.g. "core", "chat", "economy", "battlepass", "shop", "media").
     */
    String getId();

    /**
     * Display name formatted with MiniMessage or Component.
     */
    Component getDisplayName();

    /**
     * Visual icon in the Master Admin GUI.
     */
    Material getIcon();

    /**
     * Description lines / lore explaining what this module manages.
     */
    List<Component> getDescription(Player player);

    /**
     * Permission required to execute/open this module.
     */
    String getPermission();

    /**
     * Priority order in the GUI matrix (lower numbers appear first).
     */
    int getPriority();

    /**
     * Action to perform when an authorized admin clicks on this module card.
     */
    void open(Player admin);
}
