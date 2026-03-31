package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.ForgotPasswordRequest;
import com.fyp.HostelMate.security.jwt.JwtService;
import com.fyp.HostelMate.dto.response.AuthResponse;
import com.fyp.HostelMate.dto.request.HostelRegistrationRequest;
import com.fyp.HostelMate.dto.request.LoginRequest;
import com.fyp.HostelMate.dto.request.StudentRegistrationRequest;
import com.fyp.HostelMate.entity.*;
import com.fyp.HostelMate.entity.enums.AccountStatus;
import com.fyp.HostelMate.entity.enums.UserRole;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import com.fyp.HostelMate.exceptions.EmailAlreadyExistsException;
import com.fyp.HostelMate.service.AuthService;
import com.fyp.HostelMate.service.EmailService;
import com.fyp.HostelMate.service.OtpService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import  com.fyp.HostelMate.repository.*;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final HostelRepository hostelRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final EmailService emailService;

    @Autowired
    public AuthServiceImpl(UserRepository u, StudentRepository s,
                           HostelRepository h, PasswordEncoder e, JwtService j,
                           OtpService o, EmailService em) {
        this.userRepo = u;
        this.studentRepo = s;
        this.hostelRepo = h;
        this.encoder = e;
        this.jwtService = j;
        this.otpService = o;
        this.emailService = em;
    }

    // ---------------- STUDENT REGISTER ----------------
    @Override
    @Transactional
    public void registerStudent(StudentRegistrationRequest req) {

            if (userRepo.existsByEmail(req.getEmail()))
                throw new EmailAlreadyExistsException("Email already exists");

            User user = new User();
            user.setFullName(req.getFullName());
            user.setEmail(req.getEmail());
            user.setPhone(req.getPhone());
            user.setRole(UserRole.STUDENT);
            user.setPasswordHash(encoder.encode(req.getPassword()));
            user.setStatus(AccountStatus.ACTIVE);
            user.setVerificationStatus(VerificationStatus.PENDING);
            user.setCreatedAt(Instant.now());

            userRepo.save(user);

            Student student = new Student();
            student.setUser(user);
            student.setGender(req.getGender());
            student.setCreatedAt(Instant.now());

            studentRepo.save(student);
    }

    // ---------------- HOSTEL REGISTER ----------------
    @Override
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
            user.setStatus(AccountStatus.ACTIVE);
            user.setVerificationStatus(VerificationStatus.PENDING);
            user.setCreatedAt(Instant.now());

            User savedUser = userRepo.save(user);

            // Create and save Hostel
            Hostel hostel = new Hostel();
            hostel.setUser(savedUser);
            hostel.setHostelName(req.getHostelName());
            hostel.setHostelType(req.getHostelType());
            hostel.setOwnerName(req.getOwnerName());
            hostel.setCreatedAt(Instant.now());

            hostelRepo.save(hostel);
            log.info("Hostel registration completed successfully");
    }

    // ---------------- LOGIN (COMMON) ----------------
    @Override
    @Transactional
    public AuthResponse login(LoginRequest req) {
        try {

            User user = userRepo.findByEmail(req.getEmail().toLowerCase())
                    .orElseThrow(() -> new RuntimeException("Invalid Email credentials"));

            if (!encoder.matches(req.getPassword(), user.getPasswordHash()))
                throw new RuntimeException("Invalid Password credentials");

            String token = jwtService.generateToken(user);
            String role = user.getRole().toString();
            String id = user.getUserId().toString();

            boolean requireKyc = false;
            if( user.getRole() == UserRole.STUDENT) {
                requireKyc = user.getVerificationStatus() == VerificationStatus.VERIFIED;
            }else if( user.getRole() == UserRole.HOSTEL) {
                requireKyc = user.getVerificationStatus() == VerificationStatus.VERIFIED;
            }

            return AuthResponse.builder()
                    .token(token)
                    .role(role)
                    .id(id)
                    .fullName(user.getFullName())
                    .kycVerified(requireKyc)
                    .email(user.getEmail())
                    .build();
        }catch (Exception e) {
            throw new RuntimeException("Error in login: " + e.getMessage());
        }
    }

    // ---------------- FORGOT PASSWORD ----------------
    @Override
    public ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) {
        User user = userRepo.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found with this email"));
                
        String otp = otpService.generateOtp(user.getEmail());
        emailService.sendOtp(user.getEmail(), otp);
        
        return ResponseEntity.ok(Map.of("message", "OTP sent to email"));
    }

    // ---------------- VERIFY OTP ----------------
    @Override
    public ResponseEntity<?> verifyOtp(com.fyp.HostelMate.dto.request.VerifyOtpRequest request) {
        boolean isValid = otpService.checkOtpWithoutEvict(request.getEmail().toLowerCase(), request.getOtp());
        if (!isValid) {
            throw new RuntimeException("Invalid or expired OTP");
        }
        return ResponseEntity.ok(Map.of("message", "OTP verified successfully"));
    }

    // ---------------- RESET PASSWORD ----------------
    @Override
    @Transactional
    public ResponseEntity<?> resetPasswordWithOtp(com.fyp.HostelMate.dto.request.ResetPasswordWithOtpRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        

        
        User user = userRepo.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        user.setPasswordHash(encoder.encode(request.getNewPassword()));
        userRepo.save(user);
        
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

}
