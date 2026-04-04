package com.fyp.HostelMate.service;

import com.fyp.HostelMate.entity.Application;
import com.fyp.HostelMate.entity.enums.ApplicationStatus;

import java.util.List;
import java.util.UUID;

public interface ApplicationService {
    Application submitApplication(Application application);
    List<Application> getApplicationsByStudent(UUID studentId);
    List<Application> getApplicationsByHostel(UUID hostelId);
    Application updateApplicationStatus(UUID applicationId, ApplicationStatus status);
}
