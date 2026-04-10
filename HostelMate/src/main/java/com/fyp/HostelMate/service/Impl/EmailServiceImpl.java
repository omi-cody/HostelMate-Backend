package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

// Sends HTML emails for all system events.
// Methods are @Async so they run in a separate thread and don't slow down API responses.
// If email fails, we log the error but don't throw - email failure shouldn't crash the main flow.
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Reusable helper to send any HTML email
    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "HostelMate");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email sent to {}: {}", toEmail, subject);
        } catch (Exception e) {
            // Don't crash the request if email fails - just log it
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    // Wraps any email body in our standard header/footer template
    private String wrapInTemplate(String title, String bodyContent) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #1976D2; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { padding: 30px; background-color: #f9f9f9; border: 1px solid #ddd; }
                    .footer { margin-top: 20px; text-align: center; color: #888; font-size: 12px; }
                    .highlight-box { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 15px 0; border-radius: 4px; }
                    .success-box { background: #d4edda; border-left: 4px solid #28a745; padding: 15px; margin: 15px 0; border-radius: 4px; }
                    .danger-box { background: #f8d7da; border-left: 4px solid #dc3545; padding: 15px; margin: 15px 0; border-radius: 4px; }
                    .otp-box { font-size: 36px; font-weight: bold; text-align: center; background: #fff3cd; padding: 20px; border-radius: 8px; margin: 20px 0; letter-spacing: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header"><h2>%s</h2></div>
                    <div class="content">%s</div>
                    <div class="footer">
                        <p>This is an automated message from HostelMate. Please do not reply.</p>
                        <p>&copy; 2025 HostelMate. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(title, bodyContent);
    }

    @Override
    @Async
    public void sendOtp(String toEmail, String otp) {
        String body = """
            <p>You requested a password reset for your HostelMate account.</p>
            <p>Use the OTP below to reset your password:</p>
            <div class="otp-box">%s</div>
            <div class="highlight-box">
                <strong>This OTP expires in 5 minutes.</strong><br>
                Never share this code with anyone. HostelMate will never ask for your OTP.
            </div>
            <p>If you did not request this, you can safely ignore this email.</p>
            """.formatted(otp);
        sendHtmlEmail(toEmail, "Password Reset OTP - HostelMate", wrapInTemplate("Password Reset", body));
    }

    @Override
    @Async
    public void sendKycVerifiedEmail(String toEmail, String fullName) {
        String body = """
            <p>Hi <strong>%s</strong>,</p>
            <div class="success-box">
                Your KYC has been <strong>verified successfully</strong> by the admin.
            </div>
            <p>You can now log in to HostelMate and:</p>
            <ul>
                <li>Search and apply to hostels</li>
                <li>Track your applications</li>
                <li>Access all student features</li>
            </ul>
            <p>Welcome to HostelMate!</p>
            """.formatted(fullName);
        sendHtmlEmail(toEmail, "KYC Verified - HostelMate", wrapInTemplate("KYC Verified", body));
    }

    @Override
    @Async
    public void sendKycRejectedEmail(String toEmail, String fullName, String remark) {
        String body = """
            <p>Hi <strong>%s</strong>,</p>
            <div class="danger-box">
                Your KYC submission has been <strong>rejected</strong> by the admin.
            </div>
            <p><strong>Reason:</strong> %s</p>
            <p>Please log in to your account, review the remarks, and resubmit your KYC with the correct information.</p>
            """.formatted(fullName, remark);
        sendHtmlEmail(toEmail, "KYC Rejected - Action Required - HostelMate",
                wrapInTemplate("KYC Rejected", body));
    }

    @Override
    @Async
    public void sendApplicationStatusEmail(String toEmail, String studentName,
                                           String hostelName, String status, String remark) {
        String statusBox = status.equals("ADMITTED")
                ? "<div class=\"success-box\">Your application has been <strong>accepted</strong>!</div>"
                : "<div class=\"danger-box\">Your application has been <strong>" + status.toLowerCase() + "</strong>.</div>";

        String remarkSection = (remark != null && !remark.isBlank())
                ? "<p><strong>Note from hostel:</strong> " + remark + "</p>"
                : "";

        String body = """
            <p>Hi <strong>%s</strong>,</p>
            <p>Your application to <strong>%s</strong> has been updated.</p>
            %s
            %s
            <p>Log in to HostelMate to see the full details and next steps.</p>
            """.formatted(studentName, hostelName, statusBox, remarkSection);
        sendHtmlEmail(toEmail, "Application Update - " + hostelName + " - HostelMate",
                wrapInTemplate("Application Update", body));
    }

    @Override
    @Async
    public void sendFeeReminderEmail(String toEmail, String studentName,
                                     String hostelName, BigDecimal amount, LocalDate dueDate) {
        String body = """
            <p>Hi <strong>%s</strong>,</p>
            <p>Your monthly hostel fee for <strong>%s</strong> is due.</p>
            <div class="highlight-box">
                <strong>Amount Due:</strong> Rs. %s<br>
                <strong>Due Date:</strong> %s
            </div>
            <p>Please log in to HostelMate to pay via Khalti or arrange cash payment at the hostel.</p>
            <p>Paying on time helps maintain a good record at your hostel.</p>
            """.formatted(studentName, hostelName, amount.toPlainString(), dueDate.toString());
        sendHtmlEmail(toEmail, "Monthly Fee Reminder - " + hostelName + " - HostelMate",
                wrapInTemplate("Fee Reminder", body));
    }

    @Override
    @Async
    public void sendReviewReceivedEmail(String toEmail, String name, int rating) {
        String stars = "★".repeat(rating) + "☆".repeat(5 - rating);
        String body = """
            <p>Hi <strong>%s</strong>,</p>
            <p>You have received a new rating on HostelMate.</p>
            <div class="highlight-box">
                <strong>Rating:</strong> %s (%d/5)
            </div>
            <p>Log in to view the full review.</p>
            """.formatted(name, stars, rating);
        sendHtmlEmail(toEmail, "New Review Received - HostelMate", wrapInTemplate("New Review", body));
    }

    @Override
    @Async
    public void sendEventNotificationEmail(String toEmail, String studentName,
                                           String hostelName, String eventName,
                                           String eventDate, String location) {
        String body = """
            <p>Hi <strong>%s</strong>,</p>
            <p>Your hostel <strong>%s</strong> has posted a new event:</p>
            <div class="success-box">
                <strong>Event:</strong> %s<br>
                <strong>Date:</strong> %s<br>
                <strong>Location:</strong> %s
            </div>
            <p>Log in to HostelMate to see the full details.</p>
            """.formatted(studentName, hostelName, eventName, eventDate, location);
        sendHtmlEmail(toEmail, "New Event: " + eventName + " - HostelMate",
                wrapInTemplate("New Event", body));
    }

    @Override
    @Async
    public void sendLeaveStatusEmail(String toEmail, String studentName,
                                     String hostelName, boolean accepted, String remark) {
        String statusBox = accepted
                ? "<div class=\"success-box\">Your leave request has been <strong>accepted</strong>.</div>"
                : "<div class=\"danger-box\">Your leave request has been <strong>rejected</strong>.</div>";

        String remarkSection = (remark != null && !remark.isBlank())
                ? "<p><strong>Hostel's note:</strong> " + remark + "</p>"
                : "";

        String nextStep = accepted
                ? "<p>Please log in to complete the process - you'll need to leave a review for the hostel.</p>"
                : "<p>You can contact the hostel directly for more information.</p>";

        String body = """
            <p>Hi <strong>%s</strong>,</p>
            <p>Your leave request from <strong>%s</strong> has been reviewed.</p>
            %s
            %s
            %s
            """.formatted(studentName, hostelName, statusBox, remarkSection, nextStep);
        sendHtmlEmail(toEmail, "Leave Request " + (accepted ? "Accepted" : "Rejected") + " - HostelMate",
                wrapInTemplate("Leave Request Update", body));
    }
}
