package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.SiteContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Single-row table - always use findById(1L) or save() with id=1.
// No custom queries needed; JPA handles everything.
@Repository
public interface SiteContentRepository extends JpaRepository<SiteContent, Long> {
}
