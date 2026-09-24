package com.apexsions.core.sleep;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.config.ConfigManager;
import net.kyori.adventure.text.Component;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SleepManagerTest {

    private ApexsionsCorePlugin plugin;
    private ConfigManager configManager;
    private SleepManager sleepManager;

    @BeforeEach
    void setUp() {
        plugin = mock(ApexsionsCorePlugin.class);
        configManager = mock(ConfigManager.class);
        when(plugin.getConfigManager()).thenReturn(configManager);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("SleepManagerTest"));

        when(configManager.isSleepEnabled()).thenReturn(true);
        when(configManager.isSinglePlayerSleep()).thenReturn(true);
        when(configManager.isSleepBroadcast()).thenReturn(true);
        when(configManager.isSleepMorningBroadcast()).thenReturn(true);
        when(configManager.isSleepClearWeather()).thenReturn(true);
        when(configManager.getSleepBroadcastMessage()).thenReturn("<yellow>%player% sedang tidur.</yellow>");
        when(configManager.getSleepMorningMessage()).thenReturn("<yellow>Matahari terbit!</yellow>");

        sleepManager = new SleepManager(plugin);
    }

    @Test
    void testApplyGameruleOverworld() {
        World world = mock(World.class);
        when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);

        sleepManager.applyGamerule(world);

        verify(world, times(1)).setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 0);
    }

    @Test
    void testApplyGameruleNetherOrEndIgnored() {
        World nether = mock(World.class);
        when(nether.getEnvironment()).thenReturn(World.Environment.NETHER);

        World end = mock(World.class);
        when(end.getEnvironment()).thenReturn(World.Environment.THE_END);

        sleepManager.applyGamerule(nether);
        sleepManager.applyGamerule(end);

        verify(nether, never()).setGameRule(any(), any());
        verify(end, never()).setGameRule(any(), any());
    }

    @Test
    void testOnPlayerSleepBroadcast() {
        World world = mock(World.class);
        when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
        when(world.getUID()).thenReturn(UUID.randomUUID());

        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Steve");
        when(player.getWorld()).thenReturn(world);

        sleepManager.onPlayerSleep(player);

        verify(world, times(1)).sendMessage(any(Component.class));
    }

    @Test
    void testOnNightSkipClearWeatherAndBroadcast() {
        World world = mock(World.class);
        when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
        when(world.getUID()).thenReturn(UUID.randomUUID());
        when(world.hasStorm()).thenReturn(true);
        when(world.isThundering()).thenReturn(true);

        sleepManager.onNightSkip(world);

        verify(world, times(1)).setStorm(false);
        verify(world, times(1)).setThundering(false);
        verify(world, times(1)).sendMessage(any(Component.class));
    }
}
