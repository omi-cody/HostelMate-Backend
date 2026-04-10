package com.fyp.HostelMate.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fyp.HostelMate.entity.enums.RoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

// Represents an actual physical room in the hostel.
// Hostel adds rooms after their KYC is verified. Students get assigned to rooms on admission.
// A room cannot be deleted if there are students currently occupying it.
@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue
    @Column(name = "room_id")
    private UUID roomId;

    @ManyToOne
    @JoinColumn(name = "hostel_id", nullable = false)
    @JsonIgnoreProperties({"rooms", "applications", "events", "payments", "reviews", "hostelKyc"})
    private Hostel hostel;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Column(name = "floor", nullable = false)
    private Integer floor;

    // SINGLE means 1 bed, DOUBLE means 2 beds, TRIPLE means 3, QUAD means 4
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;

    // Derived from roomType - set when room is created
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    // Students currently assigned to this room (admitted and not yet left)
    // We check this list before allowing room deletion
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"room", "hostel", "student", "application"})
    private List<Admission> admissions;
}
