package com.edutaskboard.service;

import com.edutaskboard.model.Announcement;
import com.edutaskboard.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AnnouncementService {
    @Autowired private AnnouncementRepository repo;

    public List<Announcement> getAll() { return repo.findAllByOrderByPinnedDescCreatedAtDesc(); }
    public List<Announcement> forAudience(Announcement.AudienceType a) { return repo.findForAudience(a); }
    public Optional<Announcement> byId(Long id) { return repo.findById(id); }
    public Announcement save(Announcement a) { return repo.save(a); }
    public void delete(Long id) { repo.deleteById(id); }
}
