package com.edutaskboard.service;

import com.edutaskboard.model.LeaveRequest;
import com.edutaskboard.model.User;
import com.edutaskboard.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveService {
    @Autowired private LeaveRequestRepository repo;

    public List<LeaveRequest> byTeacher(User teacher) { return repo.findByTeacherOrderByCreatedAtDesc(teacher); }
    public List<LeaveRequest> getAll() { return repo.findAllByOrderByCreatedAtDesc(); }
    public Optional<LeaveRequest> byId(Long id) { return repo.findById(id); }

    public LeaveRequest request(User teacher, LocalDate start, LocalDate end, String reason) {
        LeaveRequest lr = new LeaveRequest();
        lr.setTeacher(teacher); lr.setStartDate(start); lr.setEndDate(end); lr.setReason(reason);
        lr.setStatus(LeaveRequest.Status.PENDING);
        return repo.save(lr);
    }

    public LeaveRequest review(Long id, boolean approve, User reviewer, String note) {
        LeaveRequest lr = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        lr.setStatus(approve ? LeaveRequest.Status.APPROVED : LeaveRequest.Status.REJECTED);
        lr.setReviewedBy(reviewer);
        lr.setReviewNote(note);
        return repo.save(lr);
    }

    public boolean isOnLeave(User teacher, LocalDate date) {
        return repo.findByTeacherOrderByCreatedAtDesc(teacher).stream().anyMatch(lr -> lr.coversDate(date));
    }
}
