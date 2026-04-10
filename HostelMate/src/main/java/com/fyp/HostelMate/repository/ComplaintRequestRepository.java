package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.ComplaintRequest;
import com.fyp.HostelMate.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintRequestRepository extends JpaRepository<ComplaintRequest, UUID> {

    // Student views their own requests to track status
    List<ComplaintRequest> findByStudent_StudentIdOrderByCreatedAtDesc(UUID studentId);

    // Hostel sees all requests sent to them
    List<ComplaintRequest> findByHostel_HostelIdOrderByCreatedAtDesc(UUID hostelId);

    // Hostel dashboard shows pending count for quick overview
    long countByHostel_HostelIdAndStatus(UUID hostelId, RequestStatus status);
}
