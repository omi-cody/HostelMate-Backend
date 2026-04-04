package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.service.HostelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hostels")
public class HostelController {

    private final HostelService hostelService;

    @Autowired
    public HostelController(HostelService hostelService) {
        this.hostelService = hostelService;
    }

    @GetMapping
    public ResponseEntity<List<Hostel>> getAllHostels() {
        return ResponseEntity.ok(hostelService.getAllHostels());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hostel> getHostelById(@PathVariable UUID id) {
        return ResponseEntity.ok(hostelService.getHostelById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Hostel>> searchHostels(@RequestParam(required = false) String keyword, 
                                                      @RequestParam(required = false) String city) {
        return ResponseEntity.ok(hostelService.searchHostels(keyword, city));
    }

    @PostMapping(value = "/{id}/kyc", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> submitKyc(
            @PathVariable UUID id,
            @ModelAttribute com.fyp.HostelMate.dto.request.HostelKycRequest request) {
        try {
            hostelService.submitKyc(id, request);
            return ResponseEntity.ok("Hostel KYC submitted successfully and is now pending verification.");
        } catch (java.io.IOException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload KYC documents: " + e.getMessage());
        }
    }
}
