package com.edutaskboard.controller;

import com.edutaskboard.model.*;
import com.edutaskboard.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/substitutes")
public class SubstituteController {

    @Autowired private SubstituteService substituteService;
    @Autowired private SchoolClassService classService;
    @Autowired private UserService userService;
    @Autowired private NotificationService notificationService;

    @GetMapping
    public String list(Model m) {
        m.addAttribute("assignments", substituteService.getAll());
        m.addAttribute("classes", classService.getAll());
        m.addAttribute("teachers", userService.byRole(User.Role.TEACHER));
        m.addAttribute("days", TimetableSlotService.SCHOOL_DAYS);
        return "admin/substitutes";
    }

    /** AJAX lookup: which teachers are free for this class's lesson on this day/period. */
    @GetMapping("/available")
    @ResponseBody
    public List<AvailableTeacherDto> available(@RequestParam Long classId, @RequestParam String day,
                                                @RequestParam int periodNumber, @RequestParam Long originalTeacherId,
                                                @RequestParam String date) {
        User original = userService.byId(originalTeacherId).orElse(null);
        List<User> teachers = substituteService.availableSubstitutes(
                TimetableSlot.SchoolDay.valueOf(day), periodNumber, original, LocalDate.parse(date));
        return teachers.stream().map(t -> new AvailableTeacherDto(t.getId(), t.getFullName())).toList();
    }

    @PostMapping("/assign")
    public String assign(@RequestParam Long classId, @RequestParam String day, @RequestParam int periodNumber,
                          @RequestParam Long originalTeacherId, @RequestParam Long substituteTeacherId,
                          @RequestParam String startDate, @RequestParam String endDate,
                          @RequestParam(required = false) String reason, RedirectAttributes ra) {
        SchoolClass sc = classService.byId(classId).orElse(null);
        User original = userService.byId(originalTeacherId).orElse(null);
        User substitute = userService.byId(substituteTeacherId).orElse(null);
        if (sc == null || original == null || substitute == null) {
            ra.addFlashAttribute("error", "Invalid class or teacher selection.");
            return "redirect:/admin/substitutes";
        }
        try {
            substituteService.assign(sc, TimetableSlot.SchoolDay.valueOf(day), periodNumber, original, substitute,
                    LocalDate.parse(startDate), LocalDate.parse(endDate), reason);

            notificationService.notify(substitute, Notification.NotificationType.TIMETABLE,
                    "Substitute Teaching Assignment",
                    "You've been assigned to substitute for " + original.getFullName() + " in " + sc.getName()
                            + " (" + day + ", period " + periodNumber + ") from " + startDate + " to " + endDate + ".",
                    "/teacher/timetable");
            for (User student : sc.getStudents()) {
                notificationService.notify(student, Notification.NotificationType.TIMETABLE,
                        "Substitute Teacher Assigned",
                        substitute.getFullName() + " will cover your " + day + " period " + periodNumber + " lesson.",
                        "/student/timetable");
            }
            ra.addFlashAttribute("success", "Substitute teacher assigned and notified.");
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/substitutes";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        substituteService.cancel(id);
        ra.addFlashAttribute("success", "Substitute assignment cancelled — original timetable restored.");
        return "redirect:/admin/substitutes";
    }

    public record AvailableTeacherDto(Long id, String fullName) {}
}
