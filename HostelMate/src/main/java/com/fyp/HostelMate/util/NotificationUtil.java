package com.fyp.HostelMate.util;

import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.Notification;
import com.fyp.HostelMate.entity.Student;
import com.fyp.HostelMate.entity.enums.NotificationType;
import com.fyp.HostelMate.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

// Helper that creates and saves notification records to the database.
// Every significant system event (KYC verified, application accepted, fee due, etc.)
// calls one of these methods to create an in-app notification for the user.
@Component
@RequiredArgsConstructor
public class NotificationUtil {

    private final NotificationRepository notificationRepository;

    // Send a notification to a student
    public void notifyStudent(Student student, NotificationType type,
                               String message, String referenceId) {
        Notification notification = new Notification();
        notification.setStudent(student);
        notification.setNotificationType(type);
        notification.setMessage(message);
        notification.setReferenceId(referenceId);
        notification.setRead(false);
        notification.setDeleted(false);
        notification.setCreatedAt(Instant.now());
        notificationRepository.save(notification);
    }

    // Send a notification to a hostel
    public void notifyHostel(Hostel hostel, NotificationType type,
                              String message, String referenceId) {
        Notification notification = new Notification();
        notification.setHostel(hostel);
        notification.setNotificationType(type);
        notification.setMessage(message);
        notification.setReferenceId(referenceId);
        notification.setRead(false);
        notification.setDeleted(false);
        notification.setCreatedAt(Instant.now());
        notificationRepository.save(notification);
    }

    // Convenience overload without referenceId for simple notifications
    public void notifyStudent(Student student, NotificationType type, String message) {
        notifyStudent(student, type, message, null);
    }

    public void notifyHostel(Hostel hostel, NotificationType type, String message) {
        notifyHostel(hostel, type, message, null);
    }
}
