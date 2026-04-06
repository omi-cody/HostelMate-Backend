package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.ApplicationRequest;
import com.fyp.HostelMate.dto.request.ApplicationStatusRequest;
import com.fyp.HostelMate.dto.response.AdmissionResponse;
import com.fyp.HostelMate.dto.response.ApplicationResponse;
import com.fyp.HostelMate.entity.*;
import com.fyp.HostelMate.entity.enums.*;
import com.fyp.HostelMate.exceptions.BadRequestException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.*;
import com.fyp.HostelMate.service.ApplicationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final AdmissionRepository admissionRepository;
    private final StudentRepository studentRepository;
    private final HostelRepository hostelRepository;
    private final RoomRepository roomRepository;

    // ── STUDENT: APPLY ────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ApplicationResponse apply(User currentUser, ApplicationRequest req) {

        // Student must be verified
        if (currentUser.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new BadRequestException("Your profile must be verified before you can apply.");
        }

        Student student = studentRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));

        // If applying for ADMISSION, student must not already be admitted somewhere
        if (req.getApplicationType() == ApplicationType.ADMISSION &&
                admissionRepository.existsByStudent_StudentIdAndIsActiveTrue(student.getStudentId())) {
            throw new BadRequestException("You are already admitted to a hostel. Leave your current hostel before applying elsewhere.");
        }

        Hostel hostel = hostelRepository.findById(req.getHostelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));

        if (hostel.getUser().getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new BadRequestException("This hostel is not yet verified and not accepting applications.");
        }

        // Prevent duplicate PENDING application to same hostel
        boolean hasPending = applicationRepository
                .existsByStudent_StudentIdAndHostel_HostelIdAndStatusNot(
                        student.getStudentId(), hostel.getHostelId(), ApplicationStatus.CANCELLED);
        if (hasPending) {
            throw new BadRequestException("You already have an active application to this hostel.");
        }

        Application application = new Application();
        application.setStudent(student);
        application.setHostel(hostel);
        application.setApplicationType(req.getApplicationType());
        application.setStatus(ApplicationStatus.PENDING);

        applicationRepository.save(application);
        log.info("Application submitted: studentId={} hostelId={} type={}",
                student.getStudentId(), hostel.getHostelId(), req.getApplicationType());

        return ApplicationResponse.from(application);
    }

    // ── STUDENT: MY APPLICATIONS ──────────────────────────────────────────────
    @Override
    public List<ApplicationResponse> getMyApplications(User currentUser) {
        Student student = studentRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));
        return applicationRepository.findByStudent_StudentId(student.getStudentId())
                .stream().map(ApplicationResponse::from).toList();
    }

    // ── STUDENT: CANCEL APPLICATION ───────────────────────────────────────────
    @Override
    @Transactional
    public void cancelApplication(User currentUser, UUID applicationId) {
        Student student = studentRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found."));

        if (!application.getStudent().getStudentId().equals(student.getStudentId())) {
            throw new BadRequestException("This application does not belong to you.");
        }
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Only PENDING applications can be cancelled.");
        }

        application.setStatus(ApplicationStatus.CANCELLED);
        application.setUpdatedAt(Instant.now());
        applicationRepository.save(application);
    }

    // ── HOSTEL: INCOMING APPLICATIONS ─────────────────────────────────────────
    @Override
    public List<ApplicationResponse> getIncomingApplications(User currentUser) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel profile not found."));
        return applicationRepository.findByHostel_HostelId(hostel.getHostelId())
                .stream().map(ApplicationResponse::from).toList();
    }

    // ── HOSTEL: UPDATE APPLICATION STATUS ─────────────────────────────────────
    @Override
    @Transactional
    public ApplicationResponse updateApplicationStatus(User currentUser, UUID applicationId,
                                                        ApplicationStatusRequest req) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel profile not found."));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found."));

        if (!application.getHostel().getHostelId().equals(hostel.getHostelId())) {
            throw new BadRequestException("This application does not belong to your hostel.");
        }
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Only PENDING applications can be updated.");
        }

        application.setStatus(req.getStatus());
        application.setHostelRemarks(req.getHostelRemarks());
        application.setUpdatedAt(Instant.now());

        // Schedule a visit
        if (req.getStatus() == ApplicationStatus.APPROVED &&
                application.getApplicationType() == ApplicationType.VISIT) {
            if (req.getVisitScheduledAt() == null) {
                throw new BadRequestException("visitScheduledAt is required when approving a VISIT application.");
            }
            application.setVisitScheduledAt(req.getVisitScheduledAt());
        }

        // Approve an admission → create Admission record
        if (req.getStatus() == ApplicationStatus.APPROVED &&
                application.getApplicationType() == ApplicationType.ADMISSION) {
            if (req.getRoomId() == null) {
                throw new BadRequestException("roomId is required when approving an ADMISSION application.");
            }
            createAdmission(application, req.getRoomId());
        }

        applicationRepository.save(application);
        return ApplicationResponse.from(application);
    }

    // ── HELPER: CREATE ADMISSION ──────────────────────────────────────────────
    private void createAdmission(Application application, UUID roomId) {
        Student student = application.getStudent();

        if (admissionRepository.existsByStudent_StudentIdAndIsActiveTrue(student.getStudentId())) {
            throw new BadRequestException("Student is already admitted to a hostel.");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found."));

        if (!room.getHostel().getHostelId().equals(application.getHostel().getHostelId())) {
            throw new BadRequestException("Room does not belong to your hostel.");
        }
        if (room.getAvailableBeds() <= 0) {
            throw new BadRequestException("Selected room has no available beds.");
        }

        // Determine monthly fee from hostel pricing (using room type)
        // For now use admissionFee as a proxy — real pricing comes from HostelPricing (Phase 3+)
        double monthlyFee = application.getHostel().getAdmissionFee() != null
                ? application.getHostel().getAdmissionFee() : 0.0;

        Admission admission = new Admission();
        admission.setStudent(student);
        admission.setHostel(application.getHostel());
        admission.setRoom(room);
        admission.setApplication(application);
        admission.setAdmissionDate(LocalDate.now());
        admission.setMonthlyFee(monthlyFee);
        admission.setNextPaymentDue(LocalDate.now().plusMonths(1));
        admission.setMealPreference(MealPreference.NON_VEG);
        admission.setIsActive(true);

        admissionRepository.save(admission);

        // Update room occupancy
        room.setOccupiedCount(room.getOccupiedCount() + 1);
        roomRepository.save(room);

        // Link room to application
        application.setRoom(room);

        log.info("Admission created: studentId={} hostelId={} roomId={}",
                student.getStudentId(), application.getHostel().getHostelId(), roomId);
    }

    // ── STUDENT: GET MY ADMISSION ─────────────────────────────────────────────
    @Override
    public AdmissionResponse getMyAdmission(User currentUser) {
        Student student = studentRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));

        Admission admission = admissionRepository
                .findByStudent_StudentIdAndIsActiveTrue(student.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("No active admission found."));

        return AdmissionResponse.from(admission);
    }

    // ── HOSTEL: LIST ADMITTED STUDENTS ────────────────────────────────────────
    @Override
    public List<AdmissionResponse> getHostelAdmissions(User currentUser) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel profile not found."));

        return admissionRepository.findByHostel_HostelIdAndIsActiveTrue(hostel.getHostelId())
                .stream().map(AdmissionResponse::from).toList();
    }

    // ── STUDENT: UPDATE MEAL PREFERENCE ──────────────────────────────────────
    @Override
    @Transactional
    public AdmissionResponse updateMealPreference(User currentUser, MealPreference preference) {
        Student student = studentRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));

        Admission admission = admissionRepository
                .findByStudent_StudentIdAndIsActiveTrue(student.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("No active admission found."));

        admission.setMealPreference(preference);
        admissionRepository.save(admission);
        return AdmissionResponse.from(admission);
    }
}
