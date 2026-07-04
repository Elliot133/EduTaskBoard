package com.edutaskboard.repository;

import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.SubstituteAssignment;
import com.edutaskboard.model.TimetableSlot;
import com.edutaskboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubstituteAssignmentRepository extends JpaRepository<SubstituteAssignment, Long> {
    List<SubstituteAssignment> findBySchoolClassAndDayAndPeriodNumberAndCancelledFalse(
            SchoolClass schoolClass, TimetableSlot.SchoolDay day, int periodNumber);
    List<SubstituteAssignment> findBySubstituteTeacherAndCancelledFalse(User teacher);
    List<SubstituteAssignment> findByOriginalTeacherAndCancelledFalse(User teacher);
    List<SubstituteAssignment> findAllByOrderByCreatedAtDesc();
}
