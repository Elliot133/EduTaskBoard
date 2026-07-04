package com.edutaskboard.service;

import com.edutaskboard.model.*;
import com.edutaskboard.repository.AssessmentAttemptRepository;
import com.edutaskboard.repository.StudentAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AssessmentAttemptService {
    @Autowired private AssessmentAttemptRepository attemptRepo;
    @Autowired private StudentAnswerRepository answerRepo;
    @Autowired private AssessmentService assessmentService;

    public Optional<AssessmentAttempt> byId(Long id) { return attemptRepo.findById(id); }
    public Optional<AssessmentAttempt> find(Assessment a, User student) { return attemptRepo.findByAssessmentAndStudent(a, student); }
    public List<AssessmentAttempt> byAssessment(Assessment a) { return attemptRepo.findByAssessment(a); }
    public List<AssessmentAttempt> byStudent(User student) { return attemptRepo.findByStudent(student); }

    /**
     * Starts a new attempt for the student, or resumes an in-progress one.
     * Prevents multiple attempts (Module 6).
     */
    public AssessmentAttempt start(Assessment assessment, User student) {
        Optional<AssessmentAttempt> existing = attemptRepo.findByAssessmentAndStudent(assessment, student);
        if (existing.isPresent()) {
            AssessmentAttempt a = existing.get();
            if (a.getStatus() != AssessmentAttempt.AttemptStatus.IN_PROGRESS) {
                throw new IllegalStateException("You have already submitted this assessment.");
            }
            return a; // resume
        }
        if (!assessmentService.isOpenNow(assessment)) {
            throw new IllegalStateException("This assessment is not currently open.");
        }
        AssessmentAttempt attempt = new AssessmentAttempt();
        attempt.setAssessment(assessment);
        attempt.setStudent(student);
        attempt.setStatus(AssessmentAttempt.AttemptStatus.IN_PROGRESS);
        LocalDateTime now = LocalDateTime.now();
        attempt.setStartedAt(now);
        int duration = assessment.getDurationMinutes() != null ? assessment.getDurationMinutes() : 60;
        LocalDateTime deadline = now.plusMinutes(duration);
        if (assessment.getCloseDateTime() != null && deadline.isAfter(assessment.getCloseDateTime())) {
            deadline = assessment.getCloseDateTime();
        }
        attempt.setDeadlineAt(deadline);
        return attemptRepo.save(attempt);
    }

    /** Autosave a single answer (objective or theory). */
    public StudentAnswer autosaveAnswer(AssessmentAttempt attempt, Question question,
                                         Question.CorrectOption selected, String text) {
        if (attempt.getStatus() != AssessmentAttempt.AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("This assessment has already been submitted.");
        }
        StudentAnswer answer = answerRepo.findByAttemptAndQuestion(attempt, question).orElseGet(() -> {
            StudentAnswer sa = new StudentAnswer();
            sa.setAttempt(attempt);
            sa.setQuestion(question);
            return sa;
        });
        if (question.getType() == Question.QuestionType.OBJECTIVE) {
            answer.setSelectedOption(selected);
        } else {
            answer.setAnswerText(text);
        }
        return answerRepo.save(answer);
    }

    public List<StudentAnswer> answersFor(AssessmentAttempt attempt) { return answerRepo.findByAttempt(attempt); }

    /**
     * Submits the attempt (manually by student, or automatically when the timer expires).
     * Objective questions are auto-marked immediately; theory stays pending.
     */
    public AssessmentAttempt submit(AssessmentAttempt attempt, boolean auto) {
        if (attempt.getStatus() != AssessmentAttempt.AttemptStatus.IN_PROGRESS) {
            return attempt; // already submitted — idempotent
        }
        List<Question> questions = assessmentService.questionsFor(attempt.getAssessment());
        List<StudentAnswer> answers = answerRepo.findByAttempt(attempt);

        int objectiveScore = 0;
        boolean hasTheory = false;
        for (Question q : questions) {
            if (q.getType() != Question.QuestionType.OBJECTIVE) { hasTheory = true; continue; }
            StudentAnswer ans = answers.stream().filter(a -> a.getQuestion().getId().equals(q.getId())).findFirst().orElse(null);
            if (ans != null && ans.getSelectedOption() != null && ans.getSelectedOption() == q.getCorrectOption()) {
                ans.setMarksAwarded(q.getMarks());
                objectiveScore += q.getMarks();
            } else if (ans != null) {
                ans.setMarksAwarded(0);
            }
            if (ans != null) { ans.setMarked(true); answerRepo.save(ans); }
        }

        attempt.setObjectiveScore(objectiveScore);
        attempt.setTheoryScore(0); // pending until teacher marks
        attempt.setTotalScore(objectiveScore);
        attempt.setFullyMarked(!hasTheory);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setStatus(auto ? AssessmentAttempt.AttemptStatus.AUTO_SUBMITTED : AssessmentAttempt.AttemptStatus.SUBMITTED);
        return attemptRepo.save(attempt);
    }

    /** Checks if the attempt's timer has expired and, if so, auto-submits it. Call on each page load. */
    public AssessmentAttempt checkAndAutoSubmit(AssessmentAttempt attempt) {
        if (attempt.getStatus() == AssessmentAttempt.AttemptStatus.IN_PROGRESS
                && attempt.getDeadlineAt() != null
                && LocalDateTime.now().isAfter(attempt.getDeadlineAt())) {
            return submit(attempt, true);
        }
        return attempt;
    }

    /** Teacher marks a theory answer; recomputes attempt totals. */
    public void markTheoryAnswer(StudentAnswer answer, int marksAwarded) {
        Question q = answer.getQuestion();
        int capped = Math.max(0, Math.min(marksAwarded, q.getMarks()));
        answer.setMarksAwarded(capped);
        answer.setMarked(true);
        answerRepo.save(answer);
        recomputeTotals(answer.getAttempt());
    }

    private void recomputeTotals(AssessmentAttempt attempt) {
        List<StudentAnswer> answers = answerRepo.findByAttempt(attempt);
        List<Question> questions = assessmentService.questionsFor(attempt.getAssessment());

        int objective = answers.stream()
                .filter(a -> a.getQuestion().getType() == Question.QuestionType.OBJECTIVE && a.getMarksAwarded() != null)
                .mapToInt(StudentAnswer::getMarksAwarded).sum();
        int theory = answers.stream()
                .filter(a -> a.getQuestion().getType() == Question.QuestionType.THEORY && a.getMarksAwarded() != null)
                .mapToInt(StudentAnswer::getMarksAwarded).sum();

        long theoryQuestionCount = questions.stream().filter(q -> q.getType() == Question.QuestionType.THEORY).count();
        long theoryMarkedCount = answers.stream()
                .filter(a -> a.getQuestion().getType() == Question.QuestionType.THEORY && a.isMarked()).count();

        attempt.setObjectiveScore(objective);
        attempt.setTheoryScore(theory);
        attempt.setTotalScore(objective + theory);
        boolean fullyMarked = theoryMarkedCount >= theoryQuestionCount;
        attempt.setFullyMarked(fullyMarked);
        if (fullyMarked && attempt.getStatus() != AssessmentAttempt.AttemptStatus.IN_PROGRESS) {
            attempt.setStatus(AssessmentAttempt.AttemptStatus.MARKED);
        }
        attemptRepo.save(attempt);
    }

    public long countAll() { return attemptRepo.count(); }
}
