package com.edutaskboard.service;

import com.edutaskboard.model.*;
import com.edutaskboard.repository.SubstituteAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SubstituteService {
    @Autowired private SubstituteAssignmentRepository repo;
    @Autowired private TimetableSlotService timetableSlotService;
    @Autowired private LeaveService leaveService;
    @Autowired private UserService userService;

    public List<SubstituteAssignment> getAll() { return repo.findAllByOrderByCreatedAtDesc(); }
    public Optional<SubstituteAssignment> byId(Long id) { return repo.findById(id); }
    public List<SubstituteAssignment> forSubstituteTeacher(User teacher) { return repo.findBySubstituteTeacherAndCancelledFalse(teacher); }

    /** Only teachers currently free at that day/period, and not on approved leave, are selectable. */
    public List<User> availableSubstitutes(TimetableSlot.SchoolDay day, int periodNumber, User originalTeacher, LocalDate date) {
        List<User> allTeachers = userService.byRole(User.Role.TEACHER);
        return timetableSlotService.getAvailableSubstitutes(allTeachers, day, periodNumber, originalTeacher, leaveService, date);
    }

    /**
     * Assigns a substitute teacher for a class/day/period across a date range.
     * Throws IllegalStateException if the chosen substitute is not actually available.
     */
    public SubstituteAssignment assign(SchoolClass schoolClass, TimetableSlot.SchoolDay day, int periodNumber,
                                        User originalTeacher, User substituteTeacher,
                                        LocalDate startDate, LocalDate endDate, String reason) {
        List<User> available = availableSubstitutes(day, periodNumber, originalTeacher, startDate);
        boolean ok = available.stream().anyMatch(t -> t.getId().equals(substituteTeacher.getId()));
        if (!ok) {
            throw new IllegalStateException("The selected teacher is not available (already teaching or on leave) at this time.");
        }
        SubstituteAssignment sa = new SubstituteAssignment();
        sa.setSchoolClass(schoolClass);
        sa.setDay(day);
        sa.setPeriodNumber(periodNumber);
        sa.setOriginalTeacher(originalTeacher);
        sa.setSubstituteTeacher(substituteTeacher);
        sa.setStartDate(startDate);
        sa.setEndDate(endDate);
        sa.setReason(reason);
        return repo.save(sa);
    }

    public void cancel(Long id) {
        repo.findById(id).ifPresent(sa -> { sa.setCancelled(true); repo.save(sa); });
    }
}
