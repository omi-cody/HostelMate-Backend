package com.fyp.HostelMate.entity;

import com.fyp.HostelMate.entity.enums.RoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue
    @Column(name = "room_id")
    private UUID roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    private String roomNumber;
    private Integer capacity;
    private Integer currentOccupancy;

    @Enumerated(EnumType.STRING)
    @Column(name = "roomType_enum")
    private RoomType roomType;

    private Double pricePerMonth;
}
