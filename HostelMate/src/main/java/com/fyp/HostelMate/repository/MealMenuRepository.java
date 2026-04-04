package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.MealMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MealMenuRepository extends JpaRepository<MealMenu, UUID> {
    List<MealMenu> findByHostelHostelId(UUID hostelId);
}
