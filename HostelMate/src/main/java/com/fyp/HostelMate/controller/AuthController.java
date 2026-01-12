package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.AuthResponse;
import com.fyp.HostelMate.dto.HostelRegistrationRequest;
import com.fyp.HostelMate.dto.LoginRequest;
import com.fyp.HostelMate.dto.StudentRegistrationRequest;
import com.fyp.HostelMate.enums.HostelType;
import com.fyp.HostelMate.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @GetMapping("/test")
    public String test() {
        return "Public endpoint works!";
    }

    @PostMapping("/register/student")
    public ResponseEntity<?> registerStudent(
            @Valid @RequestBody StudentRegistrationRequest req) {
        service.registerStudent(req);
        return ResponseEntity.ok(Map.of("message", "Student registered successfully!"));
    }

    @PostMapping("/register/hostel")
    public ResponseEntity<?> registerHostel(
            @Valid @RequestBody HostelRegistrationRequest req) {
        service.registerHostel(req);
        return ResponseEntity.ok(Map.of("message", "Hostel registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(service.login(req));
    }
}

