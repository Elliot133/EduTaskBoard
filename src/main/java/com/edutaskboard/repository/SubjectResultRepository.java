package com.edutaskboard.repository;

import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.Subject;
import com.edutaskboard.model.SubjectResult;
import com.edutaskboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectResultRepository extends JpaRepository<SubjectResult, Long> {
    Optional<SubjectResult> findByStudentAndSubjectAndSchoolClass(User student, Subject subject, SchoolClass schoolClass);
    List<SubjectResult> findBySubjectAndSchoolClass(Subject subject, SchoolClass schoolClass);
    List<SubjectResult> findByStudentAndSchoolClass(User student, SchoolClass schoolClass);
    List<SubjectResult> findBySchoolClass(SchoolClass schoolClass);
}
