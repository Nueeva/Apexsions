package com.yourserver.apexsionschat.moderation;

public class ModerationResult {

    private final boolean allowed;
    private final ModerationRule rule;
    private final ModerationAction action;
    private final String message;
    private final String reason;

    private ModerationResult(boolean allowed, ModerationRule rule, ModerationAction action, String message, String reason) {
        this.allowed = allowed;
        this.rule = rule;
        this.action = action;
        this.message = message;
        this.reason = reason;
    }

    public static ModerationResult allow(String message) {
        return new ModerationResult(true, ModerationRule.NONE, ModerationAction.ALLOW, message, null);
    }

    public static ModerationResult replace(String filteredMessage, ModerationRule rule, String reason) {
        return new ModerationResult(true, rule, ModerationAction.REPLACE, filteredMessage, reason);
    }

    public static ModerationResult block(ModerationRule rule, String reason) {
        return new ModerationResult(false, rule, ModerationAction.BLOCK, null, reason);
    }

    public static ModerationResult tempMute(ModerationRule rule, String reason) {
        return new ModerationResult(false, rule, ModerationAction.TEMP_MUTE, null, reason);
    }

    public static ModerationResult warn(String message, ModerationRule rule, String reason) {
        return new ModerationResult(true, rule, ModerationAction.WARN, message, reason);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public boolean isBlocked() {
        return !allowed;
    }

    public ModerationRule getRule() { return rule; }
    public String getRuleViolated() { return rule.name(); }
    public ModerationAction getAction() { return action; }
    public String getMessage() { return message; }
    public String getReason() { return reason; }
}
