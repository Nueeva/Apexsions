package com.apexsions.chat.model;

import java.time.Instant;
import java.util.UUID;

public class Mail {

    private long mailId;
    private UUID senderUuid;
    private String senderName;
    private UUID recipientUuid;
    private String recipientName;
    private String subject;
    private String body;
    private Instant createdAt;
    private Instant readAt;
    private boolean read;
    private boolean archived;

    public Mail() {
        this.createdAt = Instant.now();
        this.read = false;
        this.archived = false;
    }

    public Mail(UUID senderUuid, String senderName, UUID recipientUuid, String recipientName, String subject, String body) {
        this.senderUuid = senderUuid;
        this.senderName = senderName;
        this.recipientUuid = recipientUuid;
        this.recipientName = recipientName;
        this.subject = subject;
        this.body = body;
        this.createdAt = Instant.now();
        this.read = false;
        this.archived = false;
    }

    public long getMailId() { return mailId; }
    public void setMailId(long mailId) { this.mailId = mailId; }

    public UUID getSenderUuid() { return senderUuid; }
    public void setSenderUuid(UUID senderUuid) { this.senderUuid = senderUuid; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public UUID getRecipientUuid() { return recipientUuid; }
    public void setRecipientUuid(UUID recipientUuid) { this.recipientUuid = recipientUuid; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
}
