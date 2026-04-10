package com.fyp.HostelMate.service;

import com.fyp.HostelMate.dto.request.StudentKycRequest;
import com.fyp.HostelMate.dto.request.UpdateStudentProfileRequest;
import com.fyp.HostelMate.entity.StudentKyc;

public interface StudentKycService {

    // Student submits KYC for the first time after registration
    void submitKyc(String email, StudentKycRequest request);

    // Student resubmits KYC after admin rejected it - updates existing record
    void resubmitKyc(String email, StudentKycRequest request);

    // Returns the student's current KYC record and status
    StudentKyc getMyKyc(String email);

    // Update allowed profile fields (cannot change DOB, address, documents after verification)
    void updateProfile(String email, UpdateStudentProfileRequest request);
}
