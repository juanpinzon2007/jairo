package com.jairo.inventory.report.repository;

import com.jairo.inventory.report.domain.ReportRequestLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRequestLogRepository extends JpaRepository<ReportRequestLog, UUID> {
}
