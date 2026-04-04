package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import com.fyp.HostelMate.repository.HostelRepository;
import com.fyp.HostelMate.repository.UserRepository;
import com.fyp.HostelMate.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final HostelRepository hostelRepository;

    @Autowired
    public AdminServiceImpl(UserRepository userRepository, HostelRepository hostelRepository) {
        this.userRepository = userRepository;
        this.hostelRepository = hostelRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User approveUser(UUID userId, VerificationStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerificationStatus(status);
        return userRepository.save(user);
    }

    @Override
    public Hostel approveHostel(UUID hostelId, VerificationStatus status) {
        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() -> new RuntimeException("Hostel not found"));
        hostel.setVerificationStatus(status);
        return hostelRepository.save(hostel);
    }
}
