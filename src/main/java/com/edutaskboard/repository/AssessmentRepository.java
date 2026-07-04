package com.edutaskboard.repository;

import com.edutaskboard.model.Assessment;
import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findByTeacher(User teacher);
    List<Assessment> findBySchoolClass(SchoolClass schoolClass);
    List<Assessment> findBySchoolClassAndPublishedTrue(SchoolClass schoolClass);
    Optional<Assessment> findBySubjectAndSchoolClassAndType(com.edutaskboard.model.Subject subject, SchoolClass schoolClass, Assessment.AssessmentType type);
}
