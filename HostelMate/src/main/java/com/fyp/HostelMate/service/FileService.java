package com.fyp.HostelMate.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileService {
    String saveFile(MultipartFile file, String folder) throws IOException;
    void deleteFile(String filePath) throws IOException;
}
