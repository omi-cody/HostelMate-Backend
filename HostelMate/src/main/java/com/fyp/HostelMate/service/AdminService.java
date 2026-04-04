package com.fyp.HostelMate.service;

import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.entity.enums.VerificationStatus;

import java.util.List;
import java.util.UUID;

public interface AdminService {
    List<User> getAllUsers();
    User approveUser(UUID userId, VerificationStatus status);
    Hostel approveHostel(UUID hostelId, VerificationStatus status);
}
