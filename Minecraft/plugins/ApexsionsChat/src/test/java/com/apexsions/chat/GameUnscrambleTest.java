package com.apexsions.chat;

import com.apexsions.chat.game.UnscrambleGame;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameUnscrambleTest {

    @Test
    public void testUnscrambleAnswer() {
        UnscrambleGame game = new UnscrambleGame(Collections.singletonList("Apexsions"));
        assertTrue(game.isCorrect("Apexsions"));
        assertTrue(game.isCorrect("apexsions"));
        assertTrue(game.isCorrect("APEXSIONS"));
        assertFalse(game.isCorrect("wrongword"));
    }
}
