package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.StudentKycRequest;
import com.fyp.HostelMate.dto.request.StudentUpdateRequest;
import com.fyp.HostelMate.dto.response.StudentProfileResponse;
import com.fyp.HostelMate.entity.Student;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import com.fyp.HostelMate.exceptions.BadRequestException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.StudentRepository;
import com.fyp.HostelMate.service.StudentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final com.fyp.HostelMate.service.FileUploadService fileUploadService;

    // ── KYC SUBMIT ────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void submitKyc(User currentUser, StudentKycRequest req) {

        if (currentUser.getVerificationStatus() == VerificationStatus.VERIFIED) {
            throw new BadRequestException("KYC already verified — you cannot resubmit.");
        }

        Student student = studentRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));

        // Immutable fields (set once, never changed after KYC)
        student.setDateOfBirth(LocalDate.parse(req.getDob()));
        student.setDocumentType(req.getIdType());
        student.setDocumentNumber(req.getIdentityNumber());
        student.setProvince(req.getProvince());
        student.setDistrict(req.getDistrict());
        student.setMunicipality(req.getMunicipality());
        student.setTole(req.getTole());
        student.setWardNumber(Integer.parseInt(req.getWardNo()));

        // Mutable fields also populated at KYC time
        student.setLevelOfStudy(req.getLevelOfStudy());
        student.setInstituteName(req.getInstituteName());
        student.setInstituteAddress(req.getInstituteAddress());

        // File uploads — named by student identity and file type
        if (req.getProfilePicture() != null && !req.getProfilePicture().isEmpty()) {
            student.setProfilePicture(fileUploadService.uploadStudentProfile(
                    req.getProfilePicture(),
                    student.getStudentId().toString(),
                    student.getFullName()
            ));
        }
        if (req.getIdentityPhoto() != null && !req.getIdentityPhoto().isEmpty()) {
            student.setDocumentPhoto(fileUploadService.uploadStudentDocument(
                    req.getIdentityPhoto(),
                    student.getStudentId().toString(),
                    student.getFullName(),
                    req.getIdType() != null ? req.getIdType() : "document"
            ));
        }

        // Mark as pending admin review
        currentUser.setVerificationStatus(VerificationStatus.PENDING);
        studentRepository.save(student);

        log.info("Student KYC submitted for userId={}", currentUser.getUserId());
    }

    // ── GET PROFILE ───────────────────────────────────────────────────────────
    @Override
    public StudentProfileResponse getProfile(User currentUser) {
        Student student = studentRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));
        return StudentProfileResponse.from(student);
    }

    // ── UPDATE PROFILE ────────────────────────────────────────────────────────
    @Override
    @Transactional
    public StudentProfileResponse updateProfile(User currentUser, StudentUpdateRequest req) {

        if (currentUser.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new BadRequestException("Profile can only be updated after KYC is verified.");
        }

        Student student = studentRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));

        // Only mutable fields — dob, document, permanent address are locked
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            currentUser.setPhone(req.getPhone());
        }
        if (req.getInstituteName() != null)   student.setInstituteName(req.getInstituteName());
        if (req.getLevelOfStudy() != null)     student.setLevelOfStudy(req.getLevelOfStudy());
        if (req.getInstituteAddress() != null) student.setInstituteAddress(req.getInstituteAddress());
        if (req.getProfilePicture() != null)   student.setProfilePicture(req.getProfilePicture());

        studentRepository.save(student);
        log.info("Student profile updated for userId={}", currentUser.getUserId());
        return StudentProfileResponse.from(student);
    }
}
