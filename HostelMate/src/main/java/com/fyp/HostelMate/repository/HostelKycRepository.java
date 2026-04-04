package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.HostelKyc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HostelKycRepository extends JpaRepository<HostelKyc, String> {
}
