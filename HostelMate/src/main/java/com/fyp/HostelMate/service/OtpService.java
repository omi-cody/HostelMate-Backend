package com.fyp.HostelMate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

// OTP store for forgot-password flow.
// Uses an in-memory map - fine for single-node deployments.
// For multi-node, replace with Redis.
@Service
@Slf4j
public class OtpService {

    private final Map<String, OtpData> otpStore = new HashMap<>();

    private static final int OTP_EXPIRY_MINUTES = 5;

    // Generate a 6-digit OTP and store it. Replaces any existing OTP for this email.
    public String generateOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(email, new OtpData(otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));
        log.info("Generated OTP for {}: {}", email, otp);
        return otp;
    }

    // CHECK only - does NOT consume (remove) the OTP from the store.
    // Use this for step 2 (verify OTP) so step 3 can still use it.
    public boolean peekOtp(String email, String otp) {
        OtpData data = otpStore.get(email);
        if (data == null) { log.warn("No OTP found for: {}", email); return false; }
        if (data.isExpired()) { otpStore.remove(email); log.warn("OTP expired for: {}", email); return false; }
        boolean match = data.getOtp().equals(otp);
        if (!match) log.warn("Wrong OTP for: {}", email);
        return match;
    }

    // CONSUME - validates AND removes the OTP from the store.
    // Use this for step 3 (reset password) only.
    public boolean validateOtp(String email, String otp) {
        OtpData data = otpStore.get(email);
        if (data == null) { log.warn("No OTP found for: {}", email); return false; }
        if (data.isExpired()) { otpStore.remove(email); log.warn("OTP expired for: {}", email); return false; }
        boolean match = data.getOtp().equals(otp);
        otpStore.remove(email); // Always remove after consumption attempt
        if (match) log.info("OTP consumed successfully for: {}", email);
        else log.warn("Wrong OTP (consumed) for: {}", email);
        return match;
    }

    // Check if a valid (non-expired) OTP exists without consuming it
    public boolean hasValidOtp(String email) {
        OtpData data = otpStore.get(email);
        return data != null && !data.isExpired();
    }

    public long getOtpRemainingTime(String email) {
        OtpData data = otpStore.get(email);
        if (data == null || data.isExpired()) return 0;
        return java.time.Duration.between(LocalDateTime.now(), data.getExpiryTime()).getSeconds();
    }

    public void clearOtp(String email) {
        otpStore.remove(email);
    }

    private static class OtpData {
        private final String otp;
        private final LocalDateTime expiryTime;

        OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        String getOtp() { return otp; }
        LocalDateTime getExpiryTime() { return expiryTime; }
        boolean isExpired() { return LocalDateTime.now().isAfter(expiryTime); }
    }
}
