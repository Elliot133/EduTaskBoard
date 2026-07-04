package com.edutaskboard.repository;

import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.Subject;
import com.edutaskboard.model.TeacherAssignment;
import com.edutaskboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long> {
    List<TeacherAssignment> findByTeacher(User teacher);
    List<TeacherAssignment> findBySchoolClass(SchoolClass schoolClass);
    List<TeacherAssignment> findBySubject(Subject subject);
    Optional<TeacherAssignment> findByTeacherAndSubjectAndSchoolClass(User teacher, Subject subject, SchoolClass schoolClass);
    boolean existsByTeacherAndSubjectAndSchoolClass(User teacher, Subject subject, SchoolClass schoolClass);
    List<TeacherAssignment> findByTeacherAndSchoolClass(User teacher, SchoolClass schoolClass);
}
