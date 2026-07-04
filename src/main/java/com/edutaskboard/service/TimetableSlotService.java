package com.edutaskboard.service;

import com.edutaskboard.dto.TimetableRow;
import com.edutaskboard.model.*;
import com.edutaskboard.repository.SubstituteAssignmentRepository;
import com.edutaskboard.repository.TimetableSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Fixed daily period structure (COMPLETE REDESIGN):
 * Period1(30) -> ShortBreak(15) -> Period2(30) -> Period3(30) -> LongBreak(30) -> Period4(30) -> Period5(30)
 * Only the admin's daily start time is configurable; every other time is derived automatically.
 */
@Service
public class TimetableSlotService {

    @Autowired private TimetableSlotRepository repo;
    @Autowired private SubstituteAssignmentRepository substituteRepo;
    @Autowired private SchoolSettingsService settingsService;

    public static final List<TimetableSlot.SchoolDay> SCHOOL_DAYS = List.of(
            TimetableSlot.SchoolDay.MONDAY, TimetableSlot.SchoolDay.TUESDAY, TimetableSlot.SchoolDay.WEDNESDAY,
            TimetableSlot.SchoolDay.THURSDAY, TimetableSlot.SchoolDay.FRIDAY);

    /** Computes the fixed daily sequence of periods/breaks (times only, no subject/teacher data). */
    public List<TimetableRow> computeDayStructure(LocalTime start) {
        List<TimetableRow> rows = new ArrayList<>();
        LocalTime cursor = start;
        cursor = addPeriod(rows, cursor, 1);
        cursor = addBreak(rows, cursor, TimetableRow.RowType.SHORT_BREAK, 15);
        cursor = addPeriod(rows, cursor, 2);
        cursor = addPeriod(rows, cursor, 3);
        cursor = addBreak(rows, cursor, TimetableRow.RowType.LONG_BREAK, 30);
        cursor = addPeriod(rows, cursor, 4);
        addPeriod(rows, cursor, 5);
        return rows;
    }

    private LocalTime addPeriod(List<TimetableRow> rows, LocalTime start, int periodNumber) {
        TimetableRow r = new TimetableRow();
        r.setRowType(TimetableRow.RowType.PERIOD);
        r.setPeriodNumber(periodNumber);
        r.setStartTime(start);
        LocalTime end = start.plusMinutes(30);
        r.setEndTime(end);
        rows.add(r);
        return end;
    }

    private LocalTime addBreak(List<TimetableRow> rows, LocalTime start, TimetableRow.RowType type, int minutes) {
        TimetableRow r = new TimetableRow();
        r.setRowType(type);
        r.setStartTime(start);
        LocalTime end = start.plusMinutes(minutes);
        r.setEndTime(end);
        rows.add(r);
        return end;
    }

    /** Full week schedule for a class: Day -> ordered rows (periods + breaks), Free Period where unassigned. */
    public Map<TimetableSlot.SchoolDay, List<TimetableRow>> getClassWeekSchedule(SchoolClass schoolClass) {
        LocalTime start = settingsService.get().getDailyStartTime();
        List<TimetableSlot> slots = repo.findBySchoolClassOrderByDayAscPeriodNumberAsc(schoolClass);
        Map<TimetableSlot.SchoolDay, List<TimetableRow>> result = new LinkedHashMap<>();
        for (TimetableSlot.SchoolDay day : SCHOOL_DAYS) {
            List<TimetableRow> rows = computeDayStructure(start);
            for (TimetableRow row : rows) {
                if (row.getRowType() != TimetableRow.RowType.PERIOD) continue;
                TimetableSlot slot = slots.stream()
                        .filter(s -> s.getDay() == day && s.getPeriodNumber() == row.getPeriodNumber())
                        .findFirst().orElse(null);
                applySlot(row, slot, day, schoolClass, null);
            }
            result.put(day, rows);
        }
        return result;
    }

    /** Full week schedule for a teacher: shows their class/subject each period, "Free Period" elsewhere. */
    public Map<TimetableSlot.SchoolDay, List<TimetableRow>> getTeacherWeekSchedule(User teacher) {
        LocalTime start = settingsService.get().getDailyStartTime();
        List<TimetableSlot> slots = repo.findByTeacherOrderByDayAscPeriodNumberAsc(teacher);
        Map<TimetableSlot.SchoolDay, List<TimetableRow>> result = new LinkedHashMap<>();
        for (TimetableSlot.SchoolDay day : SCHOOL_DAYS) {
            List<TimetableRow> rows = computeDayStructure(start);
            for (TimetableRow row : rows) {
                if (row.getRowType() != TimetableRow.RowType.PERIOD) continue;
                TimetableSlot slot = slots.stream()
                        .filter(s -> s.getDay() == day && s.getPeriodNumber() == row.getPeriodNumber())
                        .findFirst().orElse(null);
                if (slot == null) {
                    row.setFreePeriod(true);
                    row.setSubjectName("Free Period");
                } else {
                    row.setFreePeriod(false);
                    row.setSubjectName(slot.getSubject() != null ? slot.getSubject().getName() : "Free Period");
                    row.setTeacherName(slot.getSchoolClass().getName()); // reused field to show class name for teacher view
                    row.setSlotId(slot.getId());
                }
            }
            result.put(day, rows);
        }
        return result;
    }

    /** Today's schedule for a class, with substitute-teacher overlays applied. */
    public List<TimetableRow> getClassTodaySchedule(SchoolClass schoolClass) {
        LocalDate today = LocalDate.now();
        TimetableSlot.SchoolDay day = toSchoolDay(today);
        LocalTime start = settingsService.get().getDailyStartTime();
        List<TimetableRow> rows = computeDayStructure(start);
        if (day == null) return rows; // weekend: structure only
        List<TimetableSlot> slots = repo.findBySchoolClassOrderByDayAscPeriodNumberAsc(schoolClass);
        for (TimetableRow row : rows) {
            if (row.getRowType() != TimetableRow.RowType.PERIOD) continue;
            TimetableSlot slot = slots.stream()
                    .filter(s -> s.getDay() == day && s.getPeriodNumber() == row.getPeriodNumber())
                    .findFirst().orElse(null);
            applySlot(row, slot, day, schoolClass, today);
        }
        return rows;
    }

    private void applySlot(TimetableRow row, TimetableSlot slot, TimetableSlot.SchoolDay day, SchoolClass schoolClass, LocalDate dateForSubstitution) {
        if (slot == null || slot.getSubject() == null || slot.getTeacher() == null) {
            row.setFreePeriod(true);
            row.setSubjectName("Free Period");
            return;
        }
        row.setFreePeriod(false);
        row.setSubjectName(slot.getSubject().getName());
        row.setTeacherName(slot.getTeacher().getFullName());
        row.setSlotId(slot.getId());

        if (dateForSubstitution != null) {
            List<SubstituteAssignment> subs = substituteRepo
                    .findBySchoolClassAndDayAndPeriodNumberAndCancelledFalse(schoolClass, day, row.getPeriodNumber());
            subs.stream().filter(s -> s.coversDate(dateForSubstitution)).findFirst().ifPresent(s -> {
                row.setSubstituted(true);
                row.setSubstituteTeacherName(s.getSubstituteTeacher().getFullName());
            });
        }
    }

    public static TimetableSlot.SchoolDay toSchoolDay(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> TimetableSlot.SchoolDay.MONDAY;
            case TUESDAY -> TimetableSlot.SchoolDay.TUESDAY;
            case WEDNESDAY -> TimetableSlot.SchoolDay.WEDNESDAY;
            case THURSDAY -> TimetableSlot.SchoolDay.THURSDAY;
            case FRIDAY -> TimetableSlot.SchoolDay.FRIDAY;
            default -> null;
        };
    }

    /* ── Admin CRUD & validation ── */
    public List<TimetableSlot> getAll() { return repo.findAllByOrderByDayAscPeriodNumberAsc(); }
    public Optional<TimetableSlot> byId(Long id) { return repo.findById(id); }

    /**
     * Validates that the period number is within 1-5 and that the teacher isn't already
     * teaching a different class at the same day/period.
     */
    public String validate(TimetableSlot slot) {
        if (slot.getPeriodNumber() < 1 || slot.getPeriodNumber() > 5) {
            return "Period number must be between 1 and 5.";
        }
        if (slot.getTeacher() != null) {
            List<TimetableSlot> teacherDay = repo.findByTeacherAndDay(slot.getTeacher(), slot.getDay());
            for (TimetableSlot other : teacherDay) {
                if (slot.getId() != null && slot.getId().equals(other.getId())) continue;
                if (other.getPeriodNumber() == slot.getPeriodNumber()
                        && !other.getSchoolClass().getId().equals(slot.getSchoolClass().getId())) {
                    return "This teacher is already teaching " + other.getSchoolClass().getName()
                            + " at period " + slot.getPeriodNumber() + " on " + slot.getDay() + ".";
                }
            }
        }
        return null;
    }

    /** Creates or updates the (class, day, period) slot — one subject per class per period is enforced by upsert. */
    public TimetableSlot save(TimetableSlot slot) {
        Optional<TimetableSlot> existing = repo.findBySchoolClassAndDayAndPeriodNumber(
                slot.getSchoolClass(), slot.getDay(), slot.getPeriodNumber());
        TimetableSlot target = existing.orElse(slot);
        if (existing.isPresent()) {
            target.setSubject(slot.getSubject());
            target.setTeacher(slot.getTeacher());
        }
        return repo.save(target);
    }

    public void delete(Long id) { repo.deleteById(id); }

    /** Moves (or swaps) a slot to a new day/period — used by drag-and-drop editing. */
    public String move(Long slotId, TimetableSlot.SchoolDay newDay, int newPeriod) {
        TimetableSlot slot = repo.findById(slotId).orElseThrow(() -> new IllegalArgumentException("Slot not found"));
        TimetableSlot probe = new TimetableSlot();
        probe.setId(slot.getId());
        probe.setSchoolClass(slot.getSchoolClass());
        probe.setTeacher(slot.getTeacher());
        probe.setDay(newDay);
        probe.setPeriodNumber(newPeriod);
        String error = validate(probe);
        if (error != null) return error;

        Optional<TimetableSlot> targetSlot = repo.findBySchoolClassAndDayAndPeriodNumber(slot.getSchoolClass(), newDay, newPeriod);
        if (targetSlot.isPresent() && !targetSlot.get().getId().equals(slot.getId())) {
            TimetableSlot t = targetSlot.get();
            Subject tmpSubject = t.getSubject(); User tmpTeacher = t.getTeacher();
            t.setSubject(slot.getSubject()); t.setTeacher(slot.getTeacher());
            slot.setSubject(tmpSubject); slot.setTeacher(tmpTeacher);
            repo.save(t);
            repo.save(slot);
        } else {
            slot.setDay(newDay);
            slot.setPeriodNumber(newPeriod);
            repo.save(slot);
        }
        return null;
    }

    /** Available (free) teachers for a given day/period, excluding one teacher and anyone on approved leave. */
    public List<User> getAvailableSubstitutes(List<User> allTeachers, TimetableSlot.SchoolDay day, int periodNumber,
                                               User excludeTeacher, LeaveService leaveService, LocalDate date) {
        Set<Long> busyTeacherIds = new HashSet<>();
        for (TimetableSlot s : repo.findByDayAndPeriodNumber(day, periodNumber)) {
            if (s.getTeacher() != null) busyTeacherIds.add(s.getTeacher().getId());
        }
        List<User> available = new ArrayList<>();
        for (User t : allTeachers) {
            if (excludeTeacher != null && t.getId().equals(excludeTeacher.getId())) continue;
            if (busyTeacherIds.contains(t.getId())) continue;
            if (leaveService.isOnLeave(t, date)) continue;
            available.add(t);
        }
        return available;
    }
}
