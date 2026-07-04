package com.edutaskboard.repository;

import com.edutaskboard.model.GeneralMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GeneralMessageRepository extends JpaRepository<GeneralMessage, Long> {

    @Query("SELECT m FROM GeneralMessage m WHERE m.expiresAt > :now ORDER BY m.sentAt ASC")
    List<GeneralMessage> findActive(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM GeneralMessage m WHERE m.expiresAt <= :now")
    int deleteExpired(@Param("now") LocalDateTime now);
}
