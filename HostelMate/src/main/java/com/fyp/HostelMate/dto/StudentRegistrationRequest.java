package com.fyp.HostelMate.dto;

import com.fyp.HostelMate.enums.GenderType;
import jakarta.validation.constraints.*;
import lombok.*;


@Getter
@Setter
public class StudentRegistrationRequest {

    @NotBlank(message = "Full name is required")
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
