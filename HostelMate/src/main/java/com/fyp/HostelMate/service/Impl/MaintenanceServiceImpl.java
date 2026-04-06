package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.MaintenanceRequestDto;
import com.fyp.HostelMate.dto.request.MaintenanceStatusRequest;
import com.fyp.HostelMate.dto.response.MaintenanceResponse;
import com.fyp.HostelMate.entity.*;
import com.fyp.HostelMate.entity.enums.NotificationType;
import com.fyp.HostelMate.exceptions.BadRequestException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.*;
import com.fyp.HostelMate.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl {

    private final MaintenanceRequestRepository maintenanceRepository;
    private final StudentRepository studentRepository;
    private final HostelRepository hostelRepository;
    private final AdmissionRepository admissionRepository;
    private final NotificationService notificationService;

    // ── STUDENT: SUBMIT COMPLAINT ─────────────────────────────────────────────
    @Transactional
    public MaintenanceResponse submitComplaint(User currentUser, MaintenanceRequestDto req) {
        Student student = studentRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found."));

        Admission admission = admissionRepository
                .findByStudent_StudentIdAndIsActiveTrue(student.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("You must be admitted to a hostel to raise a complaint."));

        MaintenanceRequest request = new MaintenanceRequest();
        request.setStudent(student);
        request.setHostel(admission.getHostel());
        request.setRoom(admission.getRoom());
        request.setComplaintType(req.getComplaintType());
        request.setDescription(req.getDescription());

        maintenanceRepository.save(request);

        notificationService.send(
                admission.getHostel().getUser(),
                NotificationType.NEW_MAINTENANCE_REQUEST,
                "New maintenance request",
                student.getFullName() + " raised a complaint: " + req.getComplaintType(),
                "maintenance:" + request.getRequestId()
        );

        log.info("Complaint submitted: requestId={}", request.getRequestId());
        return MaintenanceResponse.from(request);
    }

    // ── STUDENT: TRACK OWN COMPLAINTS ────────────────────────────────────────
    public List<MaintenanceResponse> getMyComplaints(User currentUser) {
        Student student = studentRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found."));
        return maintenanceRepository.findByStudent_StudentIdOrderByCreatedAtDesc(student.getStudentId())
                .stream().map(MaintenanceResponse::from).toList();
    }

    // ── HOSTEL: LIST ALL REQUESTS ────────────────────────────────────────────
    public List<MaintenanceResponse> getHostelComplaints(User currentUser) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));
        return maintenanceRepository.findByHostel_HostelIdOrderByCreatedAtDesc(hostel.getHostelId())
                .stream().map(MaintenanceResponse::from).toList();
    }

    // ── HOSTEL: UPDATE STATUS ─────────────────────────────────────────────────
    @Transactional
    public MaintenanceResponse updateStatus(User currentUser, UUID requestId, MaintenanceStatusRequest req) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));

        MaintenanceRequest request = maintenanceRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance request not found."));

        if (!request.getHostel().getHostelId().equals(hostel.getHostelId())) {
            throw new BadRequestException("This request does not belong to your hostel.");
        }

        request.setStatus(req.getStatus());
        request.setResolutionNote(req.getResolutionNote());
        request.setUpdatedAt(Instant.now());
        maintenanceRepository.save(request);

        notificationService.send(
                request.getStudent().getUser(),
                NotificationType.MAINTENANCE_STATUS_UPDATED,
                "Complaint status updated",
                "Your complaint '" + request.getComplaintType() + "' is now " + req.getStatus().name(),
                "maintenance:" + requestId
        );

        return MaintenanceResponse.from(request);
    }
}
