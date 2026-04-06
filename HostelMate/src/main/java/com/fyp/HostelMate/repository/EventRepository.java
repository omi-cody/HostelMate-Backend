package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByHostel_HostelIdOrderByEventDateAsc(UUID hostelId);

    /** Upcoming events for a hostel (for student notification dispatch) */
    List<Event> findByHostel_HostelIdAndEventDateAfterOrderByEventDateAsc(
            UUID hostelId, LocalDateTime after);
}
