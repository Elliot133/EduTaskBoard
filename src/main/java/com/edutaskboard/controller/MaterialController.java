package com.edutaskboard.controller;

import com.edutaskboard.config.CustomUserDetails;
import com.edutaskboard.model.*;
import com.edutaskboard.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

/** Module 3: Learning Materials. Teachers upload; students view/download. */
@Controller
public class MaterialController {

    @Autowired private LearningMaterialService materialService;
    @Autowired private TeacherAssignmentService assignmentService;
    @Autowired private SchoolClassService classService;
    @Autowired private SubjectService subjectService;

    private User me(Authentication a) { return ((CustomUserDetails) a.getPrincipal()).getUser(); }

    /* ── Teacher ── */
    @GetMapping("/teacher/materials")
    public String teacherMaterials(Authentication auth, Model m) {
        User teacher = me(auth);
        List<TeacherAssignment> myAssignments = assignmentService.byTeacher(teacher);
        m.addAttribute("materials", materialService.byTeacher(teacher));
        m.addAttribute("assignments", myAssignments);
        return "teacher/materials";
    }

    @PostMapping("/teacher/materials/upload")
    public String upload(@RequestParam String title,
                          @RequestParam(required = false) String description,
                          @RequestParam Long classId,
                          @RequestParam(required = false) Long subjectId,
                          @RequestParam("file") MultipartFile file,
                          Authentication auth, RedirectAttributes ra) {
        User teacher = me(auth);
        SchoolClass sc = classService.byId(classId).orElse(null);
        if (sc == null || file.isEmpty()) {
            ra.addFlashAttribute("error", "Please select a class and a file.");
            return "redirect:/teacher/materials";
        }
        Subject subject = subjectId != null ? subjectService.byId(subjectId).orElse(null) : null;
        try {
            materialService.store(file, title, description, teacher, sc, subject);
            ra.addFlashAttribute("success", "Material uploaded.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        } catch (IOException ex) {
            ra.addFlashAttribute("error", "Upload failed: " + ex.getMessage());
        }
        return "redirect:/teacher/materials";
    }

    @PostMapping("/teacher/materials/{id}/delete")
    public String deleteMaterial(@PathVariable Long id, RedirectAttributes ra) {
        materialService.delete(id);
        ra.addFlashAttribute("success", "Material removed.");
        return "redirect:/teacher/materials";
    }

    /* ── Student ── */
    @GetMapping("/student/materials")
    public String studentMaterials(Authentication auth, Model m) {
        User student = me(auth);
        if (student.getSchoolClass() != null) {
            m.addAttribute("materials", materialService.byClass(student.getSchoolClass()));
        } else {
            m.addAttribute("materials", List.of());
        }
        return "student/materials";
    }

    /* ── Shared download endpoint (student view/download, teacher can re-download own uploads) ── */
    @GetMapping("/materials/{id}/download")
    @ResponseBody
    public ResponseEntity<Resource> download(@PathVariable Long id) throws MalformedURLException {
        LearningMaterial material = materialService.byId(id).orElse(null);
        if (material == null) return ResponseEntity.notFound().build();
        Path path = materialService.resolvePath(material);
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + material.getOriginalFileName() + "\"")
                .body(resource);
    }
}
