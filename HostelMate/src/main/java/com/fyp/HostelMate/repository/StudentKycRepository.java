package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.StudentKyc;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentKycRepository extends JpaRepository<StudentKyc, UUID> {

    Optional<StudentKyc> findByStudent_StudentId(UUID studentId);

    // Admin uses this to fetch all pending KYC submissions for the review queue
    List<StudentKyc> findByKycStatus(VerificationStatus status);
}
