package com.apexsions.customenchants.gui.input;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class EnchantsInputManager {

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

        new VirtualKeypadGUI(plugin, player, title, prompt, defaultText, onInput, onCancel).open();
    }
}
