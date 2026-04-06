package com.fyp.HostelMate.dto.request;

import lombok.Data;

@Data
public class StudentUpdateRequest {
    private String profilePicture;
    private String instituteName;
    private String levelOfStudy;
    private String instituteAddress;
    private String phone;
}
