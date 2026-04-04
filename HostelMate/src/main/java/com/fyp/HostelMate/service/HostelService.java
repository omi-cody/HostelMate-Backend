package com.fyp.HostelMate.service;

import com.fyp.HostelMate.entity.Hostel;
import java.util.List;
import java.util.UUID;

public interface HostelService {
    List<Hostel> getAllHostels();
    Hostel getHostelById(UUID id);
    List<Hostel> searchHostels(String keyword, String city);
    
    void submitKyc(UUID hostelId, com.fyp.HostelMate.dto.request.HostelKycRequest request) throws java.io.IOException;
}
