package com.edutaskboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A plain-text message posted to the school-wide General Chat, visible to every
 * Admin, Teacher and Student. Messages are permanently deleted 2 hours after they
 * are sent (see MessagingService's scheduled cleanup).
 */
@Entity
@Table(name = "general_messages")
public class GeneralMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
        expiresAt = sentAt.plusHours(2);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getSentAt() { return sentAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
