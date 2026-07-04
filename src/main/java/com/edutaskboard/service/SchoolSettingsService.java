package com.edutaskboard.service;

import com.edutaskboard.model.SchoolSettings;
import com.edutaskboard.repository.SchoolSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalTime;

@Service
public class SchoolSettingsService {
    @Autowired private SchoolSettingsRepository repo;

    public SchoolSettings get() {
        return repo.findById(1L).orElseGet(() -> {
            SchoolSettings s = new SchoolSettings();
            s.setId(1L);
            return repo.save(s);
        });
    }

    public SchoolSettings updateStartTime(LocalTime startTime) {
        SchoolSettings s = get();
        s.setDailyStartTime(startTime);
        return repo.save(s);
    }
}
