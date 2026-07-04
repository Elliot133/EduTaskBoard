package com.edutaskboard.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "school_settings")
public class SchoolSettings {

    @Id
    private Long id = 1L;

    @Column(nullable = false)
    private LocalTime dailyStartTime = LocalTime.of(8, 0);

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalTime getDailyStartTime() { return dailyStartTime; }
    public void setDailyStartTime(LocalTime dailyStartTime) { this.dailyStartTime = dailyStartTime; }
}
