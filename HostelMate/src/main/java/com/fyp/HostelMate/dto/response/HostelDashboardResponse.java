package com.fyp.HostelMate.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

// Overview data shown on the hostel's main dashboard.
// Gives the hostel owner a quick snapshot of the current state of their hostel.
@Data
@Builder
public class HostelDashboardResponse {

    // Student counts
    private long totalStudents;         // currently admitted
    private long totalApplications;     // all time, including pending
    private long pendingApplications;   // still waiting for hostel to respond

    // Room and bed availability
    private int totalRooms;
    private int totalCapacity;          // sum of all room capacities
    private int occupiedBeds;           // beds with active admission
    private int availableBeds;

    // Financial overview
    private BigDecimal monthlyRevenue;  // confirmed payments this month
    private BigDecimal totalRevenue;    // all time confirmed payments
    private BigDecimal pendingFees;     // unpaid amounts from current students

    // Pending maintenance and complaint requests needing attention
    private long pendingMaintenanceCount;
    private long pendingComplaintCount;

    // List of recent pending maintenance requests shown on dashboard
    private List<PendingRequestItem> recentPendingRequests;

    @Data
    @Builder
    public static class PendingRequestItem {
        private String requestId;
        private String studentName;
        private String title;
        private String requestType;
        private String createdAt;
    }
}
