package com.edutaskboard.controller;

import com.edutaskboard.config.CustomUserDetails;
import com.edutaskboard.model.User;
import com.edutaskboard.service.ReportCardPdfService;
import com.edutaskboard.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@org.springframework.web.bind.annotation.RequestMapping("/student/results")
public class StudentResultController {

    @Autowired private ResultService resultService;
    @Autowired private ReportCardPdfService pdfService;

    private User me(Authentication a) { return ((CustomUserDetails) a.getPrincipal()).getUser(); }

    @GetMapping
    public String myResults(Authentication auth, Model m) {
        User student = me(auth);
        if (student.getSchoolClass() == null) {
            m.addAttribute("results", java.util.List.of());
            return "student/results";
        }
        m.addAttribute("schoolClass", student.getSchoolClass());
        m.addAttribute("results", resultService.subjectResultsFor(student, student.getSchoolClass()));
        m.addAttribute("reportCard", resultService.reportCardFor(student, student.getSchoolClass()).orElse(null));
        return "student/results";
    }

    @GetMapping("/pdf")
    @ResponseBody
    public ResponseEntity<byte[]> downloadPdf(Authentication auth) {
        User student = me(auth);
        if (student.getSchoolClass() == null) return ResponseEntity.notFound().build();
        byte[] pdf = pdfService.generatePdf(student, student.getSchoolClass());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report-card.pdf\"")
                .body(pdf);
    }
}
