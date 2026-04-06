package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Application;
import com.fyp.HostelMate.entity.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    List<Application> findByStudent_StudentId(UUID studentId);

    List<Application> findByHostel_HostelId(UUID hostelId);

    List<Application> findByHostel_HostelIdAndStatus(UUID hostelId, ApplicationStatus status);

    List<Application> findByStudent_StudentIdAndStatus(UUID studentId, ApplicationStatus status);

    boolean existsByStudent_StudentIdAndHostel_HostelIdAndStatusNot(
            UUID studentId, UUID hostelId, ApplicationStatus status);
}
