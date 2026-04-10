package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.KycVerifyRequest;
import com.fyp.HostelMate.entity.*;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import com.fyp.HostelMate.exceptions.BusinessException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.*;
import com.fyp.HostelMate.service.EmailService;
import com.fyp.HostelMate.util.NotificationUtil;
import com.fyp.HostelMate.entity.enums.NotificationType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl {

    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final HostelRepository hostelRepo;
    private final StudentKycRepository studentKycRepo;
    private final HostelKycRepository hostelKycRepo;
    private final AdmissionRepository admissionRepo;
    private final EmailService emailService;
    private final NotificationUtil notificationUtil;

    // STUDENT KYC VERIFICATION
    @Transactional
    public void verifyStudentKyc(UUID kycId, KycVerifyRequest req) {

        StudentKyc kyc = studentKycRepo.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("Student KYC not found"));

        VerificationStatus action = parseAction(req.getAction());

        if (action == VerificationStatus.REJECTED && isBlank(req.getRemark()))
            throw new BusinessException("Rejection remark is required");

        kyc.setKycStatus(action);
        if (action == VerificationStatus.REJECTED) {
            kyc.setRejectionRemark(req.getRemark());
        } else {
            kyc.setVerifiedAt(Instant.now());
            kyc.setRejectionRemark(null);
        }
        studentKycRepo.save(kyc);

        // Mirror the KYC status onto the user account so login checks work
        User user = kyc.getStudent().getUser();
        user.setVerificationStatus(action);
        userRepo.save(user);

        Student student = kyc.getStudent();
        String studentName = user.getFullName();
        String studentEmail = user.getEmail();

        // Notify student by email and in-app notification
        if (action == VerificationStatus.VERIFIED) {
            emailService.sendKycVerifiedEmail(studentEmail, studentName);
            notificationUtil.notifyStudent(student, NotificationType.KYC_VERIFIED,
                    "Your KYC has been verified! You can now search and apply to hostels.");
        } else {
            emailService.sendKycRejectedEmail(studentEmail, studentName, req.getRemark());
            notificationUtil.notifyStudent(student, NotificationType.KYC_REJECTED,
                    "Your KYC was rejected. Reason: " + req.getRemark() +
                    ". Please log in and resubmit.");
        }

        log.info("Student KYC {} for kycId: {}", action, kycId);
    }

    //  HOSTEL KYC VERIFICATION
    @Transactional
    public void verifyHostelKyc(UUID kycId, KycVerifyRequest req) {

        HostelKyc kyc = hostelKycRepo.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel KYC not found"));

        VerificationStatus action = parseAction(req.getAction());

        if (action == VerificationStatus.REJECTED && isBlank(req.getRemark()))
            throw new BusinessException("Rejection remark is required");

        kyc.setKycStatus(action);
        if (action == VerificationStatus.REJECTED) {
            kyc.setRejectionRemark(req.getRemark());
        } else {
            kyc.setVerifiedAt(Instant.now());
            kyc.setRejectionRemark(null);
        }
        hostelKycRepo.save(kyc);

        Hostel hostel = kyc.getHostel();
        hostel.setVerificationStatus(action);
        hostelRepo.save(hostel);

        User user = hostel.getUser();
        user.setVerificationStatus(action);
        userRepo.save(user);

        String hostelEmail = user.getEmail();
        String hostelName = hostel.getHostelName();

        // Let the hostel know via email and in-app notification
        if (action == VerificationStatus.VERIFIED) {
            emailService.sendKycVerifiedEmail(hostelEmail, hostelName);
            notificationUtil.notifyHostel(hostel, NotificationType.KYC_VERIFIED,
                    "Your KYC is verified! Add your rooms to start receiving applications.");
        } else {
            emailService.sendKycRejectedEmail(hostelEmail, hostelName, req.getRemark());
            notificationUtil.notifyHostel(hostel, NotificationType.KYC_REJECTED,
                    "Your KYC was rejected. Reason: " + req.getRemark());
        }

        log.info("Hostel KYC {} for kycId: {}", action, kycId);
    }

    // ADMIN DASHBOARD STATS
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalStudents = studentRepo.count();
        long totalHostels = hostelRepo.count();
        long verifiedStudents = userRepo.countByRoleAndVerificationStatus(
                com.fyp.HostelMate.entity.enums.UserRole.STUDENT, VerificationStatus.VERIFIED);
        long verifiedHostels = userRepo.countByRoleAndVerificationStatus(
                com.fyp.HostelMate.entity.enums.UserRole.HOSTEL, VerificationStatus.VERIFIED);
        long pendingStudentKyc = studentKycRepo.findByKycStatus(VerificationStatus.SUBMITTED).size();
        long pendingHostelKyc = hostelKycRepo.findByKycStatus(VerificationStatus.SUBMITTED).size();

        stats.put("totalStudents", totalStudents);
        stats.put("totalHostels", totalHostels);
        stats.put("verifiedStudents", verifiedStudents);
        stats.put("verifiedHostels", verifiedHostels);
        stats.put("pendingStudentKyc", pendingStudentKyc);
        stats.put("pendingHostelKyc", pendingHostelKyc);

        return stats;
    }

    // LISTS FOR ADMIN
    public List<StudentKyc> getAllPendingStudentKyc() {
        return studentKycRepo.findByKycStatus(VerificationStatus.SUBMITTED);
    }

    public List<HostelKyc> getAllPendingHostelKyc() {
        return hostelKycRepo.findByKycStatus(VerificationStatus.SUBMITTED);
    }

    public List<Student> getAllStudents() {
        return studentRepo.findAll();
    }

    public List<Hostel> getAllHostels() {
        return hostelRepo.findAll();
    }

    //HELPERS

    private VerificationStatus parseAction(String action) {
        if ("VERIFIED".equalsIgnoreCase(action)) return VerificationStatus.VERIFIED;
        if ("REJECTED".equalsIgnoreCase(action)) return VerificationStatus.REJECTED;
        throw new BusinessException("Action must be VERIFIED or REJECTED");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
