package com.edutaskboard.websocket;

import com.edutaskboard.config.CustomUserDetails;
import com.edutaskboard.dto.ChatSendPayload;
import com.edutaskboard.dto.MessageDto;
import com.edutaskboard.model.GeneralMessage;
import com.edutaskboard.model.Notification;
import com.edutaskboard.model.PrivateMessage;
import com.edutaskboard.model.User;
import com.edutaskboard.service.MessagingAccessService;
import com.edutaskboard.service.MessagingService;
import com.edutaskboard.service.NotificationService;
import com.edutaskboard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/** Handles real-time delivery of General Chat and Private messages over STOMP/WebSocket. */
@Controller
public class ChatWebSocketController {

    @Autowired private MessagingService messagingService;
    @Autowired private MessagingAccessService accessService;
    @Autowired private NotificationService notificationService;
    @Autowired private UserService userService;
    @Autowired private SimpMessagingTemplate broker;

    @MessageMapping("/chat.send")
    public void send(ChatSendPayload payload, Authentication auth) {
        User sender = ((CustomUserDetails) auth.getPrincipal()).getUser();
        String content = payload.getContent() == null ? "" : payload.getContent().trim();
        if (content.isEmpty()) return;
        if ("GENERAL".equalsIgnoreCase(payload.getType())) {
            sendGeneral(sender, content);
        } else {
            sendPrivate(sender, payload.getReceiverId(), content);
        }
    }

    private void sendGeneral(User sender, String content) {
        GeneralMessage saved = messagingService.sendGeneral(sender, content);
        broker.convertAndSend("/topic/general", toDto(saved));
    }

    private void sendPrivate(User sender, Long receiverId, String content) {
        if (receiverId == null) return;
        User receiver = userService.byId(receiverId).orElse(null);
        if (receiver == null) return;
        if (!accessService.canMessagePrivately(sender, receiver)) return; // silently drop unauthorized sends

        PrivateMessage saved = messagingService.sendPrivate(sender, receiver, content);
        MessageDto dto = toDto(saved);

        broker.convertAndSendToUser(sender.getUsername(), "/queue/private", dto);
        if (!receiver.getUsername().equals(sender.getUsername())) {
            broker.convertAndSendToUser(receiver.getUsername(), "/queue/private", dto);
        }

        String preview = content.length() > 80 ? content.substring(0, 80) + "…" : content;
        notificationService.notify(receiver, Notification.NotificationType.MESSAGE,
                "New message from " + sender.getFullName(), preview, "/messages?peer=" + sender.getId());
    }

    private MessageDto toDto(GeneralMessage m) {
        return new MessageDto(m.getId(), "GENERAL", m.getSender().getId(), m.getSender().getFullName(),
                m.getSender().getRole().name(), null, m.getContent(), m.getSentAt());
    }

    private MessageDto toDto(PrivateMessage m) {
        return new MessageDto(m.getId(), "PRIVATE", m.getSender().getId(), m.getSender().getFullName(),
                m.getSender().getRole().name(), m.getReceiver().getId(), m.getContent(), m.getSentAt());
    }
}
