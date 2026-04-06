package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.response.AdminDashboardResponse;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import com.fyp.HostelMate.repository.HostelRepository;
import com.fyp.HostelMate.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl {

    private final StudentRepository studentRepository;
    private final HostelRepository hostelRepository;

    public AdminDashboardResponse getDashboard() {
        long totalStudents = studentRepository.count();
        long totalHostels  = hostelRepository.count();

        long pendingStudents = studentRepository.findByUser_VerificationStatus(VerificationStatus.PENDING).size();
        long pendingHostels  = hostelRepository.findByUser_VerificationStatus(VerificationStatus.PENDING).size();
        long verifiedStudents = studentRepository.findByUser_VerificationStatus(VerificationStatus.VERIFIED).size();
        long verifiedHostels  = hostelRepository.findByUser_VerificationStatus(VerificationStatus.VERIFIED).size();

        double studentRate = totalStudents > 0 ? (verifiedStudents * 100.0 / totalStudents) : 0;
        double hostelRate  = totalHostels  > 0 ? (verifiedHostels  * 100.0 / totalHostels)  : 0;

        // Total capacity = sum of all rooms' capacities for verified hostels
        long totalCapacity = hostelRepository.findByUser_VerificationStatus(VerificationStatus.VERIFIED)
                .stream()
                .mapToLong(h -> {
                    try { return Long.parseLong(h.getTotalRoom()); } catch (Exception e) { return 0; }
                })
                .sum();

        return AdminDashboardResponse.builder()
                .totalStudents(totalStudents)
                .totalHostels(totalHostels)
                .pendingStudentVerifications(pendingStudents)
                .pendingHostelVerifications(pendingHostels)
                .verifiedStudents(verifiedStudents)
                .verifiedHostels(verifiedHostels)
                .activeHostels(verifiedHostels)
                .totalCapacity(totalCapacity)
                .studentVerificationRate(Math.round(studentRate * 10.0) / 10.0)
                .hostelVerificationRate(Math.round(hostelRate * 10.0) / 10.0)
                .studentGrowth(List.of()) // extend with @Query grouped by month when needed
                .hostelGrowth(List.of())
                .build();
    }
}
