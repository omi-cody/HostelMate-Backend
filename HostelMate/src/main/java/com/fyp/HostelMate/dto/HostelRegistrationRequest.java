package com.fyp.HostelMate.dto;

import com.fyp.HostelMate.enums.HostelType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class HostelRegistrationRequest {

    @Email
    @NotBlank
    private String email;

    @Pattern(regexp = "^[0-9]{10}$")
    private String phone;

    @Size(min = 6)
    private String password;

    @NotBlank
    private String hostelName;

    @NotBlank
    private String ownerName;

    @NotNull
    private HostelType hostelType;
}
