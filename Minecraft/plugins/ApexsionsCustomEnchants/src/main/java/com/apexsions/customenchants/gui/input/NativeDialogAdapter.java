package com.apexsions.customenchants.gui.input;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Adapter to interact with Minecraft 1.21.4+ / 26.2 Native Dialog API.
 * Supports:
 * 1. NightCore UI Dialog API (su.nightexpress.nightcore.ui.dialog.Dialogs) - Exact engine used by ExcellentCrates
 * 2. Paper API (io.papermc.paper.dialog.Dialog)
 * 3. Spigot/Bungee API (net.md_5.bungee.api.dialog.Dialog)
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

        // 1. Prioritize NightCore Dialogs (exact mechanism used by ExcellentCrates)
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
    // 1. NIGHTCORE DIALOG IMPLEMENTATION (EXCELLENTCRATES)
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
            Method baseBuilderM = dialogBasesClass.getMethod("builder", String.class);
            Object baseBuilder = baseBuilderM.invoke(null, cleanTitle);

            // 2. Body: DialogBodies.plainMessage(cleanPrompt)
            Method plainBodyM = dialogBodiesClass.getMethod("plainMessage", String.class);
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

            // 3. TextInput: DialogInputs.text("input_key", "").labelVisible(false).build()
            Method textInputM = dialogInputsClass.getMethod("text", String.class, String.class);
            Object textBuilder = textInputM.invoke(null, "input_key", "");
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
                if (method.getName().equals("handle")) {
                    Object nbtHolder = (args != null && args.length > 2) ? args[2] : null;
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
            Method handleResponseM = dialogBuilder.getClass().getMethod("handleResponse", String.class, dialogResponseHandlerClass);
            handleResponseM.invoke(dialogBuilder, "ok", okProxy);
            handleResponseM.invoke(dialogBuilder, "confirm", okProxy);
            handleResponseM.invoke(dialogBuilder, "apply", okProxy);

            if (onCancel != null) {
                InvocationHandler backHandler = (proxy, method, args) -> {
                    if (method.getName().equals("handle")) {
                        Bukkit.getScheduler().runTask(plugin, onCancel);
                    }
                    return null;
                };
                Object backProxy = Proxy.newProxyInstance(dialogResponseHandlerClass.getClassLoader(), new Class<?>[]{dialogResponseHandlerClass}, backHandler);
                handleResponseM.invoke(dialogBuilder, "back", backProxy);
                handleResponseM.invoke(dialogBuilder, "cancel", backProxy);
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
                dialogsClass.getMethod("showDialog", Player.class, wrappedDialog.getClass()).invoke(null, player, wrappedDialog);
            }

            return true;
        } catch (Throwable t) {
            plugin.getLogger().warning("[NativeDialogAdapter] NightCore dialog unavailable or error: " + t.getMessage());
            return false;
        }
    }

    private static String extractNightCoreText(Object nbtHolder, String key) {
        if (nbtHolder == null) return "";
        try {
            Method getTextM = nbtHolder.getClass().getMethod("getText", String.class);
            Object opt = getTextM.invoke(nbtHolder, key);
            if (opt instanceof java.util.Optional<?> o && o.isPresent()) {
                return (String) o.get();
            }
            opt = getTextM.invoke(nbtHolder, "id");
            if (opt instanceof java.util.Optional<?> o2 && o2.isPresent()) {
                return (String) o2.get();
            }
        } catch (Throwable ignored) {}

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
            Class<?> dialogTypeClass = findClass("io.papermc.paper.registry.data.dialog.type.DialogType", "io.papermc.paper.dialog.DialogType");
            Class<?> actionButtonClass = findClass("io.papermc.paper.registry.data.dialog.action.ActionButton", "io.papermc.paper.dialog.ActionButton");
            Class<?> dialogActionClass = findClass("io.papermc.paper.registry.data.dialog.action.DialogAction", "io.papermc.paper.dialog.DialogAction");
            Class<?> dialogActionCallbackClass = findClass("io.papermc.paper.registry.data.dialog.action.DialogActionCallback", "io.papermc.paper.dialog.DialogActionCallback");
            Class<?> clickCallbackOptionsClass = findClass("net.kyori.adventure.text.event.ClickCallback$Options", "net.kyori.adventure.text.event.ClickCallback.Options");

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

            Object clickOptions = null;
            if (clickCallbackOptionsClass != null) {
                try {
                    Method builderM = clickCallbackOptionsClass.getMethod("builder");
                    Object b = builderM.invoke(null);
                    clickOptions = b.getClass().getMethod("build").invoke(b);
                } catch (Throwable ignored) {}
            }

            InvocationHandler okHandler = (proxy, method, args) -> {
                if (method.getDeclaringClass() == Object.class) {
                    if (method.getName().equals("equals")) return proxy == (args != null && args.length > 0 ? args[0] : null);
                    if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                    if (method.getName().equals("toString")) return "DialogOkCallbackProxy@" + Integer.toHexString(System.identityHashCode(proxy));
                    return null;
                }
                Object responseView = (args != null && args.length > 0) ? args[0] : null;
                String textVal = extractTextValue(responseView, "input_key");
                final String finalResult = textVal != null ? textVal.trim() : "";
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        onInput.accept(finalResult);
                    } catch (Throwable ex) {
                        player.sendMessage("§cError: " + ex.getMessage());
                    }
                });
                return null;
            };

            Class<?> callbackInterface = dialogActionCallbackClass != null ? dialogActionCallbackClass : java.util.function.BiConsumer.class;
            Object okCallback = Proxy.newProxyInstance(callbackInterface.getClassLoader(), new Class<?>[]{callbackInterface}, okHandler);

            Object okAction = createDialogAction(dialogActionClass, okCallback, clickOptions);

            InvocationHandler backHandler = (proxy, method, args) -> {
                if (method.getDeclaringClass() == Object.class) {
                    if (method.getName().equals("equals")) return proxy == (args != null && args.length > 0 ? args[0] : null);
                    if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                    if (method.getName().equals("toString")) return "DialogBackCallbackProxy@" + Integer.toHexString(System.identityHashCode(proxy));
                    return null;
                }
                if (onCancel != null) {
                    Bukkit.getScheduler().runTask(plugin, onCancel);
                }
                return null;
            };
            Object backCallback = Proxy.newProxyInstance(callbackInterface.getClassLoader(), new Class<?>[]{callbackInterface}, backHandler);

            Object backAction = createDialogAction(dialogActionClass, backCallback, clickOptions);

            Component okComp = mm.deserialize("<green>✔</green> <white><b>OK</b></white>");
            Component backComp = mm.deserialize("<yellow>⬅</yellow> <white><b>BACK</b></white>");

            Object okButton = createActionButton(actionButtonClass, dialogActionClass, okComp, okAction);
            Object backButton = createActionButton(actionButtonClass, dialogActionClass, backComp, backAction);

            Object dialogType = createDialogType(dialogTypeClass, okButton, backButton);

            final Object finalBase = dialogBase;
            final Object finalType = dialogType;
            final Class<?> finalBaseClass = dialogBaseClass;
            final Class<?> finalTypeClass = dialogTypeClass;

            Object dialogInstance = null;

            // Option A: Dialog.create(Consumer<Dialog.Builder>)
            try {
                Method createDialogMethod = dialogClass.getMethod("create", Consumer.class);
                Consumer<Object> dialogBuilderConsumer = builder -> {
                    try {
                        Object empty = builder.getClass().getMethod("empty").invoke(builder);
                        empty.getClass().getMethod("base", finalBaseClass).invoke(empty, finalBase);
                        empty.getClass().getMethod("type", finalTypeClass).invoke(empty, finalType);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                };
                dialogInstance = createDialogMethod.invoke(null, dialogBuilderConsumer);
            } catch (Throwable ignored) {}

            // Option B: Dialog.create(DialogBase, DialogType)
            if (dialogInstance == null) {
                for (Method m : dialogClass.getMethods()) {
                    if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) && (m.getName().equals("create") || m.getName().equals("of"))) {
                        if (m.getParameterCount() == 2) {
                            try {
                                dialogInstance = m.invoke(null, finalBase, finalType);
                                if (dialogInstance != null) break;
                            } catch (Throwable ignored) {}
                        }
                    }
                }
            }

            // Option C: Dialog.builder()
            if (dialogInstance == null) {
                try {
                    Method builderM = dialogClass.getMethod("builder");
                    Object b = builderM.invoke(null);
                    b.getClass().getMethod("base", finalBaseClass).invoke(b, finalBase);
                    b.getClass().getMethod("type", finalTypeClass).invoke(b, finalType);
                    dialogInstance = b.getClass().getMethod("build").invoke(b);
                } catch (Throwable ignored) {}
            }

            if (dialogInstance == null) {
                return false;
            }

            // Show on player
            for (Method m : player.getClass().getMethods()) {
                if (m.getName().equals("showDialog") && m.getParameterCount() == 1) {
                    try {
                        m.invoke(player, dialogInstance);
                        return true;
                    } catch (Throwable ignored) {}
                }
            }

            for (Class<?> iface : player.getClass().getInterfaces()) {
                for (Method m : iface.getMethods()) {
                    if (m.getName().equals("showDialog") && m.getParameterCount() == 1) {
                        try {
                            m.invoke(player, dialogInstance);
                            return true;
                        } catch (Throwable ignored) {}
                    }
                }
            }

            return false;
        } catch (Throwable t) {
            plugin.getLogger().warning("[NativeDialogAdapter] Paper Dialog unavailable or error: " + t.getMessage());
            return false;
        }
    }

    private static Object createTextInput(Class<?> dialogInputClass, Class<?> textDialogInputClass,
                                          String key, Component label, String defaultText) {
        if (textDialogInputClass != null) {
            for (Method m : textDialogInputClass.getMethods()) {
                if (java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
                    if (m.getName().equals("builder") || m.getName().equals("text") || m.getName().equals("create") || m.getName().equals("of")) {
                        try {
                            Object[] args = buildDynamicArguments(m, key, label, defaultText);
                            Object res = m.invoke(null, args);
                            if (res != null) {
                                Object built = configureAndBuildTextInput(res, defaultText);
                                if (built != null) return built;
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            }
        }

        if (dialogInputClass != null) {
            for (Method m : dialogInputClass.getMethods()) {
                if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) && m.getName().equals("text")) {
                    if (m.getParameterCount() == 2 || m.getParameterCount() == 1) {
                        try {
                            Object[] args = buildDynamicArguments(m, key, label, defaultText);
                            Object res = m.invoke(null, args);
                            if (res != null) {
                                Object built = configureAndBuildTextInput(res, defaultText);
                                if (built != null) return built;
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            }

            for (Method m : dialogInputClass.getMethods()) {
                if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) && (m.getName().equals("text") || m.getName().equals("create") || m.getName().equals("of"))) {
                    try {
                        Object[] args = buildDynamicArguments(m, key, label, defaultText);
                        Object res = m.invoke(null, args);
                        if (res != null) {
                            Object built = configureAndBuildTextInput(res, defaultText);
                            if (built != null) return built;
                        }
                    } catch (Throwable ignored) {}
                }
            }
        }

        Class<?> targetClass = (textDialogInputClass != null) ? textDialogInputClass : dialogInputClass;
        if (targetClass != null) {
            for (java.lang.reflect.Constructor<?> c : targetClass.getConstructors()) {
                try {
                    Object[] args = buildDynamicConstructorArguments(c, key, label, defaultText);
                    Object res = c.newInstance(args);
                    if (res != null) return res;
                } catch (Throwable ignored) {}
            }
        }

        return null;
    }

    private static Object[] buildDynamicArguments(Method m, String key, Component label, String defaultText) {
        return fillParameters(m.getParameterTypes(), key, label, defaultText);
    }

    private static Object[] buildDynamicConstructorArguments(java.lang.reflect.Constructor<?> c, String key, Component label, String defaultText) {
        return fillParameters(c.getParameterTypes(), key, label, defaultText);
    }

    private static Object[] fillParameters(Class<?>[] paramTypes, String key, Component label, String defaultText) {
        Object[] args = new Object[paramTypes.length];
        int stringCount = 0;
        int intCount = 0;

        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> pt = paramTypes[i];
            if (pt == String.class) {
                if (stringCount == 0) {
                    args[i] = key;
                } else {
                    args[i] = (defaultText != null ? defaultText.trim() : "");
                }
                stringCount++;
            } else if (Component.class.isAssignableFrom(pt) || pt.getName().contains("Component")) {
                args[i] = (label != null ? label : Component.text("Input"));
            } else if (pt == int.class || pt == Integer.class) {
                if (intCount == 0) {
                    args[i] = 200; // width
                } else {
                    args[i] = 256; // max_length
                }
                intCount++;
            } else if (pt == boolean.class || pt == Boolean.class) {
                args[i] = false;
            } else if (pt.isEnum()) {
                Object[] constants = pt.getEnumConstants();
                args[i] = (constants != null && constants.length > 0) ? constants[0] : null;
            } else {
                args[i] = null;
            }
        }
        return args;
    }

    private static Object configureAndBuildTextInput(Object target, String defaultText) {
        if (target == null) return null;
        for (Method m : target.getClass().getMethods()) {
            if (m.getName().equals("labelVisible") && m.getParameterCount() == 1 && m.getParameterTypes()[0] == boolean.class) {
                try { m.invoke(target, false); } catch (Throwable ignored) {}
            } else if (m.getName().equals("width") && m.getParameterCount() == 1 && (m.getParameterTypes()[0] == int.class || m.getParameterTypes()[0] == Integer.class)) {
                try { m.invoke(target, 200); } catch (Throwable ignored) {}
            } else if (m.getName().equals("maxLength") && m.getParameterCount() == 1 && (m.getParameterTypes()[0] == int.class || m.getParameterTypes()[0] == Integer.class)) {
                try { m.invoke(target, 256); } catch (Throwable ignored) {}
            } else if ((m.getName().equals("initial") || m.getName().equals("defaultValue") || m.getName().equals("value") || m.getName().equals("initialValue"))
                    && m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class) {
                if (defaultText != null && !defaultText.isBlank()) {
                    try { m.invoke(target, defaultText.trim()); } catch (Throwable ignored) {}
                }
            }
        }
        try {
            Method buildM = target.getClass().getMethod("build");
            return buildM.invoke(target);
        } catch (Throwable ignored) {
            return target;
        }
    }

    private static Object createDialogAction(Class<?> dialogActionClass, Object callback, Object clickOptions) {
        if (dialogActionClass == null || callback == null) return null;
        for (Method m : dialogActionClass.getMethods()) {
            if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) &&
                    (m.getName().equals("customClick") || m.getName().equals("click") || m.getName().equals("of") || m.getName().equals("action"))) {
                if (m.getParameterCount() == 2 && clickOptions != null) {
                    try {
                        Object res = m.invoke(null, callback, clickOptions);
                        if (res != null) return res;
                    } catch (Throwable ignored) {}
                } else if (m.getParameterCount() == 1) {
                    try {
                        Object res = m.invoke(null, callback);
                        if (res != null) return res;
                    } catch (Throwable ignored) {}
                }
            }
        }
        return null;
    }

    private static Object createActionButton(Class<?> actionButtonClass, Class<?> dialogActionClass, Component label, Object action) {
        if (actionButtonClass == null) return null;

        for (Method m : actionButtonClass.getMethods()) {
            if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) && (m.getName().equals("create") || m.getName().equals("of"))) {
                Class<?>[] pTypes = m.getParameterTypes();
                Object[] args = new Object[pTypes.length];
                for (int i = 0; i < pTypes.length; i++) {
                    Class<?> pt = pTypes[i];
                    if (Component.class.isAssignableFrom(pt) || pt.getName().contains("Component")) {
                        args[i] = label;
                    } else if (dialogActionClass != null && dialogActionClass.isAssignableFrom(pt)) {
                        args[i] = action;
                    } else if (pt == int.class || pt == Integer.class) {
                        args[i] = 200;
                    } else if (pt == Object.class) {
                        args[i] = action;
                    } else {
                        args[i] = null;
                    }
                }
                try {
                    Object res = m.invoke(null, args);
                    if (res != null) return res;
                } catch (Throwable ignored) {}
            }
        }

        for (Method m : actionButtonClass.getMethods()) {
            if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) && m.getName().equals("builder")) {
                try {
                    Object b = (m.getParameterCount() == 1) ? m.invoke(null, label) : m.invoke(null);
                    if (b != null) {
                        for (Method bm : b.getClass().getMethods()) {
                            if ((bm.getName().equals("action") || bm.getName().equals("dialogAction")) && bm.getParameterCount() == 1) {
                                try { bm.invoke(b, action); } catch (Throwable ignored) {}
                            } else if ((bm.getName().equals("label") || bm.getName().equals("text")) && bm.getParameterCount() == 1 && Component.class.isAssignableFrom(bm.getParameterTypes()[0])) {
                                try { bm.invoke(b, label); } catch (Throwable ignored) {}
                            } else if (bm.getName().equals("width") && bm.getParameterCount() == 1 && (bm.getParameterTypes()[0] == int.class || bm.getParameterTypes()[0] == Integer.class)) {
                                try { bm.invoke(b, 200); } catch (Throwable ignored) {}
                            }
                        }
                        Method buildM = b.getClass().getMethod("build");
                        return buildM.invoke(b);
                    }
                } catch (Throwable ignored) {}
            }
        }

        return null;
    }

    private static Object createDialogType(Class<?> dialogTypeClass, Object okButton, Object backButton) {
        if (dialogTypeClass == null) return null;

        for (Method m : dialogTypeClass.getMethods()) {
            if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) && m.getName().equals("confirmation")) {
                if (m.getParameterCount() == 2 && backButton != null) {
                    try {
                        Object res = m.invoke(null, okButton, backButton);
                        if (res != null) return res;
                    } catch (Throwable ignored) {}
                } else if (m.getParameterCount() == 1) {
                    try {
                        Object res = m.invoke(null, okButton);
                        if (res != null) return res;
                    } catch (Throwable ignored) {}
                }
            }
        }

        for (Method m : dialogTypeClass.getMethods()) {
            if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) && m.getName().equals("notice")) {
                if (m.getParameterCount() == 1) {
                    try {
                        Object res = m.invoke(null, okButton);
                        if (res != null) return res;
                    } catch (Throwable ignored) {}
                }
            }
        }

        return null;
    }

    private static String extractTextValue(Object responseView, String expectedKey) {
        if (responseView == null) return "";
        try {
            Method m = responseView.getClass().getMethod("getText", String.class);
            Object res = m.invoke(responseView, expectedKey);
            if (res != null) return res.toString();
        } catch (Throwable ignored) {}

        try {
            Method m = responseView.getClass().getMethod("getString", String.class);
            Object res = m.invoke(responseView, expectedKey);
            if (res != null) return res.toString();
        } catch (Throwable ignored) {}

        try {
            Method m = responseView.getClass().getMethod("get", String.class);
            Object res = m.invoke(responseView, expectedKey);
            if (res != null) return res.toString();
        } catch (Throwable ignored) {}

        try {
            Method m = responseView.getClass().getMethod("text", String.class);
            Object res = m.invoke(responseView, expectedKey);
            if (res != null) return res.toString();
        } catch (Throwable ignored) {}

        for (Method m : responseView.getClass().getMethods()) {
            if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class && m.getReturnType() != void.class) {
                try {
                    Object res = m.invoke(responseView, expectedKey);
                    if (res != null) return res.toString();
                } catch (Throwable ignored) {}
            }
        }

        for (Method m : responseView.getClass().getMethods()) {
            if ((m.getName().equals("value") || m.getName().equals("text") || m.getName().equals("input")) && m.getParameterCount() == 0) {
                try {
                    Object res = m.invoke(responseView);
                    if (res != null) return res.toString();
                } catch (Throwable ignored) {}
            }
        }

        return "";
    }

    // ==========================================
    // 3. BUNGEE / SPIGOT DIALOG IMPLEMENTATION
    // ==========================================
    private static boolean showBungeeInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                           Consumer<String> onInput, Runnable onCancel) {
        try {
            Class<?> dialogClass = findClass("net.md_5.bungee.api.dialog.Dialog");
            Class<?> dialogBaseClass = findClass("net.md_5.bungee.api.dialog.DialogBase");
            Class<?> dialogBodyClass = findClass("net.md_5.bungee.api.dialog.body.PlainMessageBody", "net.md_5.bungee.api.dialog.body.DialogBody");
            Class<?> dialogInputClass = findClass("net.md_5.bungee.api.dialog.input.TextInput");
            Class<?> dialogTypeClass = findClass("net.md_5.bungee.api.dialog.type.NoticeDialog", "net.md_5.bungee.api.dialog.NoticeDialog");
            Class<?> actionButtonClass = findClass("net.md_5.bungee.api.dialog.ActionButton");
            Class<?> customActionClass = findClass("net.md_5.bungee.api.dialog.action.CustomClickAction");
            Class<?> textComponentClass = findClass("net.md_5.bungee.api.chat.TextComponent");

            if (dialogClass == null || dialogBaseClass == null || dialogBodyClass == null || dialogInputClass == null || actionButtonClass == null) {
                return false;
            }

            Component titleComp = (title != null && !title.isBlank())
                    ? (title.contains("<") || title.contains("&") ? mm.deserialize(title) : Component.text(title))
                    : mm.deserialize("<gold><b>INPUT</b></gold>");
            Component promptComp = (prompt != null && !prompt.isBlank())
                    ? (prompt.contains("<") || prompt.contains("&") ? mm.deserialize(prompt) : Component.text(prompt))
                    : mm.deserialize("<yellow>Masukkan teks:</yellow>");
            Component okComp = mm.deserialize("<green>✔</green> <white><b>OK</b></white>");

            Object bungeeTitle = getBungeeComponent(textComponentClass, titleComp);
            Object bungeePrompt = getBungeeComponent(textComponentClass, promptComp);
            Object bungeeOk = getBungeeComponent(textComponentClass, okComp);

            Object textInput = null;
            for (java.lang.reflect.Constructor<?> c : dialogInputClass.getConstructors()) {
                if (c.getParameterCount() == 7) {
                    textInput = c.newInstance("input_key", 200, bungeePrompt, false, (defaultText != null ? defaultText.trim() : ""), 256, null);
                    break;
                }
            }
            if (textInput == null) return false;

            Object plainBody = null;
            for (java.lang.reflect.Constructor<?> c : dialogBodyClass.getConstructors()) {
                if (c.getParameterCount() == 2) {
                    plainBody = c.newInstance(bungeePrompt, 200);
                    break;
                } else if (c.getParameterCount() == 1) {
                    plainBody = c.newInstance(bungeePrompt);
                    break;
                }
            }
            if (plainBody == null) return false;

            Class<?> afterActionEnum = findClass("net.md_5.bungee.api.dialog.DialogBase$AfterAction", "net.md_5.bungee.api.dialog.DialogBase.AfterAction");
            Object afterActionClose = null;
            if (afterActionEnum != null) {
                for (Object constant : afterActionEnum.getEnumConstants()) {
                    if (constant.toString().equals("CLOSE")) {
                        afterActionClose = constant;
                        break;
                    }
                }
            }

            Object dialogBase = null;
            for (java.lang.reflect.Constructor<?> c : dialogBaseClass.getConstructors()) {
                if (c.getParameterCount() == 7) {
                    dialogBase = c.newInstance(bungeeTitle, null, List.of(textInput), List.of(plainBody), true, false, afterActionClose);
                    break;
                }
            }
            if (dialogBase == null) return false;

            Object action = null;
            if (customActionClass != null) {
                for (java.lang.reflect.Constructor<?> c : customActionClass.getConstructors()) {
                    if (c.getParameterCount() == 1 && c.getParameterTypes()[0] == String.class) {
                        action = c.newInstance("apexsions:input");
                        break;
                    }
                }
            }
            if (action == null) return false;

            Object okButton = null;
            for (java.lang.reflect.Constructor<?> c : actionButtonClass.getConstructors()) {
                if (c.getParameterCount() == 4) {
                    okButton = c.newInstance(bungeeOk, null, 200, action);
                    break;
                }
            }
            if (okButton == null) return false;

            Object noticeDialogType = null;
            for (java.lang.reflect.Constructor<?> c : dialogTypeClass.getConstructors()) {
                if (c.getParameterCount() == 2) {
                    noticeDialogType = c.newInstance(dialogBase, okButton);
                    break;
                }
            }
            if (noticeDialogType == null) return false;

            Object playerSpigot = player.getClass().getMethod("spigot").invoke(player);
            Method showDialogMethod = playerSpigot.getClass().getMethod("showDialog", findClass("net.md_5.bungee.api.dialog.Dialog"));
            showDialogMethod.invoke(playerSpigot, noticeDialogType);

            Class<? extends org.bukkit.event.Event> eventClass = (Class<? extends org.bukkit.event.Event>) findClass("org.bukkit.event.player.PlayerCustomClickEvent");
            if (eventClass != null) {
                org.bukkit.event.Listener dummyListener = new org.bukkit.event.Listener() {};
                org.bukkit.plugin.EventExecutor executor = (listener, event) -> {
                    if (!eventClass.isInstance(event)) return;
                    try {
                        Method getPlayerMethod = eventClass.getMethod("getPlayer");
                        Player eventPlayer = (Player) getPlayerMethod.invoke(event);

                        if (!eventPlayer.equals(player)) return;

                        Method getIdMethod = eventClass.getMethod("getId");
                        Object namespacedKey = getIdMethod.invoke(event);
                        String keyStr = namespacedKey != null ? namespacedKey.toString() : "";

                        if (!keyStr.equals("apexsions:input")) return;

                        Method getDataMethod = eventClass.getMethod("getData");
                        Object jsonElement = getDataMethod.invoke(event);

                        String textVal = "";
                        if (jsonElement != null) {
                            try {
                                Method isJsonObject = jsonElement.getClass().getMethod("isJsonObject");
                                if ((boolean) isJsonObject.invoke(jsonElement)) {
                                    Method getAsJsonObject = jsonElement.getClass().getMethod("getAsJsonObject");
                                    Object jsonObj = getAsJsonObject.invoke(jsonElement);
                                    Method getMethod = jsonObj.getClass().getMethod("get", String.class);
                                    Object jsonVal = getMethod.invoke(jsonObj, "input_key");
                                    if (jsonVal != null) {
                                        Method getAsString = jsonVal.getClass().getMethod("getAsString");
                                        textVal = (String) getAsString.invoke(jsonVal);
                                    }
                                }
                            } catch (Throwable ignored) {}
                        }

                        final String finalResult = textVal != null ? textVal.trim() : "";
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            try {
                                onInput.accept(finalResult);
                            } catch (Throwable ex) {
                                player.sendMessage("§cError: " + ex.getMessage());
                            }
                        });

                        org.bukkit.event.HandlerList.unregisterAll(dummyListener);
                    } catch (Throwable ignored) {}
                };

                Bukkit.getPluginManager().registerEvent(eventClass, dummyListener, org.bukkit.event.EventPriority.NORMAL, executor, plugin);
            }

            return true;
        } catch (Throwable t) {
            plugin.getLogger().warning("[NativeDialogAdapter] Bungee Dialog unavailable or error: " + t.getMessage());
            return false;
        }
    }

    private static Object getBungeeComponent(Class<?> textComponentClass, Component adv) {
        try {
            String legacyStr = legacy.serialize(adv);
            Method fromLegacyMethod = textComponentClass.getMethod("fromLegacyText", String.class);
            Object array = fromLegacyMethod.invoke(null, legacyStr);
            for (java.lang.reflect.Constructor<?> c : textComponentClass.getConstructors()) {
                if (c.getParameterCount() == 1 && c.getParameterTypes()[0].isArray()) {
                    return c.newInstance(array);
                }
            }
            return ((Object[]) array)[0];
        } catch (Throwable t) {
            return null;
        }
    }

    // ==========================================
    // ITEM MODIFIER DIALOG GUI (ITEM IN TOP CENTER)
    // ==========================================
    public static class DialogButtonData {
        private final String label;
        private final String tooltip;
        private final Runnable callback;

        public DialogButtonData(String label, String tooltip, Runnable callback) {
            this.label = label != null ? label : "";
            this.tooltip = tooltip != null ? tooltip : "";
            this.callback = callback;
        }

        public String getLabel() { return label; }
        public String getTooltip() { return tooltip; }
        public Runnable getCallback() { return callback; }
    }

    public static boolean showMultiActionDialog(
            Plugin plugin,
            Player player,
            org.bukkit.inventory.ItemStack item,
            String title,
            String description,
            List<DialogButtonData> buttons,
            DialogButtonData exitButton,
            int columns
    ) {
        if (player == null || !player.isOnline() || item == null) return false;

        // 1. Bedrock Floodgate Simple Form
        if (BedrockFormAdapter.isBedrockPlayer(player)) {
            if (BedrockFormAdapter.openMultiActionForm(plugin, player, item, title, description, buttons, exitButton)) {
                return true;
            }
        }

        // 2. NightCore Native Dialog (Exact engine used by ExcellentCrates on 26.2)
        if (isNightCoreSupported()) {
            if (showNightCoreMultiActionDialog(plugin, player, item, title, description, buttons, exitButton, columns)) {
                return true;
            }
        }

        // 3. Native Paper Dialog API
        if (isPaperSupported()) {
            if (showPaperMultiActionDialog(plugin, player, item, title, description, buttons, exitButton, columns)) {
                return true;
            }
        }

        return false;
    }

    public static boolean showItemModifierDialog(
            Plugin plugin,
            Player player,
            org.bukkit.inventory.ItemStack item,
            String description,
            boolean isArmor,
            boolean isTool,
            int activeCE,
            int activeVanilla,
            Runnable onCustomEnchants,
            Runnable onVanillaEnchants,
            Runnable onRenameItem,
            Runnable onSetBonus,
            Runnable onRemoveEnchants,
            Runnable onResetEnchants,
            Runnable onBack
    ) {
        List<DialogButtonData> buttons = new ArrayList<>();
        buttons.add(new DialogButtonData("<purple><bold>🔮 KELOLA CUSTOM ENCHANTS</bold></purple>", "Buka katalog 182 Custom Enchantments", onCustomEnchants));
        buttons.add(new DialogButtonData("<yellow><bold>📜 KELOLA VANILLA ENCHANTS</bold></yellow>", "Buka katalog sihir Vanilla Minecraft", onVanillaEnchants));
        buttons.add(new DialogButtonData("<gold><bold>🏷 UBAH NAMA ITEM</bold></gold>", "Ubah nama item via GUI Dialog", onRenameItem));

        if (isArmor) {
            buttons.add(new DialogButtonData("<blue><bold>🛡 ATUR ARMOR SET BONUS</bold></blue>", "Atur bonus 2-piece dan 4-piece visual", onSetBonus));
        } else if (isTool) {
            buttons.add(new DialogButtonData("<aqua><bold>⚔ ATUR TOOL SET BONUS</bold></aqua>", "Atur bonus atribut & sinergi tool", onSetBonus));
        }

        int totalActive = activeCE + activeVanilla;
        if (totalActive > 0) {
            buttons.add(new DialogButtonData("<red><bold>✂ HAPUS ENCHANT TERTENTU</bold></red>", "Lepas sihir satu per satu (" + totalActive + " aktif)", onRemoveEnchants));
        }

        buttons.add(new DialogButtonData("<red><bold>🗑 RESET SEMUA ENCHANT</bold></red>", "Hapus seluruh sihir dari item", onResetEnchants));

        DialogButtonData exitBtn = new DialogButtonData("<gray><bold>⬅ KEMBALI KE ITEM CREATOR</bold></gray>", "Simpan dan kembali ke creator", onBack);

        return showMultiActionDialog(
                plugin,
                player,
                item,
                "<gradient:#e74c3c:#f39c12><bold>🛠 EDIT ITEM & ENCHANTS 🛠</bold></gradient>",
                description,
                buttons,
                exitBtn,
                2
        );
    }

    private static boolean showNightCoreMultiActionDialog(
            Plugin plugin,
            Player player,
            org.bukkit.inventory.ItemStack item,
            String title,
            String description,
            List<DialogButtonData> buttons,
            DialogButtonData exitButton,
            int columns
    ) {
        try {
            Class<?> dialogsClass = findClass("su.nightexpress.nightcore.ui.dialog.Dialogs");
            Class<?> dialogBasesClass = findClass("su.nightexpress.nightcore.ui.dialog.build.DialogBases");
            Class<?> dialogBodiesClass = findClass("su.nightexpress.nightcore.ui.dialog.build.DialogBodies");
            Class<?> dialogButtonsClass = findClass("su.nightexpress.nightcore.ui.dialog.build.DialogButtons");
            Class<?> dialogActionsClass = findClass("su.nightexpress.nightcore.ui.dialog.build.DialogActions");
            Class<?> dialogTypesClass = findClass("su.nightexpress.nightcore.ui.dialog.build.DialogTypes");
            Class<?> dialogResponseHandlerClass = findClass("su.nightexpress.nightcore.bridge.dialog.response.DialogResponseHandler");
            Class<?> wrappedDialogBuilderClass = findClass("su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog$Builder", "su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog.Builder");

            if (dialogsClass == null || dialogBasesClass == null || dialogBodiesClass == null || dialogButtonsClass == null
                    || dialogActionsClass == null || dialogTypesClass == null || dialogResponseHandlerClass == null || wrappedDialogBuilderClass == null) {
                return false;
            }

            // 1. Item Dialog Body (item rendered at top center)
            Method itemBodyMethod = null;
            for (Method m : dialogBodiesClass.getMethods()) {
                if (m.getName().equals("item") && m.getParameterCount() == 1 && m.getParameterTypes()[0] == org.bukkit.inventory.ItemStack.class) {
                    itemBodyMethod = m;
                    break;
                }
            }
            if (itemBodyMethod == null) return false;

            Object itemBodyBuilder = itemBodyMethod.invoke(null, item.clone());
            String cleanDesc = (description != null && !description.isBlank()) ? description : "";
            Method plainMsgM = dialogBodiesClass.getMethod("plainMessage", String.class);
            Object plainDesc = plainMsgM.invoke(null, cleanDesc);

            for (Method m : itemBodyBuilder.getClass().getMethods()) {
                if (m.getName().equals("description") && m.getParameterCount() == 1) {
                    m.invoke(itemBodyBuilder, plainDesc);
                    break;
                }
            }
            Object itemBody = itemBodyBuilder.getClass().getMethod("build").invoke(itemBodyBuilder);

            // 2. Base Builder
            Method baseBuilderM = dialogBasesClass.getMethod("builder", String.class);
            Object baseBuilder = baseBuilderM.invoke(null, (title != null && !title.isBlank()) ? title : "APEXSIONS");
            for (Method m : baseBuilder.getClass().getMethods()) {
                if (m.getName().equals("body")) {
                    if (m.getParameterCount() == 1 && List.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        m.invoke(baseBuilder, List.of(itemBody));
                        break;
                    } else if (m.getParameterCount() == 1 && m.getParameterTypes()[0].isArray()) {
                        Object arr = java.lang.reflect.Array.newInstance(m.getParameterTypes()[0].getComponentType(), 1);
                        java.lang.reflect.Array.set(arr, 0, itemBody);
                        m.invoke(baseBuilder, arr);
                        break;
                    }
                }
            }
            Object base = baseBuilder.getClass().getMethod("build").invoke(baseBuilder);

            // 3. Action Buttons
            Method btnActionM = dialogButtonsClass.getMethod("action", String.class, String.class);
            Method customClickM = dialogActionsClass.getMethod("customClick", String.class);

            List<Object> buttonList = new ArrayList<>();
            if (buttons != null) {
                for (int i = 0; i < buttons.size(); i++) {
                    DialogButtonData b = buttons.get(i);
                    buttonList.add(createNightCoreButton(btnActionM, customClickM, b.getLabel(), b.getTooltip(), "btn_" + i));
                }
            }

            // 4. Exit / Back Button
            Object backBtn = null;
            if (exitButton != null) {
                backBtn = createNightCoreButton(btnActionM, customClickM, exitButton.getLabel(), exitButton.getTooltip(), "exit_action");
            }

            // 5. MultiAction Dialog Type
            Object multiActionBuilder = null;
            for (Method m : dialogTypesClass.getMethods()) {
                if (m.getName().equals("multiAction")) {
                    if (List.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        multiActionBuilder = m.invoke(null, buttonList);
                        break;
                    } else if (m.getParameterTypes()[0].isArray()) {
                        Object arr = java.lang.reflect.Array.newInstance(m.getParameterTypes()[0].getComponentType(), buttonList.size());
                        for (int i = 0; i < buttonList.size(); i++) java.lang.reflect.Array.set(arr, i, buttonList.get(i));
                        multiActionBuilder = m.invoke(null, arr);
                        break;
                    }
                }
            }
            if (multiActionBuilder == null) return false;

            for (Method m : multiActionBuilder.getClass().getMethods()) {
                if (m.getName().equals("exitAction") && m.getParameterCount() == 1 && backBtn != null) {
                    m.invoke(multiActionBuilder, backBtn);
                }
                if (m.getName().equals("columns") && m.getParameterCount() == 1) {
                    m.invoke(multiActionBuilder, Math.max(1, columns));
                }
            }
            Object dialogType = multiActionBuilder.getClass().getMethod("build").invoke(multiActionBuilder);

            // 6. WrappedDialog Builder & Handlers
            Object dialogBuilder = wrappedDialogBuilderClass.getDeclaredConstructor().newInstance();
            for (Method m : dialogBuilder.getClass().getMethods()) {
                if (m.getName().equals("base") && m.getParameterCount() == 1) {
                    m.invoke(dialogBuilder, base);
                }
                if (m.getName().equals("type") && m.getParameterCount() == 1) {
                    m.invoke(dialogBuilder, dialogType);
                }
            }

            Method handleResponseM = dialogBuilder.getClass().getMethod("handleResponse", String.class, dialogResponseHandlerClass);
            if (buttons != null) {
                for (int i = 0; i < buttons.size(); i++) {
                    DialogButtonData b = buttons.get(i);
                    registerNightCoreHandler(handleResponseM, dialogBuilder, dialogResponseHandlerClass, "btn_" + i, plugin, b.getCallback());
                }
            }

            Runnable exitRunnable = (exitButton != null && exitButton.getCallback() != null) ? exitButton.getCallback() : () -> {};
            registerNightCoreHandler(handleResponseM, dialogBuilder, dialogResponseHandlerClass, "exit_action", plugin, exitRunnable);
            registerNightCoreHandler(handleResponseM, dialogBuilder, dialogResponseHandlerClass, "back", plugin, exitRunnable);
            registerNightCoreHandler(handleResponseM, dialogBuilder, dialogResponseHandlerClass, "cancel", plugin, exitRunnable);

            // 7. Build and Show
            Object wrappedDialog = dialogBuilder.getClass().getMethod("build").invoke(dialogBuilder);
            Method showDialogM = null;
            for (Method m : dialogsClass.getMethods()) {
                if (m.getName().equals("showDialog") && m.getParameterCount() == 3) {
                    showDialogM = m;
                    break;
                }
            }
            if (showDialogM != null) {
                showDialogM.invoke(null, player, wrappedDialog, exitRunnable);
            } else {
                dialogsClass.getMethod("showDialog", Player.class, wrappedDialog.getClass()).invoke(null, player, wrappedDialog);
            }

            return true;
        } catch (Throwable t) {
            plugin.getLogger().warning("[NativeDialogAdapter] NightCore multi-action dialog error: " + t.getMessage());
            return false;
        }
    }

    private static Object createNightCoreButton(Method btnActionM, Method customClickM, String label, String tooltip, String actionId) throws Exception {
        Object btnBuilder = btnActionM.invoke(null, label, tooltip);
        Object action = customClickM.invoke(null, actionId);
        for (Method m : btnBuilder.getClass().getMethods()) {
            if (m.getName().equals("action") && m.getParameterCount() == 1) {
                m.invoke(btnBuilder, action);
                break;
            }
        }
        return btnBuilder.getClass().getMethod("build").invoke(btnBuilder);
    }

    private static void registerNightCoreHandler(Method handleResponseM, Object dialogBuilder, Class<?> handlerClass,
                                                 String actionId, Plugin plugin, Runnable callback) throws Exception {
        if (callback == null) return;
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("handle")) {
                Bukkit.getScheduler().runTask(plugin, callback);
            }
            return null;
        };
        Object proxy = Proxy.newProxyInstance(handlerClass.getClassLoader(), new Class<?>[]{handlerClass}, handler);
        handleResponseM.invoke(dialogBuilder, actionId, proxy);
    }

    private static boolean showPaperMultiActionDialog(
            Plugin plugin,
            Player player,
            org.bukkit.inventory.ItemStack item,
            String title,
            String description,
            List<DialogButtonData> buttons,
            DialogButtonData exitButton,
            int columns
    ) {
        try {
            Class<?> dialogClass = findClass("io.papermc.paper.dialog.Dialog");
            Class<?> dialogBaseClass = findClass("io.papermc.paper.registry.data.dialog.DialogBase", "io.papermc.paper.dialog.DialogBase");
            Class<?> dialogBodyClass = findClass("io.papermc.paper.registry.data.dialog.body.DialogBody", "io.papermc.paper.dialog.DialogBody");
            Class<?> dialogTypeClass = findClass("io.papermc.paper.registry.data.dialog.type.DialogType", "io.papermc.paper.dialog.DialogType");
            Class<?> actionButtonClass = findClass("io.papermc.paper.registry.data.dialog.action.ActionButton", "io.papermc.paper.dialog.ActionButton");
            Class<?> dialogActionClass = findClass("io.papermc.paper.registry.data.dialog.action.DialogAction", "io.papermc.paper.dialog.DialogAction");
            Class<?> dialogActionCallbackClass = findClass("io.papermc.paper.registry.data.dialog.action.DialogActionCallback", "io.papermc.paper.dialog.DialogActionCallback");

            if (dialogClass == null || dialogBaseClass == null || dialogBodyClass == null || actionButtonClass == null || dialogActionClass == null) {
                return false;
            }

            Component titleComp = mm.deserialize((title != null && !title.isBlank()) ? title : "APEXSIONS");
            Method baseBuilderMethod = dialogBaseClass.getMethod("builder", Component.class);
            Object baseBuilder = baseBuilderMethod.invoke(null, titleComp);

            Component descComp = mm.deserialize((description != null && !description.isBlank()) ? description : "");
            Method plainDescM = dialogBodyClass.getMethod("plainMessage", Component.class);
            Object plainDesc = plainDescM.invoke(null, descComp);

            Method itemBodyM = null;
            for (Method m : dialogBodyClass.getMethods()) {
                if (m.getName().equals("item")) {
                    itemBodyM = m;
                    break;
                }
            }
            if (itemBodyM != null) {
                Object itemBody = null;
                if (itemBodyM.getParameterCount() == 6) {
                    itemBody = itemBodyM.invoke(null, item.clone(), plainDesc, true, true, 200, 200);
                } else if (itemBodyM.getParameterCount() == 2) {
                    itemBody = itemBodyM.invoke(null, item.clone(), plainDesc);
                }
                if (itemBody != null) {
                    baseBuilder.getClass().getMethod("body", List.class).invoke(baseBuilder, List.of(itemBody));
                }
            }

            Object dialogBase = baseBuilder.getClass().getMethod("build").invoke(baseBuilder);

            Class<?> callbackInterface = dialogActionCallbackClass != null ? dialogActionCallbackClass : java.util.function.BiConsumer.class;
            List<Object> paperButtons = new ArrayList<>();

            if (buttons != null) {
                for (DialogButtonData b : buttons) {
                    paperButtons.add(createPaperActionButton(actionButtonClass, dialogActionClass, callbackInterface, plugin,
                            mm.deserialize(b.getLabel()),
                            mm.deserialize(b.getTooltip()),
                            b.getCallback()));
                }
            }

            Object backButton = null;
            if (exitButton != null) {
                backButton = createPaperActionButton(actionButtonClass, dialogActionClass, callbackInterface, plugin,
                        mm.deserialize(exitButton.getLabel()),
                        mm.deserialize(exitButton.getTooltip()),
                        exitButton.getCallback());
            }

            Object multiActionType = null;
            for (Method m : dialogTypeClass.getMethods()) {
                if (m.getName().equals("multiAction")) {
                    if (m.getParameterCount() == 3) {
                        multiActionType = m.invoke(null, paperButtons, backButton, Math.max(1, columns));
                        break;
                    } else if (m.getParameterCount() == 2) {
                        multiActionType = m.invoke(null, paperButtons, backButton);
                        break;
                    }
                }
            }
            if (multiActionType == null) return false;

            Method createDialogMethod = dialogClass.getMethod("create", dialogBaseClass, dialogTypeClass);
            Object dialogObj = createDialogMethod.invoke(null, dialogBase, multiActionType);

            Method showDialogMethod = player.getClass().getMethod("showDialog", dialogClass);
            showDialogMethod.invoke(player, dialogObj);
            return true;
        } catch (Throwable t) {
            plugin.getLogger().warning("[NativeDialogAdapter] Paper multi-action dialog error: " + t.getMessage());
            return false;
        }
    }

    private static Object createPaperActionButton(Class<?> actionButtonClass, Class<?> dialogActionClass, Class<?> callbackInterface,
                                                  Plugin plugin, Component label, Component tooltip, Runnable callback) throws Exception {
        InvocationHandler clickHandler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                if (method.getName().equals("equals")) return proxy == (args != null && args.length > 0 ? args[0] : null);
                if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                return "PaperActionCallback@" + Integer.toHexString(System.identityHashCode(proxy));
            }
            if (callback != null) {
                Bukkit.getScheduler().runTask(plugin, callback);
            }
            return null;
        };
        Object callbackProxy = Proxy.newProxyInstance(callbackInterface.getClassLoader(), new Class<?>[]{callbackInterface}, clickHandler);
        Method customActionMethod = null;
        for (Method m : dialogActionClass.getMethods()) {
            if (m.getParameterCount() == 1 && m.getParameterTypes()[0].isAssignableFrom(callbackInterface)) {
                customActionMethod = m;
                break;
            }
        }
        Object action = customActionMethod != null ? customActionMethod.invoke(null, callbackProxy) : null;
        Method createButtonMethod = actionButtonClass.getMethod("create", Component.class, Component.class, int.class, dialogActionClass);
        return createButtonMethod.invoke(null, label, tooltip, 150, action);
    }
}