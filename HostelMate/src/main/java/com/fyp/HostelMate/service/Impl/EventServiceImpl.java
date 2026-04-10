package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.EventRequest;
import com.fyp.HostelMate.entity.Admission;
import com.fyp.HostelMate.entity.Event;
import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.enums.AdmissionStatus;
import com.fyp.HostelMate.entity.enums.NotificationType;
import com.fyp.HostelMate.exceptions.BusinessException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.AdmissionRepository;
import com.fyp.HostelMate.repository.EventRepository;
import com.fyp.HostelMate.repository.HostelRepository;
import com.fyp.HostelMate.service.EmailService;
import com.fyp.HostelMate.util.NotificationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl {

    private final EventRepository eventRepo;
    private final HostelRepository hostelRepo;
    private final AdmissionRepository admissionRepo;
    private final EmailService emailService;
    private final NotificationUtil notificationUtil;

    //  CREATE EVENT
    @Transactional
    public Event createEvent(String hostelEmail, EventRequest req) {

        Hostel hostel = getHostelByEmail(hostelEmail);

        Event event = new Event();
        event.setHostel(hostel);
        event.setEventName(req.getEventName());
        event.setDetail(req.getDetail());
        event.setLocation(req.getLocation());
        event.setEventDate(req.getEventDate());
        event.setCreatedAt(Instant.now());
        Event saved = eventRepo.save(event);

        // Notify every student currently living at this hostel
        notifyAllAdmittedStudents(hostel, saved);

        log.info("Event '{}' created by hostel {}", req.getEventName(), hostelEmail);
        return saved;
    }

    //  UPDATE EVENT
    @Transactional
    public Event updateEvent(String hostelEmail, UUID eventId, EventRequest req) {

        Hostel hostel = getHostelByEmail(hostelEmail);
        Event event = getEventForHostel(hostel, eventId);

        event.setEventName(req.getEventName());
        event.setDetail(req.getDetail());
        event.setLocation(req.getLocation());
        event.setEventDate(req.getEventDate());
        event.setUpdatedAt(Instant.now());

        log.info("Event {} updated by hostel {}", eventId, hostelEmail);
        return eventRepo.save(event);
    }

    // DELETE EVENT
    @Transactional
    public void deleteEvent(String hostelEmail, UUID eventId) {
        Hostel hostel = getHostelByEmail(hostelEmail);
        Event event = getEventForHostel(hostel, eventId);
        eventRepo.delete(event);
        log.info("Event {} deleted by hostel {}", eventId, hostelEmail);
    }

    // GET EVENTS FOR HOSTEL (hostel management view)
    public List<Event> getHostelEvents(String hostelEmail) {
        Hostel hostel = getHostelByEmail(hostelEmail);
        return eventRepo.findByHostel_HostelIdOrderByEventDateDesc(hostel.getHostelId());
    }

    // GET EVENTS FOR STUDENT (events at their current hostel)
    public List<Event> getEventsForStudent(UUID hostelId) {
        return eventRepo.findByHostel_HostelIdOrderByEventDateDesc(hostelId);
    }

    // PRIVATE HELPERS

    // Send in-app notification and email to every student currently admitted to this hostel
    private void notifyAllAdmittedStudents(Hostel hostel, Event event) {

        List<Admission> activeAdmissions = admissionRepo
                .findByHostel_HostelIdAndStatusOrderByAdmittedDateDesc(
                        hostel.getHostelId(), AdmissionStatus.ACTIVE);

        String eventDateStr = event.getEventDate().toString();

        for (Admission admission : activeAdmissions) {
            var student = admission.getStudent();

            notificationUtil.notifyStudent(student, NotificationType.EVENT_ADDED,
                    "New event at " + hostel.getHostelName() + ": " +
                    event.getEventName() + " on " + eventDateStr,
                    event.getEventId().toString());

            // Email notification runs async so it won't block the loop
            emailService.sendEventNotificationEmail(
                    student.getUser().getEmail(),
                    student.getUser().getFullName(),
                    hostel.getHostelName(),
                    event.getEventName(),
                    eventDateStr,
                    event.getLocation());
        }

        log.info("Notified {} students about event '{}'",
                activeAdmissions.size(), event.getEventName());
    }

    private Hostel getHostelByEmail(String email) {
        return hostelRepo.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found"));
    }

    private Event getEventForHostel(Hostel hostel, UUID eventId) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (!event.getHostel().getHostelId().equals(hostel.getHostelId()))
            throw new BusinessException("This event does not belong to your hostel");
        return event;
    }
}
