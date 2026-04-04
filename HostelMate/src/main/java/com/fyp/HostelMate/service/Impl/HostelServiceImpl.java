package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.HostelKycRequest;
import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.HostelKyc;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import com.fyp.HostelMate.repository.HostelKycRepository;
import com.fyp.HostelMate.repository.HostelRepository;
import com.fyp.HostelMate.repository.UserRepository;
import com.fyp.HostelMate.service.FileService;
import com.fyp.HostelMate.service.HostelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class HostelServiceImpl implements HostelService {

    private final HostelRepository hostelRepository;
    private final UserRepository userRepository;
    private final HostelKycRepository hostelKycRepository;
    private final FileService fileService;

    @Autowired
    public HostelServiceImpl(HostelRepository hostelRepository,
                             UserRepository userRepository,
                             HostelKycRepository hostelKycRepository,
                             FileService fileService) {
        this.hostelRepository = hostelRepository;
        this.userRepository = userRepository;
        this.hostelKycRepository = hostelKycRepository;
        this.fileService = fileService;
    }

    @Override
    public List<Hostel> getAllHostels() {
        return hostelRepository.findAll();
    }

    @Override
    public Hostel getHostelById(UUID id) {
        return hostelRepository.findById(id).orElseThrow(() -> new RuntimeException("Hostel not found"));
    }

    @Override
    public List<Hostel> searchHostels(String keyword, String city) {
        return hostelRepository.findAll();
    }

    @Override
    @Transactional
    public void submitKyc(UUID hostelId, HostelKycRequest request) throws IOException {
        Hostel hostel = getHostelById(hostelId);

        // Upload single files
        String registrationPhotoUrl = null;
        if (request.getRegistrationPhoto() != null && !request.getRegistrationPhoto().isEmpty()) {
            registrationPhotoUrl = fileService.saveFile(request.getRegistrationPhoto(), "hostel_kyc/registration");
        }

        String identityPhotoUrl = null;
        if (request.getIdentityPhoto() != null && !request.getIdentityPhoto().isEmpty()) {
            identityPhotoUrl = fileService.saveFile(request.getIdentityPhoto(), "hostel_kyc/identity");
        }

        // Upload multiple hostel photos
        List<String> hostelPhotoUrls = new ArrayList<>();
        if (request.getHostelPhotos() != null) {
            for (MultipartFile photo : request.getHostelPhotos()) {
                if (photo != null && !photo.isEmpty()) {
                    hostelPhotoUrls.add(fileService.saveFile(photo, "hostel_kyc/photos"));
                }
            }
        }

        // Create or update KYC record
        HostelKyc kyc = hostel.getHostelKyc();
        if (kyc == null) {
            kyc = new HostelKyc();
            kyc.setHostel(hostel);
        }

        kyc.setRegistrationNumber(request.getRegistrationNumber());
        kyc.setRegistrationPhotoUrl(registrationPhotoUrl);
        kyc.setIdType(request.getIdType());
        kyc.setIdentityNumber(request.getIdentityNumber());
        kyc.setIdentityPhotoUrl(identityPhotoUrl);
        kyc.setProvince(request.getProvince());
        kyc.setDistrict(request.getDistrict());
        kyc.setMunicipality(request.getMunicipality());
        kyc.setTole(request.getTole());
        kyc.setWardNo(request.getWardNo());
        kyc.setHostelPhotoUrls(hostelPhotoUrls);
        kyc.setHostelType(request.getHostelType());
        kyc.setAdmissionFee(request.getAdmissionFee());

        if (request.getRules() != null) kyc.setRules(request.getRules());
        if (request.getAmenities() != null) kyc.setAmenities(request.getAmenities());

        // Store complex JSON structures as-is (from frontend JSON.stringify)
        kyc.setRoomsJson(request.getRoomsJson());
        kyc.setMealsJson(request.getMealsJson());

        hostelKycRepository.save(kyc);
        hostel.setHostelKyc(kyc);
        hostelRepository.save(hostel);

        // Set user verification status to PENDING
        User user = hostel.getUser();
        user.setVerificationStatus(VerificationStatus.PENDING);
        userRepository.save(user);
    }


}
