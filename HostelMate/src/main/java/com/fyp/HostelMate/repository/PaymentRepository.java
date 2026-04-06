package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Payment;
import com.fyp.HostelMate.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByStudent_StudentIdOrderByCreatedAtDesc(UUID studentId);

    List<Payment> findByHostel_HostelIdOrderByCreatedAtDesc(UUID hostelId);

    List<Payment> findByHostel_HostelIdAndPaymentStatus(UUID hostelId, PaymentStatus status);

    Optional<Payment> findByKhaltiTransactionId(String khaltiTransactionId);

    Optional<Payment> findByInvoiceNumber(String invoiceNumber);

    /** Total amount paid by a student across all admissions */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.student.studentId = :studentId AND p.paymentStatus = 'COMPLETED'")
    Double sumCompletedByStudentId(UUID studentId);

    /** Total collected by a hostel in completed payments */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.hostel.hostelId = :hostelId AND p.paymentStatus = 'COMPLETED'")
    Double sumCompletedByHostelId(UUID hostelId);

    /** Check if a student already has a payment record for a given month */
    boolean existsByAdmission_AdmissionIdAndPaymentForMonthAndPaymentStatusNot(
            UUID admissionId, java.time.LocalDate paymentForMonth, PaymentStatus status);
}
