package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.entity.enums.UserRole;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // Used by admin dashboard to count verified/pending students and hostels
    long countByRoleAndVerificationStatus(UserRole role, VerificationStatus verificationStatus);
}
