package com.fyp.HostelMate.dto.request;

import com.fyp.HostelMate.entity.enums.DietType;
import com.fyp.HostelMate.entity.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

// The student submits this after registration. File uploads (profile photo, document photo)
// are sent as multipart form data separately and stored before calling this endpoint.
// The URLs are then included in this request body.
@Data
public class StudentKycRequest {

    // Personal info
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotNull(message = "Diet preference is required")
    private DietType dietType;

    // URL returned after uploading the profile photo via /api/upload
    private String profilePhotoUrl;

    // Guardian info
    @NotBlank(message = "Guardian name is required")
    private String guardianName;

    @NotBlank(message = "Guardian relation is required")
    private String guardianRelation;

    @Pattern(regexp = "^[0-9]{10}$", message = "Guardian phone must be 10 digits")
    private String guardianPhone;

    // Identity document
    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "Identity number is required")
    private String identityNumber;

    // URL returned after uploading the document photo
    @NotBlank(message = "Document photo is required")
    private String documentPhotoUrl;

    // Institute details
    @NotBlank(message = "Institute name is required")
    private String instituteName;

    @NotBlank(message = "Institute address is required")
    private String instituteAddress;

    @NotBlank(message = "Level of study is required")
    private String levelOfStudy;

    // Permanent address
    @NotBlank(message = "Province is required")
    private String province;

    @NotBlank(message = "District is required")
    private String district;

    @NotBlank(message = "Municipality is required")
    private String municipality;

    @NotBlank(message = "Tole is required")
    private String tole;

    @NotBlank(message = "Ward number is required")
    private String wardNumber;
}
