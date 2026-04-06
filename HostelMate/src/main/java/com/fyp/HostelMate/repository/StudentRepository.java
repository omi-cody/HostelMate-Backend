package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Student;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<Student> findByUser_UserId(UUID userId);
    Optional<Student> findByUser_Email(String email);
    List<Student> findByUser_VerificationStatus(VerificationStatus status);
}
