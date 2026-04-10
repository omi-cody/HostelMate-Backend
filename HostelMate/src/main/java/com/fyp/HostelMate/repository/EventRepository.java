package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    // Events for the students of a hostel, newest first
    List<Event> findByHostel_HostelIdOrderByEventDateDesc(UUID hostelId);
}
