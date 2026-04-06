package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.EventRequest;
import com.fyp.HostelMate.dto.response.EventResponse;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.service.Impl.EventServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventServiceImpl eventService;

    /** POST /api/hostel/events — hostel creates an event, notifies all admitted students */
    @PostMapping("/api/hostel/events")
    public ResponseEntity<EventResponse> createEvent(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(currentUser, request));
    }

    /** GET /api/hostel/events — hostel lists all their events */
    @GetMapping("/api/hostel/events")
    public ResponseEntity<List<EventResponse>> getHostelEvents(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(eventService.getHostelEvents(currentUser));
    }

    /** PUT /api/hostel/events/{id} — hostel edits an event */
    @PutMapping("/api/hostel/events/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(currentUser, id, request));
    }

    /** DELETE /api/hostel/events/{id} — hostel deletes an event */
    @DeleteMapping("/api/hostel/events/{id}")
    public ResponseEntity<?> deleteEvent(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        eventService.deleteEvent(currentUser, id);
        return ResponseEntity.ok(Map.of("message", "Event deleted."));
    }

    /** GET /api/student/events — student views upcoming events for their hostel */
    @GetMapping("/api/student/events")
    public ResponseEntity<List<EventResponse>> getStudentEvents(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(eventService.getUpcomingEventsForStudent(currentUser));
    }
}
