package com.fyp.HostelMate.dto.response;

import com.fyp.HostelMate.entity.Application;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ApplicationResponse {
    private UUID applicationId;
    private UUID studentId;
    private String studentName;
    private UUID hostelId;
    private String hostelName;
    private String applicationType;
    private String status;
    private LocalDateTime visitScheduledAt;
    private String hostelRemarks;
    private UUID roomId;
    private String roomNumber;
    private Instant appliedAt;
    private Instant updatedAt;

    public static ApplicationResponse from(Application a) {
        return ApplicationResponse.builder()
                .applicationId(a.getApplicationId())
                .studentId(a.getStudent().getStudentId())
                .studentName(a.getStudent().getFullName())
                .hostelId(a.getHostel().getHostelId())
                .hostelName(a.getHostel().getHostelName())
                .applicationType(a.getApplicationType().name())
                .status(a.getStatus().name())
                .visitScheduledAt(a.getVisitScheduledAt())
                .hostelRemarks(a.getHostelRemarks())
                .roomId(a.getRoom() != null ? a.getRoom().getRoomId() : null)
                .roomNumber(a.getRoom() != null ? a.getRoom().getRoomNumber() : null)
                .appliedAt(a.getAppliedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
