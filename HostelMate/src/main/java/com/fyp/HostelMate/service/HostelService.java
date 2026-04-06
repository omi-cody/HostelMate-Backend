package com.fyp.HostelMate.service;

import com.fyp.HostelMate.dto.request.HostelKycRequest;
import com.fyp.HostelMate.dto.request.HostelUpdateRequest;
import com.fyp.HostelMate.dto.response.HostelProfileResponse;
import com.fyp.HostelMate.entity.User;

import java.util.List;
import java.util.UUID;

public interface HostelService {
    void submitKyc(User currentUser, HostelKycRequest request);
    HostelProfileResponse getProfile(User currentUser);
    HostelProfileResponse updateProfile(User currentUser, HostelUpdateRequest request);
    HostelProfileResponse getPublicProfile(UUID hostelId);
    List<HostelProfileResponse> listVerifiedHostels();
}
