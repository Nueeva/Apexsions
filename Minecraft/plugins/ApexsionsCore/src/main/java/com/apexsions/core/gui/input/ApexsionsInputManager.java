package com.apexsions.core.gui.input;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

/**
 * Master GUI Input Coordinator for Apexsions.
 * Automatically selects the best input method for the player:
 * - Bedrock Edition -> Floodgate Native CustomForm (Modal GUI input)
 * - Java Edition 26.2 -> Paper Native Dialog GUI (Screenshot match)
 * - Java Fallback -> Universal Anvil Text Input GUI (Typing on keyboard, no chest GUI)
 */
public class ApexsionsInputManager {

    public static void openInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                 boolean numericOnly, Consumer<String> onInput, Runnable onCancel) {
        // 1. Bedrock Edition check (Floodgate / Geyser)
        if (BedrockFormAdapter.isBedrockPlayer(player)) {
            if (BedrockFormAdapter.openInputForm(plugin, player, title, prompt, defaultText, onInput, onCancel)) {
                return;
            }
        }

        // 2. Java Edition Minecraft 26.2 Native Dialog check
        if (PaperDialogAdapter.isSupported()) {
            if (PaperDialogAdapter.showInput(plugin, player, title, prompt, defaultText, onInput, onCancel)) {
                return;
            }
        }

        // 3. Fallback: Native Text Input GUI via Anvil (real keyboard typing, not a chest GUI)
        new AnvilTextInputGUI(plugin, player, title, prompt, defaultText, onInput, onCancel).open();
    }

    public static void openTextInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                     Consumer<String> onInput, Runnable onCancel) {
        openInput(plugin, player, title, prompt, defaultText, false, onInput, onCancel);
    }

    public static void openNumericInput(Plugin plugin, Player player, String title, String prompt, int defaultValue,
                                        int min, int max, Consumer<Integer> onNumber, Runnable onCancel) {
        openInput(plugin, player, title, prompt + " (" + min + " - " + max + ")", String.valueOf(defaultValue), true, input -> {
            try {
                int val = Integer.parseInt(input.trim());
                if (val < min || val > max) {
                    player.sendMessage("§cAngka harus berada di antara " + min + " dan " + max + "!");
                    if (onCancel != null) onCancel.run();
                    return;
                }
                onNumber.accept(val);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInput bukan angka yang valid!");
                if (onCancel != null) onCancel.run();
            }
        }, onCancel);
    }

    public static void openDoubleInput(Plugin plugin, Player player, String title, String prompt, double defaultValue,
                                       double min, double max, Consumer<Double> onNumber, Runnable onCancel) {
        openInput(plugin, player, title, prompt, String.valueOf(defaultValue), true, input -> {
            try {
                double val = Double.parseDouble(input.trim().replace(',', '.'));
                if (val < min || val > max) {
                    player.sendMessage("§cNilai harus berada di antara " + min + " dan " + max + "!");
                    if (onCancel != null) onCancel.run();
                    return;
                }
                onNumber.accept(val);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInput bukan angka desimal yang valid!");
                if (onCancel != null) onCancel.run();
            }
        }, onCancel);
    }
}
