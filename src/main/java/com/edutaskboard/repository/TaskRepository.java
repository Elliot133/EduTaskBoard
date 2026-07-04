package com.edutaskboard.repository;

import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.Task;
import com.edutaskboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByTeacherOrderByCreatedAtDesc(User teacher);
    List<Task> findAllByOrderByCreatedAtDesc();

    @Query("SELECT t FROM Task t WHERE t.schoolClass = :cls ORDER BY t.createdAt DESC")
    List<Task> findBySchoolClassOrdered(@Param("cls") SchoolClass cls);

    long countByTeacher(User teacher);
}
