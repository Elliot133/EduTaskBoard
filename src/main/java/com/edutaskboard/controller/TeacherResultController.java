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

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher/results")
public class TeacherResultController {

    @Autowired private ResultService resultService;
    @Autowired private TeacherAssignmentService assignmentService;
    @Autowired private SchoolClassService classService;

    private User me(Authentication a) { return ((CustomUserDetails) a.getPrincipal()).getUser(); }

    @GetMapping
    public String dashboard(Authentication auth, Model m) {
        User teacher = me(auth);
        List<TeacherAssignment> myAssignments = assignmentService.byTeacher(teacher);
        List<SchoolClass> classTeacherOf = classService.getAll().stream()
                .filter(c -> c.getClassTeacher() != null && c.getClassTeacher().getId().equals(teacher.getId()))
                .collect(Collectors.toList());
        m.addAttribute("myAssignments", myAssignments);
        m.addAttribute("classTeacherOf", classTeacherOf);
        return "teacher/results";
    }

    /* ── Subject teacher: view + remark on their subject's class results ── */
    @GetMapping("/subject")
    public String subjectResults(@RequestParam Long subjectId, @RequestParam Long classId, Model m) {
        SchoolClass sc = classService.byId(classId).orElse(null);
        if (sc == null) return "redirect:/teacher/results";
        List<SubjectResult> results = resultService.subjectResultsForTeacher(
                assignmentService.byClass(sc).stream().filter(a -> a.getSubject().getId().equals(subjectId))
                        .findFirst().map(TeacherAssignment::getSubject).orElse(null), sc);
        m.addAttribute("results", results);
        m.addAttribute("schoolClass", sc);
        return "teacher/results-subject";
    }

    @PostMapping("/subject/remark")
    public String saveRemark(@RequestParam Long subjectResultId, @RequestParam String remark, RedirectAttributes ra) {
        resultService.saveTeacherRemark(subjectResultId, remark);
        ra.addFlashAttribute("success", "Remark saved.");
        return "redirect:/teacher/results";
    }

    /* ── Class teacher: view + remark on the whole class's report cards ── */
    @GetMapping("/class")
    public String classReportCards(@RequestParam Long classId, Model m) {
        SchoolClass sc = classService.byId(classId).orElse(null);
        if (sc == null) return "redirect:/teacher/results";
        m.addAttribute("reportCards", resultService.reportCardsFor(sc));
        m.addAttribute("schoolClass", sc);
        return "teacher/results-class";
    }

    @PostMapping("/class/remark")
    public String saveClassRemark(@RequestParam Long reportCardId, @RequestParam String remark, RedirectAttributes ra) {
        resultService.saveClassTeacherRemark(reportCardId, remark);
        ra.addFlashAttribute("success", "Class teacher remark saved.");
        return "redirect:/teacher/results";
    }
}
