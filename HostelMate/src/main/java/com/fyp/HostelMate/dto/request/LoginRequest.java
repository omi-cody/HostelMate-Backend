package com.fyp.HostelMate.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;


@Data
public class LoginRequest {

    @Email(message = "Invalid Email Format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "password is required")
    private String password;


}
