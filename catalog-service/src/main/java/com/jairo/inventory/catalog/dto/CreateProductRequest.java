package com.jairo.inventory.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @DecimalMin("0.0") BigDecimal unitPrice,
        @NotNull UUID categoryId
) {
}
