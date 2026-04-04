package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.entity.Application;
import com.fyp.HostelMate.entity.enums.ApplicationStatus;
import com.fyp.HostelMate.repository.ApplicationRepository;
import com.fyp.HostelMate.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Autowired
    public ApplicationServiceImpl(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public Application submitApplication(Application application) {
        application.setApplicationDate(Instant.now());
        application.setStatus(ApplicationStatus.PENDING);
        return applicationRepository.save(application);
    }

    @Override
    public List<Application> getApplicationsByStudent(UUID studentId) {
        return applicationRepository.findByStudentStudentId(studentId);
    }

    @Override
    public List<Application> getApplicationsByHostel(UUID hostelId) {
        return applicationRepository.findByHostelHostelId(hostelId);
    }

    @Override
    public Application updateApplicationStatus(UUID applicationId, ApplicationStatus status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        application.setStatus(status);
        return applicationRepository.save(application);
    }
}
