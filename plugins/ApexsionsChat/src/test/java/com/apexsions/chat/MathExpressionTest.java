package com.apexsions.chat;

import com.apexsions.chat.game.QuickMathGame;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MathExpressionTest {

    @Test
    public void testSimpleAddition() {
        List<Integer> nums = new ArrayList<>(Arrays.asList(10, 5));
        List<String> ops = new ArrayList<>(List.of("+"));
        int res = QuickMathGame.evaluateExpression(nums, ops);
        assertEquals(15, res);
    }

    @Test
    public void testOrderOfOperations() {
        // 10 + 5 * 2 = 20 (not 30)
        List<Integer> nums = new ArrayList<>(Arrays.asList(10, 5, 2));
        List<String> ops = new ArrayList<>(Arrays.asList("+", "*"));
        int res = QuickMathGame.evaluateExpression(nums, ops);
        assertEquals(20, res);
    }

    @Test
    public void testComplexPrecedence() {
        // 20 - 4 / 2 + 3 * 5 = 20 - 2 + 15 = 33
        List<Integer> nums = new ArrayList<>(Arrays.asList(20, 4, 2, 3, 5));
        List<String> ops = new ArrayList<>(Arrays.asList("-", "/", "+", "*"));
        int res = QuickMathGame.evaluateExpression(nums, ops);
        assertEquals(33, res);
    }
}
