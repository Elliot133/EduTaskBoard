package com.edutaskboard.service;

import com.edutaskboard.model.User;
import com.edutaskboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired private UserRepository repo;
    @Autowired private PasswordEncoder enc;

    public List<User> getAll() { return repo.findAll(); }
    public List<User> byRole(User.Role role) { return repo.findByRole(role); }
    public List<User> pending() { return repo.findByApprovalStatus(User.ApprovalStatus.PENDING); }
    public Optional<User> byId(Long id) { return repo.findById(id); }
    public Optional<User> byUsername(String u) { return repo.findByUsername(u); }
    public boolean usernameExists(String u) { return repo.existsByUsername(u); }
    public boolean emailExists(String e) { return repo.existsByEmail(e); }
    public long countByRole(User.Role role) { return repo.countByRole(role); }
    public long countPending() { return repo.countPending(); }

    /** Admin creates user — already approved, password hashed */
    public User create(User user) {
        user.setPassword(enc.encode(user.getPassword()));
        user.setEnabled(true);
        user.setApprovalStatus(User.ApprovalStatus.APPROVED);
        return repo.save(user);
    }

    /** Public self-registration — starts PENDING + disabled */
    public User register(User user) {
        user.setPassword(enc.encode(user.getPassword()));
        user.setEnabled(false);
        user.setApprovalStatus(User.ApprovalStatus.PENDING);
        return repo.save(user);
    }

    public void approve(Long id) {
        repo.findById(id).ifPresent(u -> {
            u.setApprovalStatus(User.ApprovalStatus.APPROVED);
            u.setEnabled(true);
            repo.save(u);
        });
    }

    public void reject(Long id) {
        repo.findById(id).ifPresent(u -> {
            u.setApprovalStatus(User.ApprovalStatus.REJECTED);
            u.setEnabled(false);
            repo.save(u);
        });
    }

    public void delete(Long id) { repo.deleteById(id); }
    public User save(User u) { return repo.save(u); }
}
