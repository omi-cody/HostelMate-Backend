package com.fyp.HostelMate.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

// Handles saving uploaded files (profile photos, documents, hostel photos) to disk.
// Files are stored under the 'uploads/' folder in the project root, organized by type.
// The returned URL is the path the frontend uses to fetch the file via /uploads/** endpoint.
@Component
public class FileUploadUtil {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Only allow these image types for security - no PDFs or executables
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // Save a file to a subfolder (e.g. "profile-photos", "documents", "hostel-photos")
    // Returns the relative URL path like "/uploads/profile-photos/uuid-filename.jpg"
    public String saveFile(MultipartFile file, String subFolder) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only JPEG, PNG and WebP images are allowed");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size cannot exceed 5MB");
        }

        // Create the target directory if it doesn't already exist
        Path uploadPath = Paths.get(uploadDir, subFolder);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate a unique filename so two students with the same photo name don't clash
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID() + extension;

        // Write the file bytes to disk
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.write(filePath, file.getBytes());

        // Return a URL path that the frontend can use with the base server URL
        return "/uploads/" + subFolder + "/" + uniqueFilename;
    }

    // Delete a file by its stored URL path (used when a user replaces their photo)
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        try {
            // Strip the leading /uploads/ to get the relative path on disk
            String relativePath = fileUrl.replace("/uploads/", "");
            Path filePath = Paths.get(uploadDir, relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Non-critical - log but don't crash the operation
            System.err.println("Could not delete file: " + fileUrl);
        }
    }
}
