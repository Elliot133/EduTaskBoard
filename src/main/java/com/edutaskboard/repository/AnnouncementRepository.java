package com.edutaskboard.repository;

import com.edutaskboard.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findAllByOrderByPinnedDescCreatedAtDesc();

    @Query("SELECT a FROM Announcement a WHERE a.audience = 'ALL' OR a.audience = :aud ORDER BY a.pinned DESC, a.createdAt DESC")
    List<Announcement> findForAudience(@Param("aud") Announcement.AudienceType aud);
}
