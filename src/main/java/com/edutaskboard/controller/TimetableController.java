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

import java.time.LocalTime;
import java.util.Map;

/** Complete redesign: fixed 30-minute periods, auto breaks, free periods, drag-and-drop moves. */
@Controller
public class TimetableController {

    @Autowired private TimetableSlotService timetableService;
    @Autowired private SchoolSettingsService settingsService;
    @Autowired private SchoolClassService classService;
    @Autowired private SubjectService subjectService;
    @Autowired private UserService userService;

    private User me(Authentication a) { return ((CustomUserDetails) a.getPrincipal()).getUser(); }

    /* ── Admin ── */
    @GetMapping("/admin/timetable")
    public String adminTimetable(@RequestParam(required = false) Long classId, Model m) {
        var classes = classService.getAll();
        SchoolClass selected = classId != null ? classService.byId(classId).orElse(classes.isEmpty() ? null : classes.get(0))
                : (classes.isEmpty() ? null : classes.get(0));
        java.util.Map<TimetableSlot.SchoolDay, java.util.List<com.edutaskboard.dto.TimetableRow>> schedule;
        if (selected != null) {
            schedule = timetableService.getClassWeekSchedule(selected);
        } else {
            schedule = new java.util.LinkedHashMap<>();
        }
        m.addAttribute("classes", classes);
        m.addAttribute("selectedClass", selected);
        m.addAttribute("schedule", schedule);
        m.addAttribute("subjects", subjectService.getAll());
        m.addAttribute("teachers", userService.byRole(User.Role.TEACHER));
        m.addAttribute("days", TimetableSlotService.SCHOOL_DAYS);
        m.addAttribute("settings", settingsService.get());
        return "admin/timetable";
    }

    @PostMapping("/admin/timetable/settings")
    public String updateStartTime(@RequestParam String startTime, RedirectAttributes ra) {
        settingsService.updateStartTime(LocalTime.parse(startTime));
        ra.addFlashAttribute("success", "School start time updated — every timetable was recalculated instantly.");
        return "redirect:/admin/timetable";
    }

    @PostMapping("/admin/timetable/save")
    public String saveSlot(@RequestParam Long classId,
                            @RequestParam String day,
                            @RequestParam int periodNumber,
                            @RequestParam(required = false) Long subjectId,
                            @RequestParam(required = false) Long teacherId,
                            RedirectAttributes ra) {
        SchoolClass sc = classService.byId(classId).orElse(null);
        if (sc == null) { ra.addFlashAttribute("error", "Invalid class."); return "redirect:/admin/timetable"; }

        TimetableSlot slot = new TimetableSlot();
        slot.setSchoolClass(sc);
        slot.setDay(TimetableSlot.SchoolDay.valueOf(day));
        slot.setPeriodNumber(periodNumber);
        if (subjectId != null) subjectService.byId(subjectId).ifPresent(slot::setSubject);
        if (teacherId != null) userService.byId(teacherId).ifPresent(slot::setTeacher);

        String error = timetableService.validate(slot);
        if (error != null) {
            ra.addFlashAttribute("error", error);
        } else {
            timetableService.save(slot);
            ra.addFlashAttribute("success", "Timetable updated.");
        }
        return "redirect:/admin/timetable?classId=" + classId;
    }

    @PostMapping("/admin/timetable/{id}/clear")
    public String clearSlot(@PathVariable Long id, @RequestParam Long classId, RedirectAttributes ra) {
        timetableService.byId(id).ifPresent(slot -> { slot.setSubject(null); slot.setTeacher(null); timetableService.save(slot); });
        ra.addFlashAttribute("success", "Period cleared — now a Free Period.");
        return "redirect:/admin/timetable?classId=" + classId;
    }

    @PostMapping("/admin/timetable/move")
    @ResponseBody
    public Map<String, String> moveSlot(@RequestParam Long slotId, @RequestParam String newDay, @RequestParam int newPeriod) {
        String error = timetableService.move(slotId, TimetableSlot.SchoolDay.valueOf(newDay), newPeriod);
        return error != null ? Map.of("status", "error", "message", error) : Map.of("status", "ok");
    }

    /* ── Teacher ── */
    @GetMapping("/teacher/timetable")
    public String teacherTimetable(Authentication auth, Model m) {
        User teacher = me(auth);
        m.addAttribute("schedule", timetableService.getTeacherWeekSchedule(teacher));
        return "teacher/timetable";
    }

    /* ── Student ── */
    @GetMapping("/student/timetable")
    public String studentTimetable(Authentication auth, Model m) {
        User student = me(auth);
        if (student.getSchoolClass() != null) {
            m.addAttribute("schedule", timetableService.getClassWeekSchedule(student.getSchoolClass()));
            m.addAttribute("today", timetableService.getClassTodaySchedule(student.getSchoolClass()));
        } else {
            m.addAttribute("schedule", Map.of());
            m.addAttribute("today", java.util.List.of());
        }
        return "student/timetable";
    }
}
