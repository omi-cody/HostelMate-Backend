package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.HostelKyc;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HostelKycRepository extends JpaRepository<HostelKyc, UUID> {

    Optional<HostelKyc> findByHostel_HostelId(UUID hostelId);

    // Admin sees all hostels waiting for KYC review
    List<HostelKyc> findByKycStatus(VerificationStatus status);

    boolean existsByPanNumber(String panNumber);
}
