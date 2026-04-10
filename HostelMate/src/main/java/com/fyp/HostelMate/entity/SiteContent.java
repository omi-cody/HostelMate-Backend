package com.fyp.HostelMate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

// Stores all admin-editable content for the public homepage.
// Single-row table - always one record with id = 1.
@Entity
@Table(name = "site_content")
@Getter
@Setter
public class SiteContent {

    @Id
    @Column(name = "id")
    private Long id = 1L;

    // ── HERO ─────────────────────────────────────────────────────────────
    @Column(name = "hero_title", columnDefinition = "TEXT")
    private String heroTitle;

    @Column(name = "hero_subtitle", columnDefinition = "TEXT")
    private String heroSubtitle;

    @Column(name = "hero_button_text")
    private String heroButtonText;

    // ── ABOUT ─────────────────────────────────────────────────────────────
    @Column(name = "about_title")
    private String aboutTitle;

    @Column(name = "about_description", columnDefinition = "TEXT")
    private String aboutDescription;

    // ── FEATURES ─────────────────────────────────────────────────────────
    // JSON: [{ title, description, icon }, ...]
    @Column(name = "features_json", columnDefinition = "TEXT")
    private String featuresJson;

    // ── CONTACT ──────────────────────────────────────────────────────────
    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_address")
    private String contactAddress;

    // ── FOOTER ───────────────────────────────────────────────────────────
    @Column(name = "footer_tagline", columnDefinition = "TEXT")
    private String footerTagline;

    @Column(name = "footer_copyright")
    private String footerCopyright;

    // ── BRANDING ─────────────────────────────────────────────────────────
    @Column(name = "system_logo_url")
    private String systemLogoUrl;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
