package com.jairo.inventory.catalog.service;

import com.jairo.inventory.catalog.client.InventoryClient;
import com.jairo.inventory.catalog.domain.Category;
import com.jairo.inventory.catalog.domain.Product;
import com.jairo.inventory.catalog.dto.CreateProductRequest;
import com.jairo.inventory.catalog.dto.ProductResponse;
import com.jairo.inventory.catalog.dto.ProductSummaryResponse;
import com.jairo.inventory.catalog.repository.ProductRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final CategoryService categoryService;
    private final InventoryClient inventoryClient;
    private final TransactionTemplate transactionTemplate;

    public ProductService(ProductRepository repository,
                          CategoryService categoryService,
                          InventoryClient inventoryClient,
                          TransactionTemplate transactionTemplate) {
        this.repository = repository;
        this.categoryService = categoryService;
        this.inventoryClient = inventoryClient;
        this.transactionTemplate = transactionTemplate;
    }

    public Flux<ProductResponse> findAll() {
        return Mono.fromCallable(repository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .flatMap(product -> Mono.fromCallable(() -> toResponse(product))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    public Mono<ProductResponse> create(CreateProductRequest request) {
        return Mono.fromCallable(() -> transactionTemplate.execute(status -> {
                    if (repository.existsBySkuIgnoreCase(request.sku())) {
                        throw new EntityExistsException("Ya existe un producto con ese SKU");
                    }
                    Category category = categoryService.getRequired(request.categoryId());
                    Product product = new Product();
                    product.setSku(request.sku());
                    product.setName(request.name());
                    product.setDescription(request.description());
                    product.setUnitPrice(request.unitPrice());
                    product.setCategory(category);
                    Product saved = repository.save(product);
                    return toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ProductResponse> findById(UUID productId) {
        return Mono.fromCallable(() -> repository.findById(productId)
                        .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado")))
                .subscribeOn(Schedulers.boundedElastic())
                .map(this::toResponse);
    }

    public Mono<ProductSummaryResponse> getSummary(UUID productId) {
        return Mono.fromCallable(() -> repository.findById(productId)
                        .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado")))
                .subscribeOn(Schedulers.boundedElastic())
                .map(product -> new ProductSummaryResponse(product.getId(), product.getSku(), product.getName()));
    }

    private ProductResponse toResponse(Product product) {
        long stock;
        try {
            InventoryClient.ProductStockResponse stockResponse = inventoryClient.getStock(product.getId());
            stock = stockResponse == null ? 0L : stockResponse.currentStock();
        } catch (Exception ex) {
            stock = 0L;
        }
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getUnitPrice(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.isActive(),
                product.getCreatedAt(),
                stock
        );
    }
}
