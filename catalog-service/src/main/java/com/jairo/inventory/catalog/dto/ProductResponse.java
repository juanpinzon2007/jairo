package com.jairo.inventory.catalog.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductResponse(UUID id,
                              String sku,
                              String name,
                              String description,
                              BigDecimal unitPrice,
                              UUID categoryId,
                              String categoryName,
                              boolean active,
                              OffsetDateTime createdAt,
                              long stock) {
}
