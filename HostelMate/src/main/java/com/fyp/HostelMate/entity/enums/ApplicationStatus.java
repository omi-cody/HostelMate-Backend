package com.fyp.HostelMate.entity.enums;

// Tracks where an application is in its lifecycle
public enum ApplicationStatus {
    PENDING,           // Just submitted, hostel hasn't acted yet
    ACCEPTED,          // Hostel accepted - for direct admission, awaiting fee payment
    VISIT_SCHEDULED,   // Hostel scheduled a visit date
    ADMITTED,          // Student is now admitted
    REJECTED,          // Hostel rejected the application
    CANCELLED          // Visit happened but hostel or student cancelled
}
