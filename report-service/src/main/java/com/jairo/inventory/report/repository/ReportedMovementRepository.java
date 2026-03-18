package com.jairo.inventory.report.repository;

import com.jairo.inventory.report.domain.ReportedMovement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportedMovementRepository extends JpaRepository<ReportedMovement, UUID> {

    List<ReportedMovement> findByProductIdOrderByCreatedAtDesc(UUID productId);
}
