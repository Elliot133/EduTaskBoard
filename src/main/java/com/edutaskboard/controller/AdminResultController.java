package com.edutaskboard.controller;

import com.edutaskboard.model.ReportCard;
import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.service.ReportCardPdfService;
import com.edutaskboard.service.ResultService;
import com.edutaskboard.service.SchoolClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/results")
public class AdminResultController {

    @Autowired private ResultService resultService;
    @Autowired private SchoolClassService classService;
    @Autowired private ReportCardPdfService pdfService;

    @GetMapping
    public String list(@RequestParam(required = false) Long classId, Model m) {
        List<SchoolClass> classes = classService.getAll();
        SchoolClass selected = classId != null ? classService.byId(classId).orElse(null)
                : (classes.isEmpty() ? null : classes.get(0));
        m.addAttribute("classes", classes);
        m.addAttribute("selectedClass", selected);
        if (selected != null) {
            m.addAttribute("reportCards", resultService.reportCardsFor(selected));
        }
        return "admin/results";
    }

    @PostMapping("/principal-remark")
    public String saveRemark(@RequestParam Long reportCardId, @RequestParam String remark, RedirectAttributes ra) {
        resultService.savePrincipalRemark(reportCardId, remark);
        ra.addFlashAttribute("success", "Principal's remark saved.");
        return "redirect:/admin/results";
    }

    @GetMapping("/{reportCardId}/pdf")
    @ResponseBody
    public ResponseEntity<byte[]> pdf(@PathVariable Long reportCardId) {
        for (SchoolClass c : classService.getAll()) {
            for (ReportCard rc : resultService.reportCardsFor(c)) {
                if (rc.getId().equals(reportCardId)) {
                    byte[] pdf = pdfService.generatePdf(rc.getStudent(), c);
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_PDF)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report-card-" + rc.getStudent().getUsername() + ".pdf\"")
                            .body(pdf);
                }
            }
        }
        return ResponseEntity.notFound().build();
    }
}
