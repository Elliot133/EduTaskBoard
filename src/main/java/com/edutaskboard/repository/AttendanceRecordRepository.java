package com.edutaskboard.repository;

import com.edutaskboard.model.AttendanceRecord;
import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findBySchoolClassAndDate(SchoolClass schoolClass, LocalDate date);
    List<AttendanceRecord> findByStudentOrderByDateDesc(User student);
    Optional<AttendanceRecord> findByStudentAndDate(User student, LocalDate date);
    List<AttendanceRecord> findBySchoolClassOrderByDateDesc(SchoolClass schoolClass);
}
