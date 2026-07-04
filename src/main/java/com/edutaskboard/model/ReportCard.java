package com.edutaskboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_cards",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "class_id"}))
public class ReportCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    private int overallTotal;
    private double overallAverage;
    private String overallGrade;
    private int numberOfSubjects;

    /** 1 = top of the class overall; ties share the same rank. */
    private int overallPosition;

    private double attendancePercent;

    @Column(columnDefinition = "TEXT")
    private String classTeacherRemark;

    @Column(columnDefinition = "TEXT")
    private String principalRemark;

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
    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }
    public int getOverallTotal() { return overallTotal; }
    public void setOverallTotal(int overallTotal) { this.overallTotal = overallTotal; }
    public double getOverallAverage() { return overallAverage; }
    public void setOverallAverage(double overallAverage) { this.overallAverage = overallAverage; }
    public String getOverallGrade() { return overallGrade; }
    public void setOverallGrade(String overallGrade) { this.overallGrade = overallGrade; }
    public int getNumberOfSubjects() { return numberOfSubjects; }
    public void setNumberOfSubjects(int numberOfSubjects) { this.numberOfSubjects = numberOfSubjects; }
    public int getOverallPosition() { return overallPosition; }
    public void setOverallPosition(int overallPosition) { this.overallPosition = overallPosition; }
    public double getAttendancePercent() { return attendancePercent; }
    public void setAttendancePercent(double attendancePercent) { this.attendancePercent = attendancePercent; }
    public String getClassTeacherRemark() { return classTeacherRemark; }
    public void setClassTeacherRemark(String classTeacherRemark) { this.classTeacherRemark = classTeacherRemark; }
    public String getPrincipalRemark() { return principalRemark; }
    public void setPrincipalRemark(String principalRemark) { this.principalRemark = principalRemark; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
