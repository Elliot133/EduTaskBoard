package com.edutaskboard.service;

import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.Task;
import com.edutaskboard.model.User;
import com.edutaskboard.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    @Autowired private TaskRepository repo;

    public List<Task> getAll() { return repo.findAllByOrderByCreatedAtDesc(); }
    public Optional<Task> byId(Long id) { return repo.findById(id); }
    public List<Task> byTeacher(User t) { return repo.findByTeacherOrderByCreatedAtDesc(t); }

    public List<Task> forStudent(User s) {
        if (s.getSchoolClass() == null) return Collections.emptyList();
        return repo.findBySchoolClassOrdered(s.getSchoolClass());
    }

    public Task save(Task t) { return repo.save(t); }
    public void delete(Long id) { repo.deleteById(id); }
    public long countAll() { return repo.count(); }
    public long countByTeacher(User t) { return repo.countByTeacher(t); }
}
