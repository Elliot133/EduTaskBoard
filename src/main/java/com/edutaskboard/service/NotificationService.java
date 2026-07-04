package com.edutaskboard.service;

import com.edutaskboard.model.Notification;
import com.edutaskboard.model.User;
import com.edutaskboard.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    @Autowired private NotificationRepository repo;
    @Autowired private SimpMessagingTemplate broker;

    public Notification notify(User recipient, Notification.NotificationType type, String title, String message, String link) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setLink(link);
        Notification saved = repo.save(n);
        // Push a live update to the user's personal notification channel, if connected.
        try {
            broker.convertAndSendToUser(recipient.getUsername(), "/queue/notifications", saved);
        } catch (Exception ignored) { /* user may not be connected; notification is still persisted */ }
        return saved;
    }

    public void notifyAll(List<User> recipients, Notification.NotificationType type, String title, String message, String link) {
        for (User u : recipients) notify(u, type, title, message, link);
    }

    public List<Notification> forUser(User user) { return repo.findByRecipientOrderByCreatedAtDesc(user); }

    public long unreadCount(User user) { return repo.countByRecipientAndReadFalse(user); }

    public void markRead(Long id, User user) {
        repo.findById(id).ifPresent(n -> {
            if (n.getRecipient().getId().equals(user.getId())) { n.setRead(true); repo.save(n); }
        });
    }

    public void markAllRead(User user) {
        forUser(user).forEach(n -> { if (!n.isRead()) { n.setRead(true); repo.save(n); } });
    }
}
