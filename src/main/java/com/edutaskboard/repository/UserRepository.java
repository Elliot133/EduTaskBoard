package com.edutaskboard.repository;

import com.edutaskboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(User.Role role);
    List<User> findByApprovalStatus(User.ApprovalStatus status);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(User.Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.approvalStatus = com.edutaskboard.model.User.ApprovalStatus.PENDING")
    long countPending();
}
