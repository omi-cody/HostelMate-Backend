package com.fyp.HostelMate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveRequestDto {

    @NotNull(message = "requestedLeaveDate is required")
    private LocalDate requestedLeaveDate;

    private String reason;
}
