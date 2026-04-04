package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Warden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WardenRepository extends JpaRepository<Warden, UUID> {
}
