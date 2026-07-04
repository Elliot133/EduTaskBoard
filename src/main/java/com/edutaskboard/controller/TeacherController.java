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
import java.time.LocalDateTime;
import java.util.List;

@Controller @RequestMapping("/teacher")
public class TeacherController {

    @Autowired private UserService us;
    @Autowired private TaskService ts;
    @Autowired private SubmissionService ss;
    @Autowired private AnnouncementService as;
    @Autowired private SchoolClassService cs;

    private User me(Authentication a) { return ((CustomUserDetails)a.getPrincipal()).getUser(); }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model m) {
        User teacher = me(auth);
        List<Task> tasks = ts.byTeacher(teacher);
        m.addAttribute("teacher", teacher);
        m.addAttribute("myTasks", tasks);
        m.addAttribute("taskCount", tasks.size());
        m.addAttribute("myClasses", cs.byTeacher(teacher));
        m.addAttribute("announcements", as.forAudience(Announcement.AudienceType.TEACHERS));
        return "teacher/dashboard";
    }

    @GetMapping("/tasks")
    public String tasks(Authentication auth, Model m) {
        User t = me(auth);
        m.addAttribute("tasks",   ts.byTeacher(t));
        m.addAttribute("classes", cs.byTeacher(t));
        return "teacher/tasks";
    }

    @GetMapping("/tasks/new")
    public String newTask(Authentication auth, Model m) {
        m.addAttribute("classes", cs.byTeacher(me(auth)));
        m.addAttribute("task", new Task());
        return "teacher/task-form";
    }

    @GetMapping("/tasks/{id}/edit")
    public String editTask(@PathVariable Long id, Authentication auth, Model m) {
        ts.byId(id).ifPresent(t -> m.addAttribute("task", t));
        m.addAttribute("classes", cs.byTeacher(me(auth)));
        return "teacher/task-form";
    }

    @PostMapping("/tasks/save")
    public String saveTask(@RequestParam(required=false) Long taskId,
                           @RequestParam String title,
                           @RequestParam(required=false) String description,
                           @RequestParam String dueDate,
                           @RequestParam String priority,
                           @RequestParam Long classId,
                           @RequestParam(required=false) String subject,
                           @RequestParam(required=false) Integer maxScore,
                           @RequestParam(required=false) String status,
                           Authentication auth, RedirectAttributes ra) {
        Task task = (taskId != null) ? ts.byId(taskId).orElse(new Task()) : new Task();
        task.setTitle(title); task.setDescription(description);
        task.setDueDate(LocalDate.parse(dueDate));
        task.setPriority(Task.Priority.valueOf(priority));
        task.setSubject(subject); task.setMaxScore(maxScore);
        task.setStatus(status != null ? Task.TaskStatus.valueOf(status) : Task.TaskStatus.ACTIVE);
        task.setTeacher(me(auth));
        cs.byId(classId).ifPresent(task::setSchoolClass);
        ts.save(task);
        ra.addFlashAttribute("success","Task saved."); return "redirect:/teacher/tasks";
    }

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id, RedirectAttributes ra) {
        ts.delete(id); ra.addFlashAttribute("success","Deleted."); return "redirect:/teacher/tasks";
    }

    @GetMapping("/tasks/{id}/submissions")
    public String submissions(@PathVariable Long id, Model m) {
        ts.byId(id).ifPresent(t -> { m.addAttribute("task",t); m.addAttribute("submissions",ss.byTask(t)); });
        return "teacher/submissions";
    }

    @GetMapping("/submissions/{id}/grade")
    public String gradeForm(@PathVariable Long id, Model m) {
        ss.byId(id).ifPresent(s -> m.addAttribute("submission", s)); return "teacher/grade-form";
    }

    @PostMapping("/submissions/{id}/grade")
    public String submitGrade(@PathVariable Long id, @RequestParam Integer score,
                              @RequestParam String teacherFeedback, RedirectAttributes ra) {
        ss.byId(id).ifPresent(s -> {
            s.setScore(score); s.setTeacherFeedback(teacherFeedback);
            s.setStatus(TaskSubmission.SubmissionStatus.GRADED);
            s.setGradedAt(LocalDateTime.now()); ss.save(s);
        });
        ra.addFlashAttribute("success","Graded."); return "redirect:/teacher/tasks";
    }

    @GetMapping("/students")
    public String students(Authentication auth, Model m) {
        m.addAttribute("myClasses", cs.byTeacher(me(auth))); return "teacher/students";
    }

    @GetMapping("/announcements")
    public String announcements(Model m) {
        m.addAttribute("announcements", as.forAudience(Announcement.AudienceType.TEACHERS));
        return "teacher/announcements";
    }

    @GetMapping("/messages")
    public String messages() { return "redirect:/messages"; }
}
