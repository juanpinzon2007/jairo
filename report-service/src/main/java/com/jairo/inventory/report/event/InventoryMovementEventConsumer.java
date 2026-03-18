package com.jairo.inventory.report.event;

import com.jairo.inventory.report.domain.ReportedMovement;
import com.jairo.inventory.report.domain.ReportedProductStock;
import com.jairo.inventory.report.repository.ReportedMovementRepository;
import com.jairo.inventory.report.repository.ReportedProductStockRepository;
import com.jairo.inventory.shared.events.InventoryMovementEvent;
import java.time.OffsetDateTime;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class InventoryMovementEventConsumer {

    private final ReportedMovementRepository movementRepository;
    private final ReportedProductStockRepository stockRepository;
    private final TransactionTemplate transactionTemplate;

    public InventoryMovementEventConsumer(ReportedMovementRepository movementRepository,
                                          ReportedProductStockRepository stockRepository,
                                          TransactionTemplate transactionTemplate) {
        this.movementRepository = movementRepository;
        this.stockRepository = stockRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.inventory-movements:inventory.movements}",
            groupId = "${spring.kafka.consumer.group-id:report-service}"
    )
    public void consume(InventoryMovementEvent event) {
        transactionTemplate.executeWithoutResult(status -> {
            if (!movementRepository.existsById(event.movementId())) {
                ReportedMovement movement = new ReportedMovement();
                movement.setMovementId(event.movementId());
                movement.setProductId(event.productId());
                movement.setMovementType(event.movementType());
                movement.setQuantity(event.quantity());
                movement.setReference(event.reference());
                movement.setNotes(event.notes());
                movement.setPerformedBy(event.performedBy());
                movement.setCreatedAt(event.createdAt());
                movementRepository.save(movement);
            }

            ReportedProductStock stock = stockRepository.findById(event.productId()).orElseGet(ReportedProductStock::new);
            stock.setProductId(event.productId());
            stock.setCurrentStock(event.currentStock());
            stock.setUpdatedAt(event.createdAt() == null ? OffsetDateTime.now() : event.createdAt());
            stockRepository.save(stock);
        });
    }
}
