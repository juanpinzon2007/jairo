package com.jairo.inventory.report.repository;

import com.jairo.inventory.report.domain.ReportedProductStock;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportedProductStockRepository extends JpaRepository<ReportedProductStock, UUID> {
}
