package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.response.ApiResponse;
import com.fyp.HostelMate.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

// Handles file uploads for profile photos, identity documents, and hostel photos.
// The frontend uploads a file here first, gets back a URL,
// and then includes that URL in the KYC submission request body.
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadUtil fileUploadUtil;

    // Upload a student profile photo
    @PostMapping("/profile-photo")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Object>> uploadProfilePhoto(
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = fileUploadUtil.saveFile(file, "profile-photos");
        return ResponseEntity.ok(ApiResponse.success("Photo uploaded",
                Map.of("url", url)));
    }

    // Upload an identity document photo (citizenship, voter ID, etc.)
    @PostMapping("/document")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Object>> uploadDocument(
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = fileUploadUtil.saveFile(file, "documents");
        return ResponseEntity.ok(ApiResponse.success("Document uploaded",
                Map.of("url", url)));
    }

    // Upload hostel logo
    @PostMapping("/hostel-logo")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Object>> uploadHostelLogo(
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = fileUploadUtil.saveFile(file, "hostel-logos");
        return ResponseEntity.ok(ApiResponse.success("Logo uploaded",
                Map.of("url", url)));
    }

    // Upload a hostel photo (up to 4 allowed, call this endpoint separately for each)
    @PostMapping("/hostel-photo")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Object>> uploadHostelPhoto(
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = fileUploadUtil.saveFile(file, "hostel-photos");
        return ResponseEntity.ok(ApiResponse.success("Hostel photo uploaded",
                Map.of("url", url)));
    }

    // Upload hostel PAN document
    @PostMapping("/pan-document")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Object>> uploadPanDocument(
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = fileUploadUtil.saveFile(file, "pan-documents");
        return ResponseEntity.ok(ApiResponse.success("PAN document uploaded",
                Map.of("url", url)));
    }
}
