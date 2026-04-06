package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.LeaveRequest;
import com.fyp.HostelMate.entity.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    List<LeaveRequest> findByHostel_HostelIdOrderByCreatedAtDesc(UUID hostelId);

    List<LeaveRequest> findByStudent_StudentIdOrderByCreatedAtDesc(UUID studentId);

    List<LeaveRequest> findByHostel_HostelIdAndStatusOrderByCreatedAtDesc(
            UUID hostelId, LeaveStatus status);

    /** A student can only have one pending leave request at a time */
    boolean existsByStudent_StudentIdAndStatus(UUID studentId, LeaveStatus status);

    Optional<LeaveRequest> findByAdmission_AdmissionIdAndStatus(UUID admissionId, LeaveStatus status);
}
