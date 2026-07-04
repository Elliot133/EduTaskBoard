package com.edutaskboard.model;

import jakarta.persistence.*;

/**
 * One teaching period for a class on a given day.
 * Only PERIOD 1-5 slots are stored (breaks are computed automatically, never persisted).
 * A missing subject/teacher means the period is a "Free Period".
 */
@Entity
@Table(name = "timetable_slots",
       uniqueConstraints = @UniqueConstraint(columnNames = {"class_id", "weekday", "period_number"}))
public class TimetableSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id")
    private Subject subject; // null => Free Period

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private User teacher; // null => Free Period

    @Enumerated(EnumType.STRING)
    @Column(name = "weekday", nullable = false)
    private SchoolDay day;

    /** 1 through 5, matching the fixed daily period sequence. */
    @Column(name = "period_number", nullable = false)
    private int periodNumber;

    public enum SchoolDay { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }
    public SchoolDay getDay() { return day; }
    public void setDay(SchoolDay day) { this.day = day; }
    public int getPeriodNumber() { return periodNumber; }
    public void setPeriodNumber(int periodNumber) { this.periodNumber = periodNumber; }
}
