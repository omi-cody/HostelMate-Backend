package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.StudentKycRequest;
import com.fyp.HostelMate.dto.request.UpdateStudentProfileRequest;
import com.fyp.HostelMate.entity.Student;
import com.fyp.HostelMate.entity.StudentKyc;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import com.fyp.HostelMate.exceptions.BusinessException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.StudentKycRepository;
import com.fyp.HostelMate.repository.StudentRepository;
import com.fyp.HostelMate.repository.UserRepository;
import com.fyp.HostelMate.service.StudentKycService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentKycServiceImpl implements StudentKycService {

    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final StudentKycRepository kycRepo;

    // ── SUBMIT KYC (first time) ────────────────────────────────────────────
    @Override
    @Transactional
    public void submitKyc(String email, StudentKycRequest req) {

        User user = getUserByEmail(email);
        Student student = getStudentByUser(user);

        // Block resubmission through the submit endpoint if a KYC already exists
        if (kycRepo.findByStudent_StudentId(student.getStudentId()).isPresent())
            throw new BusinessException(
                    "KYC already submitted. Use the resubmit endpoint if it was rejected.");

        StudentKyc kyc = buildKycFromRequest(req, student);
        kyc.setKycStatus(VerificationStatus.SUBMITTED);
        kyc.setSubmittedAt(Instant.now());
        kycRepo.save(kyc);

        // Mark user as SUBMITTED so they know it's in admin's queue
        user.setVerificationStatus(VerificationStatus.SUBMITTED);
        userRepo.save(user);

        log.info("KYC submitted for student: {}", email);
    }

    // ── RESUBMIT KYC (after rejection) ────────────────────────────────────
    @Override
    @Transactional
    public void resubmitKyc(String email, StudentKycRequest req) {

        User user = getUserByEmail(email);
        Student student = getStudentByUser(user);

        StudentKyc kyc = kycRepo.findByStudent_StudentId(student.getStudentId())
                .orElseThrow(() -> new BusinessException(
                        "No existing KYC found. Please submit your KYC first."));

        // Only allow resubmission if it was previously rejected
        if (kyc.getKycStatus() != VerificationStatus.REJECTED)
            throw new BusinessException(
                    "Resubmission is only allowed after a rejection.");

        // Update all KYC fields with the corrected information
        updateKycFields(kyc, req);
        kyc.setKycStatus(VerificationStatus.SUBMITTED);
        kyc.setRejectionRemark(null);  // clear old rejection remark
        kyc.setSubmittedAt(Instant.now());
        kycRepo.save(kyc);

        user.setVerificationStatus(VerificationStatus.SUBMITTED);
        userRepo.save(user);

        log.info("KYC resubmitted for student: {}", email);
    }

    // ── GET MY KYC ────────────────────────────────────────────────────────
    @Override
    public StudentKyc getMyKyc(String email) {
        User user = getUserByEmail(email);
        Student student = getStudentByUser(user);
        return kycRepo.findByStudent_StudentId(student.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("KYC not submitted yet"));
    }

    // ── UPDATE PROFILE ────────────────────────────────────────────────────
    @Override
    @Transactional
    public void updateProfile(String email, UpdateStudentProfileRequest req) {

        User user = getUserByEmail(email);
        Student student = getStudentByUser(user);

        // Update user-level fields if provided
        if (req.getFullName() != null && !req.getFullName().isBlank())
            user.setFullName(req.getFullName());
        if (req.getPhone() != null && !req.getPhone().isBlank())
            user.setPhone(req.getPhone());
        userRepo.save(user);

        // Update KYC-level mutable fields if KYC exists
        kycRepo.findByStudent_StudentId(student.getStudentId()).ifPresent(kyc -> {
            if (req.getProfilePhotoUrl() != null)
                kyc.setProfilePhotoUrl(req.getProfilePhotoUrl());
            if (req.getGuardianName() != null)
                kyc.setGuardianName(req.getGuardianName());
            if (req.getGuardianRelation() != null)
                kyc.setGuardianRelation(req.getGuardianRelation());
            if (req.getGuardianPhone() != null)
                kyc.setGuardianPhone(req.getGuardianPhone());
            if (req.getInstituteName() != null)
                kyc.setInstituteName(req.getInstituteName());
            if (req.getInstituteAddress() != null)
                kyc.setInstituteAddress(req.getInstituteAddress());
            if (req.getLevelOfStudy() != null)
                kyc.setLevelOfStudy(req.getLevelOfStudy());
            if (req.getDietType() != null)
                kyc.setDietType(req.getDietType());
            kycRepo.save(kyc);
        });

        log.info("Profile updated for student: {}", email);
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────

    private User getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Student getStudentByUser(User user) {
        return studentRepo.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
    }

    // Map request fields onto a new StudentKyc entity
    private StudentKyc buildKycFromRequest(StudentKycRequest req, Student student) {
        StudentKyc kyc = new StudentKyc();
        kyc.setStudent(student);
        updateKycFields(kyc, req);
        return kyc;
    }

    // Shared field-mapping used by both submit and resubmit
    private void updateKycFields(StudentKyc kyc, StudentKycRequest req) {
        kyc.setDateOfBirth(req.getDateOfBirth());
        kyc.setDietType(req.getDietType());
        kyc.setProfilePhotoUrl(req.getProfilePhotoUrl());
        kyc.setGuardianName(req.getGuardianName());
        kyc.setGuardianRelation(req.getGuardianRelation());
        kyc.setGuardianPhone(req.getGuardianPhone());
        kyc.setDocumentType(req.getDocumentType());
        kyc.setIdentityNumber(req.getIdentityNumber());
        kyc.setDocumentPhotoUrl(req.getDocumentPhotoUrl());
        kyc.setInstituteName(req.getInstituteName());
        kyc.setInstituteAddress(req.getInstituteAddress());
        kyc.setLevelOfStudy(req.getLevelOfStudy());
        kyc.setProvince(req.getProvince());
        kyc.setDistrict(req.getDistrict());
        kyc.setMunicipality(req.getMunicipality());
        kyc.setTole(req.getTole());
        kyc.setWardNumber(req.getWardNumber());
    }
}
