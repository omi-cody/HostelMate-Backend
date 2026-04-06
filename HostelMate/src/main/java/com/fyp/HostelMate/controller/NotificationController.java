package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.response.NotificationResponse;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** GET /api/notifications — all notifications for current user */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAll(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(notificationService.getForUser(currentUser.getUserId()));
    }

    /** GET /api/notifications/unread — unread only */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnread(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(notificationService.getUnreadForUser(currentUser.getUserId()));
    }

    /** GET /api/notifications/unread/count — badge count for UI */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> countUnread(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread(currentUser.getUserId())));
    }

    /** PATCH /api/notifications/read-all — mark all as read */
    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllRead(
            @AuthenticationPrincipal User currentUser) {
        notificationService.markAllRead(currentUser.getUserId());
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read."));
    }

    /** PATCH /api/notifications/{id}/read — mark one as read */
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markOneRead(@PathVariable UUID id) {
        notificationService.markOneRead(id);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read."));
    }
}
