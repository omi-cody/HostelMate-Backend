package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.CashPaymentRequest;
import com.fyp.HostelMate.dto.request.KhaltiVerifyRequest;
import com.fyp.HostelMate.dto.request.PaymentInitiateRequest;
import com.fyp.HostelMate.dto.response.PaymentResponse;
import com.fyp.HostelMate.dto.response.PaymentSummaryResponse;
import com.fyp.HostelMate.entity.*;
import com.fyp.HostelMate.entity.enums.NotificationType;
import com.fyp.HostelMate.entity.enums.PaymentMethod;
import com.fyp.HostelMate.entity.enums.PaymentStatus;
import com.fyp.HostelMate.exceptions.BadRequestException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.*;
import com.fyp.HostelMate.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl {

    private final PaymentRepository paymentRepository;
    private final AdmissionRepository admissionRepository;
    private final StudentRepository studentRepository;
    private final HostelRepository hostelRepository;
    private final NotificationService notificationService;
    private final com.fyp.HostelMate.service.KhaltiService khaltiService;

    // ── STUDENT: INITIATE KHALTI PAYMENT ─────────────────────────────────────
    @Transactional
    public PaymentResponse initiateKhalti(User currentUser, PaymentInitiateRequest req) {
        Student student = studentRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found."));

        Admission admission = admissionRepository
                .findByStudent_StudentIdAndIsActiveTrue(student.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("No active admission found."));

        guardDuplicatePayment(admission.getAdmissionId(), req.getPaymentForMonth());

        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setHostel(admission.getHostel());
        payment.setAdmission(admission);
        payment.setAmount(admission.getMonthlyFee());
        payment.setPaymentMethod(PaymentMethod.KHALTI);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentForMonth(req.getPaymentForMonth());

        paymentRepository.save(payment);
        log.info("Khalti payment initiated: paymentId={}", payment.getPaymentId());

        // NOTE: In production, call Khalti API here to get the payment URL and token.
        // Return the saved record — frontend will use paymentId to verify after callback.
        return PaymentResponse.from(payment);
    }

    // ── STUDENT: VERIFY KHALTI CALLBACK ──────────────────────────────────────
    @Transactional
    public PaymentResponse verifyKhalti(User currentUser, KhaltiVerifyRequest req) {
        Payment payment = paymentRepository.findById(req.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found."));

        if (!payment.getStudent().getUser().getUserId().equals(currentUser.getUserId())) {
            throw new BadRequestException("This payment does not belong to you.");
        }
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Payment is already " + payment.getPaymentStatus());
        }

        // Verify token with Khalti server before marking COMPLETED
        boolean verified = khaltiService.verifyPayment(req.getToken(), payment.getAmount());
        if (!verified) {
            throw new BadRequestException(
                    "Khalti payment verification failed. Please contact support.");
        }
        payment.setTransactionToken(req.getToken());
        payment.setKhaltiTransactionId(req.getKhaltiTransactionId());
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(Instant.now());
        updateNextDueDate(payment.getAdmission());
        paymentRepository.save(payment);

        notificationService.send(
                payment.getHostel().getUser(),
                NotificationType.PAYMENT_RECEIVED,
                "Payment received",
                payment.getStudent().getFullName() + " paid Rs " + payment.getAmount() +
                        " for " + payment.getPaymentForMonth(),
                "payment:" + payment.getPaymentId()
        );
        notificationService.send(
                currentUser,
                NotificationType.PAYMENT_CONFIRMED,
                "Payment confirmed",
                "Your payment of Rs " + payment.getAmount() + " was successful.",
                "payment:" + payment.getPaymentId()
        );

        log.info("Khalti payment verified: paymentId={}", payment.getPaymentId());
        return PaymentResponse.from(payment);
    }

    // ── HOSTEL: RECORD CASH PAYMENT ───────────────────────────────────────────
    @Transactional
    public PaymentResponse recordCash(User currentUser, CashPaymentRequest req) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));

        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found."));

        Admission admission = admissionRepository
                .findByStudent_StudentIdAndIsActiveTrue(student.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student has no active admission."));

        if (!admission.getHostel().getHostelId().equals(hostel.getHostelId())) {
            throw new BadRequestException("Student is not admitted to your hostel.");
        }

        guardDuplicatePayment(admission.getAdmissionId(), req.getPaymentForMonth());

        double amount = req.getAmount() != null ? req.getAmount() : admission.getMonthlyFee();

        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setHostel(hostel);
        payment.setAdmission(admission);
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaymentForMonth(req.getPaymentForMonth());
        payment.setInvoiceNumber(generateInvoiceNumber());
        payment.setPaidAt(Instant.now());
        updateNextDueDate(admission);
        paymentRepository.save(payment);

        notificationService.send(
                student.getUser(),
                NotificationType.PAYMENT_CONFIRMED,
                "Payment recorded",
                "Cash payment of Rs " + amount + " recorded by " + hostel.getHostelName() +
                        ". Invoice: " + payment.getInvoiceNumber(),
                "payment:" + payment.getPaymentId()
        );

        log.info("Cash payment recorded: paymentId={}", payment.getPaymentId());
        return PaymentResponse.from(payment);
    }

    // ── STUDENT: PAYMENT HISTORY ──────────────────────────────────────────────
    public PaymentSummaryResponse getStudentHistory(User currentUser) {
        Student student = studentRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found."));

        List<PaymentResponse> payments = paymentRepository
                .findByStudent_StudentIdOrderByCreatedAtDesc(student.getStudentId())
                .stream().map(PaymentResponse::from).toList();

        Double totalPaid = paymentRepository.sumCompletedByStudentId(student.getStudentId());

        // Due = current monthly fee if next payment date has passed
        double dueAmount = 0.0;
        var admissionOpt = admissionRepository.findByStudent_StudentIdAndIsActiveTrue(student.getStudentId());
        if (admissionOpt.isPresent()) {
            Admission a = admissionOpt.get();
            if (a.getNextPaymentDue() != null && !LocalDate.now().isBefore(a.getNextPaymentDue())) {
                dueAmount = a.getMonthlyFee();
            }
        }

        return PaymentSummaryResponse.builder()
                .totalPaid(totalPaid != null ? totalPaid : 0.0)
                .dueAmount(dueAmount)
                .payments(payments)
                .build();
    }

    // ── HOSTEL: PAYMENT LIST ──────────────────────────────────────────────────
    public List<PaymentResponse> getHostelPayments(User currentUser) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));
        return paymentRepository.findByHostel_HostelIdOrderByCreatedAtDesc(hostel.getHostelId())
                .stream().map(PaymentResponse::from).toList();
    }

    public List<PaymentResponse> getPendingHostelPayments(User currentUser) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));
        return paymentRepository.findByHostel_HostelIdAndPaymentStatus(hostel.getHostelId(), PaymentStatus.PENDING)
                .stream().map(PaymentResponse::from).toList();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────
    private void guardDuplicatePayment(UUID admissionId, LocalDate month) {
        boolean exists = paymentRepository
                .existsByAdmission_AdmissionIdAndPaymentForMonthAndPaymentStatusNot(
                        admissionId, month, PaymentStatus.FAILED);
        if (exists) throw new BadRequestException("A payment for this month already exists.");
    }

    private void updateNextDueDate(Admission admission) {
        LocalDate current = admission.getNextPaymentDue() != null
                ? admission.getNextPaymentDue() : LocalDate.now();
        admission.setNextPaymentDue(current.plusMonths(1));
        admissionRepository.save(admission);
    }

    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }
}
