package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByStudentStudentId(UUID studentId);
    List<Payment> findByHostelHostelId(UUID hostelId);
}
