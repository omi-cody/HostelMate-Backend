package com.fyp.HostelMate.service;

import com.fyp.HostelMate.entity.MaintenanceRequest;
import com.fyp.HostelMate.entity.enums.MaintenanceStatus;

import java.util.List;
import java.util.UUID;

public interface MaintenanceService {
    MaintenanceRequest submitRequest(MaintenanceRequest request);
    MaintenanceRequest updateRequestStatus(UUID complaintId, MaintenanceStatus status);
    List<MaintenanceRequest> getRequestsByStudent(UUID studentId);
    List<MaintenanceRequest> getRequestsByHostel(UUID hostelId);
}
