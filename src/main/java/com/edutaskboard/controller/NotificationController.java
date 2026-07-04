package com.edutaskboard.controller;

import com.edutaskboard.config.CustomUserDetails;
import com.edutaskboard.model.Notification;
import com.edutaskboard.model.User;
import com.edutaskboard.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired private NotificationService notificationService;

    private User me(Authentication a) { return ((CustomUserDetails) a.getPrincipal()).getUser(); }

    @GetMapping
    public List<Notification> list(Authentication auth) {
        return notificationService.forUser(me(auth));
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(Authentication auth) {
        return Map.of("count", notificationService.unreadCount(me(auth)));
    }

    @PostMapping("/{id}/read")
    public void markRead(@PathVariable Long id, Authentication auth) {
        notificationService.markRead(id, me(auth));
    }

    @PostMapping("/read-all")
    public void markAllRead(Authentication auth) {
        notificationService.markAllRead(me(auth));
    }
}
