package com.fyp.HostelMate.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Data shown on the student's main dashboard after they are admitted to a hostel.
// All fields are derived from the student's active admission and payment records.
@Data
@Builder
public class StudentDashboardResponse {

    // Current hostel summary
    private String hostelName;
    private String roomNumber;
    private String floor;
    private List<String> roommateNames;

    // Financial summary
    private BigDecimal totalPaidToDate;   // sum of all confirmed payments at this hostel
    private BigDecimal pendingAmount;     // unpaid fee amount for current or past months
    private BigDecimal monthlyFeeAmount;  // the recurring monthly fee for their room

    // Next fee due date - same day each month as their admission date
    private LocalDate nextFeeDueDate;

    // How many months they have been at this hostel
    private int monthsOfStay;

    // Recent complaint/maintenance requests with their current status
    private List<RecentComplaintItem> recentComplaints;

    @Data
    @Builder
    public static class RecentComplaintItem {
        private String title;
        private String requestType;  // COMPLAINT or MAINTENANCE
        private String status;       // PENDING, IN_PROGRESS, RESOLVED
        private String createdAt;
    }
}
