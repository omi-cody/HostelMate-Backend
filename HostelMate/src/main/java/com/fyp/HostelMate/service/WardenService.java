package com.fyp.HostelMate.service;

import com.fyp.HostelMate.entity.Warden;
import java.util.List;
import java.util.UUID;

public interface WardenService {
    Warden getWardenProfile(UUID wardenId);
    Warden updateWardenProfile(UUID wardenId, Warden wardenDetails);
    List<Warden> getAllWardens();
}
