package com.edutaskboard.controller;

import com.edutaskboard.model.*;
import com.edutaskboard.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Module 1: Teacher Assignment (subjects, subject/class assignment, class teacher). */
@Controller
@RequestMapping("/admin")
public class AdminAssignmentController {

    @Autowired private SubjectService subjectService;
    @Autowired private TeacherAssignmentService assignmentService;
    @Autowired private UserService userService;
    @Autowired private SchoolClassService classService;

    /* ── Subjects ── */
    @GetMapping("/subjects")
    public String subjects(Model m) {
        m.addAttribute("subjects", subjectService.getAll());
        return "admin/subjects";
    }

    @PostMapping("/subjects/save")
    public String saveSubject(@RequestParam String name,
                               @RequestParam(required = false) String code,
                               @RequestParam(required = false) String description,
                               RedirectAttributes ra) {
        if (subjectService.exists(name)) {
            ra.addFlashAttribute("error", "A subject with that name already exists.");
            return "redirect:/admin/subjects";
        }
        Subject s = new Subject();
        s.setName(name); s.setCode(code); s.setDescription(description);
        subjectService.save(s);
        ra.addFlashAttribute("success", "Subject created.");
        return "redirect:/admin/subjects";
    }

    @PostMapping("/subjects/{id}/delete")
    public String deleteSubject(@PathVariable Long id, RedirectAttributes ra) {
        subjectService.delete(id);
        ra.addFlashAttribute("success", "Subject removed.");
        return "redirect:/admin/subjects";
    }

    /* ── Teacher Assignments (subject + class) ── */
    @GetMapping("/assignments")
    public String assignments(Model m) {
        m.addAttribute("assignments", assignmentService.getAll());
        m.addAttribute("teachers", userService.byRole(User.Role.TEACHER));
        m.addAttribute("subjects", subjectService.getAll());
        m.addAttribute("classes", classService.getAll());
        return "admin/assignments";
    }

    @PostMapping("/assignments/save")
    public String saveAssignment(@RequestParam Long teacherId,
                                  @RequestParam Long subjectId,
                                  @RequestParam Long classId,
                                  RedirectAttributes ra) {
        User teacher = userService.byId(teacherId).orElse(null);
        Subject subject = subjectService.byId(subjectId).orElse(null);
        SchoolClass sc = classService.byId(classId).orElse(null);
        if (teacher == null || subject == null || sc == null) {
            ra.addFlashAttribute("error", "Invalid teacher, subject, or class.");
            return "redirect:/admin/assignments";
        }
        try {
            assignmentService.assign(teacher, subject, sc);
            ra.addFlashAttribute("success", "Teacher assigned.");
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/assignments";
    }

    @PostMapping("/assignments/{id}/delete")
    public String deleteAssignment(@PathVariable Long id, RedirectAttributes ra) {
        assignmentService.remove(id);
        ra.addFlashAttribute("success", "Assignment removed.");
        return "redirect:/admin/assignments";
    }

    /* ── Class Teacher (exactly one per class) ── */
    @PostMapping("/assignments/class-teacher")
    public String setClassTeacher(@RequestParam Long classId,
                                   @RequestParam Long teacherId,
                                   RedirectAttributes ra) {
        User teacher = userService.byId(teacherId).orElse(null);
        if (teacher == null) {
            ra.addFlashAttribute("error", "Invalid teacher.");
            return "redirect:/admin/assignments";
        }
        assignmentService.setClassTeacher(classId, teacherId, teacher);
        ra.addFlashAttribute("success", "Class teacher updated.");
        return "redirect:/admin/assignments";
    }
}
