package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.ComplaintCreateRequest;
import com.fyp.HostelMate.dto.request.UpdateComplaintStatusRequest;
import com.fyp.HostelMate.entity.*;
import com.fyp.HostelMate.entity.enums.*;
import com.fyp.HostelMate.exceptions.BusinessException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.*;
import com.fyp.HostelMate.util.NotificationUtil;
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
public class ComplaintServiceImpl {

    private final ComplaintRequestRepository complaintRepo;
    private final StudentRepository studentRepo;
    private final HostelRepository hostelRepo;
    private final AdmissionRepository admissionRepo;
    private final NotificationUtil notificationUtil;

    //  STUDENT: SUBMIT COMPLAINT OR MAINTENANCE REQUEST
    @Transactional
    public ComplaintRequest submitRequest(String studentEmail, ComplaintCreateRequest req) {

        Student student = getStudentByEmail(studentEmail);

        // Only admitted students can submit requests
        Admission admission = admissionRepo.findByStudent_StudentIdAndStatus(
                        student.getStudentId(), AdmissionStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "You must be admitted to a hostel to submit a request."));

        ComplaintRequest complaint = new ComplaintRequest();
        complaint.setStudent(student);
        complaint.setHostel(admission.getHostel());
        complaint.setRequestType(req.getRequestType());
        complaint.setTitle(req.getTitle());
        complaint.setDescription(req.getDescription());
        complaint.setStatus(RequestStatus.PENDING);
        complaint.setCreatedAt(Instant.now());
        ComplaintRequest saved = complaintRepo.save(complaint);

        // Notify hostel so they know to check the request queue
        notificationUtil.notifyHostel(admission.getHostel(), NotificationType.COMPLAINT_UPDATE,
                student.getUser().getFullName() + " submitted a new " +
                req.getRequestType().name().toLowerCase() + ": " + req.getTitle(),
                saved.getRequestId().toString());

        log.info("Complaint submitted by {} type {}", studentEmail, req.getRequestType());
        return saved;
    }

    // STUDENT: VIEW MY REQUESTS
    public List<ComplaintRequest> getMyRequests(String studentEmail) {
        Student student = getStudentByEmail(studentEmail);
        return complaintRepo.findByStudent_StudentIdOrderByCreatedAtDesc(student.getStudentId());
    }

    // HOSTEL: VIEW ALL REQUESTS
    public List<ComplaintRequest> getHostelRequests(String hostelEmail) {
        Hostel hostel = getHostelByEmail(hostelEmail);
        return complaintRepo.findByHostel_HostelIdOrderByCreatedAtDesc(hostel.getHostelId());
    }

    // HOSTEL: UPDATE REQUEST STATUS
    @Transactional
    public void updateStatus(String hostelEmail, UUID requestId, UpdateComplaintStatusRequest req) {

        Hostel hostel = getHostelByEmail(hostelEmail);
        ComplaintRequest complaint = complaintRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (!complaint.getHostel().getHostelId().equals(hostel.getHostelId()))
            throw new BusinessException("This request does not belong to your hostel.");

        complaint.setStatus(req.getStatus());
        complaint.setHostelResponse(req.getHostelResponse());
        complaint.setUpdatedAt(Instant.now());
        complaintRepo.save(complaint);

        // Let the student know their request status was updated
        notificationUtil.notifyStudent(complaint.getStudent(), NotificationType.COMPLAINT_UPDATE,
                "Your " + complaint.getRequestType().name().toLowerCase() +
                " '" + complaint.getTitle() + "' is now " + req.getStatus().name(),
                requestId.toString());

        log.info("Complaint {} updated to {} by hostel", requestId, req.getStatus());
    }

    private Student getStudentByEmail(String email) {
        return studentRepo.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private Hostel getHostelByEmail(String email) {
        return hostelRepo.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found"));
    }
}
