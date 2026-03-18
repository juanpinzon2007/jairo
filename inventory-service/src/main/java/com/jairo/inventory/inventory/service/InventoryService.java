package com.jairo.inventory.inventory.service;

import com.jairo.inventory.inventory.client.CatalogClient;
import com.jairo.inventory.inventory.domain.InventoryMovement;
import com.jairo.inventory.inventory.domain.MovementType;
import com.jairo.inventory.inventory.domain.ProductStock;
import com.jairo.inventory.inventory.dto.CreateMovementRequest;
import com.jairo.inventory.inventory.dto.MovementResponse;
import com.jairo.inventory.inventory.dto.StockResponse;
import com.jairo.inventory.inventory.event.InventoryMovementEventPublisher;
import com.jairo.inventory.inventory.repository.InventoryMovementRepository;
import com.jairo.inventory.inventory.repository.ProductStockRepository;
import com.jairo.inventory.shared.events.InventoryMovementEvent;
import com.jairo.inventory.shared.security.AuthenticatedUser;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class InventoryService {

    private final InventoryMovementRepository movementRepository;
    private final ProductStockRepository stockRepository;
    private final CatalogClient catalogClient;
    private final TransactionTemplate transactionTemplate;
    private final InventoryMovementEventPublisher eventPublisher;

    public InventoryService(InventoryMovementRepository movementRepository,
                            ProductStockRepository stockRepository,
                            CatalogClient catalogClient,
                            TransactionTemplate transactionTemplate,
                            InventoryMovementEventPublisher eventPublisher) {
        this.movementRepository = movementRepository;
        this.stockRepository = stockRepository;
        this.catalogClient = catalogClient;
        this.transactionTemplate = transactionTemplate;
        this.eventPublisher = eventPublisher;
    }

    public Mono<MovementResponse> registerMovement(CreateMovementRequest request, AuthenticatedUser user) {
        return Mono.fromCallable(() -> transactionTemplate.execute(status -> {
                    catalogClient.findProduct(request.productId());
                    ProductStock stock = stockRepository.findById(request.productId()).orElseGet(() -> {
                        ProductStock created = new ProductStock();
                        created.setProductId(request.productId());
                        created.setCurrentStock(0L);
                        return created;
                    });

                    long newStock = request.movementType() == MovementType.ENTRY
                            ? stock.getCurrentStock() + request.quantity()
                            : stock.getCurrentStock() - request.quantity();

                    if (request.movementType() == MovementType.EXIT && newStock < 0) {
                        throw new AccessDeniedException("No hay stock suficiente para registrar la salida");
                    }

                    stock.setCurrentStock(newStock);
                    ProductStock savedStock = stockRepository.save(stock);

                    InventoryMovement movement = new InventoryMovement();
                    movement.setProductId(request.productId());
                    movement.setMovementType(request.movementType());
                    movement.setQuantity(request.quantity());
                    movement.setReference(request.reference());
                    movement.setNotes(request.notes());
                    movement.setPerformedBy(user.fullName());
                    InventoryMovement savedMovement = movementRepository.save(movement);

                    return new MovementRegistrationResult(toResponse(savedMovement), toEvent(savedMovement, savedStock));
                }))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(result -> eventPublisher.publish(result.event()).thenReturn(result.response()));
    }

    public Mono<StockResponse> getStock(UUID productId) {
        return Mono.fromCallable(() -> {
                    ProductStock stock = stockRepository.findById(productId).orElseGet(() -> {
                        ProductStock empty = new ProductStock();
                        empty.setProductId(productId);
                        empty.setCurrentStock(0L);
                        return empty;
                    });
                    return new StockResponse(stock.getProductId(), stock.getCurrentStock(), stock.getUpdatedAt());
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<MovementResponse> getMovementsByProduct(UUID productId) {
        return Mono.fromCallable(() -> movementRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                        .map(this::toResponse)
                        .toList())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    private MovementResponse toResponse(InventoryMovement movement) {
        return new MovementResponse(
                movement.getId(),
                movement.getProductId(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getReference(),
                movement.getNotes(),
                movement.getPerformedBy(),
                movement.getCreatedAt()
        );
    }

    private InventoryMovementEvent toEvent(InventoryMovement movement, ProductStock stock) {
        return new InventoryMovementEvent(
                movement.getId(),
                movement.getProductId(),
                movement.getMovementType().name(),
                movement.getQuantity(),
                movement.getReference(),
                movement.getNotes(),
                movement.getPerformedBy(),
                movement.getCreatedAt(),
                stock.getCurrentStock()
        );
    }

    private record MovementRegistrationResult(MovementResponse response, InventoryMovementEvent event) {
    }
}
