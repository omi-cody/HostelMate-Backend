package com.fyp.HostelMate.entity.enums;

// Short label to categorize notifications so the frontend can show the right icon
public enum NotificationType {
    KYC_VERIFIED,         // Admin approved KYC
    KYC_REJECTED,         // Admin rejected KYC
    APPLICATION_RECEIVED, // Hostel got a new student application
    APPLICATION_ACCEPTED, // Student's application was accepted
    APPLICATION_REJECTED, // Student's application was rejected
    VISIT_SCHEDULED,      // Hostel set a visit date
    ADMITTED,             // Student officially admitted
    LEAVE_REQUESTED,      // Student requested to leave
    LEAVE_ACCEPTED,       // Hostel accepted leave
    LEAVE_REJECTED,       // Hostel rejected leave
    FEE_REMINDER,         // Monthly fee is due
    EVENT_ADDED,          // Hostel posted a new event
    COMPLAINT_UPDATE,     // Status of complaint/maintenance changed
    REVIEW_RECEIVED,      // Got a new rating/review
    PAYMENT_CONFIRMED     // Fee payment confirmed
}
