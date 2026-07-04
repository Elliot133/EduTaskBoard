package com.edutaskboard.controller;

import com.edutaskboard.config.CustomUserDetails;
import com.edutaskboard.dto.ContactDto;
import com.edutaskboard.dto.MessageDto;
import com.edutaskboard.model.GeneralMessage;
import com.edutaskboard.model.PrivateMessage;
import com.edutaskboard.model.User;
import com.edutaskboard.service.MessagingAccessService;
import com.edutaskboard.service.MessagingService;
import com.edutaskboard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/messages")
public class MessagingController {

    @Autowired private MessagingAccessService accessService;
    @Autowired private MessagingService messagingService;
    @Autowired private UserService userService;

    private User me(Authentication a) { return ((CustomUserDetails) a.getPrincipal()).getUser(); }

    @GetMapping
    public String page(Authentication auth, Model m) {
        m.addAttribute("me", me(auth));
        return "shared/messaging";
    }

    @GetMapping("/contacts")
    @ResponseBody
    public List<ContactDto> contacts(Authentication auth) {
        return accessService.buildContactList(me(auth));
    }

    @GetMapping("/history/general")
    @ResponseBody
    public List<MessageDto> generalHistory() {
        return messagingService.generalHistory().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/history/private/{peerId}")
    @ResponseBody
    public ResponseEntity<?> privateHistory(@PathVariable Long peerId, Authentication auth) {
        User user = me(auth);
        User peer = userService.byId(peerId).orElse(null);
        if (peer == null) return ResponseEntity.notFound().build();
        if (!accessService.canMessagePrivately(user, peer)) return ResponseEntity.status(403).body("forbidden");

        List<MessageDto> messages = messagingService.conversation(user.getId(), peerId).stream()
                .map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(messages);
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
