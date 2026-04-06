package com.fyp.HostelMate.dto.response;

import com.fyp.HostelMate.entity.MaintenanceRequest;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class MaintenanceResponse {
    private UUID requestId;
    private UUID studentId;
    private String studentName;
    private UUID hostelId;
    private UUID roomId;
    private String roomNumber;
    private String complaintType;
    private String description;
    private String status;
    private String resolutionNote;
    private Instant createdAt;
    private Instant updatedAt;

    public static MaintenanceResponse from(MaintenanceRequest m) {
        return MaintenanceResponse.builder()
                .requestId(m.getRequestId())
                .studentId(m.getStudent().getStudentId())
                .studentName(m.getStudent().getFullName())
                .hostelId(m.getHostel().getHostelId())
                .roomId(m.getRoom() != null ? m.getRoom().getRoomId() : null)
                .roomNumber(m.getRoom() != null ? m.getRoom().getRoomNumber() : null)
                .complaintType(m.getComplaintType())
                .description(m.getDescription())
                .status(m.getStatus().name())
                .resolutionNote(m.getResolutionNote())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
