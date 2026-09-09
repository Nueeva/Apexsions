package com.apexsions.crates.editor.crate;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import com.apexsions.crates.CratesPlugin;
import com.apexsions.crates.config.Lang;
import com.apexsions.crates.crate.CrateManager;
import com.apexsions.crates.crate.RarityDialogs;
import com.apexsions.crates.crate.impl.Rarity;
import com.apexsions.crates.dialog.DialogRegistry;
import net.kyori.adventure.text.minimessage.MiniMessage;
import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.IconLocale;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.Comparator;
import java.util.stream.IntStream;

import static com.apexsions.crates.Placeholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class RarityListMenu extends LinkedMenu<CratesPlugin, CrateManager> implements Filled<Rarity>, LangContainer {

    private static final IconLocale LOCALE_RARITY = LangEntry.iconBuilder("Editor.Button.Rarities.Rarity")
        .rawName(RARITY_NAME)
        .appendCurrent("ID",     RARITY_ID)
        .appendCurrent("Weight", RARITY_WEIGHT)
        .appendCurrent("Chance", RARITY_ROLL_CHANCE + "%").br()
        .appendClick("Click to edit")
        .appendClick(RED.wrap("[Q / Drop]") + " to delete")
        .build();

    private static final IconLocale LOCALE_CREATE = LangEntry.iconBuilder("Editor.Button.Rarities.Create")
        .accentColor(GREEN)
        .name("Add New Rarity")
        .appendInfo("Create a brand new rarity!").br()
        .appendClick("Click to create")
        .build();

    private final DialogRegistry dialogs;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public RarityListMenu(@NotNull CratesPlugin plugin, @NotNull DialogRegistry dialogs) {
        super(plugin, MenuType.GENERIC_9X5, Lang.EDITOR_TITLE_RARITY_LIST.text());
        this.dialogs = dialogs;
        this.plugin.injectLang(this);

        this.addItem(MenuItem.buildReturn(this, 40, (viewer, event) -> {
            this.runNextTick(() -> this.plugin.getEditorManager().openEditor(viewer.getPlayer()));
        }));

        this.addItem(MenuItem.buildNextPage(this, 44));
        this.addItem(MenuItem.buildPreviousPage(this, 36));
        this.addItem(MenuItem.background(Material.BLACK_STAINED_GLASS_PANE, IntStream.range(36, 45).toArray()));
        this.addItem(MenuItem.background(Material.GRAY_STAINED_GLASS_PANE,  IntStream.range(0, 36).toArray()));

        this.addItem(Material.NETHER_STAR, LOCALE_CREATE, 42, (viewer, event, manager) -> {
            this.dialogs.show(viewer.getPlayer(), RarityDialogs.RARITY_CREATION, manager, () -> this.flush(viewer.getPlayer()));
        });
    }

    @Override
    @NotNull
    public MenuFiller<Rarity> createFiller(@NotNull MenuViewer viewer) {
        CrateManager manager = this.getLink(viewer);

        var autoFill = MenuFiller.builder(this);
        autoFill.setSlots(IntStream.range(0, 36).toArray());
        autoFill.setItems(manager.getRarities().stream()
            .sorted(Comparator.comparingDouble(Rarity::getWeight).reversed())
            .toList()
        );
        autoFill.setItemCreator(rarity -> {
            Material mat = switch (rarity.getId()) {
                case "common"    -> Material.WHITE_STAINED_GLASS;
                case "uncommon"  -> Material.LIME_STAINED_GLASS;
                case "rare"      -> Material.LIGHT_BLUE_STAINED_GLASS;
                case "epic"      -> Material.PURPLE_STAINED_GLASS;
                case "legendary" -> Material.ORANGE_STAINED_GLASS;
                case "mythic"    -> Material.RED_STAINED_GLASS;
                case "secret"    -> Material.YELLOW_STAINED_GLASS;
                default          -> Material.GRAY_STAINED_GLASS;
            };

            return NightItem.fromType(mat)
                .localized(LOCALE_RARITY)
                .replacement(replacer -> replacer.replace(rarity.replacePlaceholders()));
        });
        autoFill.setItemClick(rarity -> (viewer1, event) -> {
            if (event.getClick() == ClickType.DROP) {
                // Delete confirmation — require DROP key
                if (manager.getRarities().size() <= 1) {
                    viewer1.getPlayer().sendActionBar(MM.deserialize("<red>✖ Cannot delete the last rarity!</red>"));
                    return;
                }
                manager.removeRarity(rarity.getId());
                viewer1.getPlayer().sendActionBar(MM.deserialize("<green>✔ Rarity '" + rarity.getId() + "' deleted.</green>"));
                this.runNextTick(() -> this.flush(viewer1.getPlayer()));
                return;
            }
            // Edit
            this.dialogs.show(viewer1.getPlayer(), RarityDialogs.RARITY_EDIT, rarity, () -> {
                manager.saveRarities();
                this.flush(viewer1.getPlayer());
            });
        });

        return autoFill.build();
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {}
}
