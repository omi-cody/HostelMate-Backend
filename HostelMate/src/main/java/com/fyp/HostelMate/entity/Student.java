package com.fyp.HostelMate.entity;

import com.fyp.HostelMate.entity.enums.GenderType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "students")
@Getter
@Setter
public class Student {
    @Id
    @GeneratedValue
    @Column(name = "student_id")
    private UUID studentId ;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_enum")
    private GenderType gender;

    private Instant createdAt;
}
