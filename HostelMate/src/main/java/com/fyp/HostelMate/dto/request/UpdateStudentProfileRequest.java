package com.fyp.HostelMate.dto.request;

import com.fyp.HostelMate.entity.enums.DietType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Student can update basic profile info but cannot change DOB, permanent address, or documents.
// Those fields are locked after KYC verification - only admin can change them.
@Data
public class UpdateStudentProfileRequest {

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    // URL of the new profile photo - upload first via /api/upload, then send the URL here
    private String profilePhotoUrl;

    // Diet preference can be updated at any time (not locked after KYC)
    private DietType dietType;

    // Guardian contact can be updated
    private String guardianPhone;
    private String guardianName;
    private String guardianRelation;

    // Institute info can change if student transfers
    private String instituteName;
    private String instituteAddress;
    private String levelOfStudy;
}
