package com.fyp.HostelMate.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class HostelUpdateRequest {
    private String hostelName;
    private String ownerName;
    private String phone;
    private MultipartFile hostelLogo;
    private String totalRoom;
    private Integer establishedYear;
    private Double admissionFee;
    private List<String> facilities;
    private List<String> rules;
    private String mealsJson;
}
