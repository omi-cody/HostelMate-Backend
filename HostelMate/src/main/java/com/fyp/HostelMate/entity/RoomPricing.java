package com.fyp.HostelMate.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fyp.HostelMate.entity.enums.RoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

// Stores the monthly price for each room type a hostel offers.
// A hostel can have pricing for single, double, triple, and quad rooms.
// Not all room types need to be filled - only the ones the hostel provides.
@Entity
@Table(name = "room_pricings")
@Getter
@Setter
public class RoomPricing {

    @Id
    @GeneratedValue
    @Column(name = "pricing_id")
    private UUID pricingId;

    @ManyToOne
    @JoinColumn(name = "kyc_id", nullable = false)
    @JsonIgnoreProperties({"roomPricings", "mealPlans", "hostel"})
    private HostelKyc hostelKyc;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;  // SINGLE, DOUBLE, TRIPLE, QUAD

    // Monthly price in NPR
    @Column(name = "monthly_price", nullable = false)
    private BigDecimal monthlyPrice;
}
