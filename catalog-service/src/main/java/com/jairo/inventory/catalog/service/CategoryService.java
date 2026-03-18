package com.jairo.inventory.catalog.service;

import com.jairo.inventory.catalog.domain.Category;
import com.jairo.inventory.catalog.dto.CategoryResponse;
import com.jairo.inventory.catalog.dto.CreateCategoryRequest;
import com.jairo.inventory.catalog.repository.CategoryRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final TransactionTemplate transactionTemplate;

    public CategoryService(CategoryRepository repository, TransactionTemplate transactionTemplate) {
        this.repository = repository;
        this.transactionTemplate = transactionTemplate;
    }

    public Flux<CategoryResponse> findAll() {
        return Mono.fromCallable(() -> repository.findAll().stream()
                        .map(category -> new CategoryResponse(
                                category.getId(),
                                category.getName(),
                                category.getDescription(),
                                category.getCreatedAt()))
                        .toList())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<CategoryResponse> create(CreateCategoryRequest request) {
        return Mono.fromCallable(() -> transactionTemplate.execute(status -> {
                    if (repository.existsByNameIgnoreCase(request.name())) {
                        throw new EntityExistsException("La categoria ya existe");
                    }
                    Category category = new Category();
                    category.setName(request.name());
                    category.setDescription(request.description());
                    Category saved = repository.save(category);
                    return new CategoryResponse(saved.getId(), saved.getName(), saved.getDescription(), saved.getCreatedAt());
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Category getRequired(UUID categoryId) {
        return repository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada"));
    }
}
