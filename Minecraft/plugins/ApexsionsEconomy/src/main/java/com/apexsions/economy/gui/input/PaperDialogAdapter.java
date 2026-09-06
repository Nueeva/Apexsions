package com.apexsions.economy.gui.input;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Consumer;

/**
 * Adapter to interact with Paper's Native Dialog API on modern Minecraft (1.21.6+ / 26.x).
 * Displays the native Minecraft text input dialog window (matching client 26.2 Dialog packet).
 */
public class PaperDialogAdapter {

    private static Boolean dialogSupported = null;
    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static Class<?> findClass(String... names) {
        for (String name : names) {
            try {
                return Class.forName(name);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    public static boolean isSupported() {
        if (dialogSupported == null) {
            dialogSupported = findClass("io.papermc.paper.dialog.Dialog") != null;
        }
        return dialogSupported;
    }

    /**
     * Shows the native Minecraft 26.2 Dialog text input screen.
     */
    public static boolean showInput(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                    Consumer<String> onInput, Runnable onCancel) {
        if (!isSupported()) return false;

        try {
            Class<?> dialogClass = findClass(
                    "io.papermc.paper.dialog.Dialog"
            );
            Class<?> dialogBaseClass = findClass(
                    "io.papermc.paper.registry.data.dialog.DialogBase",
                    "io.papermc.paper.dialog.DialogBase"
            );
            Class<?> dialogBodyClass = findClass(
                    "io.papermc.paper.registry.data.dialog.body.DialogBody",
                    "io.papermc.paper.registry.data.dialog.DialogBody",
                    "io.papermc.paper.dialog.DialogBody"
            );
            Class<?> dialogInputClass = findClass(
                    "io.papermc.paper.registry.data.dialog.input.DialogInput",
                    "io.papermc.paper.registry.data.dialog.DialogInput",
                    "io.papermc.paper.dialog.DialogInput"
            );
            Class<?> dialogTypeClass = findClass(
                    "io.papermc.paper.registry.data.dialog.type.DialogType",
                    "io.papermc.paper.registry.data.dialog.DialogType",
                    "io.papermc.paper.dialog.DialogType"
            );
            Class<?> actionButtonClass = findClass(
                    "io.papermc.paper.registry.data.dialog.action.ActionButton",
                    "io.papermc.paper.registry.data.dialog.ActionButton",
                    "io.papermc.paper.dialog.ActionButton"
            );
            Class<?> dialogActionClass = findClass(
                    "io.papermc.paper.registry.data.dialog.action.DialogAction",
                    "io.papermc.paper.registry.data.dialog.DialogAction",
                    "io.papermc.paper.dialog.DialogAction"
            );
            Class<?> dialogActionCallbackClass = findClass(
                    "io.papermc.paper.registry.data.dialog.action.DialogActionCallback",
                    "io.papermc.paper.registry.data.dialog.DialogActionCallback",
                    "io.papermc.paper.dialog.DialogActionCallback"
            );
            Class<?> clickCallbackOptionsClass = findClass(
                    "net.kyori.adventure.text.event.ClickCallback$Options",
                    "net.kyori.adventure.text.event.ClickCallback.Options"
            );

            if (dialogClass == null || dialogBaseClass == null || dialogBodyClass == null
                    || dialogInputClass == null || dialogTypeClass == null || actionButtonClass == null
                    || dialogActionClass == null) {
                plugin.getLogger().warning("[PaperDialogAdapter] Incompatible Paper Dialog classes in classpath.");
                return false;
            }

            Component titleComp = mm.deserialize(title != null ? title : "<gold><b>INPUT</b></gold>");
            Component promptComp = mm.deserialize(prompt != null ? prompt : "<yellow>Masukkan teks:</yellow>");

            // 1. Build DialogBase
            Method baseBuilderMethod = dialogBaseClass.getMethod("builder", Component.class);
            Object baseBuilder = baseBuilderMethod.invoke(null, titleComp);

            // DialogBody.plainMessage
            Method plainMessageMethod = dialogBodyClass.getMethod("plainMessage", Component.class);
            Object bodyItem = plainMessageMethod.invoke(null, promptComp);
            baseBuilder.getClass().getMethod("body", List.class).invoke(baseBuilder, List.of(bodyItem));

            // DialogInput.text("input_key", label)
            Method textInputMethod = null;
            for (Method m : dialogInputClass.getMethods()) {
                if (m.getName().equals("text") && m.getParameterCount() >= 2) {
                    textInputMethod = m;
                    break;
                }
            }
            if (textInputMethod != null) {
                Object textInputBuilder = textInputMethod.invoke(null, "input_key", Component.text("Value"));
                if (defaultText != null && !defaultText.isBlank()) {
                    for (Method m : textInputBuilder.getClass().getMethods()) {
                        if ((m.getName().equals("initial") || m.getName().equals("defaultValue") || m.getName().equals("value") || m.getName().equals("initialValue"))
                                && m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class) {
                            try {
                                m.invoke(textInputBuilder, defaultText.trim());
                                break;
                            } catch (Throwable ignored) {}
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

            // 2. Build ClickCallback.Options
            Object clickOptions = null;
            if (clickCallbackOptionsClass != null) {
                try {
                    Method builderM = clickCallbackOptionsClass.getMethod("builder");
                    Object b = builderM.invoke(null);
                    clickOptions = b.getClass().getMethod("build").invoke(b);
                } catch (Throwable ignored) {}
            }

            // 3. OK Button Action Callback
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
                        try {
                            okAction = m.invoke(null, okCallback, clickOptions);
                            break;
                        } catch (Throwable ignored) {}
                    } else if (m.getParameterCount() == 1) {
                        try {
                            okAction = m.invoke(null, okCallback);
                            break;
                        } catch (Throwable ignored) {}
                    }
                }
            }

            // 4. Back / Cancel Button Action Callback
            InvocationHandler backHandler = (proxy, method, args) -> {
                if (onCancel != null) {
                    Bukkit.getScheduler().runTask(plugin, onCancel);
                }
                return null;
            };
            Object backCallback = Proxy.newProxyInstance(callbackInterface.getClassLoader(), new Class<?>[]{callbackInterface}, backHandler);

            Object backAction = null;
            for (Method m : dialogActionClass.getMethods()) {
                if (m.getName().equals("customClick")) {
                    if (m.getParameterCount() == 2 && clickOptions != null) {
                        try {
                            backAction = m.invoke(null, backCallback, clickOptions);
                            break;
                        } catch (Throwable ignored) {}
                    } else if (m.getParameterCount() == 1) {
                        try {
                            backAction = m.invoke(null, backCallback);
                            break;
                        } catch (Throwable ignored) {}
                    }
                }
            }

            // 5. Build Action Buttons
            Object okButton = null;
            Object backButton = null;

            for (Method m : actionButtonClass.getMethods()) {
                if (m.getName().equals("create")) {
                    if (m.getParameterCount() == 4) {
                        try {
                            okButton = m.invoke(null, Component.text("✔ OK"), null, 200, okAction);
                            backButton = m.invoke(null, Component.text("⬅ BACK"), null, 200, backAction);
                            break;
                        } catch (Throwable ignored) {}
                    } else if (m.getParameterCount() == 2) {
                        try {
                            okButton = m.invoke(null, Component.text("✔ OK"), okAction);
                            backButton = m.invoke(null, Component.text("⬅ BACK"), backAction);
                            break;
                        } catch (Throwable ignored) {}
                    }
                }
            }

            if (okButton == null) {
                for (Method m : actionButtonClass.getMethods()) {
                    if (m.getName().equals("builder") && m.getParameterCount() == 1) {
                        try {
                            Object b1 = m.invoke(null, Component.text("✔ OK"));
                            b1.getClass().getMethod("action", dialogActionClass).invoke(b1, okAction);
                            okButton = b1.getClass().getMethod("build").invoke(b1);

                            Object b2 = m.invoke(null, Component.text("⬅ BACK"));
                            b2.getClass().getMethod("action", dialogActionClass).invoke(b2, backAction);
                            backButton = b2.getClass().getMethod("build").invoke(b2);
                            break;
                        } catch (Throwable ignored) {}
                    }
                }
            }

            // 6. Build DialogType
            Object dialogType = null;
            for (Method m : dialogTypeClass.getMethods()) {
                if (m.getName().equals("confirmation") && m.getParameterCount() == 2) {
                    try {
                        dialogType = m.invoke(null, okButton, backButton);
                        break;
                    } catch (Throwable ignored) {}
                }
            }
            if (dialogType == null) {
                for (Method m : dialogTypeClass.getMethods()) {
                    if (m.getName().equals("notice")) {
                        if (m.getParameterCount() == 1) {
                            try {
                                dialogType = m.invoke(null, okButton);
                                break;
                            } catch (Throwable ignored) {}
                        } else if (m.getParameterCount() == 0) {
                            try {
                                dialogType = m.invoke(null);
                                break;
                            } catch (Throwable ignored) {}
                        }
                    }
                }
            }

            // 7. Assemble Dialog
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

            // 8. Show Dialog
            for (Method m : Player.class.getMethods()) {
                if (m.getName().equals("showDialog") && m.getParameterCount() == 1) {
                    m.invoke(player, dialogInstance);
                    return true;
                }
            }

            Class<?> audienceClass = Class.forName("net.kyori.adventure.audience.Audience");
            for (Method m : audienceClass.getMethods()) {
                if (m.getName().equals("showDialog") && m.getParameterCount() == 1) {
                    m.invoke(player, dialogInstance);
                    return true;
                }
            }

            return false;
        } catch (Throwable t) {
            plugin.getLogger().severe("[PaperDialogAdapter] Error showing native dialog to " + player.getName() + ": " + t.getMessage());
            t.printStackTrace();
            return false;
        }
    }
}
