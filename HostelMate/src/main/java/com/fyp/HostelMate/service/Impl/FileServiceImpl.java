package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.exceptions.BadRequestException;
import com.fyp.HostelMate.service.FileService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created base upload directory: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to create base upload directory", e);
            throw new RuntimeException("Failed to initialize file storage", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String userDirectory, String subDirectory, String filePrefix) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        try {
            // Build path: uploads/{userId}/{subDirectory}/
            Path uploadPath = Paths.get(uploadDir, userDirectory, subDirectory);

            // Create directories if they don't exist
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created directory: {}", uploadPath.toAbsolutePath());
            }

            // Generate filename: {prefix}_{originalName}_{UUID}.{ext}
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // Clean prefix and create unique filename
            String safePrefix = filePrefix.replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = safePrefix + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path: /uploads/{userId}/{subDirectory}/{filename}
            String relativePath = "/uploads/" + userDirectory + "/" + subDirectory + "/" + fileName;
            log.info("File uploaded: {} -> {}", file.getOriginalFilename(), relativePath);

            return relativePath;

        } catch (IOException e) {
            log.error("Failed to upload file for user {}: {}", userDirectory, e.getMessage(), e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Remove leading /uploads/ to get relative path
            String relativePath = fileUrl.startsWith("/uploads/")
                    ? fileUrl.substring(9)
                    : fileUrl;

            Path filePath = Paths.get(uploadDir, relativePath);
            boolean deleted = Files.deleteIfExists(filePath);

            if (deleted) {
                log.info("Deleted file: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        return "/uploads/" + fileName;
    }

    @Override
    public boolean validateFile(MultipartFile file, long maxSize, List<String> allowedTypes) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Check file size
        if (file.getSize() > maxSize) {
            throw new BadRequestException("File size exceeds " + (maxSize / 1024 / 1024) + "MB");
        }

        // Check file type
        String contentType = file.getContentType();
        if (allowedTypes != null && !allowedTypes.isEmpty()) {
            // Also check by extension as fallback
            String originalFilename = file.getOriginalFilename();
            boolean validType = false;

            if (contentType != null && allowedTypes.contains(contentType)) {
                validType = true;
            } else if (originalFilename != null) {
                String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
                for (String allowed : allowedTypes) {
                    if (allowed.toLowerCase().contains(ext)) {
                        validType = true;
                        break;
                    }
                }
            }

            if (!validType) {
                throw new BadRequestException("Invalid file type. Allowed: " + allowedTypes);
            }
        }

        return true;
    }
}