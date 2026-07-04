package com.edutaskboard.repository;

import com.edutaskboard.model.Task;
import com.edutaskboard.model.TaskSubmission;
import com.edutaskboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {
    List<TaskSubmission> findByStudent(User student);
    List<TaskSubmission> findByTask(Task task);
    Optional<TaskSubmission> findByTaskAndStudent(Task task, User student);
    boolean existsByTaskAndStudent(Task task, User student);
    long countByStudentAndStatus(User student, TaskSubmission.SubmissionStatus status);

    @Query("SELECT AVG(ts.score) FROM TaskSubmission ts WHERE ts.student = :student AND ts.score IS NOT NULL")
    Double avgScoreByStudent(@Param("student") User student);
}
