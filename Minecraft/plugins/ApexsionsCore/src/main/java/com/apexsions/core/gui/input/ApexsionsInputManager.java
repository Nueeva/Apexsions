package com.apexsions.core.gui.input;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

/**
 * Master GUI Input Coordinator for Apexsions.
 * Uses CustomInputTextGUI (Minecraft 26.2 Dialogs / NightCore / Paper / Bedrock Form).
 */
public class ApexsionsInputManager {

    public static void openInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                 boolean numericOnly, Consumer<String> onInput, Runnable onCancel) {
        CustomInputTextGUI.open(plugin, player, title, prompt, defaultText, onInput, onCancel);
    }

    public static void openTextInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                     Consumer<String> onInput, Runnable onCancel) {
        CustomInputTextGUI.open(plugin, player, title, prompt, defaultText, onInput, onCancel);
    }

    public static void openNumericInput(Plugin plugin, Player player, String title, String prompt, int defaultValue,
                                        int min, int max, Consumer<Integer> onNumber, Runnable onCancel) {
        CustomInputTextGUI.openNumeric(plugin, player, title, prompt, defaultValue, min, max, onNumber, onCancel);
    }

    public static void openDoubleInput(Plugin plugin, Player player, String title, String prompt, double defaultValue,
                                       double min, double max, Consumer<Double> onNumber, Runnable onCancel) {
        CustomInputTextGUI.openDouble(plugin, player, title, prompt, defaultValue, min, max, onNumber, onCancel);
    }
}
