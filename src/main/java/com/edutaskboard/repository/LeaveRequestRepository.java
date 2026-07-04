package com.edutaskboard.repository;

import com.edutaskboard.model.LeaveRequest;
import com.edutaskboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByTeacherOrderByCreatedAtDesc(User teacher);
    List<LeaveRequest> findAllByOrderByCreatedAtDesc();
    List<LeaveRequest> findByStatus(LeaveRequest.Status status);
}
