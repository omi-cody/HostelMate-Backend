package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    List<Application> findByStudentStudentId(UUID studentId);
    List<Application> findByHostelHostelId(UUID hostelId);
}
