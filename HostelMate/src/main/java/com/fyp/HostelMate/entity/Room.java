package com.fyp.HostelMate.entity;

import com.fyp.HostelMate.entity.enums.RoomType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue
    @Column(name = "room_id")
    private UUID roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Column(name = "floor")
    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;

    /** How many beds are currently occupied */
    @Column(name = "occupied_count", nullable = false)
    private Integer occupiedCount = 0;

    /** Derived: roomType capacity minus occupiedCount */
    @Transient
    public int getAvailableBeds() {
        int capacity = switch (roomType) {
            case SINGLE -> 1;
            case DOUBLE -> 2;
            case TRIPLE -> 3;
            case QUAD   -> 4;
        };
        return Math.max(0, capacity - occupiedCount);
    }

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
