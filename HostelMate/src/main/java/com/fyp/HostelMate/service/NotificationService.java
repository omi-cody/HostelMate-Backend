package com.fyp.HostelMate.service;

import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.entity.enums.NotificationType;
import com.fyp.HostelMate.dto.response.NotificationResponse;
import com.fyp.HostelMate.entity.Notification;
import com.fyp.HostelMate.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /** Create and persist a notification for a user. */
    public void send(User recipient, NotificationType type, String title, String message, String referenceId) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setNotificationType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setReferenceId(referenceId);
        n.setIsRead(false);
        notificationRepository.save(n);
    }

    public List<NotificationResponse> getForUser(UUID userId) {
        return notificationRepository.findByRecipient_UserIdOrderByCreatedAtDesc(userId)
                .stream().map(NotificationResponse::from).toList();
    }

    public List<NotificationResponse> getUnreadForUser(UUID userId) {
        return notificationRepository.findByRecipient_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream().map(NotificationResponse::from).toList();
    }

    public long countUnread(UUID userId) {
        return notificationRepository.countByRecipient_UserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.markAllReadForUser(userId);
    }

    @Transactional
    public void markOneRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }
}
