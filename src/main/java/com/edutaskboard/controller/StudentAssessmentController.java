package com.edutaskboard.controller;

import com.edutaskboard.config.CustomUserDetails;
import com.edutaskboard.model.*;
import com.edutaskboard.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Module 6: Student Assessment. */
@Controller
@RequestMapping("/student/assessments")
public class StudentAssessmentController {

    @Autowired private AssessmentService assessmentService;
    @Autowired private AssessmentAttemptService attemptService;
    @Autowired private ResultService resultService;

    private User me(Authentication a) { return ((CustomUserDetails) a.getPrincipal()).getUser(); }

    @GetMapping
    public String list(Authentication auth, Model m) {
        User student = me(auth);
        List<Assessment> assessments = student.getSchoolClass() != null
                ? assessmentService.publishedByClass(student.getSchoolClass())
                : List.of();
        m.addAttribute("assessments", assessments);
        m.addAttribute("student", student);
        return "student/assessments";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model m) {
        Assessment a = assessmentService.byId(id).orElse(null);
        User student = me(auth);
        if (a == null || student.getSchoolClass() == null
                || !a.getSchoolClass().getId().equals(student.getSchoolClass().getId()) || !a.isPublished()) {
            return "redirect:/student/assessments";
        }
        m.addAttribute("assessment", a);
        m.addAttribute("attempt", attemptService.find(a, student).orElse(null));
        m.addAttribute("isOpen", assessmentService.isOpenNow(a));
        return "student/assessment-detail";
    }

    @PostMapping("/{id}/start")
    public String start(@PathVariable Long id, Authentication auth, org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        Assessment a = assessmentService.byId(id).orElse(null);
        User student = me(auth);
        if (a == null) return "redirect:/student/assessments";
        try {
            AssessmentAttempt attempt = attemptService.start(a, student);
            return "redirect:/student/assessments/attempts/" + attempt.getId();
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/student/assessments/" + id;
        }
    }

    @GetMapping("/attempts/{attemptId}")
    public String take(@PathVariable Long attemptId, Authentication auth, Model m) {
        AssessmentAttempt attempt = attemptService.byId(attemptId).orElse(null);
        User student = me(auth);
        if (attempt == null || !attempt.getStudent().getId().equals(student.getId())) {
            return "redirect:/student/assessments";
        }
        attempt = attemptService.checkAndAutoSubmit(attempt);

        if (attempt.getStatus() != AssessmentAttempt.AttemptStatus.IN_PROGRESS) {
            resultService.checkAndGenerateSubjectResults(attempt.getAssessment().getSubject(), attempt.getAssessment().getSchoolClass());
            return "redirect:/student/assessments/attempts/" + attemptId + "/result";
        }

        List<Question> questions = assessmentService.questionsFor(attempt.getAssessment());
        List<StudentAnswer> answers = attemptService.answersFor(attempt);
        Map<Long, StudentAnswer> answerMap = answers.stream()
                .collect(java.util.stream.Collectors.toMap(a -> a.getQuestion().getId(), a -> a));

        long secondsRemaining = Duration.between(LocalDateTime.now(), attempt.getDeadlineAt()).getSeconds();
        if (secondsRemaining < 0) secondsRemaining = 0;

        m.addAttribute("attempt", attempt);
        m.addAttribute("questions", questions);
        m.addAttribute("answerMap", answerMap);
        m.addAttribute("secondsRemaining", secondsRemaining);
        return "student/assessment-take";
    }

    /** Autosave endpoint, called via fetch() from JS while the student is answering. */
    @PostMapping("/attempts/{attemptId}/autosave")
    @ResponseBody
    public ResponseEntity<String> autosave(@PathVariable Long attemptId,
                                            @RequestParam Long questionId,
                                            @RequestParam(required = false) String selectedOption,
                                            @RequestParam(required = false) String answerText,
                                            Authentication auth) {
        AssessmentAttempt attempt = attemptService.byId(attemptId).orElse(null);
        User student = me(auth);
        if (attempt == null || !attempt.getStudent().getId().equals(student.getId())) {
            return ResponseEntity.status(403).body("forbidden");
        }
        Question question = assessmentService.questionsFor(attempt.getAssessment()).stream()
                .filter(q -> q.getId().equals(questionId)).findFirst().orElse(null);
        if (question == null) return ResponseEntity.badRequest().body("invalid question");
        try {
            Question.CorrectOption opt = (selectedOption != null && !selectedOption.isBlank())
                    ? Question.CorrectOption.valueOf(selectedOption) : null;
            attemptService.autosaveAnswer(attempt, question, opt, answerText);
            return ResponseEntity.ok("saved");
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public String submit(@PathVariable Long attemptId, Authentication auth) {
        AssessmentAttempt attempt = attemptService.byId(attemptId).orElse(null);
        User student = me(auth);
        if (attempt == null || !attempt.getStudent().getId().equals(student.getId())) {
            return "redirect:/student/assessments";
        }
        attemptService.submit(attempt, false);
        resultService.checkAndGenerateSubjectResults(attempt.getAssessment().getSubject(), attempt.getAssessment().getSchoolClass());
        return "redirect:/student/assessments/attempts/" + attemptId + "/result";
    }

    /** Called automatically by the client-side timer when it reaches zero. */
    @PostMapping("/attempts/{attemptId}/auto-submit")
    @ResponseBody
    public ResponseEntity<String> autoSubmit(@PathVariable Long attemptId, Authentication auth) {
        AssessmentAttempt attempt = attemptService.byId(attemptId).orElse(null);
        User student = me(auth);
        if (attempt == null || !attempt.getStudent().getId().equals(student.getId())) {
            return ResponseEntity.status(403).body("forbidden");
        }
        attemptService.submit(attempt, true);
        resultService.checkAndGenerateSubjectResults(attempt.getAssessment().getSubject(), attempt.getAssessment().getSchoolClass());
        return ResponseEntity.ok("submitted");
    }

    @GetMapping("/attempts/{attemptId}/result")
    public String result(@PathVariable Long attemptId, Authentication auth, Model m) {
        AssessmentAttempt attempt = attemptService.byId(attemptId).orElse(null);
        User student = me(auth);
        if (attempt == null || !attempt.getStudent().getId().equals(student.getId())) {
            return "redirect:/student/assessments";
        }
        m.addAttribute("attempt", attempt);
        m.addAttribute("answers", attemptService.answersFor(attempt));
        return "student/assessment-result";
    }
}
