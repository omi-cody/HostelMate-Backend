package com.fyp.HostelMate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MaintenanceRequestDto {

    @NotBlank(message = "complaintType is required")
    private String complaintType;

    @NotBlank(message = "description is required")
    private String description;
}
