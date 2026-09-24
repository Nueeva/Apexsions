package com.apexsions.core.util;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerResolverTest {

    private MockedStatic<Bukkit> mockedBukkit;
    private Server server;

    @BeforeEach
    void setUp() {
        mockedBukkit = mockStatic(Bukkit.class);
        server = mock(Server.class);
        mockedBukkit.when(Bukkit::getServer).thenReturn(server);
    }

    @AfterEach
    void tearDown() {
        mockedBukkit.close();
    }

    @Test
    void testStripBedrockPrefix() {
        assertEquals("Kingambit", PlayerResolver.stripBedrockPrefix(".Kingambit"));
        assertEquals("Kingambit", PlayerResolver.stripBedrockPrefix("*Kingambit"));
        assertEquals("Kingambit", PlayerResolver.stripBedrockPrefix("_Kingambit"));
        assertEquals("Kingambit", PlayerResolver.stripBedrockPrefix("Kingambit"));
        assertEquals("", PlayerResolver.stripBedrockPrefix(""));
        assertEquals("", PlayerResolver.stripBedrockPrefix(null));
    }

    @Test
    void testResolveOnlineFuzzyBedrockMatch() {
        Player bedrockPlayer = mock(Player.class);
        when(bedrockPlayer.getName()).thenReturn(".Kingambit");
        when(bedrockPlayer.isOnline()).thenReturn(true);
        when(bedrockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());

        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn(List.of(bedrockPlayer));

        // 1. Partial query 'king' matches '.Kingambit'
        Player resolved = PlayerResolver.resolveOnline("king");
        assertNotNull(resolved);
        assertEquals(".Kingambit", resolved.getName());

        // 2. Exact stripped query 'kingambit' matches '.Kingambit'
        Player resolvedExact = PlayerResolver.resolveOnline("kingambit");
        assertNotNull(resolvedExact);
        assertEquals(".Kingambit", resolvedExact.getName());

        // 3. Exact raw query '.Kingambit' matches
        Player resolvedRaw = PlayerResolver.resolveOnline(".Kingambit");
        assertNotNull(resolvedRaw);
        assertEquals(".Kingambit", resolvedRaw.getName());

        // 4. Substring query 'gambit' matches
        Player resolvedSub = PlayerResolver.resolveOnline("gambit");
        assertNotNull(resolvedSub);
        assertEquals(".Kingambit", resolvedSub.getName());
    }

    @Test
    void testCompletePlayerNames() {
        Player bedrockPlayer = mock(Player.class);
        when(bedrockPlayer.getName()).thenReturn(".Kingambit");
        when(bedrockPlayer.isOnline()).thenReturn(true);

        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn(List.of(bedrockPlayer));

        List<String> completions = PlayerResolver.completePlayerNames("kin");
        assertTrue(completions.contains(".Kingambit"));
        assertTrue(completions.contains("Kingambit"));
    }
}
