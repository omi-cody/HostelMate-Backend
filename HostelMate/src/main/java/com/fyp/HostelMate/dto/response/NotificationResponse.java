package com.fyp.HostelMate.dto.response;

import com.fyp.HostelMate.entity.Notification;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID notificationId;
    private String notificationType;
    private String title;
    private String message;
    private Boolean isRead;
    private String referenceId;
    private Instant createdAt;

    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId())
                .notificationType(n.getNotificationType().name())
                .title(n.getTitle())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .referenceId(n.getReferenceId())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
