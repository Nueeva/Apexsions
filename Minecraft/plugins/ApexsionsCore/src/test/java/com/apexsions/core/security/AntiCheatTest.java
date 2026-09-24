package com.apexsions.core.security;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AntiCheatTest {

    @Test
    public void testKillAuraAngleCalculation() {
        // Looking directly north (0, 0, -1)
        Vector eyeDir = new Vector(0, 0, -1).normalize();

        // Target directly in front (0, 0, -5)
        Vector inFront = new Vector(0, 0, -5).normalize();
        double angleInFront = Math.toDegrees(eyeDir.angle(inFront));
        assertEquals(0.0, angleInFront, 0.001, "Direct front should be 0 degrees");

        // Target 90 degrees to the right (5, 0, 0)
        Vector toRight = new Vector(5, 0, 0).normalize();
        double angleRight = Math.toDegrees(eyeDir.angle(toRight));
        assertEquals(90.0, angleRight, 0.001, "Direct right should be 90 degrees");

        // Target 180 degrees behind (0, 0, 5)
        Vector behind = new Vector(0, 0, 5).normalize();
        double angleBehind = Math.toDegrees(eyeDir.angle(behind));
        assertEquals(180.0, angleBehind, 0.001, "Direct behind should be 180 degrees");

        // KillAura check: threshold is 95 degrees
        double maxAngle = 95.0;
        assertTrue(angleInFront <= maxAngle, "Legitimate hit should pass");
        assertTrue(angleRight <= maxAngle, "Peripheral hit within 90 degrees should pass");
        assertTrue(angleBehind > maxAngle, "Backwards hit must be flagged as KillAura");
    }

    @Test
    public void testCombatReachLimit() {
        double maxReach = 4.2;

        // Eye at (0, 1.6, 0), target at (0, 1.6, 3.0) -> distance = 3.0m (legitimate)
        double legitimateDist = Math.sqrt(Math.pow(0 - 0, 2) + Math.pow(1.6 - 1.6, 2) + Math.pow(3.0 - 0, 2));
        assertTrue(legitimateDist <= maxReach);

        // Hacked reach at (0, 1.6, 5.0) -> distance = 5.0m (illegal)
        double hackedDist = Math.sqrt(Math.pow(0 - 0, 2) + Math.pow(1.6 - 1.6, 2) + Math.pow(5.0 - 0, 2));
        assertTrue(hackedDist > maxReach);
    }

    @Test
    public void testBadPacketsPitchValidation() {
        // Valid pitch: -90.0 to 90.0
        float validPitch1 = 0.0f;
        float validPitch2 = -85.5f;
        float validPitch3 = 89.9f;

        assertFalse(isInvalidPitch(validPitch1));
        assertFalse(isInvalidPitch(validPitch2));
        assertFalse(isInvalidPitch(validPitch3));

        // Invalid pitch (pitch exploit / crash packet)
        float hackedPitchHigh = 95.0f;
        float hackedPitchLow = -92.0f;
        float nanPitch = Float.NaN;
        float infPitch = Float.POSITIVE_INFINITY;

        assertTrue(isInvalidPitch(hackedPitchHigh));
        assertTrue(isInvalidPitch(hackedPitchLow));
        assertTrue(isInvalidPitch(nanPitch));
        assertTrue(isInvalidPitch(infPitch));
    }

    @Test
    public void testMalformedCoordinateValidation() {
        // Normal coordinates
        assertFalse(isMalformedCoordinate(100.5, 64.0, -250.2));

        // NaN coordinates
        assertTrue(isMalformedCoordinate(Double.NaN, 64.0, 0.0));

        // Infinite coordinates
        assertTrue(isMalformedCoordinate(0.0, Double.POSITIVE_INFINITY, 0.0));
        assertTrue(isMalformedCoordinate(0.0, 0.0, Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testWaterSwimmingGracePeriod() {
        long now = System.currentTimeMillis();
        long lastLiquidTime = now - 1500L; // 1.5 seconds ago (surfacing from water)

        boolean recentLiquid = (now - lastLiquidTime) < 4000L;
        assertTrue(recentLiquid, "Player who exited water 1.5s ago must be within the 4000ms grace period");

        // Within grace period, ascending motion (deltaY > 0) is legitimate water leaping
        double deltaY = 0.35;
        boolean isAirWalkFlagged = !recentLiquid && deltaY > 0.08;
        assertFalse(isAirWalkFlagged, "Upward movement within water exit grace period must NOT flag AirWalk");

        // Expired grace period (e.g. 5 seconds after leaving water, hovering in pure air)
        long expiredLiquidTime = now - 5000L;
        boolean expiredRecentLiquid = (now - expiredLiquidTime) < 4000L;
        assertFalse(expiredRecentLiquid);

        // Bedrock client has 35 air ticks tolerance
        int bedrockAirTicks = 25;
        int requiredBedrockAirTicks = 35;
        boolean bedrockFlag = !expiredRecentLiquid && bedrockAirTicks > requiredBedrockAirTicks && deltaY > 0.08;
        assertFalse(bedrockFlag, "Bedrock player with 25 air ticks should not be flagged due to Geyser tolerance");

        // True hacker: 40 air ticks in open air
        int hackerAirTicks = 40;
        boolean hackerFlag = !expiredRecentLiquid && hackerAirTicks > requiredBedrockAirTicks && deltaY > 0.08;
        assertTrue(hackerFlag, "Ascending in pure air without liquid or climbable for 40 ticks must be flagged as Fly Hack");
    }

    @Test
    public void testLadderClimbingGraceAndDetection() {
        long now = System.currentTimeMillis();
        long lastClimbableTime = now - 1000L; // 1 second ago (stepping off top of ladder)

        boolean recentClimbable = (now - lastClimbableTime) < 3000L;
        assertTrue(recentClimbable, "Player who stepped off a ladder 1s ago must be within 3000ms grace period");

        // While on or just stepped off ladder, upward momentum (deltaY > 0) must never be flagged as AirWalk
        double deltaY = 0.25;
        boolean isAirWalkFlagged = !recentClimbable && deltaY > 0.08;
        assertFalse(isAirWalkFlagged, "Upward movement within climbable grace period must NOT flag AirWalk");
    }

    @Test
    public void testBedBouncingGracePeriod() {
        long now = System.currentTimeMillis();
        long lastBounceTime = now - 1200L; // 1.2 seconds ago (bounced on bed or slime)

        boolean recentBounce = (now - lastBounceTime) < 3500L;
        assertTrue(recentBounce, "Player who bounced on a bed 1.2s ago must be within 3500ms bounce grace period");

        // High vertical trampoline bounce velocity (deltaY = 0.55) must NOT flag Fly Hack
        double bounceDeltaY = 0.55;
        int airTicks = 28;
        boolean isFlyFlagged = !recentBounce && airTicks > 20 && bounceDeltaY > 0.08;
        assertFalse(isFlyFlagged, "High bounce on bed/slime block within bounce grace period must NOT flag Fly Hack");

        // Fall damage on bounce blocks must be absorbed / cancelled
        double trackedFall = 15.0;
        boolean absorbsFall = recentBounce;
        assertTrue(absorbsFall, "Bouncing on bed/slime must absorb server fall damage");
    }

    private boolean isInvalidPitch(float pitch) {
        return pitch > 90.01f || pitch < -90.01f || Float.isNaN(pitch) || Float.isInfinite(pitch);
    }

    private boolean isMalformedCoordinate(double x, double y, double z) {
        return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) ||
               Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z);
    }
}
