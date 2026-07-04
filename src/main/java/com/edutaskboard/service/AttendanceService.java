package com.edutaskboard.service;

import com.edutaskboard.model.AttendanceRecord;
import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.User;
import com.edutaskboard.repository.AttendanceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceService {
    @Autowired private AttendanceRecordRepository repo;

    public List<AttendanceRecord> forClassAndDate(SchoolClass schoolClass, LocalDate date) {
        return repo.findBySchoolClassAndDate(schoolClass, date);
    }

    public List<AttendanceRecord> forStudent(User student) { return repo.findByStudentOrderByDateDesc(student); }

    public List<AttendanceRecord> forClass(SchoolClass schoolClass) { return repo.findBySchoolClassOrderByDateDesc(schoolClass); }

    /** Marks (or updates) attendance for one student on one date. */
    public AttendanceRecord mark(User student, SchoolClass schoolClass, LocalDate date,
                                  AttendanceRecord.Status status, User markedBy, String remarks) {
        AttendanceRecord rec = repo.findByStudentAndDate(student, date).orElseGet(AttendanceRecord::new);
        rec.setStudent(student);
        rec.setSchoolClass(schoolClass);
        rec.setDate(date);
        rec.setStatus(status);
        rec.setMarkedBy(markedBy);
        rec.setRemarks(remarks);
        return repo.save(rec);
    }

    /** Attendance percentage (present + late counts as attended) for a student across all recorded days. */
    public double attendancePercentage(User student) {
        List<AttendanceRecord> records = forStudent(student);
        if (records.isEmpty()) return 100.0;
        long attended = records.stream().filter(r -> r.getStatus() == AttendanceRecord.Status.PRESENT
                || r.getStatus() == AttendanceRecord.Status.LATE).count();
        return Math.round((attended * 1000.0) / records.size()) / 10.0;
    }

    public Map<String, Long> summary(User student) {
        List<AttendanceRecord> records = forStudent(student);
        return Map.of(
                "PRESENT", records.stream().filter(r -> r.getStatus() == AttendanceRecord.Status.PRESENT).count(),
                "ABSENT", records.stream().filter(r -> r.getStatus() == AttendanceRecord.Status.ABSENT).count(),
                "LATE", records.stream().filter(r -> r.getStatus() == AttendanceRecord.Status.LATE).count(),
                "EXCUSED", records.stream().filter(r -> r.getStatus() == AttendanceRecord.Status.EXCUSED).count()
        );
    }

    public long countAll() { return repo.count(); }
}
