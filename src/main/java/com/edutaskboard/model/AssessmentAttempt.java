package com.edutaskboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_attempts",
       uniqueConstraints = @UniqueConstraint(columnNames = {"assessment_id", "student_id"}))
public class AssessmentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    private LocalDateTime startedAt;
    private LocalDateTime deadlineAt;
    private LocalDateTime submittedAt;

    private Integer objectiveScore = 0;
    private Integer theoryScore = 0;
    private Integer totalScore;

    private boolean fullyMarked = false;

    public enum AttemptStatus { IN_PROGRESS, SUBMITTED, AUTO_SUBMITTED, MARKED }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public AttemptStatus getStatus() { return status; }
    public void setStatus(AttemptStatus status) { this.status = status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(LocalDateTime deadlineAt) { this.deadlineAt = deadlineAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public Integer getObjectiveScore() { return objectiveScore; }
    public void setObjectiveScore(Integer objectiveScore) { this.objectiveScore = objectiveScore; }
    public Integer getTheoryScore() { return theoryScore; }
    public void setTheoryScore(Integer theoryScore) { this.theoryScore = theoryScore; }
    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
    public boolean isFullyMarked() { return fullyMarked; }
    public void setFullyMarked(boolean fullyMarked) { this.fullyMarked = fullyMarked; }
}
