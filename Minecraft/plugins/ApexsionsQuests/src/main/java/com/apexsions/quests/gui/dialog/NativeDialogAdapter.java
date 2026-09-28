package com.apexsions.quests.gui.dialog;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Native Dialog Adapter for ApexsionsQuests.
 * Priority:
 * 1. Bedrock Native Form (Geyser / Floodgate SimpleForm)
 * 2. NightCore UI Dialog Engine (su.nightexpress.nightcore.ui.dialog.Dialogs - Custom Screen)
 * 3. Paper Native Dialog API (io.papermc.paper.dialog.Dialog)
 * 4. Interactive Chat Fallback
 */
public class NativeDialogAdapter {

    private static Boolean nightcoreSupported = null;
    private static Boolean paperSupported = null;
    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static class DialogButtonData {
        private final String label;
        private final String tooltip;
        private final String command;
        private final Runnable callback;

        public DialogButtonData(String label, String tooltip, Runnable callback) {
            this(label, tooltip, null, callback);
        }

        public DialogButtonData(String label, String tooltip, String command, Runnable callback) {
            this.label = label != null ? label : "";
            this.tooltip = tooltip != null ? tooltip : "";
            this.command = command;
            this.callback = callback;
        }

        public String getLabel() { return label; }
        public String getTooltip() { return tooltip; }
        public String getCommand() { return command; }
        public Runnable getCallback() { return callback; }
    }

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
            if (Bukkit.getPluginManager() != null) {
                for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
                    if (p == null || !p.isEnabled()) continue;
                    try {
                        return Class.forName(name, true, p.getClass().getClassLoader());
                    } catch (Throwable ignored) {}
                }
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
            paperSupported = findClass("io.papermc.paper.dialog.Dialog", "io.papermc.paper.registry.data.dialog.Dialog") != null;
        }
        return paperSupported;
    }

    public static boolean isSupported() {
        return isNightCoreSupported() || isPaperSupported() || BedrockFormAdapter.isFloodgatePresent();
    }

    public static boolean showMultiActionDialog(
            Plugin plugin,
            Player player,
            String title,
            String description,
            List<DialogButtonData> buttons,
            DialogButtonData exitButton,
            int columns
    ) {
        return showMultiActionDialog(plugin, player, null, title, description, buttons, exitButton, columns);
    }

    public static boolean showMultiActionDialog(
            Plugin plugin,
            Player player,
            ItemStack item,
            String title,
            String description,
            List<DialogButtonData> buttons,
            DialogButtonData exitButton,
            int columns
    ) {
        if (player == null || !player.isOnline()) return false;

        // Close any active chest inventory
        try {
            if (player.getOpenInventory().getTopInventory().getType() != org.bukkit.event.inventory.InventoryType.CRAFTING) {
                player.closeInventory();
            }
        } catch (Throwable ignored) {}

        // 1. Bedrock Floodgate / Geyser Form
        if (BedrockFormAdapter.isBedrockPlayer(player)) {
            if (BedrockFormAdapter.openMultiActionForm(plugin, player, item, title, description, buttons, exitButton)) {
                return true;
            }
        }

        // 2. NightCore Native Dialog / Custom Screen
        if (isNightCoreSupported()) {
            if (showNightCoreMultiActionDialog(plugin, player, item, title, description, buttons, exitButton, columns)) {
                return true;
            }
        }

        // 3. Official Paper Native Dialog API
        if (isPaperSupported()) {
            if (showPaperMultiActionDialog(plugin, player, item, title, description, buttons, exitButton, columns)) {
                return true;
            }
        }

        // 4. Bedrock Form fallback
        if (BedrockFormAdapter.isFloodgatePresent() && BedrockFormAdapter.openMultiActionForm(plugin, player, item, title, description, buttons, exitButton)) {
            return true;
        }

        // 5. Interactive Chat Fallback
        sendInteractiveChatFallback(plugin, player, title, description, buttons, exitButton);
        return true;
    }

    private static boolean showNightCoreMultiActionDialog(
            Plugin plugin,
            Player player,
            ItemStack item,
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
            ItemStack displayItem = (item != null && !item.getType().isAir()) ? item : new ItemStack(org.bukkit.Material.WRITTEN_BOOK);
            Method itemBodyMethod = null;
            for (Method m : dialogBodiesClass.getMethods()) {
                if (m.getName().equals("item") && m.getParameterCount() == 1 && m.getParameterTypes()[0] == ItemStack.class) {
                    itemBodyMethod = m;
                    break;
                }
            }
            if (itemBodyMethod == null) return false;

            Object itemBodyBuilder = itemBodyMethod.invoke(null, displayItem.clone());
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
            if (method.getDeclaringClass() == Object.class) {
                if (method.getName().equals("equals")) return proxy == (args != null && args.length > 0 ? args[0] : null);
                if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                return "DialogResponseHandler@" + Integer.toHexString(System.identityHashCode(proxy));
            }
            if (callback != null) {
                if (Bukkit.isPrimaryThread()) {
                    try {
                        callback.run();
                    } catch (Throwable t) {
                        plugin.getLogger().warning("[NativeDialogAdapter] NightCore button callback error: " + t.getMessage());
                    }
                } else {
                    Bukkit.getScheduler().runTask(plugin, callback);
                }
            }
            return null;
        };
        Object proxy = Proxy.newProxyInstance(handlerClass.getClassLoader(), new Class<?>[]{handlerClass}, handler);
        handleResponseM.invoke(dialogBuilder, actionId, proxy);
    }

    private static boolean showPaperMultiActionDialog(
            Plugin plugin,
            Player player,
            ItemStack item,
            String title,
            String description,
            List<DialogButtonData> buttons,
            DialogButtonData exitButton,
            int columns
    ) {
        try {
            Class<?> dialogClass = findClass("io.papermc.paper.dialog.Dialog", "io.papermc.paper.registry.data.dialog.Dialog");
            Class<?> dialogBaseClass = findClass("io.papermc.paper.registry.data.dialog.DialogBase", "io.papermc.paper.dialog.DialogBase");
            Class<?> dialogBodyClass = findClass("io.papermc.paper.registry.data.dialog.body.DialogBody", "io.papermc.paper.dialog.body.DialogBody", "io.papermc.paper.dialog.DialogBody");
            Class<?> dialogTypeClass = findClass("io.papermc.paper.registry.data.dialog.type.DialogType", "io.papermc.paper.dialog.type.DialogType", "io.papermc.paper.dialog.DialogType");
            Class<?> actionButtonClass = findClass("io.papermc.paper.registry.data.dialog.action.ActionButton", "io.papermc.paper.dialog.action.ActionButton", "io.papermc.paper.dialog.ActionButton");
            Class<?> dialogActionClass = findClass("io.papermc.paper.registry.data.dialog.action.DialogAction", "io.papermc.paper.dialog.action.DialogAction", "io.papermc.paper.dialog.DialogAction");
            Class<?> dialogActionCallbackClass = findClass("io.papermc.paper.registry.data.dialog.action.DialogActionCallback", "io.papermc.paper.dialog.action.DialogActionCallback");

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
            if (item != null && itemBodyM != null) {
                Object itemBody = null;
                if (itemBodyM.getParameterCount() == 6) {
                    itemBody = itemBodyM.invoke(null, item.clone(), plainDesc, true, true, 200, 200);
                } else if (itemBodyM.getParameterCount() == 2) {
                    itemBody = itemBodyM.invoke(null, item.clone(), plainDesc);
                }
                if (itemBody != null) {
                    baseBuilder.getClass().getMethod("body", List.class).invoke(baseBuilder, List.of(itemBody));
                }
            } else {
                baseBuilder.getClass().getMethod("body", List.class).invoke(baseBuilder, List.of(plainDesc));
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
                if (Bukkit.isPrimaryThread()) {
                    try {
                        callback.run();
                    } catch (Throwable t) {
                        plugin.getLogger().warning("[NativeDialogAdapter] Paper button callback error: " + t.getMessage());
                    }
                } else {
                    Bukkit.getScheduler().runTask(plugin, callback);
                }
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

    private static void sendInteractiveChatFallback(
            Plugin plugin,
            Player player,
            String title,
            String description,
            List<DialogButtonData> buttons,
            DialogButtonData exitButton
    ) {
        player.sendMessage(mm.deserialize("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>"));
        player.sendMessage(mm.deserialize("  " + title));
        if (description != null && !description.isBlank()) {
            player.sendMessage(mm.deserialize("  <gray>" + description + "</gray>"));
        }
        player.sendMessage(mm.deserialize(" "));

        if (buttons != null) {
            for (DialogButtonData b : buttons) {
                Component btnComp = mm.deserialize("  <gold>▶</gold> " + b.getLabel());
                if (b.getTooltip() != null && !b.getTooltip().isBlank()) {
                    btnComp = btnComp.hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(mm.deserialize(b.getTooltip())));
                }

                if (b.getCommand() != null && !b.getCommand().isBlank()) {
                    btnComp = btnComp.clickEvent(ClickEvent.runCommand(b.getCommand()));
                } else if (b.getCallback() != null) {
                    btnComp = btnComp.clickEvent(ClickEvent.callback(aud -> {
                        Bukkit.getScheduler().runTask(plugin, b.getCallback());
                    }));
                }
                player.sendMessage(btnComp);
            }
        }

        if (exitButton != null) {
            player.sendMessage(mm.deserialize(" "));
            Component exitComp = mm.deserialize("  <red>[✖] " + exitButton.getLabel() + "</red>");
            if (exitButton.getCallback() != null) {
                exitComp = exitComp.clickEvent(ClickEvent.callback(aud -> {
                    Bukkit.getScheduler().runTask(plugin, exitButton.getCallback());
                }));
            }
            player.sendMessage(exitComp);
        }
        player.sendMessage(mm.deserialize("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>"));
    }
}
