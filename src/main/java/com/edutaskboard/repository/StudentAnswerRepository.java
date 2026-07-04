package com.edutaskboard.repository;

import com.edutaskboard.model.AssessmentAttempt;
import com.edutaskboard.model.Question;
import com.edutaskboard.model.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    List<StudentAnswer> findByAttempt(AssessmentAttempt attempt);
    Optional<StudentAnswer> findByAttemptAndQuestion(AssessmentAttempt attempt, Question question);
}
