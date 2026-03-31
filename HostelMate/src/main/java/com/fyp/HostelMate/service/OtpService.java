package com.fyp.HostelMate.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class OtpService {

    private final Map<String, OtpData> otpStore = new HashMap<>();


    // OTP expiry time in minutes
    private static final int OTP_EXPIRY_MINUTES = 5;

    // OTP length
    private static final int OTP_LENGTH = 6;

    /**
     * Generate and store OTP for email
     */
    public String generateOtp(String email) {
        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Store OTP with expiry time
        otpStore.put(email, new OtpData(
                otp,
                LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)
        ));

        log.info("Generated OTP for {}: {}", email, otp);
        return otp;
    }

    /**
     * Validate OTP for email
     */
    public boolean validateOtp(String email, String otp) {
        OtpData otpData = otpStore.get(email);

        if (otpData == null) {
            log.warn("No OTP found for email: {}", email);
            return false;
        }

        if (otpData.isExpired()) {
            log.warn("OTP expired for email: {}", email);
            otpStore.remove(email);
            return false;
        }

        boolean isValid = otpData.getOtp().equals(otp);

        // Remove OTP after validation (whether successful or not)
        otpStore.remove(email);

        if (isValid) {
            log.info("OTP validated successfully for email: {}", email);
        } else {
            log.warn("Invalid OTP for email: {}", email);
        }

        return isValid;
    }

    /**
     * Check OTP without removing it from the store (useful for pre-verification)
     */
    public boolean checkOtpWithoutEvict(String email, String otp) {
        OtpData otpData = otpStore.get(email);

        if (otpData == null) {
            log.warn("No OTP found for email: {}", email);
            return false;
        }

        if (otpData.isExpired()) {
            log.warn("OTP expired for email: {}", email);
            otpStore.remove(email);
            return false;
        }

        return otpData.getOtp().equals(otp);
    }

    /**
     * Get remaining time for OTP in seconds
     */
    public long getOtpRemainingTime(String email) {
        OtpData otpData = otpStore.get(email);
        if (otpData == null || otpData.isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), otpData.getExpiryTime()).getSeconds();
    }

    /**
     * Check if OTP exists and is not expired
     */
    public boolean hasValidOtp(String email) {
        OtpData otpData = otpStore.get(email);
        return otpData != null && !otpData.isExpired();
    }

    /**
     * Clear OTP for email
     */
    public void clearOtp(String email) {
        otpStore.remove(email);
    }

    // Inner class to store OTP data
    private static class OtpData {
        private final String otp;
        private final LocalDateTime expiryTime;

        public OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        public String getOtp() {
            return otp;
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }
}
