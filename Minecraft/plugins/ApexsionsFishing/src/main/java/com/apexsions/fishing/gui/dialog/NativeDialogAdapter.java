package com.apexsions.fishing.gui.dialog;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

/**
 * Native Dialog Adapter for ApexsionsFishing.
 * Supports NightCore Dialog API, Paper Dialog API, Bungee/Spigot, and Geyser/Floodgate Bedrock Forms.
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
            for (String pName : new String[]{"nightcore", "NightCore", "ExcellentCrates", "excellentcrates"}) {
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

        // 1. Prioritize NightCore Dialogs
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

    private static boolean showNightCoreInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                              Consumer<String> onInput, Runnable onCancel) {
        try {
            Class<?> dialogsClass = findClass("su.nightexpress.nightcore.ui.dialog.Dialogs");
            Class<?> dialogBasesClass = findClass("su.nightexpress.nightcore.ui.dialog.build.DialogBases");
            Class<?> dialogBodiesClass = findClass("su.nightexpress.nightcore.ui.dialog.build.DialogBodies");
            Class<?> dialogInputsClass = findClass("su.nightexpress.nightcore.ui.dialog.build.DialogInputs");
            Class<?> dialogButtonsClass = findClass("su.nightexpress.nightcore.ui.dialog.build.DialogButtons");
            Class<?> dialogTypesClass = findClass("su.nightexpress.nightcore.ui.dialog.build.DialogTypes");
            Class<?> dialogResponseHandlerClass = findClass("su.nightexpress.nightcore.bridge.dialog.response.DialogResponseHandler");
            Class<?> wrappedDialogBuilderClass = findClass("su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog$Builder", "su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog.Builder");

            if (dialogsClass == null || dialogBasesClass == null || dialogBodiesClass == null || dialogInputsClass == null
                    || dialogButtonsClass == null || dialogTypesClass == null || dialogResponseHandlerClass == null || wrappedDialogBuilderClass == null) {
                return false;
            }

            String cleanTitle = (title != null && !title.isBlank()) ? title : "INPUT";
            String cleanPrompt = (prompt != null && !prompt.isBlank()) ? prompt : "Silakan masukkan teks:";
            String initialText = (defaultText != null) ? defaultText.trim() : "";

            Method baseBuilderM = dialogBasesClass.getMethod("builder", String.class);
            Object baseBuilder = baseBuilderM.invoke(null, cleanTitle);

            Method plainBodyM = dialogBodiesClass.getMethod("plainMessage", String.class);
            Object plainBody = plainBodyM.invoke(null, cleanPrompt);
            for (Method m : baseBuilder.getClass().getMethods()) {
                if (m.getName().equals("body") && m.getParameterCount() == 1) {
                    m.invoke(baseBuilder, plainBody);
                    break;
                }
            }

            Method textInputM = dialogInputsClass.getMethod("text", String.class, String.class);
            Object textInput = textInputM.invoke(null, "user_input", initialText);
            for (Method m : baseBuilder.getClass().getMethods()) {
                if (m.getName().equals("input") && m.getParameterCount() == 1) {
                    m.invoke(baseBuilder, textInput);
                    break;
                }
            }

            Object okBtn = null;
            try {
                Method okM = dialogButtonsClass.getMethod("ok");
                okBtn = okM.invoke(null);
            } catch (Throwable t) {
                for (Method m : dialogButtonsClass.getMethods()) {
                    if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class) {
                        okBtn = m.invoke(null, "SELESAI");
                        break;
                    }
                }
            }
            if (okBtn != null) {
                for (Method m : baseBuilder.getClass().getMethods()) {
                    if (m.getName().equals("addButton") || m.getName().equals("button")) {
                        if (m.getParameterCount() == 1) {
                            m.invoke(baseBuilder, okBtn);
                            break;
                        }
                    }
                }
            }

            Method createWrappedM = dialogsClass.getMethod("create", String.class, Consumer.class);
            InvocationHandler handler = (proxy, method, args) -> {
                if (method.getName().equals("handle") || method.getName().equals("accept") || method.getName().equals("onResponse")) {
                    Object response = (args != null && args.length > 0) ? args[0] : null;
                    if (response != null) {
                        try {
                            Method getTextM = response.getClass().getMethod("getText", String.class);
                            String res = (String) getTextM.invoke(response, "user_input");
                            Bukkit.getScheduler().runTask(plugin, () -> onInput.accept(res != null ? res : ""));
                            return null;
                        } catch (Throwable ignored) {}
                    }
                    if (onCancel != null) {
                        Bukkit.getScheduler().runTask(plugin, onCancel);
                    }
                    return null;
                }
                return null;
            };

            Object responseHandlerProxy = Proxy.newProxyInstance(
                    dialogResponseHandlerClass.getClassLoader(),
                    new Class<?>[]{dialogResponseHandlerClass},
                    handler
            );

            Consumer<Object> builderConsumer = builderObj -> {
                try {
                    for (Method m : builderObj.getClass().getMethods()) {
                        if (m.getName().equals("base") && m.getParameterCount() == 1) {
                            Method buildBaseM = baseBuilder.getClass().getMethod("build");
                            Object baseObj = buildBaseM.invoke(baseBuilder);
                            m.invoke(builderObj, baseObj);
                        } else if (m.getName().equals("handler") && m.getParameterCount() == 1) {
                            m.invoke(builderObj, responseHandlerProxy);
                        }
                    }
                } catch (Throwable ignored) {}
            };

            Object wrappedDialog = createWrappedM.invoke(null, "fishing_input_" + System.currentTimeMillis(), builderConsumer);
            if (wrappedDialog != null) {
                Method showM = dialogsClass.getMethod("show", Player.class, wrappedDialog.getClass());
                showM.invoke(null, player, wrappedDialog);
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private static boolean showPaperInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                          Consumer<String> onInput, Runnable onCancel) {
        try {
            Class<?> dialogClass = findClass("io.papermc.paper.dialog.Dialog");
            if (dialogClass == null) return false;
            Method builderM = dialogClass.getMethod("create");
            Object builder = builderM.invoke(null);
            if (builder != null) {
                for (Method m : builder.getClass().getMethods()) {
                    if (m.getName().equals("title") && m.getParameterCount() == 1 && m.getParameterTypes()[0] == Component.class) {
                        m.invoke(builder, mm.deserialize(title));
                    }
                }
                Method showM = builder.getClass().getMethod("show", Player.class);
                showM.invoke(builder, player);
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

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
