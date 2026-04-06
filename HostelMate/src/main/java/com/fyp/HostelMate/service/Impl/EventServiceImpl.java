package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.EventRequest;
import com.fyp.HostelMate.dto.response.EventResponse;
import com.fyp.HostelMate.entity.*;
import com.fyp.HostelMate.entity.enums.NotificationType;
import com.fyp.HostelMate.exceptions.BadRequestException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.*;
import com.fyp.HostelMate.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl {

    private final EventRepository eventRepository;
    private final HostelRepository hostelRepository;
    private final AdmissionRepository admissionRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    // ── HOSTEL: CREATE EVENT ──────────────────────────────────────────────────
    @Transactional
    public EventResponse createEvent(User currentUser, EventRequest req) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));

        Event event = new Event();
        event.setHostel(hostel);
        event.setTitle(req.getTitle());
        event.setDescription(req.getDescription());
        event.setEventDate(req.getEventDate());
        event.setLocation(req.getLocation());

        eventRepository.save(event);

        // Notify all currently admitted students
        List<Admission> admitted = admissionRepository.findByHostel_HostelIdAndIsActiveTrue(hostel.getHostelId());
        admitted.forEach(admission -> notificationService.send(
                admission.getStudent().getUser(),
                NotificationType.NEW_HOSTEL_EVENT,
                "New event: " + req.getTitle(),
                hostel.getHostelName() + " has posted a new event on " + req.getEventDate(),
                "event:" + event.getEventId()
        ));

        log.info("Event created: eventId={} hostelId={}", event.getEventId(), hostel.getHostelId());
        return EventResponse.from(event);
    }

    // ── HOSTEL: UPDATE EVENT ──────────────────────────────────────────────────
    @Transactional
    public EventResponse updateEvent(User currentUser, UUID eventId, EventRequest req) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found."));

        if (!event.getHostel().getHostelId().equals(hostel.getHostelId())) {
            throw new BadRequestException("This event does not belong to your hostel.");
        }

        event.setTitle(req.getTitle());
        event.setDescription(req.getDescription());
        event.setEventDate(req.getEventDate());
        event.setLocation(req.getLocation());
        event.setUpdatedAt(Instant.now());

        eventRepository.save(event);
        return EventResponse.from(event);
    }

    // ── HOSTEL: DELETE EVENT ──────────────────────────────────────────────────
    @Transactional
    public void deleteEvent(User currentUser, UUID eventId) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found."));

        if (!event.getHostel().getHostelId().equals(hostel.getHostelId())) {
            throw new BadRequestException("This event does not belong to your hostel.");
        }

        eventRepository.delete(event);
        log.info("Event deleted: eventId={}", eventId);
    }

    // ── HOSTEL: LIST OWN EVENTS ───────────────────────────────────────────────
    public List<EventResponse> getHostelEvents(User currentUser) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));
        return eventRepository.findByHostel_HostelIdOrderByEventDateAsc(hostel.getHostelId())
                .stream().map(EventResponse::from).toList();
    }

    // ── STUDENT: VIEW UPCOMING EVENTS FOR THEIR HOSTEL ────────────────────────
    public List<EventResponse> getUpcomingEventsForStudent(User currentUser) {
        Student student = studentRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found."));

        Admission admission = admissionRepository
                .findByStudent_StudentIdAndIsActiveTrue(student.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("No active admission found."));

        return eventRepository.findByHostel_HostelIdAndEventDateAfterOrderByEventDateAsc(
                        admission.getHostel().getHostelId(), LocalDateTime.now())
                .stream().map(EventResponse::from).toList();
    }
}
