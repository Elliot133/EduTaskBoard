package com.edutaskboard.model;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;

    // Objective options (nullable for theory)
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    @Enumerated(EnumType.STRING)
    private CorrectOption correctOption; // for objective only

    @Column(nullable = false)
    private int marks;

    private int orderIndex;

    public enum QuestionType { OBJECTIVE, THEORY }
    public enum CorrectOption { A, B, C, D }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public CorrectOption getCorrectOption() { return correctOption; }
    public void setCorrectOption(CorrectOption correctOption) { this.correctOption = correctOption; }
    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
