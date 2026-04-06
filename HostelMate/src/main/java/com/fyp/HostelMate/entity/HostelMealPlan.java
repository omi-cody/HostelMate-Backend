package com.fyp.HostelMate.entity;

import com.fyp.HostelMate.entity.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "hostel_meal_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HostelMealPlan {

    @Id
    @GeneratedValue
    @Column(name = "meal_plan_id")
    private UUID mealPlanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "breakfast", columnDefinition = "TEXT")
    private String breakfast;

    @Column(name = "lunch", columnDefinition = "TEXT")
    private String lunch;

    @Column(name = "evening_snack", columnDefinition = "TEXT")
    private String eveningSnack;

    @Column(name = "dinner", columnDefinition = "TEXT")
    private String dinner;
}