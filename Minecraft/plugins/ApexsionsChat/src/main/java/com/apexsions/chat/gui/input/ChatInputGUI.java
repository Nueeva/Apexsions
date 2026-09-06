package com.apexsions.chat.gui.input;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

/**
 * Unified GUI Input helper for ApexsionsChat.
 * Routes input requests cleanly through CustomInputTextGUI
 * (Bedrock Native Forms, Minecraft 26.2 Dialogs, NightCore Dialog API, Paper Dialog API).
 * NEVER uses chest keypad slots or anvil inventories.
 */
public class ChatInputGUI {

    public static void openInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                 Consumer<String> onInput, Runnable onCancel) {
        CustomInputTextGUI.open(plugin, player, title, prompt, defaultText, onInput, onCancel);
    }

    public static void openInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                 Consumer<String> onInput) {
        CustomInputTextGUI.open(plugin, player, title, prompt, defaultText, onInput, null);
    }
}
