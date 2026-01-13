package com.fyp.HostelMate.dto.request;

import com.fyp.HostelMate.entity.enums.GenderType;
import jakarta.validation.constraints.*;
import lombok.*;


@Data
public class StudentRegistrationRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 5, max=100, message = "Full Name must be between 2 and 100 characters")
    private String fullName;

    @Email(message = "Invalid email format")
    @NotBlank
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull
    private GenderType gender;
}
