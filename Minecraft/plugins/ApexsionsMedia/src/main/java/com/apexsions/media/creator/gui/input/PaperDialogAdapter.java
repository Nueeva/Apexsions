package com.apexsions.media.creator.gui.input;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

public class PaperDialogAdapter {

    private static Boolean dialogSupported = null;
    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static boolean isSupported() {
        if (dialogSupported == null) {
            try {
                Class.forName("io.papermc.paper.dialog.Dialog");
                dialogSupported = true;
            } catch (ClassNotFoundException e) {
                dialogSupported = false;
            }
        }
        return dialogSupported;
    }

    public static boolean showInput(Plugin plugin, Player player, String title, String prompt, String defaultText, Consumer<String> onInput, Runnable onCancel) {
        if (!isSupported()) return false;

        try {
            Class<?> dialogClass = Class.forName("io.papermc.paper.dialog.Dialog");
            Class<?> dialogBaseClass = null;
            try {
                dialogBaseClass = Class.forName("io.papermc.paper.registry.data.dialog.DialogBase");
            } catch (ClassNotFoundException ignored) {
                dialogBaseClass = Class.forName("io.papermc.paper.dialog.DialogBase");
            }

            Class<?> dialogBodyClass = null;
            try {
                dialogBodyClass = Class.forName("io.papermc.paper.registry.data.dialog.DialogBody");
            } catch (ClassNotFoundException ignored) {
                dialogBodyClass = Class.forName("io.papermc.paper.dialog.DialogBody");
            }

            Class<?> dialogInputClass = null;
            try {
                dialogInputClass = Class.forName("io.papermc.paper.registry.data.dialog.DialogInput");
            } catch (ClassNotFoundException ignored) {
                dialogInputClass = Class.forName("io.papermc.paper.dialog.DialogInput");
            }

            Class<?> dialogTypeClass = null;
            try {
                dialogTypeClass = Class.forName("io.papermc.paper.registry.data.dialog.DialogType");
            } catch (ClassNotFoundException ignored) {
                dialogTypeClass = Class.forName("io.papermc.paper.dialog.DialogType");
            }

            Class<?> actionButtonClass = null;
            try {
                actionButtonClass = Class.forName("io.papermc.paper.registry.data.dialog.ActionButton");
            } catch (ClassNotFoundException ignored) {
                actionButtonClass = Class.forName("io.papermc.paper.dialog.ActionButton");
            }

            Class<?> dialogActionClass = null;
            try {
                dialogActionClass = Class.forName("io.papermc.paper.registry.data.dialog.DialogAction");
            } catch (ClassNotFoundException ignored) {
                dialogActionClass = Class.forName("io.papermc.paper.dialog.DialogAction");
            }

            Component titleComp = mm.deserialize(title != null ? title : "<gold><b>INPUT KREATOR</b></gold>");
            Component promptComp = mm.deserialize(prompt != null ? prompt : "<yellow>Masukkan URL / Link:</yellow>");

            Method baseBuilderMethod = dialogBaseClass.getMethod("builder", Component.class);
            Object baseBuilder = baseBuilderMethod.invoke(null, titleComp);

            Method plainMessageMethod = dialogBodyClass.getMethod("plainMessage", Component.class);
            Object bodyItem = plainMessageMethod.invoke(null, promptComp);
            baseBuilder.getClass().getMethod("body", List.class).invoke(baseBuilder, List.of(bodyItem));

            Method textInputMethod = dialogInputClass.getMethod("text", String.class, Component.class);
            Object textInputBuilder = textInputMethod.invoke(null, "input_key", Component.text("URL"));
            Object textInput = textInputBuilder.getClass().getMethod("build").invoke(textInputBuilder);
            baseBuilder.getClass().getMethod("inputs", List.class).invoke(baseBuilder, List.of(textInput));

            Object dialogBase = baseBuilder.getClass().getMethod("build").invoke(baseBuilder);

            Method customClickMethod = dialogActionClass.getMethod("customClick", java.util.function.BiConsumer.class);
            java.util.function.BiConsumer<Object, Object> clickHandler = (view, audience) -> {
                try {
                    Method getTextMethod = view.getClass().getMethod("getText", String.class);
                    String result = (String) getTextMethod.invoke(view, "input_key");
                    final String val = (result != null) ? result.trim() : "";
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        try {
                            onInput.accept(val);
                        } catch (Exception ex) {
                            player.sendMessage("§cError: " + ex.getMessage());
                        }
                    });
                } catch (Exception e) {
                    if (onCancel != null) Bukkit.getScheduler().runTask(plugin, onCancel);
                }
            };
            Object action = customClickMethod.invoke(null, clickHandler);

            Method createButtonMethod = actionButtonClass.getMethod("create", Component.class, Component.class, int.class, dialogActionClass);
            Object okButton = createButtonMethod.invoke(null, Component.text("✔ OK"), null, 200, action);

            Method noticeMethod = dialogTypeClass.getMethod("notice", actionButtonClass);
            Object dialogType = noticeMethod.invoke(null, okButton);

            final Class<?> finalBaseClass = dialogBaseClass;
            final Class<?> finalTypeClass = dialogTypeClass;
            Method createDialogMethod = dialogClass.getMethod("create", Consumer.class);
            Consumer<Object> dialogBuilderConsumer = builder -> {
                try {
                    Object empty = builder.getClass().getMethod("empty").invoke(builder);
                    empty.getClass().getMethod("base", finalBaseClass).invoke(empty, dialogBase);
                    empty.getClass().getMethod("type", finalTypeClass).invoke(empty, dialogType);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };

            Object dialog = createDialogMethod.invoke(null, dialogBuilderConsumer);

            Method showDialogMethod = player.getClass().getMethod("showDialog", dialogClass);
            showDialogMethod.invoke(player, dialog);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
