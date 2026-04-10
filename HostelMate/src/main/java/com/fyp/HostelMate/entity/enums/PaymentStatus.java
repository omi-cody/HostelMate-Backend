package com.fyp.HostelMate.entity.enums;

// Whether the payment has been confirmed
public enum PaymentStatus {
    PENDING,  // Initiated but not yet confirmed (Khalti verification pending)
    PAID,     // Successfully paid and confirmed
    FAILED    // Khalti payment verification failed
}
