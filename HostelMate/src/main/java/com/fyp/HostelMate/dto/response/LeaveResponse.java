package com.fyp.HostelMate.dto.response;

import com.fyp.HostelMate.entity.LeaveRequest;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class LeaveResponse {
    private UUID leaveRequestId;
    private UUID studentId;
    private String studentName;
    private UUID hostelId;
    private String hostelName;
    private UUID admissionId;
    private LocalDate requestedLeaveDate;
    private String reason;
    private String status;
    private String hostelRemarks;
    private Instant createdAt;
    private Instant processedAt;

    public static LeaveResponse from(LeaveRequest l) {
        return LeaveResponse.builder()
                .leaveRequestId(l.getLeaveRequestId())
                .studentId(l.getStudent().getStudentId())
                .studentName(l.getStudent().getFullName())
                .hostelId(l.getHostel().getHostelId())
                .hostelName(l.getHostel().getHostelName())
                .admissionId(l.getAdmission().getAdmissionId())
                .requestedLeaveDate(l.getRequestedLeaveDate())
                .reason(l.getReason())
                .status(l.getStatus().name())
                .hostelRemarks(l.getHostelRemarks())
                .createdAt(l.getCreatedAt())
                .processedAt(l.getProcessedAt())
                .build();
    }
}
