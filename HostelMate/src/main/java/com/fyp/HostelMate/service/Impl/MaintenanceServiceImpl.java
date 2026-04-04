package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.entity.MaintenanceRequest;
import com.fyp.HostelMate.entity.enums.MaintenanceStatus;
import com.fyp.HostelMate.repository.MaintenanceRequestRepository;
import com.fyp.HostelMate.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceRequestRepository maintenanceRepository;

    @Autowired
    public MaintenanceServiceImpl(MaintenanceRequestRepository maintenanceRepository) {
        this.maintenanceRepository = maintenanceRepository;
    }

    @Override
    public MaintenanceRequest submitRequest(MaintenanceRequest request) {
        request.setStatus(MaintenanceStatus.REPORTED);
        return maintenanceRepository.save(request);
    }

    @Override
    public MaintenanceRequest updateRequestStatus(UUID complaintId, MaintenanceStatus status) {
        MaintenanceRequest request = maintenanceRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Maintenance Request not found"));
        request.setStatus(status);
        if (status == MaintenanceStatus.RESOLVED) {
            request.setResolvedAt(Instant.now());
        }
        return maintenanceRepository.save(request);
    }

    @Override
    public List<MaintenanceRequest> getRequestsByStudent(UUID studentId) {
        return maintenanceRepository.findByStudentStudentId(studentId);
    }

    @Override
    public List<MaintenanceRequest> getRequestsByHostel(UUID hostelId) {
        return maintenanceRepository.findByHostelHostelId(hostelId);
    }
}
