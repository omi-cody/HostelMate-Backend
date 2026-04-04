package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.StudentKyc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StudentKycRepository extends JpaRepository<StudentKyc, String> {
}
