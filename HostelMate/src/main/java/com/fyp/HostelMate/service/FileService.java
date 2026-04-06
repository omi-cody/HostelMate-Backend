package com.fyp.HostelMate.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    String uploadFile(MultipartFile file, String userDirectory, String subDirectory, String filePrefix);    void deleteFile(String fileUrl);
    String getFileUrl(String fileName);
    boolean validateFile(MultipartFile file, long maxSize, List<String> allowedTypes);
}