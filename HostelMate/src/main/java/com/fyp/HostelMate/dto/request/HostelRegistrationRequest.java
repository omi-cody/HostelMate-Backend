package com.fyp.HostelMate.dto.request;

import com.fyp.HostelMate.entity.enums.HostelType;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class HostelRegistrationRequest {

    @Email
    @NotBlank
    private String email;

    @Pattern(regexp = "^[0-9]{10}$")
    private String phone;

    @Size(min = 6, message = "Password must be greater than 6 character")
    private String password;

    @NotBlank
    private String hostelName;

    @NotBlank
    private String ownerName;

    @NotNull
    private HostelType hostelType;
}
