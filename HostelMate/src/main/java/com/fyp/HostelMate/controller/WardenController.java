package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.entity.Warden;
import com.fyp.HostelMate.service.WardenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wardens")
public class WardenController {

    private final WardenService wardenService;

    @Autowired
    public WardenController(WardenService wardenService) {
        this.wardenService = wardenService;
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<Warden> getProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(wardenService.getWardenProfile(id));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<Warden> updateProfile(@PathVariable UUID id, @RequestBody Warden wardenDetails) {
        return ResponseEntity.ok(wardenService.updateWardenProfile(id, wardenDetails));
    }

    @GetMapping
    public ResponseEntity<List<Warden>> getAllWardens() {
        return ResponseEntity.ok(wardenService.getAllWardens());
    }
}
