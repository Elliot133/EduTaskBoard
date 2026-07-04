package com.edutaskboard.repository;

import com.edutaskboard.model.Assessment;
import com.edutaskboard.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByAssessmentOrderByOrderIndexAsc(Assessment assessment);
    long countByAssessment(Assessment assessment);
    void deleteByAssessment(Assessment assessment);
}
