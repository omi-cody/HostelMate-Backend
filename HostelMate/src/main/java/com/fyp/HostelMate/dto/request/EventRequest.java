package com.fyp.HostelMate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

// Hostel creates or updates an event. On creation, all admitted students get a notification and email.
@Data
public class EventRequest {

    @NotBlank(message = "Event name is required")
    private String eventName;

    private String detail;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Event date is required")
    private LocalDateTime eventDate;
}
