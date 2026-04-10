package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.EventRequest;
import com.fyp.HostelMate.dto.response.ApiResponse;
import com.fyp.HostelMate.service.Impl.EventServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventServiceImpl eventService;

    // HOSTEL

    // Create a new event - automatically notifies all admitted students
    @PostMapping("/api/hostel/events")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Object>> createEvent(
            Authentication auth,
            @Valid @RequestBody EventRequest req) {
        var event = eventService.createEvent(auth.getName(), req);
        return ResponseEntity.ok(ApiResponse.success(
                "Event created and students notified", event));
    }

    // Edit event details
    @PutMapping("/api/hostel/events/{eventId}")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Object>> updateEvent(
            Authentication auth,
            @PathVariable UUID eventId,
            @Valid @RequestBody EventRequest req) {
        var event = eventService.updateEvent(auth.getName(), eventId, req);
        return ResponseEntity.ok(ApiResponse.success("Event updated", event));
    }

    // Remove an event from the hostel's calendar
    @DeleteMapping("/api/hostel/events/{eventId}")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            Authentication auth,
            @PathVariable UUID eventId) {
        eventService.deleteEvent(auth.getName(), eventId);
        return ResponseEntity.ok(ApiResponse.success("Event deleted"));
    }

    // Hostel sees all their past and upcoming events
    @GetMapping("/api/hostel/events")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Object>> getHostelEvents(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Events",
                eventService.getHostelEvents(auth.getName())));
    }

    //  STUDENT

    // Student sees events at their current hostel
    @GetMapping("/api/student/events/{hostelId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Object>> getEventsForStudent(
            @PathVariable UUID hostelId) {
        return ResponseEntity.ok(ApiResponse.success("Hostel events",
                eventService.getEventsForStudent(hostelId)));
    }
}
