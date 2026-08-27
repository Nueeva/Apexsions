package com.yourserver.apexsionschat.game;

import net.kyori.adventure.text.Component;

public interface ChatGame {
    String getTypeName();
    Component getPromptComponent();
    String getAnswer();
    boolean isCorrect(String input);
}
