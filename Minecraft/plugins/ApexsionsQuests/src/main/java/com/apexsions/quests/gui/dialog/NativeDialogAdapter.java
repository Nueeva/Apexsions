package com.apexsions.quests.gui.dialog;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Official Minecraft 1.21.6+ Native Dialog Adapter.
 * Integrates directly with Paper's official Dialog API (io.papermc.paper.dialog.*)
 * and Bedrock Native Forms (Geyser / Cumulus) for mobile players.
 */
public class NativeDialogAdapter {

    private static Boolean paper1216Supported = null;
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
        }
        return null;
    }

    /**
     * Checks if the official Paper Minecraft 1.21.6+ Native Dialog API is supported on this server.
     */
    public static boolean isPaper1216Supported() {
        if (paper1216Supported == null) {
            Class<?> dialogClass = findClass(
                    "io.papermc.paper.dialog.Dialog",
                    "io.papermc.paper.registry.data.dialog.Dialog"
            );
            boolean hasShowDialog = false;
            try {
                for (Method m : Player.class.getMethods()) {
                    if (m.getName().equals("showDialog") && m.getParameterCount() == 1) {
                        hasShowDialog = true;
                        break;
                    }
                }
            } catch (Throwable ignored) {}

            paper1216Supported = dialogClass != null && hasShowDialog;
        }
        return paper1216Supported;
    }

    public static boolean isSupported() {
        return isPaper1216Supported() || BedrockFormAdapter.isFloodgatePresent();
    }

    /**
     * Opens an official Minecraft 1.21.6+ Multi-Action Native Dialog,
     * or a Bedrock Native Form if the player is connecting via Geyser/Floodgate.
     */
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

        // Close any active inventory so screen is clear
        try {
            if (player.getOpenInventory().getTopInventory().getType() != org.bukkit.event.inventory.InventoryType.CRAFTING) {
                player.closeInventory();
            }
        } catch (Throwable ignored) {}

        // 1. Bedrock Floodgate / Geyser Native Form (SimpleForm)
        if (BedrockFormAdapter.isBedrockPlayer(player)) {
            if (BedrockFormAdapter.openMultiActionForm(plugin, player, title, description, buttons, exitButton)) {
                return true;
            }
        }

        // 2. Official Paper Minecraft 1.21.6+ Native Dialog System
        if (isPaper1216Supported()) {
            if (showPaper1216Dialog(plugin, player, title, description, buttons, exitButton, columns)) {
                return true;
            }
        }

        // 3. Bedrock Form fallback if Floodgate is present
        if (BedrockFormAdapter.isFloodgatePresent() && BedrockFormAdapter.openMultiActionForm(plugin, player, title, description, buttons, exitButton)) {
            return true;
        }

        // 4. Elegant Interactive Fallback Menu in Chat
        sendInteractiveChatFallback(plugin, player, title, description, buttons, exitButton);
        return true;
    }

    /**
     * Constructs and sends the official Minecraft 1.21.6+ Native Dialog using Paper's Dialog API.
     */
    private static boolean showPaper1216Dialog(
            Plugin plugin,
            Player player,
            String title,
            String description,
            List<DialogButtonData> buttons,
            DialogButtonData exitButton,
            int columns
    ) {
        try {
            Class<?> dialogClass = findClass(
                    "io.papermc.paper.dialog.Dialog",
                    "io.papermc.paper.registry.data.dialog.Dialog"
            );
            Class<?> dialogBaseClass = findClass(
                    "io.papermc.paper.registry.data.dialog.DialogBase",
                    "io.papermc.paper.dialog.DialogBase"
            );
            Class<?> dialogBodyClass = findClass(
                    "io.papermc.paper.registry.data.dialog.body.DialogBody",
                    "io.papermc.paper.dialog.body.DialogBody",
                    "io.papermc.paper.dialog.DialogBody"
            );
            Class<?> actionButtonClass = findClass(
                    "io.papermc.paper.registry.data.dialog.ActionButton",
                    "io.papermc.paper.dialog.ActionButton"
            );
            Class<?> dialogActionClass = findClass(
                    "io.papermc.paper.registry.data.dialog.action.DialogAction",
                    "io.papermc.paper.dialog.action.DialogAction",
                    "io.papermc.paper.dialog.DialogAction"
            );
            Class<?> dialogTypeClass = findClass(
                    "io.papermc.paper.registry.data.dialog.type.DialogType",
                    "io.papermc.paper.dialog.type.DialogType",
                    "io.papermc.paper.dialog.DialogType"
            );

            if (dialogClass == null || dialogBaseClass == null || dialogBodyClass == null
                    || actionButtonClass == null || dialogActionClass == null || dialogTypeClass == null) {
                return false;
            }

            // A. Build DialogBase (Title & Message Body)
            Component titleComp = mm.deserialize((title != null && !title.isBlank()) ? title : "<gold><bold>APEXSIONS</bold></gold>");
            Method baseBuilderM = dialogBaseClass.getMethod("builder", Component.class);
            Object baseBuilder = baseBuilderM.invoke(null, titleComp);

            String cleanDesc = (description != null && !description.isBlank()) ? description : "";
            Component descComp = mm.deserialize(cleanDesc);
            Method plainMsgM = dialogBodyClass.getMethod("plainMessage", Component.class);
            Object bodyObj = plainMsgM.invoke(null, descComp);

            for (Method m : baseBuilder.getClass().getMethods()) {
                if (m.getName().equals("body") && m.getParameterCount() == 1 && List.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    m.invoke(baseBuilder, List.of(bodyObj));
                    break;
                }
            }
            Object dialogBase = baseBuilder.getClass().getMethod("build").invoke(baseBuilder);

            // B. Build ActionButtons
            List<Object> buttonList = new ArrayList<>();
            if (buttons != null) {
                for (DialogButtonData b : buttons) {
                    Object btn = createPaperActionButton(plugin, actionButtonClass, dialogActionClass, b, 160);
                    if (btn != null) {
                        buttonList.add(btn);
                    }
                }
            }

            Object exitButtonObj = null;
            if (exitButton != null) {
                exitButtonObj = createPaperActionButton(plugin, actionButtonClass, dialogActionClass, exitButton, 120);
            }

            // C. Build DialogType (multiAction)
            Object dialogType = null;
            for (Method m : dialogTypeClass.getMethods()) {
                if (m.getName().equals("multiAction")) {
                    Class<?>[] pts = m.getParameterTypes();
                    if (pts.length == 3 && List.class.isAssignableFrom(pts[0]) && pts[1].isAssignableFrom(actionButtonClass) && (pts[2] == int.class || pts[2] == Integer.class)) {
                        dialogType = m.invoke(null, buttonList, exitButtonObj, Math.max(1, columns));
                        break;
                    } else if (pts.length == 2 && List.class.isAssignableFrom(pts[0]) && pts[1].isAssignableFrom(actionButtonClass)) {
                        dialogType = m.invoke(null, buttonList, exitButtonObj);
                        break;
                    } else if (pts.length == 2 && List.class.isAssignableFrom(pts[0]) && (pts[1] == int.class || pts[1] == Integer.class)) {
                        dialogType = m.invoke(null, buttonList, Math.max(1, columns));
                        break;
                    } else if (pts.length == 1 && List.class.isAssignableFrom(pts[0])) {
                        dialogType = m.invoke(null, buttonList);
                        break;
                    }
                }
            }

            if (dialogType == null) {
                // Fallback to notice if multiAction method wasn't matched
                for (Method m : dialogTypeClass.getMethods()) {
                    if (m.getName().equals("notice") && m.getParameterCount() == 1 && m.getParameterTypes()[0].isAssignableFrom(actionButtonClass)) {
                        Object firstBtn = exitButtonObj != null ? exitButtonObj : (!buttonList.isEmpty() ? buttonList.get(0) : null);
                        if (firstBtn != null) {
                            dialogType = m.invoke(null, firstBtn);
                            break;
                        }
                    }
                }
            }

            if (dialogType == null) return false;

            // D. Construct Dialog via Dialog.create(Consumer<Dialog.Builder>)
            final Object finalBase = dialogBase;
            final Object finalType = dialogType;

            Consumer<Object> builderConsumer = builderObj -> {
                try {
                    for (Method m : builderObj.getClass().getMethods()) {
                        if (m.getName().equals("base") && m.getParameterCount() == 1 && m.getParameterTypes()[0].isAssignableFrom(dialogBaseClass)) {
                            m.invoke(builderObj, finalBase);
                        } else if (m.getName().equals("type") && m.getParameterCount() == 1 && m.getParameterTypes()[0].isAssignableFrom(dialogTypeClass)) {
                            m.invoke(builderObj, finalType);
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            };

            Method createDialogM = dialogClass.getMethod("create", Consumer.class);
            Object dialogObj = createDialogM.invoke(null, builderConsumer);

            // E. Show Dialog to Player: player.showDialog(dialogObj)
            for (Method m : player.getClass().getMethods()) {
                if (m.getName().equals("showDialog") && m.getParameterCount() == 1) {
                    m.invoke(player, dialogObj);
                    return true;
                }
            }

            return false;
        } catch (Throwable t) {
            // Fail gracefully to chat fallback
            return false;
        }
    }

    private static Object createPaperActionButton(
            Plugin plugin,
            Class<?> actionButtonClass,
            Class<?> dialogActionClass,
            DialogButtonData b,
            int width
    ) {
        try {
            Component labelComp = mm.deserialize(b.getLabel());
            Component tooltipComp = (b.getTooltip() != null && !b.getTooltip().isBlank())
                    ? mm.deserialize(b.getTooltip())
                    : Component.empty();

            // Create ClickEvent
            ClickEvent clickEvent;
            if (b.getCommand() != null && !b.getCommand().isBlank()) {
                clickEvent = ClickEvent.runCommand(b.getCommand());
            } else if (b.getCallback() != null) {
                clickEvent = ClickEvent.callback(audience -> {
                    Bukkit.getScheduler().runTask(plugin, b.getCallback());
                });
            } else {
                clickEvent = ClickEvent.runCommand("/apexsionsnoop");
            }

            // Create DialogAction via staticAction(ClickEvent) or customClick
            Object actionObj = null;
            for (Method m : dialogActionClass.getMethods()) {
                if (m.getName().equals("staticAction") && m.getParameterCount() == 1 && m.getParameterTypes()[0].isAssignableFrom(ClickEvent.class)) {
                    actionObj = m.invoke(null, clickEvent);
                    break;
                }
            }

            if (actionObj == null) {
                for (Method m : dialogActionClass.getMethods()) {
                    if (m.getName().equals("commandTemplate") && m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class) {
                        String cmd = b.getCommand() != null ? b.getCommand() : "say click";
                        actionObj = m.invoke(null, cmd);
                        break;
                    }
                }
            }

            if (actionObj == null) return null;

            // Instantiate ActionButton: ActionButton.create(...)
            for (Method m : actionButtonClass.getMethods()) {
                if (m.getName().equals("create")) {
                    Class<?>[] pts = m.getParameterTypes();
                    if (pts.length == 4 && pts[0].isAssignableFrom(Component.class) && pts[1].isAssignableFrom(Component.class) && (pts[2] == int.class || pts[2] == Integer.class) && pts[3].isAssignableFrom(dialogActionClass)) {
                        return m.invoke(null, labelComp, tooltipComp, width, actionObj);
                    } else if (pts.length == 3 && pts[0].isAssignableFrom(Component.class) && (pts[1] == int.class || pts[1] == Integer.class) && pts[2].isAssignableFrom(dialogActionClass)) {
                        return m.invoke(null, labelComp, width, actionObj);
                    } else if (pts.length == 2 && pts[0].isAssignableFrom(Component.class) && pts[1].isAssignableFrom(dialogActionClass)) {
                        return m.invoke(null, labelComp, actionObj);
                    }
                }
            }

            return null;
        } catch (Throwable t) {
            return null;
        }
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
