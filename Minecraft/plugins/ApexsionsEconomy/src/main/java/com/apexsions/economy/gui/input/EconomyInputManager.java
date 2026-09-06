package com.apexsions.economy.gui.input;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class EconomyInputManager {

    public static void openInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                 boolean numericOnly, Consumer<String> onInput, Runnable onCancel) {
        CustomInputTextGUI.open(plugin, player, title, prompt, defaultText, onInput, onCancel);
    }

    public static void openInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                 Consumer<String> onInput, Runnable onCancel) {
        CustomInputTextGUI.open(plugin, player, title, prompt, defaultText, onInput, onCancel);
    }
}
