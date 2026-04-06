package com.fyp.HostelMate.scheduler;

import com.fyp.HostelMate.entity.Admission;
import com.fyp.HostelMate.entity.enums.NotificationType;
import com.fyp.HostelMate.repository.AdmissionRepository;
import com.fyp.HostelMate.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReminderScheduler {

    private final AdmissionRepository admissionRepository;
    private final NotificationService notificationService;

    /**
     * Runs every day at 9 AM.
     * Notifies students whose next payment is due today or already overdue.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendPaymentReminders() {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysFromNow = today.plusDays(3);

        // Get all active admissions
        List<Admission> allActive = admissionRepository.findAll()
                .stream()
                .filter(Admission::getIsActive)
                .toList();

        int reminded = 0;
        for (Admission admission : allActive) {
            LocalDate due = admission.getNextPaymentDue();
            if (due == null) continue;

            // Remind if due is today, overdue, or within 3 days
            if (!due.isAfter(threeDaysFromNow)) {
                String status = due.isBefore(today) ? "overdue" : "due on " + due;
                notificationService.send(
                        admission.getStudent().getUser(),
                        NotificationType.PAYMENT_REMINDER,
                        "Monthly fee reminder",
                        "Your monthly fee of Rs " + admission.getMonthlyFee() +
                                " for " + admission.getHostel().getHostelName() +
                                " is " + status + ". Please pay on time.",
                        "admission:" + admission.getAdmissionId()
                );
                reminded++;
            }
        }
        log.info("Payment reminder job complete — {} students notified.", reminded);
    }
}
