package com.apexsions.fishing.gui.dialog;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

/**
 * Universal Native Dialog Input GUI for ApexsionsFishing.
 * Uses 100% Native Dialogs (NightCore UI Dialog API / Paper Dialog API / Floodgate Bedrock Forms)
 * with a seamless in-game Native GUI fallback (NumericAdjusterGUI).
 *
 * Strictly NEVER uses chat prompts or AsyncPlayerChatEvent.
 */
public class FishingInputGUI {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    /**
     * Opens a Native Dialog Text Input for the player.
     * 1. Prioritizes Bedrock Form for Bedrock players.
     * 2. Uses Native Dialog GUI (NightCore / Paper / Bungee) for Java players.
     * 3. Fallback: Safely invokes onCancel if modal dialog is completely unsupported on client.
     */
    public static void open(Plugin plugin, Player player, String title, String prompt, String defaultText,
                            Consumer<String> onInput, Runnable onCancel) {
        if (player == null || !player.isOnline()) return;

        // 1. Bedrock Edition (Geyser / Floodgate Modal Form)
        if (BedrockFormAdapter.isBedrockPlayer(player)) {
            if (BedrockFormAdapter.openInputForm(plugin, player, title, prompt, defaultText, onInput, onCancel)) {
                return;
            }
        }

        // 2. Java Edition Native Dialog GUI (NightCore Dialogs / Paper Dialogs / Bungee)
        if (NativeDialogAdapter.isSupported()) {
            if (NativeDialogAdapter.showInput(plugin, player, title, prompt, defaultText, onInput, onCancel)) {
                return;
            }
        }

        // If client/server does not support modal dialogs, safely return to creator GUI
        player.sendMessage(mm.deserialize("<red>Native Dialog GUI tidak dapat dibuka pada sesi Anda.</red>"));
        if (onCancel != null) {
            Bukkit.getScheduler().runTask(plugin, onCancel);
        }
    }

    /**
     * Opens a Native Dialog Numeric Input for the player.
     * 1. Prioritizes Bedrock Form for Bedrock players.
     * 2. Uses Native Dialog GUI (NightCore / Paper / Bungee) for Java players.
     * 3. Fallback: Opens Native in-game Chest GUI Adjuster (NumericAdjusterGUI) - NEVER CHAT!
     */
    public static void openNumeric(Plugin plugin, Player player, String title, String prompt, int defaultValue,
                                   int min, int max, Consumer<Integer> onNumber, Runnable onCancel) {
        if (player == null || !player.isOnline()) return;

        String formattedPrompt = prompt + " (" + min + " - " + max + ")";

        // 1. Bedrock Edition (Geyser / Floodgate Modal Form)
        if (BedrockFormAdapter.isBedrockPlayer(player)) {
            boolean shown = BedrockFormAdapter.openInputForm(plugin, player, title, formattedPrompt, String.valueOf(defaultValue), input -> {
                try {
                    int val = Integer.parseInt(input.trim());
                    if (val < min || val > max) {
                        player.sendMessage(mm.deserialize("<red>Nilai angka harus berada di antara " + min + " dan " + max + "!</red>"));
                        if (onCancel != null) onCancel.run();
                        return;
                    }
                    onNumber.accept(val);
                } catch (NumberFormatException e) {
                    player.sendMessage(mm.deserialize("<red>Input '" + input + "' bukan angka bilangan bulat yang valid!</red>"));
                    if (onCancel != null) onCancel.run();
                }
            }, onCancel);
            if (shown) return;
        }

        // 2. Java Edition Native Dialog GUI (NightCore Dialogs / Paper Dialogs)
        if (NativeDialogAdapter.isSupported()) {
            boolean shown = NativeDialogAdapter.showInput(plugin, player, title, formattedPrompt, String.valueOf(defaultValue), input -> {
                try {
                    int val = Integer.parseInt(input.trim());
                    if (val < min || val > max) {
                        player.sendMessage(mm.deserialize("<red>Nilai angka harus berada di antara " + min + " dan " + max + "!</red>"));
                        if (onCancel != null) onCancel.run();
                        return;
                    }
                    onNumber.accept(val);
                } catch (NumberFormatException e) {
                    player.sendMessage(mm.deserialize("<red>Input '" + input + "' bukan angka bilangan bulat yang valid!</red>"));
                    if (onCancel != null) onCancel.run();
                }
            }, onCancel);
            if (shown) return;
        }

        // 3. 100% Native GUI Fallback: Open in-game NumericAdjusterGUI (NEVER CHAT)
        new NumericAdjusterGUI(plugin, player, title, prompt, defaultValue, min, max, onNumber, onCancel).open();
    }

    /**
     * Opens a Native Dialog Double/Decimal Input for the player.
     * 1. Prioritizes Bedrock Form for Bedrock players.
     * 2. Uses Native Dialog GUI (NightCore / Paper / Bungee) for Java players.
     * 3. Fallback: Opens Native in-game Chest GUI Adjuster (NumericAdjusterGUI) - NEVER CHAT!
     */
    public static void openDouble(Plugin plugin, Player player, String title, String prompt, double defaultValue,
                                  double min, double max, Consumer<Double> onNumber, Runnable onCancel) {
        if (player == null || !player.isOnline()) return;

        String formattedPrompt = prompt + " (" + min + " - " + max + ")";

        // 1. Bedrock Edition (Geyser / Floodgate Modal Form)
        if (BedrockFormAdapter.isBedrockPlayer(player)) {
            boolean shown = BedrockFormAdapter.openInputForm(plugin, player, title, formattedPrompt, String.valueOf(defaultValue), input -> {
                try {
                    String sanitized = input.trim().replace(",", ".");
                    double val = Double.parseDouble(sanitized);
                    if (val < min || val > max) {
                        player.sendMessage(mm.deserialize("<red>Nilai desimal harus berada di antara " + min + " dan " + max + "!</red>"));
                        if (onCancel != null) onCancel.run();
                        return;
                    }
                    onNumber.accept(val);
                } catch (NumberFormatException e) {
                    player.sendMessage(mm.deserialize("<red>Input '" + input + "' bukan angka desimal yang valid!</red>"));
                    if (onCancel != null) onCancel.run();
                }
            }, onCancel);
            if (shown) return;
        }

        // 2. Java Edition Native Dialog GUI (NightCore Dialogs / Paper Dialogs)
        if (NativeDialogAdapter.isSupported()) {
            boolean shown = NativeDialogAdapter.showInput(plugin, player, title, formattedPrompt, String.valueOf(defaultValue), input -> {
                try {
                    String sanitized = input.trim().replace(",", ".");
                    double val = Double.parseDouble(sanitized);
                    if (val < min || val > max) {
                        player.sendMessage(mm.deserialize("<red>Nilai desimal harus berada di antara " + min + " dan " + max + "!</red>"));
                        if (onCancel != null) onCancel.run();
                        return;
                    }
                    onNumber.accept(val);
                } catch (NumberFormatException e) {
                    player.sendMessage(mm.deserialize("<red>Input '" + input + "' bukan angka desimal yang valid!</red>"));
                    if (onCancel != null) onCancel.run();
                }
            }, onCancel);
            if (shown) return;
        }

        // 3. 100% Native GUI Fallback: Open in-game NumericAdjusterGUI scaled to percentage (NEVER CHAT)
        int defaultInt = (int) Math.round(defaultValue * 100);
        int minInt = (int) Math.round(min * 100);
        int maxInt = (int) Math.round(max * 100);
        new NumericAdjusterGUI(plugin, player, title, prompt + " (Persentase %)", defaultInt, minInt, maxInt,
                val -> onNumber.accept(val / 100.0), onCancel).open();
    }
}
