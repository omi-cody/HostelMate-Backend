package com.fyp.HostelMate.service;

import com.fyp.HostelMate.entity.Admission;
import com.fyp.HostelMate.entity.enums.AdmissionStatus;
import com.fyp.HostelMate.entity.enums.NotificationType;
import com.fyp.HostelMate.repository.AdmissionRepository;
import com.fyp.HostelMate.util.NotificationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

// Runs automatically on a schedule to remind students about upcoming fee payments.
// Sends an in-app notification + email 3 days before the fee is due each month.
// The due date is the same day of the month as their admission date.
@Component
@Slf4j
@RequiredArgsConstructor
public class FeeReminderScheduler {

    private final AdmissionRepository admissionRepo;
    private final EmailService emailService;
    private final NotificationUtil notificationUtil;

    // Runs every day at 8 AM. Checks which students have a fee due in 3 days.
    // cron = "second minute hour day month weekday"
    @Scheduled(cron = "0 0 8 * * *")
    public void sendMonthlyFeeReminders() {

        List<Admission> activeAdmissions = admissionRepo.findByStatus(AdmissionStatus.ACTIVE);

        LocalDate today = LocalDate.now();
        // We remind students 3 days before their due date
        LocalDate reminderTarget = today.plusDays(3);

        int remindersSent = 0;

        for (Admission admission : activeAdmissions) {

            // The due date is the same calendar day as their admission date, in the current month
            LocalDate dueDate = LocalDate.of(
                    today.getYear(),
                    today.getMonthValue(),
                    Math.min(admission.getAdmittedDate().getDayOfMonth(),
                             today.lengthOfMonth())  // handle months shorter than admission day
            );

            // If the due date has already passed this month, the next one is next month
            if (dueDate.isBefore(today)) {
                dueDate = dueDate.plusMonths(1);
            }

            // Only send reminder if due date is exactly 3 days away
            if (!dueDate.equals(reminderTarget)) continue;

            var student = admission.getStudent();
            var user = student.getUser();

            emailService.sendFeeReminderEmail(
                    user.getEmail(),
                    user.getFullName(),
                    admission.getHostel().getHostelName(),
                    admission.getMonthlyFeeAmount(),
                    dueDate
            );

            notificationUtil.notifyStudent(student, NotificationType.FEE_REMINDER,
                    "Your monthly fee of Rs. " + admission.getMonthlyFeeAmount() +
                    " for " + admission.getHostel().getHostelName() +
                    " is due on " + dueDate.toString() + ". Please pay on time.");

            remindersSent++;
        }

        if (remindersSent > 0)
            log.info("Fee reminders sent to {} students", remindersSent);
    }
}
