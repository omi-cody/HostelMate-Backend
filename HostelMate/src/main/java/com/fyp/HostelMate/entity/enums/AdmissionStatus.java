package com.fyp.HostelMate.entity.enums;

public enum AdmissionStatus {
    PENDING_PAYMENT,   // Hostel accepted but student hasn't paid admission fee yet
    ACTIVE,            // Student is currently living at the hostel (fee paid)
    LEAVE_REQUESTED,   // Student requested to leave
    LEFT               // Student has left the hostel
}
