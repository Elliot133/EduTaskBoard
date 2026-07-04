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
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AttendanceController {

    @Autowired private AttendanceService attendanceService;
    @Autowired private SchoolClassService classService;
    @Autowired private TeacherAssignmentService assignmentService;
    @Autowired private NotificationService notificationService;

    private User me(Authentication a) { return ((CustomUserDetails) a.getPrincipal()).getUser(); }

    /* ── Teacher: mark attendance for a class they teach ── */
    @GetMapping("/teacher/attendance")
    public String teacherAttendance(@RequestParam(required = false) Long classId,
                                     @RequestParam(required = false) String date,
                                     Authentication auth, Model m) {
        User teacher = me(auth);
        List<SchoolClass> myClasses = assignmentService.byTeacher(teacher).stream()
                .map(TeacherAssignment::getSchoolClass)
                .collect(Collectors.toMap(SchoolClass::getId, c -> c, (a, b) -> a))
                .values().stream().sorted(Comparator.comparing(SchoolClass::getName)).collect(Collectors.toList());

        LocalDate d = (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now();
        SchoolClass selected = classId != null ? classService.byId(classId).orElse(null)
                : (myClasses.isEmpty() ? null : myClasses.get(0));

        m.addAttribute("myClasses", myClasses);
        m.addAttribute("selectedClass", selected);
        m.addAttribute("date", d);
        if (selected != null) {
            List<User> students = selected.getStudents();
            Map<Long, AttendanceRecord> existing = attendanceService.forClassAndDate(selected, d).stream()
                    .collect(Collectors.toMap(r -> r.getStudent().getId(), r -> r));
            m.addAttribute("students", students);
            m.addAttribute("existing", existing);
        }
        return "teacher/attendance";
    }

    @PostMapping("/teacher/attendance/save")
    public String saveAttendance(@RequestParam Long classId, @RequestParam String date,
                                  @RequestParam List<Long> studentIds, @RequestParam List<String> statuses,
                                  Authentication auth, RedirectAttributes ra) {
        User teacher = me(auth);
        SchoolClass sc = classService.byId(classId).orElse(null);
        if (sc == null) { ra.addFlashAttribute("error", "Invalid class."); return "redirect:/teacher/attendance"; }
        LocalDate d = LocalDate.parse(date);

        Map<Long, User> studentMap = sc.getStudents().stream().collect(Collectors.toMap(User::getId, u -> u));
        for (int i = 0; i < studentIds.size(); i++) {
            User student = studentMap.get(studentIds.get(i));
            if (student == null) continue;
            AttendanceRecord.Status status = AttendanceRecord.Status.valueOf(statuses.get(i));
            attendanceService.mark(student, sc, d, status, teacher, null);
            if (status == AttendanceRecord.Status.ABSENT) {
                notificationService.notify(student, Notification.NotificationType.ATTENDANCE,
                        "Marked Absent", "You were marked absent on " + d + ".", "/student/attendance");
            }
        }
        ra.addFlashAttribute("success", "Attendance saved for " + d + ".");
        return "redirect:/teacher/attendance?classId=" + classId + "&date=" + date;
    }

    /* ── Student: view own attendance ── */
    @GetMapping("/student/attendance")
    public String studentAttendance(Authentication auth, Model m) {
        User student = me(auth);
        m.addAttribute("records", attendanceService.forStudent(student));
        m.addAttribute("percentage", attendanceService.attendancePercentage(student));
        m.addAttribute("summary", attendanceService.summary(student));
        return "student/attendance";
    }

    /* ── Admin: overview by class ── */
    @GetMapping("/admin/attendance")
    public String adminAttendance(@RequestParam(required = false) Long classId, Model m) {
        List<SchoolClass> classes = classService.getAll();
        SchoolClass selected = classId != null ? classService.byId(classId).orElse(null)
                : (classes.isEmpty() ? null : classes.get(0));
        m.addAttribute("classes", classes);
        m.addAttribute("selectedClass", selected);
        if (selected != null) {
            m.addAttribute("records", attendanceService.forClass(selected));
        }
        return "admin/attendance";
    }
}
