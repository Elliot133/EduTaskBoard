package com.edutaskboard.dto;

/** Payload sent by the client over STOMP to /app/chat.send */
public class ChatSendPayload {
    private String type;      // GENERAL or PRIVATE
    private Long receiverId;  // required when type == PRIVATE
    private String content;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
