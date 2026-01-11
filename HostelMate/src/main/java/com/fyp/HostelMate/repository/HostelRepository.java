package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Hostel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HostelRepository extends JpaRepository<Hostel, UUID> {

}
