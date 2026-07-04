package com.edutaskboard.repository;

import com.edutaskboard.model.Assessment;
import com.edutaskboard.model.AssessmentAttempt;
import com.edutaskboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Long> {
    Optional<AssessmentAttempt> findByAssessmentAndStudent(Assessment assessment, User student);
    List<AssessmentAttempt> findByAssessment(Assessment assessment);
    List<AssessmentAttempt> findByStudent(User student);
    boolean existsByAssessmentAndStudent(Assessment assessment, User student);
}
