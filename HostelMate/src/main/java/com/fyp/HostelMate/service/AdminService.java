package com.fyp.HostelMate.service;

import com.fyp.HostelMate.dto.response.HostelProfileResponse;
import com.fyp.HostelMate.dto.response.StudentProfileResponse;

import java.util.List;
import java.util.UUID;

public interface AdminService {
    // Students
    List<StudentProfileResponse> getAllStudents();
    StudentProfileResponse getStudent(UUID studentId);
    void verifyStudent(UUID studentId);
    void rejectStudent(UUID studentId);

    // Hostels
    List<HostelProfileResponse> getAllHostels();
    HostelProfileResponse getHostel(UUID hostelId);
    void verifyHostel(UUID hostelId);
    void rejectHostel(UUID hostelId);
}
