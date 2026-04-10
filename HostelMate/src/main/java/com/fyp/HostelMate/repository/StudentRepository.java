package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    // Used throughout services to load the student profile from the JWT email claim
    Optional<Student> findByUser_Email(String email);

    Optional<Student> findByUser_UserId(UUID userId);
}
