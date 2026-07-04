package com.edutaskboard.repository;

import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.TimetableSlot;
import com.edutaskboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, Long> {
    List<TimetableSlot> findBySchoolClassOrderByDayAscPeriodNumberAsc(SchoolClass schoolClass);
    List<TimetableSlot> findByTeacherOrderByDayAscPeriodNumberAsc(User teacher);
    List<TimetableSlot> findAllByOrderByDayAscPeriodNumberAsc();
    Optional<TimetableSlot> findBySchoolClassAndDayAndPeriodNumber(SchoolClass schoolClass, TimetableSlot.SchoolDay day, int periodNumber);
    List<TimetableSlot> findByTeacherAndDay(User teacher, TimetableSlot.SchoolDay day);
    List<TimetableSlot> findByDayAndPeriodNumber(TimetableSlot.SchoolDay day, int periodNumber);
}
