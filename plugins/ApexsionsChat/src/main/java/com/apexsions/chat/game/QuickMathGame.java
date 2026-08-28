package com.apexsions.chat.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;

public class QuickMathGame implements ChatGame {

    private final String expression;
    private final int answer;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public QuickMathGame(int minOperators, int maxOperators, List<String> allowedOperators, int minNum, int maxNum) {
        Random rand = new Random();
        int opCount = Math.max(1, minOperators + rand.nextInt(Math.max(1, (maxOperators - minOperators) + 1)));

        List<String> ops = (allowedOperators == null || allowedOperators.isEmpty())
                ? Arrays.asList("+", "-", "*", "/")
                : allowedOperators;

        List<Integer> numbers = new ArrayList<>();
        List<String> chosenOps = new ArrayList<>();

        for (int i = 0; i <= opCount; i++) {
            numbers.add(minNum + rand.nextInt(Math.max(1, maxNum - minNum + 1)));
            if (i < opCount) {
                chosenOps.add(ops.get(rand.nextInt(ops.size())));
            }
        }

        // Adjust for clean integer division if division is selected
        for (int i = 0; i < chosenOps.size(); i++) {
            if ("/".equals(chosenOps.get(i))) {
                int divisor = Math.max(1, 1 + rand.nextInt(10));
                int multiplier = 1 + rand.nextInt(10);
                int dividend = divisor * multiplier;
                numbers.set(i, dividend);
                numbers.set(i + 1, divisor);
            }
        }

        StringBuilder exprBuilder = new StringBuilder();
        for (int i = 0; i < numbers.size(); i++) {
            exprBuilder.append(numbers.get(i));
            if (i < chosenOps.size()) {
                exprBuilder.append(" ").append(chosenOps.get(i)).append(" ");
            }
        }

        this.expression = exprBuilder.toString();
        this.answer = evaluateExpression(new ArrayList<>(numbers), new ArrayList<>(chosenOps));
    }

    public static int evaluateExpression(List<Integer> nums, List<String> ops) {
        // First pass: Multiplication and Division
        int i = 0;
        while (i < ops.size()) {
            String op = ops.get(i);
            if ("*".equals(op) || "/".equals(op)) {
                int a = nums.get(i);
                int b = nums.get(i + 1);
                int res = "*".equals(op) ? (a * b) : (b != 0 ? a / b : 0);
                nums.set(i, res);
                nums.remove(i + 1);
                ops.remove(i);
            } else {
                i++;
            }
        }

        // Second pass: Addition and Subtraction
        int result = nums.isEmpty() ? 0 : nums.get(0);
        for (int j = 0; j < ops.size(); j++) {
            String op = ops.get(j);
            int next = nums.get(j + 1);
            if ("+".equals(op)) {
                result += next;
            } else if ("-".equals(op)) {
                result -= next;
            }
        }

        return result;
    }

    @Override
    public String getTypeName() {
        return "Quick Math";
    }

    @Override
    public Component getPromptComponent() {
        return miniMessage.deserialize("""
            <dark_gray>══════════════════════════════════════════</dark_gray>
            <gradient:#f59e0b:#ef4444><bold>⚡ CHAT GAME — QUICK MATH ⚡</bold></gradient>
            <gray>Solve the equation:</gray> <yellow><bold>%s</bold></yellow>
            <gray>First player to type the correct answer wins!</gray>
            <dark_gray>══════════════════════════════════════════</dark_gray>
            """.formatted(expression));
    }

    @Override
    public String getAnswer() {
        return String.valueOf(answer);
    }

    @Override
    public boolean isCorrect(String input) {
        if (input == null) return false;
        try {
            int val = Integer.parseInt(input.trim());
            return val == answer;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
