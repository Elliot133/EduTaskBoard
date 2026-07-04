package com.edutaskboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subject_results",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "subject_id", "class_id"}))
public class SubjectResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    private int ca1;
    private int ca2;
    private int exam;
    private int total;

    /** Highest total score achieved in this subject/class (computed across the whole class). */
    private double highest;
    /** Average total score across the class for this subject. */
    private double average;

    private String grade;

    /** 1 = best in the class for this subject; ties share the same rank. */
    private int subjectPosition;

    @Column(columnDefinition = "TEXT")
    private String teacherRemark;

    @Column(updatable = false)
    private LocalDateTime generatedAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { generatedAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }
    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }
    public int getCa1() { return ca1; }
    public void setCa1(int ca1) { this.ca1 = ca1; }
    public int getCa2() { return ca2; }
    public void setCa2(int ca2) { this.ca2 = ca2; }
    public int getExam() { return exam; }
    public void setExam(int exam) { this.exam = exam; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public double getHighest() { return highest; }
    public void setHighest(double highest) { this.highest = highest; }
    public double getAverage() { return average; }
    public void setAverage(double average) { this.average = average; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public int getSubjectPosition() { return subjectPosition; }
    public void setSubjectPosition(int subjectPosition) { this.subjectPosition = subjectPosition; }
    public String getTeacherRemark() { return teacherRemark; }
    public void setTeacherRemark(String teacherRemark) { this.teacherRemark = teacherRemark; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
