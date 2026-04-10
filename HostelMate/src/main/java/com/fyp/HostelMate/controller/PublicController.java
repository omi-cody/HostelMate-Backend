package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.response.ApiResponse;
import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.enums.HostelType;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.HostelRepository;
import com.fyp.HostelMate.repository.HostelReviewRepository;
import com.fyp.HostelMate.service.Impl.SiteContentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// Public endpoints that don't require authentication.
// Anyone can browse hostels and see their details and reviews without logging in.
// Login is required only when they click "Apply".
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final HostelRepository hostelRepo;
    private final HostelReviewRepository hostelReviewRepo;
    private final com.fyp.HostelMate.repository.AdmissionRepository admissionRepo;
    private final SiteContentServiceImpl siteContentService;

    // Search verified hostels with optional name and type filters.
    // The frontend passes query params: ?name=sunrise&hostelType=BOYS
    @GetMapping("/hostels")
    public ResponseEntity<ApiResponse<Object>> searchHostels(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String hostelType) {

        HostelType type = null;
        if (hostelType != null && !hostelType.isBlank()) {
            try {
                type = HostelType.valueOf(hostelType.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid hostel type. Use BOYS or GIRLS"));
            }
        }

        var hostels = hostelRepo.searchVerifiedHostels(name, type);
        return ResponseEntity.ok(ApiResponse.success("Hostels found", hostels));
    }

    // Full hostel detail page - shows hostel info, KYC details, and reviews
    @GetMapping("/hostels/{hostelId}")
    public ResponseEntity<ApiResponse<Object>> getHostelDetail(
            @PathVariable UUID hostelId) {

        Hostel hostel = hostelRepo.findById(hostelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found"));

        var reviews = hostelReviewRepo.findByHostel_HostelIdOrderByCreatedAtDesc(hostelId);
        Double avgRating = hostelReviewRepo.getAverageRatingForHostel(hostelId);

        var response = Map.of(
                "hostel", hostel,
                "reviews", reviews,
                "averageRating", avgRating != null ? avgRating : 0.0,
                "totalReviews", reviews.size()
        );

        return ResponseEntity.ok(ApiResponse.success("Hostel detail", response));
    }

    // Public site content for homepage - no auth required
    @GetMapping("/site-content")
    public ResponseEntity<ApiResponse<Object>> getSiteContent() {
        return ResponseEntity.ok(ApiResponse.success("Site content", siteContentService.getSiteContent()));
    }

    // Returns room types with available space for a hostel (used before applying)
    @GetMapping("/hostels/{hostelId}/room-availability")
    public ResponseEntity<ApiResponse<Object>> getRoomAvailability(@PathVariable UUID hostelId) {
        var hostel = hostelRepo.findById(hostelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found"));
        
        Map<String, Object> availability = new java.util.LinkedHashMap<>();
        for (var room : hostel.getRooms()) {
            String type = room.getRoomType().name();
            int capacity = room.getCapacity() != null ? room.getCapacity() : 1;
            List<?> occupants = admissionRepo.findActiveAdmissionsByRoom(room.getRoomId());
            int occupied = occupants.size();
            int available = capacity - occupied;
            
            availability.merge(type, available, (oldVal, newVal) -> (int)oldVal + (int)newVal);
        }
        
        return ResponseEntity.ok(ApiResponse.success("Room availability", availability));
    }
}