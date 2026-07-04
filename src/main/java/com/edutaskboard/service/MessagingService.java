package com.edutaskboard.service;

import com.edutaskboard.model.GeneralMessage;
import com.edutaskboard.model.PrivateMessage;
import com.edutaskboard.model.User;
import com.edutaskboard.repository.GeneralMessageRepository;
import com.edutaskboard.repository.PrivateMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Persists General Chat and Private messages, and permanently deletes messages
 * that are older than 2 hours. Messages are never kept in memory — every read
 * and write goes through the database.
 */
@Service
public class MessagingService {

    @Autowired private PrivateMessageRepository privateRepo;
    @Autowired private GeneralMessageRepository generalRepo;

    public PrivateMessage sendPrivate(User sender, User receiver, String content) {
        PrivateMessage m = new PrivateMessage();
        m.setSender(sender);
        m.setReceiver(receiver);
        m.setContent(content);
        return privateRepo.save(m);
    }

    public GeneralMessage sendGeneral(User sender, String content) {
        GeneralMessage m = new GeneralMessage();
        m.setSender(sender);
        m.setContent(content);
        return generalRepo.save(m);
    }

    public List<PrivateMessage> conversation(Long userA, Long userB) {
        return privateRepo.findConversation(userA, userB, LocalDateTime.now());
    }

    public List<GeneralMessage> generalHistory() {
        return generalRepo.findActive(LocalDateTime.now());
    }

    /** Runs every 5 minutes and permanently removes any message older than 2 hours. */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void deleteExpiredMessages() {
        LocalDateTime now = LocalDateTime.now();
        privateRepo.deleteExpired(now);
        generalRepo.deleteExpired(now);
    }
}
