package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.GenerateInvoiceRequest;
import com.fyp.HostelMate.dto.request.KhaltiInitiateRequest;
import com.fyp.HostelMate.dto.request.KhaltiPaymentRequest;
import com.fyp.HostelMate.dto.response.KhaltiResponse;
import com.fyp.HostelMate.entity.*;
import com.fyp.HostelMate.entity.enums.*;
import com.fyp.HostelMate.exceptions.BusinessException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.*;
import com.fyp.HostelMate.util.NotificationUtil;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl {

    private final PaymentRepository paymentRepo;
    private final AdmissionRepository admissionRepo;
    private final ApplicationRepository applicationRepo;
    private final StudentRepository studentRepo;
    private final HostelRepository hostelRepo;
    private  final  HostelKycRepository hostelKycRepo;
    private final NotificationUtil notificationUtil;
    private final WebClient.Builder webClientBuilder;
    private final ApplicationContext applicationContext;

    // These come from application.properties
    @Value("${khalti.secret-key}")
    private String khaltiSecretKey;

    @Value("${khalti.verify-url}")
    private String khaltiVerifyUrl;

    // Khalti's payment initiation endpoint (ePay v2)
    private static final String KHALTI_INITIATE_URL =
            "https://a.khalti.com/api/v2/epayment/initiate/";

    // The URL Khalti redirects to after payment - update to match your frontend
    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    // ── STUDENT: INITIATE KHALTI PAYMENT ─────────────────────────────────
    // Flow: frontend calls this → backend calls Khalti initiate API →
    //       returns Khalti payment_url → frontend opens that URL →
    //       student pays on Khalti → Khalti redirects to return_url →
    //       frontend extracts pidx from URL and calls /verify endpoint
    @Transactional
    public Map<String, Object> initiateKhaltiPayment(String studentEmail,
                                                      KhaltiPaymentRequest req) {
        Student student = getStudentByEmail(studentEmail);
        Admission admission = admissionRepo.findById(req.getAdmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found"));

        if (!admission.getStudent().getStudentId().equals(student.getStudentId()))
            throw new BusinessException("This admission does not belong to you");

        // Allow payment for both ACTIVE (monthly fee) and PENDING_PAYMENT (admission fee)
        if (admission.getStatus() != AdmissionStatus.ACTIVE &&
                admission.getStatus() != AdmissionStatus.PENDING_PAYMENT)
            throw new BusinessException("Admission is not active. Cannot make payment.");

        // Block duplicate payment for same month if already PAID
        paymentRepo.findByAdmission_AdmissionIdAndFeeMonthAndStatus(
                req.getAdmissionId(), req.getFeeMonth(), PaymentStatus.PAID)
                .ifPresent(p -> { throw new BusinessException("Fee for this month is already paid"); });

        // Reuse existing PENDING record if one exists (prevent duplicate PENDING payments)
        Optional<Payment> existingPending = paymentRepo
                .findByAdmission_AdmissionIdAndFeeMonthAndStatus(
                        req.getAdmissionId(), req.getFeeMonth(), PaymentStatus.PENDING);
        if (existingPending.isPresent()) {
            Payment existing = existingPending.get();
            // Reinitiate with Khalti using the same payment record
            // If amount is 0 (old bug), fix it first
            if (existing.getAmount() == null || existing.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal fixedAmount = hostelKycRepo
                    .findByHostel_HostelId(admission.getHostel().getHostelId())
                    .map(kyc -> kyc.getAdmissionFee() != null ? kyc.getAdmissionFee() : BigDecimal.ZERO)
                    .orElse(BigDecimal.ZERO);
                existing.setAmount(fixedAmount);
                paymentRepo.save(existing);
            }
            Long amountInPaisa = existing.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
            KhaltiInitiateRequest khaltiReq = KhaltiInitiateRequest.builder()
                    .return_url(frontendUrl + "/payment/callback")
                    .website_url(frontendUrl)
                    .amount(amountInPaisa)
                    .purchase_order_id(existing.getPaymentId().toString())
                    .purchase_order_name("HostelMate Fee - " + req.getFeeMonth().getMonth() + " " + req.getFeeMonth().getYear())
                    .build();
            try {
                KhaltiResponse khaltiResp = webClientBuilder.build()
                        .post().uri(KHALTI_INITIATE_URL)
                        .header("Authorization", "Key " + khaltiSecretKey)
                        .bodyValue(khaltiReq).retrieve().bodyToMono(KhaltiResponse.class).block();
                if (khaltiResp != null) {
                    return Map.of("paymentId", existing.getPaymentId(), "paymentUrl", khaltiResp.getPayment_url(), "pidx", khaltiResp.getPidx());
                }
            } catch (Exception e) { log.warn("Khalti reinitiate failed: {}", e.getMessage()); }
        }

        // Create a PENDING record first - we update it after Khalti confirms
        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setHostel(admission.getHostel());
        payment.setAdmission(admission);
        // For PENDING_PAYMENT admission: use the hostel's admission fee amount
        // For ACTIVE admission: use the monthly fee amount
        BigDecimal paymentAmount;
        if (admission.getStatus() == AdmissionStatus.PENDING_PAYMENT) {
            // Get admission fee from hostel KYC
            paymentAmount = hostelKycRepo.findByHostel_HostelId(admission.getHostel().getHostelId())
                .map(kyc -> kyc.getAdmissionFee() != null ? kyc.getAdmissionFee() : BigDecimal.ZERO)
                .orElse(BigDecimal.ZERO);
        } else {
            paymentAmount = admission.getMonthlyFeeAmount();
        }
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("Payment amount is zero. Please contact the hostel to set up fees.");
        }
        payment.setAmount(paymentAmount);
        payment.setFeeMonth(req.getFeeMonth());
        payment.setPaymentMethod(PaymentMethod.KHALTI);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(Instant.now());
        Payment saved = paymentRepo.save(payment);

        // Convert amount to paisa (Khalti requires paisa, not rupees)
        Long amountInPaisa = saved.getAmount()
                .multiply(BigDecimal.valueOf(100)).longValue();

        // Build the request to send to Khalti's API
        KhaltiInitiateRequest khaltiReq = KhaltiInitiateRequest.builder()
                .return_url(frontendUrl + "/payment/callback")
                .website_url(frontendUrl)
                .amount(amountInPaisa)
                .purchase_order_id(saved.getPaymentId().toString())
                .purchase_order_name("HostelMate Fee - " +
                        req.getFeeMonth().getMonth().toString() + " " +
                        req.getFeeMonth().getYear())
                .build();

        // Call Khalti's initiation endpoint to get the payment URL
        KhaltiResponse khaltiResp = webClientBuilder.build()
                .post()
                .uri(KHALTI_INITIATE_URL)
                .header("Authorization", "Key " + khaltiSecretKey)
                .bodyValue(khaltiReq)
                .retrieve()
                .bodyToMono(KhaltiResponse.class)
                .block();

        if (khaltiResp == null || khaltiResp.getPayment_url() == null)
            throw new BusinessException("Failed to connect to Khalti. Please try again.");

        // Save the pidx so we can verify it later
        saved.setKhaltiTransactionId(khaltiResp.getPidx());
        paymentRepo.save(saved);

        log.info("Khalti payment initiated: paymentId={} pidx={}", saved.getPaymentId(),
                khaltiResp.getPidx());

        // Return both the payment record and Khalti's payment URL to the frontend
        return Map.of(
                "paymentId", saved.getPaymentId().toString(),
                "paymentUrl", khaltiResp.getPayment_url(),
                "pidx", khaltiResp.getPidx(),
                "amount", saved.getAmount()
        );
    }

    // ── STUDENT: VERIFY KHALTI PAYMENT ───────────────────────────────────
    // Called by frontend after Khalti redirects back with ?pidx=xxx in the URL.
    // We call Khalti's lookup API to confirm the transaction actually completed.
    @Transactional
    public Payment verifyKhaltiPayment(String studentEmail, UUID paymentId, String pidx) {

        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found"));

        if (!payment.getStudent().getUser().getEmail().equals(studentEmail))
            throw new BusinessException("This payment does not belong to you");

        if (payment.getStatus() == PaymentStatus.PAID)
            throw new BusinessException("This payment is already confirmed");

        // Verify with Khalti that the payment actually went through
        boolean verified = callKhaltiVerifyApi(pidx, payment.getAmount());

        if (verified) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setKhaltiTransactionId(pidx);
            payment.setPaidAt(Instant.now());
            paymentRepo.save(payment);

            // ── Activate admission if this was the admission fee ──────────
            if (payment.getAdmission() != null &&
                    payment.getAdmission().getStatus() == AdmissionStatus.PENDING_PAYMENT) {
                Admission adm = payment.getAdmission();
                adm.setStatus(AdmissionStatus.ACTIVE);
                admissionRepo.save(adm);
                if (adm.getApplication() != null) {
                    adm.getApplication().setStatus(
                        ApplicationStatus.ADMITTED);
                    adm.getApplication().setUpdatedAt(Instant.now());
                    applicationRepo.save(adm.getApplication());
                }
                notificationUtil.notifyStudent(payment.getStudent(),
                        NotificationType.PAYMENT_CONFIRMED,
                        "Admission fee paid! You are now admitted to " +
                        payment.getHostel().getHostelName() +
                        ". The hostel will allocate your room shortly.");
                log.info("Admission {} activated after Khalti payment", adm.getAdmissionId());
            } else {
                notificationUtil.notifyStudent(payment.getStudent(),
                        NotificationType.PAYMENT_CONFIRMED,
                        "Payment of Rs. " + payment.getAmount() + " for " +
                        payment.getFeeMonth().getMonth() + " confirmed via Khalti.");
            }

            log.info("Khalti payment verified: paymentId={}", paymentId);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepo.save(payment);
            throw new BusinessException(
                    "Khalti payment verification failed. Contact support if money was deducted.");
        }

        return payment;
    }

    // ── HOSTEL: GENERATE CASH INVOICE ────────────────────────────────────
    @Transactional
    public Payment generateCashInvoice(String hostelEmail, GenerateInvoiceRequest req) {

        Hostel hostel = getHostelByEmail(hostelEmail);
        Admission admission = admissionRepo.findById(req.getAdmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found"));

        if (!admission.getHostel().getHostelId().equals(hostel.getHostelId()))
            throw new BusinessException("This admission does not belong to your hostel");

        paymentRepo.findByAdmission_AdmissionIdAndFeeMonthAndStatus(
                req.getAdmissionId(), req.getFeeMonth(), PaymentStatus.PAID)
                .ifPresent(p -> {
                    throw new BusinessException("Fee for this month is already recorded as paid");
                });

        BigDecimal discount = req.getDiscountAmount() != null ?
                req.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = admission.getMonthlyFeeAmount().subtract(discount);

        if (finalAmount.compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessException("Discount cannot exceed the monthly fee amount");

        // INV-{8 chars of hostelId}-{timestamp}
        String invoiceNumber = "INV-" +
                hostel.getHostelId().toString().substring(0, 8).toUpperCase() +
                "-" + System.currentTimeMillis();

        Payment payment = new Payment();
        payment.setStudent(admission.getStudent());
        payment.setHostel(hostel);
        payment.setAdmission(admission);
        payment.setAmount(finalAmount);
        payment.setDiscountAmount(discount.compareTo(BigDecimal.ZERO) > 0 ? discount : null);
        payment.setFeeMonth(req.getFeeMonth());
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setStatus(PaymentStatus.PAID);
        payment.setInvoiceNumber(invoiceNumber);
        payment.setNote(req.getNote());
        payment.setPaidAt(Instant.now());
        payment.setCreatedAt(Instant.now());

        Payment saved = paymentRepo.save(payment);

        notificationUtil.notifyStudent(admission.getStudent(),
                NotificationType.PAYMENT_CONFIRMED,
                "Cash payment of Rs. " + finalAmount + " recorded by hostel for " +
                req.getFeeMonth().getMonth() + ". Invoice: " + invoiceNumber);

        log.info("Cash invoice {} generated by hostel {}", invoiceNumber, hostelEmail);
        return saved;
    }

    // ── STUDENT: VIEW PAYMENT HISTORY ────────────────────────────────────
    public List<Payment> getMyPaymentHistory(String studentEmail) {
        Student student = getStudentByEmail(studentEmail);
        return paymentRepo.findByStudent_StudentIdOrderByCreatedAtDesc(student.getStudentId());
    }

    // ── HOSTEL: VIEW PAYMENT HISTORY ──────────────────────────────────────
    public List<Payment> getHostelPaymentHistory(String hostelEmail) {
        Hostel hostel = getHostelByEmail(hostelEmail);
        return paymentRepo.findByHostel_HostelIdOrderByCreatedAtDesc(hostel.getHostelId());
    }

    // ── EXPORT PAYMENT HISTORY AS PDF ─────────────────────────────────────
    public byte[] exportPaymentHistoryAsPdf(String userEmail, boolean isHostel) {

        List<Payment> payments = isHostel
                ? getHostelPaymentHistory(userEmail)
                : getMyPaymentHistory(userEmail);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc);

            doc.add(new Paragraph("HostelMate - Payment History Report")
                    .setBold().setFontSize(16));
            doc.add(new Paragraph("Generated: " + Instant.now())
                    .setFontSize(10));
            doc.add(new Paragraph(" "));

            // 6 columns
            Table table = new Table(new float[]{2, 2, 2, 2, 3, 3});
            table.setWidth(500);

            for (String h : new String[]{"Month", "Amount (Rs.)", "Method",
                                          "Status", "Invoice / Txn ID", "Paid At"}) {
                table.addCell(new Cell().add(new Paragraph(h).setBold()));
            }

            for (Payment p : payments) {
                table.addCell(p.getFeeMonth() != null ? p.getFeeMonth().toString() : "-");
                table.addCell(p.getAmount().toPlainString());
                table.addCell(p.getPaymentMethod().name());
                table.addCell(p.getStatus().name());
                String ref = p.getInvoiceNumber() != null ? p.getInvoiceNumber()
                        : (p.getKhaltiTransactionId() != null ? p.getKhaltiTransactionId() : "-");
                table.addCell(ref);
                table.addCell(p.getPaidAt() != null ? p.getPaidAt().toString() : "Pending");
            }

            doc.add(table);

            BigDecimal total = payments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.PAID)
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Total Paid: Rs. " + total.toPlainString()).setBold());
            doc.close();

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("PDF generation failed: {}", e.getMessage());
            throw new BusinessException("Failed to generate PDF report");
        }
    }

    // ── PRIVATE: CALL KHALTI LOOKUP API ──────────────────────────────────
    private boolean callKhaltiVerifyApi(String pidx, BigDecimal expectedAmount) {
        try {
            Map<?, ?> response = webClientBuilder.build()
                    .post()
                    .uri(khaltiVerifyUrl)
                    .header("Authorization", "Key " + khaltiSecretKey)
                    .bodyValue(Map.of("pidx", pidx))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return false;

            String status = (String) response.get("status");
            // Khalti sends amount in paisa - convert back to rupees to compare
            Number amountPaisa = (Number) response.get("total_amount");
            if (amountPaisa == null) return false;

            BigDecimal paidRs = BigDecimal.valueOf(amountPaisa.longValue())
                    .divide(BigDecimal.valueOf(100));

            boolean amountMatches = paidRs.compareTo(expectedAmount) == 0;
            boolean statusOk = "Completed".equalsIgnoreCase(status);

            if (!amountMatches)
                log.warn("Khalti amount mismatch: expected {} got {}", expectedAmount, paidRs);

            return statusOk && amountMatches;

        } catch (Exception e) {
            log.error("Khalti API error for pidx {}: {}", pidx, e.getMessage());
            return false;
        }
    }

    private Student getStudentByEmail(String email) {
        return studentRepo.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private Hostel getHostelByEmail(String email) {
        return hostelRepo.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found"));
    }

    // Admin: view all payments
    public List<Payment> getAllPayments() {
        return paymentRepo.findAll();
    }

    // Admin: manually update payment status (activate admission if needed)
    @Transactional
    public Payment adminUpdatePaymentStatus(UUID paymentId, PaymentStatus newStatus) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setStatus(newStatus);
        if (newStatus == PaymentStatus.PAID && payment.getPaidAt() == null) {
            payment.setPaidAt(Instant.now());
        }
        paymentRepo.save(payment);
        // Activate admission if this was pending admission fee
        if (newStatus == PaymentStatus.PAID &&
                payment.getAdmission() != null &&
                payment.getAdmission().getStatus() == AdmissionStatus.PENDING_PAYMENT) {
            Admission adm = payment.getAdmission();
            adm.setStatus(AdmissionStatus.ACTIVE);
            admissionRepo.save(adm);
            if (adm.getApplication() != null) {
                adm.getApplication().setStatus(ApplicationStatus.ADMITTED);
                adm.getApplication().setUpdatedAt(Instant.now());
                ApplicationRepository appRepo =
                    applicationContext.getBean(ApplicationRepository.class);
                appRepo.save(adm.getApplication());
            }
        }
        log.info("Admin updated payment {} to {}", paymentId, newStatus);
        return payment;
    }

}
