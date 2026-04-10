package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Student's notification feed - newest first, skip soft-deleted ones
    List<Notification> findByStudent_StudentIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID studentId);

    // Hostel notification feed
    List<Notification> findByHostel_HostelIdOrderByCreatedAtDesc(UUID hostelId);

    // Unread count for the notification bell badge icon
    long countByStudent_StudentIdAndIsReadFalseAndIsDeletedFalse(UUID studentId);

    // When a student leaves, we soft-delete their notifications rather than removing them
    // This keeps the data clean without hard-deleting records
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isDeleted = true WHERE n.student.studentId = :studentId")
    void softDeleteAllForStudent(@Param("studentId") UUID studentId);

    // Mark all unread notifications as read (called when student opens the notification panel)
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true " +
           "WHERE n.student.studentId = :studentId AND n.isRead = false AND n.isDeleted = false")
    void markAllAsReadForStudent(@Param("studentId") UUID studentId);
}
