package com.edutaskboard.service;

import com.edutaskboard.model.Subject;
import com.edutaskboard.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {
    @Autowired private SubjectRepository repo;

    public List<Subject> getAll() { return repo.findAll(); }
    public Optional<Subject> byId(Long id) { return repo.findById(id); }
    public boolean exists(String name) { return repo.existsByName(name); }
    public Subject save(Subject s) { return repo.save(s); }
    public void delete(Long id) { repo.deleteById(id); }
    public long countAll() { return repo.count(); }
}
