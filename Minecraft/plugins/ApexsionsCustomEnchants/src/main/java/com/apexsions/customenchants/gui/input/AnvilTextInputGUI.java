package com.apexsions.customenchants.gui.input;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @deprecated Replaced by {@link CustomInputTextGUI}.
 * Retained for backward compatibility. Automatically forwards to {@link CustomInputTextGUI}.
 * NEVER opens an anvil inventory.
 */
@Deprecated
public class AnvilTextInputGUI implements InventoryHolder {

    private final Plugin plugin;
    private final Player player;
    private final String title;
    private final String prompt;
    private final String defaultText;
    private final Consumer<String> onInput;
    private final Runnable onCancel;

    public AnvilTextInputGUI(Plugin plugin, Player player, String title, String prompt, String defaultText,
                             Consumer<String> onInput, Runnable onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.prompt = prompt;
        this.defaultText = defaultText;
        this.onInput = onInput;
        this.onCancel = onCancel;
    }

    public void open() {
        CustomInputTextGUI.open(plugin, player, title, prompt, defaultText, onInput, onCancel);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
