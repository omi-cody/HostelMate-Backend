package com.fyp.HostelMate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

// Hostel sets a date and time for the student to visit the hostel premises
@Data
public class ScheduleVisitRequest {

    @NotNull(message = "Visit date and time is required")
    private LocalDateTime visitDateTime;
}
