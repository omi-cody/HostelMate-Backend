package com.fyp.HostelMate.dto.request;

import com.fyp.HostelMate.entity.enums.MaintenanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MaintenanceStatusRequest {

    @NotNull(message = "status is required")
    private MaintenanceStatus status;

    private String resolutionNote;
}
