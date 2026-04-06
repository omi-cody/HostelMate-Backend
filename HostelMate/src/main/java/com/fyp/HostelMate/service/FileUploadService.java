package com.fyp.HostelMate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Saves uploaded files to the local project directory.
 * Folder and filename are auto-generated based on user identity and file type.
 *
 * Directory structure:
 *   uploads/
 *     students/{studentId}/profile/profile_{studentName}.{ext}
 *     students/{studentId}/documents/document_{docType}_{studentName}.{ext}
 *     hostels/{hostelId}/logo/logo_{hostelName}.{ext}
 *     hostels/{hostelId}/registration/registration_{hostelName}.{ext}
 *     hostels/{hostelId}/identity/identity_{hostelName}.{ext}
 *     hostels/{hostelId}/photos/photo_{index}_{hostelName}.{ext}
 *
 * Files are served at: http://localhost:9091/uploads/...
 */
@Slf4j
@Service
public class FileUploadService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:9091}")
    private String baseUrl;

    /**
     * Core upload method.
     *
     * @param file        the uploaded file
     * @param subFolder   path under uploads/ — e.g. "students/abc-uuid/profile"
     * @param filename    full filename without extension — e.g. "profile_john_doe"
     * @return            public URL to access the file
     */
    public String upload(MultipartFile file, String subFolder, String filename) {
        if (file == null || file.isEmpty()) return null;

        try {
            Path targetDir = Paths.get(uploadDir, subFolder).toAbsolutePath();
            Files.createDirectories(targetDir);

            String extension = extractExtension(file.getOriginalFilename());
            String safeFilename = sanitize(filename) + extension;

            Path targetPath = targetDir.resolve(safeFilename);

            // If a file with this name already exists (re-upload), overwrite it
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = baseUrl + "/uploads/" + subFolder + "/" + safeFilename;
            log.info("File saved → {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("File upload failed: {}", e.getMessage());
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }

    /**
     * Delete a file given its full URL.
     */
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        try {
            String relative = fileUrl.replace(baseUrl + "/uploads/", "");
            Path filePath = Paths.get(uploadDir, relative).toAbsolutePath();
            Files.deleteIfExists(filePath);
            log.info("File deleted → {}", filePath);
        } catch (IOException e) {
            log.warn("File deletion failed for {}: {}", fileUrl, e.getMessage());
        }
    }

    // ── Convenience methods called by services ─────────────────────────────

    /** Student profile picture: uploads/students/{id}/profile/profile_{name}.ext */
    public String uploadStudentProfile(MultipartFile file, String studentId, String studentName) {
        return upload(file,
                "students/" + studentId + "/profile",
                "profile_" + studentName);
    }

    /** Student document: uploads/students/{id}/documents/document_{docType}_{name}.ext */
    public String uploadStudentDocument(MultipartFile file, String studentId, String studentName, String docType) {
        return upload(file,
                "students/" + studentId + "/documents",
                "document_" + docType + "_" + studentName);
    }

    /** Hostel logo: uploads/hostels/{id}/logo/logo_{hostelName}.ext */
    public String uploadHostelLogo(MultipartFile file, String hostelId, String hostelName) {
        return upload(file,
                "hostels/" + hostelId + "/logo",
                "logo_" + hostelName);
    }

    /** Hostel registration doc: uploads/hostels/{id}/registration/registration_{hostelName}.ext */
    public String uploadHostelRegistration(MultipartFile file, String hostelId, String hostelName) {
        return upload(file,
                "hostels/" + hostelId + "/registration",
                "registration_" + hostelName);
    }

    /** Hostel identity doc: uploads/hostels/{id}/identity/identity_{hostelName}.ext */
    public String uploadHostelIdentity(MultipartFile file, String hostelId, String hostelName) {
        return upload(file,
                "hostels/" + hostelId + "/identity",
                "identity_" + hostelName);
    }

    /** Hostel gallery photo: uploads/hostels/{id}/photos/photo_{index}_{hostelName}.ext */
    public String uploadHostelPhoto(MultipartFile file, String hostelId, String hostelName, int index) {
        return upload(file,
                "hostels/" + hostelId + "/photos",
                "photo_" + index + "_" + hostelName);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private String extractExtension(String originalFilename) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        return "";
    }

    /**
     * Sanitize a string for use as a filename:
     * - lowercase
     * - spaces → underscores
     * - remove anything that isn't alphanumeric, underscore, or hyphen
     */
    private String sanitize(String input) {
        if (input == null) return "file";
        return input.trim()
                .toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_\\-]", "");
    }
}
