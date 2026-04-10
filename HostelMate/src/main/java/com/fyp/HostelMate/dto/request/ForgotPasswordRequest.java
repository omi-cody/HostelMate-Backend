package com.fyp.HostelMate.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Step 1 of the forgot password flow - user enters their registered email.
// We verify it exists in the database before generating and sending an OTP.
@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
