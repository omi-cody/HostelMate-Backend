package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.response.ApiResponse;
import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.Student;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.HostelRepository;
import com.fyp.HostelMate.repository.NotificationRepository;
import com.fyp.HostelMate.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepo;
    private final StudentRepository studentRepo;
    private final HostelRepository hostelRepo;

    //  STUDENT NOTIFICATIONS

    // Get all non-deleted notifications for this student, newest first
    @GetMapping("/student/notifications")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Object>> getStudentNotifications(Authentication auth) {
        Student student = getStudentByEmail(auth.getName());
        var list = notificationRepo.findByStudent_StudentIdAndIsDeletedFalseOrderByCreatedAtDesc(
                student.getStudentId());
        return ResponseEntity.ok(ApiResponse.success("Notifications", list));
    }

    // Returns unread count for the notification bell badge
    @GetMapping("/student/notifications/unread-count")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Object>> getUnreadCount(Authentication auth) {
        Student student = getStudentByEmail(auth.getName());
        long count = notificationRepo.countByStudent_StudentIdAndIsReadFalseAndIsDeletedFalse(
                student.getStudentId());
        return ResponseEntity.ok(ApiResponse.success("Unread count",
                Map.of("unreadCount", count)));
    }

    // Mark all notifications as read when student opens the notification panel
    @PostMapping("/student/notifications/mark-all-read")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> markAllRead(Authentication auth) {
        Student student = getStudentByEmail(auth.getName());
        notificationRepo.markAllAsReadForStudent(student.getStudentId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }

    //  HOSTEL NOTIFICATIONS

    @GetMapping("/hostel/notifications")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Object>> getHostelNotifications(Authentication auth) {
        Hostel hostel = getHostelByEmail(auth.getName());
        var list = notificationRepo.findByHostel_HostelIdOrderByCreatedAtDesc(
                hostel.getHostelId());
        return ResponseEntity.ok(ApiResponse.success("Notifications", list));
    }

    // PRIVATE HELPERS

    private Student getStudentByEmail(String email) {
        return studentRepo.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private Hostel getHostelByEmail(String email) {
        return hostelRepo.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found"));
    }
}
