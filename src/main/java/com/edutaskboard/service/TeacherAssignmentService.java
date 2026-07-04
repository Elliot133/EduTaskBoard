package com.edutaskboard.service;

import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.Subject;
import com.edutaskboard.model.TeacherAssignment;
import com.edutaskboard.model.User;
import com.edutaskboard.repository.SchoolClassRepository;
import com.edutaskboard.repository.TeacherAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TeacherAssignmentService {
    @Autowired private TeacherAssignmentRepository repo;
    @Autowired private SchoolClassRepository classRepo;

    public List<TeacherAssignment> getAll() { return repo.findAll(); }
    public Optional<TeacherAssignment> byId(Long id) { return repo.findById(id); }
    public List<TeacherAssignment> byTeacher(User teacher) { return repo.findByTeacher(teacher); }
    public List<TeacherAssignment> byClass(SchoolClass c) { return repo.findBySchoolClass(c); }

    /** Subjects a teacher teaches within a specific class (used to scope timetable/materials/assessments). */
    public List<TeacherAssignment> byTeacherAndClass(User teacher, SchoolClass c) {
        return repo.findByTeacherAndSchoolClass(teacher, c);
    }

    /**
     * Assign a teacher to teach a subject in a class.
     * Throws IllegalStateException if the same (teacher, subject, class) combo already exists.
     */
    public TeacherAssignment assign(User teacher, Subject subject, SchoolClass schoolClass) {
        if (repo.existsByTeacherAndSubjectAndSchoolClass(teacher, subject, schoolClass)) {
            throw new IllegalStateException("This teacher is already assigned to this subject for this class.");
        }
        TeacherAssignment ta = new TeacherAssignment();
        ta.setTeacher(teacher); ta.setSubject(subject); ta.setSchoolClass(schoolClass);
        return repo.save(ta);
    }

    public void remove(Long id) { repo.deleteById(id); }

    /**
     * Sets (or changes) the class teacher for a class. Exactly one class teacher per class —
     * simply overwrite the classTeacher field on SchoolClass, which enforces the 1:1 rule.
     */
    public SchoolClass setClassTeacher(Long classId, Long teacherId, User teacher) {
        SchoolClass c = classRepo.findById(classId).orElseThrow(() -> new IllegalArgumentException("Class not found"));
        c.setClassTeacher(teacher);
        return classRepo.save(c);
    }

    public long countAll() { return repo.count(); }
}
