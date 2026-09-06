package com.apexsions.customenchants.gui.input;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Consumer;

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

            builder.getClass().getMethod("title", String.class).invoke(builder, title != null ? title : "Item Rename Input");

            String cleanPrompt = prompt != null ? prompt : "Masukkan nama item baru:";
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
                    Runnable.class.getClassLoader(),
                    new Class<?>[]{Runnable.class},
                    cancelHandler
            );
            try {
                builder.getClass().getMethod("closedOrInvalidResultHandler", Runnable.class).invoke(builder, cancelProxy);
            } catch (NoSuchMethodException ignored) {
                try {
                    builder.getClass().getMethod("closedResultHandler", Runnable.class).invoke(builder, cancelProxy);
                } catch (NoSuchMethodException ignored2) {}
            }

            Object form = builder.getClass().getMethod("build").invoke(builder);
            Class<?> formClass = Class.forName("org.geysermc.cumulus.form.Form");
            apiClass.getMethod("sendForm", UUID.class, formClass).invoke(api, player.getUniqueId(), form);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean openItemModifierForm(
            Plugin plugin,
            Player player,
            org.bukkit.inventory.ItemStack item,
            String description,
            boolean isArmor,
            boolean isTool,
            int totalActive,
            Runnable onCustomEnchants,
            Runnable onVanillaEnchants,
            Runnable onRenameItem,
            Runnable onSetBonus,
            Runnable onRemoveEnchants,
            Runnable onResetEnchants,
            Runnable onBack
    ) {
        if (!isBedrockPlayer(player)) return false;

        try {
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object api = apiClass.getMethod("getInstance").invoke(null);

            Class<?> simpleFormClass = Class.forName("org.geysermc.cumulus.form.SimpleForm");
            Method builderMethod = simpleFormClass.getMethod("builder");
            Object builder = builderMethod.invoke(null);

            builder.getClass().getMethod("title", String.class).invoke(builder, "🛠 EDIT ITEM & ENCHANTS");

            String cleanDesc = description != null
                    ? description.replaceAll("<[^>]*>", "").replaceAll("§[0-9a-fk-orA-FK-OR]", "")
                    : "Pilih opsi pengaturan sihir atau atribut untuk item ini:";
            builder.getClass().getMethod("content", String.class).invoke(builder, cleanDesc);

            java.util.List<Runnable> actions = new java.util.ArrayList<>();

            builder.getClass().getMethod("button", String.class).invoke(builder, "🔮 Kelola Custom Enchants");
            actions.add(onCustomEnchants);

            builder.getClass().getMethod("button", String.class).invoke(builder, "📜 Kelola Vanilla Enchants");
            actions.add(onVanillaEnchants);

            builder.getClass().getMethod("button", String.class).invoke(builder, "🏷 Ubah Nama Item");
            actions.add(onRenameItem);

            if (isArmor || isTool) {
                String setLabel = isArmor ? "🛡 Pengaturan Armor Set Bonus" : "⚔ Pengaturan Tool Set Bonus";
                builder.getClass().getMethod("button", String.class).invoke(builder, setLabel);
                actions.add(onSetBonus);
            }

            if (totalActive > 0) {
                builder.getClass().getMethod("button", String.class).invoke(builder, "✂ Hapus Enchant Tertentu (" + totalActive + ")");
                actions.add(onRemoveEnchants);
            }

            builder.getClass().getMethod("button", String.class).invoke(builder, "🗑 Reset Semua Enchant");
            actions.add(onResetEnchants);

            builder.getClass().getMethod("button", String.class).invoke(builder, "⬅ Selesai & Kembali ke Creator");
            actions.add(onBack);

            java.lang.reflect.InvocationHandler validHandler = (proxy, method, args) -> {
                if (method.getName().equals("accept") || method.getName().equals("handle")) {
                    Object response = args[0];
                    if (response != null) {
                        Method clickedM = response.getClass().getMethod("clickedButtonId");
                        int clickedId = (Integer) clickedM.invoke(response);
                        if (clickedId >= 0 && clickedId < actions.size()) {
                            Runnable r = actions.get(clickedId);
                            if (r != null) {
                                Bukkit.getScheduler().runTask(plugin, r);
                            }
                        }
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
                if (onBack != null) {
                    Bukkit.getScheduler().runTask(plugin, onBack);
                }
                return null;
            };
            Object cancelProxy = java.lang.reflect.Proxy.newProxyInstance(
                    Runnable.class.getClassLoader(),
                    new Class<?>[]{Runnable.class},
                    cancelHandler
            );
            try {
                builder.getClass().getMethod("closedOrInvalidResultHandler", Runnable.class).invoke(builder, cancelProxy);
            } catch (NoSuchMethodException ignored) {
                try {
                    builder.getClass().getMethod("closedResultHandler", Runnable.class).invoke(builder, cancelProxy);
                } catch (NoSuchMethodException ignored2) {}
            }

            Object form = builder.getClass().getMethod("build").invoke(builder);
            Class<?> formClass = Class.forName("org.geysermc.cumulus.form.Form");
            apiClass.getMethod("sendForm", UUID.class, formClass).invoke(api, player.getUniqueId(), form);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
