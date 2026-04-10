package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.UpdateSiteContentRequest;
import com.fyp.HostelMate.entity.SiteContent;
import com.fyp.HostelMate.repository.SiteContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Year;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiteContentServiceImpl {

    private final SiteContentRepository repo;

    // Always returns the content row - creates defaults if the row doesn't exist yet.
    public SiteContent getSiteContent() {
        return repo.findById(1L).orElseGet(this::createDefaults);
    }

    // Partial update - only non-null fields in the request are applied.
    public SiteContent updateSiteContent(UpdateSiteContentRequest req) {
        SiteContent sc = getSiteContent();

        if (req.getHeroTitle() != null)       sc.setHeroTitle(req.getHeroTitle());
        if (req.getHeroSubtitle() != null)    sc.setHeroSubtitle(req.getHeroSubtitle());
        if (req.getHeroButtonText() != null)  sc.setHeroButtonText(req.getHeroButtonText());
        if (req.getAboutTitle() != null)      sc.setAboutTitle(req.getAboutTitle());
        if (req.getAboutDescription() != null) sc.setAboutDescription(req.getAboutDescription());
        if (req.getFeaturesJson() != null)    sc.setFeaturesJson(req.getFeaturesJson());
        if (req.getContactPhone() != null)    sc.setContactPhone(req.getContactPhone());
        if (req.getContactEmail() != null)    sc.setContactEmail(req.getContactEmail());
        if (req.getContactAddress() != null)  sc.setContactAddress(req.getContactAddress());
        if (req.getFooterTagline() != null)   sc.setFooterTagline(req.getFooterTagline());
        if (req.getFooterCopyright() != null) sc.setFooterCopyright(req.getFooterCopyright());
        if (req.getSystemLogoUrl() != null)   sc.setSystemLogoUrl(req.getSystemLogoUrl());

        sc.setUpdatedAt(Instant.now());
        SiteContent saved = repo.save(sc);
        log.info("Homepage content updated by admin");
        return saved;
    }

    private SiteContent createDefaults() {
        SiteContent sc = new SiteContent();
        sc.setId(1L);
        sc.setHeroTitle("Find Your Perfect Hostel");
        sc.setHeroSubtitle("Discover comfortable, affordable hostels near your college. Apply online, pay digitally, and manage your stay all in one place.");
        sc.setHeroButtonText("Browse Hostels");
        sc.setAboutTitle("Why Choose HostelMate?");
        sc.setAboutDescription("We connect students with verified hostels, making the search for accommodation simple, transparent, and stress-free.");
        sc.setFeaturesJson("[{\"title\":\"Verified Hostels\",\"description\":\"Every hostel is KYC-verified by admin before listing.\",\"icon\":\"Shield\"},{\"title\":\"Easy Payments\",\"description\":\"Pay fees via Khalti or cash. Track every payment.\",\"icon\":\"CreditCard\"},{\"title\":\"Stay Connected\",\"description\":\"Get real-time notifications for events and updates.\",\"icon\":\"Bell\"}]");
        sc.setContactEmail("info@hostelmate.com");
        sc.setContactPhone("+977-9800000000");
        sc.setContactAddress("Kathmandu, Nepal");
        sc.setFooterTagline("Making hostel life better for students across Nepal.");
        sc.setFooterCopyright("© " + Year.now().getValue() + " HostelMate. All rights reserved.");
        sc.setUpdatedAt(Instant.now());
        return repo.save(sc);
    }
}
