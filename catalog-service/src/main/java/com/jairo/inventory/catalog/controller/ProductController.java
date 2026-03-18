package com.jairo.inventory.catalog.controller;

import com.jairo.inventory.catalog.dto.CreateProductRequest;
import com.jairo.inventory.catalog.dto.ProductResponse;
import com.jairo.inventory.catalog.dto.ProductSummaryResponse;
import com.jairo.inventory.catalog.service.ProductService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Flux<ProductResponse> findAll() {
        return productService.findAll();
    }

    @GetMapping("/{productId}")
    public Mono<ProductResponse> findById(@PathVariable UUID productId) {
        return productService.findById(productId);
    }

    @GetMapping("/{productId}/summary")
    public Mono<ProductSummaryResponse> findSummary(@PathVariable UUID productId) {
        return productService.getSummary(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return productService.create(request);
    }
}
