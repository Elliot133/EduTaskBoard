package com.edutaskboard.controller;

import com.edutaskboard.config.CustomUserDetails;
import com.edutaskboard.model.*;
import com.edutaskboard.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Modules 4 (CBT authoring), 5 (availability/duration), 7 (teacher dashboard). */
@Controller
@RequestMapping("/teacher/assessments")
public class TeacherAssessmentController {

    @Autowired private AssessmentService assessmentService;
    @Autowired private QuestionService questionService;
    @Autowired private AssessmentAttemptService attemptService;
    @Autowired private TeacherAssignmentService assignmentService;
    @Autowired private SubjectService subjectService;
    @Autowired private SchoolClassService classService;
    @Autowired private ResultService resultService;

    private User me(Authentication a) { return ((CustomUserDetails) a.getPrincipal()).getUser(); }

    /* ── Dashboard: list all assessments owned by this teacher ── */
    @GetMapping
    public String list(Authentication auth, Model m) {
        User teacher = me(auth);
        List<Assessment> assessments = assessmentService.byTeacher(teacher);
        m.addAttribute("assessments", assessments);
        return "teacher/assessments";
    }

    /* ── Create ── */
    @GetMapping("/new")
    public String newForm(Authentication auth, Model m) {
        User teacher = me(auth);
        m.addAttribute("assessment", new Assessment());
        m.addAttribute("myAssignments", assignmentService.byTeacher(teacher));
        m.addAttribute("types", Assessment.AssessmentType.values());
        return "teacher/assessment-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model m) {
        Assessment a = assessmentService.byId(id).orElse(null);
        if (a == null) return "redirect:/teacher/assessments";
        m.addAttribute("assessment", a);
        m.addAttribute("myAssignments", assignmentService.byTeacher(a.getTeacher()));
        m.addAttribute("types", Assessment.AssessmentType.values());
        return "teacher/assessment-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long assessmentId,
                        @RequestParam String type,
                        @RequestParam String title,
                        @RequestParam Long subjectId,
                        @RequestParam Long classId,
                        @RequestParam(required = false) String openDate,
                        @RequestParam(required = false) String openTime,
                        @RequestParam(required = false) String closeDate,
                        @RequestParam(required = false) String closeTime,
                        @RequestParam(required = false) Integer durationMinutes,
                        Authentication auth, RedirectAttributes ra) {
        Assessment a = (assessmentId != null) ? assessmentService.byId(assessmentId).orElse(new Assessment()) : new Assessment();
        a.setType(Assessment.AssessmentType.valueOf(type));
        a.setTitle(title);
        subjectService.byId(subjectId).ifPresent(a::setSubject);
        classService.byId(classId).ifPresent(a::setSchoolClass);
        a.setTeacher(me(auth));
        if (openDate != null && !openDate.isBlank()) a.setOpenDate(LocalDate.parse(openDate));
        if (openTime != null && !openTime.isBlank()) a.setOpenTime(LocalTime.parse(openTime));
        if (closeDate != null && !closeDate.isBlank()) a.setCloseDate(LocalDate.parse(closeDate));
        if (closeTime != null && !closeTime.isBlank()) a.setCloseTime(LocalTime.parse(closeTime));
        a.setDurationMinutes(durationMinutes != null ? durationMinutes : 60);

        try {
            assessmentService.save(a);
            ra.addFlashAttribute("success", "Assessment saved.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Could not save: " + ex.getMessage());
            return "redirect:/teacher/assessments";
        }
        return "redirect:/teacher/assessments/" + a.getId() + "/questions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        assessmentService.delete(id);
        ra.addFlashAttribute("success", "Assessment deleted.");
        return "redirect:/teacher/assessments";
    }

    /* ── Questions (Module 4) ── */
    @GetMapping("/{id}/questions")
    public String questions(@PathVariable Long id, Model m) {
        Assessment a = assessmentService.byId(id).orElse(null);
        if (a == null) return "redirect:/teacher/assessments";
        List<Question> questions = assessmentService.questionsFor(a);
        int totalMarks = questions.stream().mapToInt(Question::getMarks).sum();
        m.addAttribute("assessment", a);
        m.addAttribute("questions", questions);
        m.addAttribute("totalMarks", totalMarks);
        m.addAttribute("validationError", assessmentService.validateForPublish(a));
        return "teacher/assessment-questions";
    }

    @PostMapping("/{id}/questions/save")
    public String saveQuestion(@PathVariable Long id,
                                @RequestParam(required = false) Long questionId,
                                @RequestParam String type,
                                @RequestParam String questionText,
                                @RequestParam(required = false) String optionA,
                                @RequestParam(required = false) String optionB,
                                @RequestParam(required = false) String optionC,
                                @RequestParam(required = false) String optionD,
                                @RequestParam(required = false) String correctOption,
                                @RequestParam int marks,
                                RedirectAttributes ra) {
        Assessment a = assessmentService.byId(id).orElse(null);
        if (a == null) return "redirect:/teacher/assessments";
        Question q = (questionId != null) ? questionService.byId(questionId).orElse(new Question()) : new Question();
        q.setAssessment(a);
        q.setType(Question.QuestionType.valueOf(type));
        q.setQuestionText(questionText);
        q.setMarks(marks);
        if (q.getType() == Question.QuestionType.OBJECTIVE) {
            q.setOptionA(optionA); q.setOptionB(optionB); q.setOptionC(optionC); q.setOptionD(optionD);
            q.setCorrectOption(correctOption != null && !correctOption.isBlank()
                    ? Question.CorrectOption.valueOf(correctOption) : null);
        } else {
            q.setOptionA(null); q.setOptionB(null); q.setOptionC(null); q.setOptionD(null);
            q.setCorrectOption(null);
        }
        questionService.save(q);
        ra.addFlashAttribute("success", "Question saved.");
        return "redirect:/teacher/assessments/" + id + "/questions";
    }

    @PostMapping("/questions/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long questionId, @RequestParam Long assessmentId, RedirectAttributes ra) {
        questionService.delete(questionId);
        ra.addFlashAttribute("success", "Question removed.");
        return "redirect:/teacher/assessments/" + assessmentId + "/questions";
    }

    /* ── Publish (Module 4 validation gate) ── */
    @PostMapping("/{id}/publish")
    public String publish(@PathVariable Long id, RedirectAttributes ra) {
        Assessment a = assessmentService.byId(id).orElse(null);
        if (a == null) return "redirect:/teacher/assessments";
        try {
            assessmentService.publish(a);
            ra.addFlashAttribute("success", "Assessment published! Students can now take it during the availability window.");
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/teacher/assessments/" + id + "/questions";
    }

    @PostMapping("/{id}/unpublish")
    public String unpublish(@PathVariable Long id, RedirectAttributes ra) {
        assessmentService.byId(id).ifPresent(assessmentService::unpublish);
        ra.addFlashAttribute("success", "Assessment unpublished.");
        return "redirect:/teacher/assessments/" + id + "/questions";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        assessmentService.byId(id).ifPresent(assessmentService::approve);
        ra.addFlashAttribute("success", "Assessment approved.");
        return "redirect:/teacher/assessments";
    }

    /* ── Submissions & marking (Module 7) ── */
    @GetMapping("/{id}/submissions")
    public String submissions(@PathVariable Long id, Model m) {
        Assessment a = assessmentService.byId(id).orElse(null);
        if (a == null) return "redirect:/teacher/assessments";
        List<AssessmentAttempt> attempts = attemptService.byAssessment(a);
        m.addAttribute("assessment", a);
        m.addAttribute("attempts", attempts);
        return "teacher/assessment-submissions";
    }

    @GetMapping("/attempts/{attemptId}/mark")
    public String markForm(@PathVariable Long attemptId, Model m) {
        AssessmentAttempt attempt = attemptService.byId(attemptId).orElse(null);
        if (attempt == null) return "redirect:/teacher/assessments";
        m.addAttribute("attempt", attempt);
        m.addAttribute("answers", attemptService.answersFor(attempt));
        return "teacher/assessment-mark";
    }

    @PostMapping("/attempts/{attemptId}/mark")
    public String markSubmit(@PathVariable Long attemptId,
                              @RequestParam List<Long> answerIds,
                              @RequestParam List<Integer> marks,
                              RedirectAttributes ra) {
        AssessmentAttempt attempt = attemptService.byId(attemptId).orElse(null);
        if (attempt == null) return "redirect:/teacher/assessments";
        List<StudentAnswer> answers = attemptService.answersFor(attempt);
        for (int i = 0; i < answerIds.size(); i++) {
            Long ansId = answerIds.get(i);
            int mark = marks.get(i);
            answers.stream().filter(a -> a.getId().equals(ansId)).findFirst()
                    .ifPresent(a -> attemptService.markTheoryAnswer(a, mark));
        }
        ra.addFlashAttribute("success", "Marking saved.");
        resultService.checkAndGenerateSubjectResults(attempt.getAssessment().getSubject(), attempt.getAssessment().getSchoolClass());
        return "redirect:/teacher/assessments/" + attempt.getAssessment().getId() + "/submissions";
    }
}
