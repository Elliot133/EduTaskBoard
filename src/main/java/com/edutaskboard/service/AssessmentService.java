package com.edutaskboard.service;

import com.edutaskboard.model.Assessment;
import com.edutaskboard.model.Question;
import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.User;
import com.edutaskboard.repository.AssessmentRepository;
import com.edutaskboard.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AssessmentService {
    @Autowired private AssessmentRepository repo;
    @Autowired private QuestionRepository questionRepo;

    public List<Assessment> getAll() { return repo.findAll(); }
    public Optional<Assessment> byId(Long id) { return repo.findById(id); }
    public List<Assessment> byTeacher(User teacher) { return repo.findByTeacher(teacher); }
    public List<Assessment> byClass(SchoolClass c) { return repo.findBySchoolClass(c); }
    public List<Assessment> publishedByClass(SchoolClass c) { return repo.findBySchoolClassAndPublishedTrue(c); }

    public Assessment save(Assessment a) {
        if (a.getOpenDate() == null || a.getOpenTime() == null) {
            LocalDateTime now = LocalDateTime.now();
            a.setOpenDate(now.toLocalDate());
            a.setOpenTime(now.toLocalTime());
        }
        if (a.getCloseDate() == null || a.getCloseTime() == null) {
            LocalDateTime close = a.getOpenDateTime().plusHours(3); // Default availability: 3 hours
            a.setCloseDate(close.toLocalDate());
            a.setCloseTime(close.toLocalTime());
        }
        return repo.save(a);
    }

    public void delete(Long id) {
        repo.findById(id).ifPresent(a -> {
            questionRepo.deleteByAssessment(a);
            repo.deleteById(id);
        });
    }

    public List<Question> questionsFor(Assessment a) { return questionRepo.findByAssessmentOrderByOrderIndexAsc(a); }

    /**
     * Validates an assessment's question set against Module 4 rules.
     * Returns null if valid, or an error message describing what's wrong.
     */
    public String validateForPublish(Assessment a) {
        List<Question> questions = questionsFor(a);
        long objCount = questions.stream().filter(q -> q.getType() == Question.QuestionType.OBJECTIVE).count();
        long theoryCount = questions.stream().filter(q -> q.getType() == Question.QuestionType.THEORY).count();
        int totalMarks = questions.stream().mapToInt(Question::getMarks).sum();

        int reqObj = a.getRequiredObjectiveCount();
        int reqTheory = a.getRequiredTheoryCount();
        int reqTotal = a.getRequiredTotalMarks();

        if (objCount != reqObj) {
            return "This assessment requires exactly " + reqObj + " objective question(s), but has " + objCount + ".";
        }
        if (theoryCount != reqTheory) {
            return "This assessment requires exactly " + reqTheory + " theory question(s), but has " + theoryCount + ".";
        }
        if (totalMarks != reqTotal) {
            return "Total marks must equal exactly " + reqTotal + ", but currently sum to " + totalMarks + ".";
        }
        for (Question q : questions) {
            if (q.getType() == Question.QuestionType.OBJECTIVE && q.getCorrectOption() == null) {
                return "Every objective question must have a correct option marked.";
            }
            if (q.getMarks() <= 0) {
                return "Every question must have marks greater than zero.";
            }
        }
        return null;
    }

    /** Attempts to publish; throws IllegalStateException with the validation error if it fails. */
    public Assessment publish(Assessment a) {
        String error = validateForPublish(a);
        if (error != null) throw new IllegalStateException(error);
        a.setPublished(true);
        return repo.save(a);
    }

    public Assessment unpublish(Assessment a) {
        a.setPublished(false);
        return repo.save(a);
    }

    public Assessment approve(Assessment a) {
        a.setApproved(true);
        return repo.save(a);
    }

    public boolean isOpenNow(Assessment a) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime open = a.getOpenDateTime();
        LocalDateTime close = a.getCloseDateTime();
        return a.isPublished() && open != null && close != null
                && !now.isBefore(open) && !now.isAfter(close);
    }

    public long countAll() { return repo.count(); }
}
