package com.apexsions.fishing.gui.dialog;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Adapter to safely interact with Geyser / Floodgate on Bedrock Edition for ApexsionsFishing.
 * Uses reflection so Floodgate is an optional soft-dependency at compile-time and runtime.
 */
public class BedrockFormAdapter {

    private static Boolean floodgateAvailable = null;

    public static boolean isFloodgatePresent() {
        if (floodgateAvailable == null) {
            try {
                Class.forName("org.geysermc.floodgate.api.FloodgateApi");
                floodgateAvailable = true;
            } catch (ClassNotFoundException e) {
                floodgateAvailable = false;
            }
        }
        return floodgateAvailable;
    }

    public static boolean isBedrockPlayer(Player player) {
        if (player == null) return false;

        // Layer 1: Official Floodgate API check
        if (isFloodgatePresent()) {
            try {
                Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
                Method getInstance = apiClass.getMethod("getInstance");
                Object api = getInstance.invoke(null);
                Method isFloodgate = apiClass.getMethod("isFloodgatePlayer", UUID.class);
                Boolean isBedrock = (Boolean) isFloodgate.invoke(api, player.getUniqueId());
                if (Boolean.TRUE.equals(isBedrock)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        // Layer 2: Username prefix check (Floodgate default '.' prefix configured on server)
        if (player.getName().startsWith(".")) {
            return true;
        }

        // Layer 3: Floodgate UUID format check (Floodgate Bedrock UUIDs have most-significant bits set to 0)
        if (player.getUniqueId().getMostSignificantBits() == 0L) {
            return true;
        }

        return false;
    }

    /**
     * Opens a Bedrock native CustomForm modal with a text input box.
     */
    public static boolean openInputForm(Plugin plugin, Player player, String title, String prompt, String defaultText, Consumer<String> onInput, Runnable onCancel) {
        if (!isBedrockPlayer(player)) return false;

        try {
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object api = apiClass.getMethod("getInstance").invoke(null);

            // Cumulus CustomForm Builder
            Class<?> customFormClass = Class.forName("org.geysermc.cumulus.form.CustomForm");
            Method builderMethod = customFormClass.getMethod("builder");
            Object builder = builderMethod.invoke(null);

            // Set title
            builder.getClass().getMethod("title", String.class).invoke(builder, title != null ? title : "Input Required");

            // Set text input field
            String cleanPrompt = prompt != null ? prompt : "Masukkan nilai:";
            String cleanDefault = defaultText != null ? defaultText : "";
            builder.getClass().getMethod("input", String.class, String.class, String.class, String.class)
                    .invoke(builder, "input_field", cleanPrompt, cleanDefault, cleanDefault);

            // Set valid result handler
            Class<?> responseClass = Class.forName("org.geysermc.cumulus.response.CustomFormResponse");
            java.lang.reflect.InvocationHandler validHandler = (proxy, method, args) -> {
                if (method.getName().equals("accept") || method.getName().equals("handle")) {
                    Object response = args[0];
                    if (response != null) {
                        Method asInput = response.getClass().getMethod("asInput", String.class);
                        String value = (String) asInput.invoke(response, "input_field");
                        if (value == null) value = "";
                        final String finalVal = value.trim();
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            try {
                                onInput.accept(finalVal);
                            } catch (Throwable t) {
                                player.sendMessage("§cError processing input: " + t.getMessage());
                            }
                        });
                    }
                }
                return null;
            };

            Class<?> consumerClass = Consumer.class;
            Object validResultConsumer = java.lang.reflect.Proxy.newProxyInstance(
                    consumerClass.getClassLoader(),
                    new Class<?>[]{consumerClass},
                    validHandler
            );

            for (Method m : builder.getClass().getMethods()) {
                if (m.getName().equals("validResultHandler") && m.getParameterCount() == 1) {
                    m.invoke(builder, validResultConsumer);
                    break;
                }
            }

            // Set closed or cancel handler
            if (onCancel != null) {
                java.lang.reflect.InvocationHandler closedHandler = (proxy, method, args) -> {
                    if (method.getName().equals("accept") || method.getName().equals("handle")) {
                        Bukkit.getScheduler().runTask(plugin, onCancel);
                    }
                    return null;
                };

                Object closedConsumer = java.lang.reflect.Proxy.newProxyInstance(
                        consumerClass.getClassLoader(),
                        new Class<?>[]{consumerClass},
                        closedHandler
                );

                for (Method m : builder.getClass().getMethods()) {
                    if (m.getName().equals("closedOrInvalidResultHandler") && m.getParameterCount() == 1) {
                        m.invoke(builder, closedConsumer);
                        break;
                    }
                }
            }

            // Build and send form to player
            Object form = builder.getClass().getMethod("build").invoke(builder);
            Method sendForm = apiClass.getMethod("sendForm", UUID.class, Object.class);
            sendForm.invoke(api, player.getUniqueId(), form);
            return true;
        } catch (Throwable t) {
            plugin.getLogger().warning("[BedrockFormAdapter] Failed to open Bedrock dialog: " + t.getMessage());
            return false;
        }
    }
}
