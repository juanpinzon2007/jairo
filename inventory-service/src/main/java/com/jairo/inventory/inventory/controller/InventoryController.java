package com.jairo.inventory.inventory.controller;

import com.jairo.inventory.inventory.dto.CreateMovementRequest;
import com.jairo.inventory.inventory.dto.MovementResponse;
import com.jairo.inventory.inventory.dto.StockResponse;
import com.jairo.inventory.inventory.service.InventoryService;
import com.jairo.inventory.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/api/movements")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public Mono<MovementResponse> register(@Valid @RequestBody CreateMovementRequest request,
                                           @AuthenticationPrincipal AuthenticatedUser user) {
        return inventoryService.registerMovement(request, user);
    }

    @GetMapping("/api/stocks/{productId}")
    public Mono<StockResponse> getStock(@PathVariable UUID productId) {
        return inventoryService.getStock(productId);
    }

    @GetMapping("/api/movements/product/{productId}")
    public Flux<MovementResponse> getProductMovements(@PathVariable UUID productId) {
        return inventoryService.getMovementsByProduct(productId);
    }
}
