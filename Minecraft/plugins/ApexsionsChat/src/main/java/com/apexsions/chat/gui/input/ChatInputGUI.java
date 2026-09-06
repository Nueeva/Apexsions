package com.apexsions.chat.gui.input;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

/**
 * Unified GUI Input helper for ApexsionsChat.
 * Routes input requests cleanly through Bedrock Forms for Bedrock players,
 * Minecraft 26.2 Dialogs for Java 26.2 players, and AnvilTextInputGUI as fallback.
 */
public class ChatInputGUI {

    public static void openInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                 Consumer<String> onInput, Runnable onCancel) {
        if (BedrockFormAdapter.isBedrockPlayer(player)) {
            if (BedrockFormAdapter.openInputForm(plugin, player, title, prompt, defaultText, onInput, onCancel)) {
                return;
            }
        }

        if (PaperDialogAdapter.isSupported()) {
            if (PaperDialogAdapter.showInput(plugin, player, title, prompt, defaultText, onInput, onCancel)) {
                return;
            }
        }

        new AnvilTextInputGUI(plugin, player, title, prompt, defaultText, onInput, onCancel).open();
    }
}
