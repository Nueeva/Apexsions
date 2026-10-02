package com.apexsions.battlepass.progression;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.player.PlayerData;
import com.apexsions.battlepass.reward.RewardManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Regression test untuk C-5: {@code checkLevelUp} tidak boleh hang ketika
 * {@code required-xp} salah config (<= 0). Player = null agar tidak ada
 * Bukkit API yang tersentuh (cabang event/message di-skip).
 */
class BattlePassXpServiceTest {

    private RewardManager rewardManager;
    private BattlePassXpService xpService;

    @BeforeEach
    void setUp() {
        ApexsionsBattlepass plugin = mock(ApexsionsBattlepass.class);
        rewardManager = mock(RewardManager.class);
        when(plugin.getRewardManager()).thenReturn(rewardManager);
        when(rewardManager.getMaxLevel()).thenReturn(10);
        xpService = new BattlePassXpService(plugin);
    }

    private static PlayerData dataAt(int level, int xp) {
        PlayerData data = new PlayerData(UUID.randomUUID(), 1);
        data.setLevel(level);
        data.setXp(xp);
        return data;
    }

    @Test
    void checkLevelUp_requiredXpZero_doesNotHang() {
        // C-5: required-xp: 0 — tanpa guard, while (xp >= 0) loop selamanya
        when(rewardManager.getRequiredXp(anyInt())).thenReturn(0);
        PlayerData data = dataAt(1, 500);

        assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> xpService.checkLevelUp(null, data));

        assertEquals(1, data.getLevel(), "level tidak boleh berubah saat required-xp invalid");
        assertEquals(500, data.getXp(), "xp tidak boleh berkurang saat required-xp invalid");
    }

    @Test
    void checkLevelUp_requiredXpNegative_doesNotHang() {
        when(rewardManager.getRequiredXp(anyInt())).thenReturn(-50);
        PlayerData data = dataAt(1, 500);

        assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> xpService.checkLevelUp(null, data));

        assertEquals(1, data.getLevel());
        assertEquals(500, data.getXp());
    }

    @Test
    void checkLevelUp_multipleLevelsAtOnce() {
        when(rewardManager.getRequiredXp(anyInt())).thenReturn(100);
        PlayerData data = dataAt(1, 350);

        xpService.checkLevelUp(null, data);

        assertEquals(4, data.getLevel(), "350 xp @ 100/level = naik 3 level");
        assertEquals(50, data.getXp(), "sisa xp setelah 3x level-up");
    }

    @Test
    void checkLevelUp_xpExactlyAtBoundary() {
        when(rewardManager.getRequiredXp(anyInt())).thenReturn(100);
        PlayerData data = dataAt(1, 100);

        xpService.checkLevelUp(null, data);

        assertEquals(2, data.getLevel(), "xp pas di batas harus naik 1 level");
        assertEquals(0, data.getXp(), "xp habis tepat setelah level-up");
    }

    @Test
    void checkLevelUp_xpBelowBoundary_noLevelUp() {
        when(rewardManager.getRequiredXp(anyInt())).thenReturn(100);
        PlayerData data = dataAt(1, 99);

        xpService.checkLevelUp(null, data);

        assertEquals(1, data.getLevel());
        assertEquals(99, data.getXp());
    }
}
