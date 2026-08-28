package com.apexsions.chat;

import com.apexsions.chat.moderation.TextNormalizer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class ProfanityDetectionTest {

    private final List<String> bannedWords = Arrays.asList("kontol", "memek", "anjing", "babi", "bangsat", "ngentot", "fuck", "shit", "bitch", "asshole");
    private final Set<String> exceptions = new HashSet<>(Arrays.asList("pass", "class", "grass", "compass", "assist", "asset", "title", "glass"));

    private boolean isProfane(String message) {
        String normalized = TextNormalizer.normalizeForInspection(message);
        String dense = TextNormalizer.stripSeparators(normalized);

        // Check if message contains an allowed exception word
        String[] tokens = normalized.split("\\s+");
        for (String token : tokens) {
            if (exceptions.contains(token)) {
                return false;
            }
        }

        for (String banned : bannedWords) {
            String cleanBanned = banned.toLowerCase();

            // Word boundary match
            if (Pattern.compile("(?i)\\b" + Pattern.quote(cleanBanned) + "\\b").matcher(normalized).find()) {
                return true;
            }

            // Dense string match for bypasses like f.u.c.k or b a b i
            if (dense.contains(cleanBanned)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testDirectProfanity() {
        assertTrue(isProfane("dasar babi kamu"));
        assertTrue(isProfane("what the fuck"));
        assertTrue(isProfane("ANJING LO"));
    }

    @Test
    public void testSpacingAndSeparatorBypass() {
        assertTrue(isProfane("b.a.b.i"));
        assertTrue(isProfane("f u c k"));
        assertTrue(isProfane("k-o-n-t-o-l"));
        assertTrue(isProfane("a_n_j_i_n_g"));
    }

    @Test
    public void testLeetspeakBypass() {
        assertTrue(isProfane("f@ck"));
        assertTrue(isProfane("b4b!"));
        assertTrue(isProfane("k0nt0l"));
        assertTrue(isProfane("b1tch"));
    }

    @Test
    public void testCollapsingRepeats() {
        assertTrue(isProfane("fuuuuuck"));
        assertTrue(isProfane("baaaabiiii"));
    }

    @Test
    public void testExceptionsAndCleanMessages() {
        assertFalse(isProfane("I need a compass"));
        assertFalse(isProfane("Look at that green grass"));
        assertFalse(isProfane("Welcome to the first class"));
        assertFalse(isProfane("Please pass me the sword"));
        assertFalse(isProfane("Apexsions is awesome!"));
    }
}
