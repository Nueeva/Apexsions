package com.apexsions.chat.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;

public class UnscrambleGame implements ChatGame {

    private final String originalWord;
    private final String scrambledWord;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public UnscrambleGame(List<String> wordPool) {
        Random rand = new Random();
        if (wordPool == null || wordPool.isEmpty()) {
            this.originalWord = "Apexsions";
        } else {
            this.originalWord = wordPool.get(rand.nextInt(wordPool.size()));
        }
        this.scrambledWord = scrambleWord(this.originalWord);
    }

    private String scrambleWord(String word) {
        if (word.length() <= 1) return word;
        List<Character> chars = new ArrayList<>();
        for (char c : word.toCharArray()) chars.add(c);

        for (int i = 0; i < 10; i++) {
            Collections.shuffle(chars);
            StringBuilder sb = new StringBuilder();
            for (char c : chars) sb.append(c);
            if (!sb.toString().equalsIgnoreCase(word)) {
                return sb.toString();
            }
        }
        return chars.get(1) + word.substring(0, 1) + word.substring(2);
    }

    @Override
    public String getTypeName() {
        return "Word Unscramble";
    }

    @Override
    public Component getPromptComponent() {
        return miniMessage.deserialize("<gradient:#06b6d4:#3b82f6><bold>⚡ GAME</bold></gradient> <dark_gray>➔</dark_gray> <gray>Susun kata acak:</gray> <yellow><bold>%s</bold></yellow> <dark_gray>•</dark_gray> <gray>Ketik jawabanmu di chat!</gray>".formatted(scrambledWord));
    }

    @Override
    public String getAnswer() {
        return originalWord;
    }

    @Override
    public boolean isCorrect(String input) {
        if (input == null) return false;
        return originalWord.equalsIgnoreCase(input.trim());
    }
}
