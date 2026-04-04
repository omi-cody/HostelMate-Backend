package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.entity.Warden;
import com.fyp.HostelMate.repository.WardenRepository;
import com.fyp.HostelMate.service.WardenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WardenServiceImpl implements WardenService {

    private final WardenRepository wardenRepository;

    @Autowired
    public WardenServiceImpl(WardenRepository wardenRepository) {
        this.wardenRepository = wardenRepository;
    }

    @Override
    public Warden getWardenProfile(UUID wardenId) {
        return wardenRepository.findById(wardenId)
                .orElseThrow(() -> new RuntimeException("Warden not found"));
    }

    @Override
    public Warden updateWardenProfile(UUID wardenId, Warden wardenDetails) {
        Warden existingWarden = getWardenProfile(wardenId);
        existingWarden.setFirstName(wardenDetails.getFirstName());
        existingWarden.setLastName(wardenDetails.getLastName());
        existingWarden.setContactNumber(wardenDetails.getContactNumber());
        existingWarden.setAddress(wardenDetails.getAddress());
        return wardenRepository.save(existingWarden);
    }

    @Override
    public List<Warden> getAllWardens() {
        return wardenRepository.findAll();
    }
}
