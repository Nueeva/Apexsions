package com.apexsions.core.gui.input;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Adapter to safely interact with Geyser / Floodgate on Bedrock Edition.
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
                            } catch (Exception ex) {
                                player.sendMessage("§cError processing input: " + ex.getMessage());
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

            // Set closed/cancelled handler
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
                // Older cumulus versions might use closedResultHandler
                try {
                    builder.getClass().getMethod("closedResultHandler", Runnable.class).invoke(builder, cancelProxy);
                } catch (NoSuchMethodException ignored2) {}
            }

            // Build form and send
            Object form = builder.getClass().getMethod("build").invoke(builder);
            Class<?> formClass = Class.forName("org.geysermc.cumulus.form.Form");
            apiClass.getMethod("sendForm", UUID.class, formClass).invoke(api, player.getUniqueId(), form);
            return true;
        } catch (Throwable t) {
            // If reflection fails, fallback to virtual GUI
            return false;
        }
    }

    /**
     * Opens a Bedrock native SimpleForm modal for the Vote experience.
     */
    public static boolean openVoteForm(
            Plugin plugin,
            Player player,
            String directVoteUrl,
            String webPortalUrl
    ) {
        if (!isBedrockPlayer(player)) return false;

        try {
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object api = apiClass.getMethod("getInstance").invoke(null);

            Class<?> simpleFormClass = Class.forName("org.geysermc.cumulus.form.SimpleForm");
            Method builderMethod = simpleFormClass.getMethod("builder");
            Object builder = builderMethod.invoke(null);

            builder.getClass().getMethod("title", String.class).invoke(builder, "APEXSIONS VOTE");

            String content = "§6§lAPEXSIONS BILIK SUARA (BEDROCK)\n"
                    + "§7Dukung kedaulatan peradaban dan klaim imbalan pusaka Anda!\n\n"
                    + "§e§lImbalan Sah Tiap Suara:\n"
                    + " §e• §f3x Kunci Peti Pusaka (Vote Keys)\n"
                    + " §a• §fRp 1.000 Saldo Uang Peradaban\n\n"
                    + "§b§lTautan Resmi Voting:\n"
                    + " §f• Vote Langsung: " + directVoteUrl + "\n"
                    + " §f• Portal Web: " + webPortalUrl + "\n\n"
                    + "§6§lPanduan Suara Cepat:\n"
                    + "§71. Pilih tombol 'Vote di Minecraft-MP' di bawah untuk mendapatkan link di chat.\n"
                    + "§72. Masukkan username: §e" + player.getName() + " §7di halaman vote.\n"
                    + "§a3. Hadiah 3x Keys & Rp 1.000 otomatis masuk tanpa perlu verifikasi manual!";

            builder.getClass().getMethod("content", String.class).invoke(builder, content);

            // Buttons
            builder.getClass().getMethod("button", String.class).invoke(builder, "🗳 Vote di Minecraft-MP");
            builder.getClass().getMethod("button", String.class).invoke(builder, "🌐 Buka Portal Web & Streak");
            builder.getClass().getMethod("button", String.class).invoke(builder, "🎁 Informasi Imbalan");
            builder.getClass().getMethod("button", String.class).invoke(builder, "✕ Tutup");

            Class<?> responseClass = Class.forName("org.geysermc.cumulus.response.SimpleFormResponse");
            java.lang.reflect.InvocationHandler validHandler = (proxy, method, args) -> {
                if (method.getName().equals("accept") || method.getName().equals("handle")) {
                    Object response = args[0];
                    if (response != null) {
                        Method clickedM = response.getClass().getMethod("clickedButtonId");
                        int clickedId = (Integer) clickedM.invoke(response);
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (clickedId == 0) {
                                // Direct Vote Link
                                player.sendMessage("§6[APEXSIONS VOTE] §fTautan voting Minecraft-MP: §b" + directVoteUrl);
                                player.sendMessage("§7Username untuk voting: §e" + player.getName());
                                player.sendMessage("§7Imbalan: §e3x Vote Keys §f+ §aRp 1.000§7.");
                            } else if (clickedId == 1) {
                                // Portal Web Link
                                player.sendMessage("§6[APEXSIONS VOTE] §fPortal Web Bilik Suara: §b" + webPortalUrl);
                                player.sendMessage("§7Cek ranking voter, histori vote, dan streak harian peradaban Anda.");
                            } else if (clickedId == 2) {
                                // Info message
                                player.sendMessage("§6[APEXSIONS VOTE] §eImbalan Suara Sah: §f3x Kunci Peti Vote + Rp 1.000 saldo peradaban.");
                                player.sendMessage("§7Auto-Reward: Hadiah otomatis masuk begitu Anda selesai memberikan suara!");
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

            Object form = builder.getClass().getMethod("build").invoke(builder);
            Class<?> formClass = Class.forName("org.geysermc.cumulus.form.Form");
            apiClass.getMethod("sendForm", UUID.class, formClass).invoke(api, player.getUniqueId(), form);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
