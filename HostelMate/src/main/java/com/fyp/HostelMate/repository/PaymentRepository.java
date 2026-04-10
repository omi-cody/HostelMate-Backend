package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Payment;
import com.fyp.HostelMate.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByStudent_StudentIdOrderByCreatedAtDesc(UUID studentId);

    List<Payment> findByHostel_HostelIdOrderByCreatedAtDesc(UUID hostelId);

    Optional<Payment> findByAdmission_AdmissionIdAndFeeMonthAndStatus(
            UUID admissionId, LocalDate feeMonth, PaymentStatus status);

    // Find all pending payments for a specific admission (used for admission fee payment)
    List<Payment> findByAdmission_AdmissionIdAndStatus(UUID admissionId, PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.hostel.hostelId = :hostelId AND p.status = 'PAID'")
    BigDecimal getTotalRevenueForHostel(@Param("hostelId") UUID hostelId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.student.studentId = :studentId " +
           "AND p.hostel.hostelId = :hostelId AND p.status = 'PAID'")
    BigDecimal getTotalPaidByStudentAtHostel(@Param("studentId") UUID studentId,
                                             @Param("hostelId") UUID hostelId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.student.studentId = :studentId " +
           "AND p.hostel.hostelId = :hostelId AND p.status = 'PENDING'")
    BigDecimal getPendingAmountForStudent(@Param("studentId") UUID studentId,
                                          @Param("hostelId") UUID hostelId);
}
