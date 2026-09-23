package com.apexsions.fishing.gui.dialog;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Consumer;

/**
 * Adapter to interact with Minecraft 1.21.4+ / 26.2 Native Dialog API for ApexsionsFishing.
 * Supports:
 * 1. NightCore UI Dialog API (su.nightexpress.nightcore.ui.dialog.Dialogs) - Exact engine used by ExcellentCrates & ApexsionsCrates
 * 2. Paper Dialog API (io.papermc.paper.dialog.Dialog)
 * 3. Spigot/Bungee Dialog API (net.md_5.bungee.api.dialog.Dialog)
 */
public class NativeDialogAdapter {

    private static Boolean nightcoreSupported = null;
    private static Boolean paperSupported = null;
    private static Boolean bungeeSupported = null;
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

    public static Class<?> findClass(String... names) {
        ClassLoader[] loaders = new ClassLoader[]{
                NativeDialogAdapter.class.getClassLoader(),
                Thread.currentThread().getContextClassLoader(),
                Bukkit.class.getClassLoader()
        };
        for (String name : names) {
            for (ClassLoader cl : loaders) {
                if (cl == null) continue;
                try {
                    return Class.forName(name, true, cl);
                } catch (Throwable ignored) {}
            }
            try {
                return Class.forName(name);
            } catch (Throwable ignored) {}
            for (String pName : new String[]{"nightcore", "NightCore", "ExcellentCrates", "excellentcrates", "ApexsionsCrates"}) {
                try {
                    Plugin p = Bukkit.getPluginManager().getPlugin(pName);
                    if (p != null) {
                        return Class.forName(name, true, p.getClass().getClassLoader());
                    }
                } catch (Throwable ignored) {}
            }
        }
        return null;
    }

    public static boolean isNightCoreSupported() {
        if (nightcoreSupported == null) {
            nightcoreSupported = findClass("su.nightexpress.nightcore.ui.dialog.Dialogs") != null;
        }
        return nightcoreSupported;
    }

    public static boolean isPaperSupported() {
        if (paperSupported == null) {
            paperSupported = findClass("io.papermc.paper.dialog.Dialog") != null;
        }
        return paperSupported;
    }

    public static boolean isBungeeSupported() {
        if (bungeeSupported == null) {
            bungeeSupported = findClass("net.md_5.bungee.api.dialog.Dialog") != null;
        }
        return bungeeSupported;
    }

    public static boolean isSupported() {
        return isNightCoreSupported() || isPaperSupported() || isBungeeSupported();
    }

    public static boolean showInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                    Consumer<String> onInput, Runnable onCancel) {
        if (!isSupported()) return false;

        // 1. Prioritize NightCore Dialogs (exact mechanism used by ExcellentCrates & ApexsionsCrates)
        if (isNightCoreSupported()) {
            boolean shown = showNightCoreInput(plugin, player, title, prompt, defaultText, onInput, onCancel);
            if (shown) return true;
        }

        // 2. Try Native Paper Dialog API
        if (isPaperSupported()) {
            boolean shown = showPaperInput(plugin, player, title, prompt, defaultText, onInput, onCancel);
            if (shown) return true;
        }

        // 3. Try Bungee/Spigot Dialog API
        if (isBungeeSupported()) {
            return showBungeeInput(plugin, player, title, prompt, defaultText, onInput, onCancel);
        }

        return false;
    }

    // ==========================================
    // 1. NIGHTCORE DIALOG IMPLEMENTATION
    // ==========================================
    private static boolean showNightCoreInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                              Consumer<String> onInput, Runnable onCancel) {
        try {
            Class<?> dialogsClass = findClass("su.nightexpress.nightcore.ui.dialog.Dialogs");
            Class<?> dialogBasesClass = findClass("su.nightexpress.nightcore.ui.dialog.build.DialogBases");
            Class<?> dialogBodiesClass = findClass("su.nightexpress.nightcore.ui.dialog.build.DialogBodies");
            Class<?> dialogInputsClass = findClass("su.nightexpress.nightcore.ui.dialog.build.DialogInputs");
            Class<?> dialogButtonsClass = findClass("su.nightexpress.nightcore.ui.dialog.build.DialogButtons");
            Class<?> dialogTypesClass = findClass("su.nightexpress.nightcore.ui.dialog.build.DialogTypes");
            Class<?> dialogActionsClass = findClass("su.nightexpress.nightcore.ui.dialog.build.DialogActions");
            Class<?> dialogResponseHandlerClass = findClass("su.nightexpress.nightcore.bridge.dialog.response.DialogResponseHandler");
            Class<?> wrappedDialogBuilderClass = findClass("su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog$Builder", "su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog.Builder");

            if (dialogsClass == null || dialogBasesClass == null || dialogBodiesClass == null || dialogInputsClass == null
                    || dialogButtonsClass == null || dialogTypesClass == null || dialogResponseHandlerClass == null || wrappedDialogBuilderClass == null) {
                return false;
            }

            String cleanTitle = (title != null && !title.isBlank()) ? title : "INPUT";
            String cleanPrompt = (prompt != null && !prompt.isBlank()) ? prompt : "Silakan masukkan teks:";
            String initialText = (defaultText != null) ? defaultText.trim() : "";

            // 1. Base Builder
            Method baseBuilderM = null;
            try {
                baseBuilderM = dialogBasesClass.getMethod("builder", String.class);
            } catch (Throwable ignored) {
                for (Method m : dialogBasesClass.getMethods()) {
                    if (m.getName().equals("builder") && m.getParameterCount() == 1) {
                        baseBuilderM = m;
                        break;
                    }
                }
            }
            if (baseBuilderM == null) return false;
            Object baseBuilder = baseBuilderM.invoke(null, cleanTitle);

            // 2. Body: DialogBodies.plainMessage(cleanPrompt)
            Method plainBodyM = null;
            for (Method m : dialogBodiesClass.getMethods()) {
                if (m.getName().equals("plainMessage") && m.getParameterCount() == 1) {
                    plainBodyM = m;
                    break;
                }
            }
            if (plainBodyM != null) {
                Object plainBody = plainBodyM.invoke(null, cleanPrompt);
                for (Method m : baseBuilder.getClass().getMethods()) {
                    if (m.getName().equals("body")) {
                        if (m.getParameterCount() == 1 && List.class.isAssignableFrom(m.getParameterTypes()[0])) {
                            m.invoke(baseBuilder, List.of(plainBody));
                            break;
                        } else if (m.getParameterCount() == 1 && m.getParameterTypes()[0].isArray()) {
                            Object arr = java.lang.reflect.Array.newInstance(m.getParameterTypes()[0].getComponentType(), 1);
                            java.lang.reflect.Array.set(arr, 0, plainBody);
                            m.invoke(baseBuilder, arr);
                            break;
                        }
                    }
                }
            }

            // 3. TextInput: DialogInputs.text("input_key", "").labelVisible(false).build()
            Object textBuilder = null;
            for (Method m : dialogInputsClass.getMethods()) {
                if (m.getName().equals("text") && m.getParameterCount() == 2) {
                    try {
                        textBuilder = m.invoke(null, "input_key", cleanPrompt);
                        break;
                    } catch (Throwable ignored) {}
                }
            }
            if (textBuilder == null) {
                for (Method m : dialogInputsClass.getMethods()) {
                    if (m.getName().equals("text") && m.getParameterCount() == 1) {
                        try {
                            textBuilder = m.invoke(null, "input_key");
                            break;
                        } catch (Throwable ignored) {}
                    }
                }
            }

            if (textBuilder != null) {
                if (!initialText.isEmpty()) {
                    for (Method m : textBuilder.getClass().getMethods()) {
                        if (m.getName().equals("initial") && m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class) {
                            m.invoke(textBuilder, initialText);
                            break;
                        }
                    }
                }
                for (Method m : textBuilder.getClass().getMethods()) {
                    if (m.getName().equals("labelVisible") && m.getParameterCount() == 1 && m.getParameterTypes()[0] == boolean.class) {
                        m.invoke(textBuilder, false);
                        break;
                    }
                }
                for (Method m : textBuilder.getClass().getMethods()) {
                    if (m.getName().equals("maxLength") && m.getParameterCount() == 1) {
                        m.invoke(textBuilder, 300);
                        break;
                    }
                }
                Object textInput = textBuilder.getClass().getMethod("build").invoke(textBuilder);

                for (Method m : baseBuilder.getClass().getMethods()) {
                    if (m.getName().equals("inputs")) {
                        if (m.getParameterCount() == 1 && List.class.isAssignableFrom(m.getParameterTypes()[0])) {
                            m.invoke(baseBuilder, List.of(textInput));
                            break;
                        } else if (m.getParameterCount() == 1 && m.getParameterTypes()[0].isArray()) {
                            Object arr = java.lang.reflect.Array.newInstance(m.getParameterTypes()[0].getComponentType(), 1);
                            java.lang.reflect.Array.set(arr, 0, textInput);
                            m.invoke(baseBuilder, arr);
                            break;
                        }
                    }
                }
            }

            Object base = baseBuilder.getClass().getMethod("build").invoke(baseBuilder);

            // 4. Buttons: DialogButtons.ok(), DialogButtons.back()
            Method okButtonM = dialogButtonsClass.getMethod("ok");
            Object okButton = okButtonM.invoke(null);

            Method backButtonM = dialogButtonsClass.getMethod("back");
            Object backButton = backButtonM.invoke(null);

            // 5. Type: DialogTypes.multiAction(okButton).exitAction(backButton).build()
            Object multiActionBuilder = null;
            for (Method m : dialogTypesClass.getMethods()) {
                if (m.getName().equals("multiAction")) {
                    if (m.getParameterTypes()[0].isArray()) {
                        Object arr = java.lang.reflect.Array.newInstance(m.getParameterTypes()[0].getComponentType(), 1);
                        java.lang.reflect.Array.set(arr, 0, okButton);
                        multiActionBuilder = m.invoke(null, arr);
                        break;
                    } else if (List.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        multiActionBuilder = m.invoke(null, List.of(okButton));
                        break;
                    }
                }
            }

            if (multiActionBuilder == null) return false;

            for (Method m : multiActionBuilder.getClass().getMethods()) {
                if (m.getName().equals("exitAction") && m.getParameterCount() == 1) {
                    m.invoke(multiActionBuilder, backButton);
                    break;
                }
            }

            Object dialogType = multiActionBuilder.getClass().getMethod("build").invoke(multiActionBuilder);

            // 6. Build WrappedDialog
            Object dialogBuilder = wrappedDialogBuilderClass.getDeclaredConstructor().newInstance();
            for (Method m : dialogBuilder.getClass().getMethods()) {
                if (m.getName().equals("base") && m.getParameterCount() == 1) {
                    m.invoke(dialogBuilder, base);
                    break;
                }
            }
            for (Method m : dialogBuilder.getClass().getMethods()) {
                if (m.getName().equals("type") && m.getParameterCount() == 1) {
                    m.invoke(dialogBuilder, dialogType);
                    break;
                }
            }

            // 7. Response Handler for "ok" and "back"
            InvocationHandler okHandler = (proxy, method, args) -> {
                if (method.getName().equals("handle") || method.getName().equals("accept") || method.getName().equals("onResponse")) {
                    Object nbtHolder = (args != null && args.length > 2) ? args[2] : null;
                    if (nbtHolder == null && args != null && args.length > 0) {
                        nbtHolder = args[args.length - 1];
                    }
                    String extracted = extractNightCoreText(nbtHolder, "input_key");
                    final String res = (extracted != null) ? extracted.trim() : "";
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        try {
                            onInput.accept(res);
                        } catch (Throwable ex) {
                            player.sendMessage("§cError: " + ex.getMessage());
                        }
                    });
                }
                return null;
            };

            Object okProxy = Proxy.newProxyInstance(dialogResponseHandlerClass.getClassLoader(), new Class<?>[]{dialogResponseHandlerClass}, okHandler);

            // Register ok handler for string and DialogActions.OK
            for (Method m : dialogBuilder.getClass().getMethods()) {
                if (m.getName().equals("handleResponse") && m.getParameterCount() == 2) {
                    Class<?> p0 = m.getParameterTypes()[0];
                    if (p0 == String.class) {
                        try { m.invoke(dialogBuilder, "ok", okProxy); } catch (Throwable ignored) {}
                        try { m.invoke(dialogBuilder, "confirm", okProxy); } catch (Throwable ignored) {}
                        try { m.invoke(dialogBuilder, "apply", okProxy); } catch (Throwable ignored) {}
                    } else if (dialogActionsClass != null) {
                        try {
                            Field okF = dialogActionsClass.getField("OK");
                            m.invoke(dialogBuilder, okF.get(null), okProxy);
                        } catch (Throwable ignored) {}
                    }
                }
            }

            if (onCancel != null) {
                InvocationHandler backHandler = (proxy, method, args) -> {
                    if (method.getName().equals("handle") || method.getName().equals("accept") || method.getName().equals("onResponse")) {
                        Bukkit.getScheduler().runTask(plugin, onCancel);
                    }
                    return null;
                };
                Object backProxy = Proxy.newProxyInstance(dialogResponseHandlerClass.getClassLoader(), new Class<?>[]{dialogResponseHandlerClass}, backHandler);

                for (Method m : dialogBuilder.getClass().getMethods()) {
                    if (m.getName().equals("handleResponse") && m.getParameterCount() == 2) {
                        Class<?> p0 = m.getParameterTypes()[0];
                        if (p0 == String.class) {
                            try { m.invoke(dialogBuilder, "back", backProxy); } catch (Throwable ignored) {}
                            try { m.invoke(dialogBuilder, "cancel", backProxy); } catch (Throwable ignored) {}
                        } else if (dialogActionsClass != null) {
                            try {
                                Field backF = dialogActionsClass.getField("BACK");
                                m.invoke(dialogBuilder, backF.get(null), backProxy);
                            } catch (Throwable ignored) {}
                        }
                    }
                }
            }

            Object wrappedDialog = dialogBuilder.getClass().getMethod("build").invoke(dialogBuilder);

            // 8. Dialogs.showDialog(player, wrappedDialog, onCancel)
            Method showDialogM = null;
            for (Method m : dialogsClass.getMethods()) {
                if (m.getName().equals("showDialog") && m.getParameterCount() == 3) {
                    showDialogM = m;
                    break;
                }
            }
            if (showDialogM != null) {
                showDialogM.invoke(null, player, wrappedDialog, onCancel);
            } else {
                for (Method m : dialogsClass.getMethods()) {
                    if (m.getName().equals("showDialog") && m.getParameterCount() == 2) {
                        m.invoke(null, player, wrappedDialog);
                        break;
                    }
                }
            }

            return true;
        } catch (Throwable t) {
            plugin.getLogger().warning("[NativeDialogAdapter] NightCore dialog unavailable or error: " + t.getMessage());
            return false;
        }
    }

    private static String extractNightCoreText(Object nbtHolder, String key) {
        if (nbtHolder == null) return "";

        // 1. Try getText(key, def)
        try {
            Method getText2 = nbtHolder.getClass().getMethod("getText", String.class, String.class);
            Object res = getText2.invoke(nbtHolder, key, "");
            if (res instanceof String s && !s.isEmpty()) {
                return s;
            }
        } catch (Throwable ignored) {}

        // 2. Try getText(key) returning Optional<String> or String
        try {
            Method getText1 = nbtHolder.getClass().getMethod("getText", String.class);
            Object opt = getText1.invoke(nbtHolder, key);
            if (opt instanceof java.util.Optional<?> o && o.isPresent()) {
                return (String) o.get();
            }
            if (opt instanceof String s && !s.isEmpty()) {
                return s;
            }
        } catch (Throwable ignored) {}

        // 3. Try getText("id")
        try {
            Method getText1 = nbtHolder.getClass().getMethod("getText", String.class);
            Object opt = getText1.invoke(nbtHolder, "id");
            if (opt instanceof java.util.Optional<?> o && o.isPresent()) {
                return (String) o.get();
            }
            if (opt instanceof String s && !s.isEmpty()) {
                return s;
            }
        } catch (Throwable ignored) {}

        // 4. Try JSON payload
        try {
            Method payloadM = nbtHolder.getClass().getMethod("payload");
            Object payload = payloadM.invoke(nbtHolder);
            if (payload instanceof com.google.gson.JsonObject json) {
                if (json.has(key)) {
                    return json.get(key).getAsString();
                } else if (json.has("id")) {
                    return json.get("id").getAsString();
                } else if (!json.entrySet().isEmpty()) {
                    return json.entrySet().iterator().next().getValue().getAsString();
                }
            }
        } catch (Throwable ignored) {}

        return "";
    }

    // ==========================================
    // 2. PAPER NATIVE DIALOG IMPLEMENTATION
    // ==========================================
    private static boolean showPaperInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                          Consumer<String> onInput, Runnable onCancel) {
        try {
            Class<?> dialogClass = findClass("io.papermc.paper.dialog.Dialog");
            Class<?> dialogBaseClass = findClass("io.papermc.paper.registry.data.dialog.DialogBase", "io.papermc.paper.dialog.DialogBase");
            Class<?> dialogBodyClass = findClass("io.papermc.paper.registry.data.dialog.body.DialogBody", "io.papermc.paper.dialog.DialogBody");
            Class<?> dialogInputClass = findClass("io.papermc.paper.registry.data.dialog.input.DialogInput", "io.papermc.paper.dialog.DialogInput");
            Class<?> textDialogInputClass = findClass("io.papermc.paper.registry.data.dialog.input.TextDialogInput", "io.papermc.paper.dialog.input.TextDialogInput");

            if (dialogClass == null || dialogBaseClass == null || dialogBodyClass == null || dialogInputClass == null) {
                return false;
            }

            Component titleComp = (title != null && !title.isBlank())
                    ? (title.contains("<") || title.contains("&") ? mm.deserialize(title) : Component.text(title))
                    : mm.deserialize("<gold><b>INPUT</b></gold>");

            Component promptComp = (prompt != null && !prompt.isBlank())
                    ? (prompt.contains("<") || prompt.contains("&") ? mm.deserialize(prompt) : Component.text(prompt))
                    : mm.deserialize("<yellow>Masukkan teks:</yellow>");

            Method baseBuilderMethod = dialogBaseClass.getMethod("builder", Component.class);
            Object baseBuilder = baseBuilderMethod.invoke(null, titleComp);

            Method plainMessageMethod = dialogBodyClass.getMethod("plainMessage", Component.class);
            Object bodyItem = plainMessageMethod.invoke(null, promptComp);
            baseBuilder.getClass().getMethod("body", List.class).invoke(baseBuilder, List.of(bodyItem));

            Object textInput = createTextInput(dialogInputClass, textDialogInputClass, "input_key", Component.text("Input"), defaultText);
            if (textInput != null) {
                baseBuilder.getClass().getMethod("inputs", List.class).invoke(baseBuilder, List.of(textInput));
            }

            Object dialogBase = baseBuilder.getClass().getMethod("build").invoke(baseBuilder);

            Method createDialogMethod = dialogClass.getMethod("create", dialogBaseClass);
            Object dialogInstance = createDialogMethod.invoke(null, dialogBase);

            Method showMethod = dialogClass.getMethod("show", Player.class);
            showMethod.invoke(dialogInstance, player);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Object createTextInput(Class<?> inputClass, Class<?> textInputClass, String key, Component label, String initial) {
        try {
            if (textInputClass != null) {
                Method builderM = textInputClass.getMethod("builder", String.class, Component.class);
                Object b = builderM.invoke(null, key, label);
                if (initial != null && !initial.isBlank()) {
                    b.getClass().getMethod("initial", String.class).invoke(b, initial);
                }
                return b.getClass().getMethod("build").invoke(b);
            }
            if (inputClass != null) {
                Method textM = inputClass.getMethod("text", String.class, Component.class);
                return textM.invoke(null, key, label);
            }
        } catch (Throwable ignored) {}
        return null;
    }

    // ==========================================
    // 3. BUNGEE / SPIGOT DIALOG IMPLEMENTATION
    // ==========================================
    private static boolean showBungeeInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                           Consumer<String> onInput, Runnable onCancel) {
        try {
            Class<?> dialogClass = findClass("net.md_5.bungee.api.dialog.Dialog");
            if (dialogClass == null) return false;
            Method createM = dialogClass.getMethod("create");
            Object dialog = createM.invoke(null);
            if (dialog != null) {
                Method showM = player.getClass().getMethod("showDialog", dialogClass);
                showM.invoke(player, dialog);
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
