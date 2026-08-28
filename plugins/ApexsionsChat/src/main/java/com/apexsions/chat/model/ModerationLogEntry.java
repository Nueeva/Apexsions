package com.apexsions.chat.model;

import java.time.Instant;
import java.util.UUID;

public class ModerationLogEntry {

    private long eventId;
    private UUID playerUuid;
    private String playerName;
    private String messageSnippet;
    private String channel;
    private String ruleViolated;
    private String actionTaken;
    private Instant timestamp;

    public ModerationLogEntry() {
        this.timestamp = Instant.now();
    }

    public ModerationLogEntry(UUID playerUuid, String playerName, String messageSnippet, String channel, String ruleViolated, String actionTaken) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.messageSnippet = messageSnippet;
        this.channel = channel;
        this.ruleViolated = ruleViolated;
        this.actionTaken = actionTaken;
        this.timestamp = Instant.now();
    }

    public long getEventId() { return eventId; }
    public void setEventId(long eventId) { this.eventId = eventId; }

    public UUID getPlayerUuid() { return playerUuid; }
    public void setPlayerUuid(UUID playerUuid) { this.playerUuid = playerUuid; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getMessageSnippet() { return messageSnippet; }
    public void setMessageSnippet(String messageSnippet) { this.messageSnippet = messageSnippet; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getRuleViolated() { return ruleViolated; }
    public void setRuleViolated(String ruleViolated) { this.ruleViolated = ruleViolated; }

    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
