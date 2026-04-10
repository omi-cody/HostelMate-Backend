package com.fyp.HostelMate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

// Hostel fills this out after registration. Includes address, PAN, photos, pricing, and meal plan.
// File uploads are done first via /api/upload, then the returned URLs are sent here.
@Data
public class HostelKycRequest {

    // Hostel branding and basic info
    private String logoUrl;

    @NotNull(message = "Admission fee is required")
    @Positive(message = "Admission fee must be a positive amount")
    private BigDecimal admissionFee;

    @NotNull(message = "Established year is required")
    private Integer establishedYear;

    // Address
    @NotBlank(message = "Province is required")
    private String province;

    @NotBlank(message = "District is required")
    private String district;

    @NotBlank(message = "Municipality is required")
    private String municipality;

    @NotBlank(message = "Tole is required")
    private String tole;

    @NotBlank(message = "Ward number is required")
    private String wardNumber;

    // Legal document - cannot be changed once admin verifies
    @NotBlank(message = "PAN number is required")
    private String panNumber;

    @NotBlank(message = "PAN document photo is required")
    private String panDocumentUrl;

    // Up to 4 photos of the hostel - stored as a list of URLs
    private List<String> hostelPhotoUrls;

    // Room pricing - list of {roomType, monthlyPrice} pairs
    private List<RoomPricingRequest> roomPricings;

    // Comma-separated list of amenities (e.g. "WiFi, Hot Water, Laundry, Parking")
    private String amenities;

    // Rules and regulations text
    private String rulesAndRegulations;

    // Meal plan for each day of the week
    private List<MealPlanRequest> mealPlans;

    // Nested DTO for room pricing entries
    @Data
    public static class RoomPricingRequest {
        private String roomType;  // SINGLE, DOUBLE, TRIPLE, QUAD
        @Positive
        private BigDecimal monthlyPrice;
    }

    // Nested DTO for one day's meal plan
    @Data
    public static class MealPlanRequest {
        private String dayOfWeek;  // SUNDAY, MONDAY, ... SATURDAY
        private String morningBreakfast;
        private String lunch;
        private String eveningSnack;
        private String dinner;
    }
}
