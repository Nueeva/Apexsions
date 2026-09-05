package com.apexsions.chat;

import com.apexsions.chat.moderation.SpamChecker;
import com.apexsions.chat.moderation.TextNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SpamDetectionTest {

    @Test
    public void testSimilarityCalculation() {
        assertEquals(1.0, SpamChecker.calculateSimilarity("hello", "hello"), 0.001);
        assertTrue(SpamChecker.calculateSimilarity("hello", "hello!") >= 0.80);
        assertTrue(SpamChecker.calculateSimilarity("hello world", "hello worlld") >= 0.85);
        assertFalse(SpamChecker.calculateSimilarity("completely different", "short") >= 0.50);
    }

    @Test
    public void testTextNormalizationForNearDuplicates() {
        String msg1 = "hello";
        String msg2 = "HELLO!!";
        String msg3 = "h.e.l.l.o";
        String msg4 = "h e l l o";

        String norm1 = TextNormalizer.normalizeForSpam(msg1);
        String norm2 = TextNormalizer.normalizeForSpam(msg2);
        String norm3 = TextNormalizer.normalizeForSpam(msg3);
        String norm4 = TextNormalizer.normalizeForSpam(msg4);

        assertEquals(norm1, norm2);
        assertEquals(norm1, norm3);
        assertEquals(norm1, norm4);
    }

    @Test
    public void testRepeatedCharactersCollapsing() {
        assertEquals("fuck", TextNormalizer.collapseRepeatedChars("fuuuuck"));
        assertEquals("anjing", TextNormalizer.collapseRepeatedChars("anjingggg"));
        assertEquals("hello", TextNormalizer.collapseRepeatedChars("hellooooo"));
        assertEquals("hello", TextNormalizer.collapseRepeatedChars("hello"));
    }
}
