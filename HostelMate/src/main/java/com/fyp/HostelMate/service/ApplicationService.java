package com.fyp.HostelMate.service;

import com.fyp.HostelMate.dto.request.ApplicationRequest;
import com.fyp.HostelMate.dto.request.ApplicationStatusRequest;
import com.fyp.HostelMate.dto.response.AdmissionResponse;
import com.fyp.HostelMate.dto.response.ApplicationResponse;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.entity.enums.MealPreference;

import java.util.List;
import java.util.UUID;

public interface ApplicationService {

    // Student actions
    ApplicationResponse apply(User currentUser, ApplicationRequest request);
    List<ApplicationResponse> getMyApplications(User currentUser);
    void cancelApplication(User currentUser, UUID applicationId);

    // Hostel actions
    List<ApplicationResponse> getIncomingApplications(User currentUser);
    ApplicationResponse updateApplicationStatus(User currentUser, UUID applicationId, ApplicationStatusRequest request);

    // Admission
    AdmissionResponse getMyAdmission(User currentUser);
    List<AdmissionResponse> getHostelAdmissions(User currentUser);
    AdmissionResponse updateMealPreference(User currentUser, MealPreference preference);
}
