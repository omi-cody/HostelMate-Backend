package com.fyp.HostelMate.dto.request;

import com.fyp.HostelMate.entity.enums.ApplicationType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ApplicationRequest {

    @NotNull(message = "Hostel ID is required")
    private UUID hostelId;

    @NotNull(message = "Application type is required (VISIT or ADMISSION)")
    private ApplicationType applicationType;
}
