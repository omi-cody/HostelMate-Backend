package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.*;
import com.fyp.HostelMate.entity.*;
import com.fyp.HostelMate.entity.enums.*;
import com.fyp.HostelMate.exceptions.BusinessException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.*;
import com.fyp.HostelMate.service.EmailService;
import com.fyp.HostelMate.util.NotificationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl {

    private final ApplicationRepository applicationRepo;
    private final StudentRepository studentRepo;
    private final HostelRepository hostelRepo;
    private final AdmissionRepository admissionRepo;
    private final RoomRepository roomRepo;
    private final HostelKycRepository hostelKycRepo;
    private final PaymentRepository paymentRepo;
    private final EmailService emailService;
    private final NotificationUtil notificationUtil;

    //  STUDENT: APPLY TO HOSTEL 
    @Transactional
    public Application applyToHostel(String studentEmail, UUID hostelId, ApplyRequest req) {

        Student student = getStudentByEmail(studentEmail);
        Hostel hostel = getVerifiedHostel(hostelId);

        // Student must be KYC verified to apply
        if (student.getUser().getVerificationStatus() != VerificationStatus.VERIFIED)
            throw new BusinessException("Your KYC must be verified before applying to a hostel.");

        // Prevent duplicate applications to the same hostel with an active status
        boolean alreadyApplied = applicationRepo
                .existsByStudent_StudentIdAndHostel_HostelIdAndStatusIn(
                        student.getStudentId(), hostelId,
                        List.of(ApplicationStatus.PENDING, ApplicationStatus.ACCEPTED,
                                ApplicationStatus.VISIT_SCHEDULED));
        if (alreadyApplied)
            throw new BusinessException(
                    "You already have an active application to this hostel.");

        Application app = new Application();
        app.setStudent(student);
        app.setHostel(hostel);
        app.setRoomType(req.getRoomType());
        app.setApplicationType(req.getApplicationType());
        app.setStatus(ApplicationStatus.PENDING);
        app.setAppliedAt(Instant.now());
        Application saved = applicationRepo.save(app);

        // Let the hostel know they have a new application
        notificationUtil.notifyHostel(hostel, NotificationType.APPLICATION_RECEIVED,
                student.getUser().getFullName() + " has applied for a " +
                req.getRoomType().name().toLowerCase() + " room.",
                saved.getApplicationId().toString());

        log.info("Application created by {} to hostel {}", studentEmail, hostel.getHostelName());
        return saved;
    }

    //  STUDENT: VIEW MY APPLICATIONS 
    public List<Application> getMyApplications(String studentEmail) {
        Student student = getStudentByEmail(studentEmail);
        return applicationRepo.findByStudent_StudentIdOrderByAppliedAtDesc(student.getStudentId());
    }

    //  HOSTEL: VIEW APPLICATIONS RECEIVED
    public List<Application> getHostelApplications(String hostelEmail) {
        Hostel hostel = getHostelByEmail(hostelEmail);
        return applicationRepo.findByHostel_HostelIdOrderByAppliedAtDesc(hostel.getHostelId());
    }

    //  HOSTEL: ACCEPT APPLICATION + ALLOT ROOM
    @Transactional
    public void acceptApplication(String hostelEmail, UUID applicationId,
                                   AcceptApplicationRequest req) {

        Application app = getApplicationForHostel(hostelEmail, applicationId);

        if (app.getStatus() != ApplicationStatus.PENDING)
            throw new BusinessException("Only pending applications can be accepted.");

        if (app.getApplicationType() == ApplicationType.DIRECT_ADMISSION) {
            // Accept: create Admission(PENDING_PAYMENT) without room yet.
            // Room is allocated by hostel AFTER student pays the admission fee.
            app.setStatus(ApplicationStatus.ACCEPTED);
            app.setUpdatedAt(Instant.now());
            applicationRepo.save(app);
            createAdmissionAndFeePayment(app); // no room yet

        } else {
            // For visit: just accept, room assigned after visit during admitAfterVisit
            app.setStatus(ApplicationStatus.ACCEPTED);
            app.setUpdatedAt(Instant.now());
            applicationRepo.save(app);
        }

        Student student = app.getStudent();
        emailService.sendApplicationStatusEmail(
                student.getUser().getEmail(), student.getUser().getFullName(),
                app.getHostel().getHostelName(), "ACCEPTED", null);
        notificationUtil.notifyStudent(student, NotificationType.APPLICATION_ACCEPTED,
                "Your application to " + app.getHostel().getHostelName() + " was accepted!",
                applicationId.toString());

        log.info("Application {} accepted by hostel", applicationId);
    }

    //  HOSTEL: SCHEDULE VISIT 
    @Transactional
    public void scheduleVisit(String hostelEmail, UUID applicationId, ScheduleVisitRequest req) {

        Application app = getApplicationForHostel(hostelEmail, applicationId);

        if (app.getApplicationType() != ApplicationType.VISIT)
            throw new BusinessException("Only visit applications can have a visit scheduled.");

        if (app.getStatus() != ApplicationStatus.PENDING &&
            app.getStatus() != ApplicationStatus.ACCEPTED)
            throw new BusinessException("Cannot schedule visit at this stage.");

        app.setStatus(ApplicationStatus.VISIT_SCHEDULED);
        app.setVisitScheduledAt(req.getVisitDateTime());
        app.setUpdatedAt(Instant.now());
        applicationRepo.save(app);

        Student student = app.getStudent();
        notificationUtil.notifyStudent(student, NotificationType.VISIT_SCHEDULED,
                "Your visit to " + app.getHostel().getHostelName() + " is scheduled for " +
                req.getVisitDateTime().toString(),
                applicationId.toString());

        log.info("Visit scheduled for application {}", applicationId);
    }

    //  HOSTEL: ADMIT AFTER VISIT
    @Transactional
    public void admitAfterVisit(String hostelEmail, UUID applicationId,
                                 AcceptApplicationRequest req) {

        Application app = getApplicationForHostel(hostelEmail, applicationId);

        if (app.getStatus() != ApplicationStatus.VISIT_SCHEDULED)
            throw new BusinessException("Student must have a completed visit to be admitted.");

        Room room = roomRepo.findById(req.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        List<?> occupants = admissionRepo.findActiveAdmissionsByRoom(room.getRoomId());
        if (occupants.size() >= room.getCapacity())
            throw new BusinessException("Selected room is already at full capacity.");

        // For visit admissions, create admission WITH room (room chosen at visit time)
        createAdmissionAndFeePayment(app);
        // Now assign the room to the just-created admission
        admissionRepo.findByStudent_StudentIdAndStatus(
                app.getStudent().getStudentId(), AdmissionStatus.PENDING_PAYMENT)
            .ifPresent(adm -> {
                // Look up monthly fee for this room type
                hostelKycRepo.findByHostel_HostelId(app.getHostel().getHostelId())
                    .ifPresent(kyc -> {
                        BigDecimal fee = kyc.getRoomPricings().stream()
                            .filter(p -> p.getRoomType() == room.getRoomType())
                            .findFirst().map(p -> p.getMonthlyPrice()).orElse(BigDecimal.ZERO);
                        adm.setRoom(room);
                        adm.setMonthlyFeeAmount(fee);
                        admissionRepo.save(adm);
                    });
            });

        app.setStatus(ApplicationStatus.ADMITTED);
        app.setUpdatedAt(Instant.now());
        applicationRepo.save(app);

        Student student = app.getStudent();
        emailService.sendApplicationStatusEmail(
                student.getUser().getEmail(), student.getUser().getFullName(),
                app.getHostel().getHostelName(), "ADMITTED", null);
        notificationUtil.notifyStudent(student, NotificationType.ADMITTED,
                "You have been admitted to " + app.getHostel().getHostelName() + "!",
                applicationId.toString());

        log.info("Student admitted after visit, application {}", applicationId);
    }

    //  HOSTEL: REJECT / CANCEL APPLICATION 
    @Transactional
    public void rejectApplication(String hostelEmail, UUID applicationId,
                                   RejectApplicationRequest req) {

        Application app = getApplicationForHostel(hostelEmail, applicationId);

        if (app.getStatus() == ApplicationStatus.ADMITTED)
            throw new BusinessException("Cannot reject an already admitted application.");

        app.setStatus(ApplicationStatus.REJECTED);
        app.setRemark(req.getRemark());
        app.setUpdatedAt(Instant.now());
        applicationRepo.save(app);

        Student student = app.getStudent();
        emailService.sendApplicationStatusEmail(
                student.getUser().getEmail(), student.getUser().getFullName(),
                app.getHostel().getHostelName(), "REJECTED", req.getRemark());
        notificationUtil.notifyStudent(student, NotificationType.APPLICATION_REJECTED,
                "Your application to " + app.getHostel().getHostelName() + " was rejected.",
                applicationId.toString());

        log.info("Application {} rejected", applicationId);
    }

    //  PRIVATE HELPERS

    // Creates the admission record and generates the first payment (admission fee)
    private void createAdmissionAndFeePayment(Application app) {
        // Check student doesn't already have active or pending-payment admission elsewhere
        admissionRepo.findByStudent_StudentIdAndStatus(app.getStudent().getStudentId(), AdmissionStatus.ACTIVE)
                .ifPresent(a -> { throw new BusinessException("Student is currently admitted at another hostel."); });
        admissionRepo.findByStudent_StudentIdAndStatus(app.getStudent().getStudentId(), AdmissionStatus.PENDING_PAYMENT)
                .ifPresent(a -> { throw new BusinessException("Student already has a pending admission fee payment."); });

        HostelKyc kyc = hostelKycRepo.findByHostel_HostelId(app.getHostel().getHostelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel KYC not found"));

        // No room assigned yet - hostel will assign after payment
        Admission admission = new Admission();
        admission.setStudent(app.getStudent());
        admission.setHostel(app.getHostel());
        admission.setRoom(null); // Room allocated after payment
        admission.setApplication(app);
        admission.setStatus(AdmissionStatus.PENDING_PAYMENT);
        admission.setAdmittedDate(LocalDate.now());
        admission.setMonthlyFeeAmount(BigDecimal.ZERO); // Set when room is allocated
        admissionRepo.save(admission);

        // Generate pending admission fee payment
        Payment admissionFee = new Payment();
        admissionFee.setStudent(app.getStudent());
        admissionFee.setHostel(app.getHostel());
        admissionFee.setAdmission(admission);
        admissionFee.setAmount(kyc.getAdmissionFee() != null ? kyc.getAdmissionFee() : BigDecimal.ZERO);
        admissionFee.setFeeMonth(LocalDate.now().withDayOfMonth(1)); // normalize to 1st for matching
        admissionFee.setPaymentMethod(PaymentMethod.CASH);
        admissionFee.setStatus(PaymentStatus.PENDING);
        admissionFee.setCreatedAt(Instant.now());
        paymentRepo.save(admissionFee);
    }

    //  HOSTEL: ALLOCATE ROOM AFTER PAYMENT
    @Transactional
    public void allocateRoom(String hostelEmail, UUID admissionId, UUID roomId) {
        Hostel hostel = getHostelByEmail(hostelEmail);
        Admission admission = admissionRepo.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found"));
        if (!admission.getHostel().getHostelId().equals(hostel.getHostelId()))
            throw new BusinessException("This admission does not belong to your hostel.");
        if (admission.getStatus() != AdmissionStatus.ACTIVE)
            throw new BusinessException("Room can only be allocated after admission fee is paid.");

        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        List<?> occupants = admissionRepo.findActiveAdmissionsByRoom(room.getRoomId());
        if (occupants.size() >= room.getCapacity())
            throw new BusinessException("Room is at full capacity.");

        // Get monthly fee for this room type
        HostelKyc kyc = hostelKycRepo.findByHostel_HostelId(hostel.getHostelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel KYC not found"));
        BigDecimal monthlyFee = kyc.getRoomPricings().stream()
                .filter(p -> p.getRoomType() == room.getRoomType())
                .findFirst().map(p -> p.getMonthlyPrice()).orElse(BigDecimal.ZERO);

        admission.setRoom(room);
        admission.setMonthlyFeeAmount(monthlyFee);
        admissionRepo.save(admission);

        notificationUtil.notifyStudent(admission.getStudent(),
                NotificationType.APPLICATION_ACCEPTED,
                "Your room has been allocated at " + hostel.getHostelName() +
                " — Room " + room.getRoomNumber() + " (" + room.getRoomType() + ")",
                admissionId.toString());

        log.info("Room {} allocated to admission {}", roomId, admissionId);
    }

    private Student getStudentByEmail(String email) {
        return studentRepo.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private Hostel getVerifiedHostel(UUID hostelId) {
        Hostel hostel = hostelRepo.findById(hostelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found"));
        if (hostel.getVerificationStatus() != VerificationStatus.VERIFIED)
            throw new BusinessException("This hostel is not verified yet.");
        return hostel;
    }

    private Hostel getHostelByEmail(String email) {
        return hostelRepo.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found"));
    }

    private Application getApplicationForHostel(String hostelEmail, UUID applicationId) {
        Hostel hostel = getHostelByEmail(hostelEmail);
        Application app = applicationRepo.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!app.getHostel().getHostelId().equals(hostel.getHostelId()))
            throw new BusinessException("This application does not belong to your hostel.");
        return app;
    }

    //  HOSTEL: DELETE APPLICATION 
    @Transactional
    public void deleteApplication(String hostelEmail, UUID applicationId) {
        Application app = getApplicationForHostel(hostelEmail, applicationId);
        if (app.getStatus() == ApplicationStatus.ADMITTED)
            throw new BusinessException("Cannot delete an admitted application.");
        app.setStatus(ApplicationStatus.CANCELLED);
        app.setUpdatedAt(Instant.now());
        applicationRepo.save(app);
        log.info("Application {} deleted by hostel", applicationId);
    }

    //  STUDENT: CANCEL APPLICATION
    @Transactional
    public void cancelApplication(String studentEmail, UUID applicationId) {
        Student student = getStudentByEmail(studentEmail);
        Application app = applicationRepo.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!app.getStudent().getStudentId().equals(student.getStudentId()))
            throw new BusinessException("This application does not belong to you.");
        if (app.getStatus() == ApplicationStatus.ADMITTED)
            throw new BusinessException("Already admitted — use leave request instead.");
        if (app.getStatus() == ApplicationStatus.CANCELLED || app.getStatus() == ApplicationStatus.REJECTED)
            throw new BusinessException("Application is already " + app.getStatus().name().toLowerCase() + ".");
        // If there's a PENDING_PAYMENT admission, cancel it too
        admissionRepo.findByApplication_ApplicationId(app.getApplicationId())
            .ifPresent(a -> {
                if (a.getStatus() == AdmissionStatus.PENDING_PAYMENT) {
                    a.setStatus(AdmissionStatus.LEFT);
                    admissionRepo.save(a);
                }
            });
        app.setStatus(ApplicationStatus.CANCELLED);
        app.setUpdatedAt(Instant.now());
        applicationRepo.save(app);
        log.info("Application {} cancelled by student", applicationId);
    }

}
