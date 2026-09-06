package com.apexsions.battlepass.gui.input;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Consumer;

/**
 * Adapter to interact with Minecraft 1.21.4+ Native Dialog API.
 * Supports both Paper API (io.papermc.paper.dialog.*) and Spigot/Bungee API (net.md_5.bungee.api.dialog.*).
 */
public class NativeDialogAdapter {

    private static Boolean paperSupported = null;
    private static Boolean bungeeSupported = null;
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

    public static Class<?> findClass(String... names) {
        for (String name : names) {
            try {
                return Class.forName(name);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    public static boolean isSupported() {
        if (paperSupported == null) {
            paperSupported = findClass("io.papermc.paper.dialog.Dialog") != null;
        }
        if (bungeeSupported == null) {
            bungeeSupported = findClass("net.md_5.bungee.api.dialog.Dialog") != null;
        }
        return paperSupported || bungeeSupported;
    }

    public static boolean showInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                    Consumer<String> onInput, Runnable onCancel) {
        if (!isSupported()) return false;

        if (paperSupported) {
            boolean shown = showPaperInput(plugin, player, title, prompt, defaultText, onInput, onCancel);
            if (shown) return true;
            if (bungeeSupported) {
                return showBungeeInput(plugin, player, title, prompt, defaultText, onInput, onCancel);
            }
            return false;
        } else if (bungeeSupported) {
            return showBungeeInput(plugin, player, title, prompt, defaultText, onInput, onCancel);
        }
        return false;
    }

    private static boolean showBungeeInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                           Consumer<String> onInput, Runnable onCancel) {
        try {
            Class<?> dialogClass = findClass("net.md_5.bungee.api.dialog.Dialog");
            Class<?> dialogBaseClass = findClass("net.md_5.bungee.api.dialog.DialogBase");
            Class<?> dialogBodyClass = findClass("net.md_5.bungee.api.dialog.body.PlainMessageBody", "net.md_5.bungee.api.dialog.body.DialogBody");
            Class<?> dialogInputClass = findClass("net.md_5.bungee.api.dialog.input.TextInput");
            Class<?> dialogTypeClass = findClass("net.md_5.bungee.api.dialog.type.NoticeDialog", "net.md_5.bungee.api.dialog.NoticeDialog");
            Class<?> actionButtonClass = findClass("net.md_5.bungee.api.dialog.ActionButton");
            Class<?> staticActionClass = findClass("net.md_5.bungee.api.dialog.action.StaticAction", "net.md_5.bungee.api.dialog.action.Action");
            Class<?> customActionClass = findClass("net.md_5.bungee.api.dialog.action.CustomClickAction");
            Class<?> baseComponentClass = findClass("net.md_5.bungee.api.chat.BaseComponent");
            Class<?> textComponentClass = findClass("net.md_5.bungee.api.chat.TextComponent");

            if (dialogClass == null || dialogBaseClass == null || dialogBodyClass == null || dialogInputClass == null || actionButtonClass == null) {
                plugin.getLogger().warning("[NativeDialogAdapter] Bungee Dialog classes incomplete.");
                return false;
            }

            // Convert Adventure Component to Bungee Component
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

            // 1. Build DialogInput
            Object textInput = null;
            for (java.lang.reflect.Constructor<?> c : dialogInputClass.getConstructors()) {
                if (c.getParameterCount() == 7) {
                    textInput = c.newInstance("input_key", 200, bungeePrompt, false, (defaultText != null ? defaultText.trim() : ""), 256, null);
                    break;
                } else if (c.getParameterCount() == 6) { 
                    textInput = c.newInstance("input_key", 200, bungeePrompt, false, (defaultText != null ? defaultText.trim() : ""), 256);
                    break;
                }
            }

            // 2. Build DialogBody
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

            // 3. Build DialogBase
            Class<?> afterActionEnum = findClass("net.md_5.bungee.api.dialog.DialogBase$AfterAction");
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

            // 4. Build ActionButton
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
            
            // 5. Build DialogType (NoticeDialog)
            Object noticeDialogType = null;
            for (java.lang.reflect.Constructor<?> c : dialogTypeClass.getConstructors()) {
                if (c.getParameterCount() == 2) {
                    noticeDialogType = c.newInstance(dialogBase, okButton);
                    break;
                }
            }
            if (noticeDialogType == null) return false;

            // 6. Show it
            Object playerSpigot = player.getClass().getMethod("spigot").invoke(player);
            Method showDialogMethod = playerSpigot.getClass().getMethod("showDialog", findClass("net.md_5.bungee.api.dialog.Dialog"));
            showDialogMethod.invoke(playerSpigot, noticeDialogType);
            
            // 7. Register a temporary event listener for PlayerCustomClickEvent
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
                        Object namespacedKey = getIdMethod.invoke(event); // NamespacedKey
                        String keyStr = namespacedKey != null ? namespacedKey.toString() : "";
                        
                        if (!keyStr.equals("apexsions:input")) return;

                        Method getDataMethod = eventClass.getMethod("getData");
                        Object jsonElement = getDataMethod.invoke(event); // JsonElement
                        
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
                        
                        // Unregister after first successful callback
                        org.bukkit.event.HandlerList.unregisterAll(dummyListener);
                    } catch (Throwable ignored) {}
                };
                
                Bukkit.getPluginManager().registerEvent(eventClass, dummyListener, org.bukkit.event.EventPriority.NORMAL, executor, plugin);
            }

            return true;
        } catch (Throwable t) {
            plugin.getLogger().severe("[NativeDialogAdapter] Error showing Bungee Dialog: " + t.getMessage());
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

            // Create TextDialogInput using robust dynamic parameter matcher
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
                plugin.getLogger().warning("[NativeDialogAdapter] Could not instantiate Paper Dialog instance.");
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
            plugin.getLogger().severe("[NativeDialogAdapter] Error showing Paper Dialog: " + t.getMessage());
            return false;
        }
    }

    private static Object createTextInput(Class<?> dialogInputClass, Class<?> textDialogInputClass,
                                          String key, Component label, String defaultText) {
        // Option 1: Look on TextDialogInput for static builder / text / of / create
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

        // Option 2: Look on dialogInputClass for static text / builder / of / create
        if (dialogInputClass != null) {
            // First pass: try 2-parameter or 1-parameter method
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

            // Second pass: try any parameter count (e.g. 7 parameters)
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

        // Option 3: Check constructors
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
                args[i] = false; // multiline or labelVisible
            } else if (pt.isEnum()) {
                Object[] constants = pt.getEnumConstants();
                args[i] = (constants != null && constants.length > 0) ? constants[0] : null;
            } else {
                args[i] = null; // validator / filter / multiline options
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

        // 1. Try static create/of methods
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

        // 2. Try builder methods
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

        // 1. Confirmation type (with OK and BACK)
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

        // 2. Notice type
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

        // 3. Builder
        for (Method m : dialogTypeClass.getMethods()) {
            if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) && m.getName().equals("builder")) {
                try {
                    Object b = m.invoke(null);
                    if (b != null) {
                        for (Method bm : b.getClass().getMethods()) {
                            if (bm.getParameterCount() == 1) {
                                try { bm.invoke(b, okButton); } catch (Throwable ignored) {}
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
}