package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, UUID> {
    List<MaintenanceRequest> findByHostelHostelId(UUID hostelId);
    List<MaintenanceRequest> findByStudentStudentId(UUID studentId);
}
