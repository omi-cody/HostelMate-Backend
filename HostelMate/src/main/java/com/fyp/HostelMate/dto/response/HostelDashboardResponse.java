package com.fyp.HostelMate.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HostelDashboardResponse {
    private long totalAdmitted;
    private long totalCapacity;
    private double occupancyRate;
    private long vegCount;
    private long nonVegCount;
    private long pendingMaintenanceRequests;
    private long pendingApplications;
    private long pendingLeaveRequests;
    private double totalPaymentCollected;
    private double pendingPaymentAmount;
}
