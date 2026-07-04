package com.edutaskboard.repository;

import com.edutaskboard.model.ReportCard;
import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportCardRepository extends JpaRepository<ReportCard, Long> {
    Optional<ReportCard> findByStudentAndSchoolClass(User student, SchoolClass schoolClass);
    List<ReportCard> findBySchoolClass(SchoolClass schoolClass);
    List<ReportCard> findByStudent(User student);
}
