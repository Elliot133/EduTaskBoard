package com.edutaskboard.repository;

import com.edutaskboard.model.LearningMaterial;
import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LearningMaterialRepository extends JpaRepository<LearningMaterial, Long> {
    List<LearningMaterial> findBySchoolClassOrderByUploadedAtDesc(SchoolClass schoolClass);
    List<LearningMaterial> findByTeacherOrderByUploadedAtDesc(User teacher);
}
