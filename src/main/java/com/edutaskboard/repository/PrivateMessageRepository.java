package com.edutaskboard.repository;

import com.edutaskboard.model.PrivateMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {

    @Query("SELECT m FROM PrivateMessage m WHERE m.expiresAt > :now AND " +
           "((m.sender.id = :userA AND m.receiver.id = :userB) OR (m.sender.id = :userB AND m.receiver.id = :userA)) " +
           "ORDER BY m.sentAt ASC")
    List<PrivateMessage> findConversation(@Param("userA") Long userA, @Param("userB") Long userB, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM PrivateMessage m WHERE m.expiresAt <= :now")
    int deleteExpired(@Param("now") LocalDateTime now);
}
