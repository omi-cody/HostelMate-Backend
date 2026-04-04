package com.fyp.HostelMate.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class StudentKycRequest {
    private MultipartFile profilePicture;
    private String dob;
    private String levelOfStudy;
    private String instituteName;
    private String instituteAddress;
    private String idType;
    private String identityNumber;
    private MultipartFile identityPhoto;
    private String province;
    private String district;
    private String municipality;
    private String tole;
    private String wardNo;
}
