package com.edutaskboard.controller;

import com.edutaskboard.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Admin read-only overview of all assessments across the school. */
@Controller
@RequestMapping("/admin/assessments")
public class AdminAssessmentController {

    @Autowired private AssessmentService assessmentService;

    @GetMapping
    public String list(Model m) {
        m.addAttribute("assessments", assessmentService.getAll());
        return "admin/assessments";
    }
}
