package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.entity.Student;
import com.fyp.HostelMate.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@Validated

public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // Explicit OPTIONS handler for KYC endpoint
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<Student> getProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(studentService.getStudentProfile(id));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<Student> updateProfile(@PathVariable UUID id, @RequestBody Student studentDetails) {
        return ResponseEntity.ok(studentService.updateStudentProfile(id, studentDetails));
    }

    @PostMapping(value = "/{id}/kyc",
            consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitKyc(
            @PathVariable UUID id,
            @ModelAttribute com.fyp.HostelMate.dto.request.StudentKycRequest request) {
        try {
            studentService.submitKyc(id, request);
            return ResponseEntity.ok("KYC submitted successfully and is now pending verification.");
        } catch (java.io.IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload KYC document: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Print full error to console
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error submitting KYC: " + e.getMessage());
        }
    }
}