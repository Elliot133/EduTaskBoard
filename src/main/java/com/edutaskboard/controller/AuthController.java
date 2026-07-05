package com.edutaskboard.controller;

import com.edutaskboard.model.User;
import com.edutaskboard.service.SchoolClassService;
import com.edutaskboard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private SchoolClassService classService;

    @GetMapping("/") public String home() { return "redirect:/login"; }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required=false) String error,
                            @RequestParam(required=false) String logout,
                            Model model) {
        if (error  != null) model.addAttribute("error",   "Invalid credentials or account pending approval.");
        if (logout != null) model.addAttribute("message", "You have been signed out.");
        return "auth/auth";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("classes", classService.getAll());
        return "auth/auth";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                             @RequestParam String fullName,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String role,
                             @RequestParam(required=false) Long classId,
                             @RequestParam(required=false) String subject,
                             @RequestParam(required=false) String parentName,
                             @RequestParam(required=false) String parentPhone,
                             RedirectAttributes ra, Model model) {

        if (userService.usernameExists(username)) {
            model.addAttribute("error", "Username already taken.");
            model.addAttribute("classes", classService.getAll());
            return "auth/auth";
        }
        if (userService.emailExists(email)) {
            model.addAttribute("error", "Email already registered.");
            model.addAttribute("classes", classService.getAll());
            return "auth/auth";
        }

        User u = new User();
        u.setUsername(username); u.setFullName(fullName);
        u.setEmail(email); u.setPassword(password);
        try { u.setRole(User.Role.valueOf(role.toUpperCase())); }
        catch (Exception e) { u.setRole(User.Role.STUDENT); }

        if (u.getRole() == User.Role.STUDENT) {
            if (classId != null) classService.byId(classId).ifPresent(u::setSchoolClass);
            u.setParentName(parentName); u.setParentPhone(parentPhone);
        } else if (u.getRole() == User.Role.TEACHER) {
            u.setSubject(subject);
        }

        userService.register(u);
        ra.addFlashAttribute("success",
            "Registration submitted! Await admin approval before logging in.");
        return "redirect:/login";
    }
}
