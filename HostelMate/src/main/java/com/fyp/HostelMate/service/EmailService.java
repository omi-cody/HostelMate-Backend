package com.fyp.HostelMate.service;

// Defines all email notifications the system can send.
// All methods are async (implemented with @Async) so they don't block the request.
public interface EmailService {

    // OTP email for forgot password flow
    void sendOtp(String toEmail, String otp);

    // Sent to student when admin approves their KYC
    void sendKycVerifiedEmail(String toEmail, String fullName);

    // Sent to student/hostel when KYC is rejected, with reason for rejection
    void sendKycRejectedEmail(String toEmail, String fullName, String remark);

    // Sent to student when their hostel application status changes
    void sendApplicationStatusEmail(String toEmail, String studentName, String hostelName, String status, String remark);

    // Sent to student every month reminding them to pay the fee
    void sendFeeReminderEmail(String toEmail, String studentName, String hostelName,
                              java.math.BigDecimal amount, java.time.LocalDate dueDate);

    // Sent to student when they get a rating/review from a hostel
    void sendReviewReceivedEmail(String toEmail, String name, int rating);

    // Sent to all students of a hostel when an event is added
    void sendEventNotificationEmail(String toEmail, String studentName,
                                    String hostelName, String eventName,
                                    String eventDate, String location);

    // Sent to student when their leave request is accepted or rejected
    void sendLeaveStatusEmail(String toEmail, String studentName, String hostelName,
                              boolean accepted, String remark);
}
