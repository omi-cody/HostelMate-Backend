package com.fyp.HostelMate.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminDashboardResponse {
    private long totalStudents;
    private long totalHostels;
    private long pendingStudentVerifications;
    private long pendingHostelVerifications;
    private long verifiedStudents;
    private long verifiedHostels;
    private long activeHostels;
    private long totalCapacity;
    private double studentVerificationRate;
    private double hostelVerificationRate;
    private List<MonthlyGrowthEntry> studentGrowth;
    private List<MonthlyGrowthEntry> hostelGrowth;

    @Data
    @Builder
    public static class MonthlyGrowthEntry {
        private String month;
        private long count;
    }
}
