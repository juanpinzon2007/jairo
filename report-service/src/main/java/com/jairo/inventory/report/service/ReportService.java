package com.jairo.inventory.report.service;

import com.jairo.inventory.report.client.CatalogReportClient;
import com.jairo.inventory.report.domain.ReportRequestLog;
import com.jairo.inventory.report.domain.ReportedProductStock;
import com.jairo.inventory.report.dto.MovementReportResponse;
import com.jairo.inventory.report.dto.StockReportItem;
import com.jairo.inventory.report.dto.StockReportResponse;
import com.jairo.inventory.report.repository.ReportRequestLogRepository;
import com.jairo.inventory.report.repository.ReportedMovementRepository;
import com.jairo.inventory.report.repository.ReportedProductStockRepository;
import com.jairo.inventory.shared.security.AuthenticatedUser;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ReportService {

    private final CatalogReportClient catalogClient;
    private final ReportRequestLogRepository logRepository;
    private final ReportedMovementRepository movementRepository;
    private final ReportedProductStockRepository stockRepository;
    private final TransactionTemplate transactionTemplate;

    public ReportService(CatalogReportClient catalogClient,
                         ReportRequestLogRepository logRepository,
                         ReportedMovementRepository movementRepository,
                         ReportedProductStockRepository stockRepository,
                         TransactionTemplate transactionTemplate) {
        this.catalogClient = catalogClient;
        this.logRepository = logRepository;
        this.movementRepository = movementRepository;
        this.stockRepository = stockRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public Mono<StockReportResponse> buildStockReport(AuthenticatedUser user) {
        return logRequest("STOCK_SUMMARY", user.fullName())
                .then(Mono.zip(
                        catalogClient.getProducts().collectList(),
                        Mono.fromCallable(() -> stockRepository.findAll().stream()
                                        .collect(Collectors.toMap(stock -> stock.getProductId(), Function.identity())))
                                .subscribeOn(Schedulers.boundedElastic())
                ))
                .map(tuple -> {
                    Map<UUID, ReportedProductStock> stockIndex = tuple.getT2();
                    var items = tuple.getT1().stream()
                            .map(product -> {
                                ReportedProductStock projection = stockIndex.get(product.id());
                                long stock = projection == null ? product.stock() : projection.getCurrentStock();
                                return new StockReportItem(
                                        product.id(),
                                        product.sku(),
                                        product.name(),
                                        product.categoryName(),
                                        product.unitPrice(),
                                        stock
                                );
                            })
                            .toList();
                    long totalUnits = items.stream().mapToLong(StockReportItem::stock).sum();
                    return new StockReportResponse(OffsetDateTime.now(), items.size(), totalUnits, items);
                });
    }

    public Mono<MovementReportResponse> buildMovementReport(UUID productId, AuthenticatedUser user) {
        return logRequest("MOVEMENT_HISTORY", user.fullName())
                .then(Mono.fromCallable(() -> movementRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                                .map(movement -> new MovementReportResponse.MovementItem(
                                        movement.getMovementId(),
                                        movement.getMovementType(),
                                        movement.getQuantity(),
                                        movement.getReference(),
                                        movement.getNotes(),
                                        movement.getPerformedBy(),
                                        movement.getCreatedAt()))
                                .toList())
                        .subscribeOn(Schedulers.boundedElastic()))
                .map(items -> new MovementReportResponse(productId, OffsetDateTime.now(), items));
    }

    private Mono<Void> logRequest(String type, String userName) {
        return Mono.fromRunnable(() -> transactionTemplate.executeWithoutResult(status -> {
                    ReportRequestLog log = new ReportRequestLog();
                    log.setReportType(type);
                    log.setRequestedBy(userName);
                    logRepository.save(log);
                }))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
