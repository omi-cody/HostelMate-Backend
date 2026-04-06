package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.response.HostelDashboardResponse;
import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.entity.enums.ApplicationStatus;
import com.fyp.HostelMate.entity.enums.LeaveStatus;
import com.fyp.HostelMate.entity.enums.MaintenanceStatus;
import com.fyp.HostelMate.entity.enums.PaymentStatus;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HostelDashboardServiceImpl {

    private final HostelRepository hostelRepository;
    private final AdmissionRepository admissionRepository;
    private final RoomRepository roomRepository;
    private final MaintenanceRequestRepository maintenanceRepository;
    private final ApplicationRepository applicationRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PaymentRepository paymentRepository;

    public HostelDashboardResponse getDashboard(User currentUser) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));
        java.util.UUID hid = hostel.getHostelId();

        long admitted = admissionRepository.findByHostel_HostelIdAndIsActiveTrue(hid).size();

        long totalCapacity = roomRepository.findByHostel_HostelIdAndIsActiveTrue(hid)
                .stream()
                .mapToLong(r -> switch (r.getRoomType()) {
                    case SINGLE -> 1; case DOUBLE -> 2; case TRIPLE -> 3; case QUAD -> 4;
                })
                .sum();

        double occupancy = totalCapacity > 0 ? (admitted * 100.0 / totalCapacity) : 0;

        long veg    = admissionRepository.countVegByHostelId(hid);
        long nonVeg = admissionRepository.countNonVegByHostelId(hid);

        long pendingMaint = maintenanceRepository.countByHostel_HostelIdAndStatus(hid, MaintenanceStatus.REPORTED);
        long pendingApps  = applicationRepository.findByHostel_HostelIdAndStatus(hid, ApplicationStatus.PENDING).size();
        long pendingLeave = leaveRequestRepository.findByHostel_HostelIdAndStatusOrderByCreatedAtDesc(hid, LeaveStatus.PENDING).size();

        Double collected = paymentRepository.sumCompletedByHostelId(hid);
        long pendingPaymentsCount = paymentRepository.findByHostel_HostelIdAndPaymentStatus(hid, PaymentStatus.PENDING).size();
        double pendingAmount = paymentRepository.findByHostel_HostelIdAndPaymentStatus(hid, PaymentStatus.PENDING)
                .stream().mapToDouble(p -> p.getAmount()).sum();

        return HostelDashboardResponse.builder()
                .totalAdmitted(admitted)
                .totalCapacity(totalCapacity)
                .occupancyRate(Math.round(occupancy * 10.0) / 10.0)
                .vegCount(veg)
                .nonVegCount(nonVeg)
                .pendingMaintenanceRequests(pendingMaint)
                .pendingApplications(pendingApps)
                .pendingLeaveRequests(pendingLeave)
                .totalPaymentCollected(collected != null ? collected : 0.0)
                .pendingPaymentAmount(pendingAmount)
                .build();
    }
}
