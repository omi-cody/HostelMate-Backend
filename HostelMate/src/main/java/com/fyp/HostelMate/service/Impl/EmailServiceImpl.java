package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final String fromName = "HostelMate";
    
    // The logo primary color
    private final String primaryColor = "#4dc3f7"; 

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendOtp(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String emailContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: %s; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background-color: #f9f9f9; }
                    .otp-box { 
                        font-size: 32px; 
                        font-weight: bold; 
                        text-align: center; 
                        background-color: #e0f7fa; 
                        padding: 20px; 
                        border-radius: 10px; 
                        margin: 20px 0; 
                        letter-spacing: 10px;
                        color: #000;
                    }
                    .footer { margin-top: 20px; text-align: center; color: #666; font-size: 12px; }
                    .warning { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; 
                              border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>HostelMate</h1>
                        <h3>Password Reset OTP</h3>
                    </div>
                    <div class="content">
                        <h2>Your One-Time Password</h2>
                        <p>Use the OTP below to reset your password:</p>
                        
                        <div class="otp-box">
                            %s
                        </div>
                        
                        <p>This OTP is valid for <strong>5 minutes</strong>.</p>
                        
                        <div class="warning">
                            <p><strong>Security Notice:</strong></p>
                            <ul>
                                <li>Never share this OTP with anyone</li>
                                <li>HostelMate will never ask for your OTP</li>
                                <li>If you didn't request this, please ignore this email</li>
                            </ul>
                        </div>
                    </div>
                    <div class="footer">
                        <p>This is an automated message, please do not reply to this email.</p>
                        <p>&copy; 2024 HostelMate. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(primaryColor, otp);

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Your Password Reset OTP - HostelMate");
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(String toEmail, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String emailContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: %s; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background-color: #f9f9f9; }
                    .title { font-size: 24px; font-weight: bold; margin-bottom: 20px; color: #333; }
                    .footer { margin-top: 20px; text-align: center; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>HostelMate</h1>
                        <p>Home away from Home</p>
                    </div>
                    <div class="content">
                        <div class="title">Welcome to HostelMate, %s!</div>
                        <p>We are thrilled to have you on board. HostelMate is designed to make your stay and management as smooth as taking a walk in the park.</p>
                        <p>You can now log in to your dashboard to view your profile, manage applications, and keep track of your hostel activities.</p>
                        <br/>
                        <p>We hope you feel right at home with us!</p>
                        <p>Best Regards,</p>
                        <p><strong>The HostelMate Team</strong></p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message, please do not reply to this email.</p>
                        <p>&copy; 2024 HostelMate. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(primaryColor, userName);

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to HostelMate - Home away from Home");
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send Welcome email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send Welcome email");
        }
    }

    @Override
    @Async
    public void sendStatusUpdateEmail(String toEmail, String subject, String messageContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String emailContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: %s; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background-color: #f9f9f9; }
                    .message-box { 
                        background-color: #fff; 
                        padding: 20px; 
                        border-left: 4px solid %s; 
                        margin: 20px 0; 
                    }
                    .footer { margin-top: 20px; text-align: center; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>HostelMate Update</h1>
                    </div>
                    <div class="content">
                        <h2>Hello!</h2>
                        <p>There is an update regarding your account or application.</p>
                        
                        <div class="message-box">
                            %s
                        </div>
                        
                        <p>If you have any questions, please contact your hostel admin.</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message, please do not reply to this email.</p>
                        <p>&copy; 2024 HostelMate. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(primaryColor, primaryColor, messageContent);

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Status update email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send Status update email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send Status update email");
        }
    }
}
