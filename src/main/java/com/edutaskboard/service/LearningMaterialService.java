package com.edutaskboard.service;

import com.edutaskboard.model.LearningMaterial;
import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.User;
import com.edutaskboard.repository.LearningMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LearningMaterialService {
    @Autowired private LearningMaterialRepository repo;

    @Value("${app.upload.dir:uploads/materials}")
    private String uploadDir;

    public List<LearningMaterial> getAll() { return repo.findAll(); }
    public Optional<LearningMaterial> byId(Long id) { return repo.findById(id); }
    public List<LearningMaterial> byClass(SchoolClass c) { return repo.findBySchoolClassOrderByUploadedAtDesc(c); }
    public List<LearningMaterial> byTeacher(User t) { return repo.findByTeacherOrderByUploadedAtDesc(t); }

    private static final List<String> ALLOWED_EXT = List.of("pdf", "docx", "pptx", "png", "jpg", "jpeg", "gif", "webp");

    public LearningMaterial store(MultipartFile file, String title, String description,
                                   User teacher, SchoolClass schoolClass,
                                   com.edutaskboard.model.Subject subject) throws IOException {
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String ext = getExtension(original).toLowerCase();
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("Unsupported file type: ." + ext);
        }

        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);

        String stored = UUID.randomUUID() + "." + ext;
        Path target = dir.resolve(stored);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        LearningMaterial m = new LearningMaterial();
        m.setTitle(title);
        m.setDescription(description);
        m.setStoredFileName(stored);
        m.setOriginalFileName(original);
        m.setFileType(resolveType(ext));
        m.setFileSize(file.getSize());
        m.setTeacher(teacher);
        m.setSchoolClass(schoolClass);
        m.setSubject(subject);
        return repo.save(m);
    }

    public Path resolvePath(LearningMaterial m) {
        return Paths.get(uploadDir).resolve(m.getStoredFileName());
    }

    public void delete(Long id) {
        repo.findById(id).ifPresent(m -> {
            try { Files.deleteIfExists(resolvePath(m)); } catch (IOException ignored) {}
            repo.deleteById(id);
        });
    }

    private String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        return (i >= 0 && i < filename.length() - 1) ? filename.substring(i + 1) : "";
    }

    private LearningMaterial.FileType resolveType(String ext) {
        return switch (ext) {
            case "pdf" -> LearningMaterial.FileType.PDF;
            case "docx" -> LearningMaterial.FileType.DOCX;
            case "pptx" -> LearningMaterial.FileType.PPTX;
            case "png", "jpg", "jpeg", "gif", "webp" -> LearningMaterial.FileType.IMAGE;
            default -> LearningMaterial.FileType.OTHER;
        };
    }

    public long countAll() { return repo.count(); }
}
