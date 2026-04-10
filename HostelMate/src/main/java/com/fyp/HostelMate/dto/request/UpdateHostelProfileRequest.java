package com.fyp.HostelMate.dto.request;

import lombok.Data;

import java.util.List;

// Hostel can update these fields at any time from their profile settings.
// PAN number and PAN document photo are locked after admin verification.
@Data
public class UpdateHostelProfileRequest {

    private String logoUrl;
    private String amenities;         // comma-separated list
    private String rulesAndRegulations;

    // Hostel can update photos at any time (stored as JSON array string of URLs)
    private String hostelPhotoUrls;

    // Hostel can update the meal plan at any time
    private List<HostelKycRequest.MealPlanRequest> mealPlans;
}
