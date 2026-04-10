package com.fyp.HostelMate.dto.request;

import lombok.Data;

// Admin sends this to update any part of the homepage.
// All fields are optional - null fields are ignored (partial update).
@Data
public class UpdateSiteContentRequest {

    // Hero section
    private String heroTitle;
    private String heroSubtitle;
    private String heroButtonText;

    // About section
    private String aboutTitle;
    private String aboutDescription;

    // Features - JSON array: [{title, description, icon}]
    private String featuresJson;

    // Contact
    private String contactPhone;
    private String contactEmail;
    private String contactAddress;

    // Footer
    private String footerTagline;
    private String footerCopyright;

    // Branding
    private String systemLogoUrl;
}
