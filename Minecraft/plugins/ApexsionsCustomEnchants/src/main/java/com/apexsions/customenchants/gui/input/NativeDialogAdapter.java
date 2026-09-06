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
            return showPaperInput(plugin, player, title, prompt, defaultText, onInput, onCancel);
        } else {
            return showBungeeInput(plugin, player, title, prompt, defaultText, onInput, onCancel);
        }
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
            Component titleComp = mm.deserialize(title != null ? title : "<gold><b>INPUT</b></gold>");
            Component promptComp = mm.deserialize(prompt != null ? prompt : "<yellow>Masukkan teks:</yellow>");
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
                            // Extract "input_key" from JsonObject
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
                            } catch (Throwable t2) {
                                // Ignore
                            }
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
            t.printStackTrace();
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
            Class<?> dialogTypeClass = findClass("io.papermc.paper.registry.data.dialog.type.DialogType", "io.papermc.paper.dialog.DialogType");
            Class<?> actionButtonClass = findClass("io.papermc.paper.registry.data.dialog.action.ActionButton", "io.papermc.paper.dialog.ActionButton");
            Class<?> dialogActionClass = findClass("io.papermc.paper.registry.data.dialog.action.DialogAction", "io.papermc.paper.dialog.DialogAction");
            Class<?> dialogActionCallbackClass = findClass("io.papermc.paper.registry.data.dialog.action.DialogActionCallback", "io.papermc.paper.dialog.DialogActionCallback");
            Class<?> clickCallbackOptionsClass = findClass("net.kyori.adventure.text.event.ClickCallback$Options", "net.kyori.adventure.text.event.ClickCallback.Options");

            Component titleComp = mm.deserialize(title != null ? title : "<gold><b>INPUT</b></gold>");
            Component promptComp = mm.deserialize(prompt != null ? prompt : "<yellow>Masukkan teks:</yellow>");

            Method baseBuilderMethod = dialogBaseClass.getMethod("builder", Component.class);
            Object baseBuilder = baseBuilderMethod.invoke(null, titleComp);

            Method plainMessageMethod = dialogBodyClass.getMethod("plainMessage", Component.class);
            Object bodyItem = plainMessageMethod.invoke(null, promptComp);
            baseBuilder.getClass().getMethod("body", List.class).invoke(baseBuilder, List.of(bodyItem));

            Method textInputMethod = null;
            for (Method m : dialogInputClass.getMethods()) {
                if (m.getName().equals("text") && m.getParameterCount() >= 2) {
                    textInputMethod = m;
                    break;
                }
            }
            if (textInputMethod != null) {
                Object textInputBuilder = textInputMethod.invoke(null, "input_key", Component.text("Input"));
                for (Method m : textInputBuilder.getClass().getMethods()) {
                    if (m.getName().equals("labelVisible") && m.getParameterCount() == 1 && m.getParameterTypes()[0] == boolean.class) {
                        try { m.invoke(textInputBuilder, false); break; } catch (Throwable ignored) {}
                    }
                }
                if (defaultText != null && !defaultText.isBlank()) {
                    for (Method m : textInputBuilder.getClass().getMethods()) {
                        if ((m.getName().equals("initial") || m.getName().equals("defaultValue") || m.getName().equals("value") || m.getName().equals("initialValue"))
                                && m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class) {
                            try { m.invoke(textInputBuilder, defaultText.trim()); break; } catch (Throwable ignored) {}
                        }
                    }
                }
                Object textInput = textInputBuilder;
                try {
                    Method buildM = textInputBuilder.getClass().getMethod("build");
                    textInput = buildM.invoke(textInputBuilder);
                } catch (Throwable ignored) {}
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
                if (method.getName().equals("accept") || method.getName().equals("handle") || method.getName().equals("apply") || method.getName().equals("run")) {
                    Object responseView = (args != null && args.length > 0) ? args[0] : null;
                    String textVal = "";
                    if (responseView != null) {
                        try {
                            Method getTextM = responseView.getClass().getMethod("getText", String.class);
                            textVal = (String) getTextM.invoke(responseView, "input_key");
                        } catch (Throwable t1) {
                            try {
                                Method getM = responseView.getClass().getMethod("get", String.class);
                                Object res = getM.invoke(responseView, "input_key");
                                textVal = (res != null) ? res.toString() : "";
                            } catch (Throwable t2) {
                                for (Method m : responseView.getClass().getMethods()) {
                                    if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class && m.getReturnType() != void.class) {
                                        try {
                                            Object res = m.invoke(responseView, "input_key");
                                            if (res != null) {
                                                textVal = res.toString();
                                                break;
                                            }
                                        } catch (Throwable ignored) {}
                                    }
                                }
                            }
                        }
                    }
                    final String finalResult = textVal != null ? textVal.trim() : "";
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        try {
                            onInput.accept(finalResult);
                        } catch (Throwable ex) {
                            player.sendMessage("§cError: " + ex.getMessage());
                        }
                    });
                }
                return null;
            };

            Class<?> callbackInterface = dialogActionCallbackClass != null ? dialogActionCallbackClass : java.util.function.BiConsumer.class;
            Object okCallback = Proxy.newProxyInstance(callbackInterface.getClassLoader(), new Class<?>[]{callbackInterface}, okHandler);

            Object okAction = null;
            for (Method m : dialogActionClass.getMethods()) {
                if (m.getName().equals("customClick")) {
                    if (m.getParameterCount() == 2 && clickOptions != null) {
                        try { okAction = m.invoke(null, okCallback, clickOptions); break; } catch (Throwable ignored) {}
                    } else if (m.getParameterCount() == 1) {
                        try { okAction = m.invoke(null, okCallback); break; } catch (Throwable ignored) {}
                    }
                }
            }

            InvocationHandler backHandler = (proxy, method, args) -> {
                if (onCancel != null) { Bukkit.getScheduler().runTask(plugin, onCancel); }
                return null;
            };
            Object backCallback = Proxy.newProxyInstance(callbackInterface.getClassLoader(), new Class<?>[]{callbackInterface}, backHandler);

            Object backAction = null;
            for (Method m : dialogActionClass.getMethods()) {
                if (m.getName().equals("customClick")) {
                    if (m.getParameterCount() == 2 && clickOptions != null) {
                        try { backAction = m.invoke(null, backCallback, clickOptions); break; } catch (Throwable ignored) {}
                    } else if (m.getParameterCount() == 1) {
                        try { backAction = m.invoke(null, backCallback); break; } catch (Throwable ignored) {}
                    }
                }
            }

            Object okButton = null;
            Object backButton = null;

            Component okComp = mm.deserialize("<green>✔</green> <white><b>OK</b></white>");
            Component backComp = mm.deserialize("<yellow>⬅</yellow> <white><b>BACK</b></white>");

            for (Method m : actionButtonClass.getMethods()) {
                if (m.getName().equals("create")) {
                    if (m.getParameterCount() == 4) {
                        try { okButton = m.invoke(null, okComp, null, 200, okAction); backButton = m.invoke(null, backComp, null, 200, backAction); break; } catch (Throwable ignored) {}
                    } else if (m.getParameterCount() == 2) {
                        try { okButton = m.invoke(null, okComp, okAction); backButton = m.invoke(null, backComp, backAction); break; } catch (Throwable ignored) {}
                    }
                }
            }

            if (okButton == null) {
                for (Method m : actionButtonClass.getMethods()) {
                    if (m.getName().equals("builder") && m.getParameterCount() == 1) {
                        try {
                            Object b1 = m.invoke(null, okComp);
                            b1.getClass().getMethod("action", dialogActionClass).invoke(b1, okAction);
                            okButton = b1.getClass().getMethod("build").invoke(b1);
                            Object b2 = m.invoke(null, backComp);
                            b2.getClass().getMethod("action", dialogActionClass).invoke(b2, backAction);
                            backButton = b2.getClass().getMethod("build").invoke(b2);
                            break;
                        } catch (Throwable ignored) {}
                    }
                }
            }

            Object dialogType = null;
            for (Method m : dialogTypeClass.getMethods()) {
                if (m.getName().equals("confirmation") && m.getParameterCount() == 2) {
                    try { dialogType = m.invoke(null, okButton, backButton); break; } catch (Throwable ignored) {}
                }
            }
            if (dialogType == null) {
                for (Method m : dialogTypeClass.getMethods()) {
                    if (m.getName().equals("notice") && m.getParameterCount() == 1) {
                        try { dialogType = m.invoke(null, okButton); break; } catch (Throwable ignored) {}
                    }
                }
            }

            final Object finalBase = dialogBase;
            final Object finalType = dialogType;
            final Class<?> finalBaseClass = dialogBaseClass;
            final Class<?> finalTypeClass = dialogTypeClass;

            Consumer<Object> dialogBuilderConsumer = builder -> {
                try {
                    Object empty = builder.getClass().getMethod("empty").invoke(builder);
                    empty.getClass().getMethod("base", finalBaseClass).invoke(empty, finalBase);
                    empty.getClass().getMethod("type", finalTypeClass).invoke(empty, finalType);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            };

            Method createDialogMethod = dialogClass.getMethod("create", Consumer.class);
            Object dialogInstance = createDialogMethod.invoke(null, dialogBuilderConsumer);

            for (Method m : Player.class.getMethods()) {
                if (m.getName().equals("showDialog") && m.getParameterCount() == 1) {
                    m.invoke(player, dialogInstance);
                    return true;
                }
            }
            return false;
        } catch (Throwable t) {
            plugin.getLogger().severe("[NativeDialogAdapter] Error showing Paper Dialog: " + t.getMessage());
            return false;
        }
    }
}
