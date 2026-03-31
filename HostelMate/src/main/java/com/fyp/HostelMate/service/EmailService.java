package com.fyp.HostelMate.service;

public interface EmailService {

    void sendOtp(String email, String otp);
    
    void sendWelcomeEmail(String toEmail, String userName);
    
    void sendStatusUpdateEmail(String toEmail, String subject, String messageContent);
}
