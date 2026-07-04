package com.edutaskboard.service;

import com.edutaskboard.model.Task;
import com.edutaskboard.model.TaskSubmission;
import com.edutaskboard.model.User;
import com.edutaskboard.repository.TaskSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SubmissionService {
    @Autowired private TaskSubmissionRepository repo;

    public List<TaskSubmission> byStudent(User s) { return repo.findByStudent(s); }
    public List<TaskSubmission> byTask(Task t) { return repo.findByTask(t); }
    public Optional<TaskSubmission> byId(Long id) { return repo.findById(id); }
    public Optional<TaskSubmission> byTaskAndStudent(Task t, User s) { return repo.findByTaskAndStudent(t, s); }
    public boolean hasSubmitted(Task t, User s) { return repo.existsByTaskAndStudent(t, s); }
    public TaskSubmission save(TaskSubmission s) { return repo.save(s); }
    public long countAll() { return repo.count(); }
    public long countGraded(User s) { return repo.countByStudentAndStatus(s, TaskSubmission.SubmissionStatus.GRADED); }
    public Double avgScore(User s) { return repo.avgScoreByStudent(s); }
}
