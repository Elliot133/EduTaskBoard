package com.edutaskboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_answers",
       uniqueConstraints = @UniqueConstraint(columnNames = {"attempt_id", "question_id"}))
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "attempt_id", nullable = false)
    private AssessmentAttempt attempt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // For objective
    @Enumerated(EnumType.STRING)
    private Question.CorrectOption selectedOption;

    // For theory
    @Column(columnDefinition = "TEXT")
    private String answerText;

    private Integer marksAwarded;
    private boolean marked = false;

    @Column(columnDefinition = "TEXT")
    private String teacherComment;

    private LocalDateTime lastSavedAt;

    @PreUpdate @PrePersist
    protected void onSave() { lastSavedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AssessmentAttempt getAttempt() { return attempt; }
    public void setAttempt(AssessmentAttempt attempt) { this.attempt = attempt; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public Question.CorrectOption getSelectedOption() { return selectedOption; }
    public void setSelectedOption(Question.CorrectOption selectedOption) { this.selectedOption = selectedOption; }
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
    public Integer getMarksAwarded() { return marksAwarded; }
    public void setMarksAwarded(Integer marksAwarded) { this.marksAwarded = marksAwarded; }
    public boolean isMarked() { return marked; }
    public void setMarked(boolean marked) { this.marked = marked; }
    public String getTeacherComment() { return teacherComment; }
    public void setTeacherComment(String teacherComment) { this.teacherComment = teacherComment; }
    public LocalDateTime getLastSavedAt() { return lastSavedAt; }
}
