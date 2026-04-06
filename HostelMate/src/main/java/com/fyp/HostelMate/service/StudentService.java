package com.fyp.HostelMate.service;

import com.fyp.HostelMate.dto.request.StudentKycRequest;
import com.fyp.HostelMate.dto.request.StudentUpdateRequest;
import com.fyp.HostelMate.dto.response.StudentProfileResponse;
import com.fyp.HostelMate.entity.User;

public interface StudentService {
    void submitKyc(User currentUser, StudentKycRequest request);
    StudentProfileResponse getProfile(User currentUser);
    StudentProfileResponse updateProfile(User currentUser, StudentUpdateRequest request);
}
