package com.edutaskboard.service;

import com.edutaskboard.model.Assessment;
import com.edutaskboard.model.Question;
import com.edutaskboard.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {
    @Autowired private QuestionRepository repo;

    public List<Question> byAssessment(Assessment a) { return repo.findByAssessmentOrderByOrderIndexAsc(a); }
    public Optional<Question> byId(Long id) { return repo.findById(id); }

    public Question save(Question q) {
        if (q.getId() == null) {
            long count = repo.countByAssessment(q.getAssessment());
            q.setOrderIndex((int) count + 1);
        }
        return repo.save(q);
    }

    public void delete(Long id) { repo.deleteById(id); }
}
