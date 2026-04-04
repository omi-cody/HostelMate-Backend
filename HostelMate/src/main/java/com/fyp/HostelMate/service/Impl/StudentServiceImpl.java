package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.StudentKycRequest;
import com.fyp.HostelMate.entity.Student;
import com.fyp.HostelMate.entity.StudentKyc;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import com.fyp.HostelMate.repository.StudentKycRepository;
import com.fyp.HostelMate.repository.StudentRepository;
import com.fyp.HostelMate.repository.UserRepository;
import com.fyp.HostelMate.service.FileService;
import com.fyp.HostelMate.service.StudentService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StudentKycRepository studentKycRepository;
    private final FileService fileService;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository,
                              UserRepository userRepository,
                              StudentKycRepository studentKycRepository,
                              FileService fileService) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.studentKycRepository = studentKycRepository;
        this.fileService = fileService;
    }

    @Override
    public Student getStudentProfile(UUID studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    @Override
    public Student updateStudentProfile(UUID studentId, Student studentDetails) {
        Student existingStudent = getStudentProfile(studentId);
        existingStudent.setFullName(studentDetails.getFullName());
        existingStudent.setContactNumber(studentDetails.getContactNumber());
        existingStudent.setParentGuardianContact(studentDetails.getParentGuardianContact());
        existingStudent.setDateOfBirth(studentDetails.getDateOfBirth());
        existingStudent.setAddress(studentDetails.getAddress());
        return studentRepository.save(existingStudent);
    }

    @Override
    @Transactional
    public void submitKyc(UUID studentId, StudentKycRequest request) throws IOException {
        Student student = getStudentProfile(studentId);

        // Upload files
        String profilePictureUrl = null;
        if (request.getProfilePicture() != null && !request.getProfilePicture().isEmpty()) {
            profilePictureUrl = fileService.saveFile(request.getProfilePicture(), "student_kyc/profile");
        }

        String identityPhotoUrl = null;
        if (request.getIdentityPhoto() != null && !request.getIdentityPhoto().isEmpty()) {
            identityPhotoUrl = fileService.saveFile(request.getIdentityPhoto(), "student_kyc/identity");
        }

        // Create or update KYC record
        StudentKyc kyc = student.getStudentKyc();
        if (kyc == null) {
            kyc = new StudentKyc();
            kyc.setStudent(student);
        }

        kyc.setProfilePictureUrl(profilePictureUrl);
        kyc.setDob(request.getDob());
        kyc.setLevelOfStudy(request.getLevelOfStudy());
        kyc.setInstituteName(request.getInstituteName());
        kyc.setInstituteAddress(request.getInstituteAddress());
        kyc.setIdType(request.getIdType());
        kyc.setIdentityNumber(request.getIdentityNumber());
        kyc.setIdentityPhotoUrl(identityPhotoUrl);
        kyc.setProvince(request.getProvince());
        kyc.setDistrict(request.getDistrict());
        kyc.setMunicipality(request.getMunicipality());
        kyc.setTole(request.getTole());
        kyc.setWardNo(request.getWardNo());

        studentKycRepository.save(kyc);

        student.setStudentKyc(kyc);
        studentRepository.save(student);

        // Set user verification status to PENDING
        User user = student.getUser();
        user.setVerificationStatus(VerificationStatus.PENDING);
        userRepository.save(user);
    }
}
