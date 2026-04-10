package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.*;
import com.fyp.HostelMate.dto.response.AuthResponse;
import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.Student;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.entity.enums.AccountStatus;
import com.fyp.HostelMate.entity.enums.UserRole;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import com.fyp.HostelMate.exceptions.BusinessException;
import com.fyp.HostelMate.exceptions.EmailAlreadyExistsException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.HostelRepository;
import com.fyp.HostelMate.repository.StudentRepository;
import com.fyp.HostelMate.repository.UserRepository;
import com.fyp.HostelMate.security.jwt.JwtService;
import com.fyp.HostelMate.service.AuthService;
import com.fyp.HostelMate.service.EmailService;
import com.fyp.HostelMate.service.OtpService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final HostelRepository hostelRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final EmailService emailService;

    // ── STUDENT REGISTRATION ───────────────────────────────────────────────
    @Override
    @Transactional
    public void registerStudent(StudentRegistrationRequest req) {

        if (userRepo.existsByEmail(req.getEmail()))
            throw new EmailAlreadyExistsException("An account with this email already exists");

        if (userRepo.existsByPhone(req.getPhone()))
            throw new BusinessException("An account with this phone number already exists");

        // Create the shared user account first
        User user = new User();
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail().toLowerCase().trim());
        user.setPhone(req.getPhone());
        user.setRole(UserRole.STUDENT);
        user.setPasswordHash(encoder.encode(req.getPassword()));
        user.setStatus(AccountStatus.ACTIVE);
        // KYC starts PENDING - student cannot use features until admin verifies
        user.setVerificationStatus(VerificationStatus.PENDING);
        user.setCreatedAt(Instant.now());
        userRepo.save(user);

        // Create the student-specific profile record linked to the user
        Student student = new Student();
        student.setUser(user);
        student.setGender(req.getGender());
        student.setCreatedAt(Instant.now());
        studentRepo.save(student);

        log.info("Student registered: {}", user.getEmail());
    }

    // ── HOSTEL REGISTRATION ────────────────────────────────────────────────
    @Override
    @Transactional
    public void registerHostel(HostelRegistrationRequest req) {

        if (userRepo.existsByEmail(req.getEmail()))
            throw new EmailAlreadyExistsException("An account with this email already exists");

        if (userRepo.existsByPhone(req.getPhone()))
            throw new BusinessException("An account with this phone number already exists");

        User user = new User();
        user.setFullName(req.getOwnerName());
        user.setEmail(req.getEmail().toLowerCase().trim());
        user.setPhone(req.getPhone());
        user.setRole(UserRole.HOSTEL);
        user.setPasswordHash(encoder.encode(req.getPassword()));
        user.setStatus(AccountStatus.ACTIVE);
        user.setVerificationStatus(VerificationStatus.PENDING);
        user.setCreatedAt(Instant.now());
        User savedUser = userRepo.save(user);

        Hostel hostel = new Hostel();
        hostel.setUser(savedUser);
        hostel.setHostelName(req.getHostelName());
        hostel.setOwnerName(req.getOwnerName());
        hostel.setHostelType(req.getHostelType());
        hostel.setVerificationStatus(VerificationStatus.PENDING);
        hostel.setCreatedAt(Instant.now());
        hostelRepo.save(hostel);

        log.info("Hostel registered: {}", user.getEmail());
    }

    // ── LOGIN ──────────────────────────────────────────────────────────────
    @Override
    public AuthResponse login(LoginRequest req) {

        User user = userRepo.findByEmail(req.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BusinessException("Invalid email or password"));

        if (!encoder.matches(req.getPassword(), user.getPasswordHash()))
            throw new BusinessException("Invalid email or password");

        if (user.getStatus() == AccountStatus.BLOCKED)
            throw new BusinessException("Your account has been blocked. Please contact support.");

        String token = jwtService.generateToken(user);

        // kycVerified = true means the user has full access to system features.
        // The frontend uses this to decide whether to redirect to KYC form or dashboard.
        boolean kycVerified = user.getVerificationStatus() == VerificationStatus.VERIFIED;

        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .kycVerified(kycVerified)
                .build();
    }

    // ── FORGOT PASSWORD: STEP 1 - send OTP ────────────────────────────────
    @Override
    public void sendForgotPasswordOtp(ForgotPasswordRequest req) {

        // Verify the email exists before generating an OTP
        User user = userRepo.findByEmail(req.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

        String otp = otpService.generateOtp(user.getEmail());

        // Send OTP via email - async so it doesn't block the response
        emailService.sendOtp(user.getEmail(), otp);

        log.info("Forgot password OTP sent to: {}", user.getEmail());
    }

    // ── FORGOT PASSWORD: STEP 2 - verify OTP ──────────────────────────────
    @Override
    public boolean verifyOtp(VerifyOtpRequest req) {
        // peekOtp checks the OTP WITHOUT consuming (removing) it from the store.
        // This is critical: if we called validateOtp here it would delete the OTP,
        // and step 3 (resetPasswordWithOtp) would then fail with "invalid OTP".
        return otpService.peekOtp(req.getEmail(), req.getOtp());
    }

    // ── FORGOT PASSWORD: STEP 3 - set new password ────────────────────────
    @Override
    @Transactional
    public void resetPasswordWithOtp(ResetPasswordWithOtpRequest req) {

        // Re-validate the OTP - this also removes it from the store after checking
        boolean valid = otpService.validateOtp(req.getEmail(), req.getOtp());
        if (!valid) throw new BusinessException("Invalid or expired OTP. Please request a new one.");

        User user = userRepo.findByEmail(req.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

        user.setPasswordHash(encoder.encode(req.getNewPassword()));
        userRepo.save(user);

        log.info("Password reset successfully for: {}", user.getEmail());
    }
}
