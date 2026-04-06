package com.fyp.HostelMate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventRequest {

    @NotBlank(message = "title is required")
    private String title;

    private String description;

    @NotNull(message = "eventDate is required")
    private LocalDateTime eventDate;

    private String location;
}
