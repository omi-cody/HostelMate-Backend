package com.fyp.HostelMate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "meal_menus")
@Getter
@Setter
public class MealMenu {

    @Id
    @GeneratedValue
    @Column(name = "menu_id")
    private UUID menuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    private String dayOfWeek;
    private String breakfast;
    private String lunch;
    private String snacks;
    private String dinner;
}
