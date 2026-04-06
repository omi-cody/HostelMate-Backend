package com.fyp.HostelMate.dto.response;

import com.fyp.HostelMate.entity.Student;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class StudentProfileResponse {
    private UUID studentId;
    private UUID userId;
    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private String profilePicture;
    private LocalDate dateOfBirth;
    private String instituteName;
    private String levelOfStudy;
    private String instituteAddress;
    private String documentType;
    private String documentNumber;
    private String documentPhoto;
    private String province;
    private String district;
    private String municipality;
    private String tole;
    private int wardNumber;
    private VerificationStatus verificationStatus;

    public static StudentProfileResponse from(Student s) {
        return StudentProfileResponse.builder()
                .studentId(s.getStudentId())
                .userId(s.getUser().getUserId())
                .fullName(s.getFullName())
                .email(s.getUser().getEmail())
                .phone(s.getUser().getPhone())
                .gender(s.getGender() != null ? s.getGender().name() : null)
                .profilePicture(s.getProfilePicture())
                .dateOfBirth(s.getDateOfBirth())
                .instituteName(s.getInstituteName())
                .levelOfStudy(s.getLevelOfStudy())
                .instituteAddress(s.getInstituteAddress())
                .documentType(s.getDocumentType())
                .documentNumber(s.getDocumentNumber())
                .documentPhoto(s.getDocumentPhoto())
                .province(s.getProvince())
                .district(s.getDistrict())
                .municipality(s.getMunicipality())
                .tole(s.getTole())
                .wardNumber(s.getWardNumber())
                .verificationStatus(s.getUser().getVerificationStatus())
                .build();
    }
}
