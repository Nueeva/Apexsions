package com.yourserver.apexsionschat.chat;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ItemShowcaseService {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    // Cache showcase snapshots by unique showcase ID for 5 minutes
    private final Cache<String, ItemStack> showcaseCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(500)
            .build();

    public ItemShowcaseService(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    public String registerShowcase(ItemStack item) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        showcaseCache.put(id, item.clone());
        return id;
    }

    public Optional<ItemStack> getShowcaseItem(String showcaseId) {
        if (showcaseId == null) return Optional.empty();
        return Optional.ofNullable(showcaseCache.getIfPresent(showcaseId));
    }

    public Component buildShowcaseComponent(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return Component.empty();
        }

        String showcaseId = registerShowcase(item);
        Component itemNameComponent = getItemNameComponent(item);
        int amount = item.getAmount();

        String template = plugin.getConfigManager().getMainConfig()
                .getString("item-showcase.format", "<gray>[</gray><gold><item_name></gold> <yellow>x<amount></yellow><gray>]</gray>")
                .replace("{item_name}", "<item_name>")
                .replace("{amount}", "<amount>");

        Component base = miniMessage.deserialize(
                template,
                Placeholder.component("item_name", itemNameComponent),
                Placeholder.unparsed("amount", String.valueOf(amount))
        );

        // Hover tooltip with lore and enchants safely constructed
        Component hoverContent = buildHoverTooltip(item, itemNameComponent, amount);

        return base
                .hoverEvent(HoverEvent.showText(hoverContent))
                .clickEvent(ClickEvent.runCommand("/showitem " + showcaseId));
    }

    public Component getItemNameComponent(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            Component customName = item.getItemMeta().displayName();
            if (customName != null) {
                return customName;
            }
        }
        String typeName = item.getType().name().replace("_", " ").toLowerCase();
        return Component.text(capitalize(typeName), NamedTextColor.GOLD);
    }

    private Component buildHoverTooltip(ItemStack item, Component itemNameComponent, int amount) {
        Component tooltip = itemNameComponent
                .decorate(TextDecoration.BOLD)
                .append(Component.text(" (x" + amount + ")", NamedTextColor.YELLOW))
                .append(Component.newline())
                .append(Component.text("Type: ", NamedTextColor.GRAY))
                .append(Component.text(item.getType().name(), NamedTextColor.WHITE));

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasEnchants()) {
                tooltip = tooltip.append(Component.newline()).append(Component.text("Enchantments:", NamedTextColor.AQUA));
                for (var entry : meta.getEnchants().entrySet()) {
                    String enchName = entry.getKey().getKey().getKey().replace("_", " ");
                    tooltip = tooltip.append(Component.newline())
                            .append(Component.text(" • " + capitalize(enchName) + " " + entry.getValue(), NamedTextColor.DARK_AQUA));
                }
            }
            if (meta.hasLore() && meta.lore() != null) {
                tooltip = tooltip.append(Component.newline()).append(Component.text("Lore:", NamedTextColor.GRAY));
                for (Component l : meta.lore()) {
                    tooltip = tooltip.append(Component.newline())
                            .append(Component.space())
                            .append(l.decorate(TextDecoration.ITALIC));
                }
            }
        }

        tooltip = tooltip.append(Component.newline())
                .append(Component.text("⚡ Click to inspect in GUI!", NamedTextColor.YELLOW));

        return tooltip;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                result.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return result.toString().trim();
    }
}
