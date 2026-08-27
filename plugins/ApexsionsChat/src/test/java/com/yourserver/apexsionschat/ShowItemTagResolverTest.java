package com.yourserver.apexsionschat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShowItemTagResolverTest {

    @Test
    public void testSafeTemplateFormattingWithoutTagLeakage() {
        String template = "<gray>[<yellow>Lv. <level> <gold><title></gold></yellow>]</gray> <gray>[<rank>]</gray> <kingdom> <white><player></white> <dark_gray>»</dark_gray> <white><message></white>";

        // Item showcase component simulated
        Component itemComp = Component.text("[Diamond Sword x1]", NamedTextColor.GOLD);
        Component messageComp = Component.text("Look at my ").append(itemComp);

        Component result = MiniMessage.miniMessage().deserialize(
                template,
                Placeholder.parsed("level", "25"),
                Placeholder.parsed("title", "Emperor"),
                Placeholder.parsed("rank", "Champion"),
                Placeholder.parsed("kingdom", "<gold>[Zenithar]</gold>"),
                Placeholder.parsed("player", "Arthur"),
                Placeholder.component("message", messageComp)
        );

        String plain = PlainTextComponentSerializer.plainText().serialize(result);

        // Verify plain text contents
        assertTrue(plain.contains("[Lv. 25 Emperor]"));
        assertTrue(plain.contains("[Champion]"));
        assertTrue(plain.contains("[Zenithar]"));
        assertTrue(plain.contains("Arthur » Look at my [Diamond Sword x1]"));

        // Verify NO raw tags leaked into plain text
        assertFalse(plain.contains("<white>"));
        assertFalse(plain.contains("</white>"));
        assertFalse(plain.contains("(/white)"));
        assertFalse(plain.contains("<gold>"));
        assertFalse(plain.contains("</gold>"));
    }

    @Test
    public void testUntrustedUserTagsDoNotBreakFormatting() {
        String template = "<dark_gray>»</dark_gray> <white><message></white>";
        // Malicious user message trying to inject unmatched closing and color tags
        Component maliciousComp = Component.text("</white><red>hacked<gradient:#ff0000:#00ff00>");

        Component result = MiniMessage.miniMessage().deserialize(
                template,
                Placeholder.component("message", maliciousComp)
        );

        String plain = PlainTextComponentSerializer.plainText().serialize(result);
        assertTrue(plain.contains("» </white><red>hacked<gradient:#ff0000:#00ff00>"));
        assertFalse(plain.contains("(/white)"));
    }
}
