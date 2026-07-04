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
import java.util.Set;
import java.util.stream.Collectors;

@Controller @RequestMapping("/student")
public class StudentController {

    @Autowired private UserService us;
    @Autowired private TaskService ts;
    @Autowired private SubmissionService ss;
    @Autowired private AnnouncementService as;
    @Autowired private com.edutaskboard.service.AttendanceService attendanceService;

    private User me(Authentication a) { return ((CustomUserDetails)a.getPrincipal()).getUser(); }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model m) {
        User student = me(auth);
        List<Task> tasks = ts.forStudent(student);
        List<TaskSubmission> subs = ss.byStudent(student);
        m.addAttribute("student", student);
        m.addAttribute("myTasks", tasks);
        m.addAttribute("taskCount",      tasks.size());
        m.addAttribute("submittedCount", subs.size());
        m.addAttribute("gradedCount",    ss.countGraded(student));
        m.addAttribute("avgScore",       ss.avgScore(student));
        m.addAttribute("announcements",  as.forAudience(Announcement.AudienceType.STUDENTS));
        m.addAttribute("attendancePercentage", attendanceService.attendancePercentage(student));
        return "student/dashboard";
    }

    @GetMapping("/tasks")
    public String tasks(Authentication auth, Model m) {
        User student = me(auth);
        List<Task> tasks = ts.forStudent(student);
        Set<Long> submittedIds = ss.byStudent(student).stream()
                .map(s -> s.getTask().getId()).collect(Collectors.toSet());
        m.addAttribute("tasks", tasks);
        m.addAttribute("submittedIds", submittedIds);
        return "student/tasks";
    }

    @GetMapping("/tasks/{id}")
    public String viewTask(@PathVariable Long id, Authentication auth, Model m) {
        User student = me(auth);
        ts.byId(id).ifPresent(task -> {
            m.addAttribute("task", task);
            boolean submitted = ss.hasSubmitted(task, student);
            m.addAttribute("submitted", submitted);
            if (submitted) ss.byTaskAndStudent(task, student).ifPresent(s -> m.addAttribute("submission", s));
        });
        return "student/task-detail";
    }

    @PostMapping("/tasks/{id}/submit")
    public String submitTask(@PathVariable Long id, @RequestParam String content,
                             Authentication auth, RedirectAttributes ra) {
        User student = me(auth);
        ts.byId(id).ifPresent(task -> {
            if (!ss.hasSubmitted(task, student)) {
                TaskSubmission sub = new TaskSubmission();
                sub.setTask(task); sub.setStudent(student);
                sub.setContent(content); sub.setStatus(TaskSubmission.SubmissionStatus.SUBMITTED);
                ss.save(sub);
            }
        });
        ra.addFlashAttribute("success","Assignment submitted!"); return "redirect:/student/tasks";
    }

    @GetMapping("/grades")
    public String grades(Authentication auth, Model m) {
        User student = me(auth);
        m.addAttribute("submissions", ss.byStudent(student));
        m.addAttribute("avgScore", ss.avgScore(student));
        return "student/grades";
    }

    @GetMapping("/announcements")
    public String announcements(Model m) {
        m.addAttribute("announcements", as.forAudience(Announcement.AudienceType.STUDENTS));
        return "student/announcements";
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model m) {
        m.addAttribute("student", me(auth)); return "student/profile";
    }

    @GetMapping("/messages")
    public String messages() { return "redirect:/messages"; }
}
