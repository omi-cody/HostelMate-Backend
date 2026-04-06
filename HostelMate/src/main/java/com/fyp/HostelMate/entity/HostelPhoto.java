package com.fyp.HostelMate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "hostel_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HostelPhoto {

    @Id
    @GeneratedValue
    @Column(name = "photo_id")
    private UUID photoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @Column(name = "photo_url", nullable = false)
    private String photoUrl;

}