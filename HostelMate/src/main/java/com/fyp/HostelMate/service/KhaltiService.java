package com.fyp.HostelMate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Handles the server-side Khalti payment verification call.
 * Docs: https://docs.khalti.com/khalti-epayment/
 */
@Slf4j
@Service
public class KhaltiService {

    @Value("${khalti.secret-key}")
    private String secretKey;

    @Value("${khalti.verify-url:https://khalti.com/api/v2/payment/verify/}")
    private String verifyUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Verify a Khalti payment token with the Khalti server.
     * Returns true if Khalti confirms the payment is valid.
     */
    public boolean verifyPayment(String token, double expectedAmount) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Key " + secretKey);

            Map<String, Object> body = Map.of(
                    "token",  token,
                    "amount", (long) (expectedAmount * 100) // Khalti uses paisa
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(verifyUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Khalti verification successful for token={}", token);
                return true;
            }
            log.warn("Khalti verification failed — status={}", response.getStatusCode());
            return false;
        } catch (Exception e) {
            log.error("Khalti verification error: {}", e.getMessage());
            return false;
        }
    }
}
