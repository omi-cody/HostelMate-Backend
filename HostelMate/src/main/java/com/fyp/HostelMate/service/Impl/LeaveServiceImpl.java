package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.LeaveRequestDto;
import com.fyp.HostelMate.dto.request.LeaveStatusRequest;
import com.fyp.HostelMate.dto.response.LeaveResponse;
import com.fyp.HostelMate.entity.*;
import com.fyp.HostelMate.entity.enums.LeaveStatus;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveServiceImpl {

    private final LeaveRequestRepository leaveRequestRepository;
    private final AdmissionRepository admissionRepository;
    private final StudentRepository studentRepository;
    private final HostelRepository hostelRepository;
    private final RoomRepository roomRepository;
    private final NotificationService notificationService;

    // ── STUDENT: REQUEST LEAVE ────────────────────────────────────────────────
    @Transactional
    public LeaveResponse requestLeave(User currentUser, LeaveRequestDto req) {
        Student student = studentRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found."));

        Admission admission = admissionRepository
                .findByStudent_StudentIdAndIsActiveTrue(student.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("No active admission found."));

        if (leaveRequestRepository.existsByStudent_StudentIdAndStatus(
                student.getStudentId(), LeaveStatus.PENDING)) {
            throw new BadRequestException("You already have a pending leave request.");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setStudent(student);
        leaveRequest.setHostel(admission.getHostel());
        leaveRequest.setAdmission(admission);
        leaveRequest.setRequestedLeaveDate(req.getRequestedLeaveDate());
        leaveRequest.setReason(req.getReason());
        leaveRequest.setStatus(LeaveStatus.PENDING);

        leaveRequestRepository.save(leaveRequest);

        notificationService.send(
                admission.getHostel().getUser(),
                NotificationType.GENERAL,
                "Leave request submitted",
                student.getFullName() + " has requested to leave on " + req.getRequestedLeaveDate(),
                "leave:" + leaveRequest.getLeaveRequestId()
        );

        log.info("Leave requested: leaveId={} studentId={}", leaveRequest.getLeaveRequestId(), student.getStudentId());
        return LeaveResponse.from(leaveRequest);
    }

    // ── STUDENT: MY LEAVE REQUESTS ────────────────────────────────────────────
    public List<LeaveResponse> getMyLeaveRequests(User currentUser) {
        Student student = studentRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found."));
        return leaveRequestRepository.findByStudent_StudentIdOrderByCreatedAtDesc(student.getStudentId())
                .stream().map(LeaveResponse::from).toList();
    }

    // ── HOSTEL: LIST LEAVE REQUESTS ───────────────────────────────────────────
    public List<LeaveResponse> getHostelLeaveRequests(User currentUser) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));
        return leaveRequestRepository.findByHostel_HostelIdOrderByCreatedAtDesc(hostel.getHostelId())
                .stream().map(LeaveResponse::from).toList();
    }

    // ── HOSTEL: PROCESS LEAVE ─────────────────────────────────────────────────
    @Transactional
    public LeaveResponse processLeave(User currentUser, UUID leaveRequestId, LeaveStatusRequest req) {
        Hostel hostel = hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found."));

        if (!leaveRequest.getHostel().getHostelId().equals(hostel.getHostelId())) {
            throw new BadRequestException("This leave request does not belong to your hostel.");
        }
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Leave request has already been processed.");
        }
        if (req.getStatus() == LeaveStatus.PENDING) {
            throw new BadRequestException("Status must be APPROVED or REJECTED.");
        }

        leaveRequest.setStatus(req.getStatus());
        leaveRequest.setHostelRemarks(req.getHostelRemarks());
        leaveRequest.setProcessedAt(Instant.now());
        leaveRequestRepository.save(leaveRequest);

        // If approved → close the admission and free the room
        if (req.getStatus() == LeaveStatus.APPROVED) {
            closeAdmission(leaveRequest);
        }

        notificationService.send(
                leaveRequest.getStudent().getUser(),
                NotificationType.LEAVE_APPROVED,
                "Leave request " + req.getStatus().name().toLowerCase(),
                hostel.getHostelName() + " has " + req.getStatus().name().toLowerCase() +
                        " your leave request." +
                        (req.getHostelRemarks() != null ? " Note: " + req.getHostelRemarks() : ""),
                "leave:" + leaveRequestId
        );

        log.info("Leave processed: leaveId={} status={}", leaveRequestId, req.getStatus());
        return LeaveResponse.from(leaveRequest);
    }

    // ── HELPER: CLOSE ADMISSION ───────────────────────────────────────────────
    private void closeAdmission(LeaveRequest leaveRequest) {
        Admission admission = leaveRequest.getAdmission();
        admission.setIsActive(false);
        admission.setLeaveDate(leaveRequest.getRequestedLeaveDate() != null
                ? leaveRequest.getRequestedLeaveDate() : LocalDate.now());
        admissionRepository.save(admission);

        // Free up the bed in the room
        Room room = admission.getRoom();
        room.setOccupiedCount(Math.max(0, room.getOccupiedCount() - 1));
        roomRepository.save(room);

        log.info("Admission closed: admissionId={} roomId={}", admission.getAdmissionId(), room.getRoomId());
    }
}
