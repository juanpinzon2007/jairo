package com.jairo.inventory.report.controller;

import com.jairo.inventory.report.dto.MovementReportResponse;
import com.jairo.inventory.report.dto.StockReportResponse;
import com.jairo.inventory.report.service.ReportService;
import com.jairo.inventory.shared.security.AuthenticatedUser;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/stock-summary")
    public Mono<StockReportResponse> stockSummary(@AuthenticationPrincipal AuthenticatedUser user) {
        return reportService.buildStockReport(user);
    }

    @GetMapping("/movements/{productId}")
    public Mono<MovementReportResponse> movementHistory(@PathVariable UUID productId,
                                                        @AuthenticationPrincipal AuthenticatedUser user) {
        return reportService.buildMovementReport(productId, user);
    }
}
