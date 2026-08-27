package com.yourserver.apexsionschat.moderation;

public enum ModerationRule {
    NONE,
    RATE_LIMIT,
    DUPLICATE_SPAM,
    SIMILARITY_SPAM,
    TEMPORARY_MUTE,
    ADVERTISEMENT,
    PROFANITY,
    HATE_SPEECH,
    EXCESSIVE_CAPS,
    EXCESSIVE_SYMBOLS
}
