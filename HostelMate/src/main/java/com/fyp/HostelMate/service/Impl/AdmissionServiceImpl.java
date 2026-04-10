package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.*;
import com.fyp.HostelMate.dto.response.StudentDashboardResponse;
import com.fyp.HostelMate.entity.*;
import com.fyp.HostelMate.entity.enums.*;
import com.fyp.HostelMate.exceptions.BusinessException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.*;
import com.fyp.HostelMate.service.EmailService;
import com.fyp.HostelMate.util.NotificationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdmissionServiceImpl {

    private final AdmissionRepository admissionRepo;
    private final StudentRepository studentRepo;
    private final HostelRepository hostelRepo;
    private final PaymentRepository paymentRepo;
    private final ComplaintRequestRepository complaintRepo;
    private final NotificationRepository notificationRepo;
    private final HostelReviewRepository hostelReviewRepo;
    private final ApplicationRepository applicationRepo;
    private final StudentReviewRepository studentReviewRepo;
    private final EmailService emailService;
    private final NotificationUtil notificationUtil;

    // ── GET STUDENT'S CURRENT ADMISSION (my hostel page) ─────────────────
    public Admission getMyActiveAdmission(String studentEmail) {
        Student student = getStudentByEmail(studentEmail);
        // Priority: ACTIVE > PENDING_PAYMENT > LEAVE_REQUESTED > LEFT (recent, for review)
        for (AdmissionStatus status : List.of(
                AdmissionStatus.ACTIVE,
                AdmissionStatus.PENDING_PAYMENT,
                AdmissionStatus.LEAVE_REQUESTED)) {
            var found = admissionRepo.findByStudent_StudentIdAndStatus(student.getStudentId(), status);
            if (found.isPresent()) return found.get();
        }
        // Check for recent LEFT admission (within 30 days) so student can review
        var leftAdmissions = admissionRepo.findByStudent_StudentIdOrderByAdmittedDateDesc(student.getStudentId());
        var recentLeft = leftAdmissions.stream()
                .filter(a -> a.getStatus() == AdmissionStatus.LEFT)
                .filter(a -> a.getLeftAt() != null &&
                        a.getLeftAt().isAfter(Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS)))
                .findFirst();
        if (recentLeft.isPresent()) return recentLeft.get();
        throw new ResourceNotFoundException("You are not currently admitted to any hostel.");
    }

    // ── STUDENT: REQUEST LEAVE ────────────────────────────────────────────
    @Transactional
    public void requestLeave(String studentEmail) {

        Student student = getStudentByEmail(studentEmail);
        Admission admission = admissionRepo.findByStudent_StudentIdAndStatus(
                        student.getStudentId(), AdmissionStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "You must be actively admitted to request a leave."));

        admission.setStatus(AdmissionStatus.LEAVE_REQUESTED);
        admission.setLeaveRequestedAt(Instant.now());
        admissionRepo.save(admission);

        // Notify the hostel that this student wants to leave
        notificationUtil.notifyHostel(admission.getHostel(), NotificationType.LEAVE_REQUESTED,
                student.getUser().getFullName() + " has requested to leave the hostel.",
                admission.getAdmissionId().toString());

        log.info("Leave requested by student {}", studentEmail);
    }

    // ── HOSTEL: RESPOND TO LEAVE REQUEST ─────────────────────────────────
    @Transactional
    public void respondToLeave(String hostelEmail, UUID admissionId, LeaveRemarkRequest req) {

        Hostel hostel = getHostelByEmail(hostelEmail);
        Admission admission = admissionRepo.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found"));

        if (!admission.getHostel().getHostelId().equals(hostel.getHostelId()))
            throw new BusinessException("This admission does not belong to your hostel.");

        if (admission.getStatus() != AdmissionStatus.LEAVE_REQUESTED)
            throw new BusinessException("No pending leave request for this admission.");

        Student student = admission.getStudent();

        if (Boolean.TRUE.equals(req.getAccept())) {
            // Accept the leave - student officially moves out
            admission.setStatus(AdmissionStatus.LEFT);
            admission.setLeftAt(Instant.now());
            admission.setLeaveRemark(req.getRemark());
            admissionRepo.save(admission);

            // Soft-delete the student's in-app notifications from this hostel
            notificationRepo.softDeleteAllForStudent(student.getStudentId());

            // Auto-cancel the linked application so student can reapply later
            if (admission.getApplication() != null) {
                Application app = admission.getApplication();
                app.setStatus(ApplicationStatus.CANCELLED);
                app.setRemark("Auto-cancelled after hostel approved leave request.");
                app.setUpdatedAt(Instant.now());
                applicationRepo.save(app);
            }

            emailService.sendLeaveStatusEmail(
                    student.getUser().getEmail(), student.getUser().getFullName(),
                    hostel.getHostelName(), true, req.getRemark());
            notificationUtil.notifyStudent(student, NotificationType.LEAVE_ACCEPTED,
                    "Your leave from " + hostel.getHostelName() + " has been accepted. " +
                    "Please leave a review for the hostel.");

        } else {
            // Reject the leave - student stays, must resolve the issue first
            admission.setStatus(AdmissionStatus.ACTIVE);
            admission.setLeaveRemark(req.getRemark());
            admissionRepo.save(admission);

            emailService.sendLeaveStatusEmail(
                    student.getUser().getEmail(), student.getUser().getFullName(),
                    hostel.getHostelName(), false, req.getRemark());
            notificationUtil.notifyStudent(student, NotificationType.LEAVE_REJECTED,
                    "Your leave request was rejected by " + hostel.getHostelName() +
                    ". Reason: " + req.getRemark());
        }

        log.info("Leave {} for admission {} by hostel {}",
                req.getAccept() ? "accepted" : "rejected", admissionId, hostelEmail);
    }

    // ── STUDENT: SUBMIT REVIEW AFTER LEAVING ─────────────────────────────
    @Transactional
    public void submitHostelReview(String studentEmail, UUID admissionId, ReviewRequest req) {

        Student student = getStudentByEmail(studentEmail);
        Admission admission = admissionRepo.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found"));

        if (!admission.getStudent().getStudentId().equals(student.getStudentId()))
            throw new BusinessException("This admission record doesn't belong to you.");

        if (admission.getStatus() != AdmissionStatus.LEFT)
            throw new BusinessException("You can only review a hostel after your leave is accepted.");

        if (hostelReviewRepo.findByAdmission_AdmissionId(admissionId).isPresent())
            throw new BusinessException("You have already submitted a review for this stay.");

        HostelReview review = new HostelReview();
        review.setStudent(student);
        review.setHostel(admission.getHostel());
        review.setAdmission(admission);
        review.setRating(req.getRating());
        review.setReviewText(req.getReviewText());
        review.setCreatedAt(Instant.now());
        hostelReviewRepo.save(review);

        notificationUtil.notifyHostel(admission.getHostel(), NotificationType.REVIEW_RECEIVED,
                student.getUser().getFullName() + " left a " + req.getRating() +
                "-star review for your hostel.");

        log.info("Hostel review submitted by {}", studentEmail);
    }

    // ── HOSTEL: SUBMIT REVIEW OF STUDENT AFTER LEAVE ─────────────────────
    @Transactional
    public void submitStudentReview(String hostelEmail, UUID admissionId, ReviewRequest req) {

        Hostel hostel = getHostelByEmail(hostelEmail);
        Admission admission = admissionRepo.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found"));

        if (!admission.getHostel().getHostelId().equals(hostel.getHostelId()))
            throw new BusinessException("This admission does not belong to your hostel.");

        if (admission.getStatus() != AdmissionStatus.LEFT)
            throw new BusinessException("You can only rate a student after their leave is accepted.");

        if (studentReviewRepo.findByAdmission_AdmissionId(admissionId).isPresent())
            throw new BusinessException("You have already submitted a review for this student.");

        StudentReview review = new StudentReview();
        review.setHostel(hostel);
        review.setStudent(admission.getStudent());
        review.setAdmission(admission);
        review.setRating(req.getRating());
        review.setReviewText(req.getReviewText());
        review.setCreatedAt(Instant.now());
        studentReviewRepo.save(review);

        Student student = admission.getStudent();
        emailService.sendReviewReceivedEmail(
                student.getUser().getEmail(), student.getUser().getFullName(), req.getRating());
        notificationUtil.notifyStudent(student, NotificationType.REVIEW_RECEIVED,
                hostel.getHostelName() + " rated your stay: " + req.getRating() + "/5 stars.");

        log.info("Student review submitted by hostel {} for admission {}", hostelEmail, admissionId);
    }

    // ── STUDENT DASHBOARD ─────────────────────────────────────────────────
    public StudentDashboardResponse getStudentDashboard(String studentEmail) {

        Student student = getStudentByEmail(studentEmail);

        Admission admission = admissionRepo.findByStudent_StudentIdAndStatus(
                        student.getStudentId(), AdmissionStatus.ACTIVE)
                .orElse(null);

        if (admission == null) {
            // Return empty dashboard if student is not currently admitted
            return StudentDashboardResponse.builder()
                    .hostelName("Not admitted")
                    .build();
        }

        // Roommates = other active students in the same room (excluding self)
        List<String> roommateNames = admissionRepo.findActiveAdmissionsByRoom(
                        admission.getRoom().getRoomId())
                .stream()
                .filter(a -> !a.getStudent().getStudentId()
                        .equals(student.getStudentId()))
                .map(a -> a.getStudent().getUser().getFullName())
                .collect(Collectors.toList());

        // Financial summary
        BigDecimal totalPaid = paymentRepo.getTotalPaidByStudentAtHostel(
                student.getStudentId(), admission.getHostel().getHostelId());
        BigDecimal pending = paymentRepo.getPendingAmountForStudent(
                student.getStudentId(), admission.getHostel().getHostelId());

        // Months of stay calculated from admission date
        int monthsOfStay = Period.between(admission.getAdmittedDate(), LocalDate.now()).getMonths() +
                Period.between(admission.getAdmittedDate(), LocalDate.now()).getYears() * 12;

        // Next fee due date = same day next month as admission date
        LocalDate nextFeeDate = admission.getAdmittedDate()
                .withMonth(LocalDate.now().getMonthValue())
                .withYear(LocalDate.now().getYear());
        if (!nextFeeDate.isAfter(LocalDate.now()))
            nextFeeDate = nextFeeDate.plusMonths(1);

        // Recent complaints for quick overview
        List<StudentDashboardResponse.RecentComplaintItem> complaints =
                complaintRepo.findByStudent_StudentIdOrderByCreatedAtDesc(student.getStudentId())
                        .stream().limit(5)
                        .map(c -> StudentDashboardResponse.RecentComplaintItem.builder()
                                .title(c.getTitle())
                                .requestType(c.getRequestType().name())
                                .status(c.getStatus().name())
                                .createdAt(c.getCreatedAt().toString())
                                .build())
                        .collect(Collectors.toList());

        return StudentDashboardResponse.builder()
                .hostelName(admission.getHostel().getHostelName())
                .roomNumber(admission.getRoom().getRoomNumber())
                .floor(admission.getRoom().getFloor().toString())
                .roommateNames(roommateNames)
                .totalPaidToDate(totalPaid)
                .pendingAmount(pending)
                .monthlyFeeAmount(admission.getMonthlyFeeAmount())
                .nextFeeDueDate(nextFeeDate)
                .monthsOfStay(monthsOfStay)
                .recentComplaints(complaints)
                .build();
    }

    // ── HOSTEL DASHBOARD ──────────────────────────────────────────────────
    public Map<String, Object> getHostelDashboard(String hostelEmail) {
        Hostel hostel = getHostelByEmail(hostelEmail);
        UUID hostelId = hostel.getHostelId();

        long totalStudents = admissionRepo.countByHostel_HostelIdAndStatus(
                hostelId, AdmissionStatus.ACTIVE);
        long pendingComplaints = complaintRepo.countByHostel_HostelIdAndStatus(
                hostelId, RequestStatus.PENDING);
        BigDecimal totalRevenue = paymentRepo.getTotalRevenueForHostel(hostelId);

        return Map.of(
                "totalStudents", totalStudents,
                "totalRevenue", totalRevenue,
                "pendingComplaints", pendingComplaints
        );
    }

    // ── HELPERS ───────────────────────────────────────────────────────────

    private Student getStudentByEmail(String email) {
        return studentRepo.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private Hostel getHostelByEmail(String email) {
        return hostelRepo.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found"));
    }
}
