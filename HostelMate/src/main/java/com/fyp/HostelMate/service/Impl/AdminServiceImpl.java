package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.response.HostelProfileResponse;
import com.fyp.HostelMate.dto.response.StudentProfileResponse;
import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.Student;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.HostelRepository;
import com.fyp.HostelMate.repository.StudentRepository;
import com.fyp.HostelMate.repository.UserRepository;
import com.fyp.HostelMate.service.AdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final StudentRepository studentRepository;
    private final HostelRepository hostelRepository;
    private final UserRepository userRepository;

    // ── STUDENTS ──────────────────────────────────────────────────────────────

    @Override
    public List<StudentProfileResponse> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(StudentProfileResponse::from)
                .toList();
    }

    @Override
    public StudentProfileResponse getStudent(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found."));
        return StudentProfileResponse.from(student);
    }

    @Override
    @Transactional
    public void verifyStudent(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found."));
        student.getUser().setVerificationStatus(VerificationStatus.VERIFIED);
        userRepository.save(student.getUser());
        log.info("Student verified: studentId={}", studentId);
    }

    @Override
    @Transactional
    public void rejectStudent(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found."));
        student.getUser().setVerificationStatus(VerificationStatus.REJECTED);
        userRepository.save(student.getUser());
        log.info("Student rejected: studentId={}", studentId);
    }

    // ── HOSTELS ───────────────────────────────────────────────────────────────

    @Override
    public List<HostelProfileResponse> getAllHostels() {
        return hostelRepository.findAll()
                .stream()
                .map(HostelProfileResponse::from)
                .toList();
    }

    @Override
    public HostelProfileResponse getHostel(UUID hostelId) {
        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));
        return HostelProfileResponse.from(hostel);
    }

    @Override
    @Transactional
    public void verifyHostel(UUID hostelId) {
        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));
        hostel.getUser().setVerificationStatus(VerificationStatus.VERIFIED);
        userRepository.save(hostel.getUser());
        log.info("Hostel verified: hostelId={}", hostelId);
    }

    @Override
    @Transactional
    public void rejectHostel(UUID hostelId) {
        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found."));
        hostel.getUser().setVerificationStatus(VerificationStatus.REJECTED);
        userRepository.save(hostel.getUser());
        log.info("Hostel rejected: hostelId={}", hostelId);
    }
}
