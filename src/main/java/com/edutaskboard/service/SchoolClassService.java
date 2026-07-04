package com.edutaskboard.service;

import com.edutaskboard.model.SchoolClass;
import com.edutaskboard.model.User;
import com.edutaskboard.repository.SchoolClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SchoolClassService {
    @Autowired private SchoolClassRepository repo;

    public List<SchoolClass> getAll() { return repo.findAllWithTeacher(); }
    public Optional<SchoolClass> byId(Long id) { return repo.findById(id); }
    public List<SchoolClass> byTeacher(User t) { return repo.findByClassTeacher(t); }
    public SchoolClass save(SchoolClass c) { return repo.save(c); }
    public void delete(Long id) { repo.deleteById(id); }
    public long countAll() { return repo.count(); }
}
