package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.response.ApiResponse;
import com.fyp.HostelMate.entity.Admission;
import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.enums.AdmissionStatus;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.AdmissionRepository;
import com.fyp.HostelMate.repository.HostelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/hostel")
@RequiredArgsConstructor
@PreAuthorize("hasRole('HOSTEL')")
public class HostelStudentsController {

    private final HostelRepository hostelRepo;
    private final AdmissionRepository admissionRepo;

    // Active students currently living at the hostel
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<Object>> getAdmittedStudents(Authentication auth) {
        Hostel hostel = hostelRepo.findByUser_Email(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found"));

        // Include PENDING_PAYMENT (awaiting admission fee) + ACTIVE students
        List<Admission> admissions = new ArrayList<>();
        admissions.addAll(admissionRepo.findByHostel_HostelIdAndStatusOrderByAdmittedDateDesc(
                hostel.getHostelId(), AdmissionStatus.PENDING_PAYMENT));
        admissions.addAll(admissionRepo.findByHostel_HostelIdAndStatusOrderByAdmittedDateDesc(
                hostel.getHostelId(), AdmissionStatus.ACTIVE));

        return ResponseEntity.ok(ApiResponse.success("Admitted students", admissions));
    }

    // All admissions including leave-requested and left students (for history + leave management)
    @GetMapping("/admissions")
    public ResponseEntity<ApiResponse<Object>> getAllAdmissions(Authentication auth) {
        Hostel hostel = hostelRepo.findByUser_Email(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found"));

        // Combine active + leave-requested + left admissions for this hostel
        List<Admission> all = new ArrayList<>();
        for (AdmissionStatus status : List.of(
                AdmissionStatus.PENDING_PAYMENT,
                AdmissionStatus.ACTIVE,
                AdmissionStatus.LEAVE_REQUESTED,
                AdmissionStatus.LEFT)) {
            all.addAll(admissionRepo
                    .findByHostel_HostelIdAndStatusOrderByAdmittedDateDesc(
                            hostel.getHostelId(), status));
        }

        return ResponseEntity.ok(ApiResponse.success("All admissions", all));
    }
}
