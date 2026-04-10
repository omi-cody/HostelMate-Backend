package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Application;
import com.fyp.HostelMate.entity.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    // Student views all their applications (to track across multiple hostels)
    List<Application> findByStudent_StudentIdOrderByAppliedAtDesc(UUID studentId);

    // Hostel views all applications they received
    List<Application> findByHostel_HostelIdOrderByAppliedAtDesc(UUID hostelId);

    // Hostel filters applications by status (e.g. only pending ones)
    List<Application> findByHostel_HostelIdAndStatusOrderByAppliedAtDesc(UUID hostelId, ApplicationStatus status);

    // Check if a student already applied to this specific hostel with an active status.
    // Prevents spamming duplicate applications to the same hostel.
    boolean existsByStudent_StudentIdAndHostel_HostelIdAndStatusIn(
            UUID studentId, UUID hostelId, List<ApplicationStatus> statuses);
}
