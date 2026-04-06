package com.fyp.HostelMate.service.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fyp.HostelMate.dto.request.HostelKycRequest;
import com.fyp.HostelMate.dto.request.HostelUpdateRequest;
import com.fyp.HostelMate.dto.response.HostelProfileResponse;
import com.fyp.HostelMate.entity.*;
import com.fyp.HostelMate.entity.enums.DayOfWeek;
import com.fyp.HostelMate.entity.enums.HostelType;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import com.fyp.HostelMate.exceptions.BadRequestException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.HostelRepository;
import com.fyp.HostelMate.service.HostelService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HostelServiceImpl implements HostelService {

    private final HostelRepository hostelRepository;
    private final ObjectMapper objectMapper;
    private final com.fyp.HostelMate.service.FileUploadService fileUploadService;

    // ── KYC SUBMIT ────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void submitKyc(User currentUser, HostelKycRequest req) {

        if (currentUser.getVerificationStatus() == VerificationStatus.VERIFIED) {
            throw new BadRequestException("KYC already verified — you cannot resubmit.");
        }

        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel profile not found."));

        // Immutable identity fields
        hostel.setRegistrationNumber(req.getRegistrationNumber());
        hostel.setPanNumber(req.getPanNumber());
        hostel.setProvince(req.getProvince());
        hostel.setDistrict(req.getDistrict());
        hostel.setMunicipality(req.getMunicipality());
        hostel.setTole(req.getTole());
        hostel.setWardNo(req.getWardNo());

        // Hostel type & fee
        if (req.getHostelType() != null) {
            hostel.setHostelType(HostelType.valueOf(req.getHostelType().toUpperCase()));
        }
        if (req.getAdmissionFee() != null) hostel.setAdmissionFee(req.getAdmissionFee());

        // File uploads — named by hostel identity and file type
        if (req.getRegistrationPhoto() != null && !req.getRegistrationPhoto().isEmpty()) {
            hostel.setRegistrationPhotoUrl(fileUploadService.uploadHostelRegistration(
                    req.getRegistrationPhoto(),
                    hostel.getHostelId().toString(),
                    hostel.getHostelName()
            ));
        }
        if (req.getIdentityPhoto() != null && !req.getIdentityPhoto().isEmpty()) {
            hostel.setIdentityPhotoUrl(fileUploadService.uploadHostelIdentity(
                    req.getIdentityPhoto(),
                    hostel.getHostelId().toString(),
                    hostel.getHostelName()
            ));
        }

        // Hostel photos (up to 4) — photo_1_hostelname.ext, photo_2_hostelname.ext ...
        if (req.getHostelPhotos() != null) {
            hostel.getPhotos().clear();
            final int[] index = {1};
            req.getHostelPhotos().stream().limit(4).forEach(file -> {
                HostelPhoto photo = new HostelPhoto();
                photo.setPhotoUrl(fileUploadService.uploadHostelPhoto(
                        file,
                        hostel.getHostelId().toString(),
                        hostel.getHostelName(),
                        index[0]++
                ));
                hostel.addPhoto(photo);
            });
        }

        // Facilities
        if (req.getAmenities() != null) {
            hostel.getFacilities().clear();
            req.getAmenities().forEach(name -> {
                HostelFacility facility = new HostelFacility();
                facility.setFacilityName(name);
                hostel.addFacility(facility);
            });
        }

        // Rules
        if (req.getRules() != null) {
            hostel.getRules().clear();
            req.getRules().forEach(text -> {
                HostelRule rule = new HostelRule();
                rule.setRuleText(text);
                hostel.addRule(rule);
            });
        }

        // Meal plan JSON: [{"day":"SUNDAY","breakfast":"...","lunch":"...","eveningSnack":"...","dinner":"..."}]
        if (req.getMealsJson() != null && !req.getMealsJson().isBlank()) {
            try {
                List<Map<String, String>> meals = objectMapper.readValue(
                        req.getMealsJson(), new TypeReference<>() {});
                hostel.getMealPlans().clear();
                meals.forEach(m -> {
                    HostelMealPlan plan = new HostelMealPlan();
                    plan.setDayOfWeek(DayOfWeek.valueOf(m.get("day").toUpperCase()));
                    plan.setBreakfast(m.get("breakfast"));
                    plan.setLunch(m.get("lunch"));
                    plan.setEveningSnack(m.get("eveningSnack"));
                    plan.setDinner(m.get("dinner"));
                    hostel.addMealPlan(plan);
                });
            } catch (Exception e) {
                throw new BadRequestException("Invalid mealsJson format: " + e.getMessage());
            }
        }

        currentUser.setVerificationStatus(VerificationStatus.PENDING);
        hostelRepository.save(hostel);
        log.info("Hostel KYC submitted for userId={}", currentUser.getUserId());
    }

    // ── GET OWN PROFILE ───────────────────────────────────────────────────────
    @Override
    public HostelProfileResponse getProfile(User currentUser) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel profile not found."));
        return HostelProfileResponse.from(hostel);
    }

    // ── UPDATE PROFILE ────────────────────────────────────────────────────────
    @Override
    @Transactional
    public HostelProfileResponse updateProfile(User currentUser, HostelUpdateRequest req) {

        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel profile not found."));

        // Only mutable fields — regNo, panNo, regPhoto are permanently locked
        if (req.getHostelName() != null)      hostel.setHostelName(req.getHostelName());
        if (req.getOwnerName() != null)        hostel.setOwnerName(req.getOwnerName());
        if (req.getPhone() != null)            currentUser.setPhone(req.getPhone());
        if (req.getTotalRoom() != null)        hostel.setTotalRoom(req.getTotalRoom());
        if (req.getEstablishedYear() != null)  hostel.setEstablishedYear(req.getEstablishedYear());
        if (req.getAdmissionFee() != null)     hostel.setAdmissionFee(req.getAdmissionFee());

        if (req.getHostelLogo() != null && !req.getHostelLogo().isEmpty()) {
            hostel.setHostelLogo(fileUploadService.uploadHostelLogo(
                    req.getHostelLogo(),
                    hostel.getHostelId().toString(),
                    hostel.getHostelName()
            ));
        }

        if (req.getFacilities() != null) {
            hostel.getFacilities().clear();
            req.getFacilities().forEach(name -> {
                HostelFacility f = new HostelFacility();
                f.setFacilityName(name);
                hostel.addFacility(f);
            });
        }

        if (req.getRules() != null) {
            hostel.getRules().clear();
            req.getRules().forEach(text -> {
                HostelRule r = new HostelRule();
                r.setRuleText(text);
                hostel.addRule(r);
            });
        }

        if (req.getMealsJson() != null && !req.getMealsJson().isBlank()) {
            try {
                List<Map<String, String>> meals = objectMapper.readValue(
                        req.getMealsJson(), new TypeReference<>() {});
                hostel.getMealPlans().clear();
                meals.forEach(m -> {
                    HostelMealPlan plan = new HostelMealPlan();
                    plan.setDayOfWeek(DayOfWeek.valueOf(m.get("day").toUpperCase()));
                    plan.setBreakfast(m.get("breakfast"));
                    plan.setLunch(m.get("lunch"));
                    plan.setEveningSnack(m.get("eveningSnack"));
                    plan.setDinner(m.get("dinner"));
                    hostel.addMealPlan(plan);
                });
            } catch (Exception e) {
                throw new BadRequestException("Invalid mealsJson format: " + e.getMessage());
            }
        }

        hostelRepository.save(hostel);
        log.info("Hostel profile updated for userId={}", currentUser.getUserId());
        return HostelProfileResponse.from(hostel);
    }

    // ── PUBLIC DETAIL ─────────────────────────────────────────────────────────
    @Override
    public HostelProfileResponse getPublicProfile(UUID hostelId) {
        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));
        if (hostel.getUser().getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new ResourceNotFoundException("Hostel not found.");
        }
        return HostelProfileResponse.from(hostel);
    }

    // ── PUBLIC LIST ───────────────────────────────────────────────────────────
    @Override
    public List<HostelProfileResponse> listVerifiedHostels() {
        return hostelRepository.findByUser_VerificationStatus(VerificationStatus.VERIFIED)
                .stream()
                .map(HostelProfileResponse::from)
                .toList();
    }
}
