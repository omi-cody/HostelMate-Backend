package com.fyp.HostelMate.service;

import com.fyp.HostelMate.config.JwtService;
import com.fyp.HostelMate.dto.AuthResponse;
import com.fyp.HostelMate.dto.HostelRegistrationRequest;
import com.fyp.HostelMate.dto.LoginRequest;
import com.fyp.HostelMate.dto.StudentRegistrationRequest;
import com.fyp.HostelMate.entity.*;
import com.fyp.HostelMate.enums.*;
import com.fyp.HostelMate.exceptions.EmailAlreadyExistsException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import  com.fyp.HostelMate.repository.*;

import java.time.Instant;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final HostelRepository hostelRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    @Autowired
    public AuthService(UserRepository u, StudentRepository s,
                       HostelRepository h, PasswordEncoder e, JwtService j) {
        this.userRepo = u;
        this.studentRepo = s;
        this.hostelRepo = h;
        this.encoder = e;
        this.jwtService = j;
    }

    // ---------------- STUDENT REGISTER ----------------
    public void registerStudent(StudentRegistrationRequest req) {

            if (userRepo.existsByEmail(req.getEmail()))
                throw new EmailAlreadyExistsException("Email already exists");

            User user = new User();
            user.setFullName(req.getFullName());
            user.setEmail(req.getEmail());
            user.setPhone(req.getPhone());
            user.setRole(UserRole.STUDENT);
            user.setPasswordHash(encoder.encode(req.getPassword()));
            user.setStatus(AccountStatus.PENDING);
            user.setCreatedAt(Instant.now());

            userRepo.save(user);

            Student student = new Student();
            student.setUser(user);
            student.setGender(req.getGender());
            student.setCreatedAt(Instant.now());

            studentRepo.save(student);



    }

    // ---------------- HOSTEL REGISTER ----------------
    @Transactional
    public void registerHostel(HostelRegistrationRequest req) {

            // Check if email already exists
            if (userRepo.existsByEmail(req.getEmail())) {
                throw new EmailAlreadyExistsException("Email already exists");
            }


            // Create and save User
            User user = new User();
            user.setFullName(req.getOwnerName());
            user.setEmail(req.getEmail());
            user.setPhone(req.getPhone());
            user.setRole(UserRole.HOSTEL);
            user.setPasswordHash(encoder.encode(req.getPassword()));
            user.setStatus(AccountStatus.PENDING);
            user.setCreatedAt(Instant.now());

            User savedUser = userRepo.save(user);

            // Create and save Hostel
            Hostel hostel = new Hostel();
            hostel.setUser(savedUser);
            hostel.setHostelName(req.getHostelName());
            hostel.setHostelType(req.getHostelType());
            hostel.setOwnerName(req.getOwnerName());
            hostel.setVerificationStatus(VerificationStatus.PENDING);
            hostel.setCreatedAt(Instant.now());

            hostelRepo.save(hostel);
            log.info("Hostel registration completed successfully");


    }

    // ---------------- LOGIN (COMMON) ----------------
    @Transactional
    public AuthResponse login(LoginRequest req) {
        try {

            User user = userRepo.findByEmail(req.getEmail().toLowerCase())
                    .orElseThrow(() -> new RuntimeException("Invalid Email credentials"));

            if (!encoder.matches(req.getPassword(), user.getPasswordHash()))
                throw new RuntimeException("Invalid Password credentials");

            String token = jwtService.generateToken(user);
            String role = user.getRole().toString();
            return new AuthResponse(token, role);
        }catch (Exception e) {
            throw new RuntimeException("Error in login: " + e.getMessage());
        }
    }
}
