package com.fyp.HostelMate.dto.response;

import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class HostelProfileResponse {
    private UUID hostelId;
    private UUID userId;
    private String hostelName;
    private String ownerName;
    private String email;
    private String phone;
    private String hostelType;
    private String hostelLogo;
    private String totalRoom;
    private Integer establishedYear;
    private Double admissionFee;
    private String registrationNumber;
    private String panNumber;
    private String registrationPhotoUrl;
    private String province;
    private String district;
    private String municipality;
    private String tole;
    private String wardNo;
    private List<String> photoUrls;
    private List<String> facilities;
    private List<String> rules;
    private VerificationStatus verificationStatus;

    public static HostelProfileResponse from(Hostel h) {
        return HostelProfileResponse.builder()
                .hostelId(h.getHostelId())
                .userId(h.getUser().getUserId())
                .hostelName(h.getHostelName())
                .ownerName(h.getOwnerName())
                .email(h.getUser().getEmail())
                .phone(h.getUser().getPhone())
                .hostelType(h.getHostelType() != null ? h.getHostelType().name() : null)
                .hostelLogo(h.getHostelLogo())
                .totalRoom(h.getTotalRoom())
                .establishedYear(h.getEstablishedYear())
                .admissionFee(h.getAdmissionFee())
                .registrationNumber(h.getRegistrationNumber())
                .panNumber(h.getPanNumber())
                .registrationPhotoUrl(h.getRegistrationPhotoUrl())
                .province(h.getProvince())
                .district(h.getDistrict())
                .municipality(h.getMunicipality())
                .tole(h.getTole())
                .wardNo(h.getWardNo())
                .photoUrls(h.getPhotos().stream()
                        .map(p -> p.getPhotoUrl()).toList())
                .facilities(h.getFacilities().stream()
                        .map(f -> f.getFacilityName()).toList())
                .rules(h.getRules().stream()
                        .map(r -> r.getRuleText()).toList())
                .verificationStatus(h.getUser().getVerificationStatus())
                .build();
    }
}
