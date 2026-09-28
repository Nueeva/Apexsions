package com.apexsions.jobs.gui.dialog;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class NativeDialogAdapter {

    private static Boolean nightcoreSupported = null;
    private static Boolean paperSupported = null;
    private static final MiniMessage mm = MiniMessage.miniMessage();

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
        if (player == null || !player.isOnline()) return false;

        if (player.getOpenInventory().getTopInventory().getType() != org.bukkit.event.inventory.InventoryType.CRAFTING) {
            player.closeInventory();
        }

        // 1. Bedrock Floodgate Form
        if (BedrockFormAdapter.isBedrockPlayer(player)) {
            if (BedrockFormAdapter.openMultiActionForm(plugin, player, title, description, buttons, exitButton)) {
                return true;
            }
        }

        // 2. NightCore Native Dialog (Paper 1.21.4+)
        if (isNightCoreSupported()) {
            if (showNightCoreDialog(plugin, player, title, description, buttons, exitButton, columns)) {
                return true;
            }
        }

        // 3. Fallback: Bedrock Form if available or Chat
        if (BedrockFormAdapter.isFloodgatePresent() && BedrockFormAdapter.openMultiActionForm(plugin, player, title, description, buttons, exitButton)) {
            return true;
        }

        player.sendMessage(mm.deserialize("<gold><bold>" + title + "</bold></gold>"));
        if (description != null && !description.isBlank()) {
            player.sendMessage(mm.deserialize("<gray>" + description + "</gray>"));
        }
        if (buttons != null) {
            for (DialogButtonData b : buttons) {
                player.sendMessage(mm.deserialize(" <yellow>•</yellow> " + b.getLabel()));
            }
        }
        return false;
    }

    private static boolean showNightCoreDialog(
            Plugin plugin,
            Player player,
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

            Method baseBuilderM = dialogBasesClass.getMethod("builder", String.class);
            Object baseBuilder = baseBuilderM.invoke(null, (title != null && !title.isBlank()) ? title : "APEXSIONS JOBS");

            String cleanDesc = (description != null && !description.isBlank()) ? description : "";
            Method plainMsgM = dialogBodiesClass.getMethod("plainMessage", String.class);
            Object plainDesc = plainMsgM.invoke(null, cleanDesc);

            for (Method m : baseBuilder.getClass().getMethods()) {
                if (m.getName().equals("body")) {
                    if (m.getParameterCount() == 1 && List.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        m.invoke(baseBuilder, List.of(plainDesc));
                        break;
                    }
                }
            }
            Object base = baseBuilder.getClass().getMethod("build").invoke(baseBuilder);

            Method btnActionM = dialogButtonsClass.getMethod("action", String.class, String.class);
            Method customClickM = dialogActionsClass.getMethod("customClick", String.class);

            List<Object> buttonList = new ArrayList<>();
            List<Runnable> callbackList = new ArrayList<>();

            if (buttons != null) {
                for (int i = 0; i < buttons.size(); i++) {
                    DialogButtonData b = buttons.get(i);
                    String actionKey = "btn_" + i;
                    Object btn = btnActionM.invoke(null, b.getLabel(), actionKey);
                    for (Method m : btn.getClass().getMethods()) {
                        if (m.getName().equals("action") && m.getParameterCount() == 1) {
                            Object clickAction = customClickM.invoke(null, actionKey);
                            m.invoke(btn, clickAction);
                            break;
                        }
                    }
                    buttonList.add(btn.getClass().getMethod("build").invoke(btn));
                    callbackList.add(b.getCallback());
                }
            }

            if (exitButton != null) {
                String actionKey = "btn_exit";
                Object btn = btnActionM.invoke(null, exitButton.getLabel(), actionKey);
                for (Method m : btn.getClass().getMethods()) {
                    if (m.getName().equals("action") && m.getParameterCount() == 1) {
                        Object clickAction = customClickM.invoke(null, actionKey);
                        m.invoke(btn, clickAction);
                        break;
                    }
                }
                buttonList.add(btn.getClass().getMethod("build").invoke(btn));
                callbackList.add(exitButton.getCallback());
            }

            Object noticeType = null;
            for (Method m : dialogTypesClass.getMethods()) {
                if (m.getName().equals("notice") && m.getParameterCount() == 2) {
                    noticeType = m.invoke(null, buttonList, Math.max(1, columns));
                    break;
                }
            }
            if (noticeType == null) return false;

            InvocationHandler handler = (proxy, method, args) -> {
                if (method.getName().equals("onCustomClick")) {
                    String clickedKey = (String) args[1];
                    if (clickedKey != null) {
                        if (clickedKey.startsWith("btn_")) {
                            if (clickedKey.equals("btn_exit")) {
                                if (exitButton != null && exitButton.getCallback() != null) {
                                    Bukkit.getScheduler().runTask(plugin, exitButton.getCallback());
                                }
                            } else {
                                try {
                                    int index = Integer.parseInt(clickedKey.replace("btn_", ""));
                                    if (index >= 0 && index < callbackList.size()) {
                                        Runnable r = callbackList.get(index);
                                        if (r != null) {
                                            Bukkit.getScheduler().runTask(plugin, r);
                                        }
                                    }
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    }
                }
                return null;
            };

            Object responseProxy = Proxy.newProxyInstance(
                    dialogResponseHandlerClass.getClassLoader(),
                    new Class<?>[]{dialogResponseHandlerClass},
                    handler
            );

            Method createWrapped = null;
            for (Method m : wrappedDialogBuilderClass.getMethods()) {
                if (m.getName().equals("create") && m.getParameterCount() == 3) {
                    createWrapped = m;
                    break;
                }
            }
            if (createWrapped == null) return false;

            Object wrappedBuilder = createWrapped.invoke(null, base, noticeType, responseProxy);
            Object wrappedDialog = wrappedBuilder.getClass().getMethod("build").invoke(wrappedBuilder);

            for (Method m : dialogsClass.getMethods()) {
                if (m.getName().equals("show") && m.getParameterCount() == 2) {
                    m.invoke(null, player, wrappedDialog);
                    return true;
                }
            }
            return false;
        } catch (Throwable t) {
            return false;
        }
    }
}
