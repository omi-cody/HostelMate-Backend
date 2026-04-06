package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.MaintenanceRequest;
import com.fyp.HostelMate.entity.enums.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, UUID> {

    List<MaintenanceRequest> findByHostel_HostelIdOrderByCreatedAtDesc(UUID hostelId);

    List<MaintenanceRequest> findByStudent_StudentIdOrderByCreatedAtDesc(UUID studentId);

    List<MaintenanceRequest> findByHostel_HostelIdAndStatusOrderByCreatedAtDesc(
            UUID hostelId, MaintenanceStatus status);

    long countByHostel_HostelIdAndStatus(UUID hostelId, MaintenanceStatus status);
}
