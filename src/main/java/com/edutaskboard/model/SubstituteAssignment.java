package com.edutaskboard.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Overlays a substitute teacher onto a specific class/day/period for a date range.
 * The base TimetableSlot is never modified — this record is checked at display time
 * and automatically stops applying once endDate has passed (no batch job needed).
 */
@Entity
@Table(name = "substitute_assignments")
public class SubstituteAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "weekday", nullable = false)
    private TimetableSlot.SchoolDay day;

    @Column(nullable = false)
    private int periodNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "original_teacher_id", nullable = false)
    private User originalTeacher;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "substitute_teacher_id", nullable = false)
    private User substituteTeacher;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private String reason;

    private boolean cancelled = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    /** Whether this substitution overlay is currently in effect for the given date. */
    public boolean coversDate(LocalDate date) {
        return !cancelled && !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }
    public TimetableSlot.SchoolDay getDay() { return day; }
    public void setDay(TimetableSlot.SchoolDay day) { this.day = day; }
    public int getPeriodNumber() { return periodNumber; }
    public void setPeriodNumber(int periodNumber) { this.periodNumber = periodNumber; }
    public User getOriginalTeacher() { return originalTeacher; }
    public void setOriginalTeacher(User originalTeacher) { this.originalTeacher = originalTeacher; }
    public User getSubstituteTeacher() { return substituteTeacher; }
    public void setSubstituteTeacher(User substituteTeacher) { this.substituteTeacher = substituteTeacher; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
