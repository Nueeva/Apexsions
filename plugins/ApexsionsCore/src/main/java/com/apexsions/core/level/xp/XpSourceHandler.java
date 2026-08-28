package com.apexsions.core.level.xp;

import org.bukkit.event.Listener;

/**
 * Common interface for all modular XP source event listeners and handlers.
 */
public interface XpSourceHandler extends Listener {

    /**
     * Gets the associated XP source.
     */
    XpSource getSource();

    /**
     * Checks if this XP source is currently enabled in configuration.
     */
    boolean isEnabled();
}
