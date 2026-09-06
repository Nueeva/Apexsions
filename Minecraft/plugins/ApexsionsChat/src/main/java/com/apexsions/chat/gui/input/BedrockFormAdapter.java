package com.apexsions.chat.gui.input;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Adapter for Bedrock players via Floodgate Cumulus forms.
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
        if (player == null || !isFloodgatePresent()) return false;
        try {
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Method getInstance = apiClass.getMethod("getInstance");
            Object api = getInstance.invoke(null);
            Method isFloodgate = apiClass.getMethod("isFloodgatePlayer", UUID.class);
            return (Boolean) isFloodgate.invoke(api, player.getUniqueId());
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean openInputForm(Plugin plugin, Player player, String title, String prompt, String defaultText, Consumer<String> onInput, Runnable onCancel) {
        if (!isBedrockPlayer(player)) return false;

        try {
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object api = apiClass.getMethod("getInstance").invoke(null);

            Class<?> customFormClass = Class.forName("org.geysermc.cumulus.form.CustomForm");
            Method builderMethod = customFormClass.getMethod("builder");
            Object builder = builderMethod.invoke(null);

            builder.getClass().getMethod("title", String.class).invoke(builder, title != null ? title : "Apexsions Chat Input");

            String cleanPrompt = prompt != null ? prompt : "Ketik pesanmu:";
            String cleanDefault = defaultText != null ? defaultText : "";
            builder.getClass().getMethod("input", String.class, String.class, String.class, String.class)
                    .invoke(builder, "input_field", cleanPrompt, cleanDefault, cleanDefault);

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
                            } catch (Exception ex) {
                                player.sendMessage("§cError: " + ex.getMessage());
                            }
                        });
                    }
                }
                return null;
            };

            Class<?> consumerClass = Class.forName("org.geysermc.cumulus.util.FormImage").getClassLoader()
                    .loadClass("java.util.function.Consumer");
            Object validProxy = java.lang.reflect.Proxy.newProxyInstance(
                    consumerClass.getClassLoader(),
                    new Class<?>[]{consumerClass},
                    validHandler
            );
            builder.getClass().getMethod("validResultHandler", consumerClass).invoke(builder, validProxy);

            java.lang.reflect.InvocationHandler cancelHandler = (proxy, method, args) -> {
                if (onCancel != null) {
                    Bukkit.getScheduler().runTask(plugin, onCancel);
                }
                return null;
            };
            Object cancelProxy = java.lang.reflect.Proxy.newProxyInstance(
                    consumerClass.getClassLoader(),
                    new Class<?>[]{consumerClass},
                    cancelHandler
            );
            try {
                builder.getClass().getMethod("closedOrInvalidResultHandler", consumerClass).invoke(builder, cancelProxy);
            } catch (NoSuchMethodException ignored) {
                try {
                    builder.getClass().getMethod("closedResultHandler", Runnable.class).invoke(builder, onCancel);
                } catch (NoSuchMethodException ignored2) {}
            }

            Object form = builder.getClass().getMethod("build").invoke(builder);
            Method sendForm = api.getClass().getMethod("sendForm", UUID.class, Object.class);
            sendForm.invoke(api, player.getUniqueId(), form);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
