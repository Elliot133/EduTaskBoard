package com.edutaskboard.dto;

/** A single entry in a user's messaging contact list (General Chat, Admin, a teacher, or a student). */
public class ContactDto {
    private String conversationId; // "general" or "u-{userId}"
    private String type;           // GENERAL or PRIVATE
    private Long userId;           // null for GENERAL
    private String name;
    private String role;           // null for GENERAL

    public ContactDto(String conversationId, String type, Long userId, String name, String role) {
        this.conversationId = conversationId;
        this.type = type;
        this.userId = userId;
        this.name = name;
        this.role = role;
    }

    public String getConversationId() { return conversationId; }
    public String getType() { return type; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getRole() { return role; }
}
