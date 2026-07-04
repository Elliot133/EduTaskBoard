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

@Controller @RequestMapping("/admin")
public class AdminController {

    @Autowired private UserService us;
    @Autowired private TaskService ts;
    @Autowired private SubmissionService ss;
    @Autowired private AnnouncementService as;
    @Autowired private SchoolClassService cs;

    private User me(Authentication a) { return ((CustomUserDetails)a.getPrincipal()).getUser(); }

    @GetMapping("/dashboard")
    public String dashboard(Model m) {
        m.addAttribute("totalStudents",    us.countByRole(User.Role.STUDENT));
        m.addAttribute("totalTeachers",    us.countByRole(User.Role.TEACHER));
        m.addAttribute("totalTasks",       ts.countAll());
        m.addAttribute("totalClasses",     cs.countAll());
        m.addAttribute("totalSubmissions", ss.countAll());
        m.addAttribute("pendingCount",     us.countPending());
        m.addAttribute("announcements",    as.getAll());
        m.addAttribute("allTasks",         ts.getAll());
        return "admin/dashboard";
    }

    /* ── Users ── */
    @GetMapping("/users")
    public String users(Model m) {
        m.addAttribute("students", us.byRole(User.Role.STUDENT));
        m.addAttribute("teachers", us.byRole(User.Role.TEACHER));
        m.addAttribute("pending",  us.pending());
        m.addAttribute("classes",  cs.getAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        us.approve(id); ra.addFlashAttribute("success","User approved."); return "redirect:/admin/users";
    }
    @PostMapping("/users/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes ra) {
        us.reject(id); ra.addFlashAttribute("success","User rejected."); return "redirect:/admin/users";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model m) {
        m.addAttribute("classes", cs.getAll()); return "admin/user-form";
    }

    @PostMapping("/users/save")
    public String saveUser(@RequestParam String username, @RequestParam String fullName,
                           @RequestParam String email,    @RequestParam String password,
                           @RequestParam String role,
                           @RequestParam(required=false) Long classId,
                           @RequestParam(required=false) String subject,
                           @RequestParam(required=false) String qualification,
                           @RequestParam(required=false) String studentId,
                           @RequestParam(required=false) String parentName,
                           @RequestParam(required=false) String parentPhone,
                           RedirectAttributes ra) {
        if (us.usernameExists(username)) { ra.addFlashAttribute("error","Username taken."); return "redirect:/admin/users/new"; }
        User u = new User();
        u.setUsername(username); u.setFullName(fullName); u.setEmail(email);
        u.setPassword(password); u.setRole(User.Role.valueOf(role));
        u.setSubject(subject); u.setQualification(qualification);
        u.setStudentId(studentId); u.setParentName(parentName); u.setParentPhone(parentPhone);
        if (classId != null) cs.byId(classId).ifPresent(u::setSchoolClass);
        us.create(u);
        ra.addFlashAttribute("success","User created."); return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        us.delete(id); ra.addFlashAttribute("success","User deleted."); return "redirect:/admin/users";
    }

    /* ── Classes ── */
    @GetMapping("/classes")
    public String classes(Model m) {
        m.addAttribute("classes",  cs.getAll());
        m.addAttribute("teachers", us.byRole(User.Role.TEACHER));
        return "admin/classes";
    }

    @PostMapping("/classes/save")
    public String saveClass(@RequestParam String name,
                            @RequestParam(required=false) String description,
                            @RequestParam(required=false) Integer academicYear,
                            @RequestParam(required=false) Long teacherId,
                            RedirectAttributes ra) {
        SchoolClass c = new SchoolClass();
        c.setName(name); c.setDescription(description);
        c.setAcademicYear(academicYear != null ? academicYear : 2025);
        if (teacherId != null) us.byId(teacherId).ifPresent(c::setClassTeacher);
        cs.save(c); ra.addFlashAttribute("success","Class saved."); return "redirect:/admin/classes";
    }

    @PostMapping("/classes/{id}/delete")
    public String deleteClass(@PathVariable Long id, RedirectAttributes ra) {
        cs.delete(id); ra.addFlashAttribute("success","Class deleted."); return "redirect:/admin/classes";
    }

    /* ── Announcements ── */
    @GetMapping("/announcements")
    public String announcements(Model m) { m.addAttribute("announcements", as.getAll()); return "admin/announcements"; }

    @PostMapping("/announcements/save")
    public String saveAnn(@RequestParam String title, @RequestParam String content,
                          @RequestParam String audience,
                          @RequestParam(defaultValue="false") boolean pinned,
                          Authentication auth, RedirectAttributes ra) {
        Announcement a = new Announcement();
        a.setTitle(title); a.setContent(content);
        a.setAudience(Announcement.AudienceType.valueOf(audience));
        a.setPinned(pinned); a.setAuthor(me(auth));
        as.save(a); ra.addFlashAttribute("success","Announcement posted."); return "redirect:/admin/announcements";
    }

    @PostMapping("/announcements/{id}/delete")
    public String deleteAnn(@PathVariable Long id, RedirectAttributes ra) {
        as.delete(id); ra.addFlashAttribute("success","Deleted."); return "redirect:/admin/announcements";
    }

    /* ── Tasks overview ── */
    @GetMapping("/tasks")
    public String tasks(Model m) { m.addAttribute("tasks", ts.getAll()); return "admin/tasks"; }

    /* ── Messages ── */
    @GetMapping("/messages")
    public String messages() { return "redirect:/messages"; }
}
