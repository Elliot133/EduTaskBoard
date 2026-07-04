package com.edutaskboard.controller;

import com.edutaskboard.config.CustomUserDetails;
import com.edutaskboard.model.LeaveRequest;
import com.edutaskboard.model.Notification;
import com.edutaskboard.model.User;
import com.edutaskboard.service.LeaveService;
import com.edutaskboard.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class LeaveController {

    @Autowired private LeaveService leaveService;
    @Autowired private NotificationService notificationService;

    private User me(Authentication a) { return ((CustomUserDetails) a.getPrincipal()).getUser(); }

    /* ── Teacher: submit and view own leave requests ── */
    @GetMapping("/teacher/leave")
    public String teacherLeave(Authentication auth, Model m) {
        m.addAttribute("requests", leaveService.byTeacher(me(auth)));
        return "teacher/leave";
    }

    @PostMapping("/teacher/leave/request")
    public String requestLeave(@RequestParam String startDate, @RequestParam String endDate,
                                @RequestParam String reason, Authentication auth, RedirectAttributes ra) {
        leaveService.request(me(auth), LocalDate.parse(startDate), LocalDate.parse(endDate), reason);
        ra.addFlashAttribute("success", "Leave request submitted for admin review.");
        return "redirect:/teacher/leave";
    }

    /* ── Admin: review leave requests ── */
    @GetMapping("/admin/leave")
    public String adminLeave(Model m) {
        m.addAttribute("requests", leaveService.getAll());
        return "admin/leave";
    }

    @PostMapping("/admin/leave/{id}/review")
    public String review(@PathVariable Long id, @RequestParam boolean approve,
                          @RequestParam(required = false) String note, Authentication auth, RedirectAttributes ra) {
        LeaveRequest lr = leaveService.review(id, approve, me(auth), note);
        notificationService.notify(lr.getTeacher(), Notification.NotificationType.LEAVE,
                "Leave Request " + (approve ? "Approved" : "Rejected"),
                "Your leave request (" + lr.getStartDate() + " to " + lr.getEndDate() + ") was " + (approve ? "approved" : "rejected") + ".",
                "/teacher/leave");
        ra.addFlashAttribute("success", "Leave request " + (approve ? "approved" : "rejected") + ".");
        return "redirect:/admin/leave";
    }
}
