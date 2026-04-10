package com.fyp.HostelMate.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fyp.HostelMate.entity.enums.DayOfWeekEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

// One row per day of the week. Each hostel has 7 rows (Sun-Sat) once the meal plan is set.
// The hostel can update meals anytime from their profile settings.
@Entity
@Table(name = "meal_plans")
@Getter
@Setter
public class MealPlan {

    @Id
    @GeneratedValue
    @Column(name = "meal_plan_id")
    private UUID mealPlanId;

    @ManyToOne
    @JoinColumn(name = "kyc_id", nullable = false)
    @JsonIgnoreProperties({"mealPlans", "roomPricings", "hostel"})
    private HostelKyc hostelKyc;

    // Day of week: SUNDAY, MONDAY, ... SATURDAY
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeekEnum dayOfWeek;

    // Meal descriptions for the day
    @Column(name = "morning_breakfast")
    private String morningBreakfast;

    @Column(name = "lunch")
    private String lunch;

    @Column(name = "evening_snack")
    private String eveningSnack;

    @Column(name = "dinner")
    private String dinner;
}
