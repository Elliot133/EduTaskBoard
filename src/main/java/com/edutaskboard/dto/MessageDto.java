package com.edutaskboard.dto;

import java.time.LocalDateTime;

/** A single chat message as sent to the client, whether General or Private. */
public class MessageDto {
    private Long id;
    private String type;       // GENERAL or PRIVATE
    private Long senderId;
    private String senderName;
    private String senderRole;
    private Long receiverId;   // null for GENERAL
    private String content;
    private LocalDateTime sentAt;

    public MessageDto(Long id, String type, Long senderId, String senderName, String senderRole,
                       Long receiverId, String content, LocalDateTime sentAt) {
        this.id = id;
        this.type = type;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.receiverId = receiverId;
        this.content = content;
        this.sentAt = sentAt;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public Long getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getSenderRole() { return senderRole; }
    public Long getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public LocalDateTime getSentAt() { return sentAt; }
}
