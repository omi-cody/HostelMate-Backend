package com.fyp.HostelMate.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class HostelKycRequest {
    // Registration / Identity
    private String registrationNumber;
    private MultipartFile registrationPhoto;
    private String idType;
    private String identityNumber;
    private MultipartFile identityPhoto;

    // Address
    private String province;
    private String district;
    private String municipality;
    private String tole;
    private String wardNo;

    // Hostel Info
    private List<MultipartFile> hostelPhotos;
    private String hostelType;
    private Double admissionFee;

    // Lists
    private List<String> rules;
    private List<String> amenities;

    // Complex structures sent as JSON strings from frontend
    // Frontend does: formData.append('roomsJson', JSON.stringify(rooms))
    private String roomsJson;
    // Frontend does: formData.append('mealsJson', JSON.stringify(meals))
    private String mealsJson;
}
