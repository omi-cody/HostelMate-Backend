package com.fyp.HostelMate.entity.enums;

// Status the hostel updates as they work on a complaint or maintenance request
public enum RequestStatus {
    PENDING,      // Just submitted by student, hostel hasn't seen it yet
    IN_PROGRESS,  // Hostel is working on it
    RESOLVED      // Issue has been fixed or addressed
}
