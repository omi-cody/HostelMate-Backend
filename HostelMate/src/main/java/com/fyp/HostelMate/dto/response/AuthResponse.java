package com.fyp.HostelMate.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AuthResponse {
    private String id;
    private String token;
    private String role;
    private String fullName;
    private boolean kycVerified;
    private String email;
}
