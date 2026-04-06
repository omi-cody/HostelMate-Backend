package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HostelRepository extends JpaRepository<Hostel, UUID> {
    Optional<Hostel> findByUser_UserId(UUID userId);
    Optional<Hostel> findByUser_Email(String email);
    List<Hostel> findByUser_VerificationStatus(VerificationStatus status);
}
