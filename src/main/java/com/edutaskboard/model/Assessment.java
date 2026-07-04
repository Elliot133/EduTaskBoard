package com.edutaskboard.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "assessments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "class_id", "type"}))
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentType type;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    private boolean published = false;
    private boolean approved = false;

    private LocalDate openDate;
    private LocalTime openTime;
    private LocalDate closeDate;
    private LocalTime closeTime;

    /** Duration in minutes the student is given once they start the assessment. */
    private Integer durationMinutes = 60;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public enum AssessmentType { CA1, CA2, EXAM }

    /* ── Business rules per type (Module 4) ── */
    public int getRequiredObjectiveCount() {
        return switch (type) {
            case CA1 -> 15;
            case CA2 -> 0;
            case EXAM -> 15;
        };
    }

    public int getRequiredTheoryCount() {
        return switch (type) {
            case CA1 -> 0;
            case CA2 -> 6;
            case EXAM -> 5;
        };
    }

    public int getRequiredTotalMarks() {
        return switch (type) {
            case CA1 -> 20;
            case CA2 -> 20;
            case EXAM -> 60;
        };
    }

    public LocalDateTime getOpenDateTime() {
        return (openDate != null && openTime != null) ? LocalDateTime.of(openDate, openTime) : null;
    }

    public LocalDateTime getCloseDateTime() {
        return (closeDate != null && closeTime != null) ? LocalDateTime.of(closeDate, closeTime) : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AssessmentType getType() { return type; }
    public void setType(AssessmentType type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }
    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public LocalDate getOpenDate() { return openDate; }
    public void setOpenDate(LocalDate openDate) { this.openDate = openDate; }
    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }
    public LocalDate getCloseDate() { return closeDate; }
    public void setCloseDate(LocalDate closeDate) { this.closeDate = closeDate; }
    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
