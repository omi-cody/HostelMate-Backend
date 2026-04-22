package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.HostelKycRequest;
import com.fyp.HostelMate.dto.request.UpdateHostelProfileRequest;
import com.fyp.HostelMate.entity.*;
import com.fyp.HostelMate.entity.enums.DayOfWeekEnum;
import com.fyp.HostelMate.entity.enums.RoomType;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import com.fyp.HostelMate.exceptions.BusinessException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HostelKycServiceImpl {

    private final UserRepository userRepo;
    private final HostelRepository hostelRepo;
    private final HostelKycRepository hostelKycRepo;

    //  SUBMIT KYC (first time) 
    @Transactional
    public void submitKyc(String email, HostelKycRequest req) {

        Hostel hostel = getHostelByEmail(email);

        if (hostelKycRepo.findByHostel_HostelId(hostel.getHostelId()).isPresent())
            throw new BusinessException(
                    "KYC already submitted. Use resubmit if it was rejected.");

        if (hostelKycRepo.existsByPanNumber(req.getPanNumber()))
            throw new BusinessException("This PAN number is already registered.");

        HostelKyc kyc = buildKycFromRequest(req, hostel);
        kyc.setKycStatus(VerificationStatus.SUBMITTED);
        kyc.setSubmittedAt(Instant.now());
        hostelKycRepo.save(kyc);

        hostel.setVerificationStatus(VerificationStatus.SUBMITTED);
        hostelRepo.save(hostel);

        log.info("Hostel KYC submitted: {}", email);
    }

    //  RESUBMIT KYC (after rejection) 
    @Transactional
    public void resubmitKyc(String email, HostelKycRequest req) {

        Hostel hostel = getHostelByEmail(email);

        HostelKyc kyc = hostelKycRepo.findByHostel_HostelId(hostel.getHostelId())
                .orElseThrow(() -> new BusinessException("No KYC found. Please submit first."));

        if (kyc.getKycStatus() != VerificationStatus.REJECTED)
            throw new BusinessException("Resubmission only allowed after rejection.");

        // PAN number and PAN document are locked - cannot change after first submission
        // This is to prevent fraud by swapping PAN after initial review
        updateMutableKycFields(kyc, req);
        kyc.setKycStatus(VerificationStatus.SUBMITTED);
        kyc.setRejectionRemark(null);
        kyc.setSubmittedAt(Instant.now());
        hostelKycRepo.save(kyc);

        hostel.setVerificationStatus(VerificationStatus.SUBMITTED);
        hostelRepo.save(hostel);

        log.info("Hostel KYC resubmitted: {}", email);
    }

    //  GET KYC
    public HostelKyc getMyKyc(String email) {
        Hostel hostel = getHostelByEmail(email);
        return hostelKycRepo.findByHostel_HostelId(hostel.getHostelId())
                .orElseThrow(() -> new ResourceNotFoundException("KYC not submitted yet"));
    }

    //  UPDATE PROFILE (logo, rules, amenities, meal plan) 
    @Transactional
    public void updateProfile(String email, UpdateHostelProfileRequest req) {

        Hostel hostel = getHostelByEmail(email);
        HostelKyc kyc = hostelKycRepo.findByHostel_HostelId(hostel.getHostelId())
                .orElseThrow(() -> new ResourceNotFoundException("KYC not found"));

        if (req.getLogoUrl() != null) kyc.setLogoUrl(req.getLogoUrl());
        if (req.getAmenities() != null) kyc.setAmenities(req.getAmenities());
        if (req.getRulesAndRegulations() != null)
            kyc.setRulesAndRegulations(req.getRulesAndRegulations());
        if (req.getHostelPhotoUrls() != null)
            kyc.setHostelPhotoUrls(req.getHostelPhotoUrls());

        // Replace meal plans if new ones are provided
        if (req.getMealPlans() != null && !req.getMealPlans().isEmpty()) {
            kyc.getMealPlans().clear();
            List<MealPlan> updated = buildMealPlans(req.getMealPlans(), kyc);
            kyc.getMealPlans().addAll(updated);
        }

        hostelKycRepo.save(kyc);
        log.info("Hostel profile updated: {}", email);
    }

    //  PRIVATE HELPERS

    private Hostel getHostelByEmail(String email) {
        return hostelRepo.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found"));
    }

    private HostelKyc buildKycFromRequest(HostelKycRequest req, Hostel hostel) {
        HostelKyc kyc = new HostelKyc();
        kyc.setHostel(hostel);
        kyc.setPanNumber(req.getPanNumber());
        kyc.setPanDocumentUrl(req.getPanDocumentUrl());
        updateMutableKycFields(kyc, req);
        return kyc;
    }

    // Fields that can be updated even after initial submission (not PAN)
    private void updateMutableKycFields(HostelKyc kyc, HostelKycRequest req) {
        kyc.setLogoUrl(req.getLogoUrl());
        kyc.setAdmissionFee(req.getAdmissionFee());
        kyc.setEstablishedYear(req.getEstablishedYear());
        kyc.setProvince(req.getProvince());
        kyc.setDistrict(req.getDistrict());
        kyc.setMunicipality(req.getMunicipality());
        kyc.setTole(req.getTole());
        kyc.setWardNumber(req.getWardNumber());
        kyc.setAmenities(req.getAmenities());
        kyc.setRulesAndRegulations(req.getRulesAndRegulations());

        // Store hostel photos as comma-separated URLs
        if (req.getHostelPhotoUrls() != null)
            kyc.setHostelPhotoUrls(String.join(",", req.getHostelPhotoUrls()));

        // Room pricing entries
        if (req.getRoomPricings() != null) {
            List<RoomPricing> pricings = req.getRoomPricings().stream().map(p -> {
                RoomPricing rp = new RoomPricing();
                rp.setHostelKyc(kyc);
                rp.setRoomType(RoomType.valueOf(p.getRoomType()));
                rp.setMonthlyPrice(p.getMonthlyPrice());
                return rp;
            }).collect(Collectors.toList());

            if (kyc.getRoomPricings() == null)
                kyc.setRoomPricings(new ArrayList<>());
            kyc.getRoomPricings().clear();
            kyc.getRoomPricings().addAll(pricings);
        }

        // Meal plans
        if (req.getMealPlans() != null) {
            if (kyc.getMealPlans() == null)
                kyc.setMealPlans(new ArrayList<>());
            kyc.getMealPlans().clear();
            kyc.getMealPlans().addAll(buildMealPlans(req.getMealPlans(), kyc));
        }
    }

    private List<MealPlan> buildMealPlans(
            List<HostelKycRequest.MealPlanRequest> planReqs, HostelKyc kyc) {
        return planReqs.stream().map(p -> {
            MealPlan mp = new MealPlan();
            mp.setHostelKyc(kyc);
            mp.setDayOfWeek(DayOfWeekEnum.valueOf(p.getDayOfWeek()));
            mp.setMorningBreakfast(p.getMorningBreakfast());
            mp.setLunch(p.getLunch());
            mp.setEveningSnack(p.getEveningSnack());
            mp.setDinner(p.getDinner());
            return mp;
        }).collect(Collectors.toList());
    }
}
