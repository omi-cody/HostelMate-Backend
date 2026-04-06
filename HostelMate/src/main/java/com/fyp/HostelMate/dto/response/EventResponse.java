package com.fyp.HostelMate.dto.response;

import com.fyp.HostelMate.entity.Event;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EventResponse {
    private UUID eventId;
    private UUID hostelId;
    private String hostelName;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private String location;
    private Instant createdAt;
    private Instant updatedAt;

    public static EventResponse from(Event e) {
        return EventResponse.builder()
                .eventId(e.getEventId())
                .hostelId(e.getHostel().getHostelId())
                .hostelName(e.getHostel().getHostelName())
                .title(e.getTitle())
                .description(e.getDescription())
                .eventDate(e.getEventDate())
                .location(e.getLocation())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
